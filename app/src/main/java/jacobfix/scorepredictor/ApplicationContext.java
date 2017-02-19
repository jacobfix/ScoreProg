package jacobfix.scorepredictor;

import android.app.Application;
import android.content.Context;

public class ApplicationContext extends Application {

    private NflGameSyncManager mNflGameSyncManager;

    public static ApplicationContext getInstance(Context context) {
        return (ApplicationContext) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSyncManager();
    }

    private void initializeSyncManager() {
        mNflGameSyncManager = new NflGameSyncManager(getApplicationContext());
    }

    public NflGameSyncManager getNflGameSyncManager() {
        return mNflGameSyncManager;
    }

}
