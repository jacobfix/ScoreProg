package jacobfix.scoreprog.util;

public class Wrapper<T> {

    private T value;

    public Wrapper() {

    }

    public Wrapper(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return this.value != null;
    }
}
