package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import application.config.AppConfig;
import application.gui.BuildFinder;
import application.model.BuildGear;
import application.model.BuildInfo;
import application.model.D3Class;
import javafx.concurrent.Task;

/**
 * Takes care of scraping HTML information and extracting the relevant data.
 * 
 * <p>
 * <b>Note:</b> Has a static block that loads the configuration file for how
 * scraping should be performed.
 * </p>
 * 
 * @author Sid Botvin
 */
public final class Scraper extends Task<Boolean> {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private Set<BuildInfo> buildInfoSet;

    private final static String BASELINE_URL = "http://www.diablofans.com";
    private final static AppConfig configuration;
    private final static String FETCH_URL;

    // ----------------------------------------------
    //
    // Static block
    //
    // ----------------------------------------------

    static {
        configuration = BuildFinder.getAppConfig();

        StringBuilder fetchUrlBuilder = new StringBuilder();
        fetchUrlBuilder.append("http://www.diablofans.com/builds?");

        if (!configuration.getBuildType().equals("0")) {
            fetchUrlBuilder.append("filter-build-type=" + configuration.getBuildType());
        }

        if (!configuration.getBuildPatch().equals("0")) {
            fetchUrlBuilder.append("&filter-build=" + configuration.getBuildPatch());
        }

        fetchUrlBuilder.append("&filter-build-tag=" + configuration.getDateRange());
        fetchUrlBuilder.append("&sort=" + configuration.getFilterType());

        FETCH_URL = fetchUrlBuilder.toString();
    }

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    /**
     * Creates a new instance around the given {@link BuildInfo} set.
     * 
     * @param buildInfoSet
     *            The set of {@link BuildInfo}s to fill with scraped data.
     */
    public Scraper(Set<BuildInfo> buildInfoSet) {
        this.buildInfoSet = buildInfoSet;
    }

    // ----------------------------------------------
    //
    // Protected API
    //
    // ----------------------------------------------

    @Override
    protected Boolean call() throws Exception {
        fetchNewBuilds();
        return true;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Extracts all the build-data from the given URL.
     */
    private Set<BuildInfo> getBuilds(String url) {
        Set<BuildInfo> buildSet = new HashSet<>();

        Document document = getDocument(url);
        buildSet.addAll(extractBuildInfo(document));

        long workDone = 1;
        updateProgress(workDone, buildSet.size());
        for (BuildInfo buildInfo : buildSet) {
            if (isCancelled()) {
                break;
            }

            updateMessage("Downloading build " + workDone + " of " + buildSet.size());
            processBuildInfo(buildInfo);

            updateProgress(workDone, buildSet.size());
            workDone++;
        }

        return buildSet;
    }

    /**
     * Fetches new builds and replaces all currently stored builds.
     */
    private void fetchNewBuilds() {
        Set<BuildInfo> builds = new HashSet<BuildInfo>();

        for (D3Class thisClass : D3Class.values()) {
            if (isCancelled()) {
                break;
            }

            // Skip any classes that aren't found in the config
            if (!configuration.getClasses().contains(thisClass)) {
                continue;
            }

            updateProgress(0, 1);
            int id = thisClass.getClassFilterId();

            String currentFetchUrl = FETCH_URL;
            currentFetchUrl += "&filter-class=" + id;

            if (configuration.getPages() == 1) {
                updateMessage("Fetching " + thisClass.toString() + " builds");
                builds.addAll(getBuilds(currentFetchUrl));
            } else {
                currentFetchUrl += "&page=";
                for (int i = 1; i <= configuration.getPages(); i++) {
                    String statusMessage = "Fetching " + thisClass.toString()
                            + " builds, page " + i + " of " + configuration.getPages();
                    updateMessage(statusMessage);

                    builds.addAll(getBuilds(currentFetchUrl + i));
                }
            }

        }

        // Only overwrite the stored builds if we finished normally
        if (!isCancelled()) {
            Set<BuildInfo> favoriteBuilds = BuildDataManager.getFavoriteBuilds();

            buildInfoSet.clear();
            buildInfoSet.addAll(builds);
            buildInfoSet.removeAll(favoriteBuilds);
            buildInfoSet.addAll(favoriteBuilds);
        }
    }

    /**
     * Fetches a {@link Document} instance from the given URL.
     * 
     * @throws IllegalStateException
     *             If the {@link Document} couldn't be fetched.
     */
    private Document getDocument(String stringUrl) {

        try {

            URL url = new URL(stringUrl);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(url.openStream(), "UTF-8"));

            StringBuilder html = new StringBuilder();
            bufferedReader.lines().forEach(html::append);
            bufferedReader.close();

            return Jsoup.parse(html.toString());

        } catch (IOException e) {
            throw new IllegalStateException("Could not fetch the document", e);
        }

    }

