package modelos;

import java.time.LocalDate;

/** Un movimiento de dinero en la caja: venta o canje de fichas (RF-07). */
public class MovimientoCaja {

    public static final String VENTA = "Venta";
    public static final String CANJE = "Canje";

    private final String tipo;
    private final double monto;
    private final LocalDate fecha;

    public MovimientoCaja(String tipo, double monto) {
        if (!VENTA.equals(tipo) && !CANJE.equals(tipo)) {
            throw new IllegalArgumentException("El movimiento debe ser una venta o un canje.");
        }
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = LocalDate.now();
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
