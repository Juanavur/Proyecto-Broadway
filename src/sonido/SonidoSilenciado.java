package broadway.sonido;

/** Implementación que no reproduce nada: se usa cuando el jugador silencia el casino. */
public class SonidoSilenciado implements ServicioSonido {

    @Override
    public void reproducir(Efecto efecto, int retrasoMs) {
        // en silencio no suena nada
    }

    @Override
    public Reproduccion bucle(Efecto efecto) {
        return () -> { }; // detener un sonido que nunca empezó no hace nada
    }

    @Override
    public void detenerTodo() {
        // no hay nada sonando
    }
}
