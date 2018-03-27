package jacobfix.scoreprog;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import jacobfix.scoreprog.components.PredictionListItemNameContainer;
import jacobfix.scoreprog.components.PredictionListItemPredictionContainer;
import jacobfix.scoreprog.components.SpreadView;
import jacobfix.scoreprog.sync.UserProvider;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.RankPredictionsTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.users.UserDetails;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Pair;
import jacobfix.scoreprog.util.ViewUtil;

public class PredictionFragment extends Fragment {

    private static final String TAG = PredictionFragment.class.getSimpleName();

    private boolean isPregame;
    private boolean isLocked;
    private String awayAbbr;
    private String homeAbbr;

    private ArrayList<Pair<Integer, PredictionFragmentListItem>> ranking = new ArrayList<>();

    private PredictionAdapter adapter = new PredictionAdapter();

    private PredictionFragmentListener listener;

    private ConstraintLayout header;
    private TextView awayHeader;
    private TextView homeHeader;
    private TextView spreadAbbr;

    private ListView list;
    private ProgressBar loadingSymbol;

    public static PredictionFragment newInstance() {
        return new PredictionFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = ((PredictionFragmentListener) getParentFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prediction, container, false);

        header = ViewUtil.findById(view, R.id.header);
        setHeaderColor(listener.getScoreboardColor());

        awayHeader = ViewUtil.findById(view, R.id.away_abbr);
        homeHeader = ViewUtil.findById(view, R.id.home_abbr);
        spreadAbbr = ViewUtil.findById(view, R.id.spread_abbr);

        awayHeader.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        homeHeader.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        spreadAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        list = ViewUtil.findById(view, R.id.list);
        loadingSymbol = ViewUtil.findById(view, R.id.loading_circle);

        list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        /* If the parent GameFragment has already acquired its FullGame by the time this fragment
           has been created. */
        Game game = listener.getGame();
        if (game != null)
            onGameStateChanged(game);

        /* If the parent GameFragment has already initialized its participants by the time this
           fragment has been created. */
        Participants participants = listener.getParticipants();
        if (participants != null)
            updateParticipants(participants);

        setHeaderColor(listener.getScoreboardColor());

        Log.d(TAG, "Getting PredictionFragment state: " + listener.getPredictionFragmentState());
        switch (listener.getPredictionFragmentState()) {
            case LOADING:
                Log.d(TAG, "Got GameFragment state: LOADING");
                showLoading();
                break;

            case LIST:
                Log.d(TAG, "Got GameFragment state: LIST");
                showList();
                break;
        }
    }

    public void showLoading() {
        loadingSymbol.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
    }

    public void showList() {
        list.setVisibility(View.VISIBLE);
        loadingSymbol.setVisibility(View.GONE);
    }

    public void setHeaderColor(int color) {
        header.setBackgroundColor(color & 0xc0ffffff);
    }

//    public void updateParticipants(final Map<String, Prediction> participants) {
//        UserProvider.getUserDetails(participants.keySet(), new AsyncCallback<Map<String, UserDetails>>() {
//            @Override
//            public void onSuccess(Map<String, UserDetails> userDetails) {
//                new MakePredictionRankingTask(listener.getGame(), userDetails, participants, new TaskFinishedListener<MakePredictionRankingTask>() {
//                    @Override
//                    public void onTaskFinished(MakePredictionRankingTask task) {
//                        if (task.errorOccurred()) {
//                            Log.e(TAG, task.getError().toString());
//                            return;
//                        }
//                        PredictionFragment.this.ranking = task.getResult();
//                        adapter.notifyDataSetChanged();
//                    }
//                }).start();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                Log.e(TAG, e.toString());
//            }
//        });
//    }

    public void updateParticipants(final Participants participants) {
        if (participants.isEmpty()) {
            ranking = new ArrayList<>();
            return;
        }

//        Map<String, RelationDetails> allRelationDetails = new HashMap<>();
//        for (String userId : participants.getUserIds()) {
//            if (LocalAccountManager.get().friends().getRelationStatus(userId) != Friends.NO_RELATION) {
//
//            }
//        }

        UserProvider.getUserDetails(participants.getUserIds(), new AsyncCallback<Map<String, UserDetails>>() {
            @Override
            public void onSuccess(Map<String, UserDetails> userDetails) {
                Log.d(TAG, "  UserDetails keys: " + userDetails.keySet());
                Log.d(TAG, "UserDetails values: " + userDetails.values());
                new MakePredictionRankingTask(listener.getGame(), userDetails, participants.getPredictions(), new TaskFinishedListener<MakePredictionRankingTask>() {
                    @Override
                    public void onTaskFinished(MakePredictionRankingTask task) {
                        if (task.errorOccurred()) {
                            Log.e(TAG, "Error occurred when make prediction ranking");
                            Log.e(TAG, task.getError().toString());
                            return;
                        }
                        PredictionFragment.this.ranking = task.getResult();
                        adapter.notifyDataSetChanged();
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error occurred when getting user details for the prediction ranking");
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        });
    }

    public void onGameStateChanged(Game game) {
        synchronized (game.acquireLock()) {
            isPregame = game.isPregame();
            isLocked = game.isLocked();
            awayAbbr = game.getAwayAbbr();
            homeAbbr = game.getHomeAbbr();
        }
        awayHeader.setText(awayAbbr);
        homeHeader.setText(homeAbbr);
    }

    class PredictionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            Log.d(TAG, "Getting size: " + ranking.size());
            return ranking.size();
        }

        @Override
        public Pair<Integer, PredictionFragmentListItem> getItem(int position) {
            return ranking.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_friend_prediction_constraint, parent, false);
                ViewHolder holder = new ViewHolder();

                holder.root = ViewUtil.findById(convertView, R.id.root);

                holder.nameContainer = ViewUtil.findById(convertView, R.id.name_container);

                holder.awayPredictionContainer = ViewUtil.findById(convertView,
                        R.id.away_prediction_container);
                holder.homePredictionContainer = ViewUtil.findById(convertView,
                        R.id.home_prediction_container);
                holder.spread = ViewUtil.findById(convertView, R.id.spread);

                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            Pair<Integer, PredictionFragmentListItem> pair = getItem(position);
            int rank = pair.getFirst();
            PredictionFragmentListItem info = pair.getSecond();

            holder.nameContainer.setRanking(rank);
            Log.d(TAG, "Username: " + info.username);
            holder.nameContainer.setUsername(info.username);

            if (!isLocked) {
                /* Show the users who have submitted predictions, but don't display their predictions. */
                holder.awayPredictionContainer.hidePrediction();
                holder.homePredictionContainer.hidePrediction();

                holder.spread.setVisibility(View.GONE);

                holder.nameContainer.hideRanking();
            } else {
                /* Display predictions. */
                holder.awayPredictionContainer.showPrediction();
                holder.homePredictionContainer.showPrediction();

                if (!isPregame)
                    holder.spread.setVisibility(View.VISIBLE);

                holder.nameContainer.showRanking();

                if (info.userId.equals(LocalAccountManager.get().userId))
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.me_in_ranking));
                else
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.friend_prediction_list_item_background));

