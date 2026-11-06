package broadway.juegos.tragamonedas;

/**
 * Símbolos de los carretes. El premio indica cuántas veces la apuesta de la línea
 * se paga cuando salen tres iguales en una línea activa.
 */
public enum Simbolo {
    CEREZA("Cereza", 10),
    LIMON("Limón", 15),
    NARANJA("Naranja", 20),
    CIRUELA("Ciruela", 30),
    CAMPANA("Campana", 50),
    BAR("BAR", 100),
    DIAMANTE("Diamante", 200),
    SIETE("Siete", 500);

    /** Pago por dos cerezas en los dos primeros carretes de la línea. */
    public static final int PREMIO_DOS_CEREZAS = 5;
    /** Pago por una cereza en el primer carrete de la línea. */
    public static final int PREMIO_UNA_CEREZA = 1;

    private final String nombre;
    private final int premioTres;

    Simbolo(String nombre, int premioTres) {
        this.nombre = nombre;
        this.premioTres = premioTres;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPremioTres() {
        return premioTres;
    }
}
