package application.gui.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.controlsfx.control.StatusBar;

import application.BuildDataManager;
import application.Scraper;
import application.Scraper.FetchMode;
import application.gui.BuildFinder;
import application.gui.component.StatusBarProgressBar;
import application.model.BuildInfo;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Controller class for the main view.
 * 
 * @author Sid Botvin
 */
public final class MainController {

    // ----------------------------------------------
    //
    // FX Fields
    //
    // ----------------------------------------------

    private AnchorPane rootPane;

    private TextField itemFilterField = new TextField();
    private Button clearFilterButton = new Button("Clear");

    private ListView<String> itemFilterListView = new ListView<>();

    private TableView<BuildInfo> buildTableView = new TableView<>();
    private TableColumn<BuildInfo, Integer> scoreColumn = new TableColumn<>("Score");
    private TableColumn<BuildInfo, String> classColumn = new TableColumn<>("Class");
    private TableColumn<BuildInfo, Boolean> isCubedColumn = new TableColumn<>("In cube");
    private TableColumn<BuildInfo, String> nameColumn = new TableColumn<>("Name");
    private TableColumn<BuildInfo, Hyperlink> urlColumn = new TableColumn<>("Link");

    private Button refreshBuildsButton = new Button("Refresh builds");
    private Button newBuildsButton = new Button("Fetch new builds");

    // Grab from main
    private StatusBar statusBar;
    private StatusBarProgressBar statusBarProgressBar;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private ObservableList<BuildInfo> tableBuildList = FXCollections
            .observableArrayList();
    private String currentlyFilteredItem  = "";
    
    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    /**
     * Instantiates the controller and grabs a few references from the main
     * application class.
     */
    public MainController(BuildFinder mainReference) {
        this.mainReference = mainReference;

        statusBar = mainReference.getStatusBar();
        statusBarProgressBar = mainReference.getStatusBarProgressBar();
    }

    // ----------------------------------------------
    //
    // View initializer
    //
    // ----------------------------------------------

    /**
     * Initializes the view and returns the root pane.
     */
    public AnchorPane getRootPane() {
        if (rootPane != null) {
            return rootPane;
        }

        // ---------------------------------
        // Configure build tableview & columns
        // ---------------------------------
        setupBuildTableView();

        VBox leftVBox = new VBox(10, buildTableView);
        VBox.setVgrow(buildTableView, Priority.ALWAYS);

        // ---------------------------------
        // Configure filter input, filter list
        // and buttons on the right-side
        // ---------------------------------
        setupFilterModules();

        HBox filterHBox = new HBox(10, itemFilterField, clearFilterButton);
        HBox.setHgrow(clearFilterButton, Priority.ALWAYS);
        HBox.setHgrow(itemFilterField, Priority.ALWAYS);

        VBox rightVBox = new VBox(10, filterHBox, itemFilterListView);
        VBox.setVgrow(itemFilterListView, Priority.ALWAYS);

        // ---------------------------------
        // Configure refresh & new builds buttons
        // ---------------------------------
        setupRefreshBuildButtons();

        HBox buttonHBox = new HBox(10, refreshBuildsButton, newBuildsButton);
        rightVBox.getChildren().add(buttonHBox);

        HBox.setHgrow(refreshBuildsButton, Priority.ALWAYS);
        HBox.setHgrow(newBuildsButton, Priority.ALWAYS);

        // ---------------------------------
        // Put left & right sides into a wrapper
        // and add the wrapper to the root
        // ---------------------------------
        HBox wrapper = new HBox(10, leftVBox, rightVBox);

        HBox.setHgrow(rightVBox, Priority.ALWAYS);
        HBox.setHgrow(leftVBox, Priority.ALWAYS);

        rootPane = new AnchorPane(wrapper);

        AnchorPane.setTopAnchor(wrapper, 10.0);
        AnchorPane.setBottomAnchor(wrapper, 10.0);
        AnchorPane.setLeftAnchor(wrapper, 10.0);
        AnchorPane.setRightAnchor(wrapper, 10.0);

        // ---------------------------------
        // Return the finished view
        // ---------------------------------
        return rootPane;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Sets focus on the filter field.
     */
    public void focusFilterField() {
        itemFilterField.requestFocus();
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Displays any stored builds that contain the given item.
     * 
     * @param item
     *            The name of the item to search for.
     */
    private void displayBuildsForItem(String item) {
        if (currentlyFilteredItem.equals(item)) {
            return;
        }
        
        currentlyFilteredItem = item;
        Set<BuildInfo> matchingBuilds = BuildDataManager.getBuildsWithItem(item);

        tableBuildList.clear();
        tableBuildList.addAll(matchingBuilds);

        scoreColumn.setSortType(SortType.DESCENDING);
        buildTableView.getSortOrder().clear();
        buildTableView.getSortOrder().add(scoreColumn);

        buildTableView.setPlaceholder(new Label("No builds found for " + item));
    }

    /**
     * Builds a {@link Hyperlink} that opens a browser window when clicked.
     */
    private Hyperlink createHyperlink(String linkText, URL targetUrl) {
        Hyperlink hyperLink = new Hyperlink(linkText);

        hyperLink.setOnAction(e -> {
            mainReference.getHostServices().showDocument(targetUrl.toString());
        });

        return hyperLink;
    }

    /**
     * Loads the list of items that can be filtered.
     */
    private List<String> loadFilterListItems() {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("items.txt");

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));

        return bufferedReader.lines().collect(Collectors.toList());
    }

