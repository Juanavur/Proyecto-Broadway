package broadway.vista;

import broadway.cartas.Carta;
import broadway.juegos.tragamonedas.Simbolo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/** Métodos para dibujar cartas, fichas y símbolos con Java2D (sin imágenes externas). */
public final class Dibujo {

    private Dibujo() {
    }

    // ================================================================ cartas

    /** Dibuja una carta boca arriba, o el reverso si {@code carta} es null. */
    public static void carta(Graphics2D g, Carta carta, int x, int y, int w, int h) {
        int arco = Math.max(6, w / 7);
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x + 3, y + 4, w, h, arco, arco);
        if (carta == null) {
            g.setPaint(new GradientPaint(x, y, new Color(0x8E2433), x + w, y + h, new Color(0x4F0E19)));
            g.fillRoundRect(x, y, w, h, arco, arco);
            g.setColor(Tema.CREMA);
            g.setStroke(new BasicStroke(Math.max(1.5f, w / 40f)));
            g.drawRoundRect(x + w / 10, y + w / 10, w - w / 5, h - w / 5, arco / 2, arco / 2);
            g.setColor(new Color(0xD4AF37));
            g.setFont(Tema.titulo(w * 0.42f));
            centrar(g, "B", x + w / 2, y + h / 2);
            g.setStroke(new BasicStroke(1f));
            return;
        }
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, arco, arco);
        g.setColor(new Color(0xBBBBBB));
        g.drawRoundRect(x, y, w, h, arco, arco);
        Color color = carta.getPalo().esRojo() ? new Color(0xC81E2B) : new Color(0x141414);
        g.setColor(color);
        String valor = carta.getValor().getSimbolo();
        String palo = String.valueOf(carta.getPalo().getSimbolo());
        Font fv = Tema.negrita(w * 0.30f);
        Font fp = new Font("SansSerif", Font.PLAIN, Math.round(w * 0.26f));
        g.setFont(fv);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(valor, x + w * 0.10f, y + fm.getAscent() * 0.95f);
        g.setFont(fp);
        g.drawString(palo, x + w * 0.11f, y + fm.getAscent() * 0.95f + g.getFontMetrics().getAscent() * 0.95f);
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.round(w * 0.55f)));
        centrar(g, palo, x + w / 2, y + h * 0.58f);
    }

    // ================================================================ fichas

    public static Color colorFicha(long valor) {
        if (valor >= 500_000) return new Color(0x6A2C91);
        if (valor >= 100_000) return new Color(0x1C1C1C);
        if (valor >= 25_000) return new Color(0x1D8A4E);
        if (valor >= 10_000) return new Color(0x1F5FB8);
        if (valor >= 5_000) return new Color(0xC0272D);
        return new Color(0xE9E4D4);
    }

    public static String textoFicha(long valor) {
        if (valor >= 1_000_000 && valor % 1_000_000 == 0) return (valor / 1_000_000) + "M";
        if (valor >= 1_000) return (valor / 1_000) + "K";
        return String.valueOf(valor);
    }

    /** Ficha de casino vista desde arriba, centrada en (cx, cy). */
    public static void ficha(Graphics2D g, long valor, float cx, float cy, float radio, boolean resaltada) {
        Color base = colorFicha(valor);
        boolean clara = base.getRed() > 200;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Float(cx - radio + 1.5f, cy - radio + 2.5f, radio * 2, radio * 2));
        g.setColor(base);
        g.fill(new Ellipse2D.Float(cx - radio, cy - radio, radio * 2, radio * 2));
        // franjas del borde
        g.setColor(clara ? new Color(0x2F5FB8) : Color.WHITE);
        g.setStroke(new BasicStroke(radio * 0.22f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        for (int i = 0; i < 6; i++) {
            g.draw(new java.awt.geom.Arc2D.Float(cx - radio * 0.88f, cy - radio * 0.88f, radio * 1.76f, radio * 1.76f,
                    i * 60 + 10, 22, java.awt.geom.Arc2D.OPEN));
        }
        g.setStroke(new BasicStroke(Math.max(1f, radio * 0.07f)));
        g.setColor(clara ? new Color(0x9A9070) : new Color(255, 255, 255, 150));
        g.draw(new Ellipse2D.Float(cx - radio * 0.62f, cy - radio * 0.62f, radio * 1.24f, radio * 1.24f));
        g.setColor(resaltada ? Tema.ORO : new Color(0, 0, 0, 120));
        g.setStroke(new BasicStroke(resaltada ? Math.max(2f, radio * 0.14f) : 1f));
        g.draw(new Ellipse2D.Float(cx - radio, cy - radio, radio * 2, radio * 2));
        g.setStroke(new BasicStroke(1f));
        g.setColor(clara ? new Color(0x222222) : Color.WHITE);
        g.setFont(Tema.negrita(radio * 0.62f));
        centrar(g, textoFicha(valor), cx, cy);
    }

    // ================================================================= texto

    public static void centrar(Graphics2D g, String texto, float cx, float cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2f, cy - fm.getHeight() / 2f + fm.getAscent());
    }

    // ======================================================= tragamonedas

    /** Dibuja un símbolo de la tragamonedas dentro de un cuadrado de lado {@code t}. */
    public static void simbolo(Graphics2D g, Simbolo s, float x, float y, float t) {
        float cx = x + t / 2;
        float cy = y + t / 2;
        switch (s) {
            case CEREZA: {
                g.setColor(new Color(0x2E7D32));
                g.setStroke(new BasicStroke(t * 0.05f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                GeneralPath tallo = new GeneralPath();
                tallo.moveTo(cx - t * 0.18, cy + t * 0.08);
                tallo.quadTo(cx - t * 0.05, cy - t * 0.25, cx + t * 0.12, cy - t * 0.33);
                tallo.moveTo(cx + t * 0.17, cy + t * 0.12);
                tallo.quadTo(cx + t * 0.12, cy - t * 0.15, cx + t * 0.12, cy - t * 0.33);
                g.draw(tallo);
                g.fill(new Ellipse2D.Float(cx + t * 0.06f, cy - t * 0.40f, t * 0.22f, t * 0.10f));
                bola(g, cx - t * 0.18f, cy + t * 0.17f, t * 0.17f, new Color(0xD81B2A));
                bola(g, cx + t * 0.17f, cy + t * 0.21f, t * 0.17f, new Color(0xB71422));
                break;
            }
            case LIMON: {
                GeneralPath p = new GeneralPath();
                p.moveTo(cx - t * 0.40, cy);
                p.curveTo(cx - t * 0.30, cy - t * 0.32, cx + t * 0.30, cy - t * 0.32, cx + t * 0.40, cy);
                p.curveTo(cx + t * 0.30, cy + t * 0.32, cx - t * 0.30, cy + t * 0.32, cx - t * 0.40, cy);
                g.setPaint(new GradientPaint(cx, cy - t * 0.3f, new Color(0xFFF176), cx, cy + t * 0.3f, new Color(0xF9A825)));
                g.fill(p);
                g.setColor(new Color(0xC28E00));
                g.setStroke(new BasicStroke(t * 0.02f));
                g.draw(p);
                break;
            }
            case NARANJA: {
                bola(g, cx, cy + t * 0.04f, t * 0.33f, new Color(0xFB8C00));
                g.setColor(new Color(0x2E7D32));
                g.fill(new Ellipse2D.Float(cx - t * 0.02f, cy - t * 0.36f, t * 0.2f, t * 0.1f));
                break;
            }
            case CIRUELA: {
                g.setPaint(new GradientPaint(cx - t * 0.2f, cy - t * 0.2f, new Color(0xB27BD6), cx + t * 0.2f, cy + t * 0.3f, new Color(0x4A148C)));
                g.fill(new Ellipse2D.Float(cx - t * 0.30f, cy - t * 0.33f, t * 0.60f, t * 0.70f));
                g.setColor(new Color(255, 255, 255, 110));
                g.fill(new Ellipse2D.Float(cx - t * 0.18f, cy - t * 0.22f, t * 0.12f, t * 0.2f));
                break;
            }
            case CAMPANA: {
                GeneralPath p = new GeneralPath();
                p.moveTo(cx - t * 0.36, cy + t * 0.22);
                p.curveTo(cx - t * 0.28, cy + t * 0.10, cx - t * 0.30, cy - t * 0.34, cx, cy - t * 0.34);
                p.curveTo(cx + t * 0.30, cy - t * 0.34, cx + t * 0.28, cy + t * 0.10, cx + t * 0.36, cy + t * 0.22);
                p.closePath();
                g.setPaint(new GradientPaint(cx - t * 0.3f, cy, new Color(0xFFE082), cx + t * 0.3f, cy, new Color(0xC79100)));
                g.fill(p);
                g.setColor(new Color(0x8D6E00));
                g.setStroke(new BasicStroke(t * 0.025f));
                g.draw(p);
                g.fill(new Ellipse2D.Float(cx - t * 0.08f, cy + t * 0.20f, t * 0.16f, t * 0.12f));
                g.fill(new Ellipse2D.Float(cx - t * 0.05f, cy - t * 0.42f, t * 0.10f, t * 0.10f));
                break;
            }
            case BAR: {
                RoundRectangle2D r = new RoundRectangle2D.Float(x + t * 0.10f, cy - t * 0.17f, t * 0.80f, t * 0.34f, t * 0.1f, t * 0.1f);
                g.setColor(new Color(0x111111));
                g.fill(r);
                g.setColor(Tema.ORO);
                g.setStroke(new BasicStroke(t * 0.03f));
                g.draw(r);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, Math.round(t * 0.26f)));
                centrar(g, "BAR", cx, cy + 1);
                break;
            }
            case DIAMANTE: {
                Polygon p = new Polygon();
                p.addPoint(Math.round(cx - t * 0.36f), Math.round(cy - t * 0.10f));
                p.addPoint(Math.round(cx - t * 0.20f), Math.round(cy - t * 0.30f));
                p.addPoint(Math.round(cx + t * 0.20f), Math.round(cy - t * 0.30f));
                p.addPoint(Math.round(cx + t * 0.36f), Math.round(cy - t * 0.10f));
                p.addPoint(Math.round(cx), Math.round(cy + t * 0.36f));
                g.setPaint(new GradientPaint(cx, cy - t * 0.3f, new Color(0xE0F7FF), cx, cy + t * 0.36f, new Color(0x2AA7D8)));
                g.fill(p);
                g.setColor(new Color(0x16708F));
                g.setStroke(new BasicStroke(t * 0.02f));
                g.draw(p);
                g.drawLine(Math.round(cx - t * 0.36f), Math.round(cy - t * 0.10f), Math.round(cx + t * 0.36f), Math.round(cy - t * 0.10f));
                g.drawLine(Math.round(cx - t * 0.12f), Math.round(cy - t * 0.10f), Math.round(cx), Math.round(cy + t * 0.36f));
                g.drawLine(Math.round(cx + t * 0.12f), Math.round(cy - t * 0.10f), Math.round(cx), Math.round(cy + t * 0.36f));
                break;
            }
            case SIETE: {
                Font f = new Font("Serif", Font.BOLD, Math.round(t * 0.78f));
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics();
                float tx = cx - fm.stringWidth("7") / 2f;
                float ty = cy - fm.getHeight() / 2f + fm.getAscent();
                java.awt.Shape forma = f.createGlyphVector(g.getFontRenderContext(), "7").getOutline(tx, ty);
                g.setColor(Tema.ORO);
                g.setStroke(new BasicStroke(t * 0.07f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(forma);
                g.setPaint(new GradientPaint(cx, cy - t * 0.3f, new Color(0xFF5252), cx, cy + t * 0.3f, new Color(0xA00010)));
                g.fill(forma);
                break;
            }
            default:
                break;
        }
        g.setStroke(new BasicStroke(1f));
    }

    private static void bola(Graphics2D g, float cx, float cy, float r, Color color) {
        g.setPaint(new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(cx - r * 0.35f, cy - r * 0.35f), r * 1.3f,
                new float[]{0f, 1f}, new Color[]{color.brighter().brighter(), color.darker()}));
        g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
    }
}
