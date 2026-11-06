package broadway.juegos.poker;

import broadway.cartas.Baraja;
import broadway.cartas.Carta;
import broadway.cartas.Palo;
import broadway.cartas.Valor;
import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Texas Hold'em sin límite contra tres rivales de la computadora.
 *
 * Reglas reales: botón rotativo, ciega pequeña y ciega grande, dos cartas
 * privadas, cuatro rondas de apuestas (preflop, flop, turn y river), quema de
 * carta antes de cada calle, subida mínima igual a la última subida, apuestas no
 * igualadas que se devuelven y botes secundarios cuando alguien va all-in.
 *
 * El jugador compra fichas para la mesa al sentarse; al levantarse se le
 * devuelve lo que tenga y se calcula el cashback sobre lo que perdió en la sesión.
 */
public class PokerTexasHoldem extends Juego {

    public static final long CIEGA_PEQUENA = 500;
    public static final long CIEGA_GRANDE = 1_000;
    public static final long FICHAS_RIVAL = 100_000;
    public static final int NUMERO_RIVALES = 3;
    private static final int SIMULACIONES_RIVAL = 600;

    private static final String[] NOMBRES_RIVALES = {
            "Valentina", "Don Arturo", "La Mona", "Checho", "Doña Gloria",
            "El Profe", "Mateo", "Lucía", "Pipe", "Camila"
    };

    private final Random azar;
    private final Baraja baraja;
    private final List<AsientoPoker> asientos = new ArrayList<>();
    private final List<Carta> comunitarias = new ArrayList<>();
    private final List<String> registro = new ArrayList<>();
    private final List<String> resumen = new ArrayList<>();
    private final Set<Integer> ganadores = new HashSet<>();

    private Jugador jugador;
    private FasePoker fase = FasePoker.SIN_SESION;
    private int boton;
    private int turno = -1;
    private long apuestaActual;
    private long subidaMinima;
    private long compradoTotal;
    private boolean cartasDescubiertas;
    private int manosJugadas;

    public PokerTexasHoldem(Random azar) {
        // Los límites del juego son los de la compra de fichas para la mesa.
        super("Póker Texas Hold'em", 20_000, 200_000);
        this.azar = azar;
        this.baraja = new Baraja(1, azar);
    }

    // ================================================================ sesión

    public void sentarse(Jugador jugador, long compra) {
        if (fase != FasePoker.SIN_SESION) {
            throw new OperacionInvalidaException("Ya estás sentado en la mesa.");
        }
        validarApuesta(jugador, compra);
        jugador.apostar(compra);
        this.jugador = jugador;
        compradoTotal = compra;
        manosJugadas = 0;
        asientos.clear();
        registro.clear();
        resumen.clear();
        asientos.add(new AsientoPoker(jugador.getNombre(), null, compra));
        for (int i = 0; i < NUMERO_RIVALES; i++) {
            asientos.add(nuevoRival());
        }
        boton = azar.nextInt(asientos.size());
        fase = FasePoker.ENTRE_MANOS;
        anotar("Te sentaste con " + Formato.pesos(compra) + ". Ciegas " + Formato.pesos(CIEGA_PEQUENA)
                + " / " + Formato.pesos(CIEGA_GRANDE) + ".");
    }

    private AsientoPoker nuevoRival() {
        List<String> libres = new ArrayList<>();
        for (String n : NOMBRES_RIVALES) {
            boolean usado = false;
            for (AsientoPoker a : asientos) {
                if (a.getNombre().equals(n)) {
                    usado = true;
                    break;
                }
            }
            if (!usado) {
                libres.add(n);
            }
        }
        String nombre = libres.get(azar.nextInt(libres.size()));
        EstrategiaRival[] estilos = {new RivalConservador(), new RivalEquilibrado(), new RivalAgresivo()};
        return new AsientoPoker(nombre, estilos[azar.nextInt(estilos.length)], FICHAS_RIVAL);
    }

