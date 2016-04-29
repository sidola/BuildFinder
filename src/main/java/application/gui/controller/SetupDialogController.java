package application.gui.controller;

import org.controlsfx.control.HyperlinkLabel;

import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.gui.BuildFinder;
import application.util.BuildUrlParser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SetupDialogController {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    @FXML
    private HyperlinkLabel diabloBuildsLink;
    @FXML
    private TextField fetchUrlField;
    @FXML
    private Spinner<Integer> pageCountSpinner;
    @FXML
    private Button continueButton;
    @FXML
    private Button cancelButton;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private Stage ownerStage;

    private final String DIABLO_BUILDS_URL = "http://www.diablofans.com/builds";
    private BuildUrlParser buildUrlParser;

    // ----------------------------------------------
    //
    // initialize
    //
    // ----------------------------------------------

    public void initialize() {
        diabloBuildsLink.setOnAction(e -> {
            mainReference.getHostServices().showDocument(DIABLO_BUILDS_URL);
        });

        continueButton.setOnAction(e -> {
            String buildUrl = fetchUrlField.getText();
            Integer pageCount = pageCountSpinner.getValue();

            buildUrlParser = new BuildUrlParser(buildUrl);

            if (!buildUrlParser.isValidUrl()) {
                Alert invalidUrlAlert = new Alert(AlertType.ERROR);
                invalidUrlAlert.initOwner(ownerStage);
                invalidUrlAlert.setTitle("Invalid URL");
                invalidUrlAlert.setHeaderText(null);
                invalidUrlAlert.setContentText("The given URL was not valid. Try again.");
                invalidUrlAlert.showAndWait();
            } else {
                UserPreferences.set(PrefKey.BUILDS_URL, buildUrl);
                UserPreferences.set(PrefKey.PAGE_COUNT, pageCount);

                mainReference.closeSetupDialog();
            }

        });

        cancelButton.setOnAction(e -> {
            System.exit(0);
        });
    }

    // ----------------------------------------------
    //
    // Getters / Setters
    //
    // ----------------------------------------------

    public void setMainReference(BuildFinder mainReference) {
        this.mainReference = mainReference;
    }

    public void setOwnerStage(Stage setupDialog) {
        this.ownerStage = setupDialog;
    }

}
