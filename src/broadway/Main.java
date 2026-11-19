package broadway;

import broadway.vista.Tema;
import broadway.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/** Punto de entrada del sistema del Casino Broadway. */
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
            new VentanaPrincipal(DatosIniciales.crear()).setVisible(true);
        });
    }
}
