package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.juegos.ruleta.ApuestaRuleta;
import broadway.juegos.ruleta.ResultadoTirada;
import broadway.juegos.ruleta.Ruleta;
import broadway.juegos.ruleta.TipoApuestaRuleta;
import broadway.modelo.Formato;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

/** Ruleta europea: rueda animada y tapete con todas las apuestas. */
public class PanelRuleta extends PanelJuego {

    private static final long[] FICHAS = {1_000, 5_000, 10_000, 25_000, 100_000};
    private static final double PASO = 360.0 / 37;

    private final Ruleta ruleta;
    private final Rueda rueda = new Rueda();
    private final Tapete tapete = new Tapete();
    private final SelectorFichas selector = new SelectorFichas(FICHAS, 22);
    private final JLabel mensaje = Tema.etiqueta("Toca el tapete para apostar. Clic derecho quita una apuesta.", Tema.negrita(16), Tema.CREMA);
    private final JLabel total = Tema.etiqueta("", Tema.negrita(16), Tema.ORO_CLARO);
    private final BotonCasino girar = new BotonCasino("GIRAR", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino repetir = new BotonCasino("Repetir apuesta");
    private final BotonCasino retirar = new BotonCasino("Retirar fichas");

    // Animación
    private double anguloRueda;
    private double anguloBola = 200;
    private double radioBola = 0.86;
    private boolean bolaVisible;
    private Timer animacion;
    private ResultadoTirada pendiente;
    private Integer numeroGanador;

    public PanelRuleta(VentanaPrincipal ventana) {
        super(ventana, ventana.getCasino().getRuleta());
        this.ruleta = ventana.getCasino().getRuleta();

        PanelFieltro centro = new PanelFieltro(new BorderLayout(10, 0));
        centro.setBorder(Tema.margen(10, 14, 6, 14));
        rueda.setPreferredSize(new Dimension(380, 380));
        centro.add(rueda, BorderLayout.WEST);
        centro.add(tapete, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setBackground(Tema.PANEL);
        abajo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE), Tema.margen(8, 18, 10, 18)));
        mensaje.setHorizontalAlignment(JLabel.CENTER);
        mensaje.setBorder(Tema.margen(0, 0, 8, 0));
        abajo.add(mensaje, BorderLayout.NORTH);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(Tema.etiqueta("Ficha:", Tema.texto(14), Tema.TEXTO_SUAVE));
        izq.add(selector);
        abajo.add(izq, BorderLayout.WEST);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        der.setOpaque(false);
        der.add(total);
        retirar.addActionListener(e -> {
            Sonidos.reproducir(Efecto.FICHAS);
            ruleta.retirarApuestas(jugador());
            despuesDeApostar();
        });
        repetir.addActionListener(e -> {
            if (intentar(() -> ruleta.repetirApuestas(jugador()))) {
                Sonidos.reproducir(Efecto.FICHAS);
                despuesDeApostar();
            }
        });
        der.add(retirar);
        der.add(repetir);
        girar.setFont(Tema.negrita(20));
        girar.setPreferredSize(new Dimension(160, 48));
        girar.addActionListener(e -> girar());
        der.add(girar);
        abajo.add(der, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
        actualizarControles();
    }

    @Override
    protected String textoLimites() {
        return "Por casilla " + Formato.pesos(getJuego().getApuestaMinima()) + " a " + Formato.pesos(getJuego().getApuestaMaxima())
                + " · Mesa hasta " + Formato.pesos(Ruleta.MAXIMO_EN_MESA);
    }

    private boolean girando() {
        return animacion != null && animacion.isRunning();
    }

    private void despuesDeApostar() {
        ventana.actualizarSaldo();
        actualizarControles();
        tapete.repaint();
    }

    private void actualizarControles() {
        total.setText("En la mesa: " + Formato.pesos(ruleta.getTotalApostado()));
        boolean libre = !girando();
        girar.setEnabled(libre && ruleta.getTotalApostado() > 0);
        retirar.setEnabled(libre && ruleta.getTotalApostado() > 0);
        repetir.setEnabled(libre && ruleta.hayApuestaAnterior());
    }

