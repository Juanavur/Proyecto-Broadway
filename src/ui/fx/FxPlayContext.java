package ui.fx;

import modelos.Casino;
import modelos.Cliente;
import modelos.Mesa;

interface FxPlayContext {
    Casino casino();
    Cliente customer();
    void refresh();
    void notice(String text, boolean error);

    default boolean acceptsTable(Mesa table) { return true; }
    default boolean dealerMode() { return false; }

    default boolean attempt(Runnable action) {
        try {
            action.run();
            return true;
        } catch (IllegalArgumentException | IllegalStateException failure) {
            notice(failure.getMessage(), true);
            return false;
        }
    }
}
