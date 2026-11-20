package modelos;

import java.time.LocalDate;

/** Jefe de sala: registra empleados, define su cargo y crea las mesas (RF-02, RF-03, RF-04). */
public class Administrador extends Trabajador {

    private final Casino casino;

    public Administrador(String documento, String nombre, LocalDate fechaNacimiento,
                         String id, String usuario, String contraseña, Casino casino) {
        super(documento, nombre, fechaNacimiento, id, usuario, contraseña);
        this.casino = casino;
    }

    public Mesa crearMesa(String codigo, Juego juego, double minima, double maxima,
                          int capacidad, Crupier crupier) {
        exigir(CREAR_MESA);
        Mesa mesa = new Mesa(codigo, juego, minima, maxima, capacidad);
        mesa.asignarCrupier(crupier);
        return mesa;
    }

    public boolean registrarEmpleados(Trabajador empleado) {
        exigir(REGISTRAR_EMPLEADO);
        return casino.registrarEmpleado(empleado);
    }

    /**
     * Crea el empleado con el cargo indicado; el cargo define qué funciones podrá usar.
     * Los cargos posibles son "Administrador", "Cajero", "Crupier" y "Técnico de máquinas".
     */
    public Trabajador definirCargoEmpleado(String cargo, String documento, String nombre, LocalDate nacimiento,
                                           String usuario, String contraseña) {
        exigir(REGISTRAR_EMPLEADO);
        String id = "E" + (casino.getEmpleados().size() + 1);
        switch (cargo) {
            case "Administrador": return new Administrador(documento, nombre, nacimiento, id, usuario, contraseña, casino);
            case "Cajero": return new Cajero(documento, nombre, nacimiento, id, usuario, contraseña, casino);
            case "Crupier": return new Crupier(documento, nombre, nacimiento, id, usuario, contraseña);
            case "Técnico de máquinas": return new TecnicoMaquina(documento, nombre, nacimiento, id, usuario, contraseña, casino);
            default: throw new IllegalArgumentException("Cargo desconocido: " + cargo);
        }
    }

    public boolean registrarMesaDisponible(Mesa mesa) {
        exigir(CREAR_MESA);
        return casino.agregarMesa(mesa);
    }

    @Override
    public Boolean puedeRealizar(String accion) {
        return CREAR_MESA.equals(accion) || REGISTRAR_EMPLEADO.equals(accion) || VER_REPORTES.equals(accion);
    }

    @Override
    public String getCargo() {
        return "Administrador";
    }
}
