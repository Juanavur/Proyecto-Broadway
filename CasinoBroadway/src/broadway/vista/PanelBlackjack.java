package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.cartas.Carta;
import broadway.juegos.blackjack.Blackjack;
import broadway.juegos.blackjack.EstadoBlackjack;
import broadway.juegos.blackjack.ManoBlackjack;
import broadway.modelo.Formato;
import broadway.modelo.ResultadoRonda;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.List;

/** Mesa de blackjack. */
public class PanelBlackjack extends PanelJuego {

    private static final long[] FICHAS = {5_000, 10_000, 25_000, 100_000, 500_000};

    private final Blackjack juego;
    private final Mesa mesa = new Mesa();
    private final JLabel mensaje = Tema.etiqueta(" ", Tema.negrita(17), Tema.CREMA);
    private final JLabel apuestaLbl = Tema.etiqueta("", Tema.negrita(17), Tema.ORO_CLARO);
    private final CardLayout tarjetasControles = new CardLayout();
    private final JPanel controles = new JPanel(tarjetasControles);
    private final BotonCasino repartir = new BotonCasino("Repartir", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino repetir = new BotonCasino("Repetir apuesta");
    private final BotonCasino pedir = new BotonCasino("Pedir carta", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino plantarse = new BotonCasino("Plantarse");
    private final BotonCasino doblar = new BotonCasino("Doblar");
    private final BotonCasino dividir = new BotonCasino("Dividir");
    private final BotonCasino rendirse = new BotonCasino("Rendirse", BotonCasino.Estilo.PELIGRO);
    private final BotonCasino tomarSeguro = new BotonCasino("Tomar seguro", BotonCasino.Estilo.PRIMARIO);

    private long apuestaPendiente;
    private long ultimaApuesta;
    private int crupierVisibles = 2;
    private boolean revelando;
    private Timer temporizador;

    public PanelBlackjack(VentanaPrincipal ventana) {
        super(ventana, ventana.getCasino().getBlackjack());
        this.juego = ventana.getCasino().getBlackjack();

        PanelFieltro centro = new PanelFieltro(new BorderLayout());
        centro.add(mesa, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setBackground(Tema.PANEL);
        abajo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE), Tema.margen(8, 18, 10, 18)));
        mensaje.setHorizontalAlignment(JLabel.CENTER);
        mensaje.setBorder(Tema.margen(0, 0, 8, 0));
        abajo.add(mensaje, BorderLayout.NORTH);
        controles.setOpaque(false);

        // Controles para apostar
        JPanel apostar = fila();
        SelectorFichas selector = new SelectorFichas(FICHAS, 24);
        selector.setAlClic(this::sumarFicha);
        apostar.add(Tema.etiqueta("Toca una ficha para apostarla:", Tema.texto(14), Tema.TEXTO_SUAVE));
        apostar.add(selector);
        apostar.add(apuestaLbl);
        BotonCasino limpiar = new BotonCasino("Limpiar");
        limpiar.addActionListener(e -> {
            apuestaPendiente = 0;
            actualizar();
        });
        apostar.add(limpiar);
        repetir.addActionListener(e -> {
            apuestaPendiente = ultimaApuesta;
            actualizar();
        });
        apostar.add(repetir);
        repartir.setFont(Tema.negrita(18));
        repartir.addActionListener(e -> repartir());
        apostar.add(repartir);
        controles.add(apostar, "apostar");

        // Controles durante la mano
        JPanel jugar = fila();
        pedir.addActionListener(e -> {
            Sonidos.reproducir(Efecto.CARTA);
            accion(juego::pedir);
        });
        plantarse.addActionListener(e -> {
            Sonidos.reproducir(Efecto.GOLPE);
            accion(juego::plantarse);
        });
        doblar.addActionListener(e -> {
            Sonidos.reproducir(Efecto.FICHAS);
            Sonidos.reproducir(Efecto.CARTA, 300);
            accion(juego::doblar);
        });
        dividir.addActionListener(e -> {
            Sonidos.reproducir(Efecto.FICHAS);
            Sonidos.reproducir(Efecto.CARTA, 300);
            Sonidos.reproducir(Efecto.CARTA, 470);
            accion(juego::dividir);
        });
        rendirse.addActionListener(e -> accion(juego::rendirse));
        for (BotonCasino b : new BotonCasino[]{pedir, plantarse, doblar, dividir, rendirse}) {
            b.setFont(Tema.negrita(17));
            jugar.add(b);
        }
        controles.add(jugar, "jugar");

        // Seguro
        JPanel seguro = fila();
        seguro.add(Tema.etiqueta("El crupier muestra un As.", Tema.texto(15), Tema.CREMA));
        tomarSeguro.addActionListener(e -> {
            Sonidos.reproducir(Efecto.FICHA);
            accion(() -> juego.decidirSeguro(true));
        });
        BotonCasino sinSeguro = new BotonCasino("No, gracias");
        sinSeguro.addActionListener(e -> accion(() -> juego.decidirSeguro(false)));
        seguro.add(tomarSeguro);
        seguro.add(sinSeguro);
        controles.add(seguro, "seguro");

        // Mientras el crupier revela
        JPanel espera = fila();
        espera.add(Tema.etiqueta("Juega el crupier...", Tema.negrita(16), Tema.TEXTO_SUAVE));
        controles.add(espera, "espera");

        abajo.add(controles, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
        actualizar();
    }

    private static JPanel fila() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        p.setOpaque(false);
        return p;
    }

