package application.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class UserPreferences {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private static final Properties PREFERENCES = new Properties();
    private static final File PREFERENCES_FILE = new File("./data/", "user.preferences");

    // ----------------------------------------------
    //
    // Static block
    //
    // ----------------------------------------------

    static {
        try {
            if (!PREFERENCES_FILE.exists()) {

                PREFERENCES.load(UserPreferences.class
                        .getResourceAsStream("../../user.preferences"));
                PREFERENCES.store(new FileWriter(PREFERENCES_FILE), null);

            } else {

                PREFERENCES.load(new FileReader(PREFERENCES_FILE));

            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user preferences", e);
        }
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public static String get(PrefKey preferenceKey) {
        return PREFERENCES.getProperty(preferenceKey.key);
    }

    public static int getInteger(PrefKey preferenceKey) {
        return Integer.parseInt(PREFERENCES.getProperty(preferenceKey.key));
    }

    public static boolean getBoolean(PrefKey preferenceKey) {
        return Boolean.parseBoolean(PREFERENCES.getProperty(preferenceKey.key));
    }

    public static <T> void set(PrefKey preferenceKey, T value) {
        PREFERENCES.setProperty(preferenceKey.key, value.toString());
        save();
    }

    public static void save() {
        try {
            PREFERENCES.store(new FileWriter(PREFERENCES_FILE), null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user preferences", e);
        }
    }

    /**
     * Debugging
     */
    @Deprecated
    public static String peek() {
        return PREFERENCES.toString();
    }

    // ----------------------------------------------
    //
    // Enum
    //
    // ----------------------------------------------

    public enum PrefKey {

        // @formatter:off
        
        BUILDS_URL("builds_url"),
        PAGE_COUNT("page_count"),
        CHECK_FOR_UPDATES("check_for_updates");
        
        // @formatter:on

        private final String key;

        private PrefKey(String key) {
            this.key = key;
        }
    }

}
