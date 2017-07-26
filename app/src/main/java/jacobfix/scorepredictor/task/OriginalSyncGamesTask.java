package jacobfix.scorepredictor.task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.sync.JsonProvider;

public class OriginalSyncGamesTask extends BaseTask<NflGame[]> {

    private LinkedList<NflGame> games;

    public OriginalSyncGamesTask(Collection<NflGame> g, TaskFinishedListener listener) {
        super(listener);
        games = new LinkedList<>(g);
    }

    @Override
    public void execute() {
        NflGame[] result = new NflGame[games.size()];
        int i = 0;

        try {
            for (NflGame game : games) {
                JSONObject json = JsonProvider.get().getGameJson(game.getGameId());
                // int startTime = ScheduleProvider.get().getStartTimes();
                if (json != null) {
                    game.syncFullDetails(json.getJSONObject(game.getGameId()));
                    result[i++] = game;
                }
            }
        } catch (IOException e) {

        } catch (JSONException e) {

        } finally {
            setResult(result);
        }
    }
}