    private void girar() {
        if (girando()) {
            return;
        }
        final ResultadoTirada[] r = new ResultadoTirada[1];
        long apostado = ruleta.getTotalApostado();
        if (!intentar(() -> r[0] = ruleta.girar(jugador()))) {
            return;
        }
        pendiente = r[0];
        numeroGanador = null;
        mensaje.setText("No va más. La bola está girando con " + Formato.pesos(apostado) + " en la mesa...");
        mensaje.setForeground(Tema.CREMA);
        int indice = 0;
        for (int i = 0; i < Ruleta.ORDEN_RUEDA.length; i++) {
            if (Ruleta.ORDEN_RUEDA[i] == pendiente.getNumero()) {
                indice = i;
            }
        }
        double ruedaInicio = anguloRueda % 360;
        double ruedaFin = ruedaInicio + 540 + Math.random() * 180;
        double bolaFin = ruedaFin + indice * PASO;
        double bolaInicio = bolaFin + 360 * 5 + Math.random() * 90;
        long t0 = System.currentTimeMillis();
        bolaVisible = true;
        Sonidos.reproducir(Efecto.RULETA);
        animacion = new Timer(15, e -> {
            double p = Math.min(1, (System.currentTimeMillis() - t0) / 5200.0);
            double suave = 1 - Math.pow(1 - p, 3);
            anguloRueda = ruedaInicio + (ruedaFin - ruedaInicio) * suave;
            double suaveBola = 1 - Math.pow(1 - p, 2.2);
            anguloBola = bolaInicio + (bolaFin - bolaInicio) * suaveBola;
            if (p < 0.65) {
                radioBola = 0.86;
            } else if (p < 0.9) {
                double q = (p - 0.65) / 0.25;
                radioBola = 0.86 - 0.17 * q + Math.sin(q * Math.PI * 3) * 0.03 * (1 - q);
            } else {
                radioBola = 0.69;
            }
            rueda.repaint();
            if (p >= 1) {
                ((Timer) e.getSource()).stop();
                terminarGiro();
            }
        });
        animacion.start();
        actualizarControles();
        tapete.repaint();
    }

    private void terminarGiro() {
        numeroGanador = pendiente.getNumero();
        int n = numeroGanador;
        String color = Ruleta.colorDe(n);
        StringBuilder sb = new StringBuilder("Salió el " + n + " " + color.toLowerCase());
        if (n != 0) {
            sb.append(n % 2 == 0 ? ", par" : ", impar").append(n <= 18 ? ", falta" : ", pasa");
        }
        sb.append(".   ").append(textoNeto(pendiente.getRonda()));
        if (!pendiente.getGanadoras().isEmpty()) {
            sb.append(" (");
            for (int i = 0; i < pendiente.getGanadoras().size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(pendiente.getGanadoras().get(i).getDescripcion());
            }
            sb.append(")");
        }
        mensaje.setText(sb.toString());
        long neto = pendiente.getRonda().getNeto();
        mensaje.setForeground(neto > 0 ? Tema.EXITO : neto == 0 ? Tema.ORO_CLARO : Tema.ERROR);
        if (!pendiente.getGanadoras().isEmpty()) {
            boolean pleno = pendiente.getGanadoras().stream().anyMatch(x -> x.getTipo() == TipoApuestaRuleta.PLENO);
            Sonidos.reproducir(pleno ? Efecto.GANAR_GRANDE : Efecto.GANAR, 200);
            Sonidos.reproducir(Efecto.FICHAS, pleno ? 1000 : 600);
        }
        rondaTerminada(pendiente.getRonda());
        actualizarControles();
        tapete.repaint();
        rueda.repaint();
    }

