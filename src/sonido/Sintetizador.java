package broadway.sonido;

import java.util.Random;

/**
 * Genera las muestras de audio de cada efecto (44.100 muestras por segundo, mono,
 * valores entre -1 y 1) combinando ondas, ruido y envolventes, como un sintetizador.
 */
final class Sintetizador {

    static final float FRECUENCIA_MUESTREO = 44_100f;
    private static final double DOS_PI = Math.PI * 2;

    private final Random azar = new Random(7);

    float[] crear(Efecto e) {
        float[] s;
        switch (e) {
            case CLIC: s = clic(); break;
            case FICHA: s = ficha(); break;
            case FICHAS: s = fichas(); break;
            case CARTA: s = carta(); break;
            case BARAJAR: s = barajar(); break;
            case RODILLOS: s = rodillos(); break;
            case RODILLO_PARA: s = rodilloPara(); break;
            case GANAR: s = ganar(); break;
            case GANAR_GRANDE: s = ganarGrande(); break;
            case PERDER: s = perder(); break;
            case RULETA: s = ruleta(); break;
            case CORNETA: s = corneta(); break;
            case GALOPE: s = galope(); break;
            case MULTITUD: s = multitud(); break;
            case CAJA: s = caja(); break;
            case GOLPE: s = golpe(); break;
            default: s = error(); break;
        }
        normalizar(s, 0.9f);
        return s;
    }

    // ================================================================ efectos

    private float[] clic() {
        float[] s = buffer(0.04);
        seno(s, 0, 1800, 0.03, 0.25, 0.006);
        ruido(s, 0, 0.01, 0.15, 0.002, 6000, true);
        return s;
    }

    /** Golpe de una ficha de arcilla contra otra. */
    private void golpeFicha(float[] s, double t, double tono, double amp) {
        ruido(s, t, 0.02, 0.55 * amp, 0.004, 9000, true);
        seno(s, t, 3300 * tono, 0.08, 0.28 * amp, 0.020);
        seno(s, t, 5200 * tono, 0.06, 0.16 * amp, 0.012);
        seno(s, t, 1450 * tono, 0.10, 0.14 * amp, 0.030);
    }

    private float[] ficha() {
        float[] s = buffer(0.16);
        golpeFicha(s, 0, 1.0, 1.0);
        golpeFicha(s, 0.03, 1.06, 0.55);
        return s;
    }

    private float[] fichas() {
        float[] s = buffer(0.6);
        double t = 0;
        for (int i = 0; i < 7; i++) {
            golpeFicha(s, t, 0.9 + azar.nextDouble() * 0.2, 1.0 - i * 0.1);
            t += 0.03 + azar.nextDouble() * 0.045;
        }
        return s;
    }

    private float[] carta() {
        float[] s = buffer(0.18);
        int n = s.length;
        double bajo = 0;
        double alto = 0;
        double aBajo = coef(600);
        double aAlto = coef(4000);
        for (int i = 0; i < n; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double env = t < 0.015 ? t / 0.015 : Math.exp(-(t - 0.015) / 0.045);
            double r = azar.nextDouble() * 2 - 1;
            alto += aAlto * (r - alto);
            bajo += aBajo * (alto - bajo);
            s[i] += (float) ((alto - bajo) * env * 0.9);
        }
        ruido(s, 0.11, 0.01, 0.25, 0.002, 7000, true);
        return s;
    }

    private float[] barajar() {
        float[] s = buffer(0.95);
        double t = 0;
        for (int i = 0; i < 48; i++) {
            ruido(s, t, 0.01, 0.28, 0.0025, 7000, true);
            seno(s, t, 2600 + azar.nextInt(800), 0.01, 0.05, 0.003);
            t += 0.024 - i * 0.00025 + azar.nextDouble() * 0.004;
        }
        ruido(s, t + 0.04, 0.12, 0.25, 0.04, 2500, true);
        return s;
    }

