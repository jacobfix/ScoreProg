package jacobfix.scoreprog;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jacobfix.scoreprog.components.LobbyListItemMiddleContainer;
import jacobfix.scoreprog.components.LobbyListItemNameContainer;
import jacobfix.scoreprog.components.LobbyListItemScoreContainer;
import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.sync.GameAndDriveFeed;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.sync.PredictionProvider;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class LobbyFragment extends Fragment {

    private static final String TAG = LobbyFragment.class.getSimpleName();

    private String[] gameIds;

    /* Maps containing the games and predictions relevant to this fragment. */
    private Map<String, Game> games = new HashMap<>();
    private Map<String, Prediction> predictions = new HashMap<>();

    /* List containing the game IDs in the order that the games should be displayed. */
    private List<String> sortedGameIds = new ArrayList<>();

    /* List containing the objects used by the adapter to display the list of games. */
    private List<LobbyFragmentListItem> listItems = new ArrayList<>();

    private String weekTitle;

    private ViewGroup root;
    private ListView list;
    private ProgressBar loadingIcon;
    private LobbyFragmentAdapter adapter = new LobbyFragmentAdapter();

    private State state = State.LOADING;

    private LayoutInflater inflater;

    private AsyncCallback<Collection<GameAndDriveFeed>> gameSyncListener = new AsyncCallback<Collection<GameAndDriveFeed>>() {
        @Override
        public void onSuccess(Collection<GameAndDriveFeed> result) {
            Log.d(TAG, "Updating list after game sync");
            // TODO: Re-enable this, but check if the UI has been initialized (the updateList() call in finishInit() has already happened)
            updateList(false);
        }

        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, e.toString());
            Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
        }
    };

    public static LobbyFragment newInstance(String[] gids) {
        LobbyFragment f = new LobbyFragment();

        Bundle args = new Bundle();
        args.putStringArray("gameIds", gids);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameIds = getArguments().getStringArray("gameIds");
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_lobby, container, false);
        initializeViews(view);

        inflater = inf;

        return view;
    }

    private void initializeViews(View container) {
        root = ViewUtil.findById(container, R.id.root);
        loadingIcon = ViewUtil.findById(container, R.id.loading_circle);
        list = ViewUtil.findById(container, R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switchToGameActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Starting a LobbyFragment with " + gameIds.length + " games");

        showLoading();

        /* Gather the Game objects themselves from Schedule. */
        for (String gameId : gameIds)
            games.put(gameId, GameProvider.getGame(gameId));

        if (games.size() > 0) {
            Game arbitrary = games.get(gameIds[0]);
            Log.d(TAG, arbitrary.getWeekType() + ", " + arbitrary.getWeek());
            weekTitle = Util.getWeekTitle(arbitrary.getWeekType(), arbitrary.getWeek());
        } else {
            Log.wtf(TAG, "LobbyFragment passed in an array of length 0");
        }

//        GameProvider.addGamesToSync(Arrays.asList(gameIds));
        GameProvider.registerGameSyncListener(gameSyncListener);

        /* Get this user's predictions for all games that are listed in this fragment. */
        PredictionProvider.getMyPredictions(Arrays.asList(gameIds), new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                predictions = result;
                finishInit();
            }

            @Override
            public void onFailure(Exception e) {
                // TODO: Snackbar
                Log.e(TAG, e.toString());
            }
        });

