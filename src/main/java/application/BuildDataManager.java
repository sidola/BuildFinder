package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import application.model.BuildGear;
import application.model.BuildInfo;
import application.model.D3Class;

/**
 * Data manager class for the build data. Handles saving, loading and provides
 * the application with methods to search for builds containing certain items.
 */
public final class BuildDataManager {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final static File buildsData = new File("./data/", "builds.data");
    private static Set<BuildInfo> buildInfoSet = new HashSet<>();
    private static LocalDate lastUpdated;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    private BuildDataManager() {
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Returns information about the data currently stored.
     */
    public static String getDataInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        String delimiter = "|";

        for (D3Class d3Class : D3Class.values()) {

            long setCount = buildInfoSet.stream().filter(b -> b.getD3Class() == d3Class)
                    .count();

            stringBuilder.append(String.format("%s: %d %s ", d3Class.getShorthandName(),
                    setCount, delimiter));

        }

        // Trim last whitespace and delimiter
        stringBuilder.setLength(stringBuilder.length() - 2);

        String buildsUpdatedDate = (lastUpdated == null) ? "Never"
                : lastUpdated.toString();

        return "Builds updated: " + buildsUpdatedDate + " | " + stringBuilder.toString();
    }

    /**
     * Returns a set of {@link BuildInfo} instances that contain the requested
     * item.
     */
    public static Set<BuildInfo> getBuildsWithItem(String targetItem) {
        Set<BuildInfo> matchingBuilds = new HashSet<>();

        for (BuildInfo thisBuild : buildInfoSet) {
            BuildGear buildGear = thisBuild.getBuildGear();

            // Found in cube-slot
            if (buildGear.cubeWeapon.equals(targetItem)
                    || buildGear.cubeArmor.equals(targetItem)
                    || buildGear.cubeJewelry.equals(targetItem)) {

                matchingBuilds.add(thisBuild);
                continue;
            }

            // @formatter:off
            
            buildGear.headSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));
         
            buildGear.shoulderSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.amuletSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.torsoSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.wristSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.handSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.waistSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.legSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.feetSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.ringSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.weaponSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            buildGear.offhandSlot.stream()
                        .filter(item -> item.equalsIgnoreCase(targetItem))
                        .findFirst()
                        .ifPresent(item -> matchingBuilds.add(thisBuild));

            // @formatter:on

        }

        return matchingBuilds;
    }

    /**
     * Adds builds to the internal storage.
     */
    public static void addBuilds(Set<BuildInfo> builds) {
        buildInfoSet.addAll(builds);
    }

    /**
     * Loads all builds from disk.
     */
    public static void loadBuilds() {

        // No file, no loading
        if (!buildsData.exists()) {
            buildsData.getParentFile().mkdirs();
            return;
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(buildsData);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            DataWrapper dataWrapper = (DataWrapper) objectInputStream.readObject();

            buildInfoSet = dataWrapper.getBuildInfoSet();
            lastUpdated = dataWrapper.getLastUpdated();

            objectInputStream.close();
            fileInputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all builds to disk.
     */
    public static void saveBuilds() {
        DataWrapper dataWrapper = new DataWrapper(buildInfoSet, lastUpdated);

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(buildsData);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);

            outputStream.writeObject(dataWrapper);

            outputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the date when the builds were last updated to the date when this
     * method is called.
     */
    public static void updateLastUpdatedDate() {
        lastUpdated = LocalDate.now();
    }

    /**
     * Deletes a specific builds from storage.
     * 
     * @throws IllegalArgumentException
     *             If the given build isn't stored.
     */
    public static void deleteBuild(BuildInfo build) {
        if (!buildInfoSet.contains(build)) {
            throw new IllegalArgumentException(
                    "The given build is not stored and cannot be deleted.");
        }

        buildInfoSet.remove(build);
    }

    /**
     * Returns all builds marked as favorites.
     */
    public static Set<BuildInfo> getFavoriteBuilds() {
        return buildInfoSet.stream().filter(buildInfo -> buildInfo.isFavorite())
                .collect(Collectors.toSet());
    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

    /**
     * Returns all the builds currently loaded. This is the actual set, any
     * changes are reflected.
     */
    public static Set<BuildInfo> getBuildInfoSet() {
        return buildInfoSet;
    }

    /**
     * Gets the date the data was last updated.
     */
    public static LocalDate getLastUpdated() {
        return lastUpdated;
    }

    // ----------------------------------------------
    //
    // Inner classes & enums
    //
    // ----------------------------------------------

    /**
     * Used to wrap the set and the date to one file when saving/loading.
     */
    private static class DataWrapper implements Serializable {

        private static final long serialVersionUID = 1L;

        private Set<BuildInfo> buildInfoSet;
        private LocalDate lastUpdated;

        public DataWrapper(Set<BuildInfo> buildInfoSet, LocalDate lastUpdated) {
            this.buildInfoSet = buildInfoSet;
            this.lastUpdated = lastUpdated;
        }

        public Set<BuildInfo> getBuildInfoSet() {
            return buildInfoSet;
        }

        public LocalDate getLastUpdated() {
            return lastUpdated;
        }

    }

}
