package broadway.juegos.blackjack;

import broadway.cartas.Baraja;
import broadway.cartas.Valor;
import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Blackjack con las reglas habituales de casino:
 * <ul>
 *   <li>Zapato de 6 barajas, que se vuelve a barajar cuando queda menos del 25 %.</li>
 *   <li>El blackjack natural paga 3:2; las demás manos ganadoras pagan 1:1.</li>
 *   <li>El crupier pide hasta 16 y se planta en cualquier 17 (incluido el 17 blando).</li>
 *   <li>Si el crupier muestra As o carta de 10, revisa si tiene blackjack antes de que juegues.</li>
 *   <li>Seguro cuando el crupier muestra As: cuesta media apuesta y paga 2:1.</li>
 *   <li>Doblar con las dos primeras cartas, también después de dividir.</li>
 *   <li>Dividir pares hasta 4 manos. Los Ases divididos reciben una sola carta cada uno.</li>
 *   <li>Rendición tardía: con las dos primeras cartas puedes retirarte y recuperar media apuesta.</li>
 * </ul>
 */
public class Blackjack extends Juego {

    public static final int NUMERO_BARAJAS = 6;
    public static final int MAXIMO_MANOS = 4;
    private static final double LIMITE_PARA_BARAJAR = 0.25;

    private final Baraja zapato;

    private Jugador jugador;
    private EstadoBlackjack estado = EstadoBlackjack.ESPERANDO_APUESTA;
    private final List<ManoBlackjack> manos = new ArrayList<>();
    private ManoBlackjack crupier = new ManoBlackjack(0, false, false);
    private int manoActual;
    private boolean cartaOcultaVisible;
    private long seguro;
    private long totalApostado;
    private long totalPagado;
    private String mensaje = "Haz tu apuesta para comenzar.";
    private ResultadoRonda ultimoResultado;
    private boolean zapatoRecienBarajado;

    public Blackjack(Random azar) {
        super("Blackjack", 5_000, 500_000);
        this.zapato = new Baraja(NUMERO_BARAJAS, azar);
    }

    // ================================================================ ronda

    public void iniciarRonda(Jugador jugador, long apuesta) {
        if (estado == EstadoBlackjack.TURNO_JUGADOR || estado == EstadoBlackjack.OFRECIENDO_SEGURO) {
            throw new OperacionInvalidaException("Termina la ronda actual antes de apostar de nuevo.");
        }
        validarApuesta(jugador, apuesta);

        zapatoRecienBarajado = false;
        if (zapato.getFraccionRestante() < LIMITE_PARA_BARAJAR) {
            zapato.rearmarYBarajar();
            zapatoRecienBarajado = true;
        }

        this.jugador = jugador;
        jugador.apostar(apuesta);
        totalApostado = apuesta;
        totalPagado = 0;
        seguro = 0;
        ultimoResultado = null;
        cartaOcultaVisible = false;
        manos.clear();
        manoActual = 0;
        crupier = new ManoBlackjack(0, false, false);

        ManoBlackjack mano = new ManoBlackjack(apuesta, false, false);
        manos.add(mano);
        // Orden real del reparto: jugador, crupier, jugador, crupier (la segunda del crupier boca abajo).
        mano.agregar(zapato.robar());
        crupier.agregar(zapato.robar());
        mano.agregar(zapato.robar());
        crupier.agregar(zapato.robar());

        if (getCartaVisibleCrupier().getValor() == Valor.AS) {
            estado = EstadoBlackjack.OFRECIENDO_SEGURO;
            mensaje = "El crupier muestra un As. ¿Quieres seguro por " + Formato.pesos(apuesta / 2) + "?";
            return;
        }
        revisarBlackjacks();
    }

    /** Responde a la oferta de seguro cuando el crupier muestra un As. */
    public void decidirSeguro(boolean tomarlo) {
        exigirEstado(EstadoBlackjack.OFRECIENDO_SEGURO);
        if (tomarlo) {
            long costo = manos.get(0).getApuesta() / 2;
            if (costo <= 0) {
                throw new OperacionInvalidaException("La apuesta es muy pequeña para asegurarla.");
            }
            if (jugador.getFichas() < costo) {
                throw new OperacionInvalidaException("No tienes fichas suficientes para el seguro (" + Formato.pesos(costo) + ").");
            }
            jugador.apostar(costo);
            seguro = costo;
            totalApostado += costo;
        }
        if (seguro > 0 && crupier.esBlackjack()) {
            totalPagado += seguro * 3; // paga 2:1 y devuelve lo apostado al seguro
        }
        revisarBlackjacks();
    }

