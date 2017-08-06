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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class GetTranslate implements Callback {
    public String stringToBeTrans;
    public OriginalCallable originalCallable;
    public boolean canCallOriginal;
    public Object userData;
    private String translatedString;

    @Override
    public void onResponse(Call call, Response response) {
        try {
            String result = response.body().string();
            response.body().close();

            utils.debugLog("In Thread " + Thread.currentThread().getId() + " In GetTranslate, setting: " + stringToBeTrans + "got response as " + result);
            try {
                if (PreferenceList.EnableYandex)
                    translatedString = result.substring(result.indexOf("<text>") + 6, result.lastIndexOf("</text>"));
                else
                    translatedString = result.substring(result.indexOf("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">") + 68, result.lastIndexOf("</string"));
            } catch (Exception e) {
                Log.e("AllTrans", "AllTrans: Got error in getting string from translation as : " + Log.getStackTraceString(e));
                translatedString = stringToBeTrans;
            }
            translatedString = utils.XMLUnescape(translatedString);

            if (translatedString == null) {
                translatedString = "";
            }

            if (PreferenceList.Caching) {
                alltrans.cacheAccess.acquireUninterruptibly();
                alltrans.cache.put(stringToBeTrans, translatedString);
                alltrans.cache.put(translatedString, translatedString);
                alltrans.cacheAccess.release();
            }

            utils.debugLog("In Thread " + Thread.currentThread().getId() + " In GetTranslate, setting: " + stringToBeTrans + " to :" + translatedString);

        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
            translatedString = stringToBeTrans;
        } finally {
//            try {
//                if(PreferenceList.Delay>0)
//                    Thread.sleep(PreferenceList.Delay);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            utils.debugLog("In Thread " + Thread.currentThread().getId() + " In GetTranslate calling callOriginalMethod with argument - " + translatedString);

            if (canCallOriginal) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        originalCallable.callOriginalMethod(translatedString, userData);
                    }
                });
            }
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
        translatedString = stringToBeTrans;

        if (canCallOriginal) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    originalCallable.callOriginalMethod(translatedString, userData);
                }
            }, PreferenceList.Delay);
        }
    }
}