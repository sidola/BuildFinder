package application.gui.component;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class StatusBarProgressBar extends HBox {

    private Label progressLabel = new Label("Label...");
    private ProgressBar progressBar = new ProgressBar();
    
    public StatusBarProgressBar() {
        super(10);
               
        progressBar.setPrefWidth(120);
        
        this.getChildren().add(progressLabel);
        this.getChildren().add(progressBar);
        
        HBox.setMargin(progressBar, new Insets(1, 0, 0, 0));
        HBox.setMargin(progressLabel, new Insets(1, 0, 0, 0));
    }
    
    public void setTask(Task<?> task) {
        progressLabel.textProperty().unbind();
        progressLabel.textProperty().bind(task.messageProperty());
        
        progressBar.progressProperty().unbind();        
        progressBar.progressProperty().bind(task.progressProperty());        
    }
    
    public void hide() {
        this.setVisible(false);
    }
    
    public void show() {
        this.setVisible(true);
    }
    
}
