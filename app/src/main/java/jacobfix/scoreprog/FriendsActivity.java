package jacobfix.scoreprog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jacobfix.scoreprog.components.LoadingDialogFragment;
import jacobfix.scoreprog.task.AcceptFriendRequestTask;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.CancelFriendRequestTask;
import jacobfix.scoreprog.task.DeclineFriendRequestTask;
import jacobfix.scoreprog.task.DeleteFriendTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class FriendsActivity extends AppCompatActivity {

    private static final String TAG = FriendsActivity.class.getSimpleName();

    private List<RelationDetails> friends = new ArrayList<>();

    private View root;

    private Toolbar toolbar;
    private ListView list;
    private TextView emptyListText;
    private FriendsAdapter friendsAdapter = new FriendsAdapter();

    private LoadingDialogFragment loadingDialog;

    private AsyncCallback friendSyncCallback = new AsyncCallback() {
        @Override
        public void onSuccess(Object result) {
            update(currentState);
        }

        @Override
        public void onFailure(Exception e) {
            Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
            Log.e(TAG, e.toString());
        }
    };

    private State currentState = State.FRIENDS_AND_PENDING;

    public enum State {
        FRIENDS_AND_PENDING,
        ONLY_PENDING,
        ONLY_BLOCKED
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        initializeViews();

        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar, "Friends", FontHelper.getYantramanavBold(this), 24, ColorUtil.STANDARD_COLOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        update(State.FRIENDS_AND_PENDING);
        LocalAccountManager.get().registerFriendsSyncListener(friendSyncCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalAccountManager.get().unregisterFriendsSyncListener(friendSyncCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.send_friend_request:
                switchToFindUsersActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        root = ViewUtil.findById(this, R.id.root);

        list = ViewUtil.findById(this, R.id.list);
        list.setAdapter(friendsAdapter);

        emptyListText = ViewUtil.findById(this, R.id.empty_list_text);
        // emptyListText.setText(R.string.no_friends);
        emptyListText.setText("");
//        SpannableString firstPart = new SpannableString("Add friends using the  ");
//        SpannableString secondPart = new SpannableString("  icon above.");
//        Drawable d = getResources().getDrawable(R.drawable.ic_person_add_black_24dp);
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//        if (Build.VERSION.SDK_INT >= 21)
//            d.setTint(emptyListText.getCurrentTextColor());
//        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//        firstPart.setSpan(span, firstPart.length() - 1, firstPart.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        emptyListText.setText(TextUtils.concat(firstPart, secondPart));
    }

    private void update(State state) {
        currentState = state;
        final List<RelationDetails> collected = new ArrayList<>();
        switch (state) {
            case FRIENDS_AND_PENDING:
                collected.addAll(LocalAccountManager.get().friends().getConfirmedFriends());
                collected.addAll(LocalAccountManager.get().friends().getPendingFriends());
                break;

            case ONLY_PENDING:
                collected.addAll(LocalAccountManager.get().friends().getPendingFriends());
                break;

            case ONLY_BLOCKED:
                collected.addAll(LocalAccountManager.get().friends().getBlockedUsers());
                break;
        }
        friends = collected;
        friendsAdapter.notifyDataSetChanged();
        if (friends.size() > 0) {
            list.setVisibility(View.VISIBLE);
            emptyListText.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            emptyListText.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Just updated friends list");
    }

    private void showLoadingDialog() {
        loadingDialog = new LoadingDialogFragment();
        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
    }

    private void hideLoadingDialog() {
        loadingDialog.dismiss();
    }

    private void switchToFindUsersActivity() {
        Intent intent = new Intent(this, FindUsersActivity.class);
        startActivity(intent);
    }

    class FriendsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public RelationDetails getItem(int position) {
            return friends.get(position);
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
                holder.pendingNotice = ViewUtil.findById(convertView, R.id.pending_notice);
                holder.actionButton = ViewUtil.findById(convertView, R.id.action_button);

                ViewUtil.findById(convertView, R.id.request_friend_button).setVisibility(View.GONE);

                holder.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final RelationDetails relationDetails = getItem(position);

                        PopupMenu popupMenu = new PopupMenu(FriendsActivity.this, holder.actionButton);
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
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG);
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        LocalAccountManager.get().friends().removeRelatedUser(relationDetails);
                                                        update(currentState);
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
                                                        update(currentState);
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
                                                        update(currentState);
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
                                                            Snackbar.make(root, R.string.server_error, Snackbar.LENGTH_LONG);
                                                            Log.e(TAG, task.getError().toString());
                                                            return;
                                                        }
                                                        LocalAccountManager.get().friends().removeRelatedUser(relationDetails);
                                                        update(currentState);
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

                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            RelationDetails relationDetails = getItem(position);
            holder.username.setText(relationDetails.getUsername());

            switch (relationDetails.getRelationStatus()) {
                case Friends.CONFIRMED:
                    holder.pendingNotice.setVisibility(View.GONE);
                    break;

                case Friends.PENDING_SENT:
                    holder.pendingNotice.setVisibility(View.VISIBLE);
                    break;

                case Friends.PENDING_RECEIVED:
                    holder.pendingNotice.setVisibility(View.VISIBLE);
                    break;

                case Friends.BLOCKED:

                    break;
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView username;
        TextView pendingNotice;
        ImageView actionButton;
    }
}
