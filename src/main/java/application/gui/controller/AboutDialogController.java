package application.gui.controller;

import application.config.AppProperties;
import application.gui.BuildFinder;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutDialogController {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    @FXML
    private Label versionLabel;
    @FXML
    private Hyperlink projectLink;

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
        projectLink.setOnAction(e -> mainReference.getHostServices()
                .showDocument("https://github.com/sidola/BuildFinder"));

        versionLabel
                .setText("Version: " + AppProperties.getProperty(AppProperties.VERSION));
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
