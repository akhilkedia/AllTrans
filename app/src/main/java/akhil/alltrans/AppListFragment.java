package akhil.alltrans;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
    public static FragmentActivity context;
    public static ListView listview;


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


        settings = this.getActivity().getSharedPreferences(getString(R.string.globalPrefFile), MODE_WORLD_READABLE);
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
        editor.putString("ClientID", "alltranstestid1");
        editor.putString("ClientSecret", "01234567890123456789");
        editor.commit();

        listview = (ListView) getView().findViewById(R.id.AppsList);

        new PrepareAdapter().execute();

        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setNestedScrollingEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationInfo curApp = (ApplicationInfo) parent.getItemAtPosition(position);
                Log.i("Akhil", curApp.packageName);
                LocalPreferenceFragment localPreferenceFragment = new LocalPreferenceFragment();
                localPreferenceFragment.applicationInfo = curApp;
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.toReplace, localPreferenceFragment)
                        .addToBackStack(null)
                        .commit();
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

            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            checkBox.setTag(position);
            System.out.println("For package " + packageName + " ");
            if (settings.contains(packageName)) {
                checkBox.setChecked(true);
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox1 = (CheckBox) v;
                    if (checkBox1.isChecked()) {
                        checkBox1.getTag();
                        int position = (Integer) checkBox1.getTag();
                        Log.i("AllTrans", "AllTrans: CheckBox clicked!" + values.get(position).packageName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(values.get(position).packageName, true);
                        editor.commit();
                    } else {
                        checkBox1.getTag();
                        int position = (Integer) checkBox1.getTag();
                        Log.i("AllTrans", "AllTrans: CheckBox clicked!" + values.get(position).packageName);
                        if (settings.contains(values.get(position).packageName)) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove(values.get(position).packageName);
                            editor.commit();
                        }
                    }
                }
            });

            return rowView;
        }

    }

    private class PrepareAdapter extends AsyncTask<Void, Void, StableArrayAdapter> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AppListFragment.context);
            dialog.setMessage("Loading List of Applications - Please Wait");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected StableArrayAdapter doInBackground(Void... params) {
            final ArrayList<String> list = new ArrayList<String>();
            final PackageManager pm = AppListFragment.context.getPackageManager();
            //get a list of installed apps.
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            Collections.sort(packages, new Comparator<ApplicationInfo>() {
                public int compare(ApplicationInfo a, ApplicationInfo b) {
                    if (settings.contains(a.packageName) && !settings.contains(b.packageName))
                        return -1;
                    if (!settings.contains(a.packageName) && settings.contains(b.packageName))
                        return 1;
                    String labela = pm.getApplicationLabel(a).toString().toLowerCase();
                    String labelb = pm.getApplicationLabel(b).toString().toLowerCase();
                    return labela.compareTo(labelb);
                }
            });


            final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, packages);
            return adapter;
        }

        protected void onPostExecute(StableArrayAdapter adapter) {
            AppListFragment.listview.setAdapter(adapter);
            dialog.dismiss();
        }
    }
}

