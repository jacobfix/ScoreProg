package jacobfix.scoreprog.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class ErrorMessageInputField extends TextInputLayout {

    private TextInputEditText field;

    private boolean showingError;

    public ErrorMessageInputField(Context context) {
        this(context, null);
    }

    public ErrorMessageInputField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorMessageInputField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        field = ViewUtil.findById(this, R.id.field);

        setHintEnabled(true);

        field.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        field.setTextColor(ColorUtil.WHITE);
        field.setHintTextColor(ColorUtil.WHITE);
        field.getBackground().setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               if (showingError) {
                   hideError();
               }
            }
        });
    }

    public void setInputType(int inputType) {
        field.setInputType(inputType);
    }

    public void setIcon(@DrawableRes int drawable) {
        Drawable icon = ResourcesCompat.getDrawable(getResources(), drawable, null);
        icon.setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);
        field.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    public void showError() {
        showingError = true;
        field.getBackground().setColorFilter(ColorUtil.ERROR_RED, PorterDuff.Mode.SRC_IN);
        Drawable errorIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_error_outline_black_24dp, null);
        errorIcon.setColorFilter(ColorUtil.ERROR_RED, PorterDuff.Mode.SRC_IN);
        field.setCompoundDrawablesWithIntrinsicBounds(field.getCompoundDrawables()[0], null, errorIcon, null);
    }

    public void showError(String message) {
        showError();
        setErrorEnabled(true);
        setError(message);
    }

    public void hideError() {
        showingError = false;
        setErrorEnabled(false);
        setError(null);
        field.getBackground().setColorFilter(ColorUtil.WHITE, PorterDuff.Mode.SRC_IN);
        field.setCompoundDrawablesWithIntrinsicBounds(field.getCompoundDrawables()[0], null, null, null);
    }

    public Editable getText() {
        return field.getText();
    }
}
