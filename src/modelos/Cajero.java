package modelos;

import java.time.LocalDate;

/** Atiende la caja: registra clientes y les vende o cambia fichas (RF-01, RF-06). */
public class Cajero extends Trabajador {

    private final Casino casino;

    public Cajero(String documento, String nombre, LocalDate fechaNacimiento,
                  String id, String usuario, String contraseña, Casino casino) {
        super(documento, nombre, fechaNacimiento, id, usuario, contraseña);
        this.casino = casino;
    }

    public boolean venderFichas(Cliente cliente, double monto) {
        exigir(VENDER_FICHAS);
        Caja caja = casino.getCaja();
        caja.requireOpenShift();
        cliente.comprarFichas(monto);
        caja.registrarVenta(monto);
        return true;
    }

    public boolean canjearFichas(Cliente cliente, double monto) {
        exigir(VENDER_FICHAS);
        Caja caja = casino.getCaja();
        caja.requireOpenShift();
        cliente.retirarFichas(monto);
        caja.registrarCanje(monto);
        return true;
    }

    public boolean registrarClientes(String documento, String nombre, LocalDate fechaNacimiento) {
        exigir(REGISTRAR_CLIENTE);
        return casino.registrarCliente(new Cliente(documento, nombre, fechaNacimiento));
    }

    @Override
    public Boolean puedeRealizar(String accion) {
        return REGISTRAR_CLIENTE.equals(accion) || VENDER_FICHAS.equals(accion) || MANEJAR_CAJA.equals(accion);
    }

    @Override
    public String getCargo() {
        return "Cajero";
    }
}
