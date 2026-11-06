package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;

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
 * Pantalla de portada: muestra la imagen del Casino Broadway y espera a que
 * el jugador presione ESPACIO (o haga clic) para pasar a la entrada del casino.
 */
public class PanelPortada extends JComponent {

    private static final String RUTA_IMAGEN = "/broadway/recursos/portada.png";

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
        Sonidos.reproducir(Efecto.FICHAS);
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

        // Imagen de portada ajustada a la ventana sin deformarse
        int ih = h, iw = w;
        if (portada != null) {
            double escala = Math.min((double) w / portada.getWidth(), (double) h / portada.getHeight());
            iw = (int) (portada.getWidth() * escala);
            ih = (int) (portada.getHeight() * escala);
            g.drawImage(portada, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        } else {
            g.setColor(Tema.ORO);
            g.setFont(Tema.titulo(72f));
            centrar(g, "CASINO BROADWAY", w, h / 2);
        }

        // Aviso "Presiona ESPACIO" con brillo pulsante
        float brillo = 0.55f + 0.45f * (float) Math.abs(Math.sin(fase));
        int caja = Math.max(380, Math.min(620, w / 2));
        int alto = 74;
        int x = (w - caja) / 2;
        int y = (h + ih) / 2 - alto - Math.max(24, ih / 14);
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
