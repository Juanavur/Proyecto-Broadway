package broadway.juegos.poker;

/** Jugadas de póker de menor a mayor. */
public enum CategoriaMano {
    CARTA_ALTA("Carta alta"),
    PAREJA("Pareja"),
    DOBLE_PAREJA("Doble pareja"),
    TRIO("Trío"),
    ESCALERA("Escalera"),
    COLOR("Color"),
    FULL("Full"),
    POKER("Póker"),
    ESCALERA_COLOR("Escalera de color"),
    ESCALERA_REAL("Escalera real");

    private final String nombre;

    CategoriaMano(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
