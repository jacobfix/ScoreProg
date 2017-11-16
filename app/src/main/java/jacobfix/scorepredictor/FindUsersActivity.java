package jacobfix.scorepredictor;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.FindMatchingUsernamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class FindUsersActivity extends AppCompatActivity {

    private static final String TAG = FindUsersActivity.class.getSimpleName();

    private Toolbar toolbar;

    private EditText searchField;
    private ImageButton searchButton;

    private ProgressBar loadingCircle;
    private ListView list;
    private FindUsersAdapter adapter;

    private ArrayList<UserDetails> suggestions = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users_new);

        initializeToolbar();
        initializeViews();

        showList();
    }

    private void initializeToolbar() {
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar);
        toolbar.setBackgroundColor(ColorUtil.STANDARD_COLOR);

        if (Build.VERSION.SDK_INT >= 21)
            toolbar.setElevation(ViewUtil.convertDpToPx(getResources(), 8));

        TextView toolbarTitle = ViewUtil.findById(this, R.id.title);
        toolbarTitle.setText("Find Users");
        toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        toolbarTitle.setTypeface(FontHelper.getYantramanavBold(this));
    }

    private void initializeViews() {
        searchField = ViewUtil.findById(this, R.id.search_field);
        searchButton = ViewUtil.findById(this, R.id.search_button);
        loadingCircle = ViewUtil.findById(this, R.id.loading_circle);
        list = ViewUtil.findById(this, R.id.list);

        adapter = new FindUsersAdapter();
        list.setAdapter(adapter);

        searchField.setTypeface(FontHelper.getYantramanavRegular(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchField.getText().toString();
                if (query.isEmpty())
                    return;

                showLoading();

                Log.d(TAG, "Searching for: " + query);
                new FindMatchingUsernamesTask(query, new TaskFinishedListener<FindMatchingUsernamesTask>() {
                    @Override
                    public void onTaskFinished(FindMatchingUsernamesTask task) {
                        if(task.errorOccurred()) {
                            return;
                        }

                        Map<String, String> pairs = task.getResult();
                        UserProvider.getUserDetails(pairs.values(), new AsyncCallback<Map<String, UserDetails>>() {
                            @Override
                            public void onSuccess(Map<String, UserDetails> result) {
                                Log.d(TAG, "" + result.size());
                                Log.d(TAG, result.toString());
                                suggestions.clear();
                                suggestions.addAll(result.values());
                                adapter.notifyDataSetChanged();
                                showList();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void showLoading() {
        loadingCircle.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
    }

    private void showList() {
        loadingCircle.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
    }

    class FindUsersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return suggestions.size();
        }

        @Override
        public UserDetails getItem(int position) {
            return suggestions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_find_users, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.findViews(convertView);

                holder.addFriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                convertView.setTag(holder);
            }

            UserDetails user = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.username.setText(user.getUsername());
            return convertView;
        }
    }

    static class ViewHolder {

        ImageView profilePic;
        TextView username;
        ImageButton addFriendButton;

        void findViews(View parent) {
            profilePic = ViewUtil.findById(parent, R.id.profile_pic);
            username = ViewUtil.findById(parent, R.id.username);
            addFriendButton = ViewUtil.findById(parent, R.id.add_friend);
        }
    }
}