    /** Agrega fichas a tu puesto entre manos, sin pasar del máximo de compra. */
    public void recomprar(long monto) {
        exigirSesion();
        if (isManoEnCurso()) {
            throw new OperacionInvalidaException("Solo puedes comprar más fichas entre manos.");
        }
        if (monto < CIEGA_GRANDE) {
            throw new OperacionInvalidaException("La recompra mínima es " + Formato.pesos(CIEGA_GRANDE) + ".");
        }
        AsientoPoker humano = getHumano();
        if (humano.getFichas() + monto > getApuestaMaxima()) {
            throw new OperacionInvalidaException("En la mesa puedes tener como máximo " + Formato.pesos(getApuestaMaxima()) + ".");
        }
        if (monto > jugador.getFichas()) {
            throw new OperacionInvalidaException("No tienes fichas suficientes. Compra más en la caja.");
        }
        jugador.apostar(monto);
        humano.agregarFichas(monto);
        compradoTotal += monto;
        anotar("Compraste " + Formato.pesos(monto) + " más para la mesa.");
    }

    /**
     * Te levantas de la mesa. Si hay una mano en curso, abandonas tus cartas
     * y los rivales la terminan. Devuelve el resultado de toda la sesión.
     */
    public ResultadoRonda levantarse() {
        exigirSesion();
        if (isManoEnCurso()) {
            AsientoPoker humano = getHumano();
            if (!humano.isRetirado()) {
                boolean eraSuTurno = turno == 0;
                humano.retirarse();
                humano.setUltimaAccion("Se levantó");
                anotar(humano.getNombre() + ": se levanta de la mesa");
                if (eraSuTurno) {
                    avanzar(0);
                } else if (contarEnMano() == 1) {
                    ganarSinMostrar();
                }
            }
            while (isManoEnCurso()) {
                ejecutarTurnoRival();
            }
        }
        long devuelve = getHumano().getFichas();
        ResultadoRonda resultado = liquidar(jugador, compradoTotal, devuelve);
        fase = FasePoker.SIN_SESION;
        asientos.clear();
        comunitarias.clear();
        jugador = null;
        compradoTotal = 0;
        turno = -1;
        return resultado;
    }

    // ================================================================= mano

    public void nuevaMano() {
        exigirSesion();
        if (isManoEnCurso()) {
            throw new OperacionInvalidaException("La mano actual no ha terminado.");
        }
        if (getHumano().getFichas() == 0) {
            throw new OperacionInvalidaException("Te quedaste sin fichas en la mesa. Recompra o levántate.");
        }
        for (int i = 1; i < asientos.size(); i++) {
            if (asientos.get(i).getFichas() == 0) {
                AsientoPoker nuevo = nuevoRival();
                anotar(asientos.get(i).getNombre() + " se quedó sin fichas y se levantó. Llega " + nuevo.getNombre() + ".");
                asientos.set(i, nuevo);
            }
        }
        for (AsientoPoker a : asientos) {
            a.nuevaMano();
        }
        comunitarias.clear();
        resumen.clear();
        ganadores.clear();
        cartasDescubiertas = false;
        baraja.rearmarYBarajar();
        manosJugadas++;

        boton = siguiente(boton);
        int pequena = siguiente(boton);
        int grande = siguiente(pequena);
        anotar("— Mano #" + manosJugadas + ". Botón: " + asientos.get(boton).getNombre());
        postearCiega(pequena, CIEGA_PEQUENA, "ciega pequeña");
        postearCiega(grande, CIEGA_GRANDE, "ciega grande");

        for (int vuelta = 0; vuelta < 2; vuelta++) {
            int i = pequena;
            for (int k = 0; k < asientos.size(); k++) {
                asientos.get(i).recibir(baraja.robar());
                i = siguiente(i);
            }
        }
        apuestaActual = CIEGA_GRANDE;
        subidaMinima = CIEGA_GRANDE;
        fase = FasePoker.PREFLOP;
        turno = siguienteQueDebeActuar(grande);
        if (turno == -1) {
            cerrarRonda();
        }
    }

    private void postearCiega(int indice, long monto, String nombre) {
        AsientoPoker a = asientos.get(indice);
        long puesto = a.poner(monto);
        a.setUltimaAccion("Ciega " + Formato.pesos(puesto));
        anotar(a.getNombre() + " pone la " + nombre + " de " + Formato.pesos(puesto) + (a.isAllIn() ? " (all-in)" : ""));
    }

    // ============================================================= acciones

    /** Acción del jugador humano. {@code subirA} solo se usa con SUBIR: es la apuesta total a la que sube. */
    public void accionJugador(AccionPoker accion, long subirA) {
        if (!esTurnoJugador()) {
            throw new OperacionInvalidaException("No es tu turno.");
        }
        aplicar(0, accion, subirA);
    }

