package broadway.vista;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Dibujo de la mesa de blackjack: cartas del crupier arriba y del cliente abajo. */
public class MesaBlackjack extends JComponent {

    private static final String[] PALOS = {"♠", "♥", "♦", "♣"};
    private static final String[] NOMBRES = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    private final Random azar = new Random();
    private final List<int[]> casa = new ArrayList<>();
    private final List<int[]> jugador = new ArrayList<>();
    private boolean ocultarSegunda;
    private String textoCasa = "";
    private String textoJugador = "";
    private String mensaje = "Escoja un cliente y la apuesta, y reparta.";

    public MesaBlackjack() {
        setPreferredSize(new Dimension(560, 420));
        setOpaque(false);
    }

    public void limpiar() {
        casa.clear();
        jugador.clear();
        textoCasa = "";
        textoJugador = "";
    }

    /** Agrega una carta (1 a 13); el palo es solo visual. */
    public void cartaCasa(int valor) {
        casa.add(new int[]{valor, azar.nextInt(4)});
        repaint();
    }

    public void cartaJugador(int valor) {
        jugador.add(new int[]{valor, azar.nextInt(4)});
        repaint();
    }

    public void setOcultarSegunda(boolean ocultar) {
        ocultarSegunda = ocultar;
        repaint();
    }

    public void setTextos(String casa, String jugador, String mensaje) {
        textoCasa = casa;
        textoJugador = jugador;
        this.mensaje = mensaje;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect(10, 10, w - 20, h - 20, 200, 200);
        g.setColor(Tema.ORO_OSCURO);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(10, 10, w - 20, h - 20, 200, 200);
        g.setFont(Tema.titulo(18));
        g.setColor(new Color(255, 255, 255, 70));
        String lema = "BLACKJACK PAGA 3 A 2";
        g.drawString(lema, (w - g.getFontMetrics().stringWidth(lema)) / 2, h / 2 + 6);

        dibujarFila(g, casa, w, 46, ocultarSegunda);
        dibujarFila(g, jugador, w, h - 46 - 120, false);
        g.setFont(Tema.negrita(15));
        g.setColor(Tema.ORO_CLARO);
        g.drawString("Crupier " + textoCasa, 34, 36);
        g.drawString("Cliente " + textoJugador, 34, h - 22);
        g.setFont(Tema.negrita(17));
        g.setColor(Tema.CREMA);
        int mw = g.getFontMetrics().stringWidth(mensaje);
        g.drawString(mensaje, (w - mw) / 2, h / 2 + 34);
        g.dispose();
    }

    private void dibujarFila(Graphics2D g, List<int[]> cartas, int w, int y, boolean ocultar) {
        int cw = 84;
        int ch = 120;
        int sep = 18;
        int total = cartas.size() * cw + Math.max(0, cartas.size() - 1) * sep;
        int x = (w - total) / 2;
        for (int i = 0; i < cartas.size(); i++) {
            int cx = x + i * (cw + sep);
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRoundRect(cx + 4, y + 5, cw, ch, 12, 12);
            if (ocultar && i == 1) {
                g.setPaint(new GradientPaint(cx, y, Tema.VINO_CLARO, cx + cw, y + ch, Tema.VINO));
                g.fillRoundRect(cx, y, cw, ch, 12, 12);
                g.setColor(Tema.ORO);
                g.drawRoundRect(cx + 6, y + 6, cw - 12, ch - 12, 8, 8);
                continue;
            }
            int[] c = cartas.get(i);
            g.setColor(Color.WHITE);
            g.fillRoundRect(cx, y, cw, ch, 12, 12);
            g.setColor(new Color(0xBBBBBB));
            g.drawRoundRect(cx, y, cw, ch, 12, 12);
            boolean rojo = c[1] == 1 || c[1] == 2;
            g.setColor(rojo ? Tema.ROJO_RULETA : Color.BLACK);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.drawString(NOMBRES[c[0]], cx + 9, y + 26);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 44));
            String palo = PALOS[c[1]];
            g.drawString(palo, cx + (cw - g.getFontMetrics().stringWidth(palo)) / 2, y + ch / 2 + 16);
        }
    }
}
