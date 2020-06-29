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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.Collator;
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


public class LocalPreferenceFragment extends PreferenceFragmentCompat {
    public ApplicationInfo applicationInfo;
    private SharedPreferences settings;

    private void handleProviderChange(String translatorProviderSelected){
        if (translatorProviderSelected.equals("y")) {
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            translateFromLanguage.setEntries(R.array.languageNamesYandex);
            translateFromLanguage.setEntryValues(R.array.languageCodesYandex);
            translateToLanguage.setEntries(R.array.languageNamesYandex);
            translateToLanguage.setEntryValues(R.array.languageCodesYandex);
        } else if (translatorProviderSelected.equals("m")){
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            translateFromLanguage.setEntries(R.array.languageNames);
            translateFromLanguage.setEntryValues(R.array.languageCodes);
            translateToLanguage.setEntries(R.array.languageNames);
            translateToLanguage.setEntryValues(R.array.languageCodes);
        } else {
            ListPreference translateFromLanguage = findPreference("TranslateFromLanguage");
            ListPreference translateToLanguage = findPreference("TranslateToLanguage");
            assert translateFromLanguage != null;
            assert translateToLanguage != null;
            translateFromLanguage.setEntries(R.array.languageNamesGoogle);
            translateFromLanguage.setEntryValues(R.array.languageCodesGoogle);
            translateToLanguage.setEntries(R.array.languageNamesGoogle);
            translateToLanguage.setEntryValues(R.array.languageCodesGoogle);
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
        //noinspection ConstantConditions
        settings = this.getActivity().getSharedPreferences("AllTransPref", Context.MODE_PRIVATE);
        final PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(applicationInfo.packageName);

        utils.debugLog("Is it enabled for package " + applicationInfo.packageName + " answer -" + settings.contains(applicationInfo.packageName));
        if (settings.contains(applicationInfo.packageName)) {
            preferenceManager.getSharedPreferences().edit().putBoolean("LocalEnabled", true).apply();
        } else {
            preferenceManager.getSharedPreferences().edit().putBoolean("LocalEnabled", false).apply();
        }
        if (applicationInfo == null) {
            Context context = getContext();
            CharSequence text = getString(R.string.wut_why_null);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        addPreferencesFromResource(R.xml.perappprefs);

        SwitchPreference drawText = findPreference("DrawText");
        if (utils.check_not_xposed(getActivity())) {
            assert drawText != null;
            drawText.setChecked(false);
            drawText.setVisible(false);
        }

        String translatorProvider = settings.getString("TranslatorProvider", "g");
        assert translatorProvider != null;
        handleProviderChange(translatorProvider);
        sortListPreferenceByEntries("TranslateFromLanguage");
        sortListPreferenceByEntries("TranslateToLanguage");

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

        Preference clearCache = findPreference("ClearCache");
        assert clearCache != null;
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                preferenceManager.getSharedPreferences().edit().putString("ClearCacheTime", System.currentTimeMillis() + "").apply();

                Context context = preference.getContext();
                CharSequence text = getString(R.string.clear_cache_success);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                return false;
            }
        });

        Preference localEnabled = findPreference("LocalEnabled");
        assert localEnabled != null;
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
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        setTargetFragment(null, -1);
    }

}
