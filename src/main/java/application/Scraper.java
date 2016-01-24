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

import application.model.BuildGear;
import application.model.BuildInfo;
import application.model.D3Class;
import javafx.concurrent.Task;

/**
 * Takes care of scraping HTML information and extracting the relevant data.
 * 
 * @author Sid Botvin
 */
public final class Scraper extends Task<Integer> {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final static String BASELINE_URL = "http://www.diablofans.com";
    private Set<BuildInfo> buildInfoSet;
    private FetchMode fetchMode;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    public Scraper(Set<BuildInfo> buildInfoSet, FetchMode fetchMode) {
        this.buildInfoSet = buildInfoSet;
        this.fetchMode = fetchMode;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Extracts all the build-data from the given URL.
     */
    public Set<BuildInfo> getBuilds(String url) {
        Set<BuildInfo> buildSet = new HashSet<>();

        Document document = getDocument(url);
        buildSet.addAll(extractBuildInfo(document));

        long workDone = 1;
        updateProgress(workDone, buildSet.size());
        for (BuildInfo buildInfo : buildSet) {

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
    public void fetchNewBuilds() {
        Set<BuildInfo> builds = new HashSet<BuildInfo>();

        for (D3Class thisClass : D3Class.values()) {
            updateProgress(0, 1);
            updateMessage("Fetching " + thisClass.toString() + " builds");

            int id = thisClass.getClassFilterId();

            String fetchUrl = "http://www.diablofans.com/builds?filter-build-type=2"
                    + "&filter-build=6&filter-has-spell-2=-1"
                    + "&filter-build-tag=5&filter-class=" + id + "&sort=-viewcount";

            builds.addAll(getBuilds(fetchUrl));
        }

        buildInfoSet.clear();
        buildInfoSet.addAll(builds);
    }

    /**
     * Refresh the contents of all currently stored build.
     */
    public void refreshBuilds() {
        long workDone = 1;
        updateProgress(workDone, buildInfoSet.size());

        Set<BuildInfo> newBuildInfoSet = new HashSet<>();

        for (BuildInfo buildInfo : buildInfoSet) {

            BuildInfo newBuildInfo = new BuildInfo(buildInfo.getD3Class(),
                    buildInfo.getBuildUrl());
            updateMessage("Updating build " + workDone + " of " + buildInfoSet.size());

            processBuildInfo(newBuildInfo);
            newBuildInfoSet.add(newBuildInfo);

            updateProgress(workDone, buildInfoSet.size());
            workDone++;
        }

        buildInfoSet.clear();
        buildInfoSet.addAll(newBuildInfoSet);
    }

    // ----------------------------------------------
    //
    // Protected API
    //
    // ----------------------------------------------

    @Override
    protected Integer call() throws Exception {

        switch (fetchMode) {
        case REFRESH:
            refreshBuilds();
            break;
        case NEW:
            fetchNewBuilds();
            break;
        default:
            throw new IllegalStateException("Could not identify the fetch-mode.");
        }

        return null;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Fetches a {@link Document} instance from the given URL.
     * 
     * @throws IllegalStateException
     *             If the {@link Document} couldn't be fetched.
     */
    private static Document getDocument(String stringUrl) {

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
    private static Set<BuildInfo> extractBuildInfo(Document document) {
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
    private static void processBuildInfo(BuildInfo buildInfo) {
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
    private static Set<String> extractItems(Document document, String itemSlot) {
        Elements headElements = document.select("#item-" + itemSlot + ">ul>li");

        return headElements.stream()
                .filter(e -> !e.getElementsByClass("build-item").text().isEmpty())
                .map(e -> getRawText(e.getElementsByClass("build-item")))
                .collect(Collectors.toSet());
    }

    /**
     * Performs any necessary processing on the text extracted from an element.
     */
    private static String getRawText(Elements elements) {
        return elements.text().replaceAll("’", "'");
    }

    // ----------------------------------------------
    //
    // Inner classes & enums
    //
    // ----------------------------------------------

    /**
     * Defines the fetch-mode that should be used when getting new builds.
     * REFRESH only refreshes the current builds. NEW wipes all current builds
     * fetches new ones.
     */
    public enum FetchMode {
        REFRESH, NEW;
    }

}