    /** Hace jugar al rival al que le toca. */
    public void ejecutarTurnoRival() {
        if (!isManoEnCurso() || turno <= 0) {
            throw new OperacionInvalidaException("No es el turno de un rival.");
        }
        EstrategiaRival.Decision decision = decidirRival(turno);
        aplicar(turno, decision.accion(), decision.monto());
    }

    private void aplicar(int indice, AccionPoker accion, long subirA) {
        AsientoPoker a = asientos.get(indice);
        long porIgualar = apuestaActual - a.getApuestaRonda();
        String texto;
        switch (accion) {
            case RETIRARSE:
                a.retirarse();
                texto = "No va";
                break;
            case PASAR:
                if (porIgualar > 0) {
                    throw new OperacionInvalidaException("No puedes pasar: hay una apuesta de "
                            + Formato.pesos(apuestaActual) + ". Iguala, sube o retírate.");
                }
                texto = "Pasa";
                break;
            case IGUALAR:
                if (porIgualar <= 0) {
                    throw new OperacionInvalidaException("No hay nada que igualar: puedes pasar.");
                }
                long puesto = a.poner(porIgualar);
                texto = "Iguala " + Formato.pesos(puesto) + (a.isAllIn() ? " (all-in)" : "");
                break;
            case SUBIR:
                texto = subir(a, subirA);
                break;
            case TODO:
                long total = a.getApuestaRonda() + a.getFichas();
                if (total <= apuestaActual || !a.isPuedeSubir()) {
                    long p = a.poner(Math.max(0, Math.min(porIgualar, a.getFichas())));
                    texto = p > 0 ? "Iguala " + Formato.pesos(p) + (a.isAllIn() ? " (all-in)" : "") : "Pasa";
                } else {
                    texto = subir(a, total);
                }
                break;
            default:
                throw new IllegalStateException();
        }
        a.setHaActuado(true);
        a.setPuedeSubir(false);
        a.setUltimaAccion(texto);
        anotar(a.getNombre() + ": " + texto);
        avanzar(indice);
    }

    private String subir(AsientoPoker a, long objetivo) {
        if (!a.isPuedeSubir()) {
            throw new OperacionInvalidaException("No puedes volver a subir: la última subida fue incompleta. Iguala o retírate.");
        }
        long maximo = a.getApuestaRonda() + a.getFichas();
        if (objetivo > maximo) {
            throw new OperacionInvalidaException("No tienes tantas fichas. Lo máximo es " + Formato.pesos(maximo) + ".");
        }
        if (objetivo <= apuestaActual) {
            throw new OperacionInvalidaException("La subida debe ser mayor que la apuesta actual (" + Formato.pesos(apuestaActual) + ").");
        }
        boolean esAllIn = objetivo == maximo;
        long minimo = getMinimoSubirA();
        if (objetivo < minimo && !esAllIn) {
            throw new OperacionInvalidaException("La subida mínima es a " + Formato.pesos(minimo) + ".");
        }
        long anterior = apuestaActual;
        long incremento = objetivo - apuestaActual;
        a.poner(objetivo - a.getApuestaRonda());
        boolean completa = incremento >= subidaMinima;
        if (completa) {
            subidaMinima = incremento;
        }
        apuestaActual = objetivo;
        for (AsientoPoker otro : asientos) {
            if (otro != a && otro.puedeActuar()) {
                otro.setHaActuado(false);
                if (completa) {
                    otro.setPuedeSubir(true);
                }
            }
        }
        return (anterior == 0 ? "Apuesta " : "Sube a ") + Formato.pesos(objetivo) + (esAllIn ? " (all-in)" : "");
    }

    // ============================================================== flujo

    private void avanzar(int ultimo) {
        if (contarEnMano() == 1) {
            ganarSinMostrar();
            return;
        }
        int sigue = siguienteQueDebeActuar(ultimo);
        if (sigue != -1) {
            turno = sigue;
        } else {
            cerrarRonda();
        }
    }

