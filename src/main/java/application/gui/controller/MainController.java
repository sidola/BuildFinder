package application.gui.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import application.BuildDataManager;
import application.Scraper;
import application.gui.BuildFinder;
import application.gui.component.ExceptionDialog;
import application.gui.component.StatusBarProgressBar;
import application.model.BuildInfo;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
    private TableColumn<BuildInfo, BuildInfo> nameColumn = new TableColumn<>("Name");
    private TableColumn<BuildInfo, Hyperlink> urlColumn = new TableColumn<>("Link");

    private Button updateBuildsButton = new Button("Update builds");
    private Button showFavoriteBuildsButton = new Button("Show favorite builds");

    // Grab from main
    private StatusBarProgressBar statusBarProgressBar;

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private BuildFinder mainReference;
    private ObservableList<BuildInfo> tableBuildList = FXCollections
            .observableArrayList();
    private String currentlyFilteredItem = "";

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
        rightVBox.setPrefWidth(280);
        rightVBox.setMaxWidth(280);
        VBox.setVgrow(itemFilterListView, Priority.ALWAYS);

        // ---------------------------------
        // Configure update & favorite builds button
        // ---------------------------------
        setupBuildsButtons();

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.add(showFavoriteBuildsButton, 0, 0);
        buttonGrid.add(updateBuildsButton, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        buttonGrid.getColumnConstraints().add(col1);
        buttonGrid.getColumnConstraints().add(col2);

        rightVBox.getChildren().add(buttonGrid);

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
     * Toggles the text on the 'Update builds' button.
     */
    private void toggleUpdateButton() {
        if (updateBuildsButton.getText().equals("Cancel")) {
            updateBuildsButton.setText("Update builds");
        } else {
            updateBuildsButton.setText("Cancel");
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

        buildTableView.setRowFactory(tableView -> {
            TableRow<BuildInfo> tableRow = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem openBuild = new MenuItem("Open in browser...");
            MenuItem toggleFavorite = new MenuItem("Toggle favorite");
            MenuItem deleteBuild = new MenuItem("Delete build");

            openBuild.setOnAction(e -> {
                BuildInfo item = tableRow.getItem();
                mainReference.getHostServices()
                        .showDocument(item.getBuildUrl().toString());
            });

            toggleFavorite.setOnAction(e -> {
                BuildInfo item = tableRow.getItem();
                item.setFavorite(!item.isFavorite());
                BuildDataManager.saveBuilds();

                // TODO: I don't know of another way to force-refresh the
                // table-row without making the whole BuildInfo class an
                // observable.
                int selectedIndex = buildTableView.getSelectionModel().getSelectedIndex();
                ObservableList<BuildInfo> items = buildTableView.getItems();
                buildTableView.setItems(null);
                buildTableView.layout();
                buildTableView.setItems(items);
                buildTableView.getSelectionModel().select(selectedIndex);

                scoreColumn.setSortType(SortType.DESCENDING);
                buildTableView.getSortOrder().clear();
                buildTableView.getSortOrder().add(scoreColumn);
            });

            deleteBuild.setOnAction(e -> {
                Alert confirmDeletion = new Alert(AlertType.CONFIRMATION);
                confirmDeletion.initOwner(mainReference.getPrimaryStage());
                confirmDeletion.setHeaderText(null);
                confirmDeletion
                        .setContentText("Are you sure you want to delete this build?");
                Optional<ButtonType> result = confirmDeletion.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    BuildInfo item = tableRow.getItem();

                    buildTableView.getItems().remove(item);
                    BuildDataManager.deleteBuild(item);
                    BuildDataManager.saveBuilds();

                    mainReference.updateStatusBarText();
                }
            });

            contextMenu.getItems().addAll(openBuild, toggleFavorite,
                    new SeparatorMenuItem(), deleteBuild);

            tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty())
                    .then((ContextMenu) null).otherwise(contextMenu)

            );

            return tableRow;
        });

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

            // This handles the edge-case where user enters a valid item name in
            // the filter box, receives builds, then enters an invalid name
            // filter in the box, then tries to make changes to the items in the
            // table via the context menu.

            // In that situation, we'll fall back on the currentlyFilteredItem
            // instead of getting a fresh item from the list.
            String itemName = currentlyFilteredItem;
            if (!itemFilterListView.getSelectionModel().isEmpty()) {
                itemName = itemFilterListView.getSelectionModel().getSelectedItem();
            }

            // TODO: When the user views all their favorite builds, the
            // 'IsCubed' column will default to 'No'. The behavior we want here
            // is either to completely hide the 'IsCubed' column when we're
            // viewing favorites, or set it to '-', not 'No'.

            return new ReadOnlyObjectWrapper<Boolean>(
                    cellData.getValue().getBuildGear().isCubed(itemName));

        });

        isCubedColumn.setCellFactory(c -> new IsCubedTableCell());
        nameColumn.setCellFactory(c -> new NameTableCell());

        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setCellValueFactory(cellData -> {
            return new SimpleObjectProperty<BuildInfo>(cellData.getValue());
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
     * Configures the update & favorite builds button.
     */
    private void setupBuildsButtons() {
        showFavoriteBuildsButton.setMaxWidth(Double.MAX_VALUE);
        showFavoriteBuildsButton.setPrefHeight(30);

        updateBuildsButton.setMaxWidth(Double.MAX_VALUE);
        updateBuildsButton.setPrefHeight(30);

        updateBuildsButton.setOnAction(new FetchBuildsHandler(updateBuildsButton));
        showFavoriteBuildsButton.setOnAction(e -> {
            tableBuildList.clear();

            Set<BuildInfo> favoriteBuilds = BuildDataManager.getFavoriteBuilds();
            if (favoriteBuilds.isEmpty()) {
                buildTableView.setPlaceholder(new Label("No favorite builds found"));
                return;
            }

            tableBuildList.addAll(favoriteBuilds);

            scoreColumn.setSortType(SortType.DESCENDING);
            buildTableView.getSortOrder().clear();
            buildTableView.getSortOrder().add(scoreColumn);
        });
    }

    // ----------------------------------------------
    //
    // Inner classes & enums
    //
    // ----------------------------------------------

    /**
     * {@link EventHandler} for the update builds button.
     */
    public class FetchBuildsHandler implements EventHandler<ActionEvent> {

        // ----------------------------------------------
        //
        // Fields
        //
        // ----------------------------------------------

        private Button button;

        // ----------------------------------------------
        //
        // Constructor
        //
        // ----------------------------------------------

        public FetchBuildsHandler(Button button) {
            this.button = button;
        }

        // ----------------------------------------------
        //
        // Public API
        //
        // ----------------------------------------------

        @Override
        public void handle(ActionEvent event) {

            // We had a scraper running, let's cancel it
            if (button.getUserData() != null) {
                Scraper scraper = (Scraper) button.getUserData();

                scraper.setOnCancelled(f -> {
                    mainReference.updateStatusBarText();
                    statusBarProgressBar.hide();

                    toggleUpdateButton();
                    button.setUserData(null);
                });

                scraper.cancel();
                return;
            }

            Alert confirmFetch = new Alert(AlertType.CONFIRMATION);
            confirmFetch.initOwner(mainReference.getPrimaryStage());
            confirmFetch.setHeaderText(null);
            confirmFetch.setContentText(
                    "This action will overwrite all currently stored builds "
                            + "except for the ones marked as favorites, do you want to continue?");

            Optional<ButtonType> result = confirmFetch.showAndWait();
            if (result.isPresent()) {
                ButtonType resultType = result.get();

                if (resultType == ButtonType.CANCEL) {
                    return;
                }
            }

            toggleUpdateButton();

            Scraper scraper = new Scraper(BuildDataManager.getBuildInfoSet());
            button.setUserData(scraper);

            statusBarProgressBar.setTask(scraper);

            Thread thread = new Thread(scraper, "Scraper thread");
            thread.setDaemon(true);
            thread.start();

            statusBarProgressBar.show();

            scraper.setOnSucceeded(f -> {
                BuildDataManager.updateLastUpdatedDate();
                BuildDataManager.saveBuilds();

                mainReference.updateStatusBarText();
                statusBarProgressBar.hide();

                toggleUpdateButton();
                button.setUserData(null);
            });

            scraper.setOnFailed(f -> {
                ExceptionDialog exceptionDialog = new ExceptionDialog(AlertType.ERROR,
                        "Something broke when parsing the html data. "
                                + "See more details for more information.",
                        f.getSource().getException());

                exceptionDialog.initOwner(mainReference.getPrimaryStage());
                exceptionDialog.showAndWait();
                System.exit(0);
            });

        }

    }

    /**
     * Custom {@link ListCell} implementation for the filter list. Adds an
     * event-handler for mouse-clicks to filter the list.
     */
    public class FilterListCell extends ListCell<String> {

        // ----------------------------------------------
        //
        // Protected API
        //
        // ----------------------------------------------

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

    /**
     * Custom {@link TableCell} implementation for the 'IsCubed' column.
     */
    public class IsCubedTableCell extends TableCell<BuildInfo, Boolean> {

        // ----------------------------------------------
        //
        // Protected API
        //
        // ----------------------------------------------

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
                setGraphic(new ImageView(new Image(MainController.class.getClassLoader()
                        .getResourceAsStream("icon/tick.png"))));
            } else {
                setText("No");
                setGraphic(null);
            }

        }

    }

    /**
     * Custom {@link TableCell} implementation for the 'Name' column.
     */
    public class NameTableCell extends TableCell<BuildInfo, BuildInfo> {

        @Override
        protected void updateItem(BuildInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (item.isFavorite()) {
                setText(item.getBuildName());
                ImageView icon = new ImageView(new Image(MainController.class
                        .getClassLoader().getResourceAsStream("icon/star.png")));
                AnchorPane iconHolder = new AnchorPane(icon);
                AnchorPane.setLeftAnchor(icon, 3.0);

                setGraphic(iconHolder);
            } else {
                setText(" " + item.getBuildName());
                setGraphic(null);
            }
        }

    }

}
