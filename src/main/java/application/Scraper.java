package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import application.config.UserPreferences;
import application.config.UserPreferences.PrefKey;
import application.model.BuildGear;
import application.model.BuildInfo;
import application.model.D3Class;
import application.util.BuildUrlParser;
import application.util.MathUtil;
import javafx.concurrent.Task;

/**
 * Takes care of scraping HTML information and extracting the relevant data.
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
    private final static int MAX_PAGE_COUNT = 3;

    private final String fetchUrl;
    private final Set<Integer> classesToFetch = new HashSet<>();

    private final List<FetchInfo> fetchInfo;

    private Set<BuildInfo> newBuildInfoSet;

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
        fetchInfo = buildFetchInfo();

        // -- Old system below --

        BuildUrlParser buildUrlParser = new BuildUrlParser(
                UserPreferences.get(PrefKey.BUILDS_URL));
        Map<String, String> optionsMap = buildUrlParser.getOptions();

        int[] classIdSet = new int[] { 2, 4, 8, 16, 32, 64 };
        if (optionsMap.containsKey("filter-class")) {

            int classSum = Integer.parseInt(optionsMap.get("filter-class"));
            classesToFetch.addAll(MathUtil.getValuesFromSum(classSum, classIdSet));

        } else {

            // We'll get all classes if no class was specified by the user
            classesToFetch.addAll(
                    Arrays.stream(classIdSet).boxed().collect(Collectors.toList()));

        }

        fetchUrl = buildUrlParser.getFetchUrlWithoutClasses();
    }

    // ----------------------------------------------
    //
    // Protected API
    //
    // ----------------------------------------------

    @Override
    protected Boolean call() throws Exception {
        newBuildInfoSet = new HashSet<>();

        for (FetchInfo fetchInfo : fetchInfo) {
            newBuildInfoSet.addAll(fetchNewBuilds(fetchInfo));
        }

        if (!isCancelled()) {
            updateStoredBuildInfo(newBuildInfoSet);
        }

        return true;
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Fetches new builds based on the given {@link FetchInfo}.
     */
    private Set<BuildInfo> fetchNewBuilds(FetchInfo fetchInfo) {
        Set<BuildInfo> builds = new HashSet<>();

        for (D3Class thisClass : D3Class.values()) {
            if (isCancelled()) {
                break;
            }

            int id = thisClass.getClassFilterId();
            if (!fetchInfo.classesToFetch.contains(id)) {
                continue;
            }

            updateProgress(0, 1);
            String thisFetchUrl = fetchInfo.fetchUrl;
            thisFetchUrl += "&filter-class=" + id;

            int pageCount = fetchInfo.pageCount;

            if (pageCount == 1) {

                updateMessage(String.format("Fetching %s builds", thisClass.toString()));
                builds.addAll(getBuilds(thisFetchUrl));

            } else {

                thisFetchUrl += "&page=";
                
                for (int i = 0; i < pageCount; i++) {

                    int currentPage = i + 1;
                    
                    updateProgress(0, 1);
                    updateMessage(String.format("Fetching %s builds, page %d of %d",
                            thisClass.toString(), currentPage, pageCount));

                    builds.addAll(getBuilds(thisFetchUrl + currentPage));

                }

            }
        }

        return builds;
    }

    /**
     * Parses the {@link UserPreferences} and extracts fetch information.
     */
    private List<FetchInfo> buildFetchInfo() {
        List<FetchInfo> returnList = new ArrayList<>();

        BuildUrlParser buildUrlParser = new BuildUrlParser(
                UserPreferences.get(PrefKey.BUILDS_URL));

        Set<Integer> classesToFetch = buildUrlParser.extractClassesToFetch();

        int pageCount = UserPreferences.getInteger(PrefKey.PAGE_COUNT);
        // Limit the amount of pages we download
        if (pageCount > MAX_PAGE_COUNT) {
            UserPreferences.set(PrefKey.PAGE_COUNT, MAX_PAGE_COUNT);
            pageCount = MAX_PAGE_COUNT;
        }

        FetchInfo defaultFetchInfo = new FetchInfo(
                buildUrlParser.getFetchUrlWithoutClasses(), pageCount, classesToFetch);

        returnList.add(defaultFetchInfo);

        // Now let's process additional URLs

        List<String> additionalFetchUrls = UserPreferences
                .getList(PrefKey.ADDITIONAL_BUILD_URLS);
        List<String> additionalPageCounts = UserPreferences
                .getList(PrefKey.ADDITIONAL_PAGE_COUNTS);

        if (additionalFetchUrls.isEmpty() || additionalPageCounts.isEmpty()) {
            return returnList;
        }

        for (int i = 0; i < additionalFetchUrls.size(); i++) {
            String additionalfetchUrl = additionalFetchUrls.get(i);
            buildUrlParser = new BuildUrlParser(additionalfetchUrl);

            int additionalPageCount = Integer.parseInt(additionalPageCounts.get(i));

            Set<Integer> additonalClassesToFetch = buildUrlParser.extractClassesToFetch();

            FetchInfo additionalFetchInfo = new FetchInfo(
                    buildUrlParser.getFetchUrlWithoutClasses(), additionalPageCount,
                    additonalClassesToFetch);

            returnList.add(additionalFetchInfo);
        }

        return returnList;
    }

    /**
     * Extracts all the build-data from the given URL.
     */
    private Set<BuildInfo> getBuilds(String url) {
        Set<BuildInfo> buildSet = new HashSet<>();

        Document document = getDocument(url);
        buildSet.addAll(extractBuildInfo(document));
        Set<BuildInfo> upToDateBuilds = extractUpToDateBuilds(buildSet);

        if (buildSet.isEmpty()) {
            updateProgress(1, 1);
            showStatusBarMessage("All builds are up to date!", 500);

            buildSet.addAll(upToDateBuilds);
            return buildSet;
        }

        BuildDownloader buildDownloader = new BuildDownloader(7, buildSet.size());
        for (BuildInfo buildInfo : buildSet) {
            buildDownloader.queueWork(buildInfo);
        }

        long workDone = 1;
        updateProgress(workDone, buildSet.size());

        while (buildDownloader.hasWork()) {
            if (isCancelled()) {
                buildDownloader.cancelWork();
                break;
            }

            updateMessage("Downloading build " + workDone + " of " + buildSet.size());

            Entry<BuildInfo, Document> buildInfoResult = buildDownloader.getResult();
            processBuildInfo(buildInfoResult.getKey(), buildInfoResult.getValue());

            updateProgress(workDone, buildSet.size());
            workDone++;
        }

        buildSet.addAll(upToDateBuilds);
        return buildSet;
    }

    /**
     * Takes a set of newly created {@link BuildInfo} instances and takes out
     * any that are already downloaded and up to date. Then returns a new set of
     * the items that were taken out.
     * 
     * @param buildSet
     *            The {@link BuildInfo} set to check for already stored builds.
     * 
     * @return A new {@link Set} containing any {@link BuildInfo} that were
     *         already stored locally.
     */
    private Set<BuildInfo> extractUpToDateBuilds(Set<BuildInfo> buildSet) {
        Set<BuildInfo> cachedBuilds = new HashSet<>();

        Iterator<BuildInfo> currentInfoIterator = buildSet.iterator();
        while (currentInfoIterator.hasNext()) {
            BuildInfo currentBuildInfo = currentInfoIterator.next();

            boolean buildRemoved = false;

            // Remove any builds we had before we started the fetch
            Iterator<BuildInfo> oldInfoIterator = buildInfoSet.iterator();
            while (oldInfoIterator.hasNext()) {
                BuildInfo oldBuildInfo = oldInfoIterator.next();

                if (currentBuildInfo.equals(oldBuildInfo) && currentBuildInfo
                        .getBuildLastUpdated() == oldBuildInfo.getBuildLastUpdated()) {
                    cachedBuilds.add(oldBuildInfo);
                    currentInfoIterator.remove();

                    buildRemoved = true;
                    break;
                }
            }

            // Remove any builds we might've gotten from earlier URLs during
            // this session, assuming they were removed above
            Iterator<BuildInfo> newInfoIterator = newBuildInfoSet.iterator();
            while (!buildRemoved && newInfoIterator.hasNext()) {
                BuildInfo newBuildInfo = newInfoIterator.next();

                if (currentBuildInfo.equals(newBuildInfo) && currentBuildInfo
                        .getBuildLastUpdated() == newBuildInfo.getBuildLastUpdated()) {

                    cachedBuilds.add(newBuildInfo);
                    currentInfoIterator.remove();
                    break;
                }
            }

        }

        return cachedBuilds;
    }

    /**
     * Updates the local storage of {@link BuildInfo} instances to the new ones
     * that were just downloaded.
     */
    private void updateStoredBuildInfo(Set<BuildInfo> newBuildInfoSet) {
        Set<BuildInfo> favoriteBuilds = BuildDataManager.getFavoriteBuilds();

        buildInfoSet.clear();
        buildInfoSet.addAll(newBuildInfoSet);
        buildInfoSet.removeAll(favoriteBuilds);
        buildInfoSet.addAll(favoriteBuilds);

        updateProgress(1, 1);
        showStatusBarMessage("Done!", 500);
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

            if (!trElement.getElementsByClass("no-results").isEmpty()) {
                return Collections.emptySet();
            }

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

            Elements classElements = trElement.getElementsByClass("tip");
            Set<String> classNames = classElements.get(0).classNames();

            D3Class d3Class = null;
            for (String className : classNames) {
                try {
                    String parsedClassName = className.replaceAll("build-", "")
                            .toUpperCase();
                    parsedClassName = parsedClassName.replaceAll("-", "_");
                    d3Class = D3Class.valueOf(parsedClassName);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                break;
            }

            Elements buildUrlElements = trElement.getElementsByClass("d3build");
            String urlPart = buildUrlElements.attr("href");

            Elements dateTimeElements = trElement.getElementsByClass("standard-datetime");
            long buildLastUpdated = Long.parseLong(dateTimeElements.attr("data-epoch"));

            builds.add(new BuildInfo(d3Class, BASELINE_URL + urlPart, buildLastUpdated));
        }

        return builds;
    }

    /**
     * Takes a baseline {@link BuildInfo} object and populates it with
     * {@link BuildGear} data.
     * 
     * @param buildInfo
     *            The {@link BuildInfo} to populate.
     * @param document
     *            All the HTML data for this {@link BuildInfo} instance.
     * 
     * @throws IllegalStateException
     *             If the given {@link BuildInfo} instance doesn't have a URL.
     */
    private void processBuildInfo(BuildInfo buildInfo, Document document) {
        if (buildInfo.getBuildUrl().toString().isEmpty()) {
            throw new IllegalStateException("The given BuildInfo does not have a URL.");
        }

        // Document document = getDocument(buildInfo.getBuildUrl().toString());

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

    /**
     * Pauses the current thread and displays a message in the status-bar.
     * 
     * @param message
     *            The message to display.
     * @param threadPause
     *            The amount of milliseconds to pause the thread for.
     */
    private void showStatusBarMessage(String message, long threadPause) {
        updateMessage(message);

        try {
            Thread.sleep(threadPause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------
    //
    // Inner class
    //
    // ----------------------------------------------

    private class FetchInfo {

        private String fetchUrl;
        private int pageCount;
        private final Set<Integer> classesToFetch = new HashSet<>();

        public FetchInfo(String fetchUrl, int pageCount, Set<Integer> classesToFetch) {
            this.fetchUrl = fetchUrl;
            this.pageCount = pageCount;
            this.classesToFetch.addAll(classesToFetch);
        }

    }

}
