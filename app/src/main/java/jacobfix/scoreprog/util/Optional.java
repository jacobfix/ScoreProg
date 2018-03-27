package jacobfix.scoreprog.util;

import java.util.NoSuchElementException;

public class Optional<T> {

    private final T value;

    private Optional() {
        this.value = null;
    }

    private Optional(T value) {
        this.value = value;
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public T get() {
        if (value == null)
            throw new NoSuchElementException("No value present");
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }
}
