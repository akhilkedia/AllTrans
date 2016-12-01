package akhil.alltrans;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by akhil on 1/12/16.
 */

public class AppListFragment extends Fragment {

    public static SharedPreferences settings;
    public static Context context;

    public AppListFragment() {
    }

    public static AppListFragment newInstance() {
        return new AppListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.apps_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        context = this.getActivity();


        settings = this.getActivity().getSharedPreferences("AllTransPref", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("com.astroframe.seoulbus", true);
        editor.putBoolean("com.nhn.android.nmap", true);
        editor.putBoolean("com.kakao.taxi", true);
        editor.putBoolean("com.fineapp.yogiyo", true);
        editor.putBoolean("com.cgv.android.movieapp", true);
        editor.putBoolean("com.wooricard.smartapp", true);
        editor.putBoolean("com.google.android.talk", true);
        editor.putBoolean("com.ebay.global.gmarket", true);
        editor.putBoolean("com.foodfly.gcm", true);
        editor.putBoolean("com.ktcs.whowho", true);
        editor.remove("EnabledApps");
        editor.commit();

        final ListView listview = (ListView) getView().findViewById(R.id.AppsList);


        final ArrayList<String> list = new ArrayList<String>();
        final PackageManager pm = this.getActivity().getPackageManager();
//get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(packages, new Comparator<ApplicationInfo>() {
            public int compare(ApplicationInfo a, ApplicationInfo b) {
                String labela = pm.getApplicationLabel(a).toString().toLowerCase();
                String labelb = pm.getApplicationLabel(b).toString().toLowerCase();
                return labela.compareTo(labelb);
            }
        });


        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, packages);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setAdapter(adapter);
        listview.setNestedScrollingEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                list.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
    }

    private class StableArrayAdapter extends ArrayAdapter<ApplicationInfo> {

        final PackageManager pm = AppListFragment.context.getPackageManager();
        private final Context context;
        private final List<ApplicationInfo> values;
        HashMap<ApplicationInfo, Integer> mIdMap = new HashMap<ApplicationInfo, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<ApplicationInfo> packages) {
            super(context, textViewResourceId, packages);
            this.context = context;
            values = new LinkedList<ApplicationInfo>(packages);
            for (int i = 0; i < values.size(); ++i) {
                mIdMap.put(values.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            ApplicationInfo item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item, parent, false);

            String packageName = values.get(position).packageName;
            String label = (String) pm.getApplicationLabel(values.get(position));
            if (label.equals(null)) {
                label = packageName;
            }
            Drawable icon = pm.getApplicationIcon(values.get(position));

            TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
            textView.setText(label);

            TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
            textView2.setText(packageName);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            imageView.setImageDrawable(icon);

            return rowView;
        }

    }
}

