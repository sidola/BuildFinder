package application;

/**
 * Holds a result created by a separate thread.
 *
 * @param <T>
 *            The type of the result object.
 */
public class ResultItem<T> {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private Throwable throwable;
    private T result;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    public ResultItem(T result) {
        this.result = result;
    }

    public ResultItem(Throwable throwable) {
        this.throwable = throwable;
    }

    public ResultItem(T result, Throwable throwable) {
        this.result = result;
        this.throwable = throwable;
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Returns false if the item contains an exception.
     */
    public boolean succeeded() {
        return (throwable == null);
    }

    public T getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
