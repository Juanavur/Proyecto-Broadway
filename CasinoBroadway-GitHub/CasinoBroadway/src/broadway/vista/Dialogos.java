package broadway.vista;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

/** Diálogos con el estilo del casino. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void mensaje(Component padre, String titulo, String texto) {
        mostrar(padre, titulo, texto, false, "Aceptar", null);
    }

    public static void error(Component padre, String texto) {
        mostrar(padre, "No se pudo", texto, false, "Entendido", Tema.ERROR);
    }

    /** Pregunta sí/no. Devuelve true si el jugador acepta. */
    public static boolean confirmar(Component padre, String titulo, String texto, String textoAceptar) {
        return mostrar(padre, titulo, texto, true, textoAceptar, null);
    }

    /** Texto largo (por ejemplo, las reglas de un juego) con barra de desplazamiento. */
    public static void textoLargo(Component padre, String titulo, String texto) {
        Window ventana = SwingUtilities.getWindowAncestor(padre);
        JDialog d = new JDialog(ventana, titulo, JDialog.ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(Tema.mono(14));
        area.setBackground(Tema.PANEL);
        area.setForeground(Tema.CREMA);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(Tema.margen(16, 18, 16, 18));
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(760, 520));
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.PANEL);
        raiz.setBorder(BorderFactory.createLineBorder(Tema.ORO_OSCURO, 2));
        JLabel encabezado = Tema.etiqueta(titulo, Tema.titulo(22), Tema.ORO_CLARO);
        encabezado.setBorder(Tema.margen(14, 18, 4, 18));
        raiz.add(encabezado, BorderLayout.NORTH);
        raiz.add(scroll, BorderLayout.CENTER);
        BotonCasino cerrar = new BotonCasino("Cerrar", BotonCasino.Estilo.PRIMARIO);
        cerrar.addActionListener(e -> d.dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.setBackground(Tema.PANEL);
        pie.add(cerrar);
        raiz.add(pie, BorderLayout.SOUTH);
        d.setContentPane(raiz);
        d.pack();
        d.setLocationRelativeTo(ventana);
        d.setVisible(true);
    }

    private static boolean mostrar(Component padre, String titulo, String texto, boolean confirmacion,
                                   String textoSi, Color colorTitulo) {
        Window ventana = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        if (ventana == null && padre instanceof Window) {
            ventana = (Window) padre;
        }
        JDialog d = new JDialog(ventana, titulo, JDialog.ModalityType.APPLICATION_MODAL);
        boolean[] respuesta = {false};
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Tema.PANEL);
        raiz.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.ORO_OSCURO, 2), Tema.margen(18, 22, 16, 22)));
        JLabel t = Tema.etiqueta(titulo, Tema.titulo(20), colorTitulo == null ? Tema.ORO_CLARO : colorTitulo);
        raiz.add(t, BorderLayout.NORTH);
        JTextArea cuerpo = new JTextArea(texto);
        cuerpo.setEditable(false);
        cuerpo.setOpaque(false);
        cuerpo.setLineWrap(true);
        cuerpo.setWrapStyleWord(true);
        cuerpo.setFont(Tema.texto(15));
        cuerpo.setForeground(Tema.CREMA);
        cuerpo.setColumns(34);
        raiz.add(cuerpo, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        if (confirmacion) {
            BotonCasino no = new BotonCasino("Cancelar");
            no.addActionListener(e -> d.dispose());
            botones.add(no);
        }
        BotonCasino si = new BotonCasino(textoSi, BotonCasino.Estilo.PRIMARIO);
        si.addActionListener(e -> {
            respuesta[0] = true;
            d.dispose();
        });
        botones.add(si);
        raiz.add(botones, BorderLayout.SOUTH);
        d.setContentPane(raiz);
        d.getRootPane().setDefaultButton(si);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(ventana);
        d.setVisible(true);
        return respuesta[0];
    }

    /** Ajusta el estilo de un componente de texto o tabla dentro de un scroll. */
    public static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        s.getViewport().setBackground(Tema.PANEL);
        return s;
    }
}
