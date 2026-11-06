package broadway.vista;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/** Panel con el fondo verde de una mesa de casino. */
public class PanelFieltro extends JPanel {

    public PanelFieltro(LayoutManager layout) {
        super(layout);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        int w = getWidth();
        int h = getHeight();
        float radio = Math.max(w, h) * 0.75f;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(w / 2f, h * 0.45f), radio,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(0x15814F), Tema.FIELTRO, Tema.FIELTRO_OSCURO}));
        g.fillRect(0, 0, w, h);
        g.dispose();
    }
}
