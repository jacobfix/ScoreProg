package jacobfix.scorepredictor;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jacobfix.scorepredictor.components.LobbyListItemMiddleContainer;
import jacobfix.scorepredictor.components.LobbyListItemNameContainer;
import jacobfix.scorepredictor.components.LobbyListItemScoreContainer;
import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

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

    private ListView list;
    private ProgressBar loadingIcon;
    private LobbyFragmentAdapter adapter = new LobbyFragmentAdapter();

    private State state = State.LOADING;

    private LayoutInflater inflater;

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
            games.put(gameId, Schedule.getGame(gameId));

        if (games.size() > 0) {
            Game arbitrary = games.get(gameIds[0]);
            weekTitle = Util.getWeekTitle(arbitrary.getWeekType(), arbitrary.getWeek());
        } else {
            Log.wtf(TAG, "LobbyFragment passed in an array of length 0");
        }

        /* Retrieve this user's predictions for all the games in the week held by this fragment. */
        AsyncCallback<Map<String, Prediction>> afterPredictionsRetrieved
                = new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                LobbyFragment.this.predictions = result;
                /* Sort the games before displaying them. */
                new MakeGamesListTask(LobbyFragment.this.games, LobbyFragment.this.predictions,
                        new TaskFinishedListener<MakeGamesListTask>() {
                            @Override
                            public void onTaskFinished(MakeGamesListTask task) {
                                if (task.errorOccurred()) {
                                    Log.e(TAG, task.getError().toString());
                                    return;
                                }

                                listItems = task.getResult();
                                Log.d(TAG, "List items size: " + listItems.size());
                                adapter.notifyDataSetChanged();
                                showList();
                            }
                        }).start();

                // TODO: Have a task that makes the list items
                // sortedGameIds = new ArrayList<>(Arrays.asList(gameIds));
                // adapter.notifyDataSetChanged();
                // showList();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        };
        PredictionProvider.getPredictions(LocalAccountManager.get().getId(), Arrays.asList(gameIds),
                afterPredictionsRetrieved);
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


            if (listItem.isLocked && !listItem.predictionComplete) {
                /* If the game is locked and a complete prediction was not made, don't color either
                   side. */
                holder.unhighlightAway();
                holder.unhighlightHome();
                holder.awayScoreContainer.hidePredictedScore();
                holder.homeScoreContainer.hidePredictedScore();
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

            if (listItem.isPregame)    holder.middleContainer.pregameDisplay(listItem.startTimeDisplay, listItem.dayOfWeek, listItem.dateDisplay);
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
            this.startTime = game.startTime;
            this.startTimeDisplay = game.startTimeDisplay;
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
