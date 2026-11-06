package broadway.juegos.poker;

import java.util.Random;

/** Juega según las probabilidades: paga cuando le conviene y sube con buenas manos. */
public class RivalEquilibrado extends EstrategiaBase {

    public RivalEquilibrado() {
        super("Equilibrado");
    }

    @Override
    public Decision decidir(Situacion s, Random azar) {
        double equidad = s.equidad();
        double r = azar.nextDouble();
        if (s.puedeSubir() && (equidad > 0.80 || (equidad > s.equidadJusta() * 2.0 && r < 0.5))) {
            if (equidad > 0.90 && azar.nextDouble() < 0.3) {
                return todo(s);
            }
            return subir(s, 0.6 + azar.nextDouble() * 0.5);
        }
        if (s.porIgualar() == 0) {
            if (s.puedeSubir() && equidad > s.equidadJusta() * 1.4 && r < 0.5) {
                return subir(s, 0.5);
            }
            if (s.puedeSubir() && azar.nextDouble() < 0.04) {
                return subir(s, 0.5); // farol de vez en cuando
            }
            return pasar();
        }
        if (equidad >= s.cuotaBote() + 0.05) {
            if (s.puedeSubir() && equidad > s.equidadJusta() * 1.7 && r < 0.25) {
                return subir(s, 0.75);
            }
            return igualar();
        }
        if (s.porIgualar() <= s.ciegaGrande() && equidad > s.cuotaBote() * 0.7) {
            return igualar();
        }
        return azar.nextDouble() < 0.03 ? igualar() : retirarse();
    }
}
