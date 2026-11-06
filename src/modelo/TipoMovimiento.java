package broadway.modelo;

/** Tipos de movimiento que quedan registrados en el historial del jugador. */
public enum TipoMovimiento {
    COMPRA_FICHAS("Compra de fichas"),
    VENTA_FICHAS("Venta de fichas"),
    CASHBACK("Cashback reclamado"),
    JUEGO("Juego");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
