package ui.fx;

import enums.Efecto;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;
import modelos.Apuesta;
import modelos.Blackjack;
import modelos.Mesa;
import modelos.RondaMesa;
import servicios.Formato;
import servicios.sonido.Sonidos;

import java.util.ArrayList;
import java.util.List;

final class FxBlackjack extends FxCustomerGame {
    private static final long[] CHIPS = {5_000, 10_000, 25_000, 100_000, 500_000};
    private static final String PROMPT = "Toca las fichas para armar tu apuesta.";
    private final Label stake = FxUi.label("", "total");
    private final Button deal = FxUi.button("Repartir", "primary", this::deal);
    private final Button repeat = FxUi.button("Repetir apuesta", "", this::repeatStake);
    private final FlowPane betting;
    private final HBox hand;
    private final Label revealing = FxUi.label("El crupier muestra sus cartas…", "hint");
    private final StackPane actions = new StackPane();
    private final List<Integer> player = new ArrayList<>();
    private final List<Integer> dealer = new ArrayList<>();
    private final FxArt.Picture artwork = new FxArt.Picture(900, 480, this::paint);
    private long draft;
    private long last;
    private int dealerVisible;
    private boolean reveal;
    private Blackjack game;
    private RondaMesa round;
    private Apuesta wager;

    FxBlackjack(FxPlayContext view) {
        super(view, "Blackjack", "Llega a 21 sin pasarte. El blackjack natural paga 3 a 2.",
                "Arma una apuesta y pulsa Repartir. El crupier pide por debajo de 17. Una mano ganadora paga 1 a 1, el blackjack natural 3 a 2.",
                Blackjack.class);
        setCenter(artwork);
        betting = new FlowPane(9, 5, chipSelector(CHIPS, chip -> setDraft(draft + chip)),
                FxUi.row(stake, FxUi.button("Limpiar", "", () -> setDraft(0)), repeat, deal));
        betting.setAlignment(Pos.CENTER);
        hand = FxUi.row(FxUi.button("Pedir carta", "primary", this::hit), FxUi.button("Plantarse", "", this::stand));
        hand.setAlignment(Pos.CENTER);
        actions.getChildren().addAll(betting, hand, revealing);
        footer(actions);
        tables.setOnAction(event -> {
            refreshLimits();
            clearHand();
        });
        clearHand();
    }

    @Override void enter() { super.enter(); clearHand(); }

    private void clearHand() {
        draft = 0;
        player.clear();
        dealer.clear();
        message.setText(table() == null ? "No hay mesas de blackjack abiertas." : PROMPT);
        update();
    }

    private void setDraft(long stake) {
        if (table() == null || !view.attempt(() -> {
            if (stake > view.customer().getSaldoFichas() || stake > table().getApuestaMaxima()) {
                throw new IllegalArgumentException("Revisa tu saldo y el máximo de la mesa.");
            }
        })) return;
        clearHand();
        draft = stake;
        update();
    }

    private void repeatStake() {
        setDraft(last);
    }

    private void deal() {
        Mesa table = table();
        if (table == null || draft == 0 || round != null || reveal) return;
        RondaMesa target = table.rondaActual();
        if (!target.getApuestas().isEmpty()) {
            view.notice("La mesa tiene una mano pendiente.", true);
            return;
        }
        if (!view.attempt(() -> wager = table.getCrupierAsignado().registrarApuestas(draft, table, view.customer(), "Mano"))) return;
        game = (Blackjack) table.getJuegoAsignado();
        round = target;
        last = draft;
        player.clear();
        dealer.clear();
        for (int index = 0; index < 2; index++) {
            player.add(game.nuevaCarta());
            dealer.add(game.nuevaCarta());
            Sonidos.reproducir(Efecto.CARTA, 200 * index);
        }
        dealerVisible = 1;
        view.refresh();
        boolean natural = Blackjack.valorMano(player) == 21;
        boolean houseNatural = Blackjack.valorMano(dealer) == 21;
        if (natural || houseNatural) {
            if (natural && houseNatural) finish(Blackjack.EMPATE, "Ambos tienen blackjack");
            else if (natural) finish(Blackjack.BLACKJACK, "¡Blackjack natural!");
            else finish(Blackjack.PIERDE, "El crupier tiene blackjack");
        } else {
            message.setText("¿Pides otra carta o te plantas?");
            update();
        }
    }

    private void hit() {
        if (round == null || reveal) return;
        player.add(game.nuevaCarta());
        Sonidos.reproducir(Efecto.CARTA);
        int value = Blackjack.valorMano(player);
        if (value > 21) finish(Blackjack.PIERDE, "Te pasaste de 21");
        else if (value == 21) stand();
        else artwork.redraw();
    }

