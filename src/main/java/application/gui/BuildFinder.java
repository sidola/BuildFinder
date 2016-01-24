package application.gui;

import java.util.Locale;

import org.controlsfx.control.StatusBar;

import application.BuildDataManager;
import application.config.AppConfig;
import application.gui.component.ExceptionDialog;
import application.gui.component.StatusBarProgressBar;
import application.gui.controller.MainController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main application class.
 * 
 * @author Sid Botvin
 */
public class BuildFinder extends Application {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    private StatusBar statusBar = new StatusBar();
    private StatusBarProgressBar statusBarProgressBar = new StatusBarProgressBar();

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private static AppConfig appConfig;
    public HostServices hostServices;
    private MainController mainController = new MainController(this);

    // ----------------------------------------------
    //
    // main
    //
    // ----------------------------------------------

    public static void main(String[] args) {
        BuildDataManager.loadBuilds();
        launch(args);
    }

    // ----------------------------------------------
    //
    // FX Start
    //
    // ----------------------------------------------

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set language
        Locale.setDefault(Locale.ENGLISH);

        // Grab a reference to the host services
        // Used to open the browser
        hostServices = getHostServices();

        // Setup status bar
        statusBar.setText(" " + BuildDataManager.getDataInfo());
        statusBar.setMaxHeight(15);
        statusBar.getRightItems().add(statusBarProgressBar);
        statusBarProgressBar.hide();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainController.getRootPane());
        borderPane.setBottom(statusBar);

        Scene scene = new Scene(borderPane);

        // Load CSS
        String css = BuildFinder.class.getClassLoader().getResource("master.css")
                .toExternalForm();
        scene.getStylesheets().add(css);

        // Bind CTRL+F key-combo to search
        final KeyCodeCombination focusFilterFieldCombo = new KeyCodeCombination(KeyCode.F,
                KeyCombination.CONTROL_DOWN);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (focusFilterFieldCombo.match(event)) {
                    mainController.focusFilterField();
                    event.consume();
                    return;
                }
            }
        });

        primaryStage.setTitle("BuildFinder");
        primaryStage.getIcons().add(new Image(BuildFinder.class.getClassLoader()
                .getResourceAsStream("icon/app_icon.png")));

        primaryStage.setScene(scene);
        primaryStage.show();

        // Load config
        try {
            appConfig = AppConfig.getAppConfig();
        } catch (Exception e) {
            ExceptionDialog exceptionDialog = new ExceptionDialog(AlertType.ERROR,
                    "Could not parse the config file. Make sure you're only using string values."
                            + "\n\nIf you have no clue what you're doing, simply"
                            + " delete the config.json file in the data folder and a new one will be created for you.",
                    e);

            exceptionDialog.initOwner(primaryStage);
            exceptionDialog.showAndWait();
            System.exit(0);
        }
    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

    /**
     * Returns the {@link StatusBar}.
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Returns the {@link StatusBarProgressBar}.
     */
    public StatusBarProgressBar getStatusBarProgressBar() {
        return statusBarProgressBar;
    }

    /**
     * Returns the {@link AppConfig}.
     */
    public static AppConfig getAppConfig() {
        return appConfig;
    }

}
