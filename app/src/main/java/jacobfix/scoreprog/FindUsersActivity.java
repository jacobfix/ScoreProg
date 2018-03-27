package jacobfix.scoreprog;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jacobfix.scoreprog.components.LoadingDialogFragment;
import jacobfix.scoreprog.task.AcceptFriendRequestTask;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.CancelFriendRequestTask;
import jacobfix.scoreprog.task.DeclineFriendRequestTask;
import jacobfix.scoreprog.task.DeleteFriendTask;
import jacobfix.scoreprog.task.FindMatchingUsernamesTask;
import jacobfix.scoreprog.task.SendFriendRequestTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class FindUsersActivity extends AppCompatActivity {

    private static final String TAG = FindUsersActivity.class.getSimpleName();

    private List<RelationDetails> suggestions = new ArrayList<>();

    private View root;

    private Toolbar toolbar;

    private EditText searchField;
    private ImageView searchButton;

    private ListView list;
    private ProgressBar loadingIcon;
    private TextView emptyListText;

    private FindUsersAdapter adapter;

    private LoadingDialogFragment loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users_new_new);

        initializeToolbar();
        initializeViews();

        showList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeToolbar() {
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar, "Find Users", FontHelper.getYantramanavBold(this), 24, ColorUtil.STANDARD_COLOR);
    }

    private void initializeViews() {
        root = ViewUtil.findById(this, R.id.root);
        
        searchField = ViewUtil.findById(this, R.id.search_field);
        searchButton = ViewUtil.findById(this, R.id.search_button);

        list = ViewUtil.findById(this, R.id.list);
        loadingIcon = ViewUtil.findById(this, R.id.loading_circle);
        emptyListText = ViewUtil.findById(this, R.id.empty_list_text);

        adapter = new FindUsersAdapter();
        list.setAdapter(adapter);

        searchField.setTypeface(FontHelper.getYantramanavRegular(this));
        emptyListText.setTypeface(FontHelper.getYantramanavRegular(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String query = searchField.getText().toString().trim();
                if (query.isEmpty())
                    return;

                showLoading();

                new FindMatchingUsernamesTask(query, new TaskFinishedListener<FindMatchingUsernamesTask>() {
                    @Override
                    public void onTaskFinished(FindMatchingUsernamesTask task) {
                        if(task.errorOccurred()) {
                            Log.e(TAG, task.getError().toString());
                            return;
                        }

                        List<RelationDetails> result = new ArrayList<>(task.getResult());
                        if (result.isEmpty()) {
                            showText(String.format("'%s' does not match any known usernames. Try searching for something else.", query));
                        } else {
                            suggestions = result;
                            updateList();
                            showList();
                        }
                    }
                }).start();
            }
        });
    }

    private void updateList() {
        for (RelationDetails relationDetails : suggestions)
            relationDetails.setRelationStatus(LocalAccountManager.get().friends().getRelationStatus(relationDetails.getUserId()));
        adapter.notifyDataSetChanged();
    }

    private void showLoading() {
        list.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);
        emptyListText.setVisibility(View.GONE);
    }

    private void showList() {
        list.setVisibility(View.VISIBLE);
        loadingIcon.setVisibility(View.GONE);
        emptyListText.setVisibility(View.GONE);
    }

    private void showText(String text) {
        emptyListText.setText(text);

        list.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.GONE);
        emptyListText.setVisibility(View.VISIBLE);
    }

    private void showLoadingDialog() {
        loadingDialog = new LoadingDialogFragment();
        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
    }

    private void hideLoadingDialog() {
        loadingDialog.dismiss();
    }

    public static class Suggestion {
        public String userId;
        public String username;
        public int friendStatus;
    }

    class FindUsersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return suggestions.size();
        }

        @Override
        public RelationDetails getItem(int position) {
            return suggestions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_friend_simple, parent, false);
                final ViewHolder holder = new ViewHolder();

                holder.username = ViewUtil.findById(convertView, R.id.username);
                holder.username.setTypeface(FontHelper.getYantramanavRegular(FindUsersActivity.this));

                holder.menuButton = ViewUtil.findById(convertView, R.id.action_button);
                holder.menuButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final RelationDetails relationDetails = getItem(position);

                        PopupMenu popupMenu = new PopupMenu(FindUsersActivity.this, holder.menuButton);
                        switch (relationDetails.getRelationStatus()) {
                            case Friends.CONFIRMED:
                                popupMenu.getMenuInflater().inflate(R.menu.menu_friend_confirmed, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.delete_friend:
                                                showLoadingDialog();
                                                new DeleteFriendTask(relationDetails.getUserId(), new TaskFinishedListener() {
                                                    @Override
                                                    public void onTaskFinished(BaseTask task) {
                                                        hideLoadingDialog();
                                                        if (task.errorOccurred()) {
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG).show();
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        LocalAccountManager.get().friends().removeRelatedUser(relationDetails);
                                                        updateList();
                                                    }
                                                }).start();
                                                return true;
                                        }
                                        return true;
                                    }
                                });
                                break;

                            case Friends.PENDING_SENT:
                                popupMenu.getMenuInflater().inflate(R.menu.menu_friend_pending_sent, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.cancel_request:
                                                showLoadingDialog();
                                                new CancelFriendRequestTask(relationDetails.getUserId(), new TaskFinishedListener() {
                                                    @Override
                                                    public void onTaskFinished(BaseTask task) {
                                                        hideLoadingDialog();
                                                        if (task.errorOccurred()) {
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG).show();
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        LocalAccountManager.get().friends().removeRelatedUser(relationDetails);
                                                        updateList();
                                                    }
                                                }).start();
                                                return true;
                                        }
                                        return true;
                                    }
                                });
                                break;

                            case Friends.PENDING_RECEIVED:
                                popupMenu.getMenuInflater().inflate(R.menu.menu_friend_pending_received, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.accept_request:
                                                showLoadingDialog();
                                                new AcceptFriendRequestTask(relationDetails.getUserId(), new TaskFinishedListener() {
                                                    @Override
                                                    public void onTaskFinished(BaseTask task) {
                                                        hideLoadingDialog();
                                                        if (task.errorOccurred()) {
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG).show();
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        relationDetails.setRelationStatus(Friends.CONFIRMED);
                                                        LocalAccountManager.get().friends().addRelatedUser(relationDetails);
                                                        updateList();
                                                    }
                                                }).start();
                                                return true;

                                            case R.id.decline_request:
                                                showLoadingDialog();
                                                new DeclineFriendRequestTask(relationDetails.getUserId(), new TaskFinishedListener() {
                                                    @Override
                                                    public void onTaskFinished(BaseTask task) {
                                                        hideLoadingDialog();
                                                        if (task.errorOccurred()) {
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG).show();
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        LocalAccountManager.get().friends().removeRelatedUser(relationDetails);
                                                        updateList();
                                                    }
                                                }).start();
                                                return true;
                                        }
                                        return true;
                                    }
                                });
                                break;

                            default:
                                throw new RuntimeException();
                        }
                        popupMenu.show();
                    }
                });

                holder.requestFriendButton = ViewUtil.findById(convertView, R.id.request_friend_button);
                holder.requestFriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final RelationDetails relationDetails = getItem(position);
                        showLoadingDialog();
                        new SendFriendRequestTask(relationDetails.getUserId(), new TaskFinishedListener<SendFriendRequestTask>() {
                            @Override
                            public void onTaskFinished(SendFriendRequestTask task) {
                                hideLoadingDialog();
                                if (task.errorOccurred()) {
                                    Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG).show();
                                    Log.e(TAG, task.getError().toString());
                                    return;
                                }
                                relationDetails.setRelationStatus(Friends.PENDING_SENT);
                                LocalAccountManager.get().friends().addRelatedUser(relationDetails);
                                updateList();
                            }
                        }).start();
                    }
                });

                holder.pendingNotice = ViewUtil.findById(convertView, R.id.pending_notice);

                convertView.setTag(holder);
            }

            RelationDetails relationDetails = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();

            Log.d(TAG, relationDetails.getUsername() + " relation " + relationDetails.getRelationStatus());
            holder.username.setText(relationDetails.getUsername());
            switch (relationDetails.getRelationStatus()) {
                case Friends.NO_RELATION:
                    holder.menuButton.setVisibility(View.GONE);
                    holder.requestFriendButton.setVisibility(View.VISIBLE);

                    holder.pendingNotice.setVisibility(View.GONE);
                    break;

                case Friends.CONFIRMED:
                    holder.menuButton.setVisibility(View.VISIBLE);
                    holder.requestFriendButton.setVisibility(View.GONE);

                    holder.pendingNotice.setVisibility(View.GONE);
                    break;

                case Friends.PENDING_SENT:
                    holder.menuButton.setVisibility(View.VISIBLE);
                    holder.requestFriendButton.setVisibility(View.GONE);

                    holder.pendingNotice.setVisibility(View.VISIBLE);
                    break;

                case Friends.PENDING_RECEIVED:
                    holder.menuButton.setVisibility(View.VISIBLE);
                    holder.requestFriendButton.setVisibility(View.GONE);

                    holder.pendingNotice.setVisibility(View.VISIBLE);
                    break;
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView username;
        ImageView menuButton;
        ImageView requestFriendButton;
        TextView pendingNotice;
    }
}
