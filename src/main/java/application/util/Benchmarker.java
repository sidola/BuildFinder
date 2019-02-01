package application.util;

/**
 * Simple utility class to get benchmarks. Does not work with nested calls.
 */
public final class Benchmarker {

    // -----------------------------------
    // Variables
    // -----------------------------------

    private static long startTime;

    // -----------------------------------
    // Constructor
    // -----------------------------------

    private Benchmarker() {
    }

    // -----------------------------------
    // Methods
    // -----------------------------------

    /**
     * Sets the start-time for the benchmark to the time this method was called.
     */
    public static void setStart() {
        startTime = System.nanoTime();
    }

    /**
     * Returns the elapsed duration (in milliseconds) between calling this
     * method and when {@link Benchmarker#setStart()} was called.
     * 
     * @return The elapsed duration in milliseconds.
     */
    public static long getDuration() {

        // Should never be 0 when this method is called
        if (startTime == 0)
            return -1;

        long elapsedDuration = ((System.nanoTime() - startTime) / 1000000);
        startTime = 0;

        return elapsedDuration;
    }

}
