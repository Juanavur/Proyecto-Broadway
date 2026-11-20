package modelos;

import java.time.LocalDate;

/** Mantiene las máquinas tragamonedas: es el único que cambia su estado (RF-09). */
public class TecnicoMaquina extends Trabajador {

    private final Casino casino;

    public TecnicoMaquina(String documento, String nombre, LocalDate fechaNacimiento,
                          String id, String usuario, String contraseña, Casino casino) {
        super(documento, nombre, fechaNacimiento, id, usuario, contraseña);
        this.casino = casino;
    }

    public void ponerEnMantenimiento(MaquinaTragaMoneda maquina) {
        exigir(CAMBIAR_ESTADO_MAQUINA);
        maquina.cerrar();
    }

    public void returnToService(MaquinaTragaMoneda machine) {
        exigir(CAMBIAR_ESTADO_MAQUINA);
        machine.abrir();
    }

    public MaquinaTragaMoneda registrarMaquinasTragaMonedas(String codigo, double costoPorGiro) {
        exigir(REGISTRAR_MAQUINA);
        MaquinaTragaMoneda maquina = new MaquinaTragaMoneda(codigo, costoPorGiro);
        if (!casino.agregarMaquina(maquina)) {
            throw new IllegalArgumentException("Ya existe una máquina con el código " + codigo + ".");
        }
        return maquina;
    }

    @Override
    public Boolean puedeRealizar(String accion) {
        return CAMBIAR_ESTADO_MAQUINA.equals(accion) || REGISTRAR_MAQUINA.equals(accion);
    }

    @Override
    public String getCargo() {
        return "Técnico de máquinas";
    }
}
