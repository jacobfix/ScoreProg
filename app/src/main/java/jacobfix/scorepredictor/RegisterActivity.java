package jacobfix.scorepredictor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RegisterTask;
import jacobfix.scorepredictor.task.RegisterTask.RegistrationResult;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private TextView title;

    private TextInputLayout usernameFieldContainer;
    private TextInputEditText usernameField;

    private TextInputLayout emailFieldContainer;
    private TextInputEditText emailField;

    private TextInputLayout passwordFieldContainer;
    private TextInputEditText passwordField;

    private TextInputLayout retypePasswordFieldContainer;
    private TextInputEditText retypePasswordField;

    private CheckBox privacyPolicyAgreement;

    private Button registerButton;
    private Button signInButton;

    private static final int USERNAME_FIELD = 1;
    private static final int EMAIL_FIELD = 2;
    private static final int PASSWORD_FIELD = 3;
    private static final int RETYPE_PASSWORD_FIELD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_new);

        initializeViews();
    }

    private void initializeViews() {
        title = ViewUtil.findById(this, R.id.title);
        title.setTypeface(FontHelper.getLobsterRegular(this));

        usernameFieldContainer = ViewUtil.findById(this, R.id.username_field_container);
        usernameField = ViewUtil.findById(this, R.id.username_field);
        usernameField.setTag(USERNAME_FIELD);
        usernameField.setTypeface(FontHelper.getYantramanavRegular(this));
        ViewUtil.setCompoundDrawablesColor(usernameField, ColorUtil.WHITE);

        emailFieldContainer = ViewUtil.findById(this, R.id.email_field_container);
        emailField = ViewUtil.findById(this, R.id.email_field);
        emailField.setTag(EMAIL_FIELD);
        emailField.setTypeface(FontHelper.getYantramanavRegular(this));
        ViewUtil.setCompoundDrawablesColor(emailField, ColorUtil.WHITE);

        passwordFieldContainer = ViewUtil.findById(this, R.id.password_field_container);
        passwordField = ViewUtil.findById(this, R.id.password_field);
        passwordField.setTag(PASSWORD_FIELD);
        passwordField.setTypeface(FontHelper.getYantramanavRegular(this));
        ViewUtil.setCompoundDrawablesColor(passwordField, ColorUtil.WHITE);

        retypePasswordFieldContainer = ViewUtil.findById(this, R.id.retype_password_field_container);
        retypePasswordField = ViewUtil.findById(this, R.id.retype_password_field);
        retypePasswordField.setTag(RETYPE_PASSWORD_FIELD);
        retypePasswordField.setTypeface(FontHelper.getYantramanavRegular(this));
        ViewUtil.setCompoundDrawablesColor(retypePasswordField, ColorUtil.WHITE);

        privacyPolicyAgreement = ViewUtil.findById(this, R.id.privacy_policy_agreement);
        privacyPolicyAgreement.setTypeface(FontHelper.getYantramanavRegular(this));

        /*
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                int field = (int) view.getTag();
                switch (field) {
                    case USERNAME_FIELD:
                        Log.d(TAG, "Username field focus: " + hasFocus);
                        setHelpText(usernameFieldContainer, hasFocus, "This is how you'll appear to other users");
                        break;

                    case EMAIL_FIELD:
                        Log.d(TAG, "Email field focus: " + hasFocus);
                        setHelpText(emailFieldContainer, hasFocus, "Your email address won't be visible to anybody else");
                        break;

                    case PASSWORD_FIELD:
                        Log.d(TAG, "Password field focus: " + hasFocus);
                        setHelpText(passwordFieldContainer, hasFocus, "Make your password at least 8 characters");
                        break;

                    case RETYPE_PASSWORD_FIELD:
                        Log.d(TAG, "Retype password field focus: " + hasFocus);
                        setHelpText(retypePasswordFieldContainer, hasFocus, "Just to be sure");
                        break;
                }
            }
        };

        usernameField.setOnFocusChangeListener(focusChangeListener);
        emailField.setOnFocusChangeListener(focusChangeListener);
        passwordField.setOnFocusChangeListener(focusChangeListener);
        retypePasswordField.setOnFocusChangeListener(focusChangeListener);
        */

        registerButton = ViewUtil.findById(this, R.id.register_button);
        registerButton.setTypeface(FontHelper.getYantramanavBold(this));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Register button clicked");

                boolean incomplete = false;

                String username = usernameField.getText().toString().trim();
                if (username.isEmpty()) {
                    enableError(usernameFieldContainer, usernameField, null);
                    incomplete = true;
                }

                String email = emailField.getText().toString().trim();
                if (email.isEmpty()) {
                    enableError(emailFieldContainer, emailField, null);
                    incomplete = true;
                }

                String password = passwordField.getText().toString().trim();
                if (password.isEmpty()) {
                    enableError(passwordFieldContainer, passwordField, null);
                    incomplete = true;
                }

                String retypedPassword = retypePasswordField.getText().toString().trim();
                if (retypedPassword.isEmpty()) {
                    enableError(retypePasswordFieldContainer, retypePasswordField, null);
                    incomplete = true;
                }

                if (incomplete)
                    return;

                if (!password.equals(retypedPassword)) {
                    enableError(retypePasswordFieldContainer, retypePasswordField, "Retyped password does not match original password");
                    return;
                }

                register(username, email, password);
            }
        });

        signInButton = ViewUtil.findById(this, R.id.sign_in_button);
        signInButton.setTypeface(FontHelper.getYantramanavBold(this));
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToLoginActivity();
            }
        });
    }

    private void register(String username, String email, String password) {
        // TODO: Check the number of accounts that have been made from this device
        new RegisterTask(username, email, password, new TaskFinishedListener<RegisterTask>() {
            @Override
            public void onTaskFinished(RegisterTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    return;
                }

                RegistrationResult result = task.getResult();
                if (result.registered()) {
                    Log.d(TAG, "Successfully registered");
                    final boolean[] retrieved = new boolean[2];
                    UserProvider.getUserDetails(result.getUserId(), new AsyncCallback<UserDetails>() {
                        @Override
                        public void onSuccess(UserDetails result) {
                            LocalAccountManager.get().setUserDetails(result);
                            retrieved[0]= true;
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
                    Log.d(TAG, "Did not register. ERROR: " + result.getError());
                    switch (result.getError()) {
                        case USERNAME_ALREADY_EXISTS:
                            enableError(usernameFieldContainer, usernameField, "Username is already in use");
                            break;
                    }
                }
            }
        }).start();
    }

    private void switchToLobbyActivity() {
        Intent intent = new Intent(this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void enableError(final TextInputLayout container, final TextInputEditText field, String errorString) {
        int errorRed = Color.parseColor("#ff4444");

        if (errorString != null) {
            container.setErrorEnabled(true);
            container.setError(errorString);
        }

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
                container.setErrorEnabled(false);
                container.setError(null);

                field.getBackground().setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);

                field.setCompoundDrawablesWithIntrinsicBounds(field.getCompoundDrawables()[0], null, null, null);
                field.removeTextChangedListener(this);
            }
        });
    }

    private void setHelpText(TextInputLayout container, boolean show, String text) {
        container.setErrorEnabled(show);
        if (show) container.setError(text);
        else      container.setError(null);
    }
}
