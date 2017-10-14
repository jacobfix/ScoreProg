package jacobfix.scorepredictor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.OriginalUserProvider;
import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SortGamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyFragment extends Fragment {

    private static final String TAG = LobbyFragment.class.getSimpleName();

    private Predictions predictions;

    private ArrayList<String> sortedGameIds = new ArrayList<>();
    private ArrayList<AtomicGame> sorted = new ArrayList<>();

    private String[] gameIds;

    private ListView list;
    private ProgressBar loadingSymbol;
    private LobbyFragmentAdapter adapter = new LobbyFragmentAdapter();

    private LayoutInflater inflater;

    public static LobbyFragment newInstance(String[] gids) {
        LobbyFragment f = new LobbyFragment();

        Bundle args = new Bundle();
        args.putStringArray("games", gids);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameIds = getArguments().getStringArray("games");
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_lobby, container, false);
        initializeViews(view);
        showLoading();

        inflater = inf;

        return view;
    }

    private void initializeViews(View container) {
        loadingSymbol = ViewUtil.findById(container, R.id.loading_circle);
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
        Log.d(TAG, "onStart() w/ " + Util.join(Arrays.asList(gameIds), ", "));

        final ArrayList<AtomicGame> unsorted = new ArrayList<>();
        for (String gameId : gameIds)
            unsorted.add(Schedule.getGame(gameId));

        PredictionProvider.getPredictions(Collections.singletonList(LocalAccountManager.get().getId()), Arrays.asList(gameIds), false, new AsyncCallback<Predictions>() {
            @Override
            public void onSuccess(Predictions result) {
                predictions = result;

                new SortGamesTask(unsorted, new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        sorted = (ArrayList<AtomicGame>) task.getResult();

                        sortedGameIds = new ArrayList<>();
                        for (AtomicGame atom : sorted)
                            sortedGameIds.add(atom.getId());

                        adapter.notifyDataSetChanged();

                        showList();
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        /* final ArrayList<NflGame> unsorted = new ArrayList<>();
        for (String gid : gameIds) {
            NflGame game = null; //Schedule.getGame(gid);
            if (game != null)
                unsorted.add(game);
        } */

        /*
        final ArrayList<AtomicGame> unsorted = new ArrayList<>();
        for (String gid : gameIds) {
            AtomicGame game = Schedule.getGame(gid);
            unsorted.add(game);
        }
        sorted.clear();
        sorted.addAll(unsorted);
        adapter.notifyDataSetChanged();
        loadingSymbol.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        */

        /*
        OriginalUserProvider.getUserPredictions(Arrays.asList(gameIds), LocalAccountManager.getId(), new AsyncCallback<Map<String, OriginalPredictions>>() {
            @Override
            public void onSuccess(Map<String, OriginalPredictions> result) {
                predictions = result.get(LocalAccountManager.getId());

                new SortGamesTask(unsorted, new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        sorted = (ArrayList<NflGame>) task.getResult();
                        adapter.notifyDataSetChanged();

                        loadingSymbol.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        */
    }

    private void showList() {
        list.setVisibility(View.VISIBLE);
        loadingSymbol.setVisibility(View.GONE);
    }

    private void showLoading() {
        list.setVisibility(View.GONE);
        loadingSymbol.setVisibility(View.VISIBLE);
    }

    private void switchToGameActivity(int currentIndex) {
        Intent intent = new Intent(getActivity(), NewGameActivity.class);
        intent.putExtra("gameIds", sortedGameIds.toArray(new String[sortedGameIds.size()]));
        intent.putExtra("current", currentIndex);
        intent.putExtra("title", Util.getWeekTitle(sorted.get(0).getWeekType(), sorted.get(0).getWeek()));
        getActivity().startActivity(intent);
    }

    class LobbyFragmentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sorted.size();
        }

        @Override
        public AtomicGame getItem(int position) {
            return sorted.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_lobby_new_new, parent, false);

                ViewHolder holder = new ViewHolder();

                holder.middleContainer = ViewUtil.findById(convertView, R.id.middle_container);

                holder.awayAbbr = ViewUtil.findById(convertView, R.id.away_abbr);
                holder.homeAbbr = ViewUtil.findById(convertView, R.id.home_abbr);
                holder.awayName = ViewUtil.findById(convertView, R.id.away_name);
                holder.homeName = ViewUtil.findById(convertView, R.id.home_name);
                holder.awayNameContainer = ViewUtil.findById(convertView, R.id.away_name_container);
                holder.homeNameContainer = ViewUtil.findById(convertView, R.id.home_name_container);

                holder.awayScore = ViewUtil.findById(convertView, R.id.away_score);
                holder.homeScore = ViewUtil.findById(convertView, R.id.home_score);

                holder.awayPrediction = ViewUtil.findById(convertView, R.id.away_prediction);
                holder.homePrediction = ViewUtil.findById(convertView, R.id.home_prediction);

                Typeface abbrTypeface = FontHelper.getYantramanavBold(getContext());
                holder.awayAbbr.setTypeface(abbrTypeface);
                holder.homeAbbr.setTypeface(abbrTypeface);

                Typeface nameTypeface = FontHelper.getYantramanavRegular(getContext());
                holder.awayName.setTypeface(nameTypeface);
                holder.homeName.setTypeface(nameTypeface);

                Typeface scoreTypeface = FontHelper.getYantramanavRegular(getContext());
                holder.awayScore.setTypeface(scoreTypeface);
                holder.homeScore.setTypeface(scoreTypeface);

                holder.awayPrediction.setTypeface(scoreTypeface, false);
                holder.homePrediction.setTypeface(scoreTypeface, false);
                holder.awayPrediction.setTextSize(32);
                holder.homePrediction.setTextSize(32);

                convertView.setTag(holder);
            }

            AtomicGame game = getItem(position);
            Prediction prediction;

            LinkedList<Prediction> userPredictions = predictions.get(game.getId());
            if (userPredictions == null || ((prediction = userPredictions.getFirst()) == null)) {
                prediction = new Prediction(LocalAccountManager.get().getId(), game.getId());
            }

            Log.d(TAG, "Prediction for game " + game.getId() + " (" + game.getAwayTeam().getAbbr() + " @ " + game.getHomeTeam().getAbbr() + ") retrieved");
            Log.d(TAG, "Away: " + prediction.getAwayScore() + ", Home: " + prediction.getHomeScore());

            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.middleContainer.setStartTime(game.getStartTimeDisplay());
            // holder.middleContainer.setClock(game.getClock());
            holder.middleContainer.setQuarter(game.getQuarter());

            if (game.isLocked()) holder.middleContainer.lock();
            else                 holder.middleContainer.unlock();

            holder.awayAbbr.setText(game.getAwayTeam().getAbbr());
            holder.homeAbbr.setText(game.getHomeTeam().getAbbr());
            holder.awayName.setText(game.getAwayTeam().getName());
            holder.homeName.setText(game.getHomeTeam().getName());

            holder.awayScore.setText(String.valueOf(game.getAwayTeam().getScore()));
            holder.homeScore.setText(String.valueOf(game.getHomeTeam().getScore()));

            holder.awayPrediction.setScore(prediction.getAwayScore());
            holder.homePrediction.setScore(prediction.getHomeScore());

            Log.d(TAG, "Game is pregame: " + game.isPregame());
            int visibility;
            /* Visibility of score display. */
            if (game.isPregame()) visibility = View.GONE;
            else                  visibility = View.VISIBLE;
            holder.awayScore.setVisibility(visibility);
            holder.homeScore.setVisibility(visibility);

            /* Visibility of prediction display. */
            if (game.isLocked() && !prediction.isComplete()) {
                visibility = View.GONE;

                holder.unhighlightAway();
                holder.unhighlightHome();
            } else {
                visibility = View.VISIBLE;

                switch (prediction.winner()) {
                    case Prediction.W_AWAY:
                        holder.highlightAway(game);
                        holder.unhighlightHome();
                        break;
                    case Prediction.W_HOME:
                        holder.unhighlightAway();
                        holder.highlightHome(game);
                        break;
                    default:
                        holder.unhighlightAway();
                        holder.unhighlightHome();
                }
            }
            holder.awayPrediction.setVisibility(visibility);
            holder.homePrediction.setVisibility(visibility);

            if (game.isPregame()) {
                holder.middleContainer.pregameDisplay(game);
            } else if (game.isFinal()) {
                holder.middleContainer.finalDisplay(game);
            } else {
                holder.middleContainer.inProgressDisplay(game);
            }

            return convertView;
        }
    }

    static class ViewHolder {

        LobbyListItemMiddleContainer middleContainer;

        TextView awayAbbr;
        TextView homeAbbr;
        TextView awayName;
        TextView homeName;

        LinearLayout awayNameContainer;
        LinearLayout homeNameContainer;

        TextView awayScore;
        TextView homeScore;

        PredictionView awayPrediction;
        PredictionView homePrediction;

        void highlightAway(AtomicGame game) {
            awayAbbr.setTextColor(ColorUtil.WHITE);
            awayName.setTextColor(ColorUtil.WHITE);
            awayNameContainer.setBackgroundColor(game.getAwayTeam().getPrimaryColor());
            // awayPrediction.solidBackground(ColorUtil.WHITE, game.getAwayTeam().getPrimaryColor());
            awayPrediction.strokedBackground(game.getAwayTeam().getPrimaryColor(), game.getAwayTeam().getPrimaryColor());
        }

        void highlightHome(AtomicGame game) {
            homeAbbr.setTextColor(ColorUtil.WHITE);
            homeName.setTextColor(ColorUtil.WHITE);
            homeNameContainer.setBackgroundColor(game.getHomeTeam().getPrimaryColor());
            // homePrediction.solidBackground(ColorUtil.WHITE, game.getHomeTeam().getPrimaryColor());
            homePrediction.strokedBackground(game.getHomeTeam().getPrimaryColor(), game.getHomeTeam().getPrimaryColor());
        }

        void unhighlightAway() {
            awayAbbr.setTextColor(ColorUtil.STANDARD_TEXT);
            awayName.setTextColor(ColorUtil.STANDARD_TEXT);
            awayNameContainer.setBackgroundColor(ColorUtil.TRANSPARENT);
            awayPrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
        }

        void unhighlightHome() {
            homeAbbr.setTextColor(ColorUtil.STANDARD_TEXT);
            homeName.setTextColor(ColorUtil.STANDARD_TEXT);
            homeNameContainer.setBackgroundColor(ColorUtil.TRANSPARENT);
            homePrediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
        }
    }

}