    /** El crupier revisa su carta oculta y se resuelven los blackjacks naturales. */
    private void revisarBlackjacks() {
        ManoBlackjack mano = manos.get(0);
        if (crupier.esBlackjack()) {
            cartaOcultaVisible = true;
            mano.terminar();
            if (mano.esBlackjack()) {
                totalPagado += mano.getApuesta();
                mano.setResultado("Empate: los dos tienen blackjack");
            } else {
                mano.setResultado("Pierde: el crupier tiene blackjack");
            }
            mensaje = "El crupier tiene blackjack." + (seguro > 0 ? " El seguro paga 2:1." : "");
            terminarRonda();
            return;
        }
        if (seguro > 0) {
            mensaje = "El crupier no tiene blackjack. Pierdes el seguro.";
        }
        if (mano.esBlackjack()) {
            cartaOcultaVisible = true;
            mano.terminar();
            totalPagado += mano.getApuesta() + mano.getApuesta() * 3 / 2;
            mano.setResultado("¡Blackjack! Paga 3:2");
            mensaje = "¡Blackjack!";
            terminarRonda();
            return;
        }
        estado = EstadoBlackjack.TURNO_JUGADOR;
        if (seguro == 0) {
            mensaje = "Tu turno: pide carta, plántate, dobla" + (mano.esPar() ? ", divide" : "") + " o ríndete.";
        }
    }

    // ======================================================= acciones jugador

    public void pedir() {
        ManoBlackjack mano = exigirTurno();
        mano.agregar(zapato.robar());
        if (mano.sePaso()) {
            mano.setResultado("Se pasó de 21");
            mano.terminar();
        } else if (mano.getValor() == 21) {
            mano.terminar();
        }
        avanzarSiTerminada();
    }

    public void plantarse() {
        ManoBlackjack mano = exigirTurno();
        mano.terminar();
        avanzarSiTerminada();
    }

    public void doblar() {
        ManoBlackjack mano = exigirTurno();
        if (!puedeDoblar()) {
            throw new OperacionInvalidaException("Solo puedes doblar con las dos primeras cartas y si tienes fichas para igualar la apuesta.");
        }
        long extra = mano.getApuesta();
        jugador.apostar(extra);
        totalApostado += extra;
        mano.duplicarApuesta();
        mano.agregar(zapato.robar());
        if (mano.sePaso()) {
            mano.setResultado("Se pasó de 21");
        }
        mano.terminar();
        avanzarSiTerminada();
    }

    public void dividir() {
        ManoBlackjack mano = exigirTurno();
        if (!puedeDividir()) {
            throw new OperacionInvalidaException("Solo puedes dividir un par, hasta " + MAXIMO_MANOS
                    + " manos y si tienes fichas para la nueva apuesta.");
        }
        long apuesta = mano.getApuesta();
        jugador.apostar(apuesta);
        totalApostado += apuesta;

        boolean ases = mano.getCartas().get(0).getValor() == Valor.AS;
        ManoBlackjack primera = new ManoBlackjack(apuesta, true, ases);
        ManoBlackjack segunda = new ManoBlackjack(apuesta, true, ases);
        primera.agregar(mano.getCartas().get(0));
        segunda.agregar(mano.getCartas().get(1));
        primera.agregar(zapato.robar());
        segunda.agregar(zapato.robar());
        manos.set(manoActual, primera);
        manos.add(manoActual + 1, segunda);

        for (ManoBlackjack m : List.of(primera, segunda)) {
            if (ases || m.getValor() == 21) {
                m.terminar(); // los Ases divididos reciben una sola carta
            }
        }
        avanzarSiTerminada();
    }

    /** Rendición tardía: entrega media apuesta y termina la mano. */
    public void rendirse() {
        ManoBlackjack mano = exigirTurno();
        if (!puedeRendirse()) {
            throw new OperacionInvalidaException("Solo puedes rendirte con tus dos primeras cartas, antes de dividir.");
        }
        mano.rendir();
        totalPagado += mano.getApuesta() / 2;
        mano.setResultado("Se rindió: recupera media apuesta");
        avanzarSiTerminada();
    }

    // ============================================================ crupier

    private void avanzarSiTerminada() {
        while (manoActual < manos.size() && manos.get(manoActual).isTerminada()) {
            manoActual++;
        }
        if (manoActual >= manos.size()) {
            juegaCrupier();
        } else {
            mensaje = manos.size() > 1
                    ? "Juegas la mano " + (manoActual + 1) + " de " + manos.size() + "."
                    : "Tu turno.";
        }
    }

    private void juegaCrupier() {
        cartaOcultaVisible = true;
        boolean hayManosVivas = false;
        for (ManoBlackjack m : manos) {
            if (!m.sePaso() && !m.isRendida()) {
                hayManosVivas = true;
                break;
            }
        }
        if (hayManosVivas) {
            // Pide hasta 16 y se planta en cualquier 17.
            while (crupier.getValor() < 17) {
                crupier.agregar(zapato.robar());
            }
        }
        int valorCrupier = crupier.getValor();
        for (ManoBlackjack m : manos) {
            if (m.isRendida() || m.sePaso()) {
                continue;
            }
            int valor = m.getValor();
            if (crupier.sePaso()) {
                totalPagado += m.getApuesta() * 2;
                m.setResultado("Gana: el crupier se pasó");
            } else if (valor > valorCrupier) {
                totalPagado += m.getApuesta() * 2;
                m.setResultado("Gana " + valor + " a " + valorCrupier);
            } else if (valor == valorCrupier) {
                totalPagado += m.getApuesta();
                m.setResultado("Empate a " + valor);
            } else {
                m.setResultado("Pierde " + valor + " a " + valorCrupier);
            }
        }
        mensaje = crupier.sePaso() ? "El crupier se pasó con " + valorCrupier + "."
                : hayManosVivas ? "El crupier se planta con " + valorCrupier + "." : "Ronda terminada.";
        terminarRonda();
    }

