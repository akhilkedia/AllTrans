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

import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static android.content.Context.MODE_WORLD_READABLE;


public class GlobalPreferencesFragment extends PreferenceFragmentCompat {

    private void fixNotRooted(){
        SwitchPreference rooted = (SwitchPreference) findPreference("Rooted");
        SwitchPreference drawText = (SwitchPreference) findPreference("DrawText");
        rooted.setVisible(false);
        if (utils.check_not_xposed(getActivity())) {
            utils.debugLog("This is Not Xposed, this is VirtualXposed or Taichi!");
            rooted.setChecked(false);
            drawText.setChecked(false);
            drawText.setVisible(false);
        } else {
            utils.debugLog("This is actual Xposed, not VirtualXposed or Taichi!");
            rooted.setChecked(true);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName("AllTransPref");
        preferenceManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences);

        fixNotRooted();

        SwitchPreference enableYandex = (SwitchPreference) findPreference("EnableYandex");
        String subscriptionKey1 = getPreferenceManager().getSharedPreferences().getString("SubscriptionKey","Enter");
        if (subscriptionKey1.startsWith("Enter") || subscriptionKey1.equals(getString(R.string.subKey_defaultValue)))
            enableYandex.setChecked(true);

        if (enableYandex.isChecked()) {
            ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
            translateFromLanguage.setEntries(R.array.languageNamesYandex);
            translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
            ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
            translateToLanguage.setEntries(R.array.languageNamesYandex);
            translateToLanguage.setEntryValues(R.array.languageCodesYandex);
            Preference subscriptionKey = findPreference("SubscriptionKey");
            subscriptionKey.setTitle(getString(R.string.subKey_yandex));
        }

        enableYandex.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
                    translateFromLanguage.setEntries(R.array.languageNamesYandex);
                    translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
                    ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
                    translateToLanguage.setEntries(R.array.languageNamesYandex);
                    translateToLanguage.setEntryValues(R.array.languageCodesYandex);
                    Preference subscriptionKey = findPreference("SubscriptionKey");
                    subscriptionKey.setTitle(getString(R.string.subKey_yandex));
                } else {
                    ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
                    translateFromLanguage.setEntries(R.array.languageNames);
                    translateFromLanguage.setEntryValues(R.array.languageCodes);
                    ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
                    translateToLanguage.setEntries(R.array.languageNames);
                    translateToLanguage.setEntryValues(R.array.languageCodes);
                    Preference subscriptionKey = findPreference("SubscriptionKey");
                    subscriptionKey.setTitle(getString(R.string.subKey_micro));
                }
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
