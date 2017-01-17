package akhil.alltrans;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import static android.content.Context.MODE_WORLD_READABLE;


public class GlobalPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(getString(R.string.globalPrefFile));
        preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences);

    }
}
