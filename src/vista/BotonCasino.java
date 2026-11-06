package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Botón redondeado con los colores del casino. */
public class BotonCasino extends JButton {

    public enum Estilo { PRIMARIO, SECUNDARIO, PELIGRO }

    private final Estilo estilo;
    private boolean encima;

    public BotonCasino(String texto, Estilo estilo) {
        super(texto);
        this.estilo = estilo;
        setFont(Tema.negrita(14));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(estilo == Estilo.PRIMARIO ? new Color(0x2B1D00) : Tema.CREMA);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                encima = true;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    Sonidos.reproducir(Efecto.CLIC);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                repaint();
            }
        });
    }

    public BotonCasino(String texto) {
        this(texto, Estilo.SECUNDARIO);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 36, fm.getHeight() + 16);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        int arco = Math.min(h, 22);
        boolean activo = isEnabled();
        boolean presionado = getModel().isPressed();
        Color a;
        Color b;
        Color borde;
        switch (estilo) {
            case PRIMARIO:
                a = encima ? new Color(0xFFE9A3) : Tema.ORO_CLARO;
                b = presionado ? Tema.ORO_OSCURO : Tema.ORO;
                borde = new Color(0x8C6D1A);
                break;
            case PELIGRO:
                a = encima ? Tema.VINO_CLARO.brighter() : Tema.VINO_CLARO;
                b = Tema.VINO;
                borde = new Color(0x4A0F18);
                break;
            default:
                a = encima ? new Color(0x2B6A52) : Tema.PANEL_CLARO;
                b = presionado ? Tema.PANEL : new Color(0x15392D);
                borde = encima ? Tema.ORO : Tema.BORDE;
        }
        if (!activo) {
            a = new Color(0x2A3832);
            b = new Color(0x1E2A25);
            borde = new Color(0x34443D);
        }
        g.setPaint(new GradientPaint(0, 0, a, 0, h, b));
        g.fillRoundRect(0, 0, w - 1, h - 1, arco, arco);
        g.setColor(borde);
        g.drawRoundRect(0, 0, w - 1, h - 1, arco, arco);
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(activo ? getForeground() : new Color(0x6F7F78));
        g.drawString(getText(), tx, ty);
        g.dispose();
    }
}
