package broadway.juegos.poker;

import java.util.Random;

/**
 * Forma de jugar de un rival de la computadora.
 *
 * Cada estilo (conservador, equilibrado, agresivo) es una clase distinta que
 * implementa esta interfaz. La mesa solo le pregunta "¿qué haces?" y no
 * necesita saber qué estilo tiene cada rival (polimorfismo).
 */
public interface EstrategiaRival {

    /** Nombre del estilo que se muestra en la mesa. */
    String getNombre();

    /** Decide qué hacer en su turno a partir de lo que ve en la mesa. */
    Decision decidir(Situacion situacion, Random azar);

    /**
     * Lo que el rival sabe en su turno.
     *
     * @param equidad      probabilidad estimada de ganar la mano (0 a 1)
     * @param oponentes    cuántos rivales siguen en la mano
     * @param porIgualar   fichas que le faltan para igualar la apuesta
     * @param bote         fichas que hay en el centro
     * @param apuestaActual apuesta más alta de esta ronda
     * @param minimoSubirA apuesta total mínima si decide subir
     * @param maximoSubirA apuesta total máxima (todas sus fichas)
     * @param puedeSubir   false si ya no le está permitido subir en esta ronda
     * @param ciegaGrande  valor de la ciega grande
     */
    record Situacion(double equidad, int oponentes, long porIgualar, long bote, long apuestaActual,
                     long minimoSubirA, long maximoSubirA, boolean puedeSubir, long ciegaGrande) {

        /** Equidad que tendría si todos tuvieran la misma probabilidad de ganar. */
        public double equidadJusta() {
            return 1.0 / (oponentes + 1);
        }

        /** Parte del bote final que le toca poner para seguir (las "pot odds"). */
        public double cuotaBote() {
            return porIgualar == 0 ? 0 : porIgualar / (double) (bote + porIgualar);
        }
    }

    /**
     * La acción que escoge el rival.
     *
     * @param monto solo para SUBIR o TODO: la apuesta total a la que sube
     */
    record Decision(AccionPoker accion, long monto) {
    }
}
