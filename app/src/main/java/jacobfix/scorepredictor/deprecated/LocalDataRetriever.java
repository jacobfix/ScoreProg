package jacobfix.scorepredictor.deprecated;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LocalDataRetriever extends DataRetriever {

    private String mAllGamesPath;
    private String mSingleGameBasePath;

    public static final int SOURCE_20160922_PREGAME = 1;
    public static final int SOURCE_20160922_IN_PROGRESS = 2;
    public static final int SOURCE_20160922_FINAL = 3;

    public LocalDataRetriever(Context context, int source) {
        super(context);
        switch (source) {
            case SOURCE_20160922_PREGAME:
                mAllGamesPath ="data/20160922-pregame/20160922-scores.json";
                mSingleGameBasePath = "data/20160922-pregame/%s.json";
                break;
            case SOURCE_20160922_IN_PROGRESS:
                mAllGamesPath = "data/20160922-inprogress/20160922-scores.json";
                mSingleGameBasePath = "data/20160922-inprogress/%s.json";
                break;
            case SOURCE_20160922_FINAL:
                mAllGamesPath ="data/20160922-final/20160922-scores.json";
                mSingleGameBasePath = "data/20160922-final/%s.json";
                break;
        }
    }

    @Override
    public JSONObject getAllGamesJson() throws IOException, JSONException {
        return getLocalJson(mAllGamesPath);
    }

    @Override
    public JSONObject getIndividualGameJson(String gameId) throws IOException, JSONException {
        return getLocalJson(String.format(mSingleGameBasePath, gameId));
    }
}
