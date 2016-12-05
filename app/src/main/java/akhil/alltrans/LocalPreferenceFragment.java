package akhil.alltrans;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

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
    }


}
