package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NumberPadFragment extends Fragment {

    private LinearLayout teamNameContainer;
    private TextView teamName;
    private GridLayout numberGrid;
    private NumberPadFragmentListener listener;

    private static final @IdRes int[] KEY_IDS = {
            R.id.key_1, R.id.key_2, R.id.key_3,
            R.id.key_4, R.id.key_5, R.id.key_6,
            R.id.key_7, R.id.key_8, R.id.key_9,
            R.id.key_cancel, R.id.key_0, R.id.key_enter
    };

    public static NumberPadFragment newInstance() {
        return new NumberPadFragment();
    }

    public static NumberPadFragment newInstance(String headerText, int headerColor) {
        NumberPadFragment fragment = new NumberPadFragment();
        Bundle args = new Bundle();
        args.putString("header", headerText);
        args.putInt("color", headerColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (NumberPadFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_number_pad, container, false);

        this.teamNameContainer = ViewUtil.findById(view, R.id.team_name_container);
        this.teamNameContainer.getBackground().setColorFilter(getArguments().getInt("color"), PorterDuff.Mode.SRC_IN);

        this.teamName = ViewUtil.findById(view, R.id.team_name);
        this.teamName.setTypeface(FontHelper.getArimoRegular(getContext()));
        this.teamName.setText(getArguments().getString("header"));

        // ((TextView) ViewUtil.findById(view, R.id.prediction_text)).setTypeface(FontHelper.getWorkSansRegular(getContext()));

        this.numberGrid = ViewUtil.findById(view, R.id.number_grid);

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
            button.setOnClickListener(onClickListener);
            button.setTag(keyValues[i]);
        }

        ImageButton imageButton;
        imageButton = ViewUtil.findById(view, R.id.key_cancel);
        imageButton.setOnClickListener(onClickListener);
        imageButton.setTag(Key.KEY_DELETE);

        imageButton = ViewUtil.findById(view, R.id.key_enter);
        imageButton.setOnClickListener(onClickListener);
        imageButton.setTag(Key.KEY_ENTER);

        return view;
    }

    public void setHeaderText(String headerText) {
        this.teamName.setText(headerText);
    }

    public void setHeaderColor(int headerColor) {
        ViewUtil.setTwoLayerRectangleColor((LayerDrawable) this.teamName.getBackground(), headerColor);
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
        KEY_DELETE
    }
}