    private void cerrarRonda() {
        devolverApuestaNoIgualada();
        for (AsientoPoker a : asientos) {
            a.reiniciarRonda();
        }
        apuestaActual = 0;
        subidaMinima = CIEGA_GRANDE;
        switch (fase) {
            case PREFLOP:
                baraja.robar(); // carta quemada
                for (int i = 0; i < 3; i++) {
                    comunitarias.add(baraja.robar());
                }
                fase = FasePoker.FLOP;
                anotar("— Flop: " + cartasTexto(comunitarias));
                break;
            case FLOP:
                baraja.robar();
                comunitarias.add(baraja.robar());
                fase = FasePoker.TURN;
                anotar("— Turn: " + comunitarias.get(3));
                break;
            case TURN:
                baraja.robar();
                comunitarias.add(baraja.robar());
                fase = FasePoker.RIVER;
                anotar("— River: " + comunitarias.get(4));
                break;
            case RIVER:
                mostrarCartas();
                return;
            default:
                throw new IllegalStateException("Fase inesperada " + fase);
        }
        int puedenApostar = 0;
        for (AsientoPoker a : asientos) {
            if (a.puedeActuar()) {
                puedenApostar++;
            }
        }
        if (puedenApostar <= 1) {
            cerrarRonda(); // los demás están all-in: se reparten las cartas que faltan sin apuestas
            return;
        }
        turno = siguienteQueDebeActuar(boton);
    }

    /** Si la apuesta más alta no la igualó nadie, la diferencia vuelve a quien la hizo. */
    private void devolverApuestaNoIgualada() {
        int mayor = -1;
        long maxima = 0;
        long segunda = 0;
        for (int i = 0; i < asientos.size(); i++) {
            long ap = asientos.get(i).getApuestaRonda();
            if (ap > maxima) {
                segunda = maxima;
                maxima = ap;
                mayor = i;
            } else if (ap > segunda) {
                segunda = ap;
            }
        }
        if (mayor >= 0 && maxima > segunda) {
            AsientoPoker a = asientos.get(mayor);
            a.devolver(maxima - segunda);
            anotar("Se le devuelven " + Formato.pesos(maxima - segunda) + " a " + a.getNombre() + " (apuesta no igualada)");
        }
    }

    private void ganarSinMostrar() {
        devolverApuestaNoIgualada();
        int ganador = -1;
        for (int i = 0; i < asientos.size(); i++) {
            if (!asientos.get(i).isRetirado()) {
                ganador = i;
            }
        }
        long bote = getBote();
        asientos.get(ganador).ganar(bote);
        ganadores.add(ganador);
        resumen.add(asientos.get(ganador).getNombre() + " gana " + Formato.pesos(bote) + ": los demás no fueron.");
        anotar(resumen.get(0));
        terminarMano();
    }

    /** Showdown: se descubren las cartas y se reparten el bote principal y los secundarios. */
    private void mostrarCartas() {
        cartasDescubiertas = true;
        List<Integer> enMano = new ArrayList<>();
        for (int i = 0; i < asientos.size(); i++) {
            AsientoPoker a = asientos.get(i);
            if (!a.isRetirado()) {
                List<Carta> siete = new ArrayList<>(a.getCartas());
                siete.addAll(comunitarias);
                a.setManoFinal(EvaluadorPoker.evaluar(siete));
                enMano.add(i);
                anotar(a.getNombre() + " muestra " + cartasTexto(a.getCartas()) + ": " + a.getManoFinal().getDescripcion());
            }
        }
        // Se arman el bote principal y los secundarios, y cada uno se le da a la mejor mano que pueda ganarlo.
        List<Bote> botes = Bote.armar(asientos, enMano);
        List<Integer> ultimosGanadores = new ArrayList<>();
        long repartido = 0;
        for (Bote bote : botes) {
            ultimosGanadores = mejoresManos(bote.getElegibles());
            repartir(bote.getMonto(), ultimosGanadores, bote.getNumero());
            repartido += bote.getMonto();
        }
        long sobrante = getBote() - repartido; // fichas de retirados por encima del último nivel
        if (sobrante > 0 && !ultimosGanadores.isEmpty()) {
            asientos.get(ultimosGanadores.get(0)).ganar(sobrante);
        }
        terminarMano();
    }

