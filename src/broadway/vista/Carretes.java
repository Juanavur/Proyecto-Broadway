package broadway.vista;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.util.Random;

/** Dibujo de los tres carretes de una tragamonedas, con animación de giro. */
public class Carretes extends JComponent {

    private final Random azar = new Random();
    private int[] simbolos = {4, 4, 4};
    private Timer timer;
    private boolean premio;

    public Carretes() {
        setPreferredSize(new Dimension(520, 300));
        setOpaque(false);
    }

    public boolean girando() {
        return timer != null && timer.isRunning();
    }

    /** Gira un momento y deja los símbolos indicados; los carretes paran uno tras otro. */
    public void girarHasta(int[] destino, boolean gano, Runnable alTerminar) {
        premio = false;
        long t0 = System.currentTimeMillis();
        timer = new Timer(70, e -> {
            long t = System.currentTimeMillis() - t0;
            for (int i = 0; i < 3; i++) {
                simbolos[i] = t > 900 + i * 350L ? destino[i] : azar.nextInt(6);
            }
            repaint();
            if (t > 900 + 2 * 350L) {
                ((Timer) e.getSource()).stop();
                premio = gano;
                repaint();
                alTerminar.run();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        int mw = Math.min(w - 20, 540);
        int mh = Math.min(h - 20, 300);
        int x0 = (w - mw) / 2;
        int y0 = (h - mh) / 2;
        g.setPaint(new GradientPaint(0, y0, Tema.VINO_CLARO, 0, y0 + mh, Tema.VINO));
        g.fillRoundRect(x0, y0, mw, mh, 40, 40);
        g.setColor(premio ? Tema.ORO_CLARO : Tema.ORO);
        g.setStroke(new BasicStroke(premio ? 7f : 4f));
        g.drawRoundRect(x0, y0, mw, mh, 40, 40);
        int margen = 26;
        int cw = (mw - margen * 4) / 3;
        int ch = mh - 2 * margen - 20;
        for (int i = 0; i < 3; i++) {
            int cx = x0 + margen + i * (cw + margen);
            int cy = y0 + margen + 10;
            g.setPaint(new GradientPaint(0, cy, new Color(0xE9E4D8), 0, cy + ch / 2f, Color.WHITE, true));
            g.fillRoundRect(cx, cy, cw, ch, 18, 18);
            g.setColor(Tema.ORO_OSCURO);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(cx, cy, cw, ch, 18, 18);
            dibujarSimbolo(g, simbolos[i], cx + cw / 2, cy + ch / 2, Math.min(cw, ch) * 0.32f);
        }
        g.dispose();
    }

    /** 0 cereza, 1 limón, 2 campana, 3 estrella, 4 siete, 5 diamante. */
    private static void dibujarSimbolo(Graphics2D g, int s, int cx, int cy, float r) {
        switch (s) {
            case 0:
                g.setColor(new Color(0x2E7D32));
                g.setStroke(new BasicStroke(r * 0.12f));
                g.drawLine(cx - (int) (r * 0.45), cy, cx, cy - (int) r);
                g.drawLine(cx + (int) (r * 0.45), cy, cx, cy - (int) r);
                g.setColor(new Color(0xD32F2F));
                g.fill(new Ellipse2D.Float(cx - r * 0.95f, cy - r * 0.1f, r * 0.9f, r * 0.9f));
                g.fill(new Ellipse2D.Float(cx + r * 0.05f, cy - r * 0.1f, r * 0.9f, r * 0.9f));
                break;
            case 1:
                g.setColor(new Color(0xF4D03F));
                g.fill(new Ellipse2D.Float(cx - r, cy - r * 0.7f, 2 * r, 1.4f * r));
                g.setColor(new Color(0xC9A400));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Float(cx - r, cy - r * 0.7f, 2 * r, 1.4f * r));
                break;
            case 2:
                g.setColor(new Color(0xF1B82D));
                g.fillArc(cx - (int) (r * 0.8), cy - (int) r, (int) (r * 1.6), (int) (r * 1.8), 0, 180);
                g.fillRect(cx - (int) (r * 0.8), cy - (int) (r * 0.1), (int) (r * 1.6), (int) (r * 0.6));
                g.fillRoundRect(cx - (int) r, cy + (int) (r * 0.4), (int) (2 * r), (int) (r * 0.3), 8, 8);
                g.setColor(new Color(0x9C6B00));
                g.fill(new Ellipse2D.Float(cx - r * 0.2f, cy + r * 0.62f, r * 0.4f, r * 0.4f));
                break;
            case 3:
                g.setColor(new Color(0xFFC107));
                g.fill(estrella(cx, cy, r, r * 0.45f));
                break;
            case 4:
                g.setColor(new Color(0xC62828));
                g.setFont(Tema.titulo(r * 2.2f));
                String siete = "7";
                g.drawString(siete, cx - g.getFontMetrics().stringWidth(siete) / 2, cy + (int) (r * 0.75));
                break;
            default:
                g.setColor(new Color(0x29B6F6));
                Polygon d = new Polygon();
                d.addPoint(cx, cy - (int) r);
                d.addPoint(cx + (int) (r * 0.8), cy);
                d.addPoint(cx, cy + (int) r);
                d.addPoint(cx - (int) (r * 0.8), cy);
                g.fill(d);
                g.setColor(new Color(0x0277BD));
                g.draw(d);
        }
    }

    private static Polygon estrella(int cx, int cy, float exterior, float interior) {
        Polygon p = new Polygon();
        for (int i = 0; i < 10; i++) {
            double a = Math.PI / 5 * i - Math.PI / 2;
            float r = i % 2 == 0 ? exterior : interior;
            p.addPoint(cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r));
        }
        return p;
    }
}
