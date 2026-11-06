package broadway.juegos.carreras;

import broadway.modelo.ResultadoRonda;

import java.util.Collections;
import java.util.List;

/** Orden de llegada, recorrido de cada caballo y apuestas que cobraron. */
public class ResultadoCarrera {

    private final SimulacionCarrera simulacion;
    private final List<ApuestaCarrera> apuestas;
    private final List<ApuestaCarrera> ganadoras;
    private final ResultadoRonda ronda;

    public ResultadoCarrera(SimulacionCarrera simulacion, List<ApuestaCarrera> apuestas,
                            List<ApuestaCarrera> ganadoras, ResultadoRonda ronda) {
        this.simulacion = simulacion;
        this.apuestas = apuestas;
        this.ganadoras = ganadoras;
        this.ronda = ronda;
    }

    public int[] getOrdenLlegada() {
        return simulacion.getOrdenLlegada();
    }

    public List<double[]> getPosiciones() {
        return simulacion.getPosiciones();
    }

    public List<ApuestaCarrera> getApuestas() {
        return Collections.unmodifiableList(apuestas);
    }

    public List<ApuestaCarrera> getGanadoras() {
        return Collections.unmodifiableList(ganadoras);
    }

    public ResultadoRonda getRonda() {
        return ronda;
    }
}
