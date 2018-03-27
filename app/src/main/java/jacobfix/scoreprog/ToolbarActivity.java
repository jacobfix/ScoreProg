package jacobfix.scoreprog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class ToolbarActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        toolbarTitle = ViewUtil.findById(this, R.id.title);

        toolbarTitle.setTypeface(FontHelper.getYellowTailRegular(this));

        int statusBarHeight = ViewUtil.getStatusBarHeight(this);
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        toolbar.getLayoutParams().height += statusBarHeight;
        toolbar.requestLayout();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    protected void setHomeButtonEnabled(boolean enabled) {
        getSupportActionBar().setHomeButtonEnabled(enabled);
    }


}
