package broadway.vista;

import broadway.modelo.Formato;
import broadway.modelo.Jugador;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Sala principal: una tarjeta por juego, la caja y las estadísticas del jugador. */
public class PanelLobby extends PanelFieltro {

    private final VentanaPrincipal ventana;
    private final JLabel estadisticas = Tema.etiqueta(" ", Tema.texto(14), Tema.CREMA);

    public PanelLobby(VentanaPrincipal ventana) {
        super(new BorderLayout());
        this.ventana = ventana;
        setBorder(Tema.margen(18, 28, 16, 28));

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.add(Tema.etiqueta("Sala de juegos", Tema.titulo(30), Tema.ORO_CLARO));
        arriba.add(Tema.etiqueta("Escoge una mesa. Todas las fichas que pierdas te devuelven cashback según tu nivel.",
                Tema.texto(14), Tema.CREMA));
        arriba.setBorder(Tema.margen(0, 4, 14, 0));
        add(arriba, BorderLayout.NORTH);

        JPanel grilla = new JPanel(new GridLayout(2, 3, 18, 18));
        grilla.setOpaque(false);
        // Cada pantalla de juego sabe dibujar su ícono: el lobby no necesita saber qué juego es cada uno.
        for (PanelJuego panel : ventana.getPanelesJuego().values()) {
            grilla.add(new Tarjeta(panel.getJuego().getNombre(), panel.getJuego().getDescripcion(),
                    panel::dibujarIcono, () -> ventana.irAJuego(panel)));
        }
        grilla.add(new Tarjeta("Caja", "Compra y vende fichas, y reclama tu cashback.",
                PanelLobby::iconoCaja, () -> ventana.mostrar(VentanaPrincipal.CAJA)));
        add(grilla, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 40)), Tema.margen(12, 4, 0, 0)));
        abajo.add(estadisticas, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        BotonCasino salir = new BotonCasino("Guardar y salir", BotonCasino.Estilo.PELIGRO);
        salir.addActionListener(e -> ventana.cerrar());
        botones.add(salir);
        abajo.add(botones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
    }

    public void actualizar() {
        Jugador j = ventana.getCasino().getJugador();
        if (j == null) {
            return;
        }
        estadisticas.setText("<html>Total apostado <b>" + Formato.pesos(j.getTotalApostado()) + "</b> &nbsp;·&nbsp; Ganado <b>"
                + Formato.pesos(j.getTotalGanado()) + "</b> &nbsp;·&nbsp; Perdido <b>" + Formato.pesos(j.getTotalPerdido())
                + "</b> &nbsp;·&nbsp; Cashback recibido <b>" + Formato.pesos(j.getCashbackTotalRecibido()) + "</b></html>");
    }

    /** Algo que sabe dibujarse dentro de un cuadrado de lado t. */
    interface Icono {
        void dibujar(Graphics2D g, float x, float y, float t);
    }

    private static void iconoCaja(Graphics2D g, float x, float y, float t) {
        long[] valores = {100_000, 25_000, 10_000, 5_000};
        for (int pila = 0; pila < 3; pila++) {
            for (int i = 0; i < 4 - pila; i++) {
                float cx = x + t * (0.22f + pila * 0.28f);
                float cy = y + t * (0.80f - i * 0.12f);
                Dibujo.ficha(g, valores[(pila * 3 + i) % 4], cx, cy, t * 0.15f, false);
            }
        }
    }

    /** Tarjeta de un juego en el lobby. */
    private static class Tarjeta extends JComponent {
        private final String titulo;
        private final String descripcion;
        private final Icono icono;
        private boolean encima;

        Tarjeta(String titulo, String descripcion, Icono icono, Runnable alAbrir) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.icono = icono;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Entrar a " + titulo);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alAbrir.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 200);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth() - 4;
            int h = getHeight() - 4;
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(4, 6, w, h, 26, 26);
            g.setPaint(new GradientPaint(0, 0, encima ? new Color(0x1E5442) : new Color(0x133A2E), 0, h, new Color(0x0A2219)));
            g.fillRoundRect(0, 0, w, h, 26, 26);
            g.setColor(encima ? Tema.ORO : Tema.ORO_OSCURO);
            g.setStroke(new BasicStroke(encima ? 2.5f : 1.5f));
            g.drawRoundRect(0, 0, w, h, 26, 26);

            float t = Math.min(h * 0.52f, w * 0.38f);
            Graphics2D gi = (Graphics2D) g.create();
            icono.dibujar(gi, w / 2f - t / 2f, h * 0.08f, t);
            gi.dispose();

            g.setColor(Tema.ORO_CLARO);
            g.setFont(Tema.titulo(Math.max(18, Math.min(26, h * 0.12f))));
            Dibujo.centrar(g, titulo, w / 2f, h * 0.71f);
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.texto(Math.max(11, Math.min(14, h * 0.065f))));
            dibujarParrafo(g, descripcion, w / 2f, h * 0.83f, w - 30);
            g.dispose();
        }

        private static void dibujarParrafo(Graphics2D g, String texto, float cx, float y, int ancho) {
            String[] palabras = texto.split(" ");
            StringBuilder linea = new StringBuilder();
            float yy = y;
            int alto = g.getFontMetrics().getHeight();
            for (String p : palabras) {
                String prueba = linea.length() == 0 ? p : linea + " " + p;
                if (g.getFontMetrics().stringWidth(prueba) > ancho && linea.length() > 0) {
                    Dibujo.centrar(g, linea.toString(), cx, yy);
                    yy += alto;
                    linea = new StringBuilder(p);
                } else {
                    linea = new StringBuilder(prueba);
                }
            }
            Dibujo.centrar(g, linea.toString(), cx, yy);
        }
    }
}
