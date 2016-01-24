package application.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BuildGear implements Serializable {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private static final long serialVersionUID = 1L;

    public final Set<String> headSlot = new HashSet<>(3);
    public final Set<String> shoulderSlot = new HashSet<>(3);
    public final Set<String> amuletSlot = new HashSet<>(3);
    public final Set<String> torsoSlot = new HashSet<>(3);
    public final Set<String> wristSlot = new HashSet<>(3);
    public final Set<String> handSlot = new HashSet<>(3);
    public final Set<String> waistSlot = new HashSet<>(3);
    public final Set<String> legSlot = new HashSet<>(3);
    public final Set<String> feetSlot = new HashSet<>(3);
    public final Set<String> ringSlot = new HashSet<>(3);
    public final Set<String> weaponSlot = new HashSet<>(3);
    public final Set<String> offhandSlot = new HashSet<>(3);

    public String cubeWeapon;
    public String cubeArmor;
    public String cubeJewelry;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    public BuildGear() {
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Checks if the given item is one of the cubed items.
     */
    public boolean isCubed(String targetItem) {

//        System.out.println(cubeWeapon);
//        System.out.println(cubeArmor);
//        System.out.println(cubeJewelry);
//        System.out.println("--");
//        System.out.println(targetItem);
//        System.out.println("\n\n");
        
        if (cubeArmor.equalsIgnoreCase(targetItem)) {
            return true;
        } else if (cubeWeapon.equalsIgnoreCase(targetItem)) {
            return true;
        } else if (cubeJewelry.equalsIgnoreCase(targetItem)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("Cube weapon: %s\nCube armor: %s\nCube jewelry: %s",
                cubeWeapon, cubeArmor, cubeJewelry);
    }

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

}
