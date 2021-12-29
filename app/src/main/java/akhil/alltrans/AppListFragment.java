/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

@SuppressWarnings("ALL")
public class AppListFragment extends Fragment {

    private static SharedPreferences settings;
    private FragmentActivity context;
    private android.widget.ListView listview;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.apps_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        context = this.getActivity();
        //noinspection ConstantConditions
        settings = this.getActivity().getSharedPreferences("AllTransPref", Context.MODE_PRIVATE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

//        if (BuildConfig.DEBUG) {
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putBoolean("codepath.apps.demointroandroid", true);
//            editor.putBoolean("com.astroframe.seoulbus", true);
//            editor.putBoolean("com.nhn.android.nmap", true);
//            editor.putBoolean("com.kakao.taxi", true);
//            editor.putBoolean("com.fineapp.yogiyo", true);
//            editor.putBoolean("com.cgv.android.movieapp", true);
//            editor.putBoolean("com.wooricard.smartapp", true);
//            editor.putBoolean("com.google.android.talk", true);
//            editor.putBoolean("com.ebay.global.gmarket", true);
//            editor.putBoolean("com.foodfly.gcm", true);
//            editor.putBoolean("com.ktcs.whowho", true);
//            editor.putBoolean("Debug", true);
//            editor.putString("SubscriptionKey", getString(R.string.microsoft_key));
//            editor.apply();
//        }

        //noinspection ConstantConditions
        listview = getView().findViewById(R.id.AppsList);

        new PrepareAdapter().execute();

        listview.setChoiceMode(android.widget.ListView.CHOICE_MODE_MULTIPLE);
        listview.setFastScrollEnabled(true);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationInfo curApp = (ApplicationInfo) parent.getItemAtPosition(position);
                utils.debugLog(curApp.packageName);
                LocalPreferenceFragment localPreferenceFragment = new LocalPreferenceFragment();
                localPreferenceFragment.applicationInfo = curApp;
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.toReplace, localPreferenceFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
        fireBaseAnalytics();
    }

    private void fireBaseAnalytics() {
        mFirebaseAnalytics.setUserProperty("Enabled", String.valueOf(settings.getBoolean("Enabled", false)));
        mFirebaseAnalytics.setUserProperty("TranslatorProvider", settings.getString("TranslatorProvider", "g"));
        mFirebaseAnalytics.setUserProperty("TranslateFromLanguage", settings.getString("TranslateFromLanguage", "ko"));
        mFirebaseAnalytics.setUserProperty("TranslateToLanguage", settings.getString("TranslateToLanguage", "en"));
    }

    private void fireBaseEnabledApps(List<ApplicationInfo> packages) {
        int count = 0;
        for (ApplicationInfo applicationInfo : packages) {
            if (settings.contains(applicationInfo.packageName))
                count++;
            else
                break;
        }
        mFirebaseAnalytics.setUserProperty("NumAppsTranslating", String.valueOf(count));
    }