    /** Carretes girando: zumbido y tic-tac regular. Se repite sin cortes. */
    private float[] rodillos() {
        double largo = 1.0;
        double cruce = 0.1;
        float[] s = buffer(largo + cruce);
        double a = coef(900);
        double y = 0;
        for (int i = 0; i < s.length; i++) {
            y += a * ((azar.nextDouble() * 2 - 1) - y);
            s[i] += (float) (y * 0.35);
        }
        for (int k = 0; k < 18; k++) {
            double t = k * largo / 16;
            if (t < s.length / FRECUENCIA_MUESTREO) {
                seno(s, t, 2400, 0.02, 0.22, 0.004);
                ruido(s, t, 0.008, 0.12, 0.002, 8000, true);
            }
        }
        return enBucle(s, largo, cruce);
    }

    private float[] rodilloPara() {
        float[] s = buffer(0.3);
        seno(s, 0, 110, 0.25, 0.55, 0.06);
        seno(s, 0, 220, 0.15, 0.22, 0.03);
        ruido(s, 0, 0.02, 0.35, 0.003, 5000, true);
        seno(s, 0, 1800, 0.12, 0.08, 0.05);
        return s;
    }

    private float[] ganar() {
        float[] s = buffer(1.2);
        double[] notas = {1046.5, 1318.5, 1568.0, 2093.0};
        for (int i = 0; i < notas.length; i++) {
            campana(s, i * 0.09, notas[i], 0.7, 0.22);
        }
        campana(s, 0.38, 2093.0, 0.8, 0.16);
        campana(s, 0.38, 2637.0, 0.8, 0.12);
        return s;
    }

    private float[] ganarGrande() {
        float[] s = buffer(2.8);
        double[] escala = {523.3, 659.3, 784.0, 1046.5, 1318.5, 1568.0, 2093.0, 2637.0};
        for (int vuelta = 0; vuelta < 2; vuelta++) {
            for (int i = 0; i < escala.length; i++) {
                campana(s, vuelta * 0.36 + i * 0.045, escala[i], 0.35, 0.13);
            }
        }
        double[] acorde = {1046.5, 1318.5, 1568.0, 2093.0};
        for (double f : acorde) {
            campana(s, 0.78, f, 1.8, 0.13);
        }
        for (int i = 0; i < 45; i++) {
            double t = 0.7 + azar.nextDouble() * 1.8;
            campana(s, t, 3000 + azar.nextDouble() * 2500, 0.15, 0.05);
        }
        return s;
    }

    private float[] perder() {
        float[] s = buffer(0.65);
        triangulo(s, 0.0, 330, 0.18, 0.16);
        triangulo(s, 0.2, 262, 0.38, 0.16);
        return pasaBajos(s, 1800);
    }

    /** La bola gira, se frena y rebota hasta caer en una casilla (dura lo mismo que la animación). */
    private float[] ruleta() {
        float[] s = buffer(5.4);
        double fin = 4.6;
        double a = coef(320);
        double y = 0;
        for (int i = 0; i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            y += a * ((azar.nextDouble() * 2 - 1) - y);
            double env = t < 0.2 ? t / 0.2 : Math.max(0, 1 - t / (fin + 0.2));
            s[i] += (float) (y * 0.55 * env);
        }
        double t = 0;
        while (t < fin) {
            double p = t / fin;
            seno(s, t, 2800, 0.02, 0.10 * (0.6 + p), 0.005);
            ruido(s, t, 0.006, 0.10 * (0.6 + p), 0.0015, 8000, true);
            t += 0.04 + 0.30 * Math.pow(p, 2.2);
        }
        double[] rebotes = {4.62, 4.80, 4.91, 4.98, 5.02};
        double[] fuerza = {0.40, 0.30, 0.20, 0.12, 0.07};
        for (int i = 0; i < rebotes.length; i++) {
            seno(s, rebotes[i], 1600 + i * 90, 0.06, fuerza[i], 0.012);
            ruido(s, rebotes[i], 0.01, fuerza[i] * 0.8, 0.003, 7000, true);
        }
        return s;
    }

