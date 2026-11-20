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
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("La base de la caja no puede ser negativa.");
        }
        this.id = siguienteId++;
        this.saldoInicial = saldoInicial;
        this.apertura = LocalDate.now();
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
        if (cierre != null) {
            throw new IllegalStateException("El turno ya está cerrado.");
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