    //TODO: Check this does not mess things up.
    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        setTargetFragment(null, -1);
    }

    static class ViewHolder {
        TextView textView;
        TextView textView2;
        ImageView imageView;
        CheckBox checkBox;
    }

    private class StableArrayAdapter extends ArrayAdapter<ApplicationInfo> {

        final PackageManager pm;
        final HashMap<ApplicationInfo, Integer> mIdMap = new HashMap<>();
        private final Context context2;
        private final List<ApplicationInfo> values;
        private final LayoutInflater inflater;

        public StableArrayAdapter(Context context, @SuppressWarnings({"SameParameterValue", "UnusedParameters"}) int textViewResourceId,
                                  List<ApplicationInfo> packages) {
            super(context, android.R.layout.simple_list_item_multiple_choice, packages);
            context2 = context;
            pm = context2.getPackageManager();
            inflater = (LayoutInflater) context2
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            values = new LinkedList<>(packages);
            for (int i = 0; i < values.size(); ++i) {
                mIdMap.put(values.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            ApplicationInfo item = getItem(position);
            //noinspection ConstantConditions
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = convertView.findViewById(R.id.firstLine);
                viewHolder.textView2 = convertView.findViewById(R.id.secondLine);
                viewHolder.imageView = convertView.findViewById(R.id.icon);
                viewHolder.checkBox = convertView.findViewById(R.id.checkBox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String packageName = values.get(position).packageName;
            String label = (String) pm.getApplicationLabel(values.get(position));
            Drawable icon = pm.getApplicationIcon(values.get(position));

            viewHolder.textView.setText(label);
            viewHolder.textView.setSelected(true);
            viewHolder.textView2.setText(packageName);
            viewHolder.textView2.setSelected(true);
            viewHolder.imageView.setImageDrawable(icon);

            viewHolder.checkBox.setTag(position);
            utils.debugLog("For package " + packageName + " ");
            if (settings.contains(packageName)) {
                viewHolder.checkBox.setChecked(true);
            } else {
                viewHolder.checkBox.setChecked(false);
            }

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox1 = (CheckBox) v;
                    int position = (Integer) checkBox1.getTag();
                    String packageName = values.get(position).packageName;
                    utils.debugLog("CheckBox clicked!" + packageName);

                    if (checkBox1.isChecked()) {
                        settings.edit().putBoolean(packageName, true).apply();

                        SharedPreferences localSettings = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
                        localSettings.edit().putBoolean("LocalEnabled", true).apply();
                    } else if (settings.contains(packageName)) {
                        settings.edit().remove(packageName).apply();

                        SharedPreferences localSettings = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
                        localSettings.edit().putBoolean("LocalEnabled", false).apply();
                    }
                }
            });
            return convertView;
        }

    }

    private class PrepareAdapter extends AsyncTask<Void, Void, StableArrayAdapter> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.loading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected StableArrayAdapter doInBackground(Void... params) {
            final PackageManager pm = context.getPackageManager();
            //get a list of installed apps.
            final List<ApplicationInfo> packages = getInstalledApplications(context);
            if (utils.isExpModuleActive(getContext())){
                List<String> packagNames = utils.getExpApps(getContext());
                utils.debugLog(packagNames + "");
                ListIterator<ApplicationInfo> iter = packages.listIterator();
                while(iter.hasNext()){
                    if(!packagNames.contains(iter.next().packageName)){
                        iter.remove();
                    }
                }
            }
            Collections.sort(packages, new Comparator<ApplicationInfo>() {
                public int compare(ApplicationInfo a, ApplicationInfo b) {
                    if (settings.contains(a.packageName) && !settings.contains(b.packageName))
                        return -1;
                    if (!settings.contains(a.packageName) && settings.contains(b.packageName))
                        return 1;
                    String labelA = pm.getApplicationLabel(a).toString().toLowerCase();
                    String labelB = pm.getApplicationLabel(b).toString().toLowerCase();
                    return labelA.compareTo(labelB);
                }
            });
            fireBaseEnabledApps(packages);
            if (getActivity() != null)
                return new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, packages);
            else
                return null;
        }

        protected void onPostExecute(StableArrayAdapter adapter) {
            if (adapter == null)
                return;
            listview.setAdapter(adapter);
            dialog.dismiss();
        }

        List<ApplicationInfo> getInstalledApplications(Context context) {
            final PackageManager pm = context.getPackageManager();
            try {
                return pm.getInstalledApplications(PackageManager.GET_META_DATA);
            } catch (Throwable ignored) {
                //we don't care why it didn't succeed. We'll do it using an alternative way instead
            }
            // use fallback:
            Process process;
            List<ApplicationInfo> result = new ArrayList<>();
            BufferedReader bufferedReader = null;
            try {
                process = Runtime.getRuntime().exec("pm list packages");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final String packageName = line.substring(line.indexOf(':') + 1);
                    final ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    result.add(applicationInfo);
                }
                process.waitFor();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null)
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return result;
        }
    }
}

