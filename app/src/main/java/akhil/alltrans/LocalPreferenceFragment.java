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
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import java.io.DataOutputStream;

import static android.content.Context.MODE_WORLD_READABLE;

public class LocalPreferenceFragment extends PreferenceFragmentCompat {
    public ApplicationInfo applicationInfo;
    @SuppressLint("WorldReadableFiles")
    private SharedPreferences settings;

    public LocalPreferenceFragment() {

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        settings = this.getActivity().getSharedPreferences("AllTransPref", MODE_WORLD_READABLE);
        final PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(applicationInfo.packageName);
        preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);

        utils.debugLog("Is it enabled for package " + applicationInfo.packageName + " answer -" + settings.contains(applicationInfo.packageName));
        if (settings.contains(applicationInfo.packageName)) {
            preferenceManager.getSharedPreferences().edit().putBoolean("LocalEnabled", true).apply();
        } else {
            preferenceManager.getSharedPreferences().edit().putBoolean("LocalEnabled", false).apply();
        }
        Boolean enabledYandex = settings.getBoolean("EnableYandex", false);
        if (applicationInfo == null) {
            Context context = getContext();
            CharSequence text = getString(R.string.wut_why_null);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        addPreferencesFromResource(R.xml.perappprefs);

        SwitchPreference drawText = (SwitchPreference) findPreference("DrawText");
        if (utils.check_not_xposed(getActivity())) {
            drawText.setChecked(false);
            drawText.setVisible(false);
        }

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

        Preference clearCache = findPreference("ClearCache");
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                preferenceManager.getSharedPreferences().edit().putLong("ClearCacheTime", System.currentTimeMillis()).apply();

                Context context = preference.getContext();
                CharSequence text = getString(R.string.clear_cache_success);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return false;
            }
        });

        Preference localEnabled = findPreference("LocalEnabled");
        localEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // do whatever you want with new value
                boolean localEnabledBool = (boolean) newValue;
                if (localEnabledBool) {
                    settings.edit().putBoolean(applicationInfo.packageName, true).apply();
                } else {
                    settings.edit().remove(applicationInfo.packageName).apply();
                }
                // true to update the state of the Preference with the new value
                // in case you want to disallow the change return false
                return true;
            }
        });
    }

    //TODO: Check this does not mess things up.
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        setTargetFragment(null, -1);
    }

}
