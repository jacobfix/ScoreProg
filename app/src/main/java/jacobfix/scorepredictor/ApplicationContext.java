package jacobfix.scorepredictor;

import android.app.Application;
import android.content.Context;

public class ApplicationContext extends Application {

    private static Context mContext;

    public static final String HOST = "192.168.1.15";
    // public static final String HOST = "172.20.8.156";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }



}
