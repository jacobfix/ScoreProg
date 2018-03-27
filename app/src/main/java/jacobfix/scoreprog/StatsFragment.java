package jacobfix.scoreprog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import jacobfix.scoreprog.util.ViewUtil;

public class StatsFragment extends Fragment {

    // TODO: Make a Stats object to associate with each FullGame, into which all the states data will be read from JSON, and which will be passed to this fragment
    private ExpandableListView list;
    private StatsAdapter statsAdapter;

    public static StatsFragment newInstance() {
        return new StatsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        list = ViewUtil.findById(view, R.id.list);

        return view;
    }

    class StatsAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            return null;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 0;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
