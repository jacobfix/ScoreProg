package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import jacobfix.scorepredictor.util.FontHelper;

public class NumberPadButton extends Button {

    private NumberPadFragment.Key value;

    public NumberPadButton(Context context) {
        this(context, null);
    }

    public NumberPadButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPadButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // initialize();
    }

    private void initialize() {
        setTypeface(FontHelper.getWorkSansRegular(getContext()));
    }

    public void setValue(NumberPadFragment.Key k) {
        this.value = k;
    }

    public NumberPadFragment.Key getValue() {
        return this.value;
    }
}
