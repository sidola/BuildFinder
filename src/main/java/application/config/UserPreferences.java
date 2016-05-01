package application.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.jsoup.helper.StringUtil;

public class UserPreferences {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private static final Properties PREFERENCES = new Properties();
    private static final File PREFERENCES_FILE = new File("./data/", "user.preferences");

    public final static String VALUE_SEPARATOR = ";";

    // ----------------------------------------------
    //
    // Static block
    //
    // ----------------------------------------------

    static {
        try {
            if (!PREFERENCES_FILE.exists()) {

                PREFERENCES.load(
                        UserPreferences.class.getResourceAsStream("/user.preferences"));
                PREFERENCES.store(new FileWriter(PREFERENCES_FILE), null);

            } else {

                FileReader reader = new FileReader(PREFERENCES_FILE);
                PREFERENCES.load(reader);
                reader.close();

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

    public static List<String> getList(PrefKey preferenceKey) {
        String stringList = PREFERENCES.getProperty(preferenceKey.key);

        if (StringUtil.isBlank(stringList)) {
            return Collections.emptyList();
        }

        return Arrays.asList(stringList.split(VALUE_SEPARATOR));
    }

    public static <T> void set(PrefKey preferenceKey, T value) {
        PREFERENCES.setProperty(preferenceKey.key, value.toString());
        save();
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(PREFERENCES_FILE);
            PREFERENCES.store(writer, null);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user preferences", e);
        }
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
        CHECK_FOR_UPDATES("check_for_updates"),
        
        ADDITIONAL_BUILD_URLS("additional_build_urls"),
        ADDITIONAL_PAGE_COUNTS("additional_page_counts");
        
        // @formatter:on

        private final String key;

        private PrefKey(String key) {
            this.key = key;
        }
    }

}
