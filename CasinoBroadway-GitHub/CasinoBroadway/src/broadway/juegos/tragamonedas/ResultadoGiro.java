package broadway.juegos.tragamonedas;

import broadway.modelo.ResultadoRonda;

import java.util.Collections;
import java.util.List;

/** Lo que quedó en pantalla después de un giro y cuánto pagó. */
public class ResultadoGiro {

    private final Simbolo[][] ventana;
    private final int[] paradas;
    private final List<LineaPremiada> lineasPremiadas;
    private final ResultadoRonda ronda;

    public ResultadoGiro(Simbolo[][] ventana, int[] paradas, List<LineaPremiada> lineasPremiadas, ResultadoRonda ronda) {
        this.ventana = ventana;
        this.paradas = paradas;
        this.lineasPremiadas = lineasPremiadas;
        this.ronda = ronda;
    }

    /** Símbolo visible en [fila][carrete]; fila 0 = arriba, 1 = centro, 2 = abajo. */
    public Simbolo getSimbolo(int fila, int carrete) {
        return ventana[fila][carrete];
    }

    /** Posición del carrete en la que quedó la fila central. */
    public int getParada(int carrete) {
        return paradas[carrete];
    }

    public List<LineaPremiada> getLineasPremiadas() {
        return Collections.unmodifiableList(lineasPremiadas);
    }

    public long getPremioTotal() {
        return ronda.getPagado();
    }

    public ResultadoRonda getRonda() {
        return ronda;
    }
}