//        GameProvider.getFullGames(games.values(), new AsyncCallback<Map<String, FullGame>>() {
//            @Override
//            public void onSuccess(Map<String, FullGame> result) {
//                for (String gameId : result.keySet())
//                    games.put(gameId, result.get(gameId));
//                fullGamesRetrieved = true;
//                if (predictionsRetrieved)
//                    finishInit();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                // TODO: Snackbar
//                Log.e(TAG, e.toString());
//            }
//        });
    }

    @Override
    public void onStop() {
        super.onStop();
        GameProvider.unregisterGameSyncListener(gameSyncListener);
    }

    private void finishInit() {
        updateList(false);
    }

    public void onGameLocked(String gameId) {
        updateList(false);
    }

    private void updateList(boolean showLoading) {
        if (showLoading)
            showLoading();

        new MakeGamesListTask(LobbyFragment.this.games, LobbyFragment.this.predictions,
                new TaskFinishedListener<MakeGamesListTask>() {
                    @Override
                    public void onTaskFinished(MakeGamesListTask task) {
                        if (task.errorOccurred()) {
                            // TODO: Snackbar
                            showList();
                            Log.e(TAG, task.getError().toString());
                            return;
                        }

                        listItems = task.getResult();
                        adapter.notifyDataSetChanged();
                        showList();
                    }
                }).start();
    }

    private void showList() {
        setVisibleComponents(View.VISIBLE, View.GONE);
        state = State.LIST;
    }

    private void showLoading() {
        setVisibleComponents(View.GONE, View.VISIBLE);
        state = State.LOADING;
    }

    private void setVisibleComponents(int listVisibility, int loadingIconVisibility) {
        list.setVisibility(listVisibility);
        loadingIcon.setVisibility(loadingIconVisibility);
    }

    private void switchToGameActivity(int currentIndex) {
        String[] gameIds = new String[listItems.size()];
        for (int i = 0; i < listItems.size(); i++)
            gameIds[i] = listItems.get(i).gameId;

        Intent intent = new Intent(getActivity(), GameActivity.class);
        // intent.putExtra("gameIds", sortedGameIds.toArray(new String[sortedGameIds.size()]));
        intent.putExtra("gameIds", gameIds);
        intent.putExtra("current", currentIndex);
        intent.putExtra("title", weekTitle);
        getActivity().startActivity(intent);
    }

    public String[] getGameIds() {
        return gameIds;
    }

    class LobbyFragmentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public LobbyFragmentListItem getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_lobby_constraint, parent, false);

                // TODO: Static variable indicating whether or not the measurements have been made, that way it applies to all LobbyFragments

                ViewHolder holder = new ViewHolder();

                holder.middleContainer = ViewUtil.findById(convertView, R.id.middle_container);

                holder.awayNameContainer = ViewUtil.findById(convertView, R.id.away_name_container);
                holder.homeNameContainer = ViewUtil.findById(convertView, R.id.home_name_container);

                holder.awayScoreContainer = ViewUtil.findById(convertView,
                        R.id.away_score_container);
                holder.homeScoreContainer = ViewUtil.findById(convertView,
                        R.id.home_score_container);

                Log.d(TAG, "Prediction text size: " + holder.awayScoreContainer.getPredictedScoreTextSize());
                holder.awayScoreContainer.setActualScoreTextSize(TypedValue.COMPLEX_UNIT_PX,
                        holder.awayScoreContainer.getPredictedScoreTextSize());
                holder.homeScoreContainer.setActualScoreTextSize(TypedValue.COMPLEX_UNIT_PX,
                        holder.homeScoreContainer.getPredictedScoreTextSize());

