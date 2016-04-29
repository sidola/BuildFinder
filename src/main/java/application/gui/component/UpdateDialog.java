package application.gui.component;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class UpdateDialog extends Alert {

    public UpdateDialog(AlertType alertType, String newVersion, String changeLog) {
        super(alertType);

        this.setTitle("A new update is available!");
        this.setHeaderText(null);
        this.setContentText("Version " + newVersion
                + " is available for download.\nDo you want to update now?");

        TextArea textArea = new TextArea(changeLog);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setMaxWidth(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane dialogContent = new GridPane();
        dialogContent.setVgap(10);
        dialogContent.setHgap(10);
        dialogContent.setMaxHeight(Double.MAX_VALUE);
        dialogContent.add(textArea, 0, 0);

        this.getDialogPane().setExpandableContent(dialogContent);
        this.getDialogPane().setExpanded(true);
    }

}
