package broadway.juegos.ruleta;

/** Tipos de apuesta de la ruleta europea con su pago (X a 1). */
public enum TipoApuestaRuleta {
    PLENO("Pleno", 35, true),
    SEMIPLENO("Semipleno", 17, true),
    CALLE("Calle", 11, true),
    CUADRO("Cuadro", 8, true),
    SEISENA("Seisena", 5, true),
    DOCENA("Docena", 2, false),
    COLUMNA("Columna", 2, false),
    ROJO("Rojo", 1, false),
    NEGRO("Negro", 1, false),
    PAR("Par", 1, false),
    IMPAR("Impar", 1, false),
    FALTA("Falta (1-18)", 1, false),
    PASA("Pasa (19-36)", 1, false);

    private final String nombre;
    private final int pago;
    private final boolean interior;

    TipoApuestaRuleta(String nombre, int pago, boolean interior) {
        this.nombre = nombre;
        this.pago = pago;
        this.interior = interior;
    }

    public String getNombre() {
        return nombre;
    }

    /** Cuántas veces la apuesta se paga si gana (además de devolverla). */
    public int getPago() {
        return pago;
    }

    /** Las apuestas interiores se hacen sobre números concretos del tapete. */
    public boolean isInterior() {
        return interior;
    }
}
