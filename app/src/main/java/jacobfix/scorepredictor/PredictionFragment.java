package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import jacobfix.scorepredictor.components.PredictionView;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RankPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Pair;
import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionFragment extends Fragment implements GameStateChangeListener {

    private static final String TAG = PredictionFragment.class.getSimpleName();

    private boolean isPregame;
    private String awayAbbr;
    private String homeAbbr;

    private ArrayList<Pair<Integer, PredictionFragmentListItem>> ranking = new ArrayList<>();

    private PredictionAdapter adapter = new PredictionAdapter();

    private PredictionFragmentListener listener;

    private RelativeLayout header;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_friend_prediction_list_new, container, false);

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

        FullGame game = listener.getGame();
        if (game != null)
            onGameStateChanged(game);

        setHeaderColor(listener.getScoreboardColor());

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

    public void updateParticipants(final Map<String, Prediction> participants) {
        UserProvider.getUserDetails(participants.keySet(), new AsyncCallback<Map<String, UserDetails>>() {
            @Override
            public void onSuccess(Map<String, UserDetails> userDetails) {
                new MakePredictionRankingTask(listener.getGame(), userDetails, participants, new TaskFinishedListener<MakePredictionRankingTask>() {
                    @Override
                    public void onTaskFinished(MakePredictionRankingTask task) {
                        if (task.errorOccurred()) {
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
                Log.e(TAG, e.toString());
            }
        });
    }

    @Override
    public void onPredictionChanged(Prediction p) {
        // loadingSymbol.getIndeterminateDrawable().setColorFilter();
    }

    @Override
    public void onGameStateChanged(Game game) {
        synchronized (game.acquireLock()) {
            isPregame = game.isPregame();
            awayAbbr = game.getAwayAbbr();
            homeAbbr = game.getHomeAbbr();
        }
        awayHeader.setText(awayAbbr);
        homeHeader.setText(homeAbbr);
    }

    class PredictionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_friend_prediction_new_new, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.findViews(convertView);

                holder.ranking.setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.username.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                holder.ranking.setTextColor(ColorUtil.STANDARD_TEXT);
                holder.username.setTextColor(ColorUtil.STANDARD_TEXT);

                holder.awayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.homeFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));

                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            Pair<Integer, PredictionFragmentListItem> pair = getItem(position);
            int rank = pair.getFirst();
            PredictionFragmentListItem info = pair.getSecond();

            holder.ranking.setText(String.valueOf(rank));
            holder.username.setText(info.username);

            if (isPregame) {
                /* Show the users who have submitted predictions, but don't display their predictions. */
                holder.awayFlipCard.setVisibility(View.INVISIBLE);
                holder.homeFlipCard.setVisibility(View.INVISIBLE);
                holder.spread.setVisibility(View.INVISIBLE);
                holder.spreadStatus.setVisibility(View.INVISIBLE);

                holder.ranking.setText("");
            } else {
                /* Display predictions. */
                holder.awayFlipCard.setVisibility(View.VISIBLE);
                holder.homeFlipCard.setVisibility(View.VISIBLE);
                holder.spread.setVisibility(View.VISIBLE);
                holder.spreadStatus.setVisibility(View.VISIBLE);

                if (info.userId.equals(LocalAccountManager.get().getId()))
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.me_in_ranking));
                else
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.friend_prediction_list_item_background));

                holder.awayFlipCard.setScore(info.awayPredictedScore);
                holder.homeFlipCard.setScore(info.homePredictedScore);

                holder.spread.setText(String.valueOf(info.spread));
                holder.spreadStatus.setProgress(100 - info.spread);

                int difference = info.awayPredictedScore - info.homePredictedScore;
                if (difference > 0) {
                    holder.awayFlipCard.solidBackground(ColorUtil.WHITE, info.awayColor);
                    holder.homeFlipCard.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
                    holder.spreadStatus.getProgressDrawable().setColorFilter(info.awayColor, PorterDuff.Mode.SRC_IN);
                } else if (difference == 0) {
                    holder.awayFlipCard.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
                    holder.homeFlipCard.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
                    holder.spreadStatus.getProgressDrawable().setColorFilter(ColorUtil.STANDARD_TEXT, PorterDuff.Mode.SRC_IN);
                } else {
                    holder.awayFlipCard.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
                    holder.homeFlipCard.solidBackground(ColorUtil.WHITE, info.homeColor);
                    holder.spreadStatus.getProgressDrawable().setColorFilter(info.homeColor, PorterDuff.Mode.SRC_IN);
                }
            }

            return convertView;
        }
    }

    static class MakePredictionRankingTask extends BaseTask<ArrayList<Pair<Integer, PredictionFragmentListItem>>> {

        FullGame game;
        Map<String, UserDetails> userDetails;
        Map<String, Prediction> predictions;

        public MakePredictionRankingTask(FullGame game, Map<String, UserDetails> userDetails, Map<String, Prediction> predictions, TaskFinishedListener listener) {
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
            this.userId = userDetails.getId();
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

        FullGame getGame();
        Prediction getPrediction();

        State getPredictionFragmentState();
        void showPredictionFragmentLoading();
        void showPredictionFragmentList();

        Map<String, Prediction> getParticipants();
    }

    public enum State {
        LOADING,
        LIST
    }

    static class ViewHolder {
        LinearLayout root;
        TextView ranking;
        ImageView profilePic;
        TextView username;

        PredictionView awayFlipCard;
        PredictionView homeFlipCard;
        TextView spread;
        ProgressBar spreadStatus;

        void findViews(View parent) {
            root = ViewUtil.findById(parent, R.id.root);
            ranking = ViewUtil.findById(parent, R.id.ranking);
            profilePic = ViewUtil.findById(parent, R.id.icon);
            username = ViewUtil.findById(parent, R.id.username);

            awayFlipCard = ViewUtil.findById(parent, R.id.away_prediction);
            homeFlipCard = ViewUtil.findById(parent, R.id.home_prediction);
            spread = ViewUtil.findById(parent, R.id.spread);
            spreadStatus = ViewUtil.findById(parent, R.id.spread_progress_bar);

            awayFlipCard.setTextSize(28);
            homeFlipCard.setTextSize(28);
            awayFlipCard.resize();
            homeFlipCard.resize();
        }
    }
}
