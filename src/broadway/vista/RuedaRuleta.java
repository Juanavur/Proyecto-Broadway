package broadway.vista;

import broadway.modelo.Ruleta;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/** Dibujo animado de la rueda europea. Solo muestra: el número lo decide la clase Ruleta. */
public class RuedaRuleta extends JComponent {

    private static final int[] ORDEN = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5,
            24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    private double angulo;
    private Integer ultimo;
    private Timer timer;

    public RuedaRuleta() {
        setPreferredSize(new Dimension(420, 420));
        setOpaque(false);
    }

    public boolean girando() {
        return timer != null && timer.isRunning();
    }

    /** Gira varias vueltas y se detiene con el número indicado debajo del marcador. */
    public void girarHasta(int numero, Runnable alTerminar) {
        int indice = 0;
        for (int i = 0; i < ORDEN.length; i++) {
            if (ORDEN[i] == numero) {
                indice = i;
            }
        }
        double paso = 360.0 / ORDEN.length;
        double destinoBase = -indice * paso;
        double inicio = angulo % 360;
        double destino = destinoBase - 360 * 5;
        while (destino > inicio - 360 * 4) {
            destino -= 360;
        }
        final double desde = inicio;
        final double hasta = destino;
        final long t0 = System.currentTimeMillis();
        final int duracion = 3200;
        ultimo = null;
        timer = new Timer(16, e -> {
            double t = Math.min(1, (System.currentTimeMillis() - t0) / (double) duracion);
            double suave = 1 - Math.pow(1 - t, 3);
            angulo = desde + (hasta - desde) * suave;
            repaint();
            if (t >= 1) {
                ((Timer) e.getSource()).stop();
                ultimo = numero;
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
        int lado = Math.min(getWidth(), getHeight()) - 20;
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double r = lado / 2.0;

        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) r,
                new float[]{0.7f, 1f}, new Color[]{new Color(0x5A3412), new Color(0x2A160A)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(Tema.ORO);
        g.setStroke(new BasicStroke(4f));
        g.draw(new Ellipse2D.Double(cx - r + 2, cy - r + 2, 2 * r - 4, 2 * r - 4));

        double rn = r * 0.86;
        double paso = 360.0 / ORDEN.length;
        AffineTransform original = g.getTransform();
        for (int i = 0; i < ORDEN.length; i++) {
            int n = ORDEN[i];
            double inicio = 90 - paso / 2 - (i * paso + angulo);
            Color c = n == 0 ? Tema.VERDE_CERO : (Ruleta.esRojo(n) ? Tema.ROJO_RULETA : Tema.NEGRO_RULETA);
            g.setColor(c);
            g.fill(new Arc2D.Double(cx - rn, cy - rn, 2 * rn, 2 * rn, inicio, paso, Arc2D.PIE));
            g.setColor(new Color(0xC9A43A));
            g.setStroke(new BasicStroke(1f));
            g.draw(new Arc2D.Double(cx - rn, cy - rn, 2 * rn, 2 * rn, inicio, paso, Arc2D.PIE));
            double medio = Math.toRadians(-(inicio + paso / 2));
            g.setTransform(original);
            g.translate(cx + Math.cos(medio) * rn * 0.86, cy + Math.sin(medio) * rn * 0.86);
            g.rotate(medio + Math.PI / 2);
            g.setColor(Color.WHITE);
            g.setFont(Tema.negrita((float) Math.max(10, r * 0.075)));
            String texto = String.valueOf(n);
            g.drawString(texto, -g.getFontMetrics().stringWidth(texto) / 2f, g.getFontMetrics().getAscent() / 2f - 1);
            g.setTransform(original);
        }
        double ri = rn * 0.62;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) ri,
                new float[]{0f, 1f}, new Color[]{new Color(0x8A5A22), new Color(0x3B220C)}));
        g.fill(new Ellipse2D.Double(cx - ri, cy - ri, 2 * ri, 2 * ri));
        g.setColor(Tema.ORO);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(cx - ri, cy - ri, 2 * ri, 2 * ri));
        g.fill(new Ellipse2D.Double(cx - 10, cy - 10, 20, 20));

        // Marcador arriba y bola
        int[] xs = {(int) cx - 12, (int) cx + 12, (int) cx};
        int[] ys = {(int) (cy - r) - 4, (int) (cy - r) - 4, (int) (cy - rn) + 10};
        g.setColor(Tema.ORO_CLARO);
        g.fillPolygon(xs, ys, 3);
        if (ultimo != null) {
            double by = cy - rn * 0.72;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - 9, by - 9, 18, 18));
            g.setColor(Tema.CREMA);
            g.setFont(Tema.titulo((float) (r * 0.26)));
            String texto = String.valueOf(ultimo);
            g.drawString(texto, (float) (cx - g.getFontMetrics().stringWidth(texto) / 2.0), (float) (cy + r * 0.09));
        }
        g.dispose();
    }
}
