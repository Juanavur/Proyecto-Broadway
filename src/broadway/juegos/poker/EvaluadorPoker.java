package broadway.juegos.poker;

import broadway.cartas.Carta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Evalúa manos de póker: de 5 a 7 cartas encuentra la mejor combinación de 5
 * y le asigna un puntaje. Un puntaje mayor siempre es una mano mejor; dos manos
 * con el mismo puntaje empatan y se reparten el bote.
 */
public final class EvaluadorPoker {

    private static final String[] SINGULAR = {"", "", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete",
            "Ocho", "Nueve", "Diez", "Jota", "Reina", "Rey", "As"};
    private static final String[] PLURAL = {"", "", "Doses", "Treses", "Cuatros", "Cincos", "Seises", "Sietes",
            "Ochos", "Nueves", "Dieces", "Jotas", "Reinas", "Reyes", "Ases"};

    private EvaluadorPoker() {
    }

    /** Mejor mano de 5 cartas que se puede formar con 5, 6 o 7 cartas. */
    public static ManoEvaluada evaluar(List<Carta> cartas) {
        if (cartas.size() < 5 || cartas.size() > 7) {
            throw new IllegalArgumentException("Se necesitan entre 5 y 7 cartas.");
        }
        ManoEvaluada mejor = null;
        int n = cartas.size();
        for (int a = 0; a < n; a++) {
            for (int b = a + 1; b < n; b++) {
                for (int c = b + 1; c < n; c++) {
                    for (int d = c + 1; d < n; d++) {
                        for (int e = d + 1; e < n; e++) {
                            ManoEvaluada m = evaluarCinco(List.of(cartas.get(a), cartas.get(b),
                                    cartas.get(c), cartas.get(d), cartas.get(e)));
                            if (mejor == null || m.compareTo(mejor) > 0) {
                                mejor = m;
                            }
                        }
                    }
                }
            }
        }
        return mejor;
    }

    static ManoEvaluada evaluarCinco(List<Carta> cinco) {
        List<Carta> ordenadas = new ArrayList<>(cinco);
        ordenadas.sort(Comparator.comparingInt((Carta c) -> c.getValor().getRango()).reversed());

        int[] conteo = new int[15];
        for (Carta c : ordenadas) {
            conteo[c.getValor().getRango()]++;
        }
        boolean color = ordenadas.stream().allMatch(c -> c.getPalo() == ordenadas.get(0).getPalo());
        int altaEscalera = buscarEscalera(conteo);

        // Rangos agrupados: primero por cuántas veces aparecen, luego por valor.
        List<int[]> grupos = new ArrayList<>(); // {rango, veces}
        for (int r = 14; r >= 2; r--) {
            if (conteo[r] > 0) {
                grupos.add(new int[]{r, conteo[r]});
            }
        }
        grupos.sort((x, y) -> x[1] != y[1] ? y[1] - x[1] : y[0] - x[0]);
        int[] desempate = grupos.stream().mapToInt(g -> g[0]).toArray();

        CategoriaMano categoria;
        String descripcion;
        if (altaEscalera > 0 && color) {
            if (altaEscalera == 14) {
                categoria = CategoriaMano.ESCALERA_REAL;
                descripcion = "Escalera real";
            } else {
                categoria = CategoriaMano.ESCALERA_COLOR;
                descripcion = "Escalera de color al " + SINGULAR[altaEscalera];
            }
            desempate = new int[]{altaEscalera};
        } else if (grupos.get(0)[1] == 4) {
            categoria = CategoriaMano.POKER;
            descripcion = "Póker de " + PLURAL[grupos.get(0)[0]];
        } else if (grupos.get(0)[1] == 3 && grupos.get(1)[1] == 2) {
            categoria = CategoriaMano.FULL;
            descripcion = "Full de " + PLURAL[grupos.get(0)[0]] + " con " + PLURAL[grupos.get(1)[0]];
        } else if (color) {
            categoria = CategoriaMano.COLOR;
            descripcion = "Color al " + SINGULAR[desempate[0]];
        } else if (altaEscalera > 0) {
            categoria = CategoriaMano.ESCALERA;
            descripcion = "Escalera al " + SINGULAR[altaEscalera];
            desempate = new int[]{altaEscalera};
        } else if (grupos.get(0)[1] == 3) {
            categoria = CategoriaMano.TRIO;
            descripcion = "Trío de " + PLURAL[grupos.get(0)[0]];
        } else if (grupos.get(0)[1] == 2 && grupos.get(1)[1] == 2) {
            categoria = CategoriaMano.DOBLE_PAREJA;
            descripcion = "Doble pareja: " + PLURAL[grupos.get(0)[0]] + " y " + PLURAL[grupos.get(1)[0]];
        } else if (grupos.get(0)[1] == 2) {
            categoria = CategoriaMano.PAREJA;
            descripcion = "Pareja de " + PLURAL[grupos.get(0)[0]];
        } else {
            categoria = CategoriaMano.CARTA_ALTA;
            descripcion = "Carta alta: " + SINGULAR[desempate[0]];
        }

        long puntaje = categoria.ordinal();
        for (int i = 0; i < 5; i++) {
            puntaje = puntaje * 15 + (i < desempate.length ? desempate[i] : 0);
        }
        return new ManoEvaluada(categoria, puntaje, ordenadas, descripcion);
    }

