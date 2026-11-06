package broadway.sonido;

/**
 * Punto de acceso a los sonidos desde las pantallas: {@code Sonidos.reproducir(Efecto.FICHA)}.
 *
 * Por dentro guarda un {@link ServicioSonido} y le delega todo. Silenciar es
 * simplemente cambiar el servicio activo por uno silenciado.
 * Si el computador no tiene salida de audio, se usa el silenciado y el casino funciona igual.
 */
public final class Sonidos {

    private static final SonidoActivo ACTIVO = new SonidoActivo();
    private static final ServicioSonido SILENCIADO = new SonidoSilenciado();
    private static ServicioSonido actual = ACTIVO;

    private Sonidos() {
    }

    /** Prepara los sonidos en segundo plano para que el primero no se retrase. */
    public static void iniciar() {
        Thread t = new Thread(() -> {
            ACTIVO.preparar();
            if (!ACTIVO.isDisponible()) {
                actual = SILENCIADO;
            }
        }, "Preparar sonidos");
        t.setDaemon(true);
        t.start();
    }

    public static void reproducir(Efecto efecto) {
        actual.reproducir(efecto);
    }

    /** Reproduce un efecto después de un retraso (por ejemplo, una carta tras otra al repartir). */
    public static void reproducir(Efecto efecto, int retrasoMs) {
        actual.reproducir(efecto, retrasoMs);
    }

    /** Reproduce un efecto en bucle; devuelve el objeto para detenerlo. */
    public static ServicioSonido.Reproduccion bucle(Efecto efecto) {
        return actual.bucle(efecto);
    }

    public static boolean isSilencio() {
        return actual == SILENCIADO;
    }

    /** Cambia entre el servicio que suena y el silenciado. */
    public static void setSilencio(boolean silencio) {
        if (silencio) {
            actual.detenerTodo();
            actual = SILENCIADO;
        } else if (ACTIVO.isDisponible()) {
            actual = ACTIVO;
        }
    }

    /** Muestras de un efecto (para exportarlo o probarlo). */
    public static float[] muestrasDe(Efecto e) {
        return ACTIVO.muestrasDe(e).clone();
    }
}