                holder.awayPredictionContainer.setPredictedScore(info.awayPredictedScore);
                holder.homePredictionContainer.setPredictedScore(info.homePredictedScore);

                holder.spread.setSpread(info.spread);

                int difference = info.awayPredictedScore - info.homePredictedScore;
                if (difference > 0) {
                    holder.awayPredictionContainer.color(info.awayColor);
                    holder.homePredictionContainer.uncolor();
                    holder.spread.setColor(info.awayColor);
                } else if (difference == 0) {
                    holder.awayPredictionContainer.uncolor();
                    holder.homePredictionContainer.uncolor();
                    holder.spread.setColor(ColorUtil.STANDARD_TEXT);
                } else {
                    holder.awayPredictionContainer.uncolor();
                    holder.homePredictionContainer.color(info.homeColor);
                    holder.spread.setColor(info.homeColor);
                }
            }

            return convertView;
        }
    }

    // TODO: Add this user's prediction to the list!
    static class MakePredictionRankingTask extends BaseTask<ArrayList<Pair<Integer, PredictionFragmentListItem>>> {

        Game game;
        Map<String, UserDetails> userDetails;
        Map<String, Prediction> predictions;

        public MakePredictionRankingTask(Game game, Map<String, UserDetails> userDetails, Map<String, Prediction> predictions, TaskFinishedListener listener) {
            super(listener);
            this.game = game;
            this.userDetails = userDetails;
            this.predictions = predictions;
        }

        @Override
        public void execute() {
            try {
                Collection<Prediction> unsorted = predictions.values();

                Game.disableUpdates(game);
                Prediction.disableUpdates(unsorted);

                RankPredictionsTask rankPredictionsTask = new RankPredictionsTask(game, unsorted, null);
                rankPredictionsTask.execute();

                if (rankPredictionsTask.errorOccurred()) {
                    Game.enableUpdates(game);
                    Prediction.enableUpdates(unsorted);
                    throw rankPredictionsTask.getError();
                }

                ArrayList<Prediction> sortedPredictions = rankPredictionsTask.getResult();
                ArrayList<Pair<Integer, PredictionFragmentListItem>> ranking = new ArrayList<>();

                int currentRank = 1;
                Prediction first = sortedPredictions.get(0);
                ranking.add(new Pair<>(currentRank, new PredictionFragmentListItem(userDetails.get(first.getUserId()), first, game)));
                for (int i = 1; i < sortedPredictions.size(); i++) {
                    if (sortedPredictions.get(i).getSpread(game) != sortedPredictions.get(i - 1).getSpread(game))
                        currentRank++;
                    Prediction p = sortedPredictions.get(i);
                    ranking.add(new Pair<>(currentRank, new PredictionFragmentListItem(userDetails.get(p.getUserId()), p, game)));
                }

                Game.enableUpdates(game);
                Prediction.enableUpdates(unsorted);

                setResult(ranking);
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    static class PredictionFragmentListItem {

        String userId;
        String username;
        int awayPredictedScore;
        int homePredictedScore;
        int spread;

        int awayColor;
        int homeColor;

        public PredictionFragmentListItem(UserDetails userDetails, Prediction prediction, Game game) {
            this.userId = userDetails.getUserId();
            this.username = userDetails.getUsername();
            this.awayPredictedScore = prediction.getAwayScore();
            this.homePredictedScore = prediction.getHomeScore();
            this.spread = prediction.getSpread(game);
            this.awayColor = game.getAwayColor();
            this.homeColor = game.getHomeColor();
        }
    }

    public interface PredictionFragmentListener {

        int getScoreboardColor();

        Game getGame();
        Prediction getPrediction();

        State getPredictionFragmentState();
        void showPredictionFragmentLoading();
        void showPredictionFragmentList();

        Participants getParticipants();
    }

    public enum State {
        LOADING,
        LIST
    }

    static class ViewHolder {
        ConstraintLayout root;
        PredictionListItemNameContainer nameContainer;

        PredictionListItemPredictionContainer awayPredictionContainer;
        PredictionListItemPredictionContainer homePredictionContainer;

        FrameLayout spreadLayoutContainer;
        SpreadView spread;
    }
}
