package application.model;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BuildInfo implements Serializable {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private static final long serialVersionUID = 1L;

    private String buildName;
    private URL buildUrl;
    private int buildScore;
    private D3Class d3Class;

    private transient BooleanProperty isFavoriteProperty = new SimpleBooleanProperty();
    private transient int buildUrlId;

    private BuildGear buildGear;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    public BuildInfo(D3Class d3Class, String urlString) {
        this.d3Class = d3Class;

        try {
            this.buildUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.buildUrlId = extractBuildId(buildUrl);
    }

    public BuildInfo(D3Class d3Class, URL buildUrl) {
        this.d3Class = d3Class;
        this.buildUrl = buildUrl;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    @Override
    public String toString() {
        return String.format("Name: %s\nScore: %d\nClass: %s\nURL: %s\n-----\nGear:\n%s",
                buildName, buildScore, d3Class, buildUrl.toString(),
                buildGear.toString());
    }

    // ----------------------------------------------
    //
    // Private API
    //
    // ----------------------------------------------

    /**
     * Extracts the unique ID from a given build URL.
     */
    private int extractBuildId(URL url) {
        // Example name format:
        // http://www.diablofans.com/builds/69831-2-4-gr-75-80-cluster-marauder

        String stringId = url.toString().split("-")[0].split("builds/")[1];
        return Integer.parseInt(stringId);

    }

    // ----------------------------------------------
    //
    // Equals & Hashcode
    //
    // ----------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + buildUrlId;
        result = prime * result + ((d3Class == null) ? 0 : d3Class.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof BuildInfo)) {
            return false;
        }
        
        BuildInfo other = (BuildInfo) obj;
        
        if (buildUrlId != other.buildUrlId) {
            return false;
        }
        
        if (d3Class != other.d3Class) {
            return false;
        }
        
        return true;
    }

    // ----------------------------------------------
    //
    // Serialization / Deserialization
    //
    // ----------------------------------------------

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeBoolean(isFavoriteProperty.get());
        s.writeInt(buildUrlId);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        isFavoriteProperty = new SimpleBooleanProperty();
        try {

            isFavoriteProperty.setValue(s.readBoolean());
            buildUrlId = s.readInt();

        } catch (EOFException e) {
            
            // Could not find fields in the file we loaded
            buildUrlId = extractBuildId(buildUrl);
            
        }

    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public URL getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(URL buildUrl) {
        this.buildUrl = buildUrl;
    }

    public int getBuildScore() {
        return buildScore;
    }

    public void setBuildScore(int buildScore) {
        this.buildScore = buildScore;
    }

    public D3Class getD3Class() {
        return d3Class;
    }

    public void setD3Class(D3Class d3Class) {
        this.d3Class = d3Class;
    }

    public BuildGear getBuildGear() {
        return buildGear;
    }

    public void setBuildGear(BuildGear buildGear) {
        this.buildGear = buildGear;
    }

    public boolean isFavorite() {
        return isFavoriteProperty.get();
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavoriteProperty.setValue(isFavorite);
    }

    public BooleanProperty isFavoriteProperty() {
        return isFavoriteProperty;
    }

}
