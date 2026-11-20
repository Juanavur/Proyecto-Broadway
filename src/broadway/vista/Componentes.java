package broadway.vista;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Piezas de interfaz reutilizables con el estilo del casino (tarjetas, campos, tablas). */
public final class Componentes {

    private Componentes() {
    }

    /** Panel oscuro con borde dorado y esquinas redondeadas. */
    public static JPanel tarjeta(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(new Color(10, 30, 22, 225));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g.setColor(Tema.ORO_OSCURO);
                g.setStroke(new BasicStroke(1.6f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(Tema.margen(16, 18, 16, 18));
        return p;
    }

    public static JLabel titulo(String texto) {
        return Tema.etiqueta(texto, Tema.titulo(26), Tema.ORO_CLARO);
    }

    public static JLabel subtitulo(String texto) {
        return Tema.etiqueta(texto, Tema.negrita(15), Tema.ORO);
    }

    public static JLabel nota(String texto) {
        return Tema.etiqueta(texto, Tema.texto(13), Tema.TEXTO_SUAVE);
    }

    public static JTextField campo(int columnas) {
        JTextField f = new JTextField(columnas);
        f.setFont(Tema.texto(15));
        f.setBackground(Tema.PANEL_CLARO);
        f.setForeground(Tema.CREMA);
        f.setCaretColor(Tema.ORO_CLARO);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE), Tema.margen(6, 8, 6, 8)));
        return f;
    }

    public static <T> JComboBox<T> combo() {
        JComboBox<T> c = new JComboBox<>();
        c.setFont(Tema.texto(14));
        c.setBackground(Tema.PANEL_CLARO);
        c.setForeground(Tema.CREMA);
        return c;
    }

    /** Formulario de dos columnas: etiqueta a la izquierda, campo a la derecha. */
    public static JPanel formulario(Object... etiquetasYCampos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 10);
        for (int i = 0; i < etiquetasYCampos.length; i += 2) {
            c.gridy = i / 2;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_END;
            p.add(Tema.etiqueta((String) etiquetasYCampos[i], Tema.texto(14), Tema.CREMA), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add((Component) etiquetasYCampos[i + 1], c);
        }
        return p;
    }

    /** Tabla de solo lectura con el estilo del casino. */
    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        t.setRowHeight(28);
        t.setFont(Tema.texto(14));
        t.setBackground(Tema.PANEL);
        t.setForeground(Tema.CREMA);
        t.setGridColor(Tema.BORDE);
        t.setSelectionBackground(Tema.ORO_OSCURO);
        t.setSelectionForeground(Tema.CREMA);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(Tema.negrita(13));
        t.getTableHeader().setBackground(Tema.PANEL_CLARO);
        t.getTableHeader().setForeground(Tema.ORO_CLARO);
        t.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(Tema.margen(0, 8, 0, 8));
        t.setDefaultRenderer(Object.class, r);
        return t;
    }

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0);
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = Dialogos.scroll(c);
        s.setPreferredSize(new Dimension(400, 240));
        return s;
    }

    /** Panel transparente con BorderLayout y separación. */
    public static JPanel bloque(int separacion) {
        JPanel p = new JPanel(new BorderLayout(separacion, separacion));
        p.setOpaque(false);
        return p;
    }

    /** Fila de componentes alineados a la izquierda. */
    public static JPanel fila(Component... componentes) {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (Component c : componentes) {
            p.add(c);
        }
        return p;
    }

    /** Lee una fecha escrita como AAAA-MM-DD. */
    public static LocalDate fecha(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Escriba la fecha como AAAA-MM-DD, por ejemplo 1999-05-20.");
        }
    }

    /** Lee un monto en pesos; acepta puntos de miles (100.000). */
    public static double monto(String texto) {
        String limpio = texto.replace("$", "").replace(".", "").replace(",", "").trim();
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escriba un monto válido, por ejemplo 50.000.");
        }
    }
}
