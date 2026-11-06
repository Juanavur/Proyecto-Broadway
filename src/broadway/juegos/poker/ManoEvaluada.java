package broadway.juegos.poker;

import broadway.cartas.Carta;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de evaluar una mano: su categoría, las 5 cartas que forman la jugada
 * y un puntaje numérico que permite comparar dos manos directamente.
 */
public class ManoEvaluada implements Comparable<ManoEvaluada> {

    private final CategoriaMano categoria;
    private final long puntaje;
    private final List<Carta> cartas;
    private final String descripcion;

    ManoEvaluada(CategoriaMano categoria, long puntaje, List<Carta> cartas, String descripcion) {
        this.categoria = categoria;
        this.puntaje = puntaje;
        this.cartas = Collections.unmodifiableList(cartas);
        this.descripcion = descripcion;
    }

    public CategoriaMano getCategoria() {
        return categoria;
    }

    public long getPuntaje() {
        return puntaje;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    /** Descripción para el jugador, por ejemplo "Full de Ases con Reyes". */
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public int compareTo(ManoEvaluada otra) {
        return Long.compare(puntaje, otra.puntaje);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
