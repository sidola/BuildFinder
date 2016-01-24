package application.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;

import application.model.D3Class;

/**
 * Contains configuration data for the application.
 * 
 * @author Sid Botvin
 */
public final class AppConfig {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private String buildType;
    private String buildPatch;
    private String dateRange;
    private String filterType;
    private int pages;
    private Set<D3Class> classes = new HashSet<>();

    private final static File configFile = new File("./data/", "config.json");
    private static AppConfig appConfig;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    private AppConfig() throws Exception {

        // Create a default config
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            configFile.createNewFile();
            FileWriter fileWriter = new FileWriter(configFile);
            System.out.println(createDefaultConfig().toString());
            createDefaultConfig().writeTo(fileWriter, WriterConfig.PRETTY_PRINT);
            fileWriter.close();
        }

        // Load config
        JsonObject loadedConfig = Json.parse(new FileReader(configFile)).asObject();

        try {
            this.buildType = loadedConfig.getString("buildType", "1");
            this.buildPatch = loadedConfig.getString("buildPatch", "1");
            this.dateRange = loadedConfig.getString("dateRange", "1");
            this.filterType = loadedConfig.getString("filterType", "-rating");

            int pageCount = Integer.parseInt(loadedConfig.getString("pages", "1"));

            // Cap page count to 3
            if (pageCount > 3) {
                pageCount = 3;
            }

            this.pages = pageCount;

            JsonArray classesArray = loadedConfig.get("classes").asArray();
            for (JsonValue jsonValue : classesArray) {
                classes.add(D3Class.getById(Integer.parseInt(jsonValue.asString())));
            }

        } catch (UnsupportedOperationException e) {
            throw new Exception("Could not parse config file: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------
    //
    // Instance getter
    //
    // ----------------------------------------------

    /**
     * Returns the loaded {@link AppConfig}.
     * 
     * @throws Exception
     *             If something went wrong when creating an instance of the
     *             {@link AppConfig}.
     */
    public static AppConfig getAppConfig() throws Exception {
        if (appConfig == null) {
            appConfig = new AppConfig();
        }

        return appConfig;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    @Override
    public String toString() {
        return "AppConfig [buildType=" + buildType + ", buildPatch=" + buildPatch
                + ", dateRange=" + dateRange + ", filterType=" + filterType + ", pages="
                + pages + ", classes=" + classes + "]";
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Creates a default config.
     */
    private static JsonObject createDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();

        // filter-build-type
        // 0 - No filter
        // 1 - Regular
        // 2 - Season
        // 3 - Hardcore
        // 4 - Season Hardcore
        defaultConfig.set("buildType", "0");

        // filter-build
        // 0 - No filter
        // 6 - 2.4
        // 5 - 2.3
        defaultConfig.set("buildPatch", "0");

        // filter-build-tag
        // 1 - Hot
        // 2 - New
        // 3 - Week
        // 4 - Month
        // 5 - All time
        defaultConfig.set("dateRange", "4");

        // sort
        // -viewcount
        // -rating
        defaultConfig.set("filterType", "-rating");

        // page count
        // 1 to 3
        defaultConfig.set("pages", "1");

        // classes
        // 2 - Barb
        // 4 - Demon hunter
        // 8 - Witch doctor
        // 16 - Monk
        // 16 - Wizard
        // 32 - Crusader
        JsonArray jsonArray = new JsonArray();
        for (D3Class thisClass : D3Class.values()) {
            jsonArray.add(Integer.toString(thisClass.getClassFilterId()));
        }

        defaultConfig.set("classes", jsonArray);
        return defaultConfig;
    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getBuildPatch() {
        return buildPatch;
    }

    public void setBuildPatch(String buildPatch) {
        this.buildPatch = buildPatch;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public Set<D3Class> getClasses() {
        return classes;
    }

    public void setClasses(Set<D3Class> classes) {
        this.classes = classes;
    }

    public static File getConfigfile() {
        return configFile;
    }

    public static void setAppConfig(AppConfig appConfig) {
        AppConfig.appConfig = appConfig;
    }

}
