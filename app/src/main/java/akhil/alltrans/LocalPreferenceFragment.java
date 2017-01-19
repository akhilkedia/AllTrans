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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import java.io.DataOutputStream;

import static android.content.Context.MODE_WORLD_READABLE;

public class LocalPreferenceFragment extends PreferenceFragmentCompat {
    public ApplicationInfo applicationInfo;

    public LocalPreferenceFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        SharedPreferences settings = this.getActivity().getSharedPreferences(getString(R.string.globalPrefFile), MODE_WORLD_READABLE);
        Boolean enabledYandex = settings.getBoolean("EnableYandex", false);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(applicationInfo.packageName);
        preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.perappprefs);

        if (enabledYandex) {
            ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
            translateFromLanguage.setEntries(R.array.languageNamesYandex);
            translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
            ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
            translateToLanguage.setEntries(R.array.languageNamesYandex);
            translateToLanguage.setEntryValues(R.array.languageCodesYandex);
        } else {
            ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
            translateFromLanguage.setEntries(R.array.languageNames);
            translateFromLanguage.setEntryValues(R.array.languageCodes);
            ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
            translateToLanguage.setEntries(R.array.languageNames);
            translateToLanguage.setEntryValues(R.array.languageCodes);
        }

        Preference pref = findPreference("clearCache");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                    @SuppressLint("SdCardPath") String path = "/data/data/" + applicationInfo.packageName + "/files/AllTransCache";

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