    private void sumarFicha(long valor) {
        if (juego.getEstado() == EstadoBlackjack.TURNO_JUGADOR || juego.getEstado() == EstadoBlackjack.OFRECIENDO_SEGURO) {
            return;
        }
        if (apuestaPendiente + valor > juego.getApuestaMaxima()) {
            ventana.aviso("La apuesta máxima es " + Formato.pesos(juego.getApuestaMaxima()) + ".", Tema.ERROR);
            return;
        }
        apuestaPendiente += valor;
        actualizar();
    }

    private void repartir() {
        long apuesta = apuestaPendiente;
        if (apuesta == 0) {
            ventana.aviso("Toca las fichas para armar tu apuesta (mínimo " + Formato.pesos(juego.getApuestaMinima()) + ").", Tema.ERROR);
            return;
        }
        if (intentar(() -> juego.iniciarRonda(jugador(), apuesta))) {
            Sonidos.reproducir(Efecto.FICHAS);
            for (int i = 0; i < 4; i++) {
                Sonidos.reproducir(Efecto.CARTA, 250 + i * 170);
            }
            ultimaApuesta = apuesta;
            crupierVisibles = 2;
            ventana.actualizarSaldo();
            despuesDeAccion();
        }
    }

    private void accion(Runnable r) {
        if (intentar(r)) {
            ventana.actualizarSaldo();
            despuesDeAccion();
        }
    }

    private void despuesDeAccion() {
        if (juego.getEstado() == EstadoBlackjack.RONDA_TERMINADA) {
            revelarCrupier();
        } else {
            actualizar();
        }
    }

    /** El crupier destapa su carta y saca las que necesite, una a una. */
    private void revelarCrupier() {
        revelando = true;
        crupierVisibles = 2;
        actualizar();
        Sonidos.reproducir(Efecto.CARTA, 150); // destapa la carta oculta
        temporizador = new Timer(650, e -> {
            if (crupierVisibles < juego.getCrupier().getCartas().size()) {
                crupierVisibles++;
                Sonidos.reproducir(Efecto.CARTA);
                mesa.repaint();
            } else {
                ((Timer) e.getSource()).stop();
                revelando = false;
                ResultadoRonda r = juego.getUltimoResultado();
                apuestaPendiente = Math.min(ultimaApuesta, jugador().getFichas());
                actualizar();
                mensaje.setText(juego.getMensaje() + "   " + textoNeto(r) + ".");
                mensaje.setForeground(r.getNeto() > 0 ? Tema.EXITO : r.getNeto() == 0 ? Tema.ORO_CLARO : Tema.ERROR);
                sonidoResultado(r);
                rondaTerminada(r);
            }
        });
        temporizador.setInitialDelay(500);
        temporizador.start();
    }

