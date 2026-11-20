package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

final class FxUi {
    private FxUi() { }

    static Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    static Button button(String label, String style, Runnable action) {
        Button button = new Button(label);
        if (!style.isEmpty()) button.getStyleClass().add(style);
        button.setOnAction(event -> action.run());
        return button;
    }

    static TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("field");
        return field;
    }

    static DatePicker birthDateField() {
        DatePicker picker = new DatePicker();
        picker.setPromptText("AAAA-MM-DD");
        picker.setConverter(new LocalDateStringConverter(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE));
        return picker;
    }

    static LocalDate birthDate(DatePicker picker) {
        try {
            return LocalDate.parse(picker.getEditor().getText().trim());
        } catch (DateTimeParseException invalid) {
            throw new IllegalArgumentException("Escribe la fecha de nacimiento como AAAA-MM-DD.");
        }
    }

    static HBox row(Node... items) {
        HBox row = new HBox(8, items);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    static VBox column(Node... nodes) {
        return new VBox(9, nodes);
    }

    static VBox panel(Node... nodes) {
        VBox panel = column(nodes);
        panel.getStyleClass().add("panel");
        return panel;
    }

    static GridPane form(String[] captions, Node... inputs) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        for (int index = 0; index < inputs.length; index++) {
            Label caption = new Label(captions[index]);
            caption.setLabelFor(inputs[index]);
            grid.addRow(index, caption, inputs[index]);
            GridPane.setHgrow(inputs[index], Priority.ALWAYS);
            if (inputs[index] instanceof Region region) region.setMaxWidth(Double.MAX_VALUE);
        }
        return grid;
    }

    static <T> void column(TableView<T> table, String heading, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(heading);
        col.setCellValueFactory(row -> new ReadOnlyStringWrapper(value.apply(row.getValue())));
        table.getColumns().add(col);
    }

    static void fill(VBox parent, Region child) {
        VBox.setVgrow(child, Priority.ALWAYS);
        child.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    static VBox page(Node... items) {
        VBox page = column(items);
        page.getStyleClass().add("felt");
        page.setPadding(new Insets(18, 24, 18, 24));
        return page;
    }
}