    /** Llamado de corneta antes de la carrera. */
    private float[] corneta() {
        double[][] notas = { // frecuencia, inicio, duración
                {392.0, 0.00, 0.13}, {523.3, 0.15, 0.13}, {659.3, 0.30, 0.13}, {784.0, 0.45, 0.30},
                {659.3, 0.78, 0.13}, {784.0, 0.93, 0.13}, {1046.5, 1.08, 0.50},
                {784.0, 1.64, 0.12}, {1046.5, 1.79, 0.55}};
        float[] s = buffer(2.5);
        for (double[] n : notas) {
            metal(s, n[1], n[0], n[2], 0.16);
        }
        return pasaBajos(s, 5000);
    }

    /** Galope de varios caballos. Se repite sin cortes. */
    private float[] galope() {
        double largo = 0.48;
        float[] s = buffer(largo);
        double[] golpes = {0.0, 0.085, 0.17};
        for (int grupo = 0; grupo < 3; grupo++) {
            double desfase = grupo * 0.155;
            double fuerza = grupo == 0 ? 1.0 : 0.65;
            for (double g : golpes) {
                pezuna(s, (g + desfase) % largo, fuerza * (0.85 + azar.nextDouble() * 0.3), 0.9 + grupo * 0.08);
            }
        }
        return s;
    }

    private void pezuna(float[] s, double t, double fuerza, double tono) {
        // se escribe "dando la vuelta" para que el bucle no tenga cortes
        int inicio = (int) (t * FRECUENCIA_MUESTREO);
        int largo = (int) (0.12 * FRECUENCIA_MUESTREO);
        double y = 0;
        double a = coef(700);
        for (int i = 0; i < largo; i++) {
            double tt = i / FRECUENCIA_MUESTREO;
            double golpe = Math.sin(DOS_PI * 85 * tono * tt) * Math.exp(-tt / 0.028) * 0.45;
            y += a * ((azar.nextDouble() * 2 - 1) - y);
            double tierra = y * Math.exp(-tt / 0.014) * 0.55;
            int k = (inicio + i) % s.length;
            s[k] += (float) ((golpe + tierra) * fuerza);
        }
    }

    /** Público celebrando la llegada: muchas voces gritando "¡aaah!" más un murmullo de fondo. */
    private float[] multitud() {
        float[] s = buffer(3.0);
        float[] voces = buffer(3.0);
        for (int v = 0; v < 46; v++) {
            double f0 = 120 + azar.nextDouble() * 230;
            double inicio = azar.nextDouble() * 0.45;
            double dur = 1.1 + azar.nextDouble() * 1.2;
            double subida = 0.08 + azar.nextDouble() * 0.15;
            double fase = 0;
            double vib = 4 + azar.nextDouble() * 3;
            int a = (int) (inicio * FRECUENCIA_MUESTREO);
            int n = (int) (dur * FRECUENCIA_MUESTREO);
            double amp = 0.5 + azar.nextDouble() * 0.5;
            for (int i = 0; i < n && a + i < voces.length; i++) {
                double t = i / FRECUENCIA_MUESTREO;
                double tono = f0 * (1 + 0.12 * Math.min(1, t / 0.3)) * (1 + 0.012 * Math.sin(DOS_PI * vib * t));
                fase += tono / FRECUENCIA_MUESTREO;
                double diente = 2 * (fase % 1.0) - 1;
                double env = Math.min(1, t / subida) * Math.min(1, (dur - t) / 0.5);
                voces[a + i] += (float) (diente * amp * env);
            }
        }
        // Formantes de la vocal "a": así la suma suena a gente y no a zumbido.
        float[] f1 = pasaBanda(voces.clone(), 750, 4);
        float[] f2 = pasaBanda(voces.clone(), 1250, 5);
        float[] f3 = pasaBanda(voces, 2600, 6);
        double y = 0;
        double a = coef(1800);
        for (int i = 0; i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            y += a * ((azar.nextDouble() * 2 - 1) - y);
            double env = Math.min(1, t / 0.3) * (t < 1.6 ? 1 : Math.exp(-(t - 1.6) / 0.5));
            s[i] = (float) (f1[i] * 1.0 + f2[i] * 0.6 + f3[i] * 0.25 + y * 0.25 * env);
        }
        for (int k = 0; k < 3; k++) {
            silbido(s, 0.3 + k * 0.5 + azar.nextDouble() * 0.2, 1900 + azar.nextInt(400), 0.35, 0.06);
        }
        return s;
    }

