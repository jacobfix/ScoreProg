package jacobfix.scoreprog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import jacobfix.scoreprog.components.PlayFeedListItemContainer;
import jacobfix.scoreprog.util.ViewUtil;

public class PlayFeedFragment extends Fragment {

    private static final String TAG = PlayFeedFragment.class.getSimpleName();

    private ListView list;

    private PlayFeedFragmentListener listener;

    private ArrayList<Play> plays = new ArrayList<>();
    private PlayFeedAdapter adapter = new PlayFeedAdapter();

    public static PlayFeedFragment newInstance() {
        Log.d(TAG, "Creating new instance of PlayFeedFragment");
        return new PlayFeedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (PlayFeedFragmentListener) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_feed, container, false);

        list = ViewUtil.findById(view, R.id.list);
        list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "PlayFeedFragment onStart()");

        /* Check if GameFragment acquired its FullGame before this fragment loaded. If not, when
         * GameFragment does get its FullGame, it will call onFullGameStateChanged. */
//        FullGame game = listener.getGame();
//        if (game != null) {
//            Log.d(TAG, "FullGame was retrieved before PlayFeedFragment started");
//            onFullGameStateChanged(game);
//        }
        DriveFeed driveFeed = listener.getDriveFeed();
        if (driveFeed != null)
            updateDriveFeed(driveFeed);
    }

    public void updateDriveFeed(DriveFeed driveFeed) {
        plays = driveFeed.allPlays();
        adapter.notifyDataSetChanged();
    }

//    @Override
//    public void onFullGameStateChanged(FullGame game) {
//        plays = game.getDriveFeed().allPlays();
//        Log.d(TAG, "num plays: " + plays.size());
//        adapter.notifyDataSetChanged();
////        plays = new ArrayList<>();
////        for (int i = 0; i < 10; i++)
////            plays.add(Play.generateRandom());
////        adapter.notifyDataSetChanged();
//    }

    class PlayFeedAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return plays.size();
        }

        @Override
        public Play getItem(int position) {
            return plays.get(getCount() - 1 - position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_play_feed, parent, false);

                ViewHolder holder = new ViewHolder();
                holder.container = ViewUtil.findById(convertView, R.id.root);

                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            Play play = getItem(position);

            if (play.isGameInstance()) {
                holder.container.setLayoutAsGameInstance(play.getDescription());
            } else {
                holder.container.setLayoutAsPlayContent(play.getDown(), play.getYardsToGo(),
                        play.getYardLine(), play.getPosTeam(), play.deduceTitle(),
                        play.getDescription());
            }

            return convertView;
        }
    }

    public interface PlayFeedFragmentListener {
        Game getGame();
        DriveFeed getDriveFeed();
    }

    static class ViewHolder {
        PlayFeedListItemContainer container;
    }
}
