package application.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Provides {@link Image} and {@link ImageView} instances of icons used by the
 * application.
 * 
 * @author Sid Botvin
 */
public enum IconImage {

    // @formatter:off
    
    APP_ICON("app_icon"),
    SETTINGS_ICON("settings_icon"),
    STAR_ICON("star"),
    TICK_ICON("tick"),
    APP_UPDATE_ICON("app_update_icon");
    
    // @formatter:on

    private final Image image;

    private IconImage(String iconName) {
        image = new Image(IconImage.class
                .getResourceAsStream(String.format("../../icon/%s.png", iconName)));
    }

    public Image getImage() {
        return image;
    }

    public ImageView getImageView() {
        return new ImageView(image);
    }

}
