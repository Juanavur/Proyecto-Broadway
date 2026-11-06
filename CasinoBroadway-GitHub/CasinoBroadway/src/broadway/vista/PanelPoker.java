package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.cartas.Carta;
import broadway.juegos.poker.AccionPoker;
import broadway.juegos.poker.AsientoPoker;
import broadway.juegos.poker.EvaluadorPoker;
import broadway.juegos.poker.FasePoker;
import broadway.juegos.poker.PokerTexasHoldem;
import broadway.modelo.Formato;
import broadway.modelo.ResultadoRonda;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Mesa de Texas Hold'em contra tres rivales. */
public class PanelPoker extends PanelJuego {

    private static final int PASO_SLIDER = 500;

    private final PokerTexasHoldem poker;
    private final Mesa mesa = new Mesa();
    private final JTextArea registro = new JTextArea();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel controles = new JPanel(tarjetas);
    private final JLabel mensaje = Tema.etiqueta(" ", Tema.negrita(16), Tema.CREMA);

    private final JSlider compra = new JSlider(20, 200, 100);
    private final JLabel compraLbl = Tema.etiqueta("", Tema.negrita(16), Tema.ORO_CLARO);
    private final BotonCasino igualar = new BotonCasino("Pasar", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino retirarse = new BotonCasino("No ir", BotonCasino.Estilo.PELIGRO);
    private final BotonCasino subir = new BotonCasino("Subir");
    private final BotonCasino allIn = new BotonCasino("All-in");
    private final JSlider montoSubida = new JSlider(0, 1, 0);
    private final JLabel subidaLbl = Tema.etiqueta("", Tema.negrita(15), Tema.ORO_CLARO);
    private final BotonCasino recomprar = new BotonCasino("Recomprar");
    private final Timer turnosRivales;
    private int cartasMesaAntes;

    public PanelPoker(VentanaPrincipal ventana) {
        super(ventana, ventana.getCasino().getPoker());
        this.poker = ventana.getCasino().getPoker();

        PanelFieltro centro = new PanelFieltro(new BorderLayout());
        centro.add(mesa, BorderLayout.CENTER);
        registro.setEditable(false);
        registro.setFont(Tema.texto(12));
        registro.setLineWrap(true);
        registro.setWrapStyleWord(true);
        registro.setBorder(Tema.margen(6, 8, 6, 8));
        JPanel lateral = new JPanel(new BorderLayout(0, 4));
        lateral.setBackground(Tema.PANEL);
        lateral.setBorder(Tema.margen(8, 8, 8, 8));
        lateral.add(Tema.etiqueta("Lo que pasa en la mesa", Tema.negrita(13), Tema.ORO_CLARO), BorderLayout.NORTH);
        lateral.add(Dialogos.scroll(registro), BorderLayout.CENTER);
        lateral.setPreferredSize(new Dimension(290, 10));
        JPanel medio = new JPanel(new BorderLayout());
        medio.add(centro, BorderLayout.CENTER);
        medio.add(lateral, BorderLayout.EAST);
        add(medio, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setBackground(Tema.PANEL);
        abajo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE), Tema.margen(8, 18, 10, 18)));
        mensaje.setHorizontalAlignment(JLabel.CENTER);
        mensaje.setBorder(Tema.margen(0, 0, 6, 0));
        abajo.add(mensaje, BorderLayout.NORTH);
        controles.setOpaque(false);

        // Sentarse
        JPanel sentarse = fila();
        sentarse.add(Tema.etiqueta("Fichas para la mesa:", Tema.texto(15), Tema.CREMA));
        estiloSlider(compra);
        compra.setPreferredSize(new Dimension(320, 40));
        compra.addChangeListener(e -> compraLbl.setText(Formato.pesos(compra.getValue() * 1_000L)));
        sentarse.add(compra);
        sentarse.add(compraLbl);
        BotonCasino sentar = new BotonCasino("Sentarse a la mesa", BotonCasino.Estilo.PRIMARIO);
        sentar.addActionListener(e -> sentarse());
        sentarse.add(sentar);
        controles.add(sentarse, "sentarse");

