package ui;

import modelos.Administrador;
import modelos.Casino;
import modelos.Trabajador;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** Registro de empleados con su cargo (RF-02, RF-03). Solo la usa el administrador. */
public class SeccionEmpleados extends Seccion {

    private final Casino casino;
    private final DefaultTableModel modelo = Componentes.modelo("Id", "Nombre", "Documento", "Cargo", "Usuario");

    public SeccionEmpleados(Casino casino, Administrador admin) {
        this.casino = casino;
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Empleados"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("El usuario es único y la contraseña tiene mínimo 6 caracteres. "
                + "El cargo define qué puede hacer cada empleado."), BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        JPanel lista = Componentes.tarjeta(new BorderLayout(0, 10));
        lista.add(Componentes.subtitulo("Personal del casino"), BorderLayout.NORTH);
        lista.add(Componentes.scroll(Componentes.tabla(modelo)), BorderLayout.CENTER);
        add(lista, BorderLayout.CENTER);

        JTextField documento = Componentes.campo(14);
        JTextField nombre = Componentes.campo(14);
        JTextField nacimiento = Componentes.campo(14);
        JComboBox<String> cargo = Componentes.combo();
        for (String c : new String[]{"Cajero", "Crupier", "Técnico de máquinas", "Administrador"}) {
            cargo.addItem(c);
        }
        JTextField usuario = Componentes.campo(14);
        JPasswordField contraseña = new JPasswordField(14);
        contraseña.setBackground(Tema.PANEL_CLARO);
        contraseña.setForeground(Tema.CREMA);
        contraseña.setCaretColor(Tema.ORO_CLARO);
        contraseña.setBorder(documento.getBorder());
        BotonCasino registrar = new BotonCasino("Registrar empleado", BotonCasino.Estilo.PRIMARIO);
        registrar.addActionListener(e -> intentar(() -> {
            Trabajador nuevo = admin.definirCargoEmpleado((String) cargo.getSelectedItem(), documento.getText(),
                    nombre.getText(), Componentes.fecha(nacimiento.getText()), usuario.getText(),
                    new String(contraseña.getPassword()));
            if (!admin.registrarEmpleados(nuevo)) {
                throw new IllegalArgumentException("El usuario \"" + usuario.getText().trim() + "\" ya existe.");
            }
            for (JTextField f : new JTextField[]{documento, nombre, nacimiento, usuario, contraseña}) {
                f.setText("");
            }
        }));
        JPanel formulario = Componentes.tarjeta(new BorderLayout(0, 10));
        formulario.add(Componentes.subtitulo("Nuevo empleado"), BorderLayout.NORTH);
        formulario.add(Componentes.formulario("Documento", documento, "Nombre", nombre,
                "Nacimiento (AAAA-MM-DD)", nacimiento, "Cargo", cargo, "Usuario", usuario,
                "Contraseña", contraseña), BorderLayout.CENTER);
        formulario.add(registrar, BorderLayout.SOUTH);
        JPanel lado = Componentes.bloque(0);
        lado.add(formulario, BorderLayout.NORTH);
        add(lado, BorderLayout.EAST);
        refrescar();
    }

    @Override
    public void refrescar() {
        modelo.setRowCount(0);
        for (Trabajador t : casino.getEmpleados()) {
            modelo.addRow(new Object[]{t.getId(), t.getNombre(), t.getDocumento(), t.getCargo(), t.getUsuario()});
        }
    }
}
