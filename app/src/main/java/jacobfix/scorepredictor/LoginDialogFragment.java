package jacobfix.scorepredictor;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.LoginTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class LoginDialogFragment extends DialogFragment {

    private static final String TAG = LoginDialogFragment.class.getSimpleName();

    private TextView mTitle;

    private EditText mUsernameEmailField;
    private EditText mPasswordField;

    private Button mLoginButton;
    private Button mRegisterButton;
    private Button mSkipLoginButton;

    public static LoginDialogFragment newInstance() {
        LoginDialogFragment f = new LoginDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login_dialog_two, container, false);

        /*
        mTitle = ViewUtil.findById(v, R.id.title);
        mTitle.setTypeface(FontHelper.getPermanentMarker(getContext()));
        */
        TextView titleComp1 = ViewUtil.findById(v, R.id.title_comp1);
        titleComp1.setTypeface(FontHelper.getAntonRegular(getContext()));
        ViewUtil.applyDeboss(titleComp1);

        TextView titleComp2 = ViewUtil.findById(v, R.id.title_comp2);
        titleComp2.setTypeface(FontHelper.getAntonRegular(getContext()));
        ViewUtil.applyDeboss(titleComp2);

        ((TextView) ViewUtil.findById(v, R.id.not_logged_in)).setTypeface(FontHelper.getArimoRegular(getContext()));
        ((TextView) ViewUtil.findById(v, R.id.more_fun)).setTypeface(FontHelper.getArimoRegular(getContext()));
        ((TextView) ViewUtil.findById(v, R.id.already_have_one)).setTypeface(FontHelper.getArimoRegular(getContext()));

        mUsernameEmailField = ViewUtil.findById(v, R.id.username_email);
        mPasswordField = ViewUtil.findById(v, R.id.password);
        mLoginButton = ViewUtil.findById(v, R.id.btn_login);
        mRegisterButton = ViewUtil.findById(v, R.id.btn_register);
        mSkipLoginButton = ViewUtil.findById(v, R.id.btn_skip_login);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Check that fields are filled. */
                Log.d(TAG, "LOG IN button clicked");
                boolean complete = true;

                String usernameEmail = mUsernameEmailField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();

                if (usernameEmail.isEmpty()) {
                    /* Highlight username/email field. */
                    complete = false;
                }

                if (password.isEmpty()) {
                    /* Highlight password field. */
                    complete = false;
                }

                if (!complete)
                    return;

                // TODO: Show progress dialog
                // int loginStatus = Login.login(usernameEmail, password);
                Log.d(TAG, "About to run OriginalLoginTask");
                new LoginTask(usernameEmail, password, new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        int loginStatus = (int) task.getResult();
                        Log.d(TAG, "loginStatus: " + loginStatus);
                        switch (loginStatus) {
                            case LoginTask.LOGIN_ERROR_NONE:
                                dismiss(); // TODO: Ensure that this is the only way to dismiss the dialog
                                break;
                            case LoginTask.LOGIN_ERROR_INVALID_PASSWORD:
                                mPasswordField.setText(R.string.empty_string);
                                break;
                            default:
                                Log.d(TAG, "Did not successfully log in");
                        }
                        // TODO: Hide progress dialog
                    }
                }).start();
            }
        });

        mSkipLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Dummy "me" user? How do we deal with friend retrieval if the user is not logged in?
                Log.d(TAG, "Skip login button clicked");
                /* Ad hoc OriginalLoginTask. */
                new BaseTask() {
                    @Override
                    public void execute() {
                        final String userId = "1";

                        /*
                        OriginalUserProvider.getUserDetails(Arrays.asList(new String[]{userId}), new AsyncCallback<Map<String, User>>() {
                            @Override
                            public void onSuccess(Map<String, User> result) {
                                User thisUser = (User) result.values().toArray()[0];
                                Log.d(TAG, "Logged in as " + thisUser);
                                LocalAccountManager.set(thisUser);
                                OriginalUserProvider.setDetailsToSync(Collections.singletonList(userId));
                                dismiss();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "There was a sync error");
                            }
                        });
                        */
                    }
                }.start();
            }
        });
        return v;
    }
}
