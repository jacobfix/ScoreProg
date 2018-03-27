package jacobfix.scoreprog.task;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.LockGameManager;
import jacobfix.scoreprog.schedule.Season;
import jacobfix.scoreprog.server.ScheduleServerInterface;
import jacobfix.scoreprog.sync.GameProvider;

public class SyncSeasonTask extends BaseTask<Collection<Game>> {

    private Season season;

    public SyncSeasonTask(Season season, TaskFinishedListener listener) {
        this.season = season;
    }

    @Override
    public void execute() {
        try {
//            JSONObject fullSeasonJson = ScheduleServerInterface.getDefault().getFullSeasonSchedule(season.getYear());
//            Map<String, JSONObject> allGamesJsonMap = ScheduleServerInterface.getDefault().updateSeason(season, fullSeasonJson);
//            for (String gameId : allGamesJsonMap.keySet()) {
//                Game game = GameProvider.getGame(gameId);
//                if (game == null)
//                    game = new Game();
//
//                ScheduleServerInterface.getDefault().updateGame(game, allGamesJsonMap.get(gameId));
//                GameProvider.putGame(game);
//                LockGameManager.get().scheduleLock(game);
//            }
        } catch (Exception e) {
            reportError(e);
        }
    }
}
