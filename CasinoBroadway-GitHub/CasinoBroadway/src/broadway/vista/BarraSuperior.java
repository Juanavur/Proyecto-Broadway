package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Barra fija arriba: logo, jugador, nivel, efectivo, fichas y cashback pendiente. */
public class BarraSuperior extends JPanel {

    private final JLabel nombre = Tema.etiqueta("", Tema.negrita(15), Tema.CREMA);
    private final InsigniaNivel insignia = new InsigniaNivel();
    private final JLabel efectivo = valor();
    private final JLabel fichas = valor();
    private final JLabel cashback = valor();
    private final BotonCasino lobby = new BotonCasino("Lobby");
    private final BotonCasino caja = new BotonCasino("Caja");
    private final BotonCasino sonido = new BotonCasino("");

    public BarraSuperior(Runnable irLobby, Runnable irCaja) {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.ORO_OSCURO), Tema.margen(8, 16, 8, 16)));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        izquierda.setOpaque(false);
        JLabel logo = Tema.etiqueta("BROADWAY", Tema.titulo(26), Tema.ORO);
        logo.setToolTipText("Casino Broadway");
        izquierda.add(logo);
        JPanel quien = new JPanel();
        quien.setOpaque(false);
        quien.setLayout(new BoxLayout(quien, BoxLayout.Y_AXIS));
        nombre.setAlignmentX(LEFT_ALIGNMENT);
        insignia.setAlignmentX(LEFT_ALIGNMENT);
        quien.add(nombre);
        quien.add(insignia);
        izquierda.add(quien);
        add(izquierda, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        derecha.setOpaque(false);
        derecha.add(dato("EFECTIVO", efectivo));
        derecha.add(dato("FICHAS", fichas));
        derecha.add(dato("CASHBACK PENDIENTE", cashback));
        derecha.add(Box.createHorizontalStrut(6));
        caja.addActionListener(e -> irCaja.run());
        lobby.addActionListener(e -> irLobby.run());
        sonido.setFont(Tema.negrita(13));
        textoSonido();
        sonido.setToolTipText("Activar o silenciar los sonidos del casino");
        sonido.addActionListener(e -> {
            Sonidos.setSilencio(!Sonidos.isSilencio());
            textoSonido();
        });
        derecha.add(sonido);
        derecha.add(caja);
        derecha.add(lobby);
        add(derecha, BorderLayout.EAST);
    }

    private void textoSonido() {
        sonido.setText(Sonidos.isSilencio() ? "\u266A Sin sonido" : "\u266A Sonido");
        sonido.revalidate();
        sonido.repaint();
    }

    private static JLabel valor() {
        return Tema.etiqueta("$0", Tema.negrita(18), Tema.CREMA);
    }

    private static JComponent dato(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = Tema.etiqueta(titulo, Tema.negrita(10), Tema.TEXTO_SUAVE);
        p.add(t);
        p.add(valor);
        return p;
    }

    public void actualizar(Jugador j) {
        if (j == null) {
            return;
        }
        nombre.setText(j.getNombre());
        insignia.setJugador(j);
        efectivo.setText(Formato.pesos(j.getDinero()));
        fichas.setText(Formato.pesos(j.getFichas()));
        cashback.setText(Formato.pesos(j.getCashbackPendiente()));
        cashback.setForeground(j.getCashbackPendiente() > 0 ? Tema.EXITO : Tema.CREMA);
        fichas.setForeground(Tema.ORO_CLARO);
    }

    public void setBotonesVisibles(boolean lobbyVisible, boolean cajaVisible) {
        lobby.setVisible(lobbyVisible);
        caja.setVisible(cajaVisible);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setPaint(new GradientPaint(0, 0, new Color(0x173D30), 0, getHeight(), new Color(0x0C241B)));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }

    /** Pequeña insignia con el color del nivel de fidelidad. */
    private static class InsigniaNivel extends JComponent {
        private Jugador jugador;

        void setJugador(Jugador j) {
            this.jugador = j;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(210, 22);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (jugador == null) {
                return;
            }
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            Color c = Tema.colorNivel(jugador.getNivel());
            String texto = jugador.getNivel().getNombre().toUpperCase() + " · " + jugador.getNivel().getPorcentajeCashback() + "% CASHBACK";
            g.setFont(Tema.negrita(11));
            int w = g.getFontMetrics().stringWidth(texto) + 18;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g.fillRoundRect(0, 2, w, 18, 18, 18);
            g.setColor(c);
            g.drawRoundRect(0, 2, w, 18, 18, 18);
            g.drawString(texto, 9, 15);
            g.dispose();
        }
    }
}
