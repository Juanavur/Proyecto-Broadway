package modelos;

import servicios.Formato;

import java.time.LocalDate;

/** Opera una mesa: recibe las apuestas de los clientes y las valida contra los límites (RF-08). */
public class Crupier extends Trabajador {

    public Crupier(String documento, String nombre, LocalDate fechaNacimiento,
                   String id, String usuario, String contraseña) {
        super(documento, nombre, fechaNacimiento, id, usuario, contraseña);
    }

    /** Registra la apuesta en la ronda abierta de la mesa y descuenta las fichas del cliente. */
    public Apuesta registrarApuestas(double monto, Mesa mesa, Cliente cliente, String tipo) {
        exigir(REGISTRAR_APUESTA);
        if (!mesa.estaDisponible()) {
            throw new IllegalStateException("La mesa " + mesa.getCodigoMesa() + " está cerrada.");
        }
        if (!mesa.validarMontoApuesta(monto)) {
            throw new IllegalArgumentException("La apuesta debe estar entre " + Formato.dinero(mesa.getApuestaMinima())
                    + " y " + Formato.dinero(mesa.getApuestaMaxima()) + ".");
        }
        cliente.apostar(monto);
        Apuesta apuesta = new Apuesta(tipo, monto, cliente);
        mesa.rondaActual().agregarApuesta(apuesta);
        return apuesta;
    }

    @Override
    public boolean puedeRealizar(String accion) {
        return REGISTRAR_APUESTA.equals(accion);
    }

    @Override
    public String getCargo() {
        return "Crupier";
    }
}
