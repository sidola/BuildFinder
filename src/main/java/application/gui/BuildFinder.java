package application.gui;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.Optional;

import org.controlsfx.control.StatusBar;

import application.BuildDataManager;
import application.UpdateTask;
import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.graphics.IconImage;
import application.gui.component.ExceptionDialog;
import application.gui.component.StatusBarProgressBar;
import application.gui.controller.AboutDialogController;
import application.gui.controller.MainController;
import application.gui.controller.PreferencesDialogController;
import application.gui.controller.SetupDialogController;
import application.gui.controller.UpdateDialogController;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
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

    private Stage primaryStage;
    private Stage setupDialog;
    private Stage preferencesDialog;

    private MainController mainController = new MainController(this);
    private Stage updateDialog;
    private Stage aboutDialog;

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

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {

                ExceptionDialog exceptionDialog = new ExceptionDialog(AlertType.ERROR,
                        "An exception occured.", e);

                exceptionDialog.initOwner(primaryStage);
                exceptionDialog.showAndWait();
                System.exit(0);

            }
        });

        // Set language
        Locale.setDefault(Locale.ENGLISH);

        this.primaryStage = primaryStage;
        setupPrimaryStage(primaryStage);
        primaryStage.show();

        performFirstTimeBoot();
        automaticUpdateCheck();
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Updates the status bar text.
     */
    public void updateStatusBarText() {
        statusBar.setText(" " + BuildDataManager.getDataInfo());
    }

    /**
     * Closes the setup dialog.
     */
    public void closeSetupDialog() {
        if (setupDialog == null) {
            return;
        }

        setupDialog.close();
        setupDialog = null;
    }

    /**
     * CLoses the update dialog.
     */
    public void closeUpdateDialog() {
        if (updateDialog == null) {
            return;
        }

        updateDialog.close();
        updateDialog = null;
    }

    /**
     * CLoses the preferences dialog.
     */
    public void closePreferencesDialog() {
        if (preferencesDialog == null) {
            return;
        }

        preferencesDialog.close();
        preferencesDialog = null;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Performs first time setup. Basically checks if the user provided us with
     * a build URL.
     */
    private void performFirstTimeBoot() {
        if (!UserPreferences.get(PrefKey.BUILDS_URL).isEmpty()) {
            return;
        }

        showSetupDialog();
    }

    /**
     * Sets up the primary {@link Stage} of the application.
     */
    private void setupPrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("BuildFinder");
        primaryStage.getIcons().add(IconImage.APP_ICON.getImage());

        primaryStage.setScene(createMainScene());

        primaryStage.setWidth(900);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(350);
    }

    /**
     * Creates and returns the primary {@link Scene} of the application.
     */
    private Scene createMainScene() {
        // Setup status bar
        updateStatusBarText();
        statusBar.setMaxHeight(15);
        statusBar.getRightItems().add(statusBarProgressBar);
        statusBarProgressBar.hide();

        // Setup a menu bar
        MenuItem preferencesItem = new MenuItem("Preferences");
        MenuItem exitItem = new MenuItem("Exit");

        MenuItem aboutItem = new MenuItem("About");
        MenuItem updateItem = new MenuItem("Check for update");
        MenuItem buildsWebsiteLinkItem = new MenuItem("Open diablofans.com...");

        Menu fileMenu = new Menu("File");
        Menu helpMenu = new Menu("Help");

        fileMenu.getItems().addAll(preferencesItem, exitItem);
        helpMenu.getItems().addAll(updateItem, aboutItem, new SeparatorMenuItem(),
                buildsWebsiteLinkItem);

        MenuBar menuBar = new MenuBar(fileMenu, helpMenu);

        preferencesItem.setOnAction(e -> showPreferencesDialog());
        preferencesItem.setAccelerator(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

        exitItem.setOnAction(e -> System.exit(0));
        exitItem.setAccelerator(
                new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

        aboutItem.setOnAction(e -> showAboutDialog());
        updateItem.setOnAction(e -> manualUpdateCheck());
        buildsWebsiteLinkItem.setOnAction(e -> openDiabloBuildsWebsite());

        // Setup BorderPane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(mainController.getRootPane());
        borderPane.setBottom(statusBar);

        Scene scene = new Scene(borderPane);

        // Load CSS
        String css = getClass().getResource("/master.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Bind CTRL+F key-combo to search
        final KeyCodeCombination focusFilterFieldCombo = new KeyCodeCombination(KeyCode.F,
                KeyCombination.CONTROL_DOWN);

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (focusFilterFieldCombo.match(event)) {
                mainController.focusFilterField();
                event.consume();
                return;
            }
        });

        // Bind DOWN from search to the item list
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {

            if (event.getCode().equals(KeyCode.DOWN)
                    && mainController.isFilterFieldFocused()) {
                mainController.focusItemFilterList();
                event.consume();
                return;
            }

        });

        return scene;
    }

    private void openDiabloBuildsWebsite() {
        getHostServices().showDocument(UserPreferences.get(PrefKey.BUILDS_URL));
    }

    private void manualUpdateCheck() {
        checkForUpdates(false);
    }

    private void showAboutDialog() {
        aboutDialog = new Stage();
        aboutDialog.getIcons().add(IconImage.APP_ICON.getImage());

        aboutDialog.setTitle("Preferences");
        aboutDialog.setResizable(false);
        aboutDialog.sizeToScene();
        aboutDialog.initModality(Modality.APPLICATION_MODAL);
        aboutDialog.initOwner(primaryStage);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AboutDialogView.fxml"));
            loader.load();

            AboutDialogController controller = loader.getController();
            controller.setMainReference(this);
            controller.setOwnerStage(aboutDialog);

            Scene aboutScene = new Scene(loader.getRoot());
            aboutDialog.setScene(aboutScene);
            aboutDialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the about dialog", e);
        }
    }

    private void showPreferencesDialog() {

        preferencesDialog = new Stage();
        preferencesDialog.getIcons().add(IconImage.SETTINGS_ICON.getImage());

        preferencesDialog.setTitle("Preferences");
        preferencesDialog.setResizable(false);
        preferencesDialog.sizeToScene();
        preferencesDialog.initModality(Modality.APPLICATION_MODAL);
        preferencesDialog.initOwner(primaryStage);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PreferencesDialogView.fxml"));
            loader.load();

            PreferencesDialogController controller = loader.getController();
            controller.setMainReference(this);
            controller.setOwnerStage(preferencesDialog);
            controller.updateView();

            Scene preferencesScene = new Scene(loader.getRoot());

            preferencesScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    closePreferencesDialog();
                }
            });

            String css = getClass().getResource("/master.css").toExternalForm();
            preferencesScene.getStylesheets().add(css);

            preferencesDialog.setScene(preferencesScene);
            preferencesDialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the preferences dialog", e);
        }

    }

    private void automaticUpdateCheck() {
        if (!UserPreferences.getBoolean(PrefKey.CHECK_FOR_UPDATES)) {
            return;
        }

        checkForUpdates(true);
    }

    private void checkForUpdates(boolean automaticUpdate) {
        Task<Optional<String>> updateTask = new UpdateTask();

        updateTask.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, e -> {
            ExceptionDialog exceptionDialog = new ExceptionDialog(AlertType.ERROR,
                    "An exception occured.", e.getSource().getException());

            exceptionDialog.initOwner(primaryStage);
            exceptionDialog.showAndWait();
        });

        updateTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
            Optional<String> updateRequired = updateTask.getValue();

            if (updateRequired.isPresent()) {
                showUpdateDialog(updateRequired.get(), automaticUpdate);
            } else {

                if (!automaticUpdate) {
                    Alert noUpdatesAlert = new Alert(AlertType.INFORMATION);
                    noUpdatesAlert.initOwner(primaryStage);
                    noUpdatesAlert.setHeaderText(null);
                    noUpdatesAlert.setTitle("No updates!");
                    noUpdatesAlert.setContentText("You're running the latest version!");
                    noUpdatesAlert.showAndWait();
                }

            }
        });

        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showSetupDialog() {
        setupDialog = new Stage();
        setupDialog.getIcons().add(IconImage.SETTINGS_ICON.getImage());

        setupDialog.setTitle("Setup fetch settings");
        setupDialog.setResizable(false);
        setupDialog.sizeToScene();
        setupDialog.initModality(Modality.APPLICATION_MODAL);
        setupDialog.initOwner(primaryStage);

        setupDialog.setOnCloseRequest(e -> {
            System.exit(0);
        });

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/SetupDialogView.fxml"));
            loader.load();

            SetupDialogController controller = loader.getController();
            controller.setMainReference(this);
            controller.setOwnerStage(setupDialog);

            Scene dialogScene = new Scene(loader.getRoot());
            setupDialog.setScene(dialogScene);
            setupDialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the setup dialog", e);
        }
    }

    private void showUpdateDialog(String changelog, boolean automaticUpdate) {
        updateDialog = new Stage();
        updateDialog.getIcons().add(IconImage.APP_UPDATE_ICON.getImage());

        updateDialog.setTitle("A new version is available!");
        updateDialog.setResizable(true);
        updateDialog.initModality(Modality.APPLICATION_MODAL);
        updateDialog.initOwner(primaryStage);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/UpdateDialogView.fxml"));
            loader.load();

            UpdateDialogController controller = loader.getController();
            controller.setMainReference(this);
            controller.setOwnerStage(updateDialog);
            controller.setChangelog(changelog);
            controller.isAutomaticUpdate(automaticUpdate);

            Scene dialogScene = new Scene(loader.getRoot());
            updateDialog.setScene(dialogScene);
            updateDialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the update dialog", e);
        }
    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

    /**
     * Returns the primary {@link Stage}.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

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

}
