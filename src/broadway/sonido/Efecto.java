package broadway.sonido;

/** Sonidos del casino. Todos se generan con código, sin archivos de audio. */
public enum Efecto {
    CLIC(0.22f),
    FICHA(0.55f),
    FICHAS(0.55f),
    CARTA(0.5f),
    BARAJAR(0.45f),
    RODILLOS(0.32f),
    RODILLO_PARA(0.55f),
    GANAR(0.6f),
    GANAR_GRANDE(0.65f),
    PERDER(0.35f),
    RULETA(0.6f),
    CORNETA(0.5f),
    GALOPE(0.42f),
    MULTITUD(0.45f),
    CAJA(0.55f),
    GOLPE(0.55f),
    ERROR(0.3f);

    /** Volumen relativo del efecto (todos se generan con el mismo pico). */
    private final float volumen;

    Efecto(float volumen) {
        this.volumen = volumen;
    }

    public float getVolumen() {
        return volumen;
    }
}
