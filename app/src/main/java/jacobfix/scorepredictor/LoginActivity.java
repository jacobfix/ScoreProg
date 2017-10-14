package jacobfix.scorepredictor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Collections;
import java.util.Map;

import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.AuthenticateTask;
import jacobfix.scorepredictor.task.AuthenticateTask.AuthenticationResult;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.LoginTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private TextView title;

    private TextInputLayout usernameEmailFieldContainer;
    private TextInputEditText usernameEmailField;

    private TextInputLayout passwordFieldContainer;
    private TextInputEditText passwordField;

    private Button signInButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new_new);

        initializeViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initializeViews() {
        usernameEmailField = ViewUtil.findById(this, R.id.username_email_field);
        passwordField = ViewUtil.findById(this, R.id.password_field);

        usernameEmailFieldContainer = ViewUtil.findById(this, R.id.username_email_field_container);
        passwordFieldContainer = ViewUtil.findById(this, R.id.password_field_container);

        usernameEmailField.getCompoundDrawables()[0].setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);
        passwordField.getCompoundDrawables()[0].setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);

        signInButton = ViewUtil.findById(this, R.id.sign_in_button);
        registerButton = ViewUtil.findById(this, R.id.register_button);

        signInButton.setTypeface(FontHelper.getYantramanavBold(this));
        registerButton.setTypeface(FontHelper.getYantramanavBold(this));

        title = ViewUtil.findById(this, R.id.title);
        title.setTypeface(FontHelper.getLobsterRegular(this));

        ((TextView) ViewUtil.findById(this, R.id.no_account_text)).setTypeface(FontHelper.getYantramanavRegular(this));

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Sign in button clicked");
                boolean incomplete = false;

                String usernameEmail = usernameEmailField.getText().toString().trim();
                if (usernameEmail.isEmpty()) {
                    enableError(usernameEmailFieldContainer, usernameEmailField);
                    incomplete = true;
                }

                String password = passwordField.getText().toString().trim();
                if (password.isEmpty()) {
                    enableError(passwordFieldContainer, passwordField);
                    incomplete = true;
                }

                if (incomplete)
                    return;

                login(usernameEmail, password);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToRegisterActivity();
            }
        });
    }

    private void login(String usernameEmail, String password) {
        new AuthenticateTask(usernameEmail, password, new TaskFinishedListener<AuthenticateTask>() {
            @Override
            public void onTaskFinished(AuthenticateTask task) {
                AuthenticationResult result = task.getResult();
                if (result.authenticated()) {
                    Log.d(TAG, "Successfully authenticated");
                    final boolean[] retrieved = new boolean[2];
                    UserProvider.getUserDetails(result.getUserId(), new AsyncCallback<UserDetails>() {
                        @Override
                        public void onSuccess(UserDetails result) {
                            LocalAccountManager.get().setUserDetails(result);
                            retrieved[0] = true;
                            if (retrieved[1])
                                switchToLobbyActivity();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    });

                    UserProvider.getFriends(result.getUserId(), new AsyncCallback<Friends>() {
                        @Override
                        public void onSuccess(Friends result) {
                            LocalAccountManager.get().setFriends(result);
                            retrieved[1] = true;
                            if (retrieved[0])
                                switchToLobbyActivity();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    });
                } else {
                    Log.d(TAG, "Authentication failed");
                    switch (result.getError()) {
                        case USERNAME_NO_MATCH:
                        case EMAIL_NO_MATCH:
                        case INVALID_PASSWORD:
                            passwordFieldContainer.setErrorEnabled(true);
                            passwordFieldContainer.setError("Username/email and password did not match");
                            break;
                        case INSUFFICIENT_PARAMS:
                        case DATABASE_FAILURE:
                            Log.e(TAG, "An internal error occurred!");
                            break;
                    }
                }
            }
        }).start();
    }

    private void setupUser(String userId) {
        Log.d(TAG, "Setting up user with ID " + userId);
    }

    private void switchToLobbyActivity() {
        Intent intent = new Intent(this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void enableError(final TextInputLayout container, final TextInputEditText field) {
        int errorRed = Color.parseColor("#e0002a");

        field.getBackground().setColorFilter(errorRed, PorterDuff.Mode.SRC_IN);
        Drawable errorIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_error_outline_black_24dp, null);
        errorIcon.setColorFilter(errorRed, PorterDuff.Mode.SRC_IN);
        field.setCompoundDrawablesWithIntrinsicBounds(field.getCompoundDrawables()[0], null, errorIcon, null);

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                field.getBackground().setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);
                field.setCompoundDrawablesWithIntrinsicBounds(field.getCompoundDrawables()[0], null, null, null);
                field.removeTextChangedListener(this);
            }
        });
    }
}
