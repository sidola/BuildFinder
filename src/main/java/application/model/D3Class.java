package application.model;

/**
 * Defines all available classes.
 * 
 * @author Sid Botvin
 */
public enum D3Class {

    // @formatter:off
    
    BARBARIAN("Barbarian", 2),
    DEMON_HUNTER("Demon Hunter", 4),
    WITCH_DOCTOR("Witch Doctor", 8),
    MONK("Monk", 16),
    WIZARD("Wizard", 32),
    CRUSADER("Crusader", 64);
    
    // @formatter:on

    private String name;
    private int classFilterId;

    private D3Class(String name, int classFilterId) {
        this.name = name;
        this.classFilterId = classFilterId;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

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
            if (thisClass.classFilterId == id) {
                return thisClass;
            }
        }

        throw new NoSuchFieldException("Invalid class id: " + id);
    }

    /**
     * Returns the id of this class.
     */
    public int getClassFilterId() {
        return classFilterId;
    }

    @Override
    public String toString() {
        return name;
    }

}
