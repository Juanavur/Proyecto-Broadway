package ui;

import modelos.Casino;
import modelos.Trabajador;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.function.Consumer;

/** Inicio de sesión de los empleados (Trabajador.autenticacion). */
public class PanelLogin extends PanelFieltro {

    private final JTextField usuario = Componentes.campo(18);
    private final JPasswordField contraseña = new JPasswordField(18);
    private final JLabel error = Tema.etiqueta(" ", Tema.texto(14), Tema.ERROR);

    public PanelLogin(Casino casino, Consumer<Trabajador> alEntrar) {
        super(new GridBagLayout());
        contraseña.setFont(Tema.texto(15));
        contraseña.setBackground(Tema.PANEL_CLARO);
        contraseña.setForeground(Tema.CREMA);
        contraseña.setCaretColor(Tema.ORO_CLARO);
        contraseña.setBorder(usuario.getBorder());

        JPanel tarjeta = Componentes.tarjeta(new BorderLayout(0, 14));
        tarjeta.setBorder(Tema.margen(26, 34, 26, 34));
        tarjeta.add(new Logo(), BorderLayout.NORTH);

        JPanel centro = Componentes.bloque(10);
        JLabel titulo = Componentes.titulo("Ingreso de empleados");
        titulo.setHorizontalAlignment(JLabel.CENTER);
        centro.add(titulo, BorderLayout.NORTH);
        centro.add(Componentes.formulario("Usuario", usuario, "Contraseña", contraseña), BorderLayout.CENTER);
        centro.add(error, BorderLayout.SOUTH);
        tarjeta.add(centro, BorderLayout.CENTER);

        BotonCasino entrar = new BotonCasino("Entrar", BotonCasino.Estilo.PRIMARIO);
        entrar.addActionListener(e -> {
            Trabajador t = casino.iniciarSesion(usuario.getText().trim(), new String(contraseña.getPassword()));
            if (t == null) {
                error.setText("Usuario o contraseña incorrectos.");
                return;
            }
            error.setText(" ");
            usuario.setText("");
            contraseña.setText("");
            alEntrar.accept(t);
        });
        usuario.addActionListener(e -> contraseña.requestFocusInWindow());
        contraseña.addActionListener(e -> entrar.doClick());

        JPanel demo = new JPanel(new GridLayout(0, 2, 6, 6));
        demo.setOpaque(false);
        String[][] cuentas = {{"admin", "admin123"}, {"cajero", "cajero123"}, {"crupier", "crupier123"},
                {"tecnico", "tecnico123"}};
        String[] cargos = {"Administrador", "Cajero", "Crupier", "Técnico"};
        for (int i = 0; i < cuentas.length; i++) {
            String[] c = cuentas[i];
            BotonCasino b = new BotonCasino(cargos[i] + " · " + c[0]);
            b.setFont(Tema.texto(13));
            b.addActionListener(e -> {
                usuario.setText(c[0]);
                contraseña.setText(c[1]);
                contraseña.requestFocusInWindow();
            });
            demo.add(b);
        }
        JPanel pie = Componentes.bloque(8);
        pie.add(entrar, BorderLayout.NORTH);
        pie.add(Componentes.nota("Cuentas de prueba (clic para llenar):"), BorderLayout.CENTER);
        pie.add(demo, BorderLayout.SOUTH);
        tarjeta.add(pie, BorderLayout.SOUTH);
        tarjeta.setPreferredSize(new Dimension(520, 610));
        add(tarjeta);
    }

    public void enfocar() {
        usuario.requestFocusInWindow();
    }

    /** Logo del casino escalado al ancho disponible. */
    static class Logo extends javax.swing.JComponent {

        private final BufferedImage imagen = cargar();

        Logo() {
            setPreferredSize(new Dimension(440, 150));
        }

        private static BufferedImage cargar() {
            try (InputStream in = Logo.class.getResourceAsStream("/files/logo.png")) {
                return in == null ? null : ImageIO.read(in);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (imagen == null) {
                return;
            }
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double escala = Math.min((double) getWidth() / imagen.getWidth(), (double) getHeight() / imagen.getHeight());
            int w = (int) (imagen.getWidth() * escala);
            int h = (int) (imagen.getHeight() * escala);
            g.drawImage(imagen, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            g.dispose();
        }
    }
}
