package broadway.sonido;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Reproduce los efectos con un mezclador propio: un hilo que suma todos los
 * sonidos que están sonando y los envía a la tarjeta de sonido. Así varios
 * efectos pueden sonar a la vez sin crear un reproductor por cada uno.
 */
public class SonidoActivo implements ServicioSonido {

    private static final float VOLUMEN_GENERAL = 0.8f;
    private static final int MUESTRAS_POR_BLOQUE = 512;
    private static final int MUESTRAS_DE_APAGADO = (int) (0.06 * Sintetizador.FRECUENCIA_MUESTREO);

    private final Map<Efecto, float[]> muestras = new EnumMap<>(Efecto.class);
    private final List<Voz> sonando = new ArrayList<>();
    private boolean disponible = true;
    private Thread mezclador;

    /** Un sonido dentro de la mezcla: sus muestras, por dónde va y si se repite. */
    private final class Voz implements Reproduccion {
        private final float[] datos;
        private final boolean bucle;
        private final float volumen;
        private int posicion;
        private int espera;
        private int apagado = -1;

        Voz(float[] datos, boolean bucle, float volumen, int espera) {
            this.datos = datos;
            this.bucle = bucle;
            this.volumen = volumen;
            this.espera = espera;
        }

        /** Se apaga con un desvanecimiento muy corto para que no haga "clic". */
        @Override
        public void detener() {
            synchronized (sonando) {
                if (apagado < 0) {
                    apagado = MUESTRAS_DE_APAGADO;
                }
            }
        }

        /** Suma un bloque de este sonido a la mezcla. Devuelve true cuando ya terminó. */
        boolean sumarA(float[] mezcla) {
            int i = 0;
            if (espera > 0) {
                int saltar = Math.min(espera, mezcla.length);
                espera -= saltar;
                i = saltar;
            }
            for (; i < mezcla.length; i++) {
                if (posicion >= datos.length) {
                    if (!bucle) {
                        return true;
                    }
                    posicion = 0;
                }
                float g = volumen;
                if (apagado >= 0) {
                    if (apagado == 0) {
                        return true;
                    }
                    g *= (float) apagado / MUESTRAS_DE_APAGADO;
                    apagado--;
                }
                mezcla[i] += datos[posicion++] * g;
            }
            return false;
        }
    }

    /** Genera todos los efectos y abre la salida de audio (se llama en segundo plano al iniciar). */
    void preparar() {
        for (Efecto e : Efecto.values()) {
            muestrasDe(e);
        }
        asegurarMezclador();
    }

    /** ¿Hay salida de audio en este computador? */
    boolean isDisponible() {
        return disponible;
    }

    @Override
    public void reproducir(Efecto efecto, int retrasoMs) {
        agregar(efecto, false, retrasoMs);
    }

    @Override
    public Reproduccion bucle(Efecto efecto) {
        return agregar(efecto, true, 0);
    }

    @Override
    public void detenerTodo() {
        synchronized (sonando) {
            for (Voz v : sonando) {
                v.detener();
            }
        }
    }

    private Voz agregar(Efecto efecto, boolean bucle, int retrasoMs) {
        Voz voz = new Voz(muestrasDe(efecto), bucle, efecto.getVolumen(),
                (int) (retrasoMs / 1000.0 * Sintetizador.FRECUENCIA_MUESTREO));
        asegurarMezclador();
        if (disponible) {
            synchronized (sonando) {
                sonando.add(voz);
            }
        }
        return voz;
    }

    synchronized float[] muestrasDe(Efecto e) {
        return muestras.computeIfAbsent(e, ef -> new Sintetizador().crear(ef));
    }

    private synchronized void asegurarMezclador() {
        if (mezclador != null || !disponible) {
            return;
        }
        try {
            AudioFormat formato = new AudioFormat(Sintetizador.FRECUENCIA_MUESTREO, 16, 1, true, false);
            SourceDataLine linea = AudioSystem.getSourceDataLine(formato);
            linea.open(formato, MUESTRAS_POR_BLOQUE * 2 * 6);
            linea.start();
            mezclador = new Thread(() -> mezclar(linea), "Mezclador de sonido");
            mezclador.setDaemon(true);
            mezclador.start();
        } catch (Exception | LinkageError ex) {
            disponible = false; // sin tarjeta de sonido: el juego sigue sin audio
        }
    }

    private void mezclar(SourceDataLine linea) {
        float[] mezcla = new float[MUESTRAS_POR_BLOQUE];
        byte[] bytes = new byte[MUESTRAS_POR_BLOQUE * 2];
        while (true) {
            Arrays.fill(mezcla, 0f);
            synchronized (sonando) {
                Iterator<Voz> it = sonando.iterator();
                while (it.hasNext()) {
                    if (it.next().sumarA(mezcla)) {
                        it.remove();
                    }
                }
            }
            for (int i = 0; i < MUESTRAS_POR_BLOQUE; i++) {
                float v = (float) Math.tanh(mezcla[i] * VOLUMEN_GENERAL); // limitador suave
                int muestra = (int) (v * 32767);
                bytes[2 * i] = (byte) muestra;
                bytes[2 * i + 1] = (byte) (muestra >> 8);
            }
            linea.write(bytes, 0, bytes.length);
        }
    }
}
