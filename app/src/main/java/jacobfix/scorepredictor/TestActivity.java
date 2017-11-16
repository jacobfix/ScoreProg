package jacobfix.scorepredictor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import jacobfix.scorepredictor.components.MyToolbar;
import jacobfix.scorepredictor.components.ScoreboardNameContainer;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class TestActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_new);

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
    }
}
