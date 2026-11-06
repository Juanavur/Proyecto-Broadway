package broadway.juegos.carreras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Simula una carrera paso a paso. La misma simulación se usa para correr la
 * carrera real y para calcular las cuotas (corriendo miles de carreras de prueba),
 * así las cuotas reflejan las probabilidades reales de cada caballo.
 */
public final class SimulacionCarrera {

    public static final double DISTANCIA = 1000.0;
    private static final int MAX_PASOS = 400;

    private final int[] ordenLlegada;
    private final List<double[]> posiciones;

    private SimulacionCarrera(int[] ordenLlegada, List<double[]> posiciones) {
        this.ordenLlegada = ordenLlegada;
        this.posiciones = posiciones;
    }

    /**
     * @param registrarPosiciones true para guardar la posición de cada caballo en cada paso (animación)
     */
    public static SimulacionCarrera correr(List<Caballo> caballos, RandomGenerator azar, boolean registrarPosiciones) {
        int n = caballos.size();
        double[] posicion = new double[n];
        double[] tiempoLlegada = new double[n];
        Arrays.fill(tiempoLlegada, Double.MAX_VALUE);
        List<double[]> historial = registrarPosiciones ? new ArrayList<>() : null;
        double[] ritmo = new double[n];
        for (int i = 0; i < n; i++) {
            ritmo[i] = 1.0 + ruido(azar) * 0.05; // cómo salió ese día
        }
        if (registrarPosiciones) {
            historial.add(posicion.clone());
        }
        int llegados = 0;
        for (int paso = 1; paso <= MAX_PASOS && llegados < n; paso++) {
            for (int i = 0; i < n; i++) {
                if (posicion[i] >= DISTANCIA) {
                    continue;
                }
                Caballo c = caballos.get(i);
                double avance = posicion[i] / DISTANCIA;
                // En la segunda mitad, los caballos con poca resistencia se cansan.
                double cansancio = 1.0 - (1.0 - c.getResistencia()) * Math.max(0, avance - 0.5) * 0.25;
                double variacion = 1.0 + ruido(azar) * 0.12;
                double velocidad = Math.max(1.0, c.getVelocidad() * ritmo[i] * cansancio * variacion);
                double antes = posicion[i];
                posicion[i] += velocidad;
                if (posicion[i] >= DISTANCIA) {
                    double fraccion = (DISTANCIA - antes) / velocidad;
                    tiempoLlegada[i] = paso - 1 + fraccion;
                    posicion[i] = DISTANCIA;
                    llegados++;
                }
            }
            if (registrarPosiciones) {
                historial.add(posicion.clone());
            }
        }
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble(i -> tiempoLlegada[i]));
        int[] orden = new int[n];
        for (int i = 0; i < n; i++) {
            orden[i] = indices[i];
        }
        return new SimulacionCarrera(orden, historial);
    }

    /** Ruido aleatorio con media 0 y desviación 1 (uniforme, más rápido que una normal). */
    private static double ruido(RandomGenerator azar) {
        return (azar.nextDouble() * 2.0 - 1.0) * 1.7320508075688772; // raíz de 3
    }

    /** Índices de los caballos en orden de llegada (posición 0 = ganador). */
    public int[] getOrdenLlegada() {
        return ordenLlegada.clone();
    }

    /** Posición de cada caballo en cada paso, de 0 a {@link #DISTANCIA}. */
    public List<double[]> getPosiciones() {
        return posiciones;
    }
}
