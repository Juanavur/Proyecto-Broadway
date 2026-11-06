package broadway.modelo;

/**
 * Niveles de fidelidad del Casino Broadway.
 * Cada nivel devuelve un porcentaje de las fichas perdidas como cashback.
 *
 * Es un enum (y no una jerarquía de clases de jugador) porque el nivel es un dato
 * del jugador, no un tipo distinto de jugador: todos juegan igual, solo cambia el porcentaje.
 */
public enum NivelFidelidad {

    BRONCE("Bronce", 2),
    PLATA("Plata", 4),
    ORO("Oro", 8),
    DIAMANTE("Diamante", 10);

    private final String nombre;
    private final int porcentajeCashback;

    NivelFidelidad(String nombre, int porcentajeCashback) {
        this.nombre = nombre;
        this.porcentajeCashback = porcentajeCashback;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPorcentajeCashback() {
        return porcentajeCashback;
    }

    /** Cashback que corresponde a una pérdida de fichas (se redondea hacia abajo). */
    public long calcularCashback(long fichasPerdidas) {
        if (fichasPerdidas <= 0) {
            return 0;
        }
        return fichasPerdidas * porcentajeCashback / 100;
    }

    @Override
    public String toString() {
        return nombre + " (" + porcentajeCashback + "% de cashback)";
    }
}
