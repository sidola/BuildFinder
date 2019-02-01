package application.gui.model;

public enum BuildTableColumn {

    // @formatter:off
    
    SCORE("Score"),
    CLASS("Class"),
    IS_CUBED("In cube"),
    NAME("Name"),
    URL("Url"),
    LAST_UPDATED("Last updated"),
    AUTHOR("Author"),
    PATCH("Patch");
    
    // @formatter:on

    public final String name;

    private BuildTableColumn(String value) {
        this.name = value;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public String toString() {
        return name;
    };

    public static BuildTableColumn fromName(String name) {
        for (BuildTableColumn column : BuildTableColumn.values()) {
            if (column.name.equals(name)) {
                return column;
            }
        }

        throw new IllegalArgumentException(
                String.format("No enum found for name: %s", name));
    }
}