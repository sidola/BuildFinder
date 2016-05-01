package application.util;

import java.util.Arrays;

import javafx.scene.Node;

/**
 * This class helps with validation decoration for FX elements.
 * 
 * @author Sid Botvin
 */
public class ValidationUtils {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final static String ERROR_STYLE = "error";

    // ----------------------------------------------
    //
    // Public Static API
    //
    // ----------------------------------------------

    /**
     * Decorates the given {@link Node} with an error border.
     */
    public static void decorateWithError(Node node) {
        if (!node.getStyleClass().contains(ERROR_STYLE)) {
            node.getStyleClass().add(ERROR_STYLE);
        }
    }

    /**
     * Removes all decorations from the given {@link Node}.
     */
    public static void removeDecorations(Node node) {
        node.getStyleClass().removeAll(Arrays.asList(ERROR_STYLE));
    }

}
