package broadway.juegos.poker;

import java.util.Random;

/** Juega pocas manos y casi nunca blofea: sube solo con manos muy fuertes. */
public class RivalConservador extends EstrategiaBase {

    public RivalConservador() {
        super("Conservador");
    }

    @Override
    public Decision decidir(Situacion s, Random azar) {
        double equidad = s.equidad();
        if (s.puedeSubir() && (equidad > 0.80 || equidad > s.equidadJusta() * 2.3)) {
            return subir(s, 0.6);
        }
        if (s.porIgualar() == 0) {
            if (s.puedeSubir() && equidad > s.equidadJusta() * 1.8 && azar.nextDouble() < 0.3) {
                return subir(s, 0.5);
            }
            return pasar();
        }
        if (equidad >= s.cuotaBote() + 0.10) {
            return igualar();
        }
        if (s.porIgualar() <= s.ciegaGrande() && equidad > s.cuotaBote()) {
            return igualar();
        }
        return retirarse();
    }
}
