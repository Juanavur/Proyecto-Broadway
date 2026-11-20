package ui.fx;

import enums.Efecto;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import modelos.Apuesta;
import modelos.Cliente;
import modelos.Mesa;
import modelos.RondaMesa;
import modelos.Ruleta;
import servicios.Formato;
import servicios.sonido.Sonidos;

import java.util.LinkedHashMap;
import java.util.Map;

final class FxRoulette extends FxCustomerGame {
    private static final long[] CHIPS = {1_000, 5_000, 10_000, 25_000, 100_000};
    private static final String[] EVEN = {"Falta (1-18)", "Par", "Rojo", "Negro", "Impar", "Pasa (19-36)"};
    private final Map<String, Long> draft = new LinkedHashMap<>();
    private final Map<String, Long> previous = new LinkedHashMap<>();
    private final Map<String, Cell> cells = new LinkedHashMap<>();
    private final Label total = FxUi.label("", "total");
    private final Label recent = FxUi.label("ÚLTIMOS NÚMEROS", "hint");
    private final Button spin = FxUi.button("GIRAR", "primary", this::spin);
    private final Button repeat = FxUi.button("Repetir apuesta", "", this::repeat);
    private final Button withdraw = FxUi.button("Retirar fichas", "", () -> { draft.clear(); refreshDraft(); });
    private final Button register = FxUi.button("Registrar apuesta", "", this::registerDraft);
    private final FxArt.Picture wheel = new FxArt.Picture(380, 380, this::paintWheel);
    private long chip = 5_000;
    private double wheelAngle;
    private double ballAngle;
    private Integer winner;
    private boolean spinning;

    FxRoulette(FxPlayContext view) {
        super(view, "Ruleta", "Ruleta europea: plenos, docenas y apuestas sencillas.",
                "Coloca fichas en el tapete. El pleno paga 35 a 1, las docenas 2 a 1 y las sencillas 1 a 1."
                        + " Puedes quitar una apuesta antes de registrarla con el clic derecho.", Ruleta.class);
        GridPane mat = mat();
        VBox wheelColumn = FxUi.column(wheel, recent);
        wheelColumn.setMinWidth(195);
        wheelColumn.setPrefWidth(380);
        FxUi.fill(wheelColumn, wheel);
        HBox center = FxUi.row(wheelColumn, mat);
        HBox.setHgrow(mat, Priority.ALWAYS);
        center.setPadding(new Insets(15, 18, 15, 18));
        center.setAlignment(Pos.CENTER);
        setCenter(center);
        message.setText("Toca el tapete para apostar. Clic derecho quita una apuesta.");
        HBox chips = chipSelector(CHIPS, value -> chip = value);
        HBox operations = FxUi.row(total, withdraw, repeat);
        if (view.dealerMode()) operations.getChildren().add(register);
        operations.getChildren().add(spin);
        FlowPane controls = new FlowPane(14, 5, FxUi.row(FxUi.label("Ficha:", "hint"), chips), operations);
        controls.setAlignment(Pos.CENTER);
        footer(controls);
        tables.setOnAction(event -> {
            draft.clear();
            refreshLimits();
            refreshDraft();
        });
        refreshDraft();
    }

    @Override void enter() {
        super.enter();
        refreshDraft();
    }

