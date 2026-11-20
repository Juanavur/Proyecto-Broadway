package ui.fx;

import enums.Efecto;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import modelos.Casino;
import modelos.Cliente;
import servicios.CustomerSession;
import servicios.Formato;
import servicios.sonido.Sonidos;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class FxCustomer extends StackPane implements FxPlayContext {
    private final Casino casino;
    private final Runnable back;
    private final BorderPane frame = new BorderPane();
    private final Label name = FxUi.label("", "section-title");
    private final Label level = FxUi.label("", "hint");
    private final Label cash = FxUi.label("", "total");
    private final Label chips = FxUi.label("", "total");
    private final Label cashback = FxUi.label("", "total");
    private final Label message = FxUi.label("", "notice");
    private final Button sound = FxUi.button("♪ Sonido", "", () -> {
        Sonidos.setSilencio(!Sonidos.isSilencio());
        refreshSound();
    });
    private final Button cashButton = FxUi.button("Caja", "", this::showCash);
    private final Button lobbyButton = FxUi.button("Lobby", "", this::showLobby);
    private final PauseTransition dismiss = new PauseTransition(Duration.seconds(3.6));
    private final List<Movement> history = new ArrayList<>();
    private CustomerSession session;
    private List<FxCustomerGame> games;
    private FxCustomerGame game;

    FxCustomer(Casino casino, Runnable back) {
        this.casino = casino;
        this.back = back;
        getStyleClass().add("felt");
        name.setMaxWidth(190);
        message.setVisible(false);
        message.setMouseTransparent(true);
        message.setWrapText(true);
        message.setMaxWidth(620);
        StackPane.setAlignment(message, Pos.TOP_CENTER);
        StackPane.setMargin(message, new Insets(72, 16, 0, 16));
        dismiss.setOnFinished(event -> message.setVisible(false));
        frame.setCenter(welcome());
        getChildren().addAll(frame, message);
    }

    private Node welcome() {
        TextField fullName = FxUi.field("Nombre completo");
        fullName.setId("customer-name");
        TextField document = FxUi.field("Documento");
        document.setId("customer-document");
        DatePicker birth = FxUi.birthDateField();
        birth.setId("customer-birth");
        TextField initialCash = FxUi.field("1.000.000");
        initialCash.setId("customer-initial-cash");
        initialCash.setText("1.000.000");
        Button enter = FxUi.button("Entrar al casino", "primary", () -> attempt(() -> {
            session = new CustomerSession(casino, fullName.getText(), document.getText(), FxUi.birthDate(birth),
                    Formato.parseAmount(initialCash.getText()));
            games = List.of(new FxSlots(this), new FxBlackjack(this), new FxRoulette(this));
            frame.setTop(toolbar());
            showLobby();
        }));
        enter.setDefaultButton(true);
        HBox actions = FxUi.row(FxUi.button("Volver al inicio", "", back), FxUi.spacer(), enter);
        VBox card = FxUi.panel(FxUi.label("CASINO BROADWAY", "brand"),
                FxUi.label("Regístrate en la entrada para pasar a la sala de juegos", "hint"),
                FxUi.form(new String[]{"Nombre completo", "Documento", "Fecha de nacimiento", "Dinero con el que llegas"},
                        fullName, document, birth, initialCash), actions,
                FxUi.label("Solo mayores de edad. Juego virtual: no se usa dinero real.", "hint"));
        card.setMaxSize(610, VBox.USE_PREF_SIZE);
        card.setPadding(new Insets(24, 34, 24, 34));
        return new StackPane(card);
    }

    private HBox toolbar() {
        HBox bar = FxUi.row(FxUi.label("BROADWAY", "brand"), FxUi.column(name, level), FxUi.spacer(),
                metric("EFECTIVO", cash), metric("FICHAS", chips), metric("CASHBACK PENDIENTE", cashback),
                sound, cashButton, lobbyButton);
        bar.getStyleClass().add("toolbar");
        bar.setSpacing(15);
        return bar;
    }

    private static VBox metric(String caption, Label value) {
        VBox box = new VBox(2, FxUi.label(caption, "metric"), value);
        return box;
    }

    private void refreshSound() {
        sound.setText(Sonidos.isSilencio() ? "♪ Sin sonido" : "♪ Sonido");
    }

    @Override
    public void refresh() {
        Cliente client = customer();
        name.setText(client.getNombre());
        level.setText(client.getNivel() + " · " + Formato.chips(client.getNivel().getPorcentaje()) + "% CASHBACK");
        cash.setText(Formato.dinero(session.cash()));
        chips.setText(Formato.dinero(client.getSaldoFichas()));
        cashback.setText(Formato.dinero(pending()));
        refreshSound();
    }

    private double pending() {
        return Cliente.NivelFidelidad.calcularCashback(customer().getNivel().name(), customer().getPerdidasPeriodo());
    }

    private boolean leaveGame() {
        if (game != null && !game.leave()) return false;
        game = null;
        message.setVisible(false);
        return true;
    }

    private void showLobby() {
        if (!leaveGame()) return;
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        for (int index = 0; index < 2; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(50);
            grid.getRowConstraints().add(row);
        }
        int index = 0;
        for (FxCustomerGame entry : games) {
            grid.add(tile(entry.title(), entry.description(), entry.icon(), () -> showGame(entry)), index % 2, index / 2);
            index++;
        }
        FxArt.Picture cashIcon = new FxArt.Picture(116, 100, (g, width, height) -> {
            for (int pile = 0; pile < 3; pile++) {
                for (int chip = 0; chip < 4 - pile; chip++) {
                    FxArt.chip(g, width * (0.23 + pile * 0.26), height * (0.76 - chip * 0.13), 16,
                            new long[]{5_000, 10_000, 25_000}[(pile + chip) % 3]);
                }
            }
        });
        grid.add(tile("Caja", "Compra y vende fichas, y reclama tu cashback.", cashIcon, this::showCash), 1, 1);
        String summary = "Total apostado " + Formato.dinero(customer().getTotalApostado()) + "  ·  Pérdidas del periodo "
                + Formato.dinero(customer().getPerdidasPeriodo());
        VBox page = FxUi.page(FxUi.label("Sala de juegos", "title"),
                FxUi.label("Escoge una mesa. Todas las fichas que pierdas te devuelven cashback según tu nivel.", "hint"),
                grid, FxUi.row(FxUi.label(summary, "hint"), FxUi.spacer(), FxUi.button("Volver al inicio", "danger", back)));
        FxUi.fill(page, grid);
        show(page, false, true);
    }

    private static Button tile(String title, String description, Node icon, Runnable open) {
        Button tile = FxUi.button(title, "tile", open);
        Label hint = FxUi.label(description, "hint");
        hint.setWrapText(true);
        hint.setAlignment(Pos.CENTER);
        VBox content = FxUi.column(icon, FxUi.label(title, "title"), hint);
        content.setAlignment(Pos.CENTER);
        tile.setGraphic(content);
        tile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return tile;
    }

    private void showGame(FxCustomerGame chosen) {
        if (!leaveGame()) return;
        game = chosen;
        game.enter();
        show(game, true, true);
    }

    private void show(Node page, boolean lobbyVisible, boolean cashVisible) {
        lobbyButton.setVisible(lobbyVisible);
        lobbyButton.setManaged(lobbyVisible);
        cashButton.setVisible(cashVisible);
        cashButton.setManaged(cashVisible);
        refresh();
        frame.setCenter(page);
    }

    private void showCash() {
        if (!leaveGame()) return;
        TableView<Movement> table = new TableView<>();
        FxUi.column(table, "Fecha", Movement::time);
        FxUi.column(table, "Movimiento", Movement::description);
        FxUi.column(table, "Efectivo", item -> delta(item.cash()));
        FxUi.column(table, "Fichas", item -> delta(item.chips()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getItems().setAll(history);
        Label pending = FxUi.label(Formato.dinero(pending()), "total");
        Button claim = new Button("Reclamar cashback");
        claim.setDisable(pending() <= 0);
        claim.setOnAction(event -> {
            double amount = customer().reclamarCashback();
            if (amount > 0) record(table, "Cashback", 0, amount);
            pending.setText(Formato.dinero(pending()));
            claim.setDisable(pending() <= 0);
        });
        VBox left = FxUi.column(FxUi.label("Caja", "title"), FxUi.label("1 peso = 1 ficha.", "hint"),
                exchange(true, table), exchange(false, table),
                FxUi.panel(FxUi.label("Cashback pendiente", "section-title"), pending, claim));
        left.setPrefWidth(420);
        VBox right = FxUi.column(FxUi.label("Historial de movimientos (el más reciente arriba)", "section-title"), table);
        FxUi.fill(right, table);
        HBox body = FxUi.row(left, right);
        body.setAlignment(Pos.TOP_LEFT);
        body.setSpacing(20);
        HBox.setHgrow(right, Priority.ALWAYS);
        VBox page = FxUi.page(body);
        VBox.setVgrow(body, Priority.ALWAYS);
        show(page, true, false);
    }

    private VBox exchange(boolean purchase, TableView<Movement> table) {
        TextField amount = FxUi.field("Monto");
        amount.setId(purchase ? "buy-amount" : "redeem-amount");
        Runnable exchange = () -> attempt(() -> {
            double value = Formato.parseAmount(amount.getText());
            if (purchase) session.buyChips(value); else session.redeemChips(value);
            amount.clear();
            record(table, purchase ? "Compra de fichas" : "Canje de fichas", purchase ? -value : value, purchase ? value : -value);
        });
        amount.setOnAction(event -> exchange.run());
        HBox presets = FxUi.row();
        for (int value : new int[]{10_000, 50_000, 100_000, 500_000}) {
            presets.getChildren().add(FxUi.button("+" + value / 1000 + "K", "", () -> attempt(() -> {
                double current = amount.getText().isBlank() ? 0 : Formato.parseAmount(amount.getText());
                amount.setText(Formato.chips(current + value));
            })));
        }
        presets.getChildren().add(FxUi.button("Todo", "", () -> amount.setText(Formato.chips(
                purchase ? session.cash() : customer().getSaldoFichas()))));
        HBox.setHgrow(amount, Priority.ALWAYS);
        return FxUi.panel(FxUi.label(purchase ? "Comprar fichas con efectivo" : "Cambiar fichas por efectivo", "section-title"),
                FxUi.row(amount, FxUi.button(purchase ? "Comprar fichas" : "Cambiar a efectivo", "primary", exchange)), presets);
    }

    private void record(TableView<Movement> table, String description, double cashDelta, double chipsDelta) {
        Movement item = new Movement(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), description, cashDelta, chipsDelta);
        history.add(0, item);
        table.getItems().add(0, item);
        Sonidos.reproducir(Efecto.CAJA);
        refresh();
        notice(description + " · " + delta(chipsDelta), false);
    }

    private static String delta(double amount) {
        return (amount > 0 ? "+" : "") + Formato.dinero(amount);
    }

    @Override
    public void notice(String text, boolean error) {
        message.setText(text);
        message.setStyle(error ? "-fx-text-fill:#ff7a7a" : "-fx-text-fill:#f2dc8f");
        message.setVisible(true);
        dismiss.playFromStart();
    }

    @Override
    public Casino casino() { return casino; }
    @Override
    public Cliente customer() { return session.customer(); }

    private record Movement(String time, String description, double cash, double chips) { }
}