    private void sonidoResultado(ResultadoRonda r) {
        boolean natural = juego.getManos().size() == 1 && juego.getManos().get(0).esBlackjack();
        if (r.getNeto() > 0) {
            Sonidos.reproducir(natural ? Efecto.GANAR_GRANDE : Efecto.GANAR);
            Sonidos.reproducir(Efecto.FICHAS, 500);
        } else if (r.getNeto() < 0) {
            Sonidos.reproducir(Efecto.PERDER);
        }
    }

    private void actualizar() {
        EstadoBlackjack e = juego.getEstado();
        if (revelando) {
            tarjetasControles.show(controles, "espera");
        } else if (e == EstadoBlackjack.TURNO_JUGADOR) {
            tarjetasControles.show(controles, "jugar");
            doblar.setEnabled(juego.puedeDoblar());
            dividir.setEnabled(juego.puedeDividir());
            rendirse.setEnabled(juego.puedeRendirse());
        } else if (e == EstadoBlackjack.OFRECIENDO_SEGURO) {
            tarjetasControles.show(controles, "seguro");
            tomarSeguro.setText("Tomar seguro (" + Formato.pesos(juego.getManos().get(0).getApuesta() / 2) + ")");
        } else {
            tarjetasControles.show(controles, "apostar");
            repetir.setEnabled(ultimaApuesta > 0);
        }
        apuestaLbl.setText("Apuesta: " + Formato.pesos(apuestaPendiente));
        if (e != EstadoBlackjack.RONDA_TERMINADA || revelando) {
            mensaje.setText(juego.getMensaje());
            mensaje.setForeground(Tema.CREMA);
        }
        if (juego.isZapatoRecienBarajado() && e != EstadoBlackjack.RONDA_TERMINADA) {
            mensaje.setText("Se barajó el zapato de 6 barajas.  " + juego.getMensaje());
        }
        mesa.repaint();
    }

    @Override
    public void alEntrar() {
        if (apuestaPendiente == 0 && jugador() != null) {
            apuestaPendiente = Math.min(10_000, jugador().getFichas() / 5_000 * 5_000);
        }
        actualizar();
    }

