package broadway.juegos.carreras;

/** Tipos de apuesta del hipódromo. */
public enum TipoApuestaCarrera {
    GANADOR("Ganador", "Tu caballo llega primero."),
    COLOCADO("Colocado", "Tu caballo llega entre los tres primeros."),
    EXACTA("Exacta", "Aciertas el primero y el segundo en el orden exacto.");

    private final String nombre;
    private final String explicacion;

    TipoApuestaCarrera(String nombre, String explicacion) {
        this.nombre = nombre;
        this.explicacion = explicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getExplicacion() {
        return explicacion;
    }
}
