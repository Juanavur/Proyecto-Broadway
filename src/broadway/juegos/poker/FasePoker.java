package broadway.juegos.poker;

/** Fases de una mano de Texas Hold'em. */
public enum FasePoker {
    SIN_SESION("Sin sentarse"),
    ENTRE_MANOS("Esperando nueva mano"),
    PREFLOP("Preflop"),
    FLOP("Flop"),
    TURN("Turn"),
    RIVER("River"),
    MANO_TERMINADA("Mano terminada");

    private final String nombre;

    FasePoker(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
