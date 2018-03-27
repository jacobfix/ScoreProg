package jacobfix.scoreprog.task;

import android.util.Log;

import org.json.JSONArray;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import jacobfix.scoreprog.Participants;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.PredictionServerInterface;
import jacobfix.scoreprog.util.Wrapper;

public class SyncParticipantsTask extends BaseTask<Collection<Participants>> {

    private static final String TAG = SyncParticipantsTask.class.getSimpleName();

    private Collection<Participants> participants;

    public SyncParticipantsTask(Collection<Participants> participants, TaskFinishedListener listener) {
        super(listener);
        this.participants = participants;
    }

    @Override
    public void execute() {
        try {
            Set<Participants> toSync = new HashSet<>(this.participants);
            for (Participants p : toSync) {
                JSONArray json = PredictionServerInterface.getDefault().getFriendPredictions(p.getGameId());
                PredictionServerInterface.getDefault().updateParticipants(p, json);
            }
            setResult(toSync);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
