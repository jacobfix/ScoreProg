package jacobfix.scorepredictor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SortGamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyFragment extends Fragment {

    private static final String TAG = LobbyFragment.class.getSimpleName();


    private ArrayList<NflGame> sorted = new ArrayList<>();
    private String[] gameIds;

    private ListView list;
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

        list = ViewUtil.findById(view, R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switchToGameActivity(sorted.get(i).getGameId());
            }
        });

        inflater = inf;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() w/ " + Util.join(Arrays.asList(gameIds), ","));
        ArrayList<NflGame> unsorted = new ArrayList<>();
        for (String gid : gameIds) {
            NflGame game = Schedule.getGame(gid);
            if (game != null)
                unsorted.add(game);
        }
        // TODO: Show loading circle before list items appear?
        new SortGamesTask(unsorted, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                sorted = (ArrayList<NflGame>) task.getResult();
                adapter.notifyDataSetChanged();
            }
        }).start();
    }

    private void switchToGameActivity(String gameId) {
        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra("game", gameId);
        getActivity().startActivity(intent);
    }

    class LobbyFragmentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sorted.size();
        }

        @Override
        public NflGame getItem(int position) {
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

                holder.awayScore = ViewUtil.findById(convertView, R.id.away_score);
                holder.homeScore = ViewUtil.findById(convertView, R.id.home_score);

                holder.awayPrediction = ViewUtil.findById(convertView, R.id.away_prediction);
                holder.homePrediction = ViewUtil.findById(convertView, R.id.home_prediction);

                holder.awayAbbr.setTypeface(FontHelper.getYantramanavBold(getContext()));
                holder.homeAbbr.setTypeface(FontHelper.getYantramanavBold(getContext()));
                holder.awayName.setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.homeName.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                holder.awayScore.setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.homeScore.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                holder.awayPrediction.setTextSize(28);
                holder.homePrediction.setTextSize(28);

                // holder.awayPrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
                // holder.homePrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
                // holder.awayPrediction.resize();
                // holder.homePrediction.resize();

                convertView.setTag(holder);
            }

            NflGame game = getItem(position);

            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.middleContainer.setStartTime(game.getStartTimeDisplay(), game.getMeridiem());
            holder.middleContainer.setClock(game.getClock());
            holder.middleContainer.setQuarter(game.getQuarter());

            if (game.isLocked()) holder.middleContainer.lock();
            else                 holder.middleContainer.unlock();

            holder.awayPrediction.setVisibility(View.GONE);
            holder.homePrediction.setVisibility(View.GONE);

            if (game.isPregame()) {
                holder.middleContainer.pregameDisplay();
                // TODO: Check if it's predicted and draw the prediction views as necessary
            } else if (game.isFinal()) {
                holder.middleContainer.finalDisplay();
            } else {
                holder.middleContainer.inProgressDisplay();
            }

            holder.awayAbbr.setText(game.getAwayTeam().getAbbr());
            holder.homeAbbr.setText(game.getHomeTeam().getAbbr());
            holder.awayName.setText(game.getAwayTeam().getName());
            holder.homeName.setText(game.getHomeTeam().getName());

            holder.awayScore.setText(String.valueOf(game.getAwayTeam().getScore()));
            holder.homeScore.setText(String.valueOf(game.getHomeTeam().getScore()));

            int standardText = ContextCompat.getColor(getContext(), R.color.standard_text);
            holder.awayPrediction.strokedBackground(standardText, standardText);
            holder.homePrediction.strokedBackground(standardText, standardText);

            holder.awayPrediction.getTextView().setText("22");
            holder.homePrediction.getTextView().setText("22");

            return convertView;
        }
    }

    static class ViewHolder {

        LobbyListItemMiddleContainer middleContainer;

        TextView awayAbbr;
        TextView homeAbbr;
        TextView awayName;
        TextView homeName;

        TextView awayScore;
        TextView homeScore;

        PredictionView awayPrediction;
        PredictionView homePrediction;
    }

}
