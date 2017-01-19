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
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
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
        SwitchPreference enableYandex = (SwitchPreference) findPreference("EnableYandex");
        if (enableYandex.isChecked()) {
            ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
            translateFromLanguage.setEntries(R.array.languageNamesYandex);
            translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
            ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
            translateToLanguage.setEntries(R.array.languageNamesYandex);
            translateToLanguage.setEntryValues(R.array.languageCodesYandex);
            Preference subscriptionKey = findPreference("SubscriptionKey");
            subscriptionKey.setTitle("Enter Yandex Translate Subscription Key");
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
                    subscriptionKey.setTitle("Enter Yandex Translate Subscription Key");
                } else {
                    ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
                    translateFromLanguage.setEntries(R.array.languageNames);
                    translateFromLanguage.setEntryValues(R.array.languageCodes);
                    ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
                    translateToLanguage.setEntries(R.array.languageNames);
                    translateToLanguage.setEntryValues(R.array.languageCodes);
                    Preference subscriptionKey = findPreference("SubscriptionKey");
                    subscriptionKey.setTitle("Enter Microsoft Translate Subscription Key");
                }
                return true;
            }
        });
    }
}
