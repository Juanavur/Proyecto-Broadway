package ui;

import modelos.Administrador;
import modelos.Casino;
import modelos.Crupier;
import servicios.Formato;
import modelos.Juego;
import modelos.Mesa;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** Creación de mesas y su apertura o cierre (RF-04, RF-05). Solo la usa el administrador. */
public class SeccionMesas extends Seccion {

    private final Casino casino;
    private final DefaultTableModel modelo = Componentes.modelo("Código", "Juego", "Mínima", "Máxima",
            "Capacidad", "Crupier", "Estado", "Rondas");
    private final JTable tabla = Componentes.tabla(modelo);
    private final JComboBox<Crupier> crupier = Componentes.combo();

    public SeccionMesas(Casino casino, Administrador admin) {
        this.casino = casino;
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Mesas de juego"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("Código único, mínima menor que la máxima, de 1 a 7 jugadores y un crupier asignado. "
                + "Máximo " + Casino.totalMesas + " mesas."), BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        BotonCasino abrir = new BotonCasino("Abrir mesa");
        BotonCasino cerrar = new BotonCasino("Cerrar mesa", BotonCasino.Estilo.PELIGRO);
        abrir.addActionListener(e -> intentar(() -> seleccionada().abrir()));
        cerrar.addActionListener(e -> intentar(() -> seleccionada().cerrar()));
        JPanel lista = Componentes.tarjeta(new BorderLayout(0, 10));
        lista.add(Componentes.subtitulo("Mesas del casino"), BorderLayout.NORTH);
        lista.add(Componentes.scroll(tabla), BorderLayout.CENTER);
        lista.add(Componentes.fila(abrir, cerrar), BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        JTextField codigo = Componentes.campo(12);
        JComboBox<Juego> juego = Componentes.combo();
        for (Juego j : casino.getJuegos()) {
            juego.addItem(j);
        }
        JTextField minima = Componentes.campo(12);
        JTextField maxima = Componentes.campo(12);
        minima.setText("10.000");
        maxima.setText("500.000");
        JSpinner capacidad = new JSpinner(new SpinnerNumberModel(5, 1, 7, 1));
        BotonCasino crear = new BotonCasino("Crear mesa", BotonCasino.Estilo.PRIMARIO);
        crear.addActionListener(e -> intentar(() -> {
            Mesa mesa = admin.crearMesa(codigo.getText(), (Juego) juego.getSelectedItem(),
                    Componentes.monto(minima.getText()), Componentes.monto(maxima.getText()),
                    (Integer) capacidad.getValue(), (Crupier) crupier.getSelectedItem());
            if (!admin.registrarMesaDisponible(mesa)) {
                throw new IllegalArgumentException("Ya existe la mesa " + mesa.getCodigoMesa() + ".");
            }
            mesa.abrir();
            codigo.setText("");
        }));
        JPanel formulario = Componentes.tarjeta(new BorderLayout(0, 10));
        formulario.add(Componentes.subtitulo("Nueva mesa"), BorderLayout.NORTH);
        formulario.add(Componentes.formulario("Código", codigo, "Juego", juego, "Apuesta mínima", minima,
                "Apuesta máxima", maxima, "Capacidad", capacidad, "Crupier", crupier), BorderLayout.CENTER);
        formulario.add(crear, BorderLayout.SOUTH);
        JPanel lado = Componentes.bloque(0);
        lado.add(formulario, BorderLayout.NORTH);
        add(lado, BorderLayout.EAST);
        refrescar();
    }

    private Mesa seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            throw new IllegalArgumentException("Seleccione una mesa en la tabla.");
        }
        return casino.getMesas().get(fila);
    }

    @Override
    public void refrescar() {
        int fila = tabla.getSelectedRow();
        modelo.setRowCount(0);
        for (Mesa m : casino.getMesas()) {
            modelo.addRow(new Object[]{m.getCodigoMesa(), m.getJuegoAsignado(), Formato.dinero(m.getApuestaMinima()),
                    Formato.dinero(m.getApuestaMaxima()), m.getJugadoresMaximos(),
                    m.getCrupierAsignado() == null ? "-" : m.getCrupierAsignado().getNombre(),
                    m.estaDisponible() ? "Abierta" : "Cerrada", m.getRondasJugadas()});
        }
        if (fila >= 0 && fila < modelo.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
        Object elegido = crupier.getSelectedItem();
        crupier.removeAllItems();
        for (Crupier c : casino.getCrupieres()) {
            crupier.addItem(c);
        }
        if (elegido != null) {
            crupier.setSelectedItem(elegido);
        }
    }
}