    private List<Integer> mejoresManos(List<Integer> elegibles) {
        long mejor = Long.MIN_VALUE;
        List<Integer> ganan = new ArrayList<>();
        for (int i : elegibles) {
            long p = asientos.get(i).getManoFinal().getPuntaje();
            if (p > mejor) {
                mejor = p;
                ganan.clear();
                ganan.add(i);
            } else if (p == mejor) {
                ganan.add(i);
            }
        }
        // Orden desde la izquierda del botón: la ficha impar va al primero.
        ganan.sort((x, y) -> Integer.compare(distanciaDesdeBoton(x), distanciaDesdeBoton(y)));
        return ganan;
    }

    private void repartir(long bote, List<Integer> ganan, int numeroBote) {
        long parte = bote / ganan.size();
        long resto = bote % ganan.size();
        List<String> nombres = new ArrayList<>();
        for (int k = 0; k < ganan.size(); k++) {
            int i = ganan.get(k);
            asientos.get(i).ganar(parte + (k == 0 ? resto : 0));
            ganadores.add(i);
            nombres.add(asientos.get(i).getNombre());
        }
        String cual = numeroBote == 0 ? "Bote principal" : "Bote secundario " + numeroBote;
        String linea = cual + " (" + Formato.pesos(bote) + "): " + String.join(" y ", nombres)
                + (ganan.size() > 1 ? " se lo reparten" : " gana") + " con "
                + asientos.get(ganan.get(0)).getManoFinal().getDescripcion() + ".";
        resumen.add(linea);
        anotar(linea);
    }

    private void terminarMano() {
        fase = FasePoker.MANO_TERMINADA;
        turno = -1;
        if (getHumano().getFichas() == 0) {
            anotar("Te quedaste sin fichas en la mesa. Puedes recomprar o levantarte.");
        }
    }

    // ===================================================== decisión del rival

    /**
     * Le pregunta al rival qué hace. La mesa calcula lo que el rival puede saber
     * (su probabilidad de ganar, el bote, cuánto debe igualar...) y su estrategia decide.
     */
    private EstrategiaRival.Decision decidirRival(int indice) {
        AsientoPoker a = asientos.get(indice);
        int oponentes = contarEnMano() - 1;
        double equidad = estimarEquidad(a.getCartas(), comunitarias, oponentes, SIMULACIONES_RIVAL, azar);
        long maximo = a.getApuestaRonda() + a.getFichas();
        EstrategiaRival.Situacion situacion = new EstrategiaRival.Situacion(
                equidad, oponentes, apuestaActual - a.getApuestaRonda(), getBote(), apuestaActual,
                getMinimoSubirA(), maximo, a.isPuedeSubir() && maximo > apuestaActual, CIEGA_GRANDE);
        return a.getEstrategia().decidir(situacion, azar);
    }

    /**
     * Estima la probabilidad de ganar con Monte Carlo: reparte al azar las cartas
     * desconocidas muchas veces y cuenta cuántas veces gana (los empates cuentan parcial).
     */
    public static double estimarEquidad(List<Carta> propias, List<Carta> mesa, int oponentes, int simulaciones, Random azar) {
        if (oponentes <= 0) {
            return 1.0;
        }
        Set<Carta> conocidas = new HashSet<>(propias);
        conocidas.addAll(mesa);
        List<Carta> resto = new ArrayList<>();
        for (Palo p : Palo.values()) {
            for (Valor v : Valor.values()) {
                Carta c = new Carta(v, p);
                if (!conocidas.contains(c)) {
                    resto.add(c);
                }
            }
        }
        Carta[] mazo = resto.toArray(new Carta[0]);
        int faltanMesa = 5 - mesa.size();
        int necesarias = faltanMesa + 2 * oponentes;
        double puntos = 0;
        Carta[] mano = new Carta[7];
        for (int s = 0; s < simulaciones; s++) {
            for (int i = 0; i < necesarias; i++) {
                int j = i + azar.nextInt(mazo.length - i);
                Carta t = mazo[i];
                mazo[i] = mazo[j];
                mazo[j] = t;
            }
            // mano[2..6] = las 5 cartas de la mesa (las conocidas y las que faltan)
            for (int i = 0; i < mesa.size(); i++) {
                mano[2 + i] = mesa.get(i);
            }
            for (int i = 0; i < faltanMesa; i++) {
                mano[2 + mesa.size() + i] = mazo[i];
            }
            mano[0] = propias.get(0);
            mano[1] = propias.get(1);
            long miPuntaje = EvaluadorPoker.puntajeRapido(mano, 7);
            boolean pierde = false;
            int empatan = 0;
            for (int o = 0; o < oponentes && !pierde; o++) {
                mano[0] = mazo[faltanMesa + 2 * o];
                mano[1] = mazo[faltanMesa + 2 * o + 1];
                long p = EvaluadorPoker.puntajeRapido(mano, 7);
                if (p > miPuntaje) {
                    pierde = true;
                } else if (p == miPuntaje) {
                    empatan++;
                }
            }
            if (!pierde) {
                puntos += 1.0 / (empatan + 1);
            }
        }
        return puntos / simulaciones;
    }