    @Override
    public boolean alSalir() {
        EstadoBlackjack e = juego.getEstado();
        if (revelando || e == EstadoBlackjack.TURNO_JUGADOR || e == EstadoBlackjack.OFRECIENDO_SEGURO) {
            ventana.aviso("Termina la mano antes de salir de la mesa.", Tema.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void prepararCierre() {
        // Si se cierra la ventana a mitad de mano, se planta todo para no perder fichas en el aire.
        if (juego.getEstado() == EstadoBlackjack.OFRECIENDO_SEGURO) {
            juego.decidirSeguro(false);
        }
        while (juego.getEstado() == EstadoBlackjack.TURNO_JUGADOR) {
            juego.plantarse();
        }
        if (temporizador != null) {
            temporizador.stop();
        }
    }

    @Override
    public void dibujarIcono(Graphics2D g, float x, float y, float t) {
        float w = t * 0.48f;
        float h = w * 1.4f;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(-0.25, x + t * 0.35f, y + t * 0.8f);
        Dibujo.carta(g2, new Carta(broadway.cartas.Valor.AS, broadway.cartas.Palo.PICAS), Math.round(x + t * 0.12f), Math.round(y + t * 0.1f), Math.round(w), Math.round(h));
        g2.dispose();
        g2 = (Graphics2D) g.create();
        g2.rotate(0.2, x + t * 0.6f, y + t * 0.8f);
        Dibujo.carta(g2, new Carta(broadway.cartas.Valor.REY, broadway.cartas.Palo.CORAZONES), Math.round(x + t * 0.42f), Math.round(y + t * 0.08f), Math.round(w), Math.round(h));
        g2.dispose();
    }

    /** Dibujo de la mesa con las cartas. */
    private class Mesa extends JComponent {

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int cw = (int) Math.max(62, Math.min(110, Math.min(w / 11.0, h / 4.6)));
            int ch = (int) (cw * 1.42);

            // Leyendas del paño
            g.setColor(new Color(255, 255, 255, 40));
            g.setStroke(new BasicStroke(2f));
            float radio = w * 0.42f;
            g.draw(new Arc2D.Float(w / 2f - radio, h * 0.18f - radio * 0.55f, radio * 2, radio * 1.6f, 200, 140, Arc2D.OPEN));
            g.setColor(new Color(0xF2DC8F));
            g.setFont(Tema.titulo(Math.max(20, cw * 0.30f)));
            Dibujo.centrar(g, "BLACKJACK PAGA 3 A 2", w / 2f, h * 0.47f);
            g.setColor(new Color(255, 255, 255, 150));
            g.setFont(Tema.negrita(Math.max(12, cw * 0.15f)));
            Dibujo.centrar(g, "EL CRUPIER PIDE HASTA 16 Y SE PLANTA EN 17  ·  EL SEGURO PAGA 2 A 1", w / 2f, h * 0.53f);

            // Zapato
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRoundRect(w - 150, 16, 130, 46, 14, 14);
            g.setColor(Tema.CREMA);
            g.setFont(Tema.texto(12));
            g.drawString("Zapato: 6 barajas", w - 140, 34);
            g.drawString(juego.getCartasEnZapato() + " cartas", w - 140, 52);

            // Crupier
            EstadoBlackjack estado = juego.getEstado();
            ManoBlackjack crupier = juego.getCrupier();
            List<Carta> cc = crupier.getCartas();
            if (!cc.isEmpty()) {
                int mostrar = juego.isCartaOcultaVisible() ? Math.min(crupierVisibles, cc.size()) : 2;
                int paso = (int) (cw * 0.62);
                int total = cw + paso * (mostrar - 1);
                int x0 = (w - total) / 2;
                int y0 = (int) (h * 0.07);
                for (int i = 0; i < mostrar; i++) {
                    boolean oculta = i == 1 && !juego.isCartaOcultaVisible();
                    Dibujo.carta(g, oculta ? null : cc.get(i), x0 + i * paso, y0, cw, ch);
                }
                String valor;
                if (!juego.isCartaOcultaVisible()) {
                    valor = "Crupier: " + cc.get(0).getValor().getPuntosBlackjack();
                } else {
                    int suma = valorParcial(cc.subList(0, mostrar));
                    valor = "Crupier: " + (mostrar == cc.size() ? crupier.getDescripcionValor() : String.valueOf(suma));
                }
                etiqueta(g, valor, w / 2f, y0 + ch + 22, Tema.CREMA);
            } else {
                g.setColor(new Color(255, 255, 255, 120));
                g.setFont(Tema.negrita(16));
                Dibujo.centrar(g, "CRUPIER", w / 2f, h * 0.18f);
            }

            // Manos del jugador
            List<ManoBlackjack> manos = juego.getManos();
            float yManos = h * 0.62f;
            if (manos.isEmpty() || estado == EstadoBlackjack.ESPERANDO_APUESTA) {
                circuloApuesta(g, w / 2f, yManos + ch * 0.45f, cw * 0.62f, apuestaPendiente);
            } else {
                int n = manos.size();
                float anchoMano = Math.min(w / (float) n, cw * 3.2f);
                float inicioX = (w - anchoMano * n) / 2f;
                boolean terminada = estado == EstadoBlackjack.RONDA_TERMINADA && !revelando;
                for (int i = 0; i < n; i++) {
                    ManoBlackjack m = manos.get(i);
                    float centro = inicioX + anchoMano * (i + 0.5f);
                    List<Carta> cs = m.getCartas();
                    int paso = (int) (cw * 0.44);
                    int total = cw + paso * (cs.size() - 1);
                    int x0 = (int) (centro - total / 2f);
                    int y0 = (int) yManos;
                    boolean actual = estado == EstadoBlackjack.TURNO_JUGADOR && i == juego.getIndiceManoActual();
                    if (actual) {
                        g.setColor(new Color(0xF2DC8F));
                        g.setStroke(new BasicStroke(3f));
                        g.drawRoundRect(x0 - 8, y0 - 8 - (cs.size() - 1) * 6, total + 16, ch + 16 + (cs.size() - 1) * 6, 16, 16);
                    }
                    for (int k = 0; k < cs.size(); k++) {
                        Dibujo.carta(g, cs.get(k), x0 + k * paso, y0 - k * 6, cw, ch);
                    }
                    Dibujo.ficha(g, m.getApuesta(), centro - cw * 0.85f - 10, y0 + ch * 0.75f, cw * 0.24f, false);
                    String texto = m.getDescripcionValor() + "  ·  " + Formato.pesos(m.getApuesta()) + (m.isDoblada() ? " (doblada)" : "");
                    etiqueta(g, texto, centro, y0 + ch + 22, Tema.CREMA);
                    if (terminada && !m.getResultado().isEmpty()) {
                        Color c = m.getResultado().startsWith("Gana") || m.getResultado().startsWith("¡") ? Tema.EXITO
                                : m.getResultado().startsWith("Empate") || m.getResultado().startsWith("Se rindió") ? Tema.ORO_CLARO : Tema.ERROR;
                        etiqueta(g, m.getResultado(), centro, y0 + ch + 50, c);
                    }
                }
                if (juego.getSeguro() > 0) {
                    etiqueta(g, "Seguro: " + Formato.pesos(juego.getSeguro()), w * 0.15f, h * 0.47f, Tema.ORO_CLARO);
                }
            }
            g.dispose();
        }

        private int valorParcial(List<Carta> cartas) {
            int total = 0;
            int ases = 0;
            for (Carta c : cartas) {
                total += c.getValor().getPuntosBlackjack();
                if (c.getValor() == broadway.cartas.Valor.AS) {
                    ases++;
                }
            }
            while (total > 21 && ases-- > 0) {
                total -= 10;
            }
            return total;
        }

        private void etiqueta(Graphics2D g, String texto, float cx, float cy, Color color) {
            g.setFont(Tema.negrita(15));
            int tw = g.getFontMetrics().stringWidth(texto) + 22;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect((int) (cx - tw / 2f), (int) (cy - 13), tw, 26, 26, 26);
            g.setColor(color);
            Dibujo.centrar(g, texto, cx, cy);
        }

        private void circuloApuesta(Graphics2D g, float cx, float cy, float r, long apuesta) {
            g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r, new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, 20), new Color(255, 255, 255, 5)}));
            g.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
            g.setColor(new Color(0xF2DC8F));
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
            if (apuesta > 0) {
                long resto = apuesta;
                int pila = 0;
                for (int i = FICHAS.length - 1; i >= 0 && pila < 8; i--) {
                    while (resto >= FICHAS[i] && pila < 8) {
                        Dibujo.ficha(g, FICHAS[i], cx, cy + r * 0.25f - pila * 5, r * 0.55f, false);
                        resto -= FICHAS[i];
                        pila++;
                    }
                }
                etiqueta(g, Formato.pesos(apuesta), cx, cy + r + 20, Tema.ORO_CLARO);
            } else {
                g.setColor(new Color(255, 255, 255, 140));
                g.setFont(Tema.negrita(14));
                Dibujo.centrar(g, "APUESTA", cx, cy);
            }
        }
    }
}
