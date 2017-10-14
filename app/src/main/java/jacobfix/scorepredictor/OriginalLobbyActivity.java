package jacobfix.scorepredictor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import jacobfix.scorepredictor.sync.OriginalGameProvider;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SortGamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class OriginalLobbyActivity extends AppCompatActivity {

    private static final String TAG = OriginalLobbyActivity.class.getSimpleName();

    private ArrayList<NflGame> sortedGames;

    private ArrayList<NflGame> mSortedGames = new ArrayList<>();
    private ListView mGamesList;
    private GamesAdapter mGamesListAdapter;

    private AsyncCallback<NflGame[]> activeGameSyncListener;
    private SyncListener mNflOracleSyncListener;

    private LayoutInflater mInflater;
    private DialogFragment mLoginDialog;

    private static int mDefaultTextColor;
    private static int mDefaultBackgroundColor;

    private static final String GAME_ID_EXTRA = "game_id";

    private static final int PREDICTION_BOX_PADDING = 8;

    private static final int MIDDLE_CLOCK = 1;
    private static final int MIDDLE_PREGAME_FINAL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(StartupActivity.EXTRA_FIRST_LAUNCH, false)) {
            /* Show first launch dialog. */
            Log.d(TAG, "FIRST LAUNCH/NEW VERSION DIALOG DISPLAYED");
            showLoginDialog();
        } else if (!intent.getBooleanExtra(StartupActivity.EXTRA_LOGGED_IN, false)) {
            /* Show login dialog. */
            showLoginDialog();
            Log.d(TAG, "LOGIN DIALOG DISPLAYED");
        } else {
            /* Procedure for logged in user. */
            Log.d(TAG, "USER IS LOGGED IN");
        }

        sortedGames = new ArrayList<>();
        mSortedGames = new ArrayList<NflGame>();
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initializeActionBar();
        initializeViews();
        initializeListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lobby, menu);
        // MenuItem menuItem = menu.findItem(R.id.action_change_source);
        // getMenuInflater().inflate(R.menu.change_source_submenu, menuItem.getSubMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                mNflGameSyncManager.sync();
                return true;
            case R.id.action_change_source_live:
                mNflGameSyncManager.setSource(NflGameSyncManager.SOURCE_NFL_WEBSITE);
                mNflGameSyncManager.sync();
                return true;
            case R.id.action_change_source_local_final:
                mNflGameSyncManager.setSource(NflGameSyncManager.SOURCE_LOCAL_FINAL);
                mNflGameSyncManager.sync();
                return true;
            case R.id.action_change_source_local_in_progress:
                mNflGameSyncManager.setSource(NflGameSyncManager.SOURCE_LOCAL_IN_PROGRESS); // IN PROGRESS
                mNflGameSyncManager.sync();
                return true;
            case R.id.action_change_source_local_pregame:
                mNflGameSyncManager.setSource(NflGameSyncManager.SOURCE_LOCAL_PREGAME);
                mNflGameSyncManager.sync();
                return true;
        } */
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        // NflGameOracle.getInstance().registerSyncListener(mNflOracleSyncListener);
        // OriginalGameProvider.registerActiveGamesSyncListener(activeGameSyncListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        // NflGameOracle.getInstance().unregisterSyncListener(mNflOracleSyncListener);
        // OriginalGameProvider.unregisterActiveGamesSyncListener(activeGameSyncListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: Check if the sync listener was really unregistered
    }

    private void initializeActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setCustomView(R.layout.lobby_title_view);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        mGamesList = ViewUtil.findById(this, R.id.games_list);

        mGamesListAdapter = new GamesAdapter();
        mGamesList.setAdapter(mGamesListAdapter);
        mGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switchToGameActivity(mGamesListAdapter.getItem(i).getGameId());
            }
        });
    }

    private void initializeListeners() {
        /* mNflOracleSyncListener = new SyncListener() {
            @Override
            public void onSyncFinished() {
                Log.d(TAG, "Sync Finished in Lobby");
                // TODO: Maybe we should only sort it once, on the first sync, so that the list items aren't jumping around
                sortGamesList();
            }

            @Override
            public void onSyncError() {
                // Show a dialog
            }
        }; */
        /*
        activeGameSyncListener = new AsyncCallback<NflGame[]>() {
            @Override
            public void onSuccess(NflGame[] result) {
                Log.d(TAG, "Returned from active game sync");
                Log.d(TAG, "Length: " + result.length);
                new SortGamesTask(Arrays.asList(result), new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        if (task.errorOccurred()) {
                            Log.e(TAG, task.getError().toString());
                            return;
                        }
                        sortedGames = (ArrayList<NflGame>) task.getResult();
                        mGamesListAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Just updated list with newly sorted games");
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        */
    }

    private void switchToGameActivity(String gameId) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_ID_EXTRA, gameId);
        startActivity(intent);
    }

    private void showLoginDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mLoginDialog = LoginDialogFragment.newInstance();
        mLoginDialog.show(ft, "login_dialog");
    }

    private void hideLoginDialog() {
        if (mLoginDialog != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mLoginDialog);
        }
    }

    class GamesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sortedGames.size();
        }

        @Override
        public NflGame getItem(int position) {
            return sortedGames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_lobby_new, null);

                ViewHolder holder = new ViewHolder();

                // Find views
                holder.gameTimeContainer = ViewUtil.findById(convertView, R.id.game_time_container);
                holder.pregameFinalContainer = ViewUtil.findById(convertView, R.id.pregame_final_container);
                holder.successFailureContainer = ViewUtil.findById(convertView, R.id.success_failure_container);
                holder.success = ViewUtil.findById(convertView, R.id.success);
                holder.failure = ViewUtil.findById(convertView, R.id.failure);
                holder.pregameFinal = ViewUtil.findById(convertView, R.id.pregame_final);
                holder.clock = ViewUtil.findById(convertView, R.id.clock);
                holder.quarter = ViewUtil.findById(convertView, R.id.quarter);
                holder.lock = ViewUtil.findById(convertView, R.id.lock);
                holder.awayTeamNameContainer = ViewUtil.findById(convertView, R.id.away_team_name_container);
                holder.homeTeamNameContainer = ViewUtil.findById(convertView, R.id.home_team_name_container);
                holder.awayTeamAbbr = ViewUtil.findById(convertView, R.id.away_team_abbr);
                holder.homeTeamAbbr = ViewUtil.findById(convertView, R.id.home_team_abbr);
                holder.awayTeamName = ViewUtil.findById(convertView, R.id.away_team_name);
                holder.homeTeamName = ViewUtil.findById(convertView, R.id.home_team_name);
                holder.awayTeamScore = ViewUtil.findById(convertView, R.id.away_team_score);
                holder.homeTeamScore = ViewUtil.findById(convertView, R.id.home_team_score);
                holder.awayTeamPrediction = ViewUtil.findById(convertView, R.id.away_team_prediction_container);
                holder.homeTeamPrediction = ViewUtil.findById(convertView, R.id.home_team_prediction_container);
                // holder.awayTeamStripe = ViewUtil.findById(convertView, R.id.away_team_stripe);
                // holder.homeTeamStripe = ViewUtil.findById(convertView, R.id.home_team_stripe);

                // Set typefaces
                holder.clock.setTypeface(FontHelper.getYantramanavRegular(OriginalLobbyActivity.this));
                holder.quarter.setTypeface(FontHelper.getYantramanavRegular(OriginalLobbyActivity.this));
                holder.awayTeamAbbr.setTypeface(FontHelper.getWorkSansBold(OriginalLobbyActivity.this));
                holder.homeTeamAbbr.setTypeface(FontHelper.getWorkSansBold(OriginalLobbyActivity.this));
                // holder.awayTeamAbbr.setTypeface(FontHelper.getArimoBold(OriginalLobbyActivity.this));
                // holder.homeTeamAbbr.setTypeface(FontHelper.getArimoBold(OriginalLobbyActivity.this));
                holder.awayTeamName.setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));
                holder.homeTeamName.setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));
                holder.awayTeamScore.setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));
                holder.homeTeamScore.setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));
                holder.awayTeamPrediction.getTextView().setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));
                holder.homeTeamPrediction.getTextView().setTypeface(FontHelper.getArimoRegular(OriginalLobbyActivity.this));

                // Set prediction box text size
                holder.awayTeamPrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.awayTeamScore.getTextSize());
                holder.homeTeamPrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.homeTeamScore.getTextSize());

                // Set padding
                int scorePadding = PREDICTION_BOX_PADDING + 2;
                holder.awayTeamScore.setPadding(scorePadding, 0, scorePadding, 0);
                holder.homeTeamScore.setPadding(scorePadding, 0, scorePadding, 0);
                holder.awayTeamPrediction.setPadding(PREDICTION_BOX_PADDING);
                holder.homeTeamPrediction.setPadding(PREDICTION_BOX_PADDING);
                // holder.awayTeamPrediction.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.light_gray));
                // holder.homeTeamPrediction.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.light_gray));

                // Redraw prediction boxes
                holder.awayTeamPrediction.resize();
                holder.homeTeamPrediction.resize();

                convertView.setTag(holder);
            }

            NflGame game = getItem(position);
            NflTeam awayTeam = game.getAwayTeam();
            NflTeam homeTeam = game.getHomeTeam();

            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (game.isPredicted()) {
                holder.showPredictions(true);
                NflTeam predictedWinner = game.getPredictedWinner();
                if (predictedWinner == null) {

                } else if (predictedWinner.isHome()) {
                    setHomeSideColor(holder, homeTeam.getPrimaryColor(), Color.WHITE);
                    // setAwaySideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                } else {
                    setAwaySideColor(holder, awayTeam.getPrimaryColor(), Color.WHITE);
                    // setHomeSideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                }
                holder.awayTeamPrediction.setColor(awayTeam.getPrimaryColor());
                holder.homeTeamPrediction.setColor(homeTeam.getPrimaryColor());
                holder.awayTeamPrediction.getTextView().setText(String.valueOf(awayTeam.getPredictedScore()));
                holder.homeTeamPrediction.getTextView().setText(String.valueOf(homeTeam.getPredictedScore()));
            } else {
                holder.showPredictions(false);
                // setAwaySideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                // setHomeSideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
            }
            if (game.isPregame()) {
                holder.showScores(false);
                if (!game.isPredicted()) {
                    holder.setScoreVisibility(View.INVISIBLE);
                }
                holder.showSuccessFailure(false);
                holder.setMiddleContents(MIDDLE_PREGAME_FINAL);
                holder.pregameFinal.setText(R.string.pregame);
            } else {
                holder.showScores(true);
                if (game.inProgress()) {
                    holder.showSuccessFailure(false);
                    holder.setMiddleContents(MIDDLE_CLOCK);
                } else if (game.isFinal()) {
                    if (game.isPredicted()) {
                        holder.showSuccessFailure(true);
                        boolean correct = game.wasPredictedCorrectly();
                        if (correct) {
                            holder.indicateSuccess(true);
                        } else {
                            holder.indicateSuccess(false);
                        }
                    } else {
                        holder.showSuccessFailure(false);
                    }
                    holder.setMiddleContents(MIDDLE_PREGAME_FINAL);
                    holder.pregameFinal.setText(R.string.final_);
                }
            }
            /*
            } else {
                holder.gameTimeContainer.setVisibility(View.GONE);
                holder.pregameFinal.setVisibility(View.VISIBLE);
                holder.pregameFinal.setText(R.string.pregame);
                if (!game.isPredicted()) {
                    holder.awayTeamScore.setVisibility(View.INVISIBLE);
                    holder.homeTeamScore.setVisibility(View.INVISIBLE);
                } else {
                    holder.awayTeamScore.setVisibility(View.GONE);
                    holder.homeTeamScore.setVisibility(View.GONE);
                }
            }*/

            holder.clock.setText(game.getClock());
            holder.quarter.setText(Util.formatQuarter(getResources(), game.getQuarter()));
            holder.awayTeamAbbr.setText(awayTeam.getAbbr());
            holder.homeTeamAbbr.setText(homeTeam.getAbbr());
            holder.awayTeamName.setText(awayTeam.getName());
            holder.homeTeamName.setText(homeTeam.getName());
            holder.awayTeamScore.setText(String.valueOf(awayTeam.getScore()));
            holder.homeTeamScore.setText(String.valueOf(homeTeam.getScore()));
            // holder.awayTeamStripe.setBackgroundColor(awayTeam.getPrimaryColor());
            // holder.homeTeamStripe.setBackgroundColor(homeTeam.getPrimaryColor());

            /*
            if (game.isPredicted()) {
                NflTeam predictedWinner = game.getPredictedWinner();
                if (predictedWinner == null) {

                } else if (predictedWinner.isHome()) {
                    setHomeSideColor(holder, homeTeam.getPrimaryColor(), Color.WHITE);
                    setAwaySideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                } else {
                    setAwaySideColor(holder, awayTeam.getPrimaryColor(), Color.WHITE);
                    setHomeSideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                }
                holder.awayTeamPrediction.setColor(awayTeam.getPrimaryColor());
                holder.homeTeamPrediction.setColor(homeTeam.getPrimaryColor());
                holder.awayTeamPrediction.getTextView().setText(String.valueOf(awayTeam.getPredictedScore()));
                holder.homeTeamPrediction.getTextView().setText(String.valueOf(homeTeam.getPredictedScore()));
                holder.awayTeamPrediction.setVisibility(View.VISIBLE);
                holder.homeTeamPrediction.setVisibility(View.VISIBLE);
            } else {
                setAwaySideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                setHomeSideColor(holder, mDefaultBackgroundColor, mDefaultTextColor);
                holder.awayTeamPrediction.setVisibility(View.GONE);
                holder.homeTeamPrediction.setVisibility(View.GONE);
            }
            */
            return convertView;
        }

        private void setAwaySideColor(ViewHolder holder, int backgroundColor, int textColor) {
            holder.awayTeamNameContainer.setBackgroundColor(backgroundColor);
            holder.awayTeamAbbr.setTextColor(textColor);
            holder.awayTeamName.setTextColor(textColor);
        }

        private void setHomeSideColor(ViewHolder holder, int backgroundColor, int textColor) {
            holder.homeTeamNameContainer.setBackgroundColor(backgroundColor);
            holder.homeTeamAbbr.setTextColor(textColor);
            holder.homeTeamName.setTextColor(textColor);
        }
    }

    static class ViewHolder {
        LinearLayout gameTimeContainer;
        LinearLayout pregameFinalContainer;
        FrameLayout successFailureContainer;
        ImageView success;
        ImageView failure;
        TextView pregameFinal;
        TextView clock;
        TextView quarter;
        ImageView lock;
        LinearLayout awayTeamNameContainer;
        LinearLayout homeTeamNameContainer;
        TextView awayTeamAbbr;
        TextView homeTeamAbbr;
        TextView awayTeamName;
        TextView homeTeamName;
        TextView awayTeamScore;
        TextView homeTeamScore;
        PredictionView awayTeamPrediction;
        PredictionView homeTeamPrediction;
        View awayTeamStripe;
        View homeTeamStripe;

        void showScores(boolean show) {
            if (show) {
                awayTeamScore.setVisibility(View.VISIBLE);
                homeTeamScore.setVisibility(View.VISIBLE);
            } else {
                awayTeamScore.setVisibility(View.GONE);
                homeTeamScore.setVisibility(View.GONE);
            }
        }

        void showPredictions(boolean show) {
            if (show) {
                awayTeamPrediction.setVisibility(View.VISIBLE);
                homeTeamPrediction.setVisibility(View.VISIBLE);
            } else {
                awayTeamPrediction.setVisibility(View.GONE);
                homeTeamPrediction.setVisibility(View.GONE);
            }
        }

        void setMiddleContents(int indicator) {
            switch (indicator) {
                case MIDDLE_CLOCK:
                    gameTimeContainer.setVisibility(View.VISIBLE);
                    pregameFinalContainer.setVisibility(View.GONE);
                    break;
                case MIDDLE_PREGAME_FINAL:
                    gameTimeContainer.setVisibility(View.GONE);
                    pregameFinalContainer.setVisibility(View.VISIBLE);
                    break;
            }
        }

        void setScoreVisibility(int visibility) {
            awayTeamScore.setVisibility(visibility);
            homeTeamScore.setVisibility(visibility);
        }

        void setPredictionVisibility(int visibility) {
            awayTeamPrediction.setVisibility(visibility);
            homeTeamPrediction.setVisibility(visibility);
        }

        void showSuccessFailure(boolean show) {
            if (show) {
                successFailureContainer.setVisibility(View.VISIBLE);
            } else {
                successFailureContainer.setVisibility(View.GONE);
            }
        }

        void indicateSuccess(boolean _success) {
            if (_success) {
                success.setVisibility(View.VISIBLE);
                failure.setVisibility(View.GONE);
            } else {
                success.setVisibility(View.GONE);
                failure.setVisibility(View.VISIBLE);
            }
        }
    }
}