    /**
     * Puntaje de la mejor mano de 5 a 7 cartas sin crear objetos (mucho más rápido).
     * Da exactamente el mismo número que {@code evaluar(cartas).getPuntaje()}; lo usan
     * los rivales para simular miles de manos por decisión.
     */
    public static long puntajeRapido(Carta[] cartas, int n) {
        int[] conteo = new int[15];
        int[] mascaraPalo = new int[4];
        int[] cuantosPalo = new int[4];
        int mascara = 0;
        for (int i = 0; i < n; i++) {
            int r = cartas[i].getValor().getRango();
            int p = cartas[i].getPalo().ordinal();
            conteo[r]++;
            mascaraPalo[p] |= 1 << r;
            cuantosPalo[p]++;
            mascara |= 1 << r;
        }
        for (int p = 0; p < 4; p++) {
            if (cuantosPalo[p] >= 5) {
                int alta = escaleraEnMascara(mascaraPalo[p]);
                if (alta > 0) {
                    return codificar(alta == 14 ? CategoriaMano.ESCALERA_REAL : CategoriaMano.ESCALERA_COLOR, alta);
                }
            }
        }
        int cuatro = 0, tresA = 0, tresB = 0, parA = 0, parB = 0, parC = 0;
        for (int r = 14; r >= 2; r--) {
            if (conteo[r] == 4) {
                cuatro = r;
            } else if (conteo[r] == 3) {
                if (tresA == 0) tresA = r; else if (tresB == 0) tresB = r;
            } else if (conteo[r] == 2) {
                if (parA == 0) parA = r; else if (parB == 0) parB = r; else if (parC == 0) parC = r;
            }
        }
        if (cuatro > 0) {
            return codificar(CategoriaMano.POKER, cuatro, mayorExcepto(conteo, cuatro, 0, 0));
        }
        if (tresA > 0 && (tresB > 0 || parA > 0)) {
            return codificar(CategoriaMano.FULL, tresA, Math.max(tresB, parA));
        }
        for (int p = 0; p < 4; p++) {
            if (cuantosPalo[p] >= 5) {
                int[] t = new int[5];
                int k = 0;
                for (int r = 14; r >= 2 && k < 5; r--) {
                    if ((mascaraPalo[p] & (1 << r)) != 0) {
                        t[k++] = r;
                    }
                }
                return codificar(CategoriaMano.COLOR, t);
            }
        }
        int altaEscalera = escaleraEnMascara(mascara);
        if (altaEscalera > 0) {
            return codificar(CategoriaMano.ESCALERA, altaEscalera);
        }
        if (tresA > 0) {
            int k1 = mayorExcepto(conteo, tresA, 0, 0);
            int k2 = mayorExcepto(conteo, tresA, k1, 0);
            return codificar(CategoriaMano.TRIO, tresA, k1, k2);
        }
        if (parA > 0 && parB > 0) {
            return codificar(CategoriaMano.DOBLE_PAREJA, parA, parB, mayorExcepto(conteo, parA, parB, 0));
        }
        if (parA > 0) {
            int k1 = mayorExcepto(conteo, parA, 0, 0);
            int k2 = mayorExcepto(conteo, parA, k1, 0);
            int k3 = mayorExcepto(conteo, parA, k1, k2);
            return codificar(CategoriaMano.PAREJA, parA, k1, k2, k3);
        }
        int[] t = new int[5];
        int k = 0;
        for (int r = 14; r >= 2 && k < 5; r--) {
            if (conteo[r] > 0) {
                t[k++] = r;
            }
        }
        return codificar(CategoriaMano.CARTA_ALTA, t);
    }

    private static int mayorExcepto(int[] conteo, int a, int b, int c) {
        for (int r = 14; r >= 2; r--) {
            if (conteo[r] > 0 && r != a && r != b && r != c) {
                return r;
            }
        }
        return 0;
    }

    private static int escaleraEnMascara(int m) {
        for (int alta = 14; alta >= 6; alta--) {
            int necesaria = 0x1F << (alta - 4);
            if ((m & necesaria) == necesaria) {
                return alta;
            }
        }
        int rueda = (1 << 14) | (1 << 2) | (1 << 3) | (1 << 4) | (1 << 5);
        return (m & rueda) == rueda ? 5 : 0;
    }

    private static long codificar(CategoriaMano categoria, int... desempate) {
        long puntaje = categoria.ordinal();
        for (int i = 0; i < 5; i++) {
            puntaje = puntaje * 15 + (i < desempate.length ? desempate[i] : 0);
        }
        return puntaje;
    }

    /** Devuelve la carta más alta de la escalera, o 0 si no hay. La escalera A-2-3-4-5 vale 5. */
    private static int buscarEscalera(int[] conteo) {
        for (int alta = 14; alta >= 6; alta--) {
            boolean ok = true;
            for (int r = alta; r > alta - 5; r--) {
                if (conteo[r] != 1) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return alta;
            }
        }
        if (conteo[14] == 1 && conteo[2] == 1 && conteo[3] == 1 && conteo[4] == 1 && conteo[5] == 1) {
            return 5;
        }
        return 0;
    }
}
