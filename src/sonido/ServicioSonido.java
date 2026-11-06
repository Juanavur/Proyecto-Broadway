package broadway.sonido;

/**
 * Lo que el casino necesita para hacer sonar efectos.
 *
 * Hay dos implementaciones: {@link SonidoActivo}, que de verdad reproduce los
 * sonidos, y {@link SonidoSilenciado}, que no hace nada. El botón de sonido solo
 * cambia cuál de las dos se usa; el resto del programa no tiene que preguntar
 * "¿está en silencio?" cada vez que algo suena.
 */
public interface ServicioSonido {

    /** Reproduce un efecto una vez, después de un retraso en milisegundos. */
    void reproducir(Efecto efecto, int retrasoMs);

    /** Reproduce un efecto una vez, de inmediato. */
    default void reproducir(Efecto efecto) {
        reproducir(efecto, 0);
    }

    /** Reproduce un efecto en bucle hasta que se detenga. */
    Reproduccion bucle(Efecto efecto);

    /** Calla todo lo que esté sonando. */
    void detenerTodo();

    /** Un sonido en curso que se puede detener (por ejemplo, el galope de una carrera). */
    interface Reproduccion {
        void detener();
    }
}
