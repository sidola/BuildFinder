package application.config;

import java.io.IOException;
import java.util.Properties;

import application.gui.BuildFinder;

public enum AppProperties {

    // @formatter:off
    
    VERSION("version"),
    RELEASES_URL("releases_url"),
    RELEASES_API_URL("releases_api_url");
    
    // @formatter:on

    private final static Properties appProperties = new Properties();
    private final String key;

    static {
        // Load properties file
        try {
            appProperties.load(
                    BuildFinder.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AppProperties(String key) {
        this.key = key;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public static String getProperty(AppProperties property) {
        return appProperties.getProperty(property.key);
    }

    public static int getPropertyAsInt(AppProperties property) {
        return Integer.parseInt(appProperties.getProperty(property.key));
    }

}
