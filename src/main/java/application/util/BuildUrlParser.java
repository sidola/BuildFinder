package application.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Returns a set of unique {@link D3Class} IDs for each class to fetch. If
     * no classes are specified in the URL, a list of all classes will be
     * returned.
     */
    public Set<Integer> extractClassesToFetch() {
        Set<Integer> classesToFetch = new HashSet<>();
        Map<String, String> optionsMap = getOptions();

        int[] classIdSet = new int[] { 2, 4, 8, 16, 32, 64 };
        if (optionsMap.containsKey("filter-class")) {
            int classSum = Integer.parseInt(optionsMap.get("filter-class"));
            classesToFetch.addAll(MathUtil.getValuesFromSum(classSum, classIdSet));
        } else {
            // We'll get all classes if no class was specified by the user
            classesToFetch.addAll(
                    Arrays.stream(classIdSet).boxed().collect(Collectors.toList()));
        }

        return classesToFetch;
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
