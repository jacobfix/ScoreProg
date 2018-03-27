package jacobfix.scoreprog;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.renderscript.ScriptGroup;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import jacobfix.scoreprog.components.ErrorMessageInputField;
import jacobfix.scoreprog.components.LoadingDialogFragment;
import jacobfix.scoreprog.server.ServerException;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private RelativeLayout root;

    private TextView title;

    private ErrorMessageInputField usernameField;
    private ErrorMessageInputField emailField;
    private ErrorMessageInputField passwordField;
    private ErrorMessageInputField retypedPasswordField;

    private CheckBox privacyPolicyAgreement;

    private LoadingDialogFragment loadingDialogFragment;

    private Button registerButton;
    private Button signInButton;

    private static final int MINIMUM_USERNAME_LENGTH = 4;
    private static final int MAXIMUM_USERNAME_LENGTH = 20;
    private static final int MAXIMUM_EMAIL_LENGTH = 90;
    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    private static final int REGISTER_SUCCESS = 0;
    private static final int REGISTER_FAILURE_USERNAME_TAKEN = 1;
    private static final int REGISTER_FAILURE_EMAIL_TAKEN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_final);

        initializeViews();
    }

    private void initializeViews() {
        root = ViewUtil.findById(this, R.id.root);

        title = ViewUtil.findById(this, R.id.title);
        title.setTypeface(FontHelper.getLobsterRegular(this));

        usernameField = ViewUtil.findById(this, R.id.username_field);
        usernameField.setHint("Username");
        usernameField.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        usernameField.setIcon(R.drawable.ic_perm_identity_black_24dp);

        emailField = ViewUtil.findById(this, R.id.email_field);
        emailField.setHint("Email address");
        emailField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailField.setIcon(R.drawable.ic_mail_outline_black_24dp);

        passwordField = ViewUtil.findById(this, R.id.password_field);
        passwordField.setHint("Password");
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setIcon(R.drawable.ic_lock_outline_black_24dp);

        retypedPasswordField = ViewUtil.findById(this, R.id.retyped_password_field);
        retypedPasswordField.setHint("Retype password");
        retypedPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        retypedPasswordField.setIcon(R.drawable.ic_refresh_black_24dp);

        privacyPolicyAgreement = ViewUtil.findById(this, R.id.privacy_policy_agreement);
        privacyPolicyAgreement.setTypeface(FontHelper.getYantramanavRegular(this));

        registerButton = ViewUtil.findById(this, R.id.register_button);
        registerButton.setTypeface(FontHelper.getYantramanavBold(this));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Register button clicked");

                String username = usernameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString();
                String retypedPassword = retypedPasswordField.getText().toString();

                boolean usernameValid = validateUsername(username);
                boolean emailValid = validateEmail(email);
                boolean passwordValid = validatePassword(password, retypedPassword);

                boolean valid = usernameValid && emailValid && passwordValid;

                if (!valid)
                    return;

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
        showLoadingDialog();
        new RegisterTask(username, email, password, new TaskFinishedListener<RegisterTask>() {
            @Override
            public void onTaskFinished(RegisterTask task) {
                hideLoadingDialog();
                if (task.errorOccurred()) {
                    Snackbar.make(root, R.string.register_error, Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, task.getError().toString());
                    return;
                }

                switch (task.getResult()) {
                    case REGISTER_SUCCESS:
                        switchToLobbyActivity();
                        break;

                    case REGISTER_FAILURE_USERNAME_TAKEN:
                        usernameField.showError("Username is taken");
                        break;

                    case REGISTER_FAILURE_EMAIL_TAKEN:
                        emailField.showError("Email address is already associated with an account");
                        break;

                    default:
                        throw new RuntimeException();
                }
            }
        }).start();
    }

    private void showLoadingDialog() {
        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), "loading_dialog");
    }

    private void hideLoadingDialog() {
        loadingDialogFragment.dismiss();
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

    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            usernameField.showError();
            return false;
        }

        if (username.length() < MINIMUM_USERNAME_LENGTH) {
            usernameField.showError("Username must be at least " + MINIMUM_USERNAME_LENGTH + " characters in length");
        }

        if (username.length() > MAXIMUM_USERNAME_LENGTH) {
            usernameField.showError("Username can be at most " + MAXIMUM_USERNAME_LENGTH + " characters in length");
            return false;
        }

        if (username.matches("^.*[^a-zA-Z0-9_].*$")) {
            usernameField.showError("Username contains invalid characters");
            return false;
        }

        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailField.showError();
            return false;
        }

        if (email.length() > MAXIMUM_EMAIL_LENGTH) {
            emailField.showError("Email address can be at most " + MAXIMUM_EMAIL_LENGTH + " characters in length");
            return false;
        }

        if (email.matches("^.*[^a-zA-Z0-9_@.].*$")) {
            emailField.showError("Email address contains invalid characters");
        }

        return true;
    }

    private boolean validatePassword(String password, String retypedPassword) {
        boolean passwordMissing = false;
        if (password.isEmpty()) {
            passwordField.showError();
            passwordMissing = true;
        }

        if (retypedPassword.isEmpty()) {
            retypedPasswordField.showError();
            passwordMissing = true;
        }

        if (passwordMissing)
            return false;

        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            passwordField.showError("Password must be at least " + MINIMUM_PASSWORD_LENGTH + " characters in length");
            return false;
        }

        if (!password.equals(retypedPassword)) {
            retypedPasswordField.showError("Retyped password does not match original password");
            return false;
        }

        return true;
    }

    class RegisterTask extends BaseTask<Integer> {

        String username;
        String email;
        String password;

        public RegisterTask(String username, String email, String password, TaskFinishedListener listener) {
            super(listener);
            this.username = username;
            this.email = email;
            this.password = password;
        }

        @Override
        public void execute() {
            try {
                Login.LoginResult result = Login.register(username, email, password);

                if (result.successful()) {
                    Login.setupLocalAccount(result.userId, result.token);
                    setResult(REGISTER_SUCCESS);
                } else if (result.error instanceof ServerException) {
                    ServerException serverFault = (ServerException) result.error;
                    switch (serverFault.getErrno()) {
                        case ServerException.ERR_USERNAME_TAKEN:
                            setResult(REGISTER_FAILURE_USERNAME_TAKEN);
                            break;

                        case ServerException.ERR_EMAIL_TAKEN:
                            setResult(REGISTER_FAILURE_EMAIL_TAKEN);
                            break;

                        default:
                            throw serverFault;
                    }
                }
            } catch (Exception e) {
                reportError(e);
            }
        }
    }
}
