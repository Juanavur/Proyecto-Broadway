package broadway.juegos.poker;

import java.util.Random;

/** Presiona todo el tiempo: sube mucho, blofea seguido y paga con manos dudosas. */
public class RivalAgresivo extends EstrategiaBase {

    public RivalAgresivo() {
        super("Agresivo");
    }

    @Override
    public Decision decidir(Situacion s, Random azar) {
        double equidad = s.equidad();
        double r = azar.nextDouble();
        if (s.puedeSubir() && (equidad > 0.70 || (equidad > s.equidadJusta() * 1.5 && r < 0.75))) {
            if (equidad > 0.85 && azar.nextDouble() < 0.4) {
                return todo(s);
            }
            return subir(s, 0.75 + azar.nextDouble() * 0.45);
        }
        if (s.porIgualar() == 0) {
            if (s.puedeSubir() && (equidad > s.equidadJusta() * 1.1 || azar.nextDouble() < 0.2)) {
                return subir(s, 0.6); // apuesta o farol
            }
            return pasar();
        }
        if (equidad >= s.cuotaBote() - 0.02) {
            if (s.puedeSubir() && r < 0.25) {
                return subir(s, 0.8);
            }
            return igualar();
        }
        return azar.nextDouble() < 0.08 ? igualar() : retirarse();
    }
}