    private void terminarRonda() {
        estado = EstadoBlackjack.RONDA_TERMINADA;
        manoActual = Math.min(manoActual, manos.size() - 1);
        ultimoResultado = liquidar(jugador, totalApostado, totalPagado);
    }

    // ======================================================= validaciones

    private void exigirEstado(EstadoBlackjack esperado) {
        if (estado != esperado) {
            throw new OperacionInvalidaException("Esa acción no está disponible en este momento.");
        }
    }

    private ManoBlackjack exigirTurno() {
        exigirEstado(EstadoBlackjack.TURNO_JUGADOR);
        return manos.get(manoActual);
    }

    public boolean puedePedir() {
        return estado == EstadoBlackjack.TURNO_JUGADOR;
    }

    public boolean puedeDoblar() {
        if (estado != EstadoBlackjack.TURNO_JUGADOR) {
            return false;
        }
        ManoBlackjack m = manos.get(manoActual);
        return m.getCartas().size() == 2 && !m.isDivisionDeAses() && jugador.getFichas() >= m.getApuesta();
    }

    public boolean puedeDividir() {
        if (estado != EstadoBlackjack.TURNO_JUGADOR) {
            return false;
        }
        ManoBlackjack m = manos.get(manoActual);
        return m.esPar() && !m.isDivisionDeAses() && manos.size() < MAXIMO_MANOS
                && jugador.getFichas() >= m.getApuesta();
    }

    public boolean puedeRendirse() {
        if (estado != EstadoBlackjack.TURNO_JUGADOR || manos.size() != 1) {
            return false;
        }
        ManoBlackjack m = manos.get(0);
        return m.getCartas().size() == 2 && !m.isVieneDeDivision();
    }

    // ============================================================ consultas

    public EstadoBlackjack getEstado() {
        return estado;
    }

    public List<ManoBlackjack> getManos() {
        return Collections.unmodifiableList(manos);
    }

    public int getIndiceManoActual() {
        return manoActual;
    }

    public ManoBlackjack getCrupier() {
        return crupier;
    }

    public broadway.cartas.Carta getCartaVisibleCrupier() {
        return crupier.getCartas().get(0);
    }

    public boolean isCartaOcultaVisible() {
        return cartaOcultaVisible;
    }

    public long getSeguro() {
        return seguro;
    }

    public String getMensaje() {
        return mensaje;
    }

    public ResultadoRonda getUltimoResultado() {
        return ultimoResultado;
    }

    public boolean isZapatoRecienBarajado() {
        return zapatoRecienBarajado;
    }

    public int getCartasEnZapato() {
        return zapato.getCartasRestantes();
    }

    @Override
    public String getDescripcion() {
        return "Llega a 21 sin pasarte. El blackjack paga 3:2.";
    }

    @Override
    public String getReglas() {
        return "BLACKJACK — 6 barajas\n\n"
                + "Objetivo: sumar más que el crupier sin pasarte de 21.\n"
                + "Las cartas del 2 al 10 valen su número, J, Q y K valen 10 y el As vale 1 u 11.\n\n"
                + "• Blackjack (As + carta de 10 en las dos primeras cartas) paga 3:2.\n"
                + "• Una mano ganadora normal paga 1:1. El empate devuelve la apuesta.\n"
                + "• El crupier pide carta hasta 16 y se planta en cualquier 17.\n"
                + "• Si el crupier muestra As o 10, revisa su carta oculta: si tiene blackjack, la ronda termina.\n"
                + "• Seguro: si el crupier muestra As puedes apostar media apuesta extra; paga 2:1 si tiene blackjack.\n"
                + "• Doblar: con tus dos primeras cartas duplicas la apuesta y recibes exactamente una carta más.\n"
                + "• Dividir: con un par separas las cartas en dos manos, cada una con su apuesta (hasta 4 manos).\n"
                + "  Puedes doblar después de dividir. Los Ases divididos reciben una sola carta y 21 con ellos no es blackjack.\n"
                + "• Rendirse: con tus dos primeras cartas puedes abandonar la mano y recuperar media apuesta.\n\n"
                + "Apuesta mínima " + Formato.pesos(getApuestaMinima()) + ", máxima " + Formato.pesos(getApuestaMaxima()) + ".";
    }
}