    private GridPane mat() {
        GridPane mat = new GridPane();
        mat.setAlignment(Pos.CENTER);
        mat.setMaxHeight(450);
        for (int index = 0; index < 13; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 13);
            mat.getColumnConstraints().add(column);
        }
        for (int index = 0; index < 5; index++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(20);
            mat.getRowConstraints().add(row);
        }
        mat.add(cell("Pleno 0", "0", "zero-cell"), 0, 0, 1, 3);
        for (int number = 1; number <= 36; number++) {
            mat.add(cell("Pleno " + number, Integer.toString(number), Ruleta.esRojo(number) ? "red-cell" : "black-cell"),
                    1 + (number - 1) / 3, 2 - (number - 1) % 3);
        }
        for (int index = 0; index < 3; index++) mat.add(cell("Docena " + (index + 1), (index + 1) + "ª 12", ""), 1 + index * 4, 3, 4, 1);
        String[] names = {"1 a 18", "PAR", "◇", "◇", "IMPAR", "19 a 36"};
        for (int index = 0; index < names.length; index++) mat.add(cell(EVEN[index], names[index],
                index == 2 ? "red-cell" : index == 3 ? "black-cell" : ""), 1 + index * 2, 4, 2, 1);
        return mat;
    }

    private Button cell(String bet, String caption, String style) {
        Button cell = FxUi.button(caption, "mat-cell", () -> putChip(bet));
        if (!style.isEmpty()) cell.getStyleClass().add(style);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cell.setId("roulette-" + bet.replace(' ', '-'));
        cell.setAccessibleText(bet);
        cell.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY && !spinning) {
                draft.remove(bet);
                refreshDraft();
            }
        });
        cells.put(bet, new Cell(cell, caption));
        return cell;
    }

    private void putChip(String bet) {
        Mesa table = table();
        if (spinning || table == null) return;
        view.attempt(() -> {
            long amount = draft.getOrDefault(bet, 0L) + chip;
            if (amount > table.getApuestaMaxima() || draftTotal(draft) + chip > view.customer().getSaldoFichas()) {
                throw new IllegalArgumentException("Revisa tu saldo y el máximo de la mesa.");
            }
            draft.put(bet, amount);
            winner = null;
            Sonidos.reproducir(Efecto.FICHA);
            refreshDraft();
        });
    }

    private static long draftTotal(Map<String, Long> bets) {
        return bets.values().stream().mapToLong(Long::longValue).sum();
    }

    private boolean checkDraft(Mesa table, Map<String, Long> bets) {
        return view.attempt(() -> {
            Cliente customer = view.customer();
            if (table == null || !table.estaDisponible()) throw new IllegalStateException("La mesa está cerrada.");
            if (draftTotal(bets) > customer.getSaldoFichas()) throw new IllegalArgumentException("Saldo insuficiente.");
            for (long amount : bets.values()) {
                if (!table.validarMontoApuesta(amount)) {
                    throw new IllegalArgumentException("Cada apuesta debe estar dentro de los límites de la mesa.");
                }
            }
        });
    }

    private void repeat() {
        if (previous.isEmpty() || !checkDraft(table(), previous)) return;
        draft.clear();
        draft.putAll(previous);
        refreshDraft();
    }

    private void refreshDraft() {
        Mesa table = table();
        double placed = view.dealerMode() && table != null ? table.rondaActual().getApuestas().stream().mapToDouble(Apuesta::getMonto).sum() : 0;
        total.setText("En la mesa: " + Formato.dinero(draftTotal(draft) + placed));
        boolean noAction = spinning || table == null;
        spin.setDisable(noAction || (draft.isEmpty() && placed == 0));
        repeat.setDisable(noAction || previous.isEmpty());
        withdraw.setDisable(noAction || draft.isEmpty());
        register.setDisable(noAction || draft.isEmpty());
        tables.setDisable(spinning);
        for (var entry : cells.entrySet()) {
            String bet = entry.getKey();
            Cell cell = entry.getValue();
            cell.button().setText(draft.containsKey(bet) ? cell.caption() + "\n" + Formato.chips(draft.get(bet)) : cell.caption());
            cell.button().setDisable(noAction);
            cell.button().getStyleClass().remove("win-cell");
            if (("Pleno " + winner).equals(bet)) cell.button().getStyleClass().add("win-cell");
        }
    }

    private boolean registerDraft() {
        if (draft.isEmpty()) return true;
        Mesa table = table();
        if (!checkDraft(table, draft) || !view.attempt(() -> {
            Cliente customer = view.customer();
            for (var entry : draft.entrySet()) table.getCrupierAsignado().registrarApuestas(entry.getValue(), table, customer, entry.getKey());
        })) return false;
        previous.clear();
        previous.putAll(draft);
        if (view.dealerMode()) {
            draft.clear();
            refreshDraft();
        }
        view.refresh();
        return true;
    }

    private void spin() {
        Mesa table = table();
        if (table == null || spinning || !registerDraft()) return;
        RondaMesa round = table.rondaActual();
        if (round.getApuestas().isEmpty()) return;
        Ruleta roulette = (Ruleta) table.getJuegoAsignado();
        int number = roulette.girar();
        int index = 0;
        while (FxArt.WHEEL_ORDER[index] != number) index++;
        double startAngle = wheelAngle % 360;
        double endAngle = startAngle + 650;
        double finalBall = endAngle + 360.0 * index / FxArt.WHEEL_ORDER.length;
        spinning = true;
        winner = null;
        refreshDraft();
        message.setText("No va más. La bola está girando…");
        Sonidos.reproducir(Efecto.RULETA);
        new AnimationTimer() {
            private long start;
            @Override public void handle(long now) {
                if (start == 0) start = now;
                double progress = Math.min(1, (now - start) / 4_400_000_000.0);
                wheelAngle = endAngle - (endAngle - startAngle) * Math.pow(1 - progress, 3);
                ballAngle = finalBall + 1800 * Math.pow(1 - progress, 2);
                wheel.redraw();
                if (progress == 1) {
                    stop();
                    settle(roulette, round, number);
                }
            }
        }.start();
    }

    private void settle(Ruleta roulette, RondaMesa round, int number) {
        double net = round.getApuestas().stream().filter(bet -> view.dealerMode() || bet.getCliente() == view.customer())
                .mapToDouble(bet -> roulette.liquidarApuesta(bet, number) - bet.getMonto()).sum();
        int winners = round.liquidar(number);
        winner = number;
        spinning = false;
        draft.clear();
        String text = recent.getText();
        String numbers = number + "   " + (text.contains("\n") ? text.substring(text.indexOf('\n') + 1) : "");
        recent.setText("ÚLTIMOS NÚMEROS\n" + numbers.substring(0, Math.min(numbers.length(), 35)));
        message.setText("Salió el " + number + " · " + (view.dealerMode() ? winners + " apuestas ganadoras" : result(net)));
        if (net > 0) Sonidos.reproducir(Efecto.GANAR);
        refreshDraft();
        view.refresh();
        wheel.redraw();
    }

    private void paintWheel(javafx.scene.canvas.GraphicsContext g, double width, double height) {
        double radius = Math.min(width, height) * 0.46;
        FxArt.wheel(g, width / 2, height / 2, radius, wheelAngle, winner);
        if (spinning || winner != null) {
            double radians = Math.toRadians(ballAngle);
            double x = width / 2 + radius * .72 * Math.sin(radians);
            double y = height / 2 - radius * .72 * Math.cos(radians);
            g.setFill(Color.WHITE);
            g.fillOval(x - 5, y - 5, 10, 10);
        }
    }

    @Override Node icon() {
        return new FxArt.Picture(116, 100, (g, w, h) -> FxArt.wheel(g, w / 2, h / 2, Math.min(w, h) * .46, 0, null));
    }

    @Override boolean leave() {
        if (!waitForRound(spinning, "Espera a que se detenga la bola.")) return false;
        draft.clear();
        return true;
    }

    private record Cell(Button button, String caption) { }
}