    /** Filtro pasa banda resonante (biquad) centrado en {@code centro}. */
    private static float[] pasaBanda(float[] x, double centro, double q) {
        double w0 = DOS_PI * centro / FRECUENCIA_MUESTREO;
        double alfa = Math.sin(w0) / (2 * q);
        double b0 = alfa;
        double a0 = 1 + alfa;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - alfa;
        double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        for (int i = 0; i < x.length; i++) {
            double entrada = x[i];
            double salida = (b0 * entrada - b0 * x2 - a1 * y1 - a2 * y2) / a0;
            x2 = x1;
            x1 = entrada;
            y2 = y1;
            y1 = salida;
            x[i] = (float) salida;
        }
        return x;
    }

    /** Caja registradora: golpe del cajón, campanita y monedas. */
    private float[] caja() {
        float[] s = buffer(1.3);
        ruido(s, 0, 0.06, 0.45, 0.03, 500, false);
        seno(s, 0, 140, 0.15, 0.32, 0.05);
        ruido(s, 0.05, 0.22, 0.08, 0.08, 1500, false);
        campana(s, 0.12, 2637, 1.0, 0.30);
        campana(s, 0.12, 3520, 0.9, 0.16);
        for (int i = 0; i < 9; i++) {
            campana(s, 0.28 + azar.nextDouble() * 0.4, 4000 + azar.nextDouble() * 2200, 0.12, 0.05);
        }
        return s;
    }

    /** Dos golpecitos en la mesa (pasar en póker). */
    private float[] golpe() {
        float[] s = buffer(0.25);
        for (double t : new double[]{0, 0.11}) {
            seno(s, t, 200, 0.1, 0.45, 0.025);
            ruido(s, t, 0.02, 0.30, 0.008, 1200, false);
        }
        return s;
    }

    private float[] error() {
        float[] s = buffer(0.3);
        cuadrada(s, 0, 220, 0.1, 0.07);
        cuadrada(s, 0.13, 185, 0.12, 0.07);
        return pasaBajos(s, 1400);
    }

    // ============================================================ utilidades

    private static float[] buffer(double segundos) {
        return new float[(int) (segundos * FRECUENCIA_MUESTREO)];
    }

    private static double coef(double corte) {
        return 1 - Math.exp(-DOS_PI * corte / FRECUENCIA_MUESTREO);
    }

