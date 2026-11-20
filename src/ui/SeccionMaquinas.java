package ui;

import modelos.Casino;
import servicios.Formato;
import modelos.MaquinaTragaMoneda;
import modelos.TecnicoMaquina;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** Registro de máquinas y mantenimiento (RF-09). Solo la usa el técnico. */
public class SeccionMaquinas extends Seccion {

    private final Casino casino;
    private final DefaultTableModel modelo = Componentes.modelo("Código", "Valor por giro", "Estado", "Jugadas", "Premios pagados");
    private final JTable tabla = Componentes.tabla(modelo);

    public SeccionMaquinas(Casino casino, TecnicoMaquina tecnico) {
        this.casino = casino;
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Máquinas tragamonedas"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales paga 10 veces el giro."),
                BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        BotonCasino estado = new BotonCasino("Poner en mantenimiento / volver a servicio");
        estado.addActionListener(e -> intentar(() -> {
            MaquinaTragaMoneda machine = seleccionada();
            if (machine.estaDisponible()) {
                tecnico.ponerEnMantenimiento(machine);
            } else {
                tecnico.returnToService(machine);
            }
        }));
        JPanel lista = Componentes.tarjeta(new BorderLayout(0, 10));
        lista.add(Componentes.subtitulo("Máquinas del casino"), BorderLayout.NORTH);
        lista.add(Componentes.scroll(tabla), BorderLayout.CENTER);
        lista.add(Componentes.fila(estado), BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        JTextField codigo = Componentes.campo(12);
        JTextField costo = Componentes.campo(12);
        costo.setText("2.000");
        BotonCasino registrar = new BotonCasino("Registrar máquina", BotonCasino.Estilo.PRIMARIO);
        registrar.addActionListener(e -> intentar(() -> {
            tecnico.registrarMaquinasTragaMonedas(codigo.getText(), Componentes.monto(costo.getText()));
            codigo.setText("");
        }));
        JPanel formulario = Componentes.tarjeta(new BorderLayout(0, 10));
        formulario.add(Componentes.subtitulo("Nueva máquina"), BorderLayout.NORTH);
        formulario.add(Componentes.formulario("Código", codigo, "Valor por giro", costo), BorderLayout.CENTER);
        formulario.add(registrar, BorderLayout.SOUTH);
        JPanel lado = Componentes.bloque(0);
        lado.add(formulario, BorderLayout.NORTH);
        add(lado, BorderLayout.EAST);
        refrescar();
    }

    private MaquinaTragaMoneda seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            throw new IllegalArgumentException("Seleccione una máquina en la tabla.");
        }
        return casino.getMaquinas().get(fila);
    }

    @Override
    public void refrescar() {
        int fila = tabla.getSelectedRow();
        modelo.setRowCount(0);
        for (MaquinaTragaMoneda m : casino.getMaquinas()) {
            double premios = 0;
            for (MaquinaTragaMoneda.RondaMaquina r : m.getJugadas()) {
                premios += r.getPremio();
            }
            modelo.addRow(new Object[]{m.getCodigoMaquina(), Formato.dinero(m.getCostoPorGiro()),
                    m.estaDisponible() ? "En servicio" : "En mantenimiento", m.getJugadas().size(), Formato.dinero(premios)});
        }
        if (fila >= 0 && fila < modelo.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
    }
}
