package ui.fx;

import enums.Efecto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelos.Juego;
import modelos.Mesa;
import servicios.Formato;
import servicios.sonido.Sonidos;

import java.util.function.LongConsumer;

abstract class FxCustomerGame extends BorderPane {
    protected final FxPlayContext view;
    protected final HBox headerControls = FxUi.row();
    protected final ComboBox<Mesa> tables = new ComboBox<>();
    protected final Label message = FxUi.label("", "section-title");
    protected final Label limits = FxUi.label("", "hint");
    private final String title;
    private final String description;
    private final Class<? extends Juego> gameType;

    FxCustomerGame(FxPlayContext view, String title, String description, String rules, Class<? extends Juego> gameType) {
        this.view = view;
        this.title = title;
        this.description = description;
        this.gameType = gameType;
        getStyleClass().add("felt");
        if (gameType != null) headerControls.getChildren().addAll(tables, limits);
        headerControls.getChildren().add(FxUi.button("Reglas", "", () -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, rules);
            alert.setTitle("Reglas · " + title);
            alert.setHeaderText(title);
            alert.initOwner(getScene().getWindow());
            alert.getDialogPane().getStylesheets().setAll(getScene().getStylesheets());
            alert.getDialogPane().getStyleClass().addAll("casino", "panel");
            alert.showAndWait();
        }));
        Label caption = FxUi.label(description, "hint");
        caption.setWrapText(true);
        VBox heading = FxUi.column(FxUi.label(title, "title"), caption);
        HBox header = FxUi.row(heading, headerControls);
        HBox.setHgrow(heading, Priority.ALWAYS);
        header.getStyleClass().add("heading");
        setTop(header);
    }

    void enter() {
        if (gameType == null) return;
        Mesa previous = table();
        tables.getItems().setAll(view.casino().getMesas().stream()
                .filter(table -> table.estaDisponible() && view.acceptsTable(table) && gameType.isInstance(table.getJuegoAsignado())).toList());
        tables.setValue(tables.getItems().contains(previous) ? previous : tables.getItems().stream().findFirst().orElse(null));
        refreshLimits();
    }

    protected void refreshLimits() {
        Mesa table = table();
        limits.setText(table == null ? "Sin mesas abiertas" : "Apuesta " + Formato.dinero(table.getApuestaMinima())
                + " a " + Formato.dinero(table.getApuestaMaxima()));
    }

    protected final Mesa table() { return tables.getValue(); }

    protected void footer(Node controls) {
        VBox footer = FxUi.column(message, controls);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8, 16, 12, 16));
        footer.getStyleClass().add("footer");
        setBottom(footer);
    }

    protected static HBox chipSelector(long[] amounts, LongConsumer select) {
        HBox selector = FxUi.row();
        for (long amount : amounts) {
            Button chip = FxUi.button("", "chip", () -> {
                Sonidos.reproducir(Efecto.FICHA);
                select.accept(amount);
            });
            chip.setAccessibleText("Ficha de " + Formato.chips(amount));
            chip.setId("chip-" + amount);
            chip.setGraphic(new FxArt.Picture(45, 45, (g, w, h) -> FxArt.chip(g, w / 2, h / 2, 20, amount)));
            selector.getChildren().add(chip);
        }
        return selector;
    }

    protected static String result(double net) {
        if (net == 0) return "Recuperas tu apuesta";
        return (net > 0 ? "Ganaste " : "Perdiste ") + Formato.dinero(Math.abs(net));
    }

    protected boolean waitForRound(boolean busy, String error) {
        if (busy) view.notice(error, true);
        return !busy;
    }

    final String title() { return title; }
    final String description() { return description; }
    abstract Node icon();
    abstract boolean leave();
}
