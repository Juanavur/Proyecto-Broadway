package ui.fx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;
import javafx.scene.shape.ArcType;

final class FxArt {
    static final int[] WHEEL_ORDER = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5,
            24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};
    static final String[] SYMBOLS = {"🍒", "🍋", "🍊", "🟣", "🔔", "7"};
    static final Color GOLD = Color.web("#d4af37");
    static final Color CREAM = Color.web("#f2dc8f");

    private FxArt() { }

    @FunctionalInterface
    interface Painter {
        void paint(GraphicsContext graphics, double width, double height);
    }

    static final class Picture extends Region {
        private final Canvas canvas = new Canvas();
        private final Painter painter;

        Picture(double width, double height, Painter painter) {
            this.painter = painter;
            setPrefSize(width, height);
            getChildren().add(canvas);
        }

        @Override
        protected void layoutChildren() {
            canvas.setWidth(getWidth());
            canvas.setHeight(getHeight());
            redraw();
        }

        void redraw() {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            if (width <= 0 || height <= 0) return;
            GraphicsContext graphics = canvas.getGraphicsContext2D();
            graphics.clearRect(0, 0, width, height);
            painter.paint(graphics, width, height);
        }
    }

    static void text(GraphicsContext graphics, String text, double x, double y, double size, Color color) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(Font.font("Serif", FontWeight.BOLD, size));
        graphics.setFill(color);
        graphics.fillText(text, x, y);
    }

    static void chip(GraphicsContext g, double x, double y, double radius, long amount) {
        Color color = amount >= 500_000 ? Color.web("#6a2c91") : amount >= 100_000 ? Color.web("#1c1c1c")
                : amount >= 25_000 ? Color.web("#1d8a4e") : amount >= 10_000 ? Color.web("#1f5fb8")
                : amount >= 5_000 ? Color.web("#c0272d") : Color.web("#e9e4d4");
        g.setFill(Color.rgb(0, 0, 0, 0.45));
        g.fillOval(x - radius + 1, y - radius + 3, radius * 2, radius * 2);
        g.setFill(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setStroke(amount < 5000 ? Color.web("#315fae") : Color.WHITE);
        g.setLineWidth(radius * 0.20);
        for (int index = 0; index < 6; index++) {
            double inset = radius * 0.88;
            g.strokeArc(x - inset, y - inset, 2 * inset, 2 * inset, 60 * index + 10, 22, ArcType.OPEN);
        }
        g.setStroke(CREAM);
        g.setLineWidth(1);
        g.strokeOval(x - radius * 0.65, y - radius * 0.65, radius * 1.3, radius * 1.3);
        String label = amount >= 1000 && amount % 1000 == 0 ? amount / 1000 + "K" : Long.toString(amount);
        text(g, label, x, y, radius * 0.67, amount < 5000 ? Color.web("#222222") : Color.WHITE);
    }

    static void card(GraphicsContext g, int rank, int suit, double x, double y, double width, double height) {
        g.setFill(Color.rgb(0, 0, 0, 0.34));
        g.fillRoundRect(x + 3, y + 4, width, height, 12, 12);
        g.setFill(rank == 0 ? Color.web("#7a1f2b") : Color.WHITE);
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setStroke(rank == 0 ? GOLD : Color.LIGHTGRAY);
        g.strokeRoundRect(x, y, width, height, 12, 12);
        if (rank == 0) {
            text(g, "B", x + width / 2, y + height / 2, width * 0.45, GOLD);
        } else {
            Color color = suit % 2 == 1 ? Color.web("#c81e2b") : Color.web("#141414");
            String value = " A23456789TJQK".substring(rank, rank + 1).replace("T", "10");
            String symbol = "♠♥♣♦".substring(suit, suit + 1);
            text(g, value, x + width * 0.23, y + height * 0.17, width * 0.27, color);
            text(g, symbol, x + width / 2, y + height * 0.59, width * 0.56, color);
        }
    }

    static void wheel(GraphicsContext g, double x, double y, double radius, double angle, Integer winner) {
        g.setFill(Color.web("#5d3a1a"));
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setStroke(GOLD);
        g.setLineWidth(Math.max(1, radius * .025));
        g.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        double outer = radius * 0.83;
        double step = 360.0 / WHEEL_ORDER.length;
        for (int index = 0; index < WHEEL_ORDER.length; index++) {
            int value = WHEEL_ORDER[index];
            double rotation = angle + index * step;
            g.setFill(numberColor(value));
            double start = 90 - rotation - step / 2;
            g.fillArc(x - outer, y - outer, outer * 2, outer * 2, start, step, ArcType.ROUND);
            if (winner != null && winner == value) {
                g.setFill(Color.rgb(255, 215, 118, 0.55));
                g.fillArc(x - outer, y - outer, outer * 2, outer * 2, start, step, ArcType.ROUND);
            }
            g.setStroke(GOLD);
            g.setLineWidth(.65);
            g.strokeArc(x - outer, y - outer, outer * 2, outer * 2, start, step, ArcType.ROUND);
            g.save();
            g.translate(x, y);
            g.rotate(rotation);
            text(g, Integer.toString(value), 0, -radius * 0.71, Math.max(7, radius * 0.095), Color.WHITE);
            g.restore();
        }
        double inner = radius * .56;
        g.setFill(Color.web("#513920"));
        g.fillOval(x - inner, y - inner, inner * 2, inner * 2);
        g.setStroke(GOLD);
        g.setLineWidth(Math.max(1, radius * .025));
        g.strokeOval(x - inner, y - inner, inner * 2, inner * 2);
        g.strokeLine(x - inner * .83, y, x + inner * .83, y);
        g.strokeLine(x, y - inner * .83, x, y + inner * .83);
        g.setFill(CREAM);
        g.fillOval(x - radius * .12, y - radius * .12, radius * .24, radius * .24);
    }

    static Color numberColor(int number) {
        return number == 0 ? Color.web("#13834f") : modelos.Ruleta.esRojo(number) ? Color.web("#c0272d") : Color.web("#1b1b1b");
    }

    static void symbol(GraphicsContext g, int index, double x, double y, double size) {
        g.save();
        g.translate(x, y);
        g.scale(size, size);
        if (index == 0) {
            g.setStroke(Color.web("#235c2d"));
            g.setLineWidth(.055);
            g.strokeLine(-.17, .12, .05, -.35);
            g.strokeLine(.14, .12, .05, -.35);
            g.setFill(Color.web("#246f35"));
            g.fillOval(0, -.4, .31, .10);
            fruit(g, -.32, -.01, .3, .32, "#ff334b", "#8b0325");
            fruit(g, .03, -.02, .3, .32, "#ec2544", "#840524");
        } else if (index == 1) {
            fruit(g, -.43, -.24, .86, .48, "#fff48a", "#ed9618");
        } else if (index == 2 || index == 3) {
            fruit(g, -.34, -.33, .68, .66, index == 2 ? "#ffd744" : "#b692ee", index == 2 ? "#d47705" : "#573593");
            if (index == 2) {
                g.setFill(Color.web("#357f3e"));
                g.fillOval(-.04, -.39, .20, .11);
            }
        } else if (index == 4) {
            g.setFill(Color.web("#c68913"));
            g.fillOval(-.09, .21, .18, .17);
            g.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.web("#a07511")), new javafx.scene.paint.Stop(.46, Color.web("#ffe75d")),
                    new javafx.scene.paint.Stop(1, Color.web("#b88512"))));
            g.beginPath();
            g.moveTo(-.37, .23);
            g.bezierCurveTo(-.21, .12, -.27, -.37, 0, -.38);
            g.bezierCurveTo(.27, -.37, .21, .12, .37, .23);
            g.quadraticCurveTo(0, .31, -.37, .23);
            g.closePath();
            g.fill();
        } else {
            g.setTextBaseline(VPos.CENTER);
            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("Serif", FontWeight.BOLD, .88));
            g.setStroke(GOLD);
            g.setLineWidth(.07);
            g.strokeText("7", 0, 0);
            g.setFill(Color.web("#c0272d"));
            g.fillText("7", 0, 0);
        }
        g.restore();
    }

    private static void fruit(GraphicsContext g, double x, double y, double width, double height, String light, String dark) {
        g.setFill(new javafx.scene.paint.RadialGradient(0, 0, .34, .22, .8, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web(light)), new javafx.scene.paint.Stop(1, Color.web(dark))));
        g.fillOval(x, y, width, height);
    }
}
