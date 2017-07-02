package jacobfix.scorepredictor;

public interface ResultListener<T> {

    void onSuccess(T result);
    void onFailure(Exception e);
}
