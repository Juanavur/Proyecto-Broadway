package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.ServicioSonido;
import broadway.sonido.Sonidos;
import broadway.juegos.tragamonedas.LineaPremiada;
import broadway.juegos.tragamonedas.ResultadoGiro;
import broadway.juegos.tragamonedas.Simbolo;
import broadway.juegos.tragamonedas.Tragamonedas;
import broadway.modelo.Formato;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/** Pantalla de la tragamonedas: carretes animados, líneas de pago y tabla de premios. */
public class PanelTragamonedas extends PanelJuego {

    private static final Color[] COLOR_LINEA = {
            new Color(0xFFD54F), new Color(0x4FC3F7), new Color(0x81C784), new Color(0xFF8A65), new Color(0xBA68C8)};

    private final Tragamonedas maquina;
    private final Maquina vista = new Maquina();
    private final SelectorFichas selector = new SelectorFichas(Tragamonedas.VALORES_LINEA, 22);
    private final JLabel lineasLbl = Tema.etiqueta("", Tema.negrita(16), Tema.CREMA);
    private final JLabel apuestaLbl = Tema.etiqueta("", Tema.negrita(16), Tema.ORO_CLARO);
    private final JLabel resultado = Tema.etiqueta("Escoge el valor por línea y gira.", Tema.negrita(18), Tema.CREMA);
    private final BotonCasino girar = new BotonCasino("GIRAR", BotonCasino.Estilo.PRIMARIO);
    private int lineas = 5;

    // Estado de la animación
    private final double[] posicion = new double[Tragamonedas.CARRETES];
    private final double[] inicio = new double[Tragamonedas.CARRETES];
    private final double[] destino = new double[Tragamonedas.CARRETES];
    private long inicioAnimacion;
    private Timer animacion;
    private ResultadoGiro ultimo;
    private boolean mostrarPremios;
    private final boolean[] detenido = new boolean[Tragamonedas.CARRETES];
    private ServicioSonido.Reproduccion sonidoCarretes;

