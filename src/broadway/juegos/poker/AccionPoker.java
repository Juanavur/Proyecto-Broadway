package broadway.juegos.poker;

/** Acciones posibles en tu turno. */
public enum AccionPoker {
    RETIRARSE("No ir"),
    PASAR("Pasar"),
    IGUALAR("Igualar"),
    SUBIR("Subir"),
    TODO("All-in");

    private final String nombre;

    AccionPoker(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
