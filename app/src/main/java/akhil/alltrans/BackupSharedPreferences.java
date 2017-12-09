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
import android.os.Environment;
import android.widget.Toast;

import java.io.DataOutputStream;


class BackupSharedPreferences {
    public static void backupSharedPreferences(Context context) {
        @SuppressLint("SdCardPath")
        String sharedPath = "/data/data/akhil.alltrans/shared_prefs/";
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!isExternalStorageWritable()) {
            return;
        }

        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            utils.debugLog("cp -R" + " " + sharedPath + " " + externalPath + "/AllTransBackup/" + "\n");
            outputStream.writeBytes("cp -R" + " " + sharedPath + " " + externalPath + "/AllTransBackup/" + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
            su.waitFor();

            CharSequence text = context.getString(R.string.backup_pref_success);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } catch (Exception e) {
            CharSequence text = context.getString(R.string.backup_pref_error);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