    // ================================================================= botes

    /**
     * Un bote de la mano: cuántas fichas tiene y qué jugadores pueden ganarlo.
     *
     * Cuando alguien va all-in con menos fichas, solo puede ganar lo que alcanzó
     * a igualar; el resto forma un bote secundario entre los demás. Es una clase
     * anidada porque un bote solo tiene sentido dentro de una mano de póker.
     */
    public static final class Bote {

        private final int numero;
        private final long monto;
        private final List<Integer> elegibles;

        private Bote(int numero, long monto, List<Integer> elegibles) {
            this.numero = numero;
            this.monto = monto;
            this.elegibles = Collections.unmodifiableList(elegibles);
        }

        /**
         * Divide lo aportado en botes según los niveles de all-in.
         *
         * @param enMano índices de los jugadores que llegaron al final sin retirarse
         */
        static List<Bote> armar(List<AsientoPoker> asientos, List<Integer> enMano) {
            TreeSet<Long> niveles = new TreeSet<>();
            for (int i : enMano) {
                niveles.add(asientos.get(i).getAportadoMano());
            }
            List<Bote> botes = new ArrayList<>();
            long previo = 0;
            for (long nivel : niveles) {
                long monto = 0;
                for (AsientoPoker a : asientos) {
                    long aporte = a.getAportadoMano();
                    monto += Math.max(0, Math.min(aporte, nivel) - Math.min(aporte, previo));
                }
                previo = nivel;
                if (monto == 0) {
                    continue;
                }
                List<Integer> elegibles = new ArrayList<>();
                for (int i : enMano) {
                    if (asientos.get(i).getAportadoMano() >= nivel) {
                        elegibles.add(i);
                    }
                }
                botes.add(new Bote(botes.size(), monto, elegibles));
            }
            return botes;
        }

        /** 0 = bote principal, 1 en adelante = botes secundarios. */
        public int getNumero() {
            return numero;
        }

        public long getMonto() {
            return monto;
        }

        /** Índices de los asientos que pueden ganar este bote. */
        public List<Integer> getElegibles() {
            return elegibles;
        }
    }

    // ============================================================ utilidades

    private int siguiente(int i) {
        return (i + 1) % asientos.size();
    }

    private int distanciaDesdeBoton(int i) {
        return Math.floorMod(i - boton - 1, asientos.size());
    }

    private int siguienteQueDebeActuar(int desde) {
        for (int k = 1; k <= asientos.size(); k++) {
            int i = (desde + k) % asientos.size();
            AsientoPoker a = asientos.get(i);
            if (a.puedeActuar() && (!a.isHaActuado() || a.getApuestaRonda() < apuestaActual)) {
                return i;
            }
        }
        return -1;
    }

    private int contarEnMano() {
        int n = 0;
        for (AsientoPoker a : asientos) {
            if (!a.isRetirado()) {
                n++;
            }
        }
        return n;
    }

    private void anotar(String linea) {
        registro.add(linea);
        if (registro.size() > 200) {
            registro.remove(0);
        }
    }

    private static String cartasTexto(List<Carta> cartas) {
        List<String> t = new ArrayList<>();
        for (Carta c : cartas) {
            t.add(c.toString());
        }
        return String.join(" ", t);
    }

    private void exigirSesion() {
        if (fase == FasePoker.SIN_SESION) {
            throw new OperacionInvalidaException("Primero debes sentarte en la mesa.");
        }
    }

    // ============================================================= consultas

    public boolean isSentado() {
        return fase != FasePoker.SIN_SESION;
    }