    @Override
    public boolean alSalir() {
        if (girando()) {
            ventana.aviso("Espera a que la bola se detenga.", Tema.ERROR);
            return false;
        }
        if (ruleta.getTotalApostado() > 0) {
            ruleta.retirarApuestas(jugador());
            ventana.actualizarSaldo();
            ventana.aviso("Se te devolvieron las fichas que tenías en el tapete.", Tema.ORO_CLARO);
        }
        return true;
    }

    @Override
    public void alEntrar() {
        actualizarControles();
    }

    @Override
    public void prepararCierre() {
        if (animacion != null) {
            animacion.stop();
        }
        if (jugador() != null) {
            ruleta.retirarApuestas(jugador());
        }
    }

    @Override
    public void dibujarIcono(Graphics2D g, float x, float y, float t) {
        dibujarRueda(g, x + t / 2, y + t / 2, t * 0.48f, 0, null);
    }

    // ================================================================= rueda

    private static Color colorNumero(int n) {
        if (n == 0) {
            return Tema.VERDE_CERO;
        }
        return Ruleta.esRojo(n) ? Tema.ROJO_RULETA : Tema.NEGRO_RULETA;
    }

    /** Dibuja la rueda europea. El ángulo se mide en grados, en sentido horario desde arriba. */
    static void dibujarRueda(Graphics2D g, float cx, float cy, float r, double rotacion, Integer resaltado) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r, new float[]{0.7f, 1f},
                new Color[]{new Color(0x5D3A1A), new Color(0x2E1A0B)}));
        g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        g.setColor(Tema.ORO_OSCURO);
        g.setStroke(new BasicStroke(Math.max(1.5f, r * 0.02f)));
        g.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        float rExt = r * 0.80f;
        float rInt = r * 0.58f;
        for (int i = 0; i < Ruleta.ORDEN_RUEDA.length; i++) {
            int n = Ruleta.ORDEN_RUEDA[i];
            double centro = rotacion + i * PASO;
            double inicio = 90 - centro - PASO / 2;
            g.setColor(colorNumero(n));
            g.fill(new Arc2D.Double(cx - rExt, cy - rExt, rExt * 2, rExt * 2, inicio, PASO, Arc2D.PIE));
            if (resaltado != null && resaltado == n) {
                g.setColor(new Color(255, 230, 140, 150));
                g.fill(new Arc2D.Double(cx - rExt, cy - rExt, rExt * 2, rExt * 2, inicio, PASO, Arc2D.PIE));
            }
            g.setColor(new Color(0xC9A94A));
            g.setStroke(new BasicStroke(Math.max(0.6f, r * 0.006f)));
            g.draw(new Arc2D.Double(cx - rExt, cy - rExt, rExt * 2, rExt * 2, inicio, PASO, Arc2D.PIE));
            Graphics2D gt = (Graphics2D) g.create();
            gt.rotate(Math.toRadians(centro), cx, cy);
            gt.setColor(Color.WHITE);
            gt.setFont(Tema.negrita(Math.max(6, r * 0.085f)));
            Dibujo.centrar(gt, String.valueOf(n), cx, cy - r * 0.715f);
            gt.dispose();
        }
        // centro de madera y torreta
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), rInt, new float[]{0f, 1f},
                new Color[]{new Color(0x8B5A2B), new Color(0x4A2C12)}));
        g.fill(new Ellipse2D.Float(cx - rInt, cy - rInt, rInt * 2, rInt * 2));
        g.setColor(Tema.ORO);
        g.setStroke(new BasicStroke(Math.max(1f, r * 0.02f)));
        g.draw(new Ellipse2D.Float(cx - rInt, cy - rInt, rInt * 2, rInt * 2));
        for (int k = 0; k < 4; k++) {
            Graphics2D gb = (Graphics2D) g.create();
            gb.rotate(Math.toRadians(rotacion + k * 90), cx, cy);
            gb.setPaint(new GradientPaint(cx, cy - rInt * 0.8f, Tema.ORO_CLARO, cx, cy, Tema.ORO_OSCURO));
            gb.fill(new Rectangle2D.Float(cx - r * 0.025f, cy - rInt * 0.8f, r * 0.05f, rInt * 0.8f));
            gb.dispose();
        }
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx - r * 0.03f, cy - r * 0.03f), r * 0.1f, new float[]{0f, 1f},
                new Color[]{Tema.ORO_CLARO, Tema.ORO_OSCURO}));
        g.fill(new Ellipse2D.Float(cx - r * 0.09f, cy - r * 0.09f, r * 0.18f, r * 0.18f));
        g.setStroke(new BasicStroke(1f));
    }

    private class Rueda extends JComponent {
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            float alturaHistorial = 64;
            float r = Math.min(w, h - alturaHistorial) / 2f - 10;
            float cx = w / 2f;
            float cy = 10 + r;
            dibujarRueda(g, cx, cy, r, anguloRueda, numeroGanador);
            if (bolaVisible) {
                double a = Math.toRadians(anguloBola);
                float rb = (float) (r * radioBola);
                float bx = (float) (cx + rb * Math.sin(a));
                float by = (float) (cy - rb * Math.cos(a));
                float br = Math.max(4, r * 0.04f);
                g.setPaint(new RadialGradientPaint(new Point2D.Float(bx - br / 3, by - br / 3), br * 1.4f,
                        new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(0xA0A0A0)}));
                g.fill(new Ellipse2D.Float(bx - br, by - br, br * 2, br * 2));
            }
            // Historial
            List<Integer> ultimos = ruleta.getUltimosNumeros();
            float y = cy + r + 18;
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.negrita(11));
            g.drawString("ÚLTIMOS NÚMEROS", 12, y);
            float x = 12;
            for (int i = 0; i < ultimos.size() && x < w - 30; i++) {
                int n = ultimos.get(i);
                g.setColor(colorNumero(n));
                g.fillRoundRect((int) x, (int) y + 6, 28, 26, 8, 8);
                g.setColor(i == 0 ? Tema.ORO : new Color(255, 255, 255, 80));
                g.drawRoundRect((int) x, (int) y + 6, 28, 26, 8, 8);
                g.setColor(Color.WHITE);
                g.setFont(Tema.negrita(13));
                Dibujo.centrar(g, String.valueOf(n), x + 14, y + 19);
                x += 32;
            }
            g.dispose();
        }
    }

    // ================================================================ tapete

    private class Tapete extends JComponent {
        private ApuestaRuleta bajoCursor;
        // geometría calculada en cada pintado
        private float x0;
        private float izquierda;
        private float arriba;
        private float cw;
        private float ch;

        Tapete() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    bajoCursor = apuestaEn(e.getX(), e.getY(), 1);
                    if (!girando()) {
                        if (bajoCursor != null) {
                            mensaje.setText(bajoCursor.getDescripcion() + " · paga " + bajoCursor.getTipo().getPago() + " a 1"
                                    + " · ficha de " + Formato.pesos(selector.getValor()));
                            mensaje.setForeground(Tema.ORO_CLARO);
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    bajoCursor = null;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (girando()) {
                        return;
                    }
                    ApuestaRuleta a = apuestaEn(e.getX(), e.getY(), selector.getValor());
                    if (a == null) {
                        return;
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        for (ApuestaRuleta existente : ruleta.getApuestas()) {
                            if (existente.mismaPosicion(a)) {
                                Sonidos.reproducir(Efecto.FICHA);
                                ruleta.quitarApuesta(jugador(), existente);
                                break;
                            }
                        }
                        despuesDeApostar();
                        return;
                    }
                    if (intentar(() -> ruleta.apostar(jugador(), a))) {
                        Sonidos.reproducir(Efecto.FICHA);
                        numeroGanador = null;
                        despuesDeApostar();
                    }
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
        }

        private void calcular() {
            int w = getWidth();
            int h = getHeight();
            cw = Math.min((w - 24) / 14.2f, 78);
            ch = Math.min((h - 24) / 4.75f, cw * 1.5f);
            float anchoTotal = cw * 14;
            x0 = (w - anchoTotal) / 2f;
            izquierda = x0 + cw;
            arriba = Math.max(10, (h - ch * 4.7f) / 2f);
        }

        private Rectangle2D celda(int n) {
            int c = (n - 1) / 3;
            int r = 2 - (n - 1) % 3;
            return new Rectangle2D.Float(izquierda + c * cw, arriba + r * ch, cw, ch);
        }

        private Rectangle2D celdaCero() {
            return new Rectangle2D.Float(x0, arriba, cw, ch * 3);
        }

        private Rectangle2D celdaColumna(int columna) {
            return new Rectangle2D.Float(izquierda + 12 * cw, arriba + (3 - columna) * ch, cw, ch);
        }

        private Rectangle2D celdaDocena(int d) {
            return new Rectangle2D.Float(izquierda + (d - 1) * 4 * cw, arriba + 3 * ch, 4 * cw, ch * 0.8f);
        }

        private static final int SENCILLAS = 6;

        private TipoApuestaRuleta sencilla(int i) {
            return new TipoApuestaRuleta[]{TipoApuestaRuleta.FALTA, TipoApuestaRuleta.PAR, TipoApuestaRuleta.ROJO,
                    TipoApuestaRuleta.NEGRO, TipoApuestaRuleta.IMPAR, TipoApuestaRuleta.PASA}[i];
        }

        private Rectangle2D celdaSencilla(int i) {
            return new Rectangle2D.Float(izquierda + i * 2 * cw, arriba + 3.8f * ch, 2 * cw, ch * 0.8f);
        }

        /** Qué apuesta corresponde a un punto del tapete (null si no hay ninguna). */
        private ApuestaRuleta apuestaEn(int px, int py, long monto) {
            calcular();
            try {
                if (celdaCero().contains(px, py)) {
                    return ApuestaRuleta.pleno(0, monto);
                }
                for (int col = 1; col <= 3; col++) {
                    if (celdaColumna(col).contains(px, py)) {
                        return ApuestaRuleta.columna(col, monto);
                    }
                }
                for (int d = 1; d <= 3; d++) {
                    if (celdaDocena(d).contains(px, py)) {
                        return ApuestaRuleta.docena(d, monto);
                    }
                }
                for (int i = 0; i < SENCILLAS; i++) {
                    if (celdaSencilla(i).contains(px, py)) {
                        return ApuestaRuleta.sencilla(sencilla(i), monto);
                    }
                }
                float lx = px - izquierda;
                float ly = py - arriba;
                if (lx < 0 || ly < 0 || lx >= 12 * cw || ly >= 3 * ch) {
                    return null;
                }
                int c = (int) (lx / cw);
                int r = (int) (ly / ch);
                float fx = lx - c * cw;
                float fy = ly - r * ch;
                float borde = Math.min(cw, ch) * 0.2f;
                boolean izq = fx < borde;
                boolean der = fx > cw - borde;
                boolean sup = fy < borde;
                boolean inf = fy > ch - borde;
                int numero = 3 * (c + 1) - r;
                // Borde inferior del tapete: calle o seisena.
                if (r == 2 && inf) {
                    if (izq && c > 0) {
                        return ApuestaRuleta.seisena(c, monto);
                    }
                    if (der && c < 11) {
                        return ApuestaRuleta.seisena(c + 1, monto);
                    }
                    return ApuestaRuleta.calle(c + 1, monto);
                }
                if ((izq || der) && (sup || inf)) {
                    int c2 = izq ? c - 1 : c + 1;
                    int r2 = sup ? r - 1 : r + 1;
                    if (c2 >= 0 && c2 <= 11 && r2 >= 0 && r2 <= 2) {
                        int menor = Math.min(3 * (Math.min(c, c2) + 1) - Math.max(r, r2), 99);
                        return ApuestaRuleta.cuadro(menor, monto);
                    }
                }
                if (izq && c == 0) {
                    return ApuestaRuleta.semipleno(0, numero, monto);
                }
                if (izq) {
                    return ApuestaRuleta.semipleno(numero, numero - 3, monto);
                }
                if (der && c < 11) {
                    return ApuestaRuleta.semipleno(numero, numero + 3, monto);
                }
                if (sup && r > 0) {
                    return ApuestaRuleta.semipleno(numero, numero + 1, monto);
                }
                if (inf && r < 2) {
                    return ApuestaRuleta.semipleno(numero, numero - 1, monto);
                }
                return ApuestaRuleta.pleno(numero, monto);
            } catch (RuntimeException ex) {
                return null;
            }
        }

        /** Punto del tapete donde se dibujan las fichas de una apuesta. */
        private Point2D.Float ancla(ApuestaRuleta a) {
            Set<Integer> nums = a.getNumeros();
            switch (a.getTipo()) {
                case PLENO: {
                    int n = nums.iterator().next();
                    Rectangle2D r = n == 0 ? celdaCero() : celda(n);
                    return new Point2D.Float((float) r.getCenterX(), (float) r.getCenterY());
                }
                case SEMIPLENO:
                case CUADRO: {
                    if (nums.contains(0)) {
                        int otro = 0;
                        for (int n : nums) {
                            otro = Math.max(otro, n);
                        }
                        return new Point2D.Float(izquierda, (float) celda(otro).getCenterY());
                    }
                    float sx = 0;
                    float sy = 0;
                    for (int n : nums) {
                        sx += celda(n).getCenterX();
                        sy += celda(n).getCenterY();
                    }
                    return new Point2D.Float(sx / nums.size(), sy / nums.size());
                }
                case CALLE: {
                    int n = nums.iterator().next();
                    return new Point2D.Float((float) celda(n).getCenterX(), arriba + 3 * ch);
                }
                case SEISENA: {
                    int n = nums.iterator().next();
                    return new Point2D.Float((float) celda(n).getMaxX(), arriba + 3 * ch);
                }
                case DOCENA: {
                    Rectangle2D r = celdaDocena(nums.iterator().next() / 12 + 1);
                    return new Point2D.Float((float) r.getCenterX(), (float) r.getCenterY());
                }
                case COLUMNA: {
                    Rectangle2D r = celdaColumna(nums.iterator().next());
                    return new Point2D.Float((float) r.getCenterX(), (float) r.getCenterY());
                }
                default: {
                    for (int i = 0; i < SENCILLAS; i++) {
                        if (sencilla(i) == a.getTipo()) {
                            Rectangle2D r = celdaSencilla(i);
                            return new Point2D.Float((float) r.getCenterX(), (float) r.getCenterY());
                        }
                    }
                    return new Point2D.Float(0, 0);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            calcular();
            Set<Integer> resaltar = bajoCursor != null && !girando() ? bajoCursor.getNumeros() : Set.of();

            // cero
            celdaPintada(g, celdaCero(), Tema.VERDE_CERO, "0", resaltar.contains(0), numeroGanador != null && numeroGanador == 0);
            for (int n = 1; n <= 36; n++) {
                celdaPintada(g, celda(n), colorNumero(n), String.valueOf(n), resaltar.contains(n),
                        numeroGanador != null && numeroGanador == n);
            }
            for (int col = 1; col <= 3; col++) {
                boolean marcado = bajoCursor != null && bajoCursor.getTipo() == TipoApuestaRuleta.COLUMNA
                        && bajoCursor.getNumeros().contains(col);
                celdaPintada(g, celdaColumna(col), null, "2 a 1", marcado, false);
            }
            String[] docenas = {"1ª 12", "2ª 12", "3ª 12"};
            for (int d = 1; d <= 3; d++) {
                boolean marcado = bajoCursor != null && bajoCursor.getTipo() == TipoApuestaRuleta.DOCENA
                        && bajoCursor.getNumeros().contains(12 * (d - 1) + 1);
                celdaPintada(g, celdaDocena(d), null, docenas[d - 1], marcado, false);
            }
            String[] textos = {"1 a 18", "PAR", "", "", "IMPAR", "19 a 36"};
            for (int i = 0; i < SENCILLAS; i++) {
                boolean marcado = bajoCursor != null && bajoCursor.getTipo() == sencilla(i);
                Color fondo = i == 2 ? Tema.ROJO_RULETA : i == 3 ? Tema.NEGRO_RULETA : null;
                celdaPintada(g, celdaSencilla(i), fondo, textos[i], marcado, false);
                if (i == 2 || i == 3) {
                    Rectangle2D r = celdaSencilla(i);
                    float cx = (float) r.getCenterX();
                    float cy = (float) r.getCenterY();
                    java.awt.Polygon rombo = new java.awt.Polygon(
                            new int[]{(int) (cx - cw * 0.55f), (int) cx, (int) (cx + cw * 0.55f), (int) cx},
                            new int[]{(int) cy, (int) (cy - ch * 0.28f), (int) cy, (int) (cy + ch * 0.28f)}, 4);
                    g.setColor(i == 2 ? new Color(0xE53935) : new Color(0x111111));
                    g.fill(rombo);
                    g.setColor(Color.WHITE);
                    g.draw(rombo);
                }
            }

            // fichas
            for (ApuestaRuleta a : ruleta.getApuestas()) {
                Point2D.Float p = ancla(a);
                Dibujo.ficha(g, valorFicha(a.getMonto()), p.x, p.y, Math.min(cw, ch) * 0.3f, false);
                if (valorFicha(a.getMonto()) != a.getMonto()) {
                    g.setFont(Tema.negrita(10));
                    String t = Dibujo.textoFicha(a.getMonto());
                    int tw = g.getFontMetrics().stringWidth(t) + 6;
                    g.setColor(new Color(0, 0, 0, 190));
                    g.fillRoundRect((int) (p.x - tw / 2f), (int) (p.y + Math.min(cw, ch) * 0.3f - 2), tw, 14, 8, 8);
                    g.setColor(Tema.ORO_CLARO);
                    Dibujo.centrar(g, t, p.x, p.y + Math.min(cw, ch) * 0.3f + 5);
                }
            }
            // ficha fantasma bajo el cursor
            if (bajoCursor != null && !girando()) {
                Point2D.Float p = ancla(bajoCursor);
                g.setColor(new Color(255, 255, 255, 150));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 4f}, 0f));
                float rr = Math.min(cw, ch) * 0.3f;
                g.draw(new Ellipse2D.Float(p.x - rr, p.y - rr, rr * 2, rr * 2));
            }
            g.dispose();
        }

        /** Ficha más grande que no supera el monto (para el color de la pila). */
        private long valorFicha(long monto) {
            for (int i = FICHAS.length - 1; i >= 0; i--) {
                if (monto >= FICHAS[i]) {
                    return FICHAS[i];
                }
            }
            return FICHAS[0];
        }

        private void celdaPintada(Graphics2D g, Rectangle2D r, Color fondo, String texto, boolean resaltada, boolean ganadora) {
            if (fondo != null) {
                g.setColor(fondo);
                g.fill(r);
            }
            if (resaltada) {
                g.setColor(new Color(255, 225, 120, 110));
                g.fill(r);
            }
            if (ganadora) {
                g.setColor(new Color(255, 225, 120, 200));
                g.fill(r);
            }
            g.setColor(new Color(0xEDE3C8));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(r);
            if (!texto.isEmpty()) {
                g.setColor(ganadora ? Color.BLACK : Color.WHITE);
                g.setFont(Tema.negrita(Math.max(11, Math.min(cw, ch) * 0.34f)));
                Dibujo.centrar(g, texto, (float) r.getCenterX(), (float) r.getCenterY());
            }
        }
    }
}
