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
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class sharedPrefProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        utils.debugLog("Got URI as - " + uri.toString());
        String packageName = uri.toString().replaceFirst("content://akhil.alltrans.sharedPrefProvider/", "");
        String[] cols = {"sharedPreferences"};
        MatrixCursor cursor = new MatrixCursor(cols);

        //noinspection ConstantConditions
        SharedPreferences globalPref = this.getContext().getSharedPreferences("AllTransPref", Context.MODE_PRIVATE);
        MatrixCursor.RowBuilder builder = cursor.newRow();
        String globalPrefGson = new Gson().toJson(globalPref.getAll());
        builder.add(globalPrefGson);
        utils.debugLog("Got globalpref as - " + globalPrefGson + " for package " + packageName);
        utils.debugLog("Got boolean as - " + globalPref.getBoolean(packageName, false) + " for package " + packageName);
        if (globalPref.getBoolean(packageName, false)) {
            String localPrefGson = new Gson().toJson(this.getContext().getSharedPreferences(packageName, Context.MODE_PRIVATE).getAll());
            utils.debugLog("Got localpref as - " + localPrefGson + " for package " + packageName);
            builder = cursor.newRow();
            builder.add(localPrefGson);
        }
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
