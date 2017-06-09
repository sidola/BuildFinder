package application.model;

/**
 * Defines all available classes.
 * 
 * @author Sid Botvin
 */
public enum D3Class {

    // @formatter:off
    
    BARBARIAN("Barbarian", "B", 2),
    DEMON_HUNTER("Demon Hunter", "DH", 4),
    WITCH_DOCTOR("Witch Doctor", "WH", 8),
    MONK("Monk", "M", 16),
    WIZARD("Wizard", "W", 32),
    CRUSADER("Crusader", "C", 64),
    NECROMANCER("Necromancer", "NC", 128);
    
    // @formatter:on

    private final String NAME;
    private final String SHORT_HAND_NAME;
    private final int CLASS_FILTER_ID;

    private static int[] classIds;

    private D3Class(String name, String shorthandName, int classFilterId) {
        this.NAME = name;
        this.SHORT_HAND_NAME = shorthandName;
        this.CLASS_FILTER_ID = classFilterId;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Returns an array of all class IDs.
     */
    public static int[] getAllClassIds() {
        if (classIds == null) {
            classIds = new int[values().length];

            for (int i = 0; i < values().length; i++) {
                D3Class d3Class = values()[i];
                classIds[i] = d3Class.getClassFilterId();
            }
        }

        return classIds;
    }

    /**
     * Returns a {@link D3Class} by its id.
     * 
     * @param id
     *            The id of the class.
     * 
     * @throws NoSuchFieldException
     *             If no class with the given ID can be found.
     */
    public static D3Class getById(int id) throws NoSuchFieldException {
        for (D3Class thisClass : values()) {
            if (thisClass.CLASS_FILTER_ID == id) {
                return thisClass;
            }
        }

        throw new NoSuchFieldException("Invalid class id: " + id);
    }

    /**
     * Returns the id of this class.
     */
    public int getClassFilterId() {
        return CLASS_FILTER_ID;
    }

    /**
     * Returns the shorthand name of this class.
     * 
     * For example, "Crusader" would return "C".
     */
    public String getShorthandName() {
        return SHORT_HAND_NAME;
    }

    @Override
    public String toString() {
        return NAME;
    }

}
