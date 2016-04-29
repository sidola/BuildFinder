package application.gui.controller;

import application.config.AppProperties;
import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.gui.BuildFinder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class UpdateDialogController {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    @FXML
    private TextArea changeLogTextArea;
    @FXML
    private CheckBox skipUpdatesCheckBox;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private Stage ownerStage;
    private boolean automaticUpdate;

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public void isAutomaticUpdate(boolean automaticUpdate) {
        this.automaticUpdate = automaticUpdate;

        if (!automaticUpdate) {
            skipUpdatesCheckBox.setVisible(false);
            skipUpdatesCheckBox.setManaged(false);
        }
    }

    public void setChangelog(String changelog) {
        changeLogTextArea.setText(changelog);
    }

    public void setMainReference(BuildFinder mainReference) {
        this.mainReference = mainReference;
    }

    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    private void readCheckboxState() {
        if (!automaticUpdate) {
            return;
        }

        UserPreferences.set(PrefKey.CHECK_FOR_UPDATES, !skipUpdatesCheckBox.isSelected());
    }

    // ----------------------------------------------
    //
    // Event handlers
    //
    // ----------------------------------------------

    @FXML
    private void onYesButton(ActionEvent event) {
        readCheckboxState();

        mainReference.getHostServices()
                .showDocument(AppProperties.getProperty(AppProperties.RELEASES_URL));
        mainReference.closeUpdateDialog();
    }

    @FXML
    private void onNoButton(ActionEvent event) {
        readCheckboxState();
        mainReference.closeUpdateDialog();
    }

}
