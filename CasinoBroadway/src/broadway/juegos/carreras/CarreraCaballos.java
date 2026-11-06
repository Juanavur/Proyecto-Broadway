package broadway.juegos.carreras;

import broadway.juegos.Apuesta;
import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * Hipódromo con cuotas fijas. Antes de cada carrera se corren miles de carreras
 * simuladas para estimar la probabilidad de cada resultado; la cuota ofrecida es
 * la cuota justa menos el margen de la casa, como en una casa de apuestas real.
 */
public class CarreraCaballos extends Juego {

    public static final int NUMERO_CABALLOS = 8;
    public static final double MARGEN_CASA = 0.15;
    private static final int SIMULACIONES = 20_000;
    private static final double CUOTA_MAXIMA = 500.0;

    private static final String[] NOMBRES = {
            "Relámpago", "Tormenta Andina", "Café Express", "Sabanero", "Cóndor Real",
            "Viento Norte", "La Candelaria", "Monserrate", "Bravo Llanero", "Estrella Fugaz",
            "Trueno Negro", "Orquídea", "Galope Dorado", "Pura Sangre", "Cumbiambero",
            "Tequendama", "Furia Roja", "Brisa de Mar", "Nevado", "Centella"
    };

    private final Random azar;
    private final List<Caballo> caballos = new ArrayList<>();
    private final double[] cuotaGanador = new double[NUMERO_CABALLOS];
    private final double[] cuotaColocado = new double[NUMERO_CABALLOS];
    private final double[][] cuotaExacta = new double[NUMERO_CABALLOS][NUMERO_CABALLOS];
    private final List<ApuestaCarrera> apuestas = new ArrayList<>();
    private int numeroCarrera;

    public CarreraCaballos(Random azar) {
        super("Carreras de caballos", 1_000, 200_000);
        this.azar = azar;
        prepararCarrera();
    }

    /** Presenta 8 caballos nuevos y calcula sus cuotas. */
    public final void prepararCarrera() {
        if (!apuestas.isEmpty()) {
            throw new OperacionInvalidaException("Hay apuestas pendientes para la carrera actual.");
        }
        numeroCarrera++;
        caballos.clear();
        List<String> nombres = new ArrayList<>(Arrays.asList(NOMBRES));
        Collections.shuffle(nombres, azar);
        for (int i = 0; i < NUMERO_CABALLOS; i++) {
            double velocidad = 9.7 + azar.nextDouble() * 0.6;
            double resistencia = 0.3 + azar.nextDouble() * 0.7;
            caballos.add(new Caballo(i + 1, nombres.get(i), velocidad, resistencia));
        }
        calcularCuotas();
    }

    private void calcularCuotas() {
        int[] ganados = new int[NUMERO_CABALLOS];
        int[] colocados = new int[NUMERO_CABALLOS];
        int[][] exactas = new int[NUMERO_CABALLOS][NUMERO_CABALLOS];
        SplittableRandom azarSimulacion = new SplittableRandom(azar.nextLong());
        for (int s = 0; s < SIMULACIONES; s++) {
            int[] orden = SimulacionCarrera.correr(caballos, azarSimulacion, false).getOrdenLlegada();
            ganados[orden[0]]++;
            colocados[orden[0]]++;
            colocados[orden[1]]++;
            colocados[orden[2]]++;
            exactas[orden[0]][orden[1]]++;
        }
        for (int i = 0; i < NUMERO_CABALLOS; i++) {
            cuotaGanador[i] = cuotaPara(ganados[i]);
            double probabilidad = (double) ganados[i] / SIMULACIONES;
            caballos.get(i).setForma(probabilidad >= 0.25 ? 5 : probabilidad >= 0.16 ? 4
                    : probabilidad >= 0.10 ? 3 : probabilidad >= 0.05 ? 2 : 1);
            cuotaColocado[i] = cuotaPara(colocados[i]);
            for (int j = 0; j < NUMERO_CABALLOS; j++) {
                cuotaExacta[i][j] = i == j ? 0 : cuotaPara(exactas[i][j]);
            }
        }
    }

    /** Cuota = (1 / probabilidad) × (1 − margen), redondeada a dos decimales, mínimo 1,01. */
    private static double cuotaPara(int veces) {
        double probabilidad = Math.max(veces, 0.5) / SIMULACIONES;
        double cuota = (1.0 / probabilidad) * (1.0 - MARGEN_CASA);
        cuota = Math.min(CUOTA_MAXIMA, Math.max(1.01, cuota));
        return Math.floor(cuota * 100) / 100.0;
    }

    public void apostarGanador(Jugador jugador, int caballo, long monto) {
        agregar(jugador, new ApuestaCarrera(TipoApuestaCarrera.GANADOR, validarCaballo(caballo), -1,
                monto, cuotaGanador[caballo]));
    }