    private void stand() {
        if (round == null || reveal) return;
        while (Blackjack.valorMano(dealer) < 17) dealer.add(game.nuevaCarta());
        int house = Blackjack.valorMano(dealer);
        int value = Blackjack.valorMano(player);
        if (house > 21 || value > house) finish(Blackjack.GANA, "Ganas la mano");
        else if (house == value) finish(Blackjack.EMPATE, "Empate");
        else finish(Blackjack.PIERDE, "Gana la casa");
    }

    private void finish(int result, String caption) {
        double net = game.liquidarApuesta(wager, result) - wager.getMonto();
        round.liquidar(result);
        round = null;
        reveal = true;
        dealerVisible = 2;
        message.setText("El crupier juega su mano…");
        update();
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(340), event -> {
            if (dealerVisible < dealer.size()) {
                dealerVisible++;
                artwork.redraw();
                Sonidos.reproducir(Efecto.CARTA);
            }
        }));
        animation.setCycleCount(Math.max(1, dealer.size() - 1));
        animation.setOnFinished(event -> {
            reveal = false;
            draft = 0;
            message.setText(caption + " · " + result(net));
            if (net > 0) Sonidos.reproducir(result == Blackjack.BLACKJACK ? Efecto.GANAR_GRANDE : Efecto.GANAR);
            view.refresh();
            update();
        });
        animation.play();
    }

    private void update() {
        stake.setText("Apuesta: " + Formato.dinero(draft));
        deal.setDisable(table() == null || draft == 0);
        repeat.setDisable(table() == null || last == 0);
        only(betting, round == null && !reveal);
        only(hand, round != null && !reveal);
        only(revealing, reveal);
        tables.setDisable(round != null || reveal);
        artwork.redraw();
    }

    private static void only(Node node, boolean visible) {
        node.setManaged(visible);
        node.setVisible(visible);
    }

    private void paint(GraphicsContext g, double width, double height) {
        double radius = width * .4;
        g.setStroke(Color.rgb(255, 255, 255, 0.23));
        g.setLineWidth(2);
        g.strokeArc(width / 2 - radius, height * .16 - radius * .55, radius * 2, radius * 1.6, 200, 140, ArcType.OPEN);
        FxArt.text(g, "BLACKJACK PAGA 3 A 2", width / 2, height * .47, 27, FxArt.CREAM);
        FxArt.text(g, "EL CRUPIER PIDE HASTA 16 Y SE PLANTA EN 17", width / 2, height * .53, 14, Color.WHITESMOKE);
        double cardWidth = Math.max(35, Math.min(98, height / 5));
        if (dealer.isEmpty()) FxArt.text(g, "CRUPIER", width / 2, height * .19, 16, Color.LIGHTGRAY);
        else drawHand(g, dealer, Math.max(2, dealerVisible), dealerVisible, width, height * .055, cardWidth, "Crupier");
        if (!player.isEmpty()) {
            drawHand(g, player, player.size(), player.size(), width, height * .62, cardWidth, "Tu mano");
        } else {
            double x = width / 2;
            double y = height * .74;
            double chipRadius = cardWidth * .55;
            g.setStroke(FxArt.CREAM);
            g.strokeOval(x - chipRadius, y - chipRadius, chipRadius * 2, chipRadius * 2);
            if (draft > 0) FxArt.chip(g, x, y, chipRadius * .71, draft);
            else FxArt.text(g, "APUESTA", x, y, 14, FxArt.CREAM);
        }
    }

    private static void drawHand(GraphicsContext g, List<Integer> cards, int count, int visible, double width, double top,
                                 double cardWidth, String title) {
        double height = cardWidth * 1.42;
        double step = cardWidth * .57;
        double x = (width - cardWidth - (count - 1) * step) / 2;
        for (int index = 0; index < count; index++) FxArt.card(g, index < visible ? cards.get(index) : 0, index % 4,
                x + index * step, top, cardWidth, height);
        FxArt.text(g, title + " · " + Blackjack.valorMano(cards.subList(0, visible)), width / 2, top + height + 17, 15, Color.WHITE);
    }

    @Override Node icon() {
        return new FxArt.Picture(116, 100, (g, w, h) -> {
            g.save();
            g.translate(w * .24, h * .23);
            g.rotate(-15);
            FxArt.card(g, 1, 0, 0, 0, 38, 54);
            g.restore();
            g.save();
            g.translate(w * .52, h * .13);
            g.rotate(14);
            FxArt.card(g, 13, 1, 0, 0, 38, 54);
            g.restore();
        });
    }

    @Override boolean leave() {
        return waitForRound(round != null || reveal, "Termina la mano antes de salir de la mesa.");
    }
}
