package broadway.juegos;

import java.util.List;

/**
 * Una apuesta que se paga según el resultado de una jugada.
 *
 * Es genérica porque cada juego tiene un tipo de resultado distinto:
 * en la ruleta el resultado es el número que salió ({@code Integer}) y en
 * las carreras es el orden de llegada ({@code int[]}). Así {@link Juego}
 * puede pagar cualquier lista de apuestas sin saber de qué juego vienen.
 *
 * @param <R> tipo del resultado de la jugada
 */
public interface Apuesta<R> {

    /** Fichas que el jugador puso en esta apuesta. */
    long getMonto();

    /** Lo que recibe el jugador con ese resultado (incluye lo apostado); 0 si pierde. */
    long calcularPremio(R resultado);

    /** Texto corto para mostrarle al jugador, por ejemplo "Pleno 17" o "Ganador #3". */
    String getDescripcion();

    /** ¿Esta apuesta gana con ese resultado? */
    default boolean gana(R resultado) {
        return calcularPremio(resultado) > 0;
    }

    /** Suma lo apostado en una lista de apuestas de cualquier juego. */
    static long sumarMontos(List<? extends Apuesta<?>> apuestas) {
        long total = 0;
        for (Apuesta<?> a : apuestas) {
            total += a.getMonto();
        }
        return total;
    }
}
