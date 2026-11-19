package broadway.vista;

import broadway.modelo.Cajero;
import broadway.modelo.Casino;
import broadway.modelo.Cliente;
import broadway.modelo.Formato;
import broadway.modelo.Trabajador;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** Registro de clientes y venta/canje de fichas (RF-01, RF-06, RF-10). El administrador solo consulta. */
public class SeccionClientes extends Seccion {

    private final Casino casino;
    private final DefaultTableModel modelo =
            Componentes.modelo("Documento", "Nombre", "Edad", "Fichas", "Total apostado", "Nivel", "Pérdidas del periodo");
    private final JTable tabla = Componentes.tabla(modelo);

    public SeccionClientes(Casino casino, Trabajador usuario) {
        this.casino = casino;
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Clientes"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("Solo se registran mayores de " + Cliente.edadMinima
                + " años y no se repiten documentos. El nivel sube con el total apostado y nunca baja."), BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        JPanel centro = Componentes.tarjeta(new BorderLayout(0, 10));
        centro.add(Componentes.subtitulo("Clientes registrados"), BorderLayout.NORTH);
        centro.add(Componentes.scroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        if (usuario instanceof Cajero) {
            add(panelCajero((Cajero) usuario), BorderLayout.EAST);
        }
        refrescar();
    }

    private JPanel panelCajero(Cajero cajero) {
        JPanel pila = new JPanel(new BorderLayout(0, 14));
        pila.setOpaque(false);

        JTextField documento = Componentes.campo(14);
        JTextField nombre = Componentes.campo(14);
        JTextField nacimiento = Componentes.campo(14);
        nacimiento.setToolTipText("AAAA-MM-DD");
        BotonCasino registrar = new BotonCasino("Registrar cliente", BotonCasino.Estilo.PRIMARIO);
        registrar.addActionListener(e -> intentar(() -> {
            boolean nuevo = cajero.registrarClientes(documento.getText(), nombre.getText(),
                    Componentes.fecha(nacimiento.getText()));
            if (!nuevo) {
                throw new IllegalArgumentException("Ya hay un cliente con el documento " + documento.getText().trim() + ".");
            }
            documento.setText("");
            nombre.setText("");
            nacimiento.setText("");
        }));
        JPanel registro = Componentes.tarjeta(new BorderLayout(0, 10));
        registro.add(Componentes.subtitulo("Nuevo cliente"), BorderLayout.NORTH);
        registro.add(Componentes.formulario("Documento", documento, "Nombre", nombre,
                "Nacimiento (AAAA-MM-DD)", nacimiento), BorderLayout.CENTER);
        registro.add(registrar, BorderLayout.SOUTH);

        JTextField monto = Componentes.campo(10);
        monto.setText("100.000");
        BotonCasino vender = new BotonCasino("Vender fichas", BotonCasino.Estilo.PRIMARIO);
        BotonCasino canjear = new BotonCasino("Canjear por dinero");
        BotonCasino cashback = new BotonCasino("Entregar cashback");
        vender.addActionListener(e -> intentar(() -> cajero.venderFichas(seleccionado(), Componentes.monto(monto.getText()))));
        canjear.addActionListener(e -> intentar(() -> cajero.canjearFichas(seleccionado(), Componentes.monto(monto.getText()))));
        cashback.addActionListener(e -> intentar(() -> {
            Cliente c = seleccionado();
            double valor = c.reclamarCashback();
            Dialogos.mensaje(this, "Cashback", c.getNombre() + " (" + c.getNivel() + ") recibe "
                    + Formato.dinero(valor) + " en fichas.");
        }));
        JPanel fichas = Componentes.tarjeta(new BorderLayout(0, 10));
        fichas.add(Componentes.subtitulo("Fichas del cliente seleccionado"), BorderLayout.NORTH);
        fichas.add(Componentes.formulario("Monto", monto), BorderLayout.CENTER);
        JPanel botones = new JPanel(new java.awt.GridLayout(3, 1, 0, 6));
        botones.setOpaque(false);
        botones.add(vender);
        botones.add(canjear);
        botones.add(cashback);
        fichas.add(botones, BorderLayout.SOUTH);

        pila.add(registro, BorderLayout.NORTH);
        pila.add(fichas, BorderLayout.CENTER);
        JPanel lado = Componentes.bloque(0);
        lado.add(pila, BorderLayout.NORTH);
        return lado;
    }

    private Cliente seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            throw new IllegalArgumentException("Seleccione un cliente en la tabla.");
        }
        return casino.buscarClientePorDocumento((String) modelo.getValueAt(fila, 0));
    }

    @Override
    public void refrescar() {
        int fila = tabla.getSelectedRow();
        modelo.setRowCount(0);
        for (Cliente c : casino.getClientes()) {
            modelo.addRow(new Object[]{c.getDocumento(), c.getNombre(), c.getEdad(),
                    Formato.dinero(c.getSaldoFichas()), Formato.dinero(c.getTotalApostado()),
                    c.getNivel(), Formato.dinero(c.getPerdidasPeriodo())});
        }
        if (fila >= 0 && fila < modelo.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
    }
}
