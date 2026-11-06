package broadway.juegos.poker;

import broadway.cartas.Carta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Un puesto en la mesa de póker: el jugador humano o un rival controlado por la computadora. */
public class AsientoPoker {

    private final String nombre;
    private final EstrategiaRival estrategia; // null para el jugador humano
    private long fichas;
    private final List<Carta> cartas = new ArrayList<>(2);
    private long apuestaRonda;
    private long aportadoMano;
    private boolean retirado;
    private boolean allIn;
    private boolean haActuado;
    private boolean puedeSubir;
    private String ultimaAccion = "";
    private ManoEvaluada manoFinal;

    AsientoPoker(String nombre, EstrategiaRival estrategia, long fichas) {
        this.nombre = nombre;
        this.estrategia = estrategia;
        this.fichas = fichas;
    }

    void nuevaMano() {
        cartas.clear();
        apuestaRonda = 0;
        aportadoMano = 0;
        retirado = false;
        allIn = false;
        haActuado = false;
        puedeSubir = true;
        ultimaAccion = "";
        manoFinal = null;
    }

    /** Pone fichas en el bote (como máximo las que tiene) y devuelve cuánto puso. */
    long poner(long monto) {
        long real = Math.min(monto, fichas);
        fichas -= real;
        apuestaRonda += real;
        aportadoMano += real;
        if (fichas == 0) {
            allIn = true;
        }
        return real;
    }

    /** Devuelve una apuesta que nadie igualó. */
    void devolver(long monto) {
        fichas += monto;
        apuestaRonda -= monto;
        aportadoMano -= monto;
        if (fichas > 0) {
            allIn = false;
        }
    }

    void ganar(long monto) {
        fichas += monto;
    }

    void agregarFichas(long monto) {
        fichas += monto;
    }

    void recibir(Carta carta) {
        cartas.add(carta);
    }

    void reiniciarRonda() {
        apuestaRonda = 0;
        haActuado = false;
        puedeSubir = true;
    }

    /** Sigue en la mano y todavía puede tomar decisiones. */
    public boolean puedeActuar() {
        return !retirado && !allIn;
    }

    public boolean isHumano() {
        return estrategia == null;
    }

    public String getNombre() {
        return nombre;
    }

    /** Estilo de juego del rival (null si es el jugador humano). */
    public EstrategiaRival getEstrategia() {
        return estrategia;
    }

    public long getFichas() {
        return fichas;
    }

    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }

    public long getApuestaRonda() {
        return apuestaRonda;
    }

    public long getAportadoMano() {
        return aportadoMano;
    }

    public boolean isRetirado() {
        return retirado;
    }

    void retirarse() {
        retirado = true;
    }

    public boolean isAllIn() {
        return allIn;
    }

    boolean isHaActuado() {
        return haActuado;
    }

    void setHaActuado(boolean haActuado) {
        this.haActuado = haActuado;
    }

    boolean isPuedeSubir() {
        return puedeSubir;
    }

    void setPuedeSubir(boolean puedeSubir) {
        this.puedeSubir = puedeSubir;
    }

    public String getUltimaAccion() {
        return ultimaAccion;
    }

    void setUltimaAccion(String ultimaAccion) {
        this.ultimaAccion = ultimaAccion;
    }

    public ManoEvaluada getManoFinal() {
        return manoFinal;
    }

    void setManoFinal(ManoEvaluada manoFinal) {
        this.manoFinal = manoFinal;
    }
}
