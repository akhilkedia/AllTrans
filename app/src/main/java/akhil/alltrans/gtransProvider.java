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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class gtransProvider extends ContentProvider {
    private Map<String, Translator> translatorClients;

    @Override
    public boolean onCreate() {
        utils.debugLog("Creating new Content Provider for gTrans!!");
        translatorClients = Collections.synchronizedMap(new HashMap<>());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        utils.debugLog("Got URI as - " + uri.toString());
        String fromLanguage = uri.getQueryParameter("from");
        String toLanguage = uri.getQueryParameter("to");
        String hashKey = fromLanguage + "##" + toLanguage;
        String tobeTrans = uri.getQueryParameter("text");
        Translator translator;
        if (translatorClients.containsKey(hashKey)){
            translator = translatorClients.get(hashKey);
        } else {
            assert fromLanguage != null;
            assert toLanguage != null;
            TranslatorOptions options =
                    new TranslatorOptions.Builder()
                            .setSourceLanguage(fromLanguage)
                            .setTargetLanguage(toLanguage)
                            .build();
            translator = Translation.getClient(options);
            translatorClients.put(hashKey, translator);
        }

        assert translator != null;
        assert tobeTrans != null;
        Task<String> task = translator.translate(tobeTrans);
        String translatedString = tobeTrans;
        try {
            translatedString = Tasks.await(task);
        } catch (Throwable e) {
            utils.debugLog(Log.getStackTraceString(e));
        }
//        translatorClient.close();
        String[] cols = {"translate"};
        MatrixCursor cursor = new MatrixCursor(cols);
        MatrixCursor.RowBuilder builder = cursor.newRow();
        builder.add(translatedString);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