    public boolean isManoEnCurso() {
        return fase == FasePoker.PREFLOP || fase == FasePoker.FLOP || fase == FasePoker.TURN || fase == FasePoker.RIVER;
    }

    public boolean esTurnoJugador() {
        return isManoEnCurso() && turno == 0;
    }

    public boolean esTurnoRival() {
        return isManoEnCurso() && turno > 0;
    }

    public AsientoPoker getHumano() {
        return asientos.get(0);
    }

    public List<AsientoPoker> getAsientos() {
        return Collections.unmodifiableList(asientos);
    }

    public List<Carta> getComunitarias() {
        return Collections.unmodifiableList(comunitarias);
    }

    public FasePoker getFase() {
        return fase;
    }

    public int getTurno() {
        return turno;
    }

    public int getBoton() {
        return boton;
    }

    /** Todo lo que hay en el centro, incluidas las apuestas de la ronda actual. */
    public long getBote() {
        long total = 0;
        for (AsientoPoker a : asientos) {
            total += a.getAportadoMano();
        }
        return total;
    }

    public long getApuestaActual() {
        return apuestaActual;
    }

    public long getPorIgualarJugador() {
        return Math.max(0, apuestaActual - getHumano().getApuestaRonda());
    }

    /** Apuesta total mínima a la que se puede subir ahora. */
    public long getMinimoSubirA() {
        return apuestaActual + subidaMinima;
    }

    public long getMaximoSubirAJugador() {
        AsientoPoker h = getHumano();
        return h.getApuestaRonda() + h.getFichas();
    }

    public boolean puedeSubirJugador() {
        AsientoPoker h = getHumano();
        return esTurnoJugador() && h.isPuedeSubir() && getMaximoSubirAJugador() > apuestaActual;
    }

    public boolean isCartasDescubiertas() {
        return cartasDescubiertas;
    }

    public boolean esGanador(int indice) {
        return ganadores.contains(indice);
    }

    public List<String> getRegistro() {
        return Collections.unmodifiableList(registro);
    }

    public List<String> getResumen() {
        return Collections.unmodifiableList(resumen);
    }

    public long getCompradoTotal() {
        return compradoTotal;
    }

    public int getManosJugadas() {
        return manosJugadas;
    }

    @Override
    public String getDescripcion() {
        return "Texas Hold'em sin límite contra 3 rivales. Ciegas $500 / $1.000.";
    }

    @Override
    public String getReglas() {
        return "PÓKER TEXAS HOLD'EM SIN LÍMITE — 4 jugadores\n\n"
                + "Al sentarte compras fichas para la mesa (entre " + Formato.pesos(getApuestaMinima()) + " y "
                + Formato.pesos(getApuestaMaxima()) + "). Al levantarte se te devuelve lo que tengas.\n\n"
                + "CÓMO SE JUEGA\n"
                + "1. El botón rota en cada mano. Los dos jugadores a su izquierda ponen las ciegas: "
                + Formato.pesos(CIEGA_PEQUENA) + " y " + Formato.pesos(CIEGA_GRANDE) + ".\n"
                + "2. Cada jugador recibe 2 cartas privadas. Ronda de apuestas (preflop).\n"
                + "3. Se quema una carta y se abren 3 cartas comunitarias (flop). Ronda de apuestas.\n"
                + "4. Se quema una carta y se abre la cuarta (turn). Ronda de apuestas.\n"
                + "5. Se quema una carta y se abre la quinta (river). Última ronda de apuestas.\n"
                + "6. Si quedan dos o más jugadores, muestran sus cartas: gana la mejor mano de 5 cartas\n"
                + "   formada con sus 2 cartas y las 5 comunitarias.\n\n"
                + "EN TU TURNO puedes: no ir (retirarte), pasar (si nadie apostó), igualar, subir o ir all-in.\n"
                + "La subida mínima es igual a la última subida (como mínimo la ciega grande). Sin límite máximo.\n"
                + "Si alguien va all-in con menos fichas, se forman botes secundarios: solo puede ganar\n"
                + "lo que pudo igualar.\n\n"
                + "MANOS, DE MAYOR A MENOR\n"
                + "Escalera real · Escalera de color · Póker · Full · Color · Escalera · Trío · Doble pareja · Pareja · Carta alta\n\n"
                + "El cashback se calcula al levantarte, sobre lo que hayas perdido en toda la sesión.";
    }
}
