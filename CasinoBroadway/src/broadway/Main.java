package broadway;

import broadway.vista.Tema;
import broadway.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/** Punto de entrada del Casino Broadway. Ejecuta esta clase para jugar. */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (Exception ignorado) {
                // Si falla, se usa el aspecto por defecto.
            }
            Tema.aplicarGlobal();
            broadway.sonido.Sonidos.iniciar();
            VentanaPrincipal ventana = new VentanaPrincipal(new Casino());
            ventana.setVisible(true);
        });
    }
}