    /** Seno con ataque corto y caída exponencial. */
    private static void seno(float[] s, double inicio, double f, double dur, double amp, double caida) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) (dur * FRECUENCIA_MUESTREO);
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double env = Math.min(1, t / 0.001) * Math.exp(-t / caida) * Math.min(1, (dur - t) / 0.006);
            s[a + i] += (float) (Math.sin(DOS_PI * f * t) * amp * env);
        }
    }

    /** Ráfaga de ruido filtrado (pasa altos o pasa bajos). */
    private void ruido(float[] s, double inicio, double dur, double amp, double caida, double corte, boolean pasaAltos) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) (dur * FRECUENCIA_MUESTREO);
        double c = coef(corte);
        double y = 0;
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double r = azar.nextDouble() * 2 - 1;
            y += c * (r - y);
            double v = pasaAltos ? r - y : y;
            double cierre = Math.min(1, (dur - t) / 0.003);
            s[a + i] += (float) (v * amp * Math.exp(-t / caida) * cierre);
        }
    }

    /** Campana: varios parciales que se apagan a distinta velocidad. */
    private static void campana(float[] s, double inicio, double f, double dur, double amp) {
        double[] parciales = {1.0, 2.0, 2.76, 5.4};
        double[] pesos = {1.0, 0.45, 0.30, 0.12};
        for (int p = 0; p < parciales.length; p++) {
            seno(s, inicio, f * parciales[p], dur, amp * pesos[p], dur * 0.35 / (1 + p * 0.8));
        }
    }

    private static void triangulo(float[] s, double inicio, double f, double dur, double amp) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) (dur * FRECUENCIA_MUESTREO);
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double fase = (t * f) % 1.0;
            double v = 4 * Math.abs(fase - 0.5) - 1;
            double env = Math.min(1, t / 0.01) * Math.min(1, (dur - t) / 0.06);
            s[a + i] += (float) (v * amp * env);
        }
    }

    private static void cuadrada(float[] s, double inicio, double f, double dur, double amp) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) (dur * FRECUENCIA_MUESTREO);
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double v = ((t * f) % 1.0) < 0.5 ? 1 : -1;
            double env = Math.min(1, t / 0.005) * Math.min(1, (dur - t) / 0.03);
            s[a + i] += (float) (v * amp * env);
        }
    }

    /** Sonido de metal (corneta): suma de armónicos con vibrato. */
    private static void metal(float[] s, double inicio, double f, double dur, double amp) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) ((dur + 0.06) * FRECUENCIA_MUESTREO);
        double fase = 0;
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double vibrato = t > 0.12 ? 1 + 0.004 * Math.sin(DOS_PI * 5.5 * t) : 1;
            fase += f * vibrato / FRECUENCIA_MUESTREO;
            double ataque = Math.min(1, t / 0.025);
            double brillo = 0.5 + 0.5 * ataque;
            double v = 0;
            for (int h = 1; h <= 8; h++) {
                v += Math.sin(DOS_PI * h * fase) * Math.pow(h, -1.1) * (h > 2 ? brillo : 1);
            }
            double salida = t > dur ? Math.max(0, 1 - (t - dur) / 0.06) : 1;
            s[a + i] += (float) (v * amp * ataque * salida);
        }
    }

    private void silbido(float[] s, double inicio, double f, double dur, double amp) {
        int a = (int) (inicio * FRECUENCIA_MUESTREO);
        int n = (int) (dur * FRECUENCIA_MUESTREO);
        double fase = 0;
        for (int i = 0; i < n && a + i < s.length; i++) {
            double t = i / FRECUENCIA_MUESTREO;
            double ff = f * (1 + 0.3 * t / dur);
            fase += ff / FRECUENCIA_MUESTREO;
            double env = Math.sin(Math.PI * t / dur);
            s[a + i] += (float) (Math.sin(DOS_PI * fase) * amp * env);
        }
    }

    private static float[] pasaBajos(float[] s, double corte) {
        double a = coef(corte);
        double y = 0;
        for (int i = 0; i < s.length; i++) {
            y += a * (s[i] - y);
            s[i] = (float) y;
        }
        return s;
    }

    /** Recorta a {@code largo} segundos mezclando el final con el inicio para que el bucle no se note. */
    private static float[] enBucle(float[] s, double largo, double cruce) {
        int n = (int) (largo * FRECUENCIA_MUESTREO);
        int c = (int) (cruce * FRECUENCIA_MUESTREO);
        float[] r = new float[n];
        System.arraycopy(s, 0, r, 0, n);
        for (int i = 0; i < c && n + i < s.length; i++) {
            float m = (float) i / c;
            r[i] = r[i] * m + s[n + i] * (1 - m);
        }
        return r;
    }

    private static void normalizar(float[] s, float pico) {
        float max = 0;
        for (float v : s) {
            max = Math.max(max, Math.abs(v));
        }
        if (max > 0) {
            float k = pico / max;
            for (int i = 0; i < s.length; i++) {
                s[i] *= k;
            }
        }
    }
}
