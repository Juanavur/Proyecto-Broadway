package broadway.modelo;

import java.time.LocalDate;

/** Un movimiento de dinero en la caja: venta o canje de fichas (RF-07). */
public class MovimientoCaja {

    public static final String VENTA = "Venta";
    public static final String CANJE = "Canje";

    private final String tipo;
    private final double monto;
    private final LocalDate fecha;

    public MovimientoCaja(String tipo, double monto) {
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