        // Entre manos
        JPanel entre = fila();
        BotonCasino nueva = new BotonCasino("Repartir nueva mano", BotonCasino.Estilo.PRIMARIO);
        nueva.setFont(Tema.negrita(17));
        nueva.addActionListener(e -> nuevaMano());
        recomprar.addActionListener(e -> recomprar());
        BotonCasino levantarse = new BotonCasino("Levantarse");
        levantarse.addActionListener(e -> levantarse());
        entre.add(nueva);
        entre.add(recomprar);
        entre.add(levantarse);
        controles.add(entre, "entre");

        // Tu turno
        JPanel turno = fila();
        retirarse.addActionListener(e -> actuar(AccionPoker.RETIRARSE, 0));
        igualar.addActionListener(e -> actuar(poker.getPorIgualarJugador() > 0 ? AccionPoker.IGUALAR : AccionPoker.PASAR, 0));
        subir.addActionListener(e -> actuar(AccionPoker.SUBIR, montoSubida.getValue() * (long) PASO_SLIDER));
        allIn.addActionListener(e -> actuar(AccionPoker.TODO, 0));
        estiloSlider(montoSubida);
        montoSubida.setPreferredSize(new Dimension(220, 40));
        montoSubida.addChangeListener(e -> actualizarSubida());
        BotonCasino medioBote = new BotonCasino("½ bote");
        medioBote.addActionListener(e -> fijarSubida(poker.getApuestaActual() + poker.getBote() / 2));
        BotonCasino bote = new BotonCasino("Bote");
        bote.addActionListener(e -> fijarSubida(poker.getApuestaActual() + poker.getBote() + poker.getPorIgualarJugador()));
        for (BotonCasino b : new BotonCasino[]{retirarse, igualar, subir, allIn}) {
            b.setFont(Tema.negrita(16));
        }
        turno.add(retirarse);
        turno.add(igualar);
        turno.add(medioBote);
        turno.add(bote);
        turno.add(montoSubida);
        turno.add(subidaLbl);
        turno.add(subir);
        turno.add(allIn);
        controles.add(turno, "turno");

        JPanel espera = fila();
        espera.add(Tema.etiqueta("Juegan los rivales...", Tema.negrita(16), Tema.TEXTO_SUAVE));
        controles.add(espera, "espera");

        abajo.add(controles, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        turnosRivales = new Timer(850, e -> turnoRival());
        turnosRivales.setRepeats(true);
        compraLbl.setText(Formato.pesos(compra.getValue() * 1_000L));
        actualizar();
    }

