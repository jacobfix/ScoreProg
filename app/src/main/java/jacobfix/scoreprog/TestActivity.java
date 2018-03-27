package jacobfix.scoreprog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item_friend_simple);

//        String string = "1st & 10";
//        SpannableString spannable = new SpannableString(string);
//        spannable.setSpan(new RelativeSizeSpan(0.25f), 5, 6, 0);
//        downTextView.setText(spannable);

//        float textSize = downTextView.getTextSize();
//
//        String first = "1st";
//        SpannableString s1 = new SpannableString(first);
//        s1.setSpan(new AbsoluteSizeSpan((int) textSize), 0, first.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        String ampersand = "&";
//        SpannableString s2 = new SpannableString(ampersand);
//        s2.setSpan(new AbsoluteSizeSpan((int) (0.75 * textSize)), 0, ampersand.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        String ten = "10";
//        SpannableString s3 = new SpannableString(ten);
//        s3.setSpan(new AbsoluteSizeSpan((int) textSize), 0, ten.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        CharSequence resultText = TextUtils.concat(s1, " ", s2, " ", s3);
//        downContainer.getDown().setText(resultText);

        // PlayFeedListItemDescriptionContainer container = ViewUtil.findById(this, R.id.description_container);
        // int necessaryHeight = container.findNecessaryHeight();
        // Log.d(TAG, "Necessary height: " + necessaryHeight);
        // ((ConstraintLayout) ViewUtil.findById(this, R.id.root)).getLayoutParams().height = necessaryHeight;

        /*
        MyToolbar toolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ScoreboardNameContainer awayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        ScoreboardNameContainer homeNameContainer = ViewUtil.findById(this, R.id.home_name_container);

        int outerPadding = (int) Util.dpToPx(this, 15);
        int innerPadding = (int) Util.dpToPx(this, 5);

        awayNameContainer.setPadding(outerPadding, 0, innerPadding, 0);
        homeNameContainer.setPadding(innerPadding, 0, outerPadding, 0);

        awayNameContainer.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        homeNameContainer.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        */
    }
}
