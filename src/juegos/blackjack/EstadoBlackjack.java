package broadway.juegos.blackjack;

/** Momentos de una ronda de blackjack. */
public enum EstadoBlackjack {
    /** No hay ronda en curso: el jugador puede apostar. */
    ESPERANDO_APUESTA,
    /** El crupier muestra un As: se ofrece el seguro antes de revisar su carta oculta. */
    OFRECIENDO_SEGURO,
    /** El jugador decide qué hacer con su mano actual. */
    TURNO_JUGADOR,
    /** La ronda terminó y ya se pagó. */
    RONDA_TERMINADA
}
