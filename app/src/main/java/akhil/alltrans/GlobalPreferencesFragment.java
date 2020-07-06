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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import java.text.Collator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;


public class GlobalPreferencesFragment extends PreferenceFragmentCompat {

    @SuppressLint("ApplySharedPref")
    private void handleSubProviderChange() {
        ListPreference translatorProvider = findPreference("TranslatorProvider");
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        assert translatorProvider != null;
        boolean isNew = true;
        if (sharedPreferences.contains("EnableYandex")){
            String subscriptionKey1 = sharedPreferences.getString("SubscriptionKey", "Enter");
            if (subscriptionKey1 != null && !subscriptionKey1.startsWith("Enter") && !subscriptionKey1.equals(getString(R.string.subKey_defaultValue))) {
                isNew = false;
            }
        }

        if (!isNew){
            if (sharedPreferences.getBoolean("EnableYandex", false)){
                translatorProvider.setValueIndex(2);
            } else {
                translatorProvider.setValueIndex(1);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("EnableYandex");
            editor.commit();
        }
    }

    private void handleProviderChange(String translatorProviderSelected){
        if (translatorProviderSelected.equals("y")) {
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            Preference subscriptionKey = findPreference("SubscriptionKey");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            assert subscriptionKey != null;
            translateFromLanguage.setEntries(R.array.languageNamesYandex);
            translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
            translateToLanguage.setEntries(R.array.languageNamesYandex);
            translateToLanguage.setEntryValues(R.array.languageCodesYandex);
            subscriptionKey.setTitle(getString(R.string.subKey_yandex));
            subscriptionKey.setEnabled(true);
        } else if (translatorProviderSelected.equals("m")){
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            Preference subscriptionKey = findPreference("SubscriptionKey");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            assert subscriptionKey != null;
            translateFromLanguage.setEntries(R.array.languageNames);
            translateFromLanguage.setEntryValues(R.array.languageCodes);
            translateToLanguage.setEntries(R.array.languageNames);
            translateToLanguage.setEntryValues(R.array.languageCodes);
            subscriptionKey.setTitle(getString(R.string.subKey_micro));
            subscriptionKey.setEnabled(true);
        } else {
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            EditTextPreference subscriptionKey = findPreference("SubscriptionKey");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            assert subscriptionKey != null;
            translateFromLanguage.setEntries(R.array.languageNamesGoogle);
            translateFromLanguage.setEntryValues(R.array.languageCodesGoogle);
            translateToLanguage.setEntries(R.array.languageNamesGoogle);
            translateToLanguage.setEntryValues(R.array.languageCodesGoogle);
            subscriptionKey.setEnabled(false);
        }
    }

    private void sortListPreferenceByEntries(String preferenceKey) {
        ListPreference preference = (ListPreference) findPreference(preferenceKey);
        assert preference != null;
        Iterator<CharSequence> labels = Arrays.asList(preference.getEntries()).iterator();
        Iterator<CharSequence> keys = Arrays.asList(preference.getEntryValues()).iterator();
        Collator sortRules = Collator.getInstance(getResources().getConfiguration().locale);
        sortRules.setStrength(Collator.PRIMARY);
        TreeMap<CharSequence, CharSequence> sorter = new TreeMap<>(sortRules);
        int size = 0;
        while (labels.hasNext() && keys.hasNext()) {
            sorter.put(labels.next(), keys.next());
            size++;
        }
        CharSequence[] sortedLabels = new CharSequence[size];
        CharSequence[] sortedValues = new CharSequence[size];
        Iterator<Map.Entry<CharSequence, CharSequence>> entryIterator = sorter.entrySet().iterator();
        if (entryIterator.hasNext()) {
            for (int i = 0; i < size ; i++) {
                Map.Entry<CharSequence, CharSequence> entry = entryIterator.next();
                sortedLabels[i] = entry.getKey();
                sortedValues[i] = entry.getValue();
            }
        }
        preference.setEntries(sortedLabels);
        preference.setEntryValues(sortedValues);
    }

    private void downloadModel(String translateLanguageSelected, boolean isFromLanguage){
        ListPreference translatorProvider1 = findPreference("TranslatorProvider");
        assert translatorProvider1 != null;
        String translatorProviderSelected1 = translatorProvider1.getValue();
        if (!translatorProviderSelected1.equals("g")) {
            return;
        }
        utils.debugLog("Downloading Translation model for Language " + translateLanguageSelected + " isFromLanguage " + isFromLanguage);
        String sourceLanguage = "";
        String targetLanguage = "";
        if (isFromLanguage) {
            sourceLanguage = translateLanguageSelected;
            targetLanguage = TranslateLanguage.ENGLISH;
        } else {
            sourceLanguage = TranslateLanguage.ENGLISH;
            targetLanguage = translateLanguageSelected;
        }
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(targetLanguage)
                        .build();
        final Translator englishGermanTranslator = Translation.getClient(options);

        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        adb.setMessage(R.string.ask_download);
        adb.setPositiveButton(R.string.download_now, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, int which) {
                final ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setMessage(getString(R.string.downloading));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();

                englishGermanTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        utils.debugLog("Successfully Downloaded Translation model!");
                                        dialogInterface.dismiss();
                                        dialog.dismiss();
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(getContext(), R.string.download_sucess, duration);
                                        toast.show();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        utils.debugLog("Could not Downloaded Translation model!");
                                        utils.debugLog("Downloaded error - " + Log.getStackTraceString(e));
                                        dialogInterface.dismiss();
                                        dialog.dismiss();
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(getContext(), R.string.download_failure, duration);
                                        toast.show();
                                    }
                                });
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        adb.show();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName("AllTransPref");
        addPreferencesFromResource(R.xml.preferences);

        handleSubProviderChange();
        ListPreference translatorProvider = findPreference("TranslatorProvider");
        assert translatorProvider != null;
        String translatorProviderSelected = translatorProvider.getValue();
        handleProviderChange(translatorProviderSelected);
        sortListPreferenceByEntries("TranslateFromLanguage");
        sortListPreferenceByEntries("TranslateToLanguage");

        translatorProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String translatorProvider = (String) newValue;
                handleProviderChange(translatorProvider);
                sortListPreferenceByEntries("TranslateFromLanguage");
                sortListPreferenceByEntries("TranslateToLanguage");
                return true;
            }
        });

        ListPreference translateFromLanguage = (ListPreference) findPreference("TranslateFromLanguage");
        ListPreference translateToLanguage = (ListPreference) findPreference("TranslateToLanguage");
        assert translateFromLanguage != null;
        assert translateToLanguage != null;
        translateFromLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String translateLanguageSelected = (String) newValue;
                downloadModel(translateLanguageSelected, true);
                return true;
            }
        });

        translateToLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String translateLanguageSelected = (String) newValue;
                downloadModel(translateLanguageSelected, false);
                return true;
            }
        });
    }

    //TODO: Check this does not mess things up.
    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        setTargetFragment(null, -1);
    }
}
