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
import android.app.Application;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static final Semaphore hookAccess = new Semaphore(1, true);
    public static final SetTextHookHandler setTextHook = new SetTextHookHandler();
    @SuppressLint("StaticFieldLeak")
    public static final WebViewHookHandler webViewHookHandler = new WebViewHookHandler();
    private static final DrawTextHookHandler drawTextHook = new DrawTextHookHandler();
    public static HashMap<String, String> cache = new HashMap<>();
    @SuppressLint("StaticFieldLeak")
    public static Context context;


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        XSharedPreferences globalPref = new XSharedPreferences(alltrans.class.getPackage().getName(), "AllTransPref");
        globalPref.makeWorldReadable();
        globalPref.reload();
        if (!globalPref.getBoolean("Enabled", false))
            return;
        if (!globalPref.getBoolean(lpparam.packageName, false))
            return;

        Log.i("AllTrans", "in package : " + lpparam.packageName);
        XSharedPreferences localPref = new XSharedPreferences(alltrans.class.getPackage().getName(), lpparam.packageName);
        localPref.makeWorldReadable();
        localPref.reload();
        PreferenceList.getPref(globalPref, localPref);


        //Android System WebView - com.google.android.webview
        XposedBridge.log("AllTrans: In Package " + lpparam.packageName);

        // Hook Application onCreate
        appOnCreateHookHandler appOnCreateHookHandler = new appOnCreateHookHandler();
        findAndHookMethod(Application.class, "onCreate", appOnCreateHookHandler);

        //Hook WebView Constructors
        WebViewOnCreateHookHandler webViewOnCreateHookHandler = new WebViewOnCreateHookHandler();
        findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, int.class, Map.class, boolean.class, webViewOnCreateHookHandler);

        //Hook all Text String methods
        if (PreferenceList.SetText)
            findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, setTextHook);
        if (PreferenceList.SetHint)
            findAndHookMethod(TextView.class, "setHint", CharSequence.class, setTextHook);

        if (PreferenceList.LoadURL) {
            findAndHookMethod(WebViewClient.class, "onPageFinished", WebView.class, String.class, webViewHookHandler);


            findAndHookMethod(WebView.class, "loadUrl", String.class, Map.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i("AllTrans", "AllTrans: we are in loadurl with headers!");
                }
            });
            findAndHookMethod(WebView.class, "loadUrl", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i("AllTrans", "AllTrans: we are in loadurl!");
                }
            });
            findAndHookMethod(WebView.class, "postUrl", String.class, (new byte[1]).getClass(), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i("AllTrans", "AllTrans: we are in posturl!");
                }
            });
            findAndHookMethod(WebView.class, "loadData", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i("AllTrans", "we are in loadData!");
                }
            });
            findAndHookMethod(WebView.class, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i("AllTrans", "we are in loadDataWithBaseURL! BaseURL - " + param.args[0] + "MimeType" + param.args[2] + " Data - " + param.args[1]);
                }
            });

        }


        if (PreferenceList.DrawText) {
            findAndHookMethod(Canvas.class, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
        }


    }
}


