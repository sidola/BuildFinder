package application.model;

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
    
    public int getClassFilterId() {
        return classFilterId;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
