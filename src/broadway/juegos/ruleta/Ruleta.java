package broadway.juegos.ruleta;

import broadway.juegos.Apuesta;
import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Ruleta europea de un solo cero (37 casillas, del 0 al 36).
 * Si sale el 0, pierden todas las apuestas que no lo incluyan.
 */
public class Ruleta extends Juego {

    /** Orden real de los números en la rueda europea, en sentido horario desde el 0. */
    public static final int[] ORDEN_RUEDA = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };
    private static final Set<Integer> ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);
    public static final long MAXIMO_EN_MESA = 2_000_000;
    private static final int HISTORIAL = 15;

    private final Random azar;
    private final List<ApuestaRuleta> apuestas = new ArrayList<>();
    private final List<ApuestaRuleta> ultimasApuestas = new ArrayList<>();
    private final LinkedList<Integer> ultimosNumeros = new LinkedList<>();

    public Ruleta(Random azar) {
        super("Ruleta", 1_000, 200_000);
        this.azar = azar;
    }

    public static boolean esRojo(int numero) {
        return ROJOS.contains(numero);
    }

    public static String colorDe(int numero) {
        if (numero == 0) {
            return "Verde";
        }
        return esRojo(numero) ? "Rojo" : "Negro";
    }

    /** Pone fichas sobre el tapete. Si ya hay fichas en esa misma casilla, se suman. */
    public void apostar(Jugador jugador, ApuestaRuleta nueva) {
        ApuestaRuleta existente = buscar(nueva);
        long montoFinal = nueva.getMonto() + (existente == null ? 0 : existente.getMonto());
        if (nueva.getMonto() < getApuestaMinima() && existente == null) {
            throw new OperacionInvalidaException("La apuesta mínima por casilla es " + Formato.pesos(getApuestaMinima()) + ".");
        }
        if (montoFinal > getApuestaMaxima()) {
            throw new OperacionInvalidaException("El máximo por casilla es " + Formato.pesos(getApuestaMaxima()) + ".");
        }
        if (getTotalApostado() + nueva.getMonto() > MAXIMO_EN_MESA) {
            throw new OperacionInvalidaException("El máximo total en la mesa es " + Formato.pesos(MAXIMO_EN_MESA) + ".");
        }
        jugador.apostar(nueva.getMonto());
        if (existente != null) {
            existente.aumentar(nueva.getMonto());
        } else {
            apuestas.add(nueva);
        }
    }

    private ApuestaRuleta buscar(ApuestaRuleta apuesta) {
        for (ApuestaRuleta a : apuestas) {
            if (a.mismaPosicion(apuesta)) {
                return a;
            }
        }
        return null;
    }

    /** Retira todas las fichas del tapete y se las devuelve al jugador. */
    public void retirarApuestas(Jugador jugador) {
        long total = getTotalApostado();
        if (total > 0) {
            jugador.cobrar(total);
        }
        apuestas.clear();
    }

    /** Vuelve a poner las mismas apuestas de la tirada anterior. */
    public void repetirApuestas(Jugador jugador) {
        if (ultimasApuestas.isEmpty()) {
            throw new OperacionInvalidaException("Todavía no hay una tirada anterior para repetir.");
        }
        long total = Apuesta.sumarMontos(ultimasApuestas);
        if (total > jugador.getFichas()) {
            throw new OperacionInvalidaException("Necesitas " + Formato.pesos(total) + " en fichas para repetir la apuesta.");
        }
        for (ApuestaRuleta a : ultimasApuestas) {
            apostar(jugador, a.copia());
        }
    }

    public ResultadoTirada girar(Jugador jugador) {
        if (apuestas.isEmpty()) {
            throw new OperacionInvalidaException("Pon al menos una ficha sobre el tapete antes de girar.");
        }
        int numero = azar.nextInt(37);
        return resolver(jugador, numero);
    }

    /** Paga las apuestas para un número ya sorteado (separado de girar para poder probarlo). */
    ResultadoTirada resolver(Jugador jugador, int numero) {
        List<ApuestaRuleta> ganadoras = new ArrayList<>();
        ResultadoRonda ronda = liquidarApuestas(jugador, apuestas, numero, ganadoras);
        ultimasApuestas.clear();
        for (ApuestaRuleta a : apuestas) {
            ultimasApuestas.add(a.copia());
        }
        apuestas.clear();
        ultimosNumeros.addFirst(numero);
        if (ultimosNumeros.size() > HISTORIAL) {
            ultimosNumeros.removeLast();
        }
        return new ResultadoTirada(numero, ganadoras, ronda);
    }

    public long getTotalApostado() {
        return Apuesta.sumarMontos(apuestas);
    }

    public List<ApuestaRuleta> getApuestas() {
        return Collections.unmodifiableList(apuestas);
    }

    public boolean hayApuestaAnterior() {
        return !ultimasApuestas.isEmpty();
    }

    public List<Integer> getUltimosNumeros() {
        return Collections.unmodifiableList(ultimosNumeros);
    }

    @Override
    public String getDescripcion() {
        return "Ruleta europea de un cero: plenos que pagan 35 a 1 y 12 tipos de apuesta.";
    }

    @Override
    public String getReglas() {
        StringBuilder sb = new StringBuilder();
        sb.append("RULETA EUROPEA — 37 casillas (0 al 36)\n\n");
        sb.append("Pon tus fichas en el tapete y gira. Si sale el 0, solo ganan las apuestas que lo incluyen.\n\n");
        sb.append("PAGOS\n");
        for (TipoApuestaRuleta t : TipoApuestaRuleta.values()) {
            sb.append(String.format("   %-14s %2d a 1%n", t.getNombre(), t.getPago()));
        }
        sb.append("\nCÓMO APOSTAR EN EL TAPETE\n");
        sb.append("• Pleno: clic en el centro de un número.\n");
        sb.append("• Semipleno: clic en la línea entre dos números vecinos (también 0 con 1, 2 o 3).\n");
        sb.append("• Calle: clic en el borde inferior de una columna de tres números (1-2-3, 4-5-6...).\n");
        sb.append("• Cuadro: clic en la esquina donde se tocan cuatro números.\n");
        sb.append("• Seisena: clic en el borde inferior, justo entre dos calles.\n");
        sb.append("• Docenas, columnas (2 a 1) y apuestas sencillas: clic en su casilla.\n");
        sb.append("• Clic derecho sobre una casilla con fichas: quita esa apuesta.\n\n");
        sb.append("Mínimo ").append(Formato.pesos(getApuestaMinima())).append(" y máximo ")
                .append(Formato.pesos(getApuestaMaxima())).append(" por casilla; máximo ")
                .append(Formato.pesos(MAXIMO_EN_MESA)).append(" en la mesa.\n");
        sb.append("Ventaja de la casa: 2,7 % (1/37) en todas las apuestas.");
        return sb.toString();
    }

    /** Quita una apuesta concreta del tapete y devuelve sus fichas. */
    public void quitarApuesta(Jugador jugador, ApuestaRuleta apuesta) {
        if (apuestas.remove(apuesta)) {
            jugador.cobrar(apuesta.getMonto());
        }
    }
}
