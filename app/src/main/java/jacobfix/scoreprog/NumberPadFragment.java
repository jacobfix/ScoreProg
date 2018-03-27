package jacobfix.scoreprog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class NumberPadFragment extends Fragment {

    private static final String TAG = NumberPadFragment.class.getSimpleName();

    private LinearLayout mBuffer;
    private LinearLayout mTeamNameContainer;
    private TextView mTeamName;
    private GridLayout numberGrid;
    private NumberPadFragmentListener listener;

    private FloatingActionButton confirmButton;
    // private FloatingActionButton clearButton;
    private Button clearButton;
    private Button cancelButton;

    private static final @IdRes int[] KEY_IDS = {
            R.id.key_1, R.id.key_2, R.id.key_3,
            R.id.key_4, R.id.key_5, R.id.key_6,
            R.id.key_7, R.id.key_8, R.id.key_9,
            R.id.key_cancel, R.id.key_0, R.id.key_enter
    };

    public static NumberPadFragment newInstance() {
        return new NumberPadFragment();
    }

    public static NumberPadFragment newInstance(String text, int teamColor, int bufferColor) {
        NumberPadFragment fragment = new NumberPadFragment();
        Bundle args = new Bundle();
        args.putString("header", text);
        args.putInt("teamColor", teamColor);
        args.putInt("bufferColor", bufferColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listener = (NumberPadFragmentListener) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_number_pad_new_new, container, false);

        mBuffer = ViewUtil.findById(view, R.id.buffer);
        mBuffer.setBackgroundColor(getArguments().getInt("bufferColor"));
        mBuffer.setAlpha(1.0f);

        mTeamNameContainer = ViewUtil.findById(view, R.id.header);

        mTeamName = ViewUtil.findById(view, R.id.header_text);
        mTeamName.setTypeface(FontHelper.getYantramanavBold(getContext()));
        // mTeamName.setTextColor(ContextCompat.getColor(getContext(), R.color.standard_text));
        mTeamName.setTextColor(getArguments().getInt("teamColor"));
        mTeamName.setText(getArguments().getString("header"));

        this.numberGrid = ViewUtil.findById(view, R.id.number_grid);
        numberGrid.setPadding(0, 0, 0, 0);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPadFragment.this.listener.keyPressed((Key) v.getTag());
            }
        };

        @IdRes int[] numberedKeyIds = {R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3,
                R.id.key_4, R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9};
        Key[] keyValues = Key.values();

        Button button;
        for (int i = 0; i < numberedKeyIds.length; i++) {
            button = ViewUtil.findById(view, numberedKeyIds[i]);
            button.setTypeface(FontHelper.getArimoRegular(getContext()));
            button.setTextColor(ColorUtil.STANDARD_TEXT);
            button.setOnClickListener(onClickListener);
            button.setTag(keyValues[i]);
        }

        confirmButton = ViewUtil.findById(view, R.id.confirm_button);
        confirmButton.setOnClickListener(onClickListener);
        confirmButton.setTag(Key.KEY_ENTER);
        confirmButton.setBackgroundTintList(ColorStateList.valueOf(getArguments().getInt("teamColor")));

        clearButton = ViewUtil.findById(view, R.id.key_clear);
        clearButton.setOnClickListener(onClickListener);
        clearButton.setTag(Key.KEY_CLEAR);
        // clearButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), android.R.color.darker_gray)));

        cancelButton = ViewUtil.findById(view, R.id.key_cancel);
        cancelButton.setOnClickListener(onClickListener);
        cancelButton.setTag(Key.KEY_CANCEL);

        return view;
    }

    public void setBufferColor(int color) {
        mBuffer.setBackgroundColor(color);
    }

    public interface NumberPadFragmentListener {
        void keyPressed(Key k);
    }

    public enum Key {
        KEY_0,
        KEY_1,
        KEY_2,
        KEY_3,
        KEY_4,
        KEY_5,
        KEY_6,
        KEY_7,
        KEY_8,
        KEY_9,
        KEY_ENTER,
        KEY_CLEAR,
        KEY_CANCEL
    }
}
