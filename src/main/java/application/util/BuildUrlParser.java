package application.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class to help parse a builds URL and extract the options in it.
 * 
 * @author Sid Botvin
 */
public class BuildUrlParser {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final String USER_URL;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    /**
     * Creates a new instance based off of the given URL.
     */
    public BuildUrlParser(String url) {
        this.USER_URL = url;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Parses the given URL and extracts the fetch options from it.
     * 
     * @return A {@link Map} containing the options and their values.
     */
    public Map<String, String> getOptions() {
        String[] optionPairs = USER_URL.split("\\?")[1].split("&");

        Map<String, String> optionsMap = new HashMap<>();
        for (String thisOption : optionPairs) {
            String[] keyValuePair = thisOption.split("=");
            optionsMap.put(keyValuePair[0], keyValuePair[1]);
        }

        return optionsMap;
    }

    /**
     * Returns parsed URL without the class param.
     * 
     * <p>
     * Using the URL:
     * <pre>http://www.diablofans.com/builds?filter-has-spell-2=-1&filter-build-tag=3
     * &filter-class=2</pre> 
     * 
     * Would return:
     * <pre>http://www.diablofans.com/builds?filter-has-spell-2=-1&filter-build-tag=3</pre>
     * </p>
     * 
     */
    public String getFetchUrlWithoutClasses() {
        return USER_URL.replaceAll("&filter-class=\\d+", "");
    }

    /**
     * Checks if the given URL is valid.
     */
    public boolean isValidUrl() {
        if (!USER_URL.contains("diablofans.com/builds")) {
            return false;
        }

        String[] urlParts = USER_URL.split("\\?");

        if (urlParts.length < 2) {
            return false;
        }

        return true;
    }

}
