package application.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

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
    // Equals & Hashcode
    //
    // ----------------------------------------------

    // ----------------------------------------------
    //
    // Serialization / Deserialization
    //
    // ----------------------------------------------

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();
    }

    public String getBuildName() {
        return buildName;
    }

    // ----------------------------------------------
    //
    // Getters & Setters
    //
    // ----------------------------------------------

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

}
