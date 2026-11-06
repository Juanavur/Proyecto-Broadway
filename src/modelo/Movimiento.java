package broadway.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Un movimiento del historial: una compra o venta en la caja, un cashback
 * o el resultado de una ronda de juego. Es inmutable.
 */
public class Movimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime fecha;
    private final TipoMovimiento tipo;
    private final String detalle;
    /** Variación del efectivo del jugador (positiva si recibió dinero). */
    private final long variacionDinero;
    /** Variación de las fichas del jugador (positiva si recibió fichas). */
    private final long variacionFichas;

    public Movimiento(TipoMovimiento tipo, String detalle, long variacionDinero, long variacionFichas) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.detalle = detalle;
        this.variacionDinero = variacionDinero;
        this.variacionFichas = variacionFichas;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public String getDetalle() {
        return detalle;
    }

    public long getVariacionDinero() {
        return variacionDinero;
    }

    public long getVariacionFichas() {
        return variacionFichas;
    }
}
