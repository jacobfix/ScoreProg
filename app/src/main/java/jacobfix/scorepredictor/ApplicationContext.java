package jacobfix.scorepredictor;

import android.app.Application;
import android.content.Context;

import jacobfix.scorepredictor.sync.NflGameSyncManager;

public class ApplicationContext extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }



}
