package broadway.juegos.poker;

/**
 * Comportamiento común a todos los estilos de rival: cómo se arma cada
 * decisión y de qué tamaño son las subidas. Cada estilo concreto solo
 * escribe su propio {@link #decidir}.
 */
public abstract class EstrategiaBase implements EstrategiaRival {

    private final String nombre;

    protected EstrategiaBase(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    protected static Decision pasar() {
        return new Decision(AccionPoker.PASAR, 0);
    }

    protected static Decision igualar() {
        return new Decision(AccionPoker.IGUALAR, 0);
    }

    protected static Decision retirarse() {
        return new Decision(AccionPoker.RETIRARSE, 0);
    }

    protected static Decision todo(Situacion s) {
        return new Decision(AccionPoker.TODO, s.maximoSubirA());
    }

    /** Sube una fracción del bote, respetando el mínimo y el máximo permitidos. */
    protected static Decision subir(Situacion s, double fraccionBote) {
        long objetivo = s.apuestaActual() + Math.round((s.bote() + s.porIgualar()) * fraccionBote / 500.0) * 500;
        objetivo = Math.max(objetivo, s.minimoSubirA());
        if (objetivo >= s.maximoSubirA()) {
            return todo(s);
        }
        return new Decision(AccionPoker.SUBIR, objetivo);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
