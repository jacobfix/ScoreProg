package jacobfix.scorepredictor.task;

import jacobfix.scorepredictor.server.UserJsonRetriever;

/* Takes a list of users and list of games and retrieves each user's prediction for each game. */
public class GetPredictionsTask extends BaseTask {

    UserJsonRetriever mJsonRetriever;

    public GetPredictionsTask(UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mJsonRetriever = jsonRetriever;
    }

    @Override
    public void execute() {

    }
}
