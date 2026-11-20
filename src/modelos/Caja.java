package modelos;

import servicios.Formato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Caja del casino: registra cada venta y canje de fichas y maneja los turnos (RF-06, RF-07). */
public class Caja {

    private final List<MovimientoCaja> movimientos = new ArrayList<>();
    private final List<TurnoCaja> turnos = new ArrayList<>();

    public void registrarVenta(double monto) {
        registrar(new MovimientoCaja(MovimientoCaja.VENTA, monto));
    }

    public void registrarCanje(double monto) {
        registrar(new MovimientoCaja(MovimientoCaja.CANJE, monto));
    }

    private void registrar(MovimientoCaja movimiento) {
        TurnoCaja turno = turnoActual();
        if (turno == null) {
            throw new IllegalStateException("Primero hay que abrir un turno de caja.");
        }
        turno.registrarMovimiento(movimiento);
        movimientos.add(movimiento);
    }

    public TurnoCaja abrirTurno(double saldoInicial) {
        if (turnoActual() != null) {
            throw new IllegalStateException("Ya hay un turno abierto.");
        }
        TurnoCaja turno = new TurnoCaja(saldoInicial);
        turnos.add(turno);
        return turno;
    }

    /** El turno abierto, o null si la caja está cerrada. */
    public TurnoCaja turnoActual() {
        if (turnos.isEmpty()) {
            return null;
        }
        TurnoCaja ultimo = turnos.get(turnos.size() - 1);
        return ultimo.estaAbierto() ? ultimo : null;
    }

    /** Cierra el turno y devuelve "CUADRADO" o "DESCUADRE" con el detalle. */
    public String cerrarTurno(double efectivoContado) {
        TurnoCaja turno = turnoActual();
        if (turno == null) {
            throw new IllegalStateException("No hay un turno abierto.");
        }
        CierreTurno cierre = turno.cerrar((int) Math.round(efectivoContado));
        String detalle = "Ventas " + Formato.dinero(cierre.getVentas()) + " - canjes " + Formato.dinero(cierre.getCanjes())
                + " = balance " + Formato.dinero(cierre.getBalance()) + "; contado " + Formato.dinero(cierre.getEfectivoContado());
        return (cierre.hayDescuadre() ? "DESCUADRE" : "CUADRADO") + " · " + detalle;
    }

    public List<MovimientoCaja> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    public List<TurnoCaja> getTurnos() {
        return Collections.unmodifiableList(turnos);
    }

    /**
     * Resultado del cierre de un turno. Está anidada porque solo existe como parte de la caja.
     * Ejemplo: ventas 1.000.000 y canjes 700.000 dan un balance de 300.000.
     */
    public static class CierreTurno {

        private final double ventas;
        private final double canjes;
        private final double efectivoContado;

        public CierreTurno(double ventas, double canjes, double efectivoContado) {
            this.ventas = ventas;
            this.canjes = canjes;
            this.efectivoContado = efectivoContado;
        }

        public double getBalance() {
            return ventas - canjes;
        }

        public boolean hayDescuadre() {
            return Math.abs(efectivoContado - getBalance()) > 0.5;
        }

        public double getVentas() {
            return ventas;
        }

        public double getCanjes() {
            return canjes;
        }

        public double getEfectivoContado() {
            return efectivoContado;
        }
    }
}
