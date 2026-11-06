package broadway.juegos.ruleta;

import broadway.modelo.ResultadoRonda;

import java.util.Collections;
import java.util.List;

/** Número ganador de una tirada y qué apuestas cobraron. */
public class ResultadoTirada {

    private final int numero;
    private final List<ApuestaRuleta> ganadoras;
    private final ResultadoRonda ronda;

    public ResultadoTirada(int numero, List<ApuestaRuleta> ganadoras, ResultadoRonda ronda) {
        this.numero = numero;
        this.ganadoras = ganadoras;
        this.ronda = ronda;
    }

    public int getNumero() {
        return numero;
    }

    public List<ApuestaRuleta> getGanadoras() {
        return Collections.unmodifiableList(ganadoras);
    }

    public ResultadoRonda getRonda() {
        return ronda;
    }
}