    public void apostarColocado(Jugador jugador, int caballo, long monto) {
        agregar(jugador, new ApuestaCarrera(TipoApuestaCarrera.COLOCADO, validarCaballo(caballo), -1,
                monto, cuotaColocado[caballo]));
    }

    public void apostarExacta(Jugador jugador, int primero, int segundo, long monto) {
        validarCaballo(primero);
        validarCaballo(segundo);
        if (primero == segundo) {
            throw new OperacionInvalidaException("En la exacta el primero y el segundo deben ser caballos distintos.");
        }
        agregar(jugador, new ApuestaCarrera(TipoApuestaCarrera.EXACTA, primero, segundo,
                monto, cuotaExacta[primero][segundo]));
    }

    private void agregar(Jugador jugador, ApuestaCarrera apuesta) {
        validarApuesta(jugador, apuesta.getMonto());
        jugador.apostar(apuesta.getMonto());
        apuestas.add(apuesta);
    }

    private int validarCaballo(int indice) {
        if (indice < 0 || indice >= NUMERO_CABALLOS) {
            throw new OperacionInvalidaException("Escoge un caballo del 1 al " + NUMERO_CABALLOS + ".");
        }
        return indice;
    }

    /** Cancela todas las apuestas de la carrera actual y devuelve las fichas. */
    public void cancelarApuestas(Jugador jugador) {
        long total = getTotalApostado();
        if (total > 0) {
            jugador.cobrar(total);
        }
        apuestas.clear();
    }

    /** Corre la carrera y paga las apuestas. Después hay que llamar a {@link #prepararCarrera()}. */
    public ResultadoCarrera correr(Jugador jugador) {
        if (apuestas.isEmpty()) {
            throw new OperacionInvalidaException("Haz al menos una apuesta antes de la largada.");
        }
        SimulacionCarrera simulacion = SimulacionCarrera.correr(caballos, azar, true);
        int[] orden = simulacion.getOrdenLlegada();
        List<ApuestaCarrera> ganadoras = new ArrayList<>();
        ResultadoRonda ronda = liquidarApuestas(jugador, apuestas, orden, ganadoras);
        List<ApuestaCarrera> jugadas = new ArrayList<>(apuestas);
        apuestas.clear();
        return new ResultadoCarrera(simulacion, jugadas, ganadoras, ronda);
    }

    public long getTotalApostado() {
        return Apuesta.sumarMontos(apuestas);
    }

    public List<Caballo> getCaballos() {
        return Collections.unmodifiableList(caballos);
    }

    public double getCuotaGanador(int caballo) {
        return cuotaGanador[caballo];
    }

    public double getCuotaColocado(int caballo) {
        return cuotaColocado[caballo];
    }

    public double getCuotaExacta(int primero, int segundo) {
        return cuotaExacta[primero][segundo];
    }

    public List<ApuestaCarrera> getApuestas() {
        return Collections.unmodifiableList(apuestas);
    }

    public int getNumeroCarrera() {
        return numeroCarrera;
    }

    @Override
    public String getDescripcion() {
        return "8 caballos, cuotas fijas y apuestas a ganador, colocado y exacta.";
    }

    @Override
    public String getReglas() {
        return "CARRERAS DE CABALLOS — 8 caballos, 1.000 metros\n\n"
                + "Antes de cada carrera se publican las cuotas. Si tu apuesta gana, recibes monto × cuota\n"
                + "(por ejemplo, $10.000 a cuota 4,50 paga $45.000, es decir, ganas $35.000).\n\n"
                + "TIPOS DE APUESTA\n"
                + "• Ganador: tu caballo llega primero.\n"
                + "• Colocado: tu caballo llega entre los tres primeros (paga menos que ganador).\n"
                + "• Exacta: aciertas el primero y el segundo en el orden exacto (paga mucho más).\n\n"
                + "La forma (estrellas) resume el rendimiento de cada caballo, pero en las carreras\n"
                + "cualquiera puede ganar: el ritmo cambia en cada tramo y los caballos con poca\n"
                + "resistencia se cansan al final.\n\n"
                + "Las cuotas salen de simular " + Formato.numero(SIMULACIONES) + " carreras con los mismos caballos,\n"
                + "y la casa se queda con un margen del " + (int) (MARGEN_CASA * 100) + " %, como en una casa de apuestas real.\n\n"
                + "Mínimo " + Formato.pesos(getApuestaMinima()) + " y máximo " + Formato.pesos(getApuestaMaxima())
                + " por apuesta. Puedes hacer varias apuestas en la misma carrera.";
    }
}