    /**
     * Used to toggle the build buttons based on which button is pressed. The
     * pressed button gets the text 'Cancel', and the other button becomes
     * disabled for the duration of the action.
     * 
     * <p>
     * Reverses the logic if called again
     * </p>
     * 
     * @param pressedButton
     *            The {@link Button} that was pressed.
     */
    private void toggleBuildButtons(Button pressedButton) {
        if (pressedButton == refreshBuildsButton) {

            if (refreshBuildsButton.getText().equals("Cancel")) {
                refreshBuildsButton.setText("Refresh builds");
                newBuildsButton.setDisable(false);
            } else {
                refreshBuildsButton.setText("Cancel");
                newBuildsButton.setDisable(true);
            }

        } else {

            if (newBuildsButton.getText().equals("Cancel")) {
                newBuildsButton.setText("Fetch new builds");
                refreshBuildsButton.setDisable(false);
            } else {
                newBuildsButton.setText("Cancel");
                refreshBuildsButton.setDisable(true);
            }

        }
    }

    // ----------------------------------------------
    //
    // FX Setup methods
    //
    // ----------------------------------------------

    /**
     * Configures the table view used to display builds.
     */
    private void setupBuildTableView() {

        buildTableView.setPrefWidth(620);
        buildTableView.setMaxWidth(Double.MAX_VALUE);
        buildTableView.setItems(tableBuildList);
        buildTableView.getColumns().add(scoreColumn);
        buildTableView.getColumns().add(classColumn);
        buildTableView.getColumns().add(isCubedColumn);
        buildTableView.getColumns().add(urlColumn);
        buildTableView.getColumns().add(nameColumn);

        // Configure columns
        scoreColumn.setStyle("-fx-alignment: CENTER;");
        scoreColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyObjectWrapper<Integer>(
                    cellData.getValue().getBuildScore());
        });

        classColumn.setStyle("-fx-alignment: CENTER;");
        classColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyObjectWrapper<String>(
                    cellData.getValue().getD3Class().toString());
        });

        isCubedColumn.setStyle("-fx-alignment: CENTER;");
        isCubedColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyObjectWrapper<Boolean>(cellData.getValue().getBuildGear()
                    .isCubed(itemFilterListView.getSelectionModel().getSelectedItem()));
        });
        isCubedColumn.setCellFactory(
                new Callback<TableColumn<BuildInfo, Boolean>, TableCell<BuildInfo, Boolean>>() {

                    @Override
                    public TableCell<BuildInfo, Boolean> call(
                            TableColumn<BuildInfo, Boolean> param) {

                        TableCell<BuildInfo, Boolean> cell = new TableCell<BuildInfo, Boolean>() {

                            @Override
                            protected void updateItem(Boolean item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }

                                if (item) {
                                    setText(null);
                                    setGraphic(
                                            new ImageView(new Image(MainController.class
                                                    .getClassLoader().getResourceAsStream(
                                                            "icon/tick.png"))));
                                } else {
                                    setText("No");
                                    setGraphic(null);
                                }

                            }

                        };

                        return cell;
                    }
                });

        nameColumn.setStyle("-fx-alignment: CENTER;");
        nameColumn.setCellValueFactory(cellData -> {
            return new ReadOnlyObjectWrapper<String>(cellData.getValue().getBuildName());
        });

        urlColumn.setStyle("-fx-alignment: CENTER;");
        urlColumn.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<Hyperlink>(
                    createHyperlink("Open", cellData.getValue().getBuildUrl()));
        });

        scoreColumn.prefWidthProperty()
                .bind(buildTableView.widthProperty().multiply(0.10));
        classColumn.prefWidthProperty()
                .bind(buildTableView.widthProperty().multiply(0.15));
        isCubedColumn.prefWidthProperty()
                .bind(buildTableView.widthProperty().multiply(0.10));
        nameColumn.prefWidthProperty()
                .bind(buildTableView.widthProperty().multiply(0.547));
        urlColumn.prefWidthProperty().bind(buildTableView.widthProperty().multiply(0.10));

    }

    /**
     * Configures the {@link TextField} and {@link ListView} on the right side
     * of the view where we can filter items.
     */
    private void setupFilterModules() {
        clearFilterButton.setPrefWidth(70);

        ObservableList<String> itemFilterList = FXCollections.observableArrayList();
        itemFilterList.addAll(loadFilterListItems());

        // Setup list filtering
        FilteredList<String> filteredList = new FilteredList<>(itemFilterList);
        itemFilterListView = new ListView<>(filteredList);
        itemFilterField.textProperty().addListener((observable, oldValue, newValue) -> {

            filteredList.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (item.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });

            itemFilterListView.getSelectionModel().select(0);
            if (itemFilterListView.getSelectionModel().getSelectedItem() != null) {
                displayBuildsForItem(
                        itemFilterListView.getSelectionModel().getSelectedItem());
            }
        });

        itemFilterField.focusedProperty()
                .addListener((observable, newChange, oldChange) -> {

                    if (oldChange) {
                        // Workaround to trigger the selection
                        // when using the mouse
                        // http://stackoverflow.com/a/14966960/1891491
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (itemFilterField.isFocused()
                                        && !itemFilterField.getText().isEmpty()) {
                                    itemFilterField.selectAll();
                                }
                            }
                        });
                    }

                });

        itemFilterListView.setOnKeyReleased(e -> {
            String selectedItem = itemFilterListView.getSelectionModel()
                    .getSelectedItem();
            displayBuildsForItem(selectedItem);
        });

        itemFilterListView.setCellFactory(cellData -> new FilterListCell());

        clearFilterButton.setOnAction(e -> {
            itemFilterField.clear();
        });

    }

    /**
     * Configures the two {@link Button}s on the right side that are used to
     * fetch new builds and update the current builds.
     */
    private void setupRefreshBuildButtons() {
        refreshBuildsButton.setMaxWidth(Double.MAX_VALUE);
        newBuildsButton.setMaxWidth(Double.MAX_VALUE);

        refreshBuildsButton.setOnAction(
                new FetchBuildsHandler(refreshBuildsButton, FetchMode.REFRESH));
        newBuildsButton
                .setOnAction(new FetchBuildsHandler(newBuildsButton, FetchMode.NEW));
    }

    // ----------------------------------------------
    //
    // Inner classes & enums
    //
    // ----------------------------------------------

    /**
     * {@link EventHandler} for the refresh and fetch build buttons.
     */
    public class FetchBuildsHandler implements EventHandler<ActionEvent> {

        private Button button;
        private FetchMode mode;

        public FetchBuildsHandler(Button button, FetchMode mode) {
            this.button = button;
            this.mode = mode;
        }

        @Override
        public void handle(ActionEvent event) {

            // We had a scraper running, let's cancel it
            if (button.getUserData() != null) {
                Scraper scraper = (Scraper) button.getUserData();

                scraper.setOnCancelled(f -> {
                    statusBar.setText(" " + BuildDataManager.getDataInfo());
                    statusBarProgressBar.hide();

                    toggleBuildButtons(button);
                    button.setUserData(null);
                });

                scraper.cancel();
                return;
            }

            toggleBuildButtons(button);

            Scraper scraper = new Scraper(BuildDataManager.getBuildInfoSet(), mode);
            button.setUserData(scraper);

            statusBarProgressBar.setTask(scraper);

            new Thread(scraper).start();
            statusBarProgressBar.show();

            scraper.setOnSucceeded(f -> {
                BuildDataManager.updateLastUpdatedDate();
                BuildDataManager.saveBuilds();

                statusBar.setText(" " + BuildDataManager.getDataInfo());
                statusBarProgressBar.hide();

                toggleBuildButtons(button);
            });

        }

    }

    /**
     * Custom {@link ListCell} implementation for the filter list. Adds an
     * event-handler for mouse-clicks to filter the list.
     */
    public class FilterListCell extends ListCell<String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(item);
            setGraphic(null);

            setOnMouseClicked(e -> {
                if (getItem() == null) {
                    return;
                }

                displayBuildsForItem(item);
            });
        }

    }

}
