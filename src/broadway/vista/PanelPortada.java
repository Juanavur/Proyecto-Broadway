package broadway.vista;


import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Pantalla de portada: muestra el logo del Casino Broadway y espera a que
 * se presione ESPACIO (o se haga clic) para pasar al inicio de sesión.
 */
public class PanelPortada extends JComponent {

    private static final String RUTA_IMAGEN = "/broadway/recursos/logo.png";

    private final BufferedImage portada = cargarImagen();
    private final Timer animacion;
    private final Runnable alEmpezar;
    private float fase;
    private boolean iniciado;

    public PanelPortada(Runnable alEmpezar) {
        this.alEmpezar = alEmpezar;
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "empezar");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "empezar");
        getActionMap().put("empezar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                empezar();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                empezar();
            }
        });

        animacion = new Timer(40, e -> {
            fase += 0.08f;
            repaint();
        });
        animacion.start();
    }

    private void empezar() {
        if (iniciado || !isShowing()) {
            return;
        }
        iniciado = true;
        animacion.stop();
        alEmpezar.run();
    }

    private static BufferedImage cargarImagen() {
        try (InputStream in = PanelPortada.class.getResourceAsStream(RUTA_IMAGEN)) {
            return in == null ? null : ImageIO.read(in);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int w = getWidth(), h = getHeight();

        // Fondo de paño verde
        g.setPaint(new GradientPaint(0, 0, new Color(0x0F5A3A), 0, h, new Color(0x05241A)));
        g.fillRect(0, 0, w, h);

        // Luz suave detrás del logo
        g.setPaint(new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(w / 2f, h * 0.36f),
                Math.max(w, h) * 0.55f, new float[]{0f, 1f},
                new Color[]{new Color(0x1E8A5A), new Color(0, 0, 0, 0)}));
        g.fillRect(0, 0, w, h);

        // Logo centrado
        int ih;
        int logoY;
        if (portada != null) {
            int iw = (int) Math.min(w * 0.72, 980);
            ih = portada.getHeight() * iw / portada.getWidth();
            logoY = (int) (h * 0.33) - ih / 2;
            g.drawImage(portada, (w - iw) / 2, logoY, iw, ih, null);
        } else {
            ih = 120;
            logoY = (int) (h * 0.33) - ih / 2;
            g.setColor(Tema.ORO);
            g.setFont(Tema.titulo(72f));
            centrar(g, "CASINO BROADWAY", w, logoY + 90);
        }

        // Subtítulo y fichas decorativas
        int base = logoY + ih + 46;
        g.setColor(Tema.ORO_CLARO);
        g.setFont(Tema.negrita(20f));
        centrar(g, "SISTEMA DE GESTIÓN DEL CASINO", w, base);
        g.setColor(new Color(0xD8E8DF));
        g.setFont(Tema.texto(16f));
        centrar(g, "Clientes · Empleados · Caja · Mesas de Ruleta y Blackjack · Máquinas tragamonedas", w, base + 28);
        Color[] colores = {new Color(0xC0272D), new Color(0x1F4FB8), new Color(0x1B1B1B), new Color(0x13834F), new Color(0x7B2FA8)};
        int r = 22;
        int fx = w / 2 - (colores.length * (2 * r + 18) - 18) / 2;
        for (int k = 0; k < colores.length; k++) {
            int cx = fx + k * (2 * r + 18) + r;
            int cy = base + 76 + (int) (4 * Math.sin(fase + k));
            g.setColor(colores[k]);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{7f, 7f}, 0f));
            g.drawOval(cx - r + 4, cy - r + 4, 2 * r - 8, 2 * r - 8);
            g.setStroke(new BasicStroke(2f));
            g.setColor(Tema.ORO);
            g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        // Aviso "Presiona ESPACIO" con brillo pulsante
        float brillo = 0.55f + 0.45f * (float) Math.abs(Math.sin(fase));
        int caja = Math.max(380, Math.min(620, w / 2));
        int alto = 74;
        int x = (w - caja) / 2;
        int y = Math.min(h - alto - 30, base + 130);
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, caja, alto, alto, alto);
        g.setStroke(new BasicStroke(3f));
        g.setColor(Tema.ORO);
        g.drawRoundRect(x, y, caja, alto, alto, alto);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brillo));
        g.setColor(Tema.ORO_CLARO);
        g.setFont(Tema.negrita(28f));
        centrar(g, "PRESIONA ESPACIO PARA EMPEZAR", w, y + 38);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(0xE8E0C8));
        g.setFont(Tema.texto(14f));
        centrar(g, "o haz clic en cualquier parte", w, y + 60);
        g.dispose();
    }

    private static void centrar(Graphics2D g, String texto, int ancho, int lineaBase) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (ancho - fm.stringWidth(texto)) / 2, lineaBase);
    }
}