//                holder.awayAbbr = ViewUtil.findById(convertView, R.id.away_abbr);
//                holder.homeAbbr = ViewUtil.findById(convertView, R.id.home_abbr);
//                holder.awayName = ViewUtil.findById(convertView, R.id.away_name);
//                holder.homeName = ViewUtil.findById(convertView, R.id.home_name);
//                holder.awayNameContainer = ViewUtil.findById(convertView, R.id.away_name_container);
//                holder.homeNameContainer = ViewUtil.findById(convertView, R.id.home_name_container);
//                holder.awayNameBackgroundContainer = ViewUtil.findById(convertView, R.id.away_name_outer_container);
//                holder.homeNameBackgroundContainer = ViewUtil.findById(convertView, R.id.home_name_outer_container);
//
//                holder.awayScoreContainer = ViewUtil.findById(convertView, R.id.away_score_container);
//                holder.homeScoreContainer = ViewUtil.findById(convertView, R.id.home_score_container);
//
//                String b = "WWW";
//                Rect boundingRect = new Rect();
//                holder.awayAbbr.getPaint().getTextBounds(b, 0, b.length(), boundingRect);
//                holder.awayNameContainer.getLayoutParams().width = boundingRect.width();
//                holder.homeNameContainer.getLayoutParams().width = boundingRect.width();
//                holder.awayNameBackgroundContainer.getLayoutParams().width = boundingRect.width();
//                holder.homeNameBackgroundContainer.getLayoutParams().width = boundingRect.width();
//                holder.awayNameContainer.requestLayout();
//                holder.homeNameContainer.requestLayout();
//
//                holder.awayScore = ViewUtil.findById(convertView, R.id.away_score);
//                holder.homeScore = ViewUtil.findById(convertView, R.id.home_score);
//
//                holder.awayPrediction = ViewUtil.findById(convertView, R.id.away_prediction);
//                holder.homePrediction = ViewUtil.findById(convertView, R.id.home_prediction);
//
//                Typeface abbrTypeface = FontHelper.getYantramanavBold(getContext());
//                holder.awayAbbr.setTypeface(abbrTypeface);
//                holder.homeAbbr.setTypeface(abbrTypeface);
//                ViewUtil.applyDeboss(holder.awayAbbr);
//                ViewUtil.applyDeboss(holder.homeAbbr);
//
//                Typeface nameTypeface = FontHelper.getYantramanavBold(getContext());
//                holder.awayName.setTypeface(nameTypeface);
//                holder.homeName.setTypeface(nameTypeface);
//                ViewUtil.applyDeboss(holder.awayName);
//                ViewUtil.applyDeboss(holder.homeName);
//
//                Typeface scoreTypeface = FontHelper.getArimoRegular(getContext());
//                holder.awayScore.setTypeface(scoreTypeface);
//                holder.homeScore.setTypeface(scoreTypeface);
//
//                holder.awayPrediction.setTypeface(scoreTypeface, false);
//                holder.homePrediction.setTypeface(scoreTypeface, false);
//                holder.awayPrediction.setTextSize(32);
//                holder.homePrediction.setTextSize(32);
//
//                holder.awayScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
//                holder.homeScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);

                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            LobbyFragmentListItem listItem = listItems.get(position);

            if (listItem.isLocked) holder.middleContainer.lock();
            else                   holder.middleContainer.unlock();

//            holder.awayAbbr.setText(listItem.awayAbbr);
//            holder.homeAbbr.setText(listItem.homeAbbr);
//            holder.awayName.setText(listItem.awayName);
//            holder.homeName.setText(listItem.homeName);

            holder.awayNameContainer.setAbbr(listItem.awayAbbr);
            holder.homeNameContainer.setAbbr(listItem.homeAbbr);
            holder.awayNameContainer.setName(listItem.awayName);
            holder.homeNameContainer.setName(listItem.homeName);

            holder.awayScoreContainer.setActualScore(listItem.awayScore);
            holder.homeScoreContainer.setActualScore(listItem.homeScore);

//            holder.awayScore.setText(String.valueOf(listItem.awayScore));
//            holder.homeScore.setText(String.valueOf(listItem.homeScore));

