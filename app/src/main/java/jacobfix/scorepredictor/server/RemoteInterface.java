package jacobfix.scorepredictor.server;

public class RemoteInterface {

    protected static RemoteInterface instance;

    public static RemoteInterface get() {
        return instance;
    }
}
