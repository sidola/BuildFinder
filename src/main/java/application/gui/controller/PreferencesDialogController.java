package application.gui.controller;

import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.gui.BuildFinder;
import application.util.BuildUrlParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class PreferencesDialogController {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    @FXML
    private CheckBox checkForUpdatesCheckBox;
    @FXML
    private TextField fetchUrlField;
    @FXML
    private Spinner<Integer> pageCountSpinner;
    @FXML
    private Button saveButton;
    @FXML
    private Button backButton;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private Stage ownerStage;

    // ----------------------------------------------
    //
    // Initialize
    //
    // ----------------------------------------------

    public void initialize() {

    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public void updateView() {
        fetchUrlField.setText(UserPreferences.get(PrefKey.BUILDS_URL));

        pageCountSpinner.getValueFactory()
                .setValue(UserPreferences.getInteger(PrefKey.PAGE_COUNT));

        checkForUpdatesCheckBox
                .setSelected(UserPreferences.getBoolean(PrefKey.CHECK_FOR_UPDATES));

        fetchUrlField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            disableSaving(false);
        });

        pageCountSpinner.getValueFactory().valueProperty()
                .addListener(new ChangeListener<Integer>() {
                    @Override
                    public void changed(ObservableValue<? extends Integer> observable,
                            Integer oldValue, Integer newValue) {
                        disableSaving(false);
                    }
                });

        checkForUpdatesCheckBox.selectedProperty()
                .addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable,
                            Boolean oldValue, Boolean newValue) {
                        disableSaving(false);
                    }
                });

        disableSaving(true);
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Toggles the state of the save button and text on the back button.
     */
    private void disableSaving(boolean disableSaving) {
        if (disableSaving) {
            saveButton.setDisable(true);
            backButton.setText("Back");
        } else {
            saveButton.setDisable(false);
            backButton.setText("Cancel");
        }
    }

    /**
     * Attempts to save the currently given preferences.
     * 
     * @return true if the preferences were saved, false otherwise.
     */
    private boolean savePreferences() {

        BuildUrlParser buildUrlParser = new BuildUrlParser(fetchUrlField.getText());
        if (!buildUrlParser.isValidUrl()) {

            Alert invalidUrlAlert = new Alert(AlertType.ERROR);
            invalidUrlAlert.initOwner(ownerStage);
            invalidUrlAlert.setTitle("Invalid URL");
            invalidUrlAlert.setHeaderText(null);
            invalidUrlAlert.setContentText("The given URL was not valid. Try again.");
            invalidUrlAlert.showAndWait();

            return false;
        }

        UserPreferences.set(PrefKey.CHECK_FOR_UPDATES,
                checkForUpdatesCheckBox.isSelected());

        UserPreferences.set(PrefKey.BUILDS_URL, fetchUrlField.getText());
        UserPreferences.set(PrefKey.PAGE_COUNT, pageCountSpinner.getValue());

        return true;
    }

    // ----------------------------------------------
    //
    // Event handlers
    //
    // ----------------------------------------------

    @FXML
    private void onSaveButton(ActionEvent event) {
        if (!savePreferences()) {
            return;
        }

        mainReference.closePreferencesDialog();
    }

    @FXML
    private void onBackButton(ActionEvent event) {
        mainReference.closePreferencesDialog();
    }

    // ----------------------------------------------
    //
    // Getters / Setters
    //
    // ----------------------------------------------

    public void setMainReference(BuildFinder mainReference) {
        this.mainReference = mainReference;
    }

    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

}
