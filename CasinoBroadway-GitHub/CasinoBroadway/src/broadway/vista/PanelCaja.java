package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.modelo.Caja;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.Movimiento;
import broadway.modelo.OperacionInvalidaException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.LongSupplier;

/** La caja: compra y venta de fichas, cashback e historial de movimientos. */
public class PanelCaja extends PanelFieltro {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final VentanaPrincipal ventana;
    private final JTextField montoCompra = campoMonto();
    private final JTextField montoVenta = campoMonto();
    private final JLabel cashbackPendiente = Tema.etiqueta("$0", Tema.negrita(34), Tema.EXITO);
    private final JLabel cashbackNivel = Tema.etiqueta(" ", Tema.texto(13), Tema.TEXTO_SUAVE);
    private final BotonCasino reclamar = new BotonCasino("Reclamar cashback", BotonCasino.Estilo.PRIMARIO);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Fecha", "Movimiento", "Detalle", "Efectivo", "Fichas"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public PanelCaja(VentanaPrincipal ventana) {
        super(new BorderLayout(20, 0));
        this.ventana = ventana;
        setBorder(Tema.margen(18, 26, 18, 26));

        JPanel izquierda = new JPanel();
        izquierda.setOpaque(false);
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));
        izquierda.add(Tema.etiqueta("Caja", Tema.titulo(30), Tema.ORO_CLARO));
        izquierda.add(Tema.etiqueta("1 peso = 1 ficha. Montos en múltiplos de $1.000.", Tema.texto(13), Tema.CREMA));
        izquierda.add(espacio(12));

        izquierda.add(tarjeta("Comprar fichas con efectivo", montoCompra,
                new long[]{10_000, 50_000, 100_000, 500_000},
                () -> jugador().getDinero() / Caja.FICHA_MINIMA * Caja.FICHA_MINIMA, "Todo",
                "Comprar fichas", this::comprar));
        izquierda.add(espacio(12));
        izquierda.add(tarjeta("Cambiar fichas por efectivo", montoVenta,
                new long[]{10_000, 50_000, 100_000, 500_000},
                () -> jugador().getFichas() / Caja.FICHA_MINIMA * Caja.FICHA_MINIMA, "Todo",
                "Cambiar a efectivo", this::vender));
        izquierda.add(espacio(12));

        JPanel cb = caja();
        cb.setLayout(new BoxLayout(cb, BoxLayout.Y_AXIS));
        cb.add(Tema.etiqueta("Cashback pendiente", Tema.negrita(16), Tema.ORO_CLARO));
        cb.add(cashbackPendiente);
        cb.add(cashbackNivel);
        cb.add(espacio(8));
        reclamar.addActionListener(e -> reclamar());
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.add(reclamar);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.add(fila);
        izquierda.add(cb);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(izquierda, BorderLayout.NORTH);
        envoltura.setPreferredSize(new Dimension(470, 10));
        add(envoltura, BorderLayout.WEST);

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(Tema.texto(13));
        tabla.getTableHeader().setFont(Tema.negrita(13));
        tabla.setShowVerticalLines(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(380);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
        DefaultTableCellRenderer montos = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                String texto = String.valueOf(v);
                setHorizontalAlignment(RIGHT);
                if (!s) {
                    comp.setForeground(texto.startsWith("+") ? Tema.EXITO : texto.startsWith("-") ? Tema.ERROR : Tema.TEXTO_SUAVE);
                }
                return comp;
            }
        };
        tabla.getColumnModel().getColumn(3).setCellRenderer(montos);
        tabla.getColumnModel().getColumn(4).setCellRenderer(montos);
        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.setOpaque(false);
        derecha.add(Tema.etiqueta("Historial de movimientos (el más reciente arriba)", Tema.negrita(16), Tema.ORO_CLARO), BorderLayout.NORTH);
        derecha.add(Dialogos.scroll(tabla), BorderLayout.CENTER);
        add(derecha, BorderLayout.CENTER);
    }

    private Jugador jugador() {
        return ventana.getCasino().getJugador();
    }

    private JPanel tarjeta(String titulo, JTextField campo, long[] rapidos, LongSupplier todo, String textoTodo,
                           String textoAccion, Runnable accion) {
        JPanel p = caja();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Tema.etiqueta(titulo, Tema.negrita(16), Tema.ORO_CLARO));
        p.add(espacio(6));
        JPanel filaCampo = new JPanel(new BorderLayout(8, 0));
        filaCampo.setOpaque(false);
        filaCampo.add(Tema.etiqueta("$", Tema.negrita(18), Tema.CREMA), BorderLayout.WEST);
        filaCampo.add(campo, BorderLayout.CENTER);
        BotonCasino hacer = new BotonCasino(textoAccion, BotonCasino.Estilo.PRIMARIO);
        hacer.addActionListener(e -> accion.run());
        campo.addActionListener(e -> accion.run());
        filaCampo.add(hacer, BorderLayout.EAST);
        filaCampo.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaCampo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(filaCampo);
        p.add(espacio(8));
        JPanel botones = new JPanel(new GridLayout(1, rapidos.length + 1, 6, 0));
        botones.setOpaque(false);
        for (long r : rapidos) {
            BotonCasino b = new BotonCasino("+" + Dibujo.textoFicha(r));
            b.setFont(Tema.negrita(13));
            b.addActionListener(e -> campo.setText(Formato.numero(leer(campo) + r)));
            botones.add(b);
        }
        BotonCasino t = new BotonCasino(textoTodo);
        t.setFont(Tema.negrita(13));
        t.setToolTipText(textoTodo.equals("Todo") ? "Poner el máximo que puedes" : null);
        t.addActionListener(e -> campo.setText(Formato.numero(todo.getAsLong())));
        botones.add(t);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(botones);
        return p;
    }

    private void comprar() {
        operar(() -> {
            long monto = leerObligatorio(montoCompra);
            ventana.getCasino().getCaja().venderFichas(jugador(), monto);
            montoCompra.setText("");
            Sonidos.reproducir(Efecto.CAJA);
            Sonidos.reproducir(Efecto.FICHAS, 350);
            ventana.aviso("Compraste " + Formato.pesos(monto) + " en fichas.", Tema.EXITO);
        });
    }

    private void vender() {
        operar(() -> {
            long monto = leerObligatorio(montoVenta);
            ventana.getCasino().getCaja().comprarFichas(jugador(), monto);
            montoVenta.setText("");
            Sonidos.reproducir(Efecto.FICHAS);
            Sonidos.reproducir(Efecto.CAJA, 250);
            ventana.aviso("Recibiste " + Formato.pesos(monto) + " en efectivo.", Tema.EXITO);
        });
    }

    private void reclamar() {
        operar(() -> {
            long monto = ventana.getCasino().getCaja().entregarCashback(jugador());
            Sonidos.reproducir(Efecto.CAJA);
            Sonidos.reproducir(Efecto.GANAR, 300);
            ventana.aviso("Se acreditaron " + Formato.pesos(monto) + " de cashback a tus fichas.", Tema.EXITO);
        });
    }

    private void operar(Runnable r) {
        try {
            r.run();
            ventana.guardarEnSilencio();
        } catch (OperacionInvalidaException ex) {
            ventana.aviso(ex.getMessage(), Tema.ERROR);
        }
        actualizar();
        ventana.actualizarSaldo();
    }

    private static long leer(JTextField campo) {
        String limpio = campo.getText().replaceAll("[^0-9]", "");
        if (limpio.isEmpty()) {
            return 0;
        }
        if (limpio.length() > 15) {
            throw new OperacionInvalidaException("El monto es demasiado grande.");
        }
        return Long.parseLong(limpio);
    }

    private static long leerObligatorio(JTextField campo) {
        if (campo.getText().replaceAll("[^0-9]", "").isEmpty()) {
            throw new OperacionInvalidaException("Escribe un monto (por ejemplo 100.000).");
        }
        return leer(campo);
    }

    public void actualizar() {
        Jugador j = jugador();
        if (j == null) {
            return;
        }
        cashbackPendiente.setText(Formato.pesos(j.getCashbackPendiente()));
        cashbackNivel.setText("Nivel " + j.getNivel().getNombre() + ": recibes el " + j.getNivel().getPorcentajeCashback()
                + "% de lo que pierdes. Total recibido: " + Formato.pesos(j.getCashbackTotalRecibido()));
        reclamar.setEnabled(j.getCashbackPendiente() > 0);
        modelo.setRowCount(0);
        List<Movimiento> h = j.getHistorial();
        for (int i = h.size() - 1; i >= 0; i--) {
            Movimiento m = h.get(i);
            modelo.addRow(new Object[]{m.getFecha().format(FECHA), m.getTipo().getDescripcion(), m.getDetalle(),
                    signo(m.getVariacionDinero()), signo(m.getVariacionFichas())});
        }
    }

    private static String signo(long v) {
        if (v == 0) {
            return "—";
        }
        return (v > 0 ? "+" : "-") + Formato.pesos(Math.abs(v));
    }

    private static JTextField campoMonto() {
        JTextField t = new JTextField(10);
        t.setFont(Tema.negrita(18));
        t.setBackground(Tema.PANEL_CLARO);
        t.setForeground(Tema.CREMA);
        t.setCaretColor(Tema.ORO_CLARO);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE), Tema.margen(4, 8, 4, 8)));
        return t;
    }

    private static JComponent espacio(int alto) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(1, alto));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JPanel caja() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(new Color(7, 26, 19, 225));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.setColor(Tema.BORDE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(Tema.margen(14, 16, 14, 16));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }
}