    public PanelTragamonedas(VentanaPrincipal ventana) {
        super(ventana, ventana.getCasino().getTragamonedas());
        this.maquina = ventana.getCasino().getTragamonedas();
        for (int c = 0; c < Tragamonedas.CARRETES; c++) {
            posicion[c] = c * 7;
        }

        PanelFieltro centro = new PanelFieltro(new BorderLayout(16, 0));
        centro.setBorder(Tema.margen(14, 18, 10, 18));
        centro.add(vista, BorderLayout.CENTER);
        centro.add(new TablaPagos(), BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        JPanel controles = new JPanel(new BorderLayout());
        controles.setBackground(Tema.PANEL);
        controles.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE), Tema.margen(10, 18, 10, 18)));
        resultado.setHorizontalAlignment(JLabel.CENTER);
        resultado.setBorder(Tema.margen(0, 0, 8, 0));
        controles.add(resultado, BorderLayout.NORTH);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(Tema.etiqueta("Valor por línea", Tema.texto(14), Tema.TEXTO_SUAVE));
        selector.setAlClic(v -> actualizarApuesta());
        izq.add(selector);
        BotonCasino menos = new BotonCasino("−");
        BotonCasino mas = new BotonCasino("+");
        menos.addActionListener(e -> cambiarLineas(-1));
        mas.addActionListener(e -> cambiarLineas(1));
        izq.add(menos);
        izq.add(lineasLbl);
        izq.add(mas);
        controles.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        der.setOpaque(false);
        der.add(apuestaLbl);
        girar.setFont(Tema.negrita(22));
        girar.setPreferredSize(new Dimension(180, 52));
        girar.addActionListener(e -> girar());
        der.add(girar);
        controles.add(der, BorderLayout.EAST);
        add(controles, BorderLayout.SOUTH);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "girar");
        getActionMap().put("girar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowing() && girar.isEnabled()) {
                    girar();
                }
            }
        });
        actualizarApuesta();
    }

    @Override
    protected String textoLimites() {
        return "Hasta 5 líneas · " + Formato.pesos(Tragamonedas.VALORES_LINEA[0]) + " a "
                + Formato.pesos(Tragamonedas.VALORES_LINEA[Tragamonedas.VALORES_LINEA.length - 1]) + " por línea · Barra espaciadora: girar";
    }

    private void cambiarLineas(int delta) {
        if (animacion != null && animacion.isRunning()) {
            return;
        }
        lineas = Math.max(1, Math.min(Tragamonedas.MAX_LINEAS, lineas + delta));
        mostrarPremios = false;
        actualizarApuesta();
        vista.repaint();
    }

    private void actualizarApuesta() {
        lineasLbl.setText(lineas + (lineas == 1 ? " línea" : " líneas"));
        apuestaLbl.setText("Apuesta: " + Formato.pesos(selector.getValor() * lineas));
        repaint();
    }

    private void girar() {
        if (animacion != null && animacion.isRunning()) {
            return;
        }
        long valor = selector.getValor();
        final ResultadoGiro[] r = new ResultadoGiro[1];
        if (!intentar(() -> r[0] = maquina.girar(jugador(), valor, lineas))) {
            return;
        }
        ultimo = r[0];
        mostrarPremios = false;
        // Muestra las fichas ya descontadas (el premio se ve al detenerse los carretes).
        ventana.actualizarSaldo();
        resultado.setText("Girando...");
        resultado.setForeground(Tema.CREMA);
        girar.setEnabled(false);
        for (int c = 0; c < Tragamonedas.CARRETES; c++) {
            int largo = maquina.getCarrete(c).size();
            inicio[c] = posicion[c];
            double objetivo = ultimo.getParada(c);
            double base = Math.floor(inicio[c] / largo) * largo;
            destino[c] = base + objetivo - (3 + c) * largo;
        }
        java.util.Arrays.fill(detenido, false);
        sonidoCarretes = Sonidos.bucle(Efecto.RODILLOS);
        inicioAnimacion = System.currentTimeMillis();
        animacion = new Timer(15, e -> pasoAnimacion());
        animacion.start();
    }

    private void pasoAnimacion() {
        long t = System.currentTimeMillis() - inicioAnimacion;
        boolean todos = true;
        for (int c = 0; c < Tragamonedas.CARRETES; c++) {
            double duracion = 1100 + 450 * c;
            double p = Math.min(1.0, t / duracion);
            double suave = 1 - Math.pow(1 - p, 3);
            posicion[c] = inicio[c] + (destino[c] - inicio[c]) * suave;
            if (p < 1) {
                todos = false;
            } else if (!detenido[c]) {
                detenido[c] = true;
                Sonidos.reproducir(Efecto.RODILLO_PARA);
                if (c == Tragamonedas.CARRETES - 1) {
                    sonidoCarretes.detener();
                }
            }
        }
        vista.repaint();
        if (todos) {
            animacion.stop();
            for (int c = 0; c < Tragamonedas.CARRETES; c++) {
                posicion[c] = ultimo.getParada(c);
            }
            terminarGiro();
        }
    }

    private void terminarGiro() {
        girar.setEnabled(true);
        mostrarPremios = true;
        vista.repaint();
        List<LineaPremiada> premiadas = ultimo.getLineasPremiadas();
        if (premiadas.isEmpty()) {
            resultado.setText("Sin premio. ¡Otra vez!");
            resultado.setForeground(Tema.TEXTO_SUAVE);
        } else {
            StringBuilder sb = new StringBuilder("¡Premio de " + Formato.pesos(ultimo.getPremioTotal()) + "!  ");
            for (LineaPremiada l : premiadas) {
                sb.append("  Línea ").append(l.getNumeroLinea()).append(": ").append(l.getCombinacion())
                        .append(" ×").append(l.getMultiplicador());
            }
            resultado.setText(sb.toString());
            resultado.setForeground(Tema.EXITO);
            boolean grande = ultimo.getPremioTotal() >= 50 * selector.getValor();
            Sonidos.reproducir(grande ? Efecto.GANAR_GRANDE : Efecto.GANAR, 120);
            Sonidos.reproducir(Efecto.FICHAS, grande ? 900 : 450);
        }
        rondaTerminada(ultimo.getRonda());
    }

    @Override
    public boolean alSalir() {
        if (animacion != null && animacion.isRunning()) {
            ventana.aviso("Espera a que se detengan los carretes.", Tema.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void prepararCierre() {
        if (animacion != null) {
            animacion.stop();
        }
        if (sonidoCarretes != null) {
            sonidoCarretes.detener();
        }
    }

    @Override
    public void dibujarIcono(Graphics2D g, float x, float y, float t) {
        Dibujo.simbolo(g, Simbolo.CEREZA, x - t * 0.28f, y + t * 0.22f, t * 0.62f);
        Dibujo.simbolo(g, Simbolo.SIETE, x + t * 0.12f, y, t * 0.78f);
        Dibujo.simbolo(g, Simbolo.CAMPANA, x + t * 0.62f, y + t * 0.24f, t * 0.62f);
    }

    /** Dibujo de la máquina con sus carretes. */
    private class Maquina extends JComponent {

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            float celda = Math.min((w - 160) / 3f, (h - 120) / 3f);
            celda = Math.max(60, celda);
            float anchoVentana = celda * 3 + 24;
            float altoVentana = celda * 3;
            float vx = (w - anchoVentana) / 2f;
            float vy = (h - altoVentana) / 2f + 22;

            // Gabinete
            float gx = vx - 60;
            float gy = vy - 76;
            float gw = anchoVentana + 120;
            float gh = altoVentana + 112;
            g.setPaint(new GradientPaint(gx, gy, new Color(0x8E2433), gx, gy + gh, new Color(0x3D0A12)));
            g.fill(new RoundRectangle2D.Float(gx, gy, gw, gh, 40, 40));
            g.setColor(Tema.ORO);
            g.setStroke(new BasicStroke(4f));
            g.draw(new RoundRectangle2D.Float(gx, gy, gw, gh, 40, 40));
            g.setFont(Tema.titulo(Math.min(34, celda * 0.32f)));
            g.setColor(Tema.ORO_CLARO);
            Dibujo.centrar(g, "★ BROADWAY 777 ★", w / 2f, gy + 38);

            // Carretes
            for (int c = 0; c < Tragamonedas.CARRETES; c++) {
                float rx = vx + c * (celda + 12);
                Shape clipAnterior = g.getClip();
                g.setClip(new RoundRectangle2D.Float(rx, vy, celda, altoVentana, 16, 16));
                g.setPaint(new GradientPaint(rx, vy, new Color(0xDCD6C6), rx, vy + altoVentana / 2, Color.WHITE, true));
                g.fillRect((int) rx, (int) vy, (int) celda + 1, (int) altoVentana + 1);
                List<Simbolo> tira = maquina.getCarrete(c);
                double pos = posicion[c];
                int base = (int) Math.floor(pos);
                for (int k = base - 2; k <= base + 2; k++) {
                    float fila = (float) (k - pos + 1);
                    float sy = vy + fila * celda;
                    Simbolo s = tira.get(Math.floorMod(k, tira.size()));
                    Dibujo.simbolo(g, s, rx + celda * 0.1f, sy + celda * 0.1f, celda * 0.8f);
                }
                // sombra de cilindro
                g.setPaint(new GradientPaint(rx, vy, new Color(0, 0, 0, 110), rx, vy + celda * 0.6f, new Color(0, 0, 0, 0)));
                g.fillRect((int) rx, (int) vy, (int) celda + 1, (int) (celda * 0.6f));
                g.setPaint(new GradientPaint(rx, vy + altoVentana - celda * 0.6f, new Color(0, 0, 0, 0), rx, vy + altoVentana, new Color(0, 0, 0, 110)));
                g.fillRect((int) rx, (int) (vy + altoVentana - celda * 0.6f), (int) celda + 1, (int) (celda * 0.6f) + 1);
                g.setClip(clipAnterior);
                g.setColor(new Color(0x3A2A10));
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Float(rx, vy, celda, altoVentana, 16, 16));
            }

            // Marcadores de líneas
            for (int l = 0; l < Tragamonedas.MAX_LINEAS; l++) {
                boolean activa = l < lineas;
                int[] filas = Tragamonedas.LINEAS[l];
                float yIzq = vy + (filas[0] + 0.5f) * celda + (l >= 3 ? (l == 3 ? -celda * 0.22f : celda * 0.22f) : 0);
                float yDer = vy + (filas[2] + 0.5f) * celda + (l >= 3 ? (l == 3 ? celda * 0.22f : -celda * 0.22f) : 0);
                marcador(g, vx - 34, yIzq, l, activa);
                marcador(g, vx + anchoVentana + 10, yDer, l, activa);
            }

            // Líneas premiadas
            if (mostrarPremios && ultimo != null) {
                for (LineaPremiada lp : ultimo.getLineasPremiadas()) {
                    int l = lp.getNumeroLinea() - 1;
                    int[] filas = Tragamonedas.LINEAS[l];
                    g.setColor(COLOR_LINEA[l]);
                    g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    java.awt.geom.Path2D.Float camino = new java.awt.geom.Path2D.Float();
                    for (int c = 0; c < 3; c++) {
                        float px = vx + c * (celda + 12) + celda / 2;
                        float py = vy + (filas[c] + 0.5f) * celda;
                        if (c == 0) {
                            camino.moveTo(vx - 8, py);
                        }
                        camino.lineTo(px, py);
                    }
                    camino.lineTo(vx + anchoVentana + 8, vy + (filas[2] + 0.5f) * celda);
                    g.draw(camino);
                    // cuántas cerezas cuentan: resaltar las celdas del premio
                    int cuenta = lp.getCombinacion().startsWith("3") ? 3 : lp.getCombinacion().startsWith("2") ? 2 : 1;
                    g.setStroke(new BasicStroke(4f));
                    for (int c = 0; c < cuenta; c++) {
                        float px = vx + c * (celda + 12);
                        float py = vy + filas[c] * celda;
                        g.draw(new RoundRectangle2D.Float(px + 4, py + 4, celda - 8, celda - 8, 14, 14));
                    }
                }
            }
            g.dispose();
        }

        private void marcador(Graphics2D g, float x, float cy, int linea, boolean activa) {
            Color c = activa ? COLOR_LINEA[linea] : new Color(0x5A3A3F);
            g.setColor(c);
            g.fillOval((int) x, (int) (cy - 12), 24, 24);
            g.setColor(activa ? new Color(0x222222) : new Color(0x8A6A6F));
            g.setFont(Tema.negrita(13));
            Dibujo.centrar(g, String.valueOf(linea + 1), x + 12, cy);
        }
    }

    /** Tabla de premios a la derecha. */
    private class TablaPagos extends JComponent {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(250, 400);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            g.setColor(new Color(7, 26, 19, 220));
            g.fillRoundRect(0, 0, w - 1, getHeight() - 1, 20, 20);
            g.setColor(Tema.ORO_OSCURO);
            g.drawRoundRect(0, 0, w - 1, getHeight() - 1, 20, 20);
            g.setColor(Tema.ORO_CLARO);
            g.setFont(Tema.titulo(19));
            Dibujo.centrar(g, "Tabla de premios", w / 2f, 24);
            g.setFont(Tema.texto(11));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.centrar(g, "veces la apuesta de la línea", w / 2f, 44);
            Simbolo[] s = Simbolo.values();
            float fila = Math.min(38, (getHeight() - 120) / 10f);
            float y = 62;
            long valor = selector.getValor();
            for (int i = s.length - 1; i >= 0; i--) {
                for (int k = 0; k < 3; k++) {
                    Dibujo.simbolo(g, s[i], 12 + k * (fila - 2), y, fila - 4);
                }
                g.setColor(Tema.CREMA);
                g.setFont(Tema.negrita(15));
                g.drawString("×" + s[i].getPremioTres(), 16 + 3 * fila, y + fila * 0.62f);
                g.setColor(Tema.TEXTO_SUAVE);
                g.setFont(Tema.texto(11));
                String p = Formato.pesos(s[i].getPremioTres() * valor);
                g.drawString(p, w - 14 - g.getFontMetrics().stringWidth(p), y + fila * 0.62f);
                y += fila;
            }
            for (int n = 2; n >= 1; n--) {
                for (int k = 0; k < n; k++) {
                    Dibujo.simbolo(g, Simbolo.CEREZA, 12 + k * (fila - 2), y, fila - 4);
                }
                int mult = n == 2 ? Simbolo.PREMIO_DOS_CEREZAS : Simbolo.PREMIO_UNA_CEREZA;
                g.setColor(Tema.CREMA);
                g.setFont(Tema.negrita(15));
                g.drawString("×" + mult, 16 + 3 * fila, y + fila * 0.62f);
                g.setColor(Tema.TEXTO_SUAVE);
                g.setFont(Tema.texto(11));
                String p = Formato.pesos(mult * valor);
                g.drawString(p, w - 14 - g.getFontMetrics().stringWidth(p), y + fila * 0.62f);
                y += fila;
            }
            g.setFont(Tema.texto(11));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.centrar(g, "Las cerezas cuentan desde la izquierda.", w / 2f, y + 14);
            g.dispose();
        }
    }
}
