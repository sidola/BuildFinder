package application.gui.model;

public class BuildTableColumnState {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final BuildTableColumn column;
    private final int index;
    private final double width;
    private final boolean isVisible;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    public BuildTableColumnState(BuildTableColumn column, int index, double width,
            boolean isVisible) {
        this.column = column;
        this.index = index;
        this.width = width;
        this.isVisible = isVisible;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    public BuildTableColumn getColumn() {
        return column;
    }

    public int getIndex() {
        return index;
    }

    public double getWidth() {
        return width;
    }

    public boolean isVisible() {
        return isVisible;
    }

    // ----------------------------------------------
    //
    // ToString
    //
    // ----------------------------------------------

    @Override
    public String toString() {
        return "ColumnState [column=" + column + ", index=" + index + ", width=" + width
                + ", isVisible=" + isVisible + "]";
    }

}
