package broadway.vista;

import broadway.simulacion.ClienteAleatorio;
import broadway.simulacion.modelo.Formato;
import broadway.simulacion.modelo.Jugador;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Random;

/**
 * Crea un cliente al azar y muestra su ficha antes de entrar al juego.
 * "Entrar al casino" esconde el sistema de gestión y abre el juego como ese cliente;
 * al terminar la simulación se vuelve a la pantalla de inicio de sesión.
 */
public class DialogoSimulacion extends JDialog {

    private final Random azar = new Random();
    private final FichaCliente ficha = new FichaCliente();
    private Jugador cliente;

    public DialogoSimulacion(Window dueño) {
        super(dueño, "Simular un cliente", ModalityType.APPLICATION_MODAL);
        JPanel raiz = Componentes.tarjeta(new BorderLayout(0, 16));
        raiz.setBorder(Tema.margen(22, 26, 22, 26));
        raiz.add(Componentes.titulo("Cliente simulado"), BorderLayout.NORTH);
        raiz.add(ficha, BorderLayout.CENTER);

        BotonCasino otro = new BotonCasino("Otro cliente");
        BotonCasino cancelar = new BotonCasino("Cancelar");
        BotonCasino entrar = new BotonCasino("Entrar al casino", BotonCasino.Estilo.PRIMARIO);
        otro.addActionListener(e -> nuevoCliente());
        cancelar.addActionListener(e -> dispose());
        entrar.addActionListener(e -> {
            dispose();
            dueño.setVisible(false);
            broadway.simulacion.vista.VentanaPrincipal.abrirSimulacion(cliente, () -> {
                Tema.aplicarGlobal();
                dueño.setVisible(true);
                dueño.toFront();
            });
        });
        JPanel botones = new JPanel(new GridLayout(1, 3, 10, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(otro);
        botones.add(entrar);
        raiz.add(botones, BorderLayout.SOUTH);

        JPanel fondo = new PanelFieltro(new BorderLayout());
        fondo.setBorder(Tema.margen(14, 14, 14, 14));
        fondo.add(raiz);
        setContentPane(fondo);
        nuevoCliente();
        pack();
        setResizable(false);
        setLocationRelativeTo(dueño);
    }

    private void nuevoCliente() {
        cliente = ClienteAleatorio.generar(azar);
        ficha.repaint();
    }

    /** Tarjeta con los datos del cliente generado. */
    private class FichaCliente extends JComponent {

        FichaCliente() {
            setPreferredSize(new Dimension(520, 230));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            Color nivel = broadway.simulacion.vista.Tema.colorNivel(cliente.getNivel());

            // Avatar con iniciales
            int d = 120;
            int ay = (getHeight() - d) / 2;
            g.setColor(Tema.VINO);
            g.fillOval(0, ay, d, d);
            g.setColor(nivel);
            g.setStroke(new BasicStroke(5f));
            g.drawOval(3, ay + 3, d - 6, d - 6);
            String[] partes = cliente.getNombre().split(" ");
            String iniciales = "" + partes[0].charAt(0) + partes[partes.length - 2].charAt(0);
            g.setColor(Tema.CREMA);
            g.setFont(Tema.titulo(40));
            g.drawString(iniciales, (d - g.getFontMetrics().stringWidth(iniciales)) / 2, ay + d / 2 + 14);

            int x = d + 28;
            int y = 30;
            g.setColor(Tema.CREMA);
            g.setFont(Tema.titulo(24));
            g.drawString(cliente.getNombre(), x, y);
            y += 40;
            fila(g, "Documento", cliente.getDocumento(), x, y);
            fila(g, "Edad", cliente.getEdad() + " años", x, y += 32);
            fila(g, "Dinero en efectivo", Formato.pesos(cliente.getDinero()), x, y += 32);

            y += 26;
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.texto(15));
            g.drawString("Nivel de fidelidad", x, y + 22);
            String texto = cliente.getNivel().getNombre() + " · " + cliente.getNivel().getPorcentajeCashback() + " % cashback";
            g.setFont(Tema.negrita(15));
            int ancho = g.getFontMetrics().stringWidth(texto) + 28;
            int px = x + 160;
            g.setColor(nivel);
            g.fillRoundRect(px, y, ancho, 32, 32, 32);
            g.setColor(new Color(0x1A1A1A));
            g.drawString(texto, px + 14, y + 22);
            g.dispose();
        }

        private void fila(Graphics2D g, String etiqueta, String valor, int x, int y) {
            g.setColor(Tema.TEXTO_SUAVE);
            g.setFont(Tema.texto(15));
            g.drawString(etiqueta, x, y);
            g.setColor(Tema.CREMA);
            g.setFont(Tema.negrita(17));
            g.drawString(valor, x + 160, y);
        }
    }
}