//            holder.awayNameBackgroundContainer.setBackgroundColor(listItem.awayColor);
//            holder.homeNameBackgroundContainer.setBackgroundColor(listItem.homeColor);

            if (listItem.predictionExists) {
                /* If this game has been predicted, show the prediction. */
//                holder.awayPrediction.setScore(listItem.awayPredictedScore);
//                holder.homePrediction.setScore(listItem.homePredictedScore);
                holder.awayScoreContainer.setPredictedScore(listItem.awayPredictedScore);
                holder.homeScoreContainer.setPredictedScore(listItem.homePredictedScore);
                holder.awayScoreContainer.showPredictedScore();
                holder.homeScoreContainer.showPredictedScore();

//                holder.awayPrediction.setVisibility(View.VISIBLE);
//                holder.homePrediction.setVisibility(View.VISIBLE);
            } else {
                /* Hide the prediction boxes if this game has not been predicted. */
                holder.awayScoreContainer.hidePredictedScore();
                holder.homeScoreContainer.hidePredictedScore();

//                holder.awayPrediction.setVisibility(View.GONE);
//                holder.homePrediction.setVisibility(View.GONE);
            }

            if (listItem.isPregame) {
                /* Don't show the score if the game has not yet begun (i.e., don't show 0-0). */
//                holder.awayScore.setVisibility(View.GONE);
//                holder.homeScore.setVisibility(View.GONE);
                holder.awayScoreContainer.hideActualScore();
                holder.homeScoreContainer.hideActualScore();
            } else {
//                holder.awayScore.setVisibility(View.VISIBLE);
//                holder.homeScore.setVisibility(View.VISIBLE);
                holder.awayScoreContainer.showActualScore();
                holder.homeScoreContainer.showActualScore();
            }


            if (!listItem.predictionComplete) {
                holder.unhighlightAway();
                holder.unhighlightHome();
                if (listItem.isLocked) {
                    holder.awayScoreContainer.hidePredictedScore();
                    holder.homeScoreContainer.hidePredictedScore();
                } else {
                    holder.awayScoreContainer.showPredictedScore();
                    holder.homeScoreContainer.showPredictedScore();
                }
            } else {
                holder.awayScoreContainer.showPredictedScore();
                holder.homeScoreContainer.showPredictedScore();
                int diff = listItem.awayPredictedScore - listItem.homePredictedScore;
                if (diff > 0) {
                    holder.highlightAway(listItem.awayColor);
                    holder.unhighlightHome();
                } else if (diff < 0) {
                    holder.unhighlightAway();
                    holder.highlightHome(listItem.homeColor);
                } else {
                    holder.unhighlightAway();
                    holder.unhighlightHome();
                }
            }

//            if (listItem.awayColor % 2 == 0) {
//                holder.highlightAway(listItem.awayColor);
//                holder.unhighlightHome();
//            } else {
//                holder.unhighlightAway();
//                holder.highlightHome(listItem.homeColor);
//            }

