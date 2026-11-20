package modelos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Turno de un cajero: desde que abre la caja con una base hasta que la cierra (RF-07). */
public class TurnoCaja {

    private static long siguienteId = 1;

    private final Long id;
    private final double saldoInicial;
    private final LocalDate apertura;
    private LocalDate cierre;
    private final List<MovimientoCaja> movimientos = new ArrayList<>();

    public TurnoCaja(double saldoInicial) {
        if (!Double.isFinite(saldoInicial) || saldoInicial < 0) {
            throw new IllegalArgumentException("La base de la caja debe ser un monto válido y no negativo.");
        }
        this.id = siguienteId++;
        this.saldoInicial = saldoInicial;
        this.apertura = LocalDate.now();
    }

    /** Turno leído del archivo (RF-11): conserva su número y sus fechas. */
    TurnoCaja(long id, double saldoInicial, LocalDate apertura, LocalDate cierre, List<MovimientoCaja> movimientos) {
        this.id = id;
        this.saldoInicial = saldoInicial;
        this.apertura = apertura;
        this.cierre = cierre;
        this.movimientos.addAll(movimientos);
        siguienteId = Math.max(siguienteId, id + 1);
    }

    /** Fecha de cierre, o null si el turno sigue abierto. */
    public LocalDate getCierre() {
        return cierre;
    }

    public void registrarMovimiento(MovimientoCaja movimiento) {
        if (cierre != null) {
            throw new IllegalStateException("El turno ya está cerrado.");
        }
        movimientos.add(movimiento);
    }

    /** Lo que debería haber en la caja: base + ventas - canjes. */
    public double calcularSaldoEsperado() {
        return saldoInicial + total(MovimientoCaja.VENTA) - total(MovimientoCaja.CANJE);
    }

    /** Cierra el turno con el efectivo del turno contado por el cajero (sin la base). */
    public Caja.CierreTurno cerrar(int efectivo) {
        return cerrar((double) efectivo);
    }

    Caja.CierreTurno cerrar(double efectivo) {
        if (cierre != null) {
            throw new IllegalStateException("El turno ya está cerrado.");
        }
        if (!Double.isFinite(efectivo)) {
            throw new IllegalArgumentException("El efectivo contado debe ser un monto válido.");
        }
        cierre = LocalDate.now();
        return new Caja.CierreTurno(total(MovimientoCaja.VENTA), total(MovimientoCaja.CANJE), efectivo);
    }

    double total(String tipo) {
        double suma = 0;
        for (MovimientoCaja m : movimientos) {
            if (m.getTipo().equals(tipo)) {
                suma += m.getMonto();
            }
        }
        return suma;
    }

    public boolean estaAbierto() {
        return cierre == null;
    }

    public Long getId() {
        return id;
    }

    public double getSaldoInicial() {
        return saldoInicial;
    }

    public LocalDate getApertura() {
        return apertura;
    }

    public List<MovimientoCaja> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}
