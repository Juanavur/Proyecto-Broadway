package broadway.vista;

import broadway.modelo.Casino;
import broadway.modelo.Trabajador;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;

/** Ventana del sistema: portada → inicio de sesión → pantalla del cargo del empleado. */
public class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Casino casino;
    private final PanelLogin login;
    private PanelTrabajo trabajo;

    public VentanaPrincipal(Casino casino) {
        super(casino.getNombre() + " · Sistema de gestión");
        this.casino = casino;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setSize(1360, 840);
        setLocationRelativeTo(null);

        login = new PanelLogin(casino, this::entrar);
        contenido.add(new PanelPortada(() -> {
            tarjetas.show(contenido, "login");
            SwingUtilities.invokeLater(login::enfocar);
        }), "portada");
        contenido.add(login, "login");
        setContentPane(contenido);
        tarjetas.show(contenido, "portada");
    }

    private void entrar(Trabajador usuario) {
        if (trabajo != null) {
            contenido.remove(trabajo);
        }
        trabajo = new PanelTrabajo(casino, usuario, () -> {
            tarjetas.show(contenido, "login");
            SwingUtilities.invokeLater(login::enfocar);
        });
        contenido.add(trabajo, "trabajo");
        tarjetas.show(contenido, "trabajo");
    }
}
