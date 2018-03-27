package jacobfix.scoreprog;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scoreprog.components.ErrorMessageInputField;
import jacobfix.scoreprog.components.LoadingDialogFragment;
import jacobfix.scoreprog.server.ServerException;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

import static jacobfix.scoreprog.ApplicationContext.getContext;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private RelativeLayout root;

    private TextView title;

    private ErrorMessageInputField usernameEmailField;
    private ErrorMessageInputField passwordField;

    private Button signInButton;
    private Button registerButton;

    private LoadingDialogFragment loadingDialog;

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAILURE_WRONG_PASSWORD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_final);

        initializeViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initializeViews() {
        root = ViewUtil.findById(this, R.id.root);

        usernameEmailField = ViewUtil.findById(this, R.id.username_email_field);
        usernameEmailField.setHint("Username or email");
        usernameEmailField.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        usernameEmailField.setIcon(R.drawable.ic_perm_identity_black_24dp);

        passwordField = ViewUtil.findById(this, R.id.password_field);
        passwordField.setHint("Password");
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setIcon(R.drawable.ic_lock_outline_black_24dp);

        signInButton = ViewUtil.findById(this, R.id.sign_in_button);
        signInButton.setTypeface(FontHelper.getYantramanavBold(this));

        registerButton = ViewUtil.findById(this, R.id.register_button);
        registerButton.setTypeface(FontHelper.getYantramanavBold(this));

        title = ViewUtil.findById(this, R.id.title);
        title.setTypeface(FontHelper.getLobsterRegular(this));

        ((TextView) ViewUtil.findById(this, R.id.no_account_text)).setTypeface(FontHelper.getYantramanavRegular(this));

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean valid = true;

                String usernameEmail = usernameEmailField.getText().toString().trim();
                if (usernameEmail.isEmpty()) {
                    usernameEmailField.showError();
                    valid = false;
                }

                String password = passwordField.getText().toString().trim();
                if (password.isEmpty()) {
                    passwordField.showError();
                    valid = false;
                }

                if (!valid)
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
        showLoadingDialog();
        new LoginTask(usernameEmail, password, new TaskFinishedListener<LoginTask>() {
            @Override
            public void onTaskFinished(LoginTask task) {
                hideLoadingDialog();
                if (task.errorOccurred()) {
                    Snackbar.make(root, R.string.login_error, Snackbar.LENGTH_LONG);
                    Log.e(TAG, task.getError().toString());
                    return;
                }

                switch(task.getResult()) {
                    case LOGIN_SUCCESS:
                        switchToLobbyActivity();
                        break;

                    case LOGIN_FAILURE_WRONG_PASSWORD:
                        passwordField.showError("Username and password do not match");
                        break;

                    default:
                        throw new RuntimeException();
                }
            }
        }).start();
    }

    private void showLoadingDialog() {
        loadingDialog = new LoadingDialogFragment();
        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
    }

    private void hideLoadingDialog() {
        loadingDialog.dismiss();
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

    class LoginTask extends BaseTask<Integer> {

        String usernameEmail;
        String password;

        public LoginTask(String usernameEmail, String password, TaskFinishedListener listener) {
            super(listener);
            this.usernameEmail = usernameEmail;
            this.password = password;
        }

        @Override
        public void execute() {
            try {
                Login.LoginResult result = Login.login(usernameEmail, password, Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches());

                if (result.successful()) {
                    Login.setupLocalAccount(result.userId, result.token);
                    Login.setLocalCredentials(ApplicationContext.getContext(), result.userId, result.token);
                    setResult(LOGIN_SUCCESS);
                } else if (result.error instanceof ServerException) {
                    ServerException serverFault = (ServerException) result.error;
                    switch (serverFault.getErrno()) {
                        case ServerException.ERR_WRONG_PASSWD:
                            setResult(LOGIN_FAILURE_WRONG_PASSWORD);
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
