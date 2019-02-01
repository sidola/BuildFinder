package application.util;

import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import application.model.D3Class;

import static org.junit.Assert.*;

public class BuildUrlParserTest {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final String VALID_URL_NO_FILTERS = "http://www.diablofans.com/builds?filter-build-tag=5&filter-has-spell-2=-1";
    private final String VALID_URL_BARB_PATCH_2_4 = "http://www.diablofans.com/builds?filter-build=7&filter-has-spell-2=-1&filter-build-tag=5&filter-class=2";

    /**
     * Fetches builds for barb, demon hunter, monk and witch doctor
     */
    private final String VALID_URL_MULTIPLE_CLASSES_PATCH_2_4 = "http://www.diablofans.com/builds?filter-build=6&filter-has-spell-2=-1&filter-build-tag=5&filter-class=30";

    private final String INVALID_URL_TYPO = "http://www.diabloans.com/builds?filter-build-tag=5&filter-has-spell-2=-1&sort=-viewcount";
    private final String INVALID_URL_NO_FILTERS = "http://www.diablofans.com/builds?";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public BuildUrlParser buildUrlParser;

    // ----------------------------------------------
    //
    // Public API Tests
    //
    // ----------------------------------------------

    @Test
    public void testIsValid() {
        buildUrlParser = new BuildUrlParser(INVALID_URL_TYPO);
        assertEquals(buildUrlParser.isValidUrl(), false);

        buildUrlParser = new BuildUrlParser(INVALID_URL_NO_FILTERS);
        assertEquals(buildUrlParser.isValidUrl(), false);

        buildUrlParser = new BuildUrlParser(VALID_URL_NO_FILTERS);
        assertEquals(buildUrlParser.isValidUrl(), true);

        buildUrlParser = new BuildUrlParser(VALID_URL_BARB_PATCH_2_4);
        assertEquals(buildUrlParser.isValidUrl(), true);

        buildUrlParser = new BuildUrlParser(VALID_URL_MULTIPLE_CLASSES_PATCH_2_4);
        assertEquals(buildUrlParser.isValidUrl(), true);
    }

    @Test
    public void testGetOptionsInvalidUrl() {
        buildUrlParser = new BuildUrlParser(INVALID_URL_NO_FILTERS);

        thrown.expect(IllegalStateException.class);
        buildUrlParser.getOptions();
    }

    @Test
    public void testGetOptionsValidUrl() {
        buildUrlParser = new BuildUrlParser(VALID_URL_NO_FILTERS);
        Map<String, String> options = buildUrlParser.getOptions();

        assertEquals(options.size(), 2);
        assertEquals(options.get("filter-build-tag"), "5");
        assertEquals(options.get("filter-has-spell-2"), "-1");
        
        // Perform multiple calls to confirm caching is working
        options = buildUrlParser.getOptions();

        assertEquals(options.size(), 2);
        assertEquals(options.get("filter-build-tag"), "5");
        assertEquals(options.get("filter-has-spell-2"), "-1");
        
    }

    @Test
    public void testExtractClassesInvalidUrl() {
        buildUrlParser = new BuildUrlParser(INVALID_URL_TYPO);

        thrown.expect(IllegalStateException.class);
        buildUrlParser.extractClassesToFetch();
    }

    @Test
    public void testExtractClassesValidUrl() {
        buildUrlParser = new BuildUrlParser(VALID_URL_MULTIPLE_CLASSES_PATCH_2_4);
        Set<Integer> classesToFetch = buildUrlParser.extractClassesToFetch();

        assertTrue(classesToFetch.contains(D3Class.BARBARIAN.getClassFilterId()));
        assertTrue(classesToFetch.contains(D3Class.DEMON_HUNTER.getClassFilterId()));
        assertTrue(classesToFetch.contains(D3Class.MONK.getClassFilterId()));
        assertTrue(classesToFetch.contains(D3Class.WITCH_DOCTOR.getClassFilterId()));

        buildUrlParser = new BuildUrlParser(VALID_URL_BARB_PATCH_2_4);
        classesToFetch = buildUrlParser.extractClassesToFetch();

        assertTrue(classesToFetch.contains(D3Class.BARBARIAN.getClassFilterId()));
        assertFalse(classesToFetch.contains(D3Class.DEMON_HUNTER.getClassFilterId()));
        
        // Perform multiple calls to confirm caching is working
        classesToFetch = buildUrlParser.extractClassesToFetch();

        assertTrue(classesToFetch.contains(D3Class.BARBARIAN.getClassFilterId()));
        assertFalse(classesToFetch.contains(D3Class.DEMON_HUNTER.getClassFilterId()));
    }

    @Test
    public void testGetFetchUrlWithoutClassesInvalidUrl() {
        buildUrlParser = new BuildUrlParser(INVALID_URL_NO_FILTERS);
        thrown.expect(IllegalStateException.class);
        buildUrlParser.getFetchUrlWithoutClasses();
    }

    @Test
    public void testGetFetchUrlWithoutClassesValidUrl() {
        buildUrlParser = new BuildUrlParser(VALID_URL_BARB_PATCH_2_4);
        String fetchUrlWithoutClasses = buildUrlParser.getFetchUrlWithoutClasses();

        assertTrue(fetchUrlWithoutClasses.equals(
                "http://www.diablofans.com/builds?filter-build=7&filter-has-spell-2=-1&filter-build-tag=5"));
    }

}