    private static JPanel fila() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        p.setOpaque(false);
        return p;
    }

    private static void estiloSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(Tema.CREMA);
        s.setFocusable(false);
    }

    @Override
    protected String textoLimites() {
        return "Ciegas " + Formato.pesos(PokerTexasHoldem.CIEGA_PEQUENA) + " / " + Formato.pesos(PokerTexasHoldem.CIEGA_GRANDE)
                + " · Compra " + Formato.pesos(getJuego().getApuestaMinima()) + " a " + Formato.pesos(getJuego().getApuestaMaxima());
    }

    // ============================================================== acciones

    private void sentarse() {
        long monto = compra.getValue() * 1_000L;
        if (intentar(() -> poker.sentarse(jugador(), monto))) {
            Sonidos.reproducir(Efecto.FICHAS);
            ventana.actualizarSaldo();
            mensaje.setText("Te sentaste con " + Formato.pesos(monto) + ". Reparte la primera mano.");
            actualizar();
        }
    }

    private void nuevaMano() {
        if (intentar(poker::nuevaMano)) {
            cartasMesaAntes = 0;
            Sonidos.reproducir(Efecto.BARAJAR);
            Sonidos.reproducir(Efecto.FICHAS, 950);
            for (int i = 0; i < 8; i++) {
                Sonidos.reproducir(Efecto.CARTA, 1100 + i * 110);
            }
            mensaje.setText("Mano #" + poker.getManosJugadas() + " · " + poker.getFase().getNombre());
            mensaje.setForeground(Tema.CREMA);
            actualizar();
            programarRivales();
        }
    }

    private void actuar(AccionPoker accion, long monto) {
        if (intentar(() -> poker.accionJugador(accion, monto))) {
            sonidoAccion(poker.getHumano());
            actualizar();
            programarRivales();
        }
    }

    private void programarRivales() {
        if (poker.esTurnoRival() && !turnosRivales.isRunning()) {
            turnosRivales.start();
        }
        if (!poker.isManoEnCurso()) {
            manoTerminada();
        }
    }

    private void turnoRival() {
        if (!poker.esTurnoRival()) {
            turnosRivales.stop();
            actualizar();
            if (!poker.isManoEnCurso()) {
                manoTerminada();
            }
            return;
        }
        AsientoPoker quien = poker.getAsientos().get(poker.getTurno());
        poker.ejecutarTurnoRival();
        sonidoAccion(quien);
        actualizar();
        if (!poker.isManoEnCurso()) {
            turnosRivales.stop();
            manoTerminada();
        } else if (!poker.esTurnoRival()) {
            turnosRivales.stop();
        }
    }

    /** Sonido según lo que hizo un jugador y las cartas nuevas que salieron a la mesa. */
    private void sonidoAccion(AsientoPoker a) {
        String accion = a.getUltimaAccion();
        if (accion.startsWith("Pasa")) {
            Sonidos.reproducir(Efecto.GOLPE);
        } else if (accion.startsWith("No va") || accion.startsWith("Se levant")) {
            Sonidos.reproducir(Efecto.CARTA);
        } else {
            Sonidos.reproducir(Efecto.FICHAS);
        }
        int ahora = poker.getComunitarias().size();
        for (int i = cartasMesaAntes; i < ahora; i++) {
            Sonidos.reproducir(Efecto.CARTA, 400 + (i - cartasMesaAntes) * 140);
        }
        cartasMesaAntes = ahora;
    }

    private void manoTerminada() {
        if (poker.esGanador(0)) {
            Sonidos.reproducir(Efecto.GANAR, 500);
        }
        Sonidos.reproducir(Efecto.FICHAS, 800);
        String resumen = String.join("  ", poker.getResumen());
        mensaje.setText(resumen.isEmpty() ? "Mano terminada." : resumen);
        boolean gane = poker.esGanador(0);
        mensaje.setForeground(gane ? Tema.EXITO : Tema.ORO_CLARO);
        ventana.guardarEnSilencio();
        actualizar();
    }

    private void recomprar() {
        long falta = poker.getApuestaMaxima() - poker.getHumano().getFichas();
        long monto = Math.min(falta, Math.max(PokerTexasHoldem.CIEGA_GRANDE, Math.min(100_000, jugador().getFichas())));
        monto = monto / 1_000 * 1_000;
        long m = monto;
        if (m <= 0) {
            ventana.aviso("Ya tienes el máximo permitido en la mesa.", Tema.ERROR);
            return;
        }
        if (intentar(() -> poker.recomprar(m))) {
            ventana.actualizarSaldo();
            ventana.aviso("Agregaste " + Formato.pesos(m) + " a tu puesto.", Tema.EXITO);
            actualizar();
        }
    }

    private void levantarse() {
        ResultadoRonda r = poker.levantarse();
        rondaTerminada(r);
        mensaje.setText("Te levantaste de la mesa. Sesión: " + textoNeto(r) + ".");
        mensaje.setForeground(r.getNeto() >= 0 ? Tema.EXITO : Tema.ERROR);
        registro.setText("");
        actualizar();
    }

    private void fijarSubida(long objetivo) {
        long v = Math.max(poker.getMinimoSubirA(), Math.min(poker.getMaximoSubirAJugador(), objetivo));
        montoSubida.setValue((int) (v / PASO_SLIDER));
        actualizarSubida();
    }

    private void actualizarSubida() {
        long v = montoSubida.getValue() * (long) PASO_SLIDER;
        if (poker.isSentado() && v >= poker.getMaximoSubirAJugador()) {
            v = poker.getMaximoSubirAJugador();
        }
        subidaLbl.setText(Formato.pesos(v));
        subir.setText(poker.getApuestaActual() == 0 ? "Apostar" : "Subir a");
    }

    private void actualizar() {
        if (!poker.isSentado()) {
            tarjetas.show(controles, "sentarse");
            if (mensaje.getText().isBlank()) {
                mensaje.setText("Escoge cuántas fichas llevas a la mesa y siéntate.");
            }
        } else if (poker.esTurnoJugador()) {
            tarjetas.show(controles, "turno");
            long porIgualar = poker.getPorIgualarJugador();
            long fichas = poker.getHumano().getFichas();
            igualar.setText(porIgualar > 0 ? "Igualar " + Formato.pesos(Math.min(porIgualar, fichas)) : "Pasar");
            retirarse.setEnabled(true);
            boolean puede = poker.puedeSubirJugador();
            subir.setEnabled(puede && poker.getMaximoSubirAJugador() >= poker.getMinimoSubirA());
            allIn.setEnabled(puede || porIgualar > 0);
            if (puede) {
                int min = (int) Math.ceil(poker.getMinimoSubirA() / (double) PASO_SLIDER);
                int max = (int) Math.ceil(poker.getMaximoSubirAJugador() / (double) PASO_SLIDER);
                montoSubida.setMinimum(Math.min(min, max));
                montoSubida.setMaximum(max);
                montoSubida.setValue(Math.min(min, max));
            }
            montoSubida.setEnabled(puede);
            actualizarSubida();
            String mano = describirMano();
            mensaje.setText("Tu turno." + (porIgualar > 0 ? " Para seguir debes poner " + Formato.pesos(porIgualar) + "." : "")
                    + (mano.isEmpty() ? "" : "  Tienes: " + mano + "."));
            mensaje.setForeground(Tema.ORO_CLARO);
        } else if (poker.isManoEnCurso()) {
            tarjetas.show(controles, "espera");
        } else {
            tarjetas.show(controles, "entre");
            recomprar.setEnabled(poker.getHumano().getFichas() < poker.getApuestaMaxima());
        }
        if (poker.isSentado()) {
            List<String> r = poker.getRegistro();
            StringBuilder sb = new StringBuilder();
            for (String linea : r) {
                sb.append(linea).append('\n');
            }
            registro.setText(sb.toString());
            registro.setCaretPosition(registro.getDocument().getLength());
        }
        mesa.repaint();
    }

    private String describirMano() {
        List<Carta> mias = new ArrayList<>(poker.getHumano().getCartas());
        mias.addAll(poker.getComunitarias());
        if (mias.size() < 5) {
            Carta a = poker.getHumano().getCartas().get(0);
            Carta b = poker.getHumano().getCartas().get(1);
            return a.getValor() == b.getValor() ? "pareja en mano" : "";
        }
        return EvaluadorPoker.evaluar(mias).getDescripcion();
    }

    @Override
    public boolean alSalir() {
        if (!poker.isSentado()) {
            return true;
        }
        String texto = "Te levantarás de la mesa con " + Formato.pesos(poker.getHumano().getFichas()) + " en fichas.";
        if (poker.isManoEnCurso() && !poker.getHumano().isRetirado()) {
            texto += " Como hay una mano en curso, abandonarás tus cartas (no ir).";
        }
        if (!Dialogos.confirmar(this, "Salir del póker", texto, "Levantarme")) {
            return false;
        }
        turnosRivales.stop();
        levantarse();
        return true;
    }

    @Override
    public void prepararCierre() {
        turnosRivales.stop();
        if (poker.isSentado()) {
            poker.levantarse();
        }
    }

    @Override
    public void alEntrar() {
        actualizar();
    }

    @Override
    public void dibujarIcono(Graphics2D g, float x, float y, float t) {
        float w = t * 0.40f;
        float h = w * 1.4f;
        Carta[] cs = {new Carta(broadway.cartas.Valor.AS, broadway.cartas.Palo.TREBOLES),
                new Carta(broadway.cartas.Valor.AS, broadway.cartas.Palo.DIAMANTES),
                new Carta(broadway.cartas.Valor.AS, broadway.cartas.Palo.CORAZONES),
                new Carta(broadway.cartas.Valor.AS, broadway.cartas.Palo.PICAS)};
        for (int i = 0; i < cs.length; i++) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate(Math.toRadians(-30 + i * 20), x + t / 2, y + t * 1.0f);
            Dibujo.carta(g2, cs[i], Math.round(x + t / 2 - w / 2), Math.round(y + t * 0.08f), Math.round(w), Math.round(h));
            g2.dispose();
        }
    }

    /** Dibujo de la mesa ovalada con los cuatro jugadores. */
    private class Mesa extends JComponent {

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            float mx = w * 0.10f;
            float my = h * 0.14f;
            float mw = w - 2 * mx;
            float mh = h - 2 * my;
            // borde de madera y paño
            g.setPaint(new GradientPaint(0, my, new Color(0x6B3E1D), 0, my + mh, new Color(0x3A200C)));
            g.fill(new RoundRectangle2D.Float(mx - 18, my - 18, mw + 36, mh + 36, mh + 36, mh + 36));
            g.setPaint(new RadialGradientPaint(new Point2D.Float(w / 2f, h / 2f), mw * 0.6f, new float[]{0f, 1f},
                    new Color[]{new Color(0x1A8F59), new Color(0x0B5233)}));
            g.fill(new RoundRectangle2D.Float(mx, my, mw, mh, mh, mh));
            g.setColor(new Color(255, 255, 255, 35));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(mx + 20, my + 20, mw - 40, mh - 40, mh - 40, mh - 40));

            if (!poker.isSentado()) {
                g.setColor(new Color(255, 255, 255, 130));
                g.setFont(Tema.titulo(30));
                Dibujo.centrar(g, "TEXAS HOLD'EM", w / 2f, h / 2f - 16);
                g.setFont(Tema.texto(15));
                Dibujo.centrar(g, "Sin límite · Ciegas $500 / $1.000 · 3 rivales", w / 2f, h / 2f + 18);
                g.dispose();
                return;
            }

            int cw = (int) Math.max(44, Math.min(78, Math.min(w / 14.0, h / 8.2)));
            int ch = (int) (cw * 1.42);

            // cartas comunitarias y bote
            List<Carta> mesaCartas = poker.getComunitarias();
            int sep = 8;
            int total = 5 * cw + 4 * sep;
            int x0 = (w - total) / 2;
            int y0 = (int) (h / 2f - ch / 2f - 6);
            for (int i = 0; i < 5; i++) {
                if (i < mesaCartas.size()) {
                    Dibujo.carta(g, mesaCartas.get(i), x0 + i * (cw + sep), y0, cw, ch);
                } else {
                    g.setColor(new Color(255, 255, 255, 30));
                    g.drawRoundRect(x0 + i * (cw + sep), y0, cw, ch, 10, 10);
                }
            }
            g.setFont(Tema.negrita(16));
            String bote = "Bote: " + Formato.pesos(poker.getBote()) + "  ·  " + poker.getFase().getNombre();
            etiqueta(g, bote, w / 2f, y0 + ch + 22, Tema.ORO_CLARO);

            // asientos
            float[][] lugares = {
                    {w / 2f, my + mh + 4},
                    {mx + 10, h / 2f},
                    {w / 2f, my - 4},
                    {mx + mw - 10, h / 2f}};
            float[][] apuestas = {
                    {w / 2f, my + mh - ch * 0.45f},
                    {mx + mw * 0.2f, h / 2f + ch * 0.9f},
                    {w / 2f, my + 18 + ch * 0.82f + 14},
                    {mx + mw * 0.8f, h / 2f + ch * 0.9f}};
            List<AsientoPoker> asientos = poker.getAsientos();
            for (int i = 0; i < asientos.size(); i++) {
                dibujarAsiento(g, i, asientos.get(i), lugares[i][0], lugares[i][1], cw, ch);
                AsientoPoker a = asientos.get(i);
                if (a.getApuestaRonda() > 0) {
                    float ax = apuestas[i][0];
                    float ay = apuestas[i][1];
                    Dibujo.ficha(g, Math.max(1_000, a.getApuestaRonda()), ax - 30, ay, 14, false);
                    g.setFont(Tema.negrita(13));
                    g.setColor(Tema.CREMA);
                    g.drawString(Formato.pesos(a.getApuestaRonda()), ax - 12, ay + 5);
                }
            }
            g.dispose();
        }

        private void dibujarAsiento(Graphics2D g, int i, AsientoPoker a, float cx, float cy, int cw, int ch) {
            boolean turno = poker.getTurno() == i && poker.isManoEnCurso();
            boolean gana = poker.getFase() == FasePoker.MANO_TERMINADA && poker.esGanador(i);
            float bw = 190;
            float bh = 58;
            float bx = cx - bw / 2;
            float by = cy - bh / 2;
            // cartas
            List<Carta> cartas = a.getCartas();
            if (!cartas.isEmpty() && !a.isRetirado()) {
                boolean visibles = a.isHumano() || (poker.isCartasDescubiertas() && !a.isRetirado());
                int ccw = (int) (cw * (a.isHumano() ? 1.0 : 0.82));
                int cch = (int) (ccw * 1.42);
                float cartasY = i == 0 ? by - cch + 10 : i == 2 ? by + bh + 2 : by - cch + 4;
                float cartasX = cx - ccw - 3;
                for (int k = 0; k < cartas.size(); k++) {
                    Dibujo.carta(g, visibles ? cartas.get(k) : null, Math.round(cartasX + k * (ccw + 6)), Math.round(cartasY), ccw, cch);
                }
            }
            // recuadro
            if (turno || gana) {
                g.setColor(gana ? new Color(0x4CD08A) : Tema.ORO_CLARO);
                g.setStroke(new BasicStroke(4f));
                g.draw(new RoundRectangle2D.Float(bx - 3, by - 3, bw + 6, bh + 6, 22, 22));
            }
            g.setColor(a.isRetirado() ? new Color(20, 20, 20, 200) : new Color(10, 25, 18, 235));
            g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 18, 18));
            g.setColor(Tema.ORO_OSCURO);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 18, 18));
            // avatar
            Color c = a.isHumano() ? Tema.colorNivel(jugador().getNivel()) : new Color(0x5C7C70);
            g.setColor(c);
            g.fill(new Ellipse2D.Float(bx + 8, by + 9, 40, 40));
            g.setColor(new Color(0x111111));
            g.setFont(Tema.negrita(16));
            Dibujo.centrar(g, a.getNombre().substring(0, 1).toUpperCase(), bx + 28, by + 29);
            g.setFont(Tema.negrita(14));
            g.setColor(a.isRetirado() ? Tema.TEXTO_SUAVE : Tema.CREMA);
            String nombre = a.isHumano() ? a.getNombre() + " (tú)" : a.getNombre();
            g.drawString(recortar(g, nombre, bw - 64), bx + 56, by + 22);
            g.setFont(Tema.negrita(13));
            g.setColor(Tema.ORO_CLARO);
            g.drawString(Formato.pesos(a.getFichas()) + (a.isAllIn() ? "  ALL-IN" : ""), bx + 56, by + 40);
            g.setFont(Tema.texto(11));
            g.setColor(Tema.TEXTO_SUAVE);
            String extra = a.isHumano() ? "" : a.getEstrategia().getNombre();
            g.drawString(extra, bx + 56, by + 53);
            // botón del repartidor
            if (poker.getBoton() == i) {
                float dx = bx + bw - 8;
                float dy = by - 6;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Float(dx - 11, dy - 11, 22, 22));
                g.setColor(Color.BLACK);
                g.setFont(Tema.negrita(12));
                Dibujo.centrar(g, "D", dx, dy);
            }
            // última acción o mano final
            String accion = a.getUltimaAccion();
            if (poker.isCartasDescubiertas() && a.getManoFinal() != null) {
                accion = a.getManoFinal().getDescripcion();
            }
            if (gana) {
                accion = "GANA" + (a.getManoFinal() != null ? " · " + a.getManoFinal().getDescripcion() : "");
            }
            if (accion != null && !accion.isEmpty()) {
                Color color = gana ? Tema.EXITO : a.isRetirado() ? Tema.TEXTO_SUAVE : Tema.CREMA;
                if (i == 0) {
                    // a la derecha de tu puesto, para no tapar el bote
                    g.setFont(Tema.negrita(13));
                    float ancho = g.getFontMetrics().stringWidth(accion) + 18;
                    etiqueta(g, accion, bx + bw + 12 + ancho / 2, cy, color);
                } else {
                    etiqueta(g, accion, cx, i == 2 ? by - 16 : by + bh + 14, color);
                }
            }
        }

        private String recortar(Graphics2D g, String t, float ancho) {
            String r = t;
            while (g.getFontMetrics().stringWidth(r) > ancho && r.length() > 3) {
                r = r.substring(0, r.length() - 2) + "…";
            }
            return r;
        }

        private void etiqueta(Graphics2D g, String texto, float cx, float cy, Color color) {
            g.setFont(Tema.negrita(13));
            int tw = g.getFontMetrics().stringWidth(texto) + 18;
            cx = Math.max(tw / 2f + 4, Math.min(getWidth() - tw / 2f - 4, cx)); // que no se salga del panel
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect((int) (cx - tw / 2f), (int) (cy - 12), tw, 24, 24, 24);
            g.setColor(color);
            Dibujo.centrar(g, texto, cx, cy);
        }
    }
}
