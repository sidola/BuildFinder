package application.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.helper.StringUtil;

import application.model.D3Class;

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

    private final static int[] classIdSets = new int[] { 2, 4, 8, 16, 32, 64 };

    private final String USER_URL;
    private final boolean VALID_URL;

    private Map<String, String> optionsMap;
    private Set<Integer> classesToFetch;
    private String USER_URL_NO_CLASSES;

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
        VALID_URL = validateUrl(url);
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Parses the given URL and extracts the fetch options from it.
     * 
     * @return An unmodifiable {@link Map} containing the options and their
     *         values.
     */
    public Map<String, String> getOptions() {
        if (!isValidUrl()) {
            throw new IllegalStateException("The given URL is not valid");
        }

        if (optionsMap == null) {
            String[] optionPairs = USER_URL.split("\\?")[1].split("&");

            optionsMap = new HashMap<>();
            for (String thisOption : optionPairs) {
                // This can happen if we hit double ampersands with nothing in
                // between
                if (StringUtil.isBlank(thisOption)) {
                    continue;
                }

                String[] keyValuePair = thisOption.split("=");
                optionsMap.put(keyValuePair[0], keyValuePair[1]);
            }
        }

        return Collections.unmodifiableMap(optionsMap);
    }

    /**
     * Returns an unmodifiable set of unique {@link D3Class} IDs for each class
     * to fetch. If no classes are specified in the URL, a list of all classes
     * will be returned.
     */
    public Set<Integer> extractClassesToFetch() {
        if (!isValidUrl()) {
            throw new IllegalStateException("The given URL is not valid");
        }

        if (classesToFetch == null) {
            classesToFetch = new HashSet<>();
            Map<String, String> optionsMap = getOptions();

            if (optionsMap.containsKey("filter-class")) {
                int classSum = Integer.parseInt(optionsMap.get("filter-class"));
                classesToFetch.addAll(MathUtil.getValuesFromSum(classSum, classIdSets));
            } else {
                // We'll get all classes if no class was specified by the user
                classesToFetch.addAll(
                        Arrays.stream(classIdSets).boxed().collect(Collectors.toList()));
            }
        }

        return Collections.unmodifiableSet(classesToFetch);
    }

    /**
     * Returns parsed URL without the class param.
     * 
     * <p>
     * Using the URL:
     * 
     * <pre>
     * http://www.diablofans.com/builds?filter-has-spell-2=-1&filter-build-tag=3
     * &filter-class=2
     * </pre>
     * 
     * Would return:
     * 
     * <pre>
     * http://www.diablofans.com/builds?filter-has-spell-2=-1&filter-build-tag=3
     * </pre>
     * </p>
     * 
     */
    public String getFetchUrlWithoutClasses() {
        if (!isValidUrl()) {
            throw new IllegalStateException("The given URL is not valid");
        }

        if (StringUtil.isBlank(USER_URL_NO_CLASSES)) {
            USER_URL_NO_CLASSES = USER_URL.replaceAll("&filter-class=\\d+", "");
        }

        return USER_URL_NO_CLASSES;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Validates the given URL.
     * 
     * @return true if the URL is valid, false otherwise.
     */
    private boolean validateUrl(String url) {
        if (!url.contains("diablofans.com/builds")) {
            return false;
        }

        String[] urlParts = url.split("\\?");

        if (urlParts.length < 2) {
            return false;
        }

        return true;
    }

    // ----------------------------------------------
    //
    // Getters
    //
    // ----------------------------------------------

    public boolean isValidUrl() {
        return VALID_URL;
    }

}
