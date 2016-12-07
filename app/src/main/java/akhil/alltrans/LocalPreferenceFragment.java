package akhil.alltrans;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.DataOutputStream;

import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by akhil on 1/12/16.
 */

public class LocalPreferenceFragment extends PreferenceFragmentCompat {
    public ApplicationInfo applicationInfo;

    public LocalPreferenceFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootkey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(applicationInfo.packageName);
        preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.perappprefs);
        Preference pref = findPreference("clearCache");
        RecyclerView v = getListView();
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                    String path = "/data/data/" + applicationInfo.packageName + "/files/AllTransCache";

                    outputStream.writeBytes("am force-stop " + applicationInfo.packageName + "\n");
                    outputStream.flush();

                    outputStream.writeBytes("rm " + path + "\n");
                    outputStream.flush();

                    outputStream.writeBytes("am force-stop " + applicationInfo.packageName + "\n");
                    outputStream.flush();

                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    su.waitFor();
                } catch (Exception e) {
                    Context context = preference.getContext();
                    CharSequence text = "Some Error. Could not erase cache!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                Context context = preference.getContext();
                CharSequence text = "Translate Cache for this app has been erased!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return false;
            }
        });
    }


}
