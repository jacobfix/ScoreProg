package jacobfix.scoreprog;

public interface AsyncCallback<T> {

    void onSuccess(T result);
    void onFailure(Exception e);
}