//            if (listItem.awayColor % 2 == 0) {
//                holder.highlightAway(listItem.awayColor);
//                holder.unhighlightHome();
//            } else {
//                holder.unhighlightAway();
//                holder.highlightHome(listItem.homeColor);
//            }

            if (listItem.isPregame)    holder.middleContainer.pregameDisplay(listItem.startTime);
            else if (listItem.isFinal) holder.middleContainer.finalDisplay();
            else                       holder.middleContainer.inProgressDisplay(listItem.quarter, listItem.clock);

            return convertView;
        }
    }

    static class ViewHolder {

        LobbyListItemMiddleContainer middleContainer;

        LobbyListItemNameContainer awayNameContainer;
        LobbyListItemNameContainer homeNameContainer;

        LobbyListItemScoreContainer awayScoreContainer;
        LobbyListItemScoreContainer homeScoreContainer;

        void highlightAway(int color) {
            awayNameContainer.color(color);
            awayScoreContainer.color(color);
        }

        void unhighlightAway() {
            awayNameContainer.uncolor();
            awayScoreContainer.uncolor();
        }

        void highlightHome(int color) {
            homeNameContainer.color(color);
            homeScoreContainer.color(color);
        }

        void unhighlightHome() {
            homeNameContainer.uncolor();
            homeScoreContainer.uncolor();
        }
//
//        void highlightAway(int color) {
//            awayAbbr.setTextColor(ColorUtil.WHITE);
//            awayName.setTextColor(ColorUtil.WHITE);
//            awayNameContainer.setBackgroundColor(color);
//            // awayPrediction.solidBackground(ColorUtil.WHITE, game.getAwayTeam().getPrimaryColor());
//            // awayPrediction.strokedBackground(color, color);
//            awayPrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
//
//            awayScoreContainer.setBackgroundColor(color);
//            awayPrediction.solidBackground(color, ColorUtil.WHITE);
//        }

//        void highlightHome(int color) {
//            homeAbbr.setTextColor(ColorUtil.WHITE);
//            homeName.setTextColor(ColorUtil.WHITE);
//            homeNameContainer.setBackgroundColor(color);
//            // homePrediction.solidBackground(ColorUtil.WHITE, game.getHomeTeam().getPrimaryColor());
//            // homePrediction.strokedBackground(color, color);
//            homePrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
//
//            homeScoreContainer.setBackgroundColor(color);
//            homePrediction.solidBackground(color, ColorUtil.WHITE);
//        }
//
//        void unhighlightAway() {
//            awayAbbr.setTextColor(ColorUtil.STANDARD_TEXT);
//            awayName.setTextColor(ColorUtil.STANDARD_TEXT);
//            awayNameContainer.setBackgroundColor(ColorUtil.WHITE);
//            awayPrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
//
//            homeScoreContainer.setBackgroundColor(ColorUtil.WHITE);
//        }
//
//        void unhighlightHome() {
//            homeAbbr.setTextColor(ColorUtil.STANDARD_TEXT);
//            homeName.setTextColor(ColorUtil.STANDARD_TEXT);
//            homeNameContainer.setBackgroundColor(ColorUtil.WHITE);
//            homePrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
//
//            homeScoreContainer.setBackgroundColor(ColorUtil.WHITE);
//        }
    }

    public enum State {
        LOADING,
        LIST
    }

    static class MakeGamesListTask extends BaseTask<List<LobbyFragmentListItem>> {

        Map<String, Game> games;
        Map<String, Prediction> predictions;

        public MakeGamesListTask(Map<String, Game> games, Map<String, Prediction> predictions,
                                 TaskFinishedListener listener) {
            super(listener);
            this.games = games;
            this.predictions = predictions;
        }

        @Override
        public void execute() {
            try {
                ArrayList<LobbyFragmentListItem> listItems = new ArrayList<>();

                Set<String> gameIds = games.keySet();
                for (String gameId : gameIds) {
                    Game game = games.get(gameId);
                    Prediction prediction = predictions.get(gameId);

                    LobbyFragmentListItem listItem = new LobbyFragmentListItem();

                    synchronized (game.acquireLock()) {
                        listItem.copyGameInfo(game);
                    }

                    if (prediction != null) {
                        synchronized (prediction.acquireLock()) {
                            listItem.copyPredictionInfo(prediction);
                        }
                    }
                    listItems.add(listItem);
                }

                setResult(listItems);
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    static class LobbyFragmentListItem {

        String gameId;
        boolean isLocked;
        boolean isPregame;
        boolean isFinal;
        int quarter;
        long startTime;
        String startTimeDisplay;
        String dateDisplay;
        Schedule.Day dayOfWeek;

        String awayAbbr;
        String awayName;

        String homeAbbr;
        String homeName;

        int awayScore;
        int homeScore;

        int awayColor;
        int homeColor;

        int awayPredictedScore;
        int homePredictedScore;
        int spread;
        boolean predictionComplete;

        String clock;

        boolean gameExists = false;
        boolean predictionExists = false;

        public void copyGameInfo(Game game) {
            this.gameId = game.getId();
            this.isLocked = game.isLocked;

            this.isPregame = game.isPregame();
            this.isFinal = game.isFinal();

            this.quarter = game.quarter;
            this.clock = game.clock;
            this.startTime = game.getStartTime();
            this.startTimeDisplay = game.getStartTimeDisplay();
            this.dateDisplay = game.getDateDisplay();
            this.dayOfWeek = game.dayOfWeek;

            this.awayAbbr = game.getAwayAbbr();
            this.homeAbbr = game.getHomeAbbr();
            this.awayName = game.getAwayName();
            this.homeName = game.getHomeName();

            this.awayScore = game.getAwayScore();
            this.homeScore = game.getHomeScore();

            this.awayColor = game.getAwayColor();
            this.homeColor = game.getHomeColor();

            gameExists = true;
        }

        public void copyPredictionInfo(Prediction prediction) {
            this.awayPredictedScore = prediction.getAwayScore();
            this.homePredictedScore = prediction.getHomeScore();
            this.predictionComplete = prediction.isComplete();

            if (gameExists) {
                spread = Prediction.computeSpread(awayPredictedScore, homePredictedScore, awayScore,
                        homeScore);
            }
            predictionExists = true;
        }
    }

}