    /**
     * Extracts baseline information about all the builds in the given document.
     * 
     * <p>
     * We're extracting the name of the build, its score, what class it is for
     * and its URL.
     * </p>
     */
    private Set<BuildInfo> extractBuildInfo(Document document) {
        Set<BuildInfo> builds = new HashSet<>();

        Elements table = document.select(".listing-builds tbody tr");

        for (Element trElement : table) {

            // Extract the score first since we might not save this build given
            // a particular score
            Elements buildScoreElements = trElement.getElementsByClass("rating-sum");

            // Score of 0
            if (buildScoreElements.text().length() == 1) {
                continue;
            }

            int score = Integer.parseInt(buildScoreElements.text().substring(1));

            // Negative score, don't want it
            if (score < 0) {
                continue;
            }

            Elements buildClassElements = trElement.getElementsByClass("col-class");
            Elements buildUrlElements = trElement.getElementsByClass("d3build");

            String urlPart = buildUrlElements.attr("href");
            D3Class d3Class = D3Class.valueOf(
                    buildClassElements.text().toUpperCase().replaceAll("\\s", "_"));

            builds.add(new BuildInfo(d3Class, BASELINE_URL + urlPart));
        }

        return builds;
    }

    /**
     * Takes a baseline {@link BuildInfo} object and populates it with
     * {@link BuildGear} data.
     * 
     * @throws IllegalStateException
     *             If the given {@link BuildInfo} instance doesn't have a URL.
     */
    private void processBuildInfo(BuildInfo buildInfo) {
        if (buildInfo.getBuildUrl().toString().isEmpty()) {
            throw new IllegalStateException("The given BuildInfo does not have a URL.");
        }

        Document document = getDocument(buildInfo.getBuildUrl().toString());

        String buildName = getRawText(document.select(".build-title"));

        // Handle 0-score edge-cases
        String buildScoreString = getRawText(document.select(".rating-sum"));
        int buildScore = (buildScoreString.length() == 1) ? 0
                : Integer.parseInt(buildScoreString.substring(1));

        String cubeWeapon = getRawText(document.select("#kanai-weapon>span"));
        String cubeArmor = getRawText(document.select("#kanai-armor>span"));
        String cubeJewelry = getRawText(document.select("#kanai-jewelry>span"));

        BuildGear buildGear = new BuildGear();
        buildGear.cubeWeapon = cubeWeapon;
        buildGear.cubeArmor = cubeArmor;
        buildGear.cubeJewelry = cubeJewelry;

        buildGear.headSlot.addAll(extractItems(document, "head"));
        buildGear.shoulderSlot.addAll(extractItems(document, "shoulders"));
        buildGear.amuletSlot.addAll(extractItems(document, "amulet"));
        buildGear.torsoSlot.addAll(extractItems(document, "torso"));
        buildGear.wristSlot.addAll(extractItems(document, "wrists"));
        buildGear.handSlot.addAll(extractItems(document, "hands"));
        buildGear.waistSlot.addAll(extractItems(document, "waist"));
        buildGear.legSlot.addAll(extractItems(document, "legs"));
        buildGear.feetSlot.addAll(extractItems(document, "feet"));
        buildGear.ringSlot.addAll(extractItems(document, "rings"));
        buildGear.weaponSlot.addAll(extractItems(document, "weapon"));
        buildGear.offhandSlot.addAll(extractItems(document, "offhand"));

        buildInfo.setBuildName(buildName);
        buildInfo.setBuildScore(buildScore);

        buildInfo.setBuildGear(buildGear);
    }

    /**
     * Extracts items from a build-document.
     * 
     * @param document
     *            The {@link Document} for this build.
     * @param itemSlot
     *            The item-slot to extract.
     * 
     * @return Returns a {@link Set} of items found in that slot.
     */
    private Set<String> extractItems(Document document, String itemSlot) {
        Elements headElements = document.select("#item-" + itemSlot + ">ul>li");

        return headElements.stream()
                .filter(e -> !e.getElementsByClass("build-item").text().isEmpty())
                .map(e -> getRawText(e.getElementsByClass("build-item")))
                .collect(Collectors.toSet());
    }

    /**
     * Performs any necessary processing on the text extracted from an element.
     */
    private String getRawText(Elements elements) {
        return elements.text().replaceAll("’", "'");
    }

}
