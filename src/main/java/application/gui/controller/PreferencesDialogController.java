package application.gui.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.helper.StringUtil;

import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.gui.BuildFinder;
import application.util.BuildUrlParser;
import application.util.ValidationUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    @FXML
    private VBox urlVBoxWrapper;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private Stage ownerStage;

    private LinkedList<FetchUrlElements> fetchUrlElementsList = new LinkedList<>();
    private ChangeListener<Integer> pageCountSaveStateListener;

    private static final int MAX_ADDITONAL_URLS = 5;

    // ----------------------------------------------
    //
    // Initialize
    //
    // ----------------------------------------------

    public void initialize() {
        pageCountSaveStateListener = new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable,
                    Integer oldValue, Integer newValue) {
                disableSaving(false);
            }
        };

        fetchUrlField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            disableSaving(false);
        });

        pageCountSpinner.getValueFactory().valueProperty()
                .addListener(pageCountSaveStateListener);

        checkForUpdatesCheckBox.selectedProperty()
                .addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable,
                            Boolean oldValue, Boolean newValue) {
                        disableSaving(false);
                    }
                });
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

        loadAdditionalFetchUrls();
        disableSaving(true);
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Loads any additional URLs found in the {@link UserPreferences} and
     * creates elements for them.
     */
    private void loadAdditionalFetchUrls() {

        List<String> additionalBuildUrls = UserPreferences
                .getList(PrefKey.ADDITIONAL_BUILD_URLS);
        List<String> additionalPageCounts = UserPreferences
                .getList(PrefKey.ADDITIONAL_PAGE_COUNTS);

        if (additionalBuildUrls.size() != additionalPageCounts.size()) {
            throw new RuntimeException("The amount of additional URLs did not"
                    + " match the amount of additional page counts,"
                    + "there's something wrong with the preferences file");
        }

        if (additionalBuildUrls.isEmpty() || additionalPageCounts.isEmpty()) {
            createAdditionalFetchUrlElements(null, null);
            return;
        }

        for (int i = 0; i < additionalBuildUrls.size(); i++) {
            createAdditionalFetchUrlElements(additionalBuildUrls.get(i),
                    additionalPageCounts.get(i));
        }

        if (fetchUrlElementsList.size() != MAX_ADDITONAL_URLS) {
            createAdditionalFetchUrlElements(null, null);
        }
    }

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
        if (!arePreferencesValid()) {
            return false;
        }

        UserPreferences.set(PrefKey.CHECK_FOR_UPDATES,
                checkForUpdatesCheckBox.isSelected());

        UserPreferences.set(PrefKey.BUILDS_URL, fetchUrlField.getText());
        UserPreferences.set(PrefKey.PAGE_COUNT, pageCountSpinner.getValue());

        List<String> additionalUrls = new ArrayList<>();
        List<String> additionalPageCounts = new ArrayList<>();

        for (FetchUrlElements fetchUrlElements : fetchUrlElementsList) {
            if (fetchUrlElements.isActive()) {
                additionalUrls.add(fetchUrlElements.getUrlText());
                additionalPageCounts.add(fetchUrlElements.getPageCount());
            }
        }

        String additionalUrlsString = additionalUrls.stream()
                .collect(Collectors.joining(UserPreferences.VALUE_SEPARATOR));

        String additionalPageCountsString = additionalPageCounts.stream()
                .collect(Collectors.joining(UserPreferences.VALUE_SEPARATOR));

        UserPreferences.set(PrefKey.ADDITIONAL_BUILD_URLS, additionalUrlsString);
        UserPreferences.set(PrefKey.ADDITIONAL_PAGE_COUNTS, additionalPageCountsString);

        return true;
    }

    /**
     * Checks if all the preferences are valid. If invalid preferences are
     * found, they're decorated with an error style.
     * 
     * @return true if all preferences are valid, false otherwise.
     */
    private boolean arePreferencesValid() {
        boolean validPreferences = true;

        // First we'll validate the default URL as that always have to be there
        BuildUrlParser buildUrlParser = new BuildUrlParser(fetchUrlField.getText());
        if (!buildUrlParser.isValidUrl()) {
            validPreferences = false;
            ValidationUtils.decorateWithError(fetchUrlField);
        } else {
            ValidationUtils.removeDecorations(fetchUrlField);
        }

        // Next we'll check all additional URLs
        for (FetchUrlElements fetchUrlElements : fetchUrlElementsList) {
            if (fetchUrlElements.isActive()) {

                buildUrlParser = new BuildUrlParser(fetchUrlElements.getUrlText());
                if (!buildUrlParser.isValidUrl()) {
                    validPreferences = false;
                    ValidationUtils
                            .decorateWithError(fetchUrlElements.getFetchUrlTextField());
                } else {
                    ValidationUtils
                            .removeDecorations(fetchUrlElements.getFetchUrlTextField());
                }

            }
        }

        if (!validPreferences) {
            showInvalidUrlAlert();
        }

        return validPreferences;
    }

    /**
     * Shows an alert informing the user one or more URL is invalid and must be
     * fixed.
     */
    private void showInvalidUrlAlert() {
        Alert invalidUrlAlert = new Alert(AlertType.ERROR);
        invalidUrlAlert.initOwner(ownerStage);
        invalidUrlAlert.setTitle("Invalid URL");
        invalidUrlAlert.setHeaderText(null);
        invalidUrlAlert.setContentText("The given URL was not valid. Try again.");
        invalidUrlAlert.showAndWait();
    }

    /**
     * Creates the additional elements required to allow the user to enter
     * multiple URLs. Call this method with <code>null</code> as both params to
     * create a disabled element.
     * 
     * @param url
     *            The URL of the build.
     * @param pageCount
     *            The amount of pages to get.
     */
    private void createAdditionalFetchUrlElements(String url, String pageCount) {

        // -----------------------------------
        // --------- GUI code begins----------
        // -----------------------------------

        Separator separator = new Separator();

        // -----------------------------------
        // Creates the HBox that wraps all elements below
        // -----------------------------------

        HBox hBoxWrapper = new HBox(10);
        hBoxWrapper.setFillHeight(true);

        // -----------------------------------
        // Creates the button used to add/remove additional elements
        // -----------------------------------

        Button toggleBuildButton = new Button("+");

        AnchorPane.setTopAnchor(toggleBuildButton, 0.0);
        AnchorPane.setRightAnchor(toggleBuildButton, 0.0);
        AnchorPane.setBottomAnchor(toggleBuildButton, 0.0);
        AnchorPane.setLeftAnchor(toggleBuildButton, 0.0);

        AnchorPane addBuildButtonPane = new AnchorPane(toggleBuildButton);
        addBuildButtonPane.setPrefWidth(30);
        HBox.setMargin(addBuildButtonPane, new Insets(0, 5, 0, 0));

        // -----------------------------------
        // Creates the URL text field and label
        // -----------------------------------

        VBox urlVBox = new VBox(5);
        HBox.setHgrow(urlVBox, Priority.ALWAYS);

        Label fetchUrlLabel = new Label("Fetch URL");
        TextField fetchUrlTextField = new TextField();
        fetchUrlTextField.setPrefWidth(280);

        urlVBox.getChildren().addAll(fetchUrlLabel, fetchUrlTextField);

        // -----------------------------------
        // Creates the page count spinner and label
        // -----------------------------------

        VBox spinnerVBox = new VBox(5);
        HBox.setHgrow(spinnerVBox, Priority.ALWAYS);

        Label pageCountLabel = new Label("Page Count");
        Spinner<Integer> pageCountSpinner = new Spinner<>(1, 3, 1);
        spinnerVBox.getChildren().addAll(pageCountLabel, pageCountSpinner);

        // -----------------------------------
        // Places all elements in the layout
        // -----------------------------------

        hBoxWrapper.getChildren().addAll(addBuildButtonPane, urlVBox, spinnerVBox);
        urlVBoxWrapper.getChildren().addAll(separator, hBoxWrapper);

        // -----------------------------------
        // ---------- GUI code ends ----------
        // -----------------------------------

        FetchUrlElements fetchUrlElements = new FetchUrlElements(fetchUrlTextField,
                pageCountSpinner);

        if (!StringUtil.isBlank(url) && !StringUtil.isBlank(pageCount)) {

            fetchUrlElements.setActive(true);

            fetchUrlTextField.setText(url);
            pageCountSpinner.getValueFactory().setValue(Integer.parseInt(pageCount));

            toggleBuildButton.setUserData(AddBuildButtonState.REMOVE);
            toggleBuildButton.setText("-");

        } else {

            toggleBuildButton.setUserData(AddBuildButtonState.ADD);
            urlVBox.setDisable(true);
            spinnerVBox.setDisable(true);

        }

        fetchUrlTextField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            disableSaving(false);
        });

        pageCountSpinner.getValueFactory().valueProperty()
                .addListener(pageCountSaveStateListener);

        toggleBuildButton.setOnAction(e -> {
            AddBuildButtonState buttonState = (AddBuildButtonState) toggleBuildButton
                    .getUserData();

            switch (buttonState) {
            case ADD:
                toggleBuildButton.setText("-");
                toggleBuildButton.setUserData(AddBuildButtonState.REMOVE);
                urlVBox.setDisable(false);
                spinnerVBox.setDisable(false);

                fetchUrlTextField.requestFocus();
                fetchUrlElements.setActive(true);

                if (fetchUrlElementsList.size() != MAX_ADDITONAL_URLS) {
                    createAdditionalFetchUrlElements(null, null);
                }

                disableSaving(false);
                break;

            case REMOVE:
                backButton.requestFocus();
                urlVBoxWrapper.getChildren().removeAll(separator, hBoxWrapper);

                if (fetchUrlElementsList.getLast().isActive()) {
                    createAdditionalFetchUrlElements(null, null);
                }

                fetchUrlElementsList.remove(fetchUrlElements);
                disableSaving(false);
                break;
            }

            ownerStage.sizeToScene();
        });

        fetchUrlElementsList.add(fetchUrlElements);
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
    // Inner class / Enum
    //
    // ----------------------------------------------

    /**
     * Encapsulates data about additional URL elements.
     */
    private class FetchUrlElements {

        private final TextField fetchUrlTextField;
        private final Spinner<Integer> pageCountSpinner;
        private boolean active;

        public FetchUrlElements(TextField fetchUrlTextField,
                Spinner<Integer> pageCountSpiner) {
            this.fetchUrlTextField = fetchUrlTextField;
            this.pageCountSpinner = pageCountSpiner;
        }

        public TextField getFetchUrlTextField() {
            return fetchUrlTextField;
        }

        public String getUrlText() {
            return fetchUrlTextField.getText();
        }

        public String getPageCount() {
            return Integer.toString(pageCountSpinner.getValue());
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

    }

    private enum AddBuildButtonState {
        ADD, REMOVE;
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
