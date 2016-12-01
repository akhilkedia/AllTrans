package akhil.alltrans;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by akhil on 1/12/16.
 */

public class GlobalPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootkey) {
        addPreferencesFromResource(R.xml.preferences);

    }
}
