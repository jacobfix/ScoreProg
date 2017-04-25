package jacobfix.scorepredictor;

import java.util.HashMap;

public class SyncNflGamesTask extends BaseTask<HashMap<String, NflGame>> {

    public SyncNflGamesTask() {
        super();
    }

    public SyncNflGamesTask(TaskFinishedListener listener) {
        super(listener);
    }

    @Override
    public void execute() {

    }
}
