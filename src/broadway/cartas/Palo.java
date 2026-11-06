package broadway.cartas;

/** Los cuatro palos de la baraja francesa. */
public enum Palo {
    PICAS("Picas", '♠', false),
    CORAZONES("Corazones", '♥', true),
    DIAMANTES("Diamantes", '♦', true),
    TREBOLES("Tréboles", '♣', false);

    private final String nombre;
    private final char simbolo;
    private final boolean rojo;

    Palo(String nombre, char simbolo, boolean rojo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.rojo = rojo;
    }

    public String getNombre() {
        return nombre;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public boolean esRojo() {
        return rojo;
    }
}
