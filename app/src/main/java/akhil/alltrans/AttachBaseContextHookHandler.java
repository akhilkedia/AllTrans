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

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.text.MeasuredText;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

class AttachBaseContextHookHandler extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam methodHookParam) {
        try {
            Context context = (Context) methodHookParam.args[0];
            String packageName = context.getPackageName();
            utils.debugLog("AllTrans: in attachBaseContext of ContextWrapper for package " + packageName);
            if (context.getApplicationContext() == null) {
                utils.debugLog("AllTrans: returning because null context for package " + packageName);
                return;
            }
//        TODO: Verify using this context is fine.
            if (alltrans.context != null) {
                utils.debugLog("AllTrans: returning because context already not null for package " + packageName);
                return;
            }
            alltrans.context = ((Context) methodHookParam.args[0]).getApplicationContext();
            utils.debugLog("Successfully got context for package " + packageName);

            utils.debugLog(alltrans.context.getPackageName());
            Cursor cursor = alltrans.context.getContentResolver().query(Uri.parse("content://akhil.alltrans.sharedPrefProvider/" + packageName), null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }
            String globalPref = cursor.getString(cursor.getColumnIndex("sharedPreferences"));
            String localPref;
            if (!cursor.moveToNext()) {
                localPref = globalPref;
            } else {
                localPref = cursor.getString(cursor.getColumnIndex("sharedPreferences"));
            }
            cursor.close();
            utils.debugLog(globalPref);
            utils.debugLog(localPref);
            PreferenceList.getPref(globalPref, localPref, packageName);

            if (!PreferenceList.Enabled)
                return;
            if (!PreferenceList.LocalEnabled)
                return;

            utils.Debug = PreferenceList.Debug;

            utils.debugLog("Alltrans is Enabled for Package " + packageName);

//        Delete Cache if needed
            if (PreferenceList.Caching) {
                if (alltrans.cache.isEmpty()) {
                    clearCacheIfNeeded(alltrans.context, PreferenceList.CachingTime);
                    alltrans.cacheAccess.acquireUninterruptibly();
                    try {
                        FileInputStream fileInputStream = alltrans.context.openFileInput("AllTransCache");
                        ObjectInputStream s = new ObjectInputStream(fileInputStream);
                        //noinspection
                        if (alltrans.cache.isEmpty()) {
                            alltrans.cache = (HashMap<String, String>) s.readObject();
                        }
                        utils.debugLog("Successfully read old cache");
                        s.close();
                    } catch (Throwable e) {
                        utils.debugLog("Could not read cache ");
                        utils.debugLog(e.toString());
                        alltrans.cache = new HashMap<>(10000);
                        alltrans.cache.put("ThisIsAPlaceHolderStringYouWillNeverEncounter", "ThisIsAPlaceHolderStringYouWillNeverEncounter");
                    }
                    alltrans.cacheAccess.release();
                }
            }


            //Hook all Text String methods
            SetTextHookHandler setTextHook = new SetTextHookHandler();
            if (PreferenceList.SetText) {
                utils.tryHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, setTextHook);
                hookAllMethods(NotificationManager.class, "notify", alltrans.notifyHook);
            }
            if (PreferenceList.SetHint)
                utils.tryHookMethod(TextView.class, "setHint", CharSequence.class, setTextHook);

            if (PreferenceList.LoadURL) {
                // Hook WebView Constructor to inject JS object
                findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, int.class, Map.class, boolean.class, new WebViewOnCreateHookHandler());
                utils.tryHookMethod(WebView.class, "setWebViewClient", WebViewClient.class, new WebViewSetClientHookHandler());
                utils.tryHookMethod(WebViewClient.class, "onPageFinished", WebView.class, String.class, new WebViewOnLoadHookHandler());
            }

            if (PreferenceList.DrawText) {
                utils.tryHookMethod(Canvas.class, "drawText", char[].class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawTextOnPath", char[].class, int.class, int.class, Path.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawTextRun", char[].class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawText", String.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawTextOnPath", String.class, Path.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawTextRun", CharSequence.class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(Canvas.class, "drawTextRun", MeasuredText.class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);

                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawText", char[].class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawTextOnPath", char[].class, int.class, int.class, Path.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawTextRun", char[].class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawText", String.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawTextOnPath", String.class, Path.class, float.class, float.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawTextRun", CharSequence.class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);
                utils.tryHookMethod(alltrans.baseRecordingCanvas, "drawTextRun", MeasuredText.class, int.class, int.class, int.class, int.class, float.class, float.class, boolean.class, Paint.class, alltrans.drawTextHook);
            }

            // hookAllConstructors(RemoteViews.class, alltrans.notifyHook);
        } catch (Throwable e){
            utils.debugLog("Caught Exception in attachBaseContext " + Log.getStackTraceString(e));
        }

    }

    protected void clearCacheIfNeeded(Context context, long cachingTime) {
        // If cache never cleared, exit
        if (cachingTime == 0)
            return;

        alltrans.cacheAccess.acquireUninterruptibly();

        // Attempt to read last time successfully cleared cache if any
        long lastClearTime = 0;
        try {
            FileInputStream fileInputStream = context.openFileInput("AllTransCacheClear");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            lastClearTime = (long) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Throwable ignored) {
        }

        // If we cache was cleared after we deleted cache last time, delete cache again
        if (lastClearTime < cachingTime) {
            try {
                // Set the time cache was cleared
                lastClearTime = System.currentTimeMillis();
                FileOutputStream fileOutputStream = context.openFileOutput("AllTransCacheClear", 0);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(lastClearTime);
                objectOutputStream.close();

                // Actually clear cache
                context.deleteFile("AllTransCache");
            } catch (Throwable ignored) {
            }
        }

        alltrans.cacheAccess.release();
    }

}