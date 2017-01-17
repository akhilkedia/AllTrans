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
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static final Semaphore hookAccess = new Semaphore(1, true);
    public static final XC_MethodReplacement setTextHook = new SetTextHookHandler();
    private static final XC_MethodReplacement drawTextHook = new DrawTextHookHandler();
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

        appOnCreateHookHandler appOnCreateHookHandler = new appOnCreateHookHandler();
        findAndHookMethod(Application.class, "onCreate", appOnCreateHookHandler);

        //Hook all Text String methods
        if (PreferenceList.SetText)
            findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, setTextHook);
        if (PreferenceList.SetHint)
            findAndHookMethod(TextView.class, "setHint", CharSequence.class, setTextHook);

        if (PreferenceList.LoadURL) {
            findAndHookMethod(WebViewClient.class, "onPageFinished", WebView.class, String.class, new WebViewHookHandler());
            findAndHookMethod(WebView.class, "loadUrl", String.class, Map.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    XposedBridge.log("AllTrans: we are in loadurl with headers!");
                }
            });
        }
//        findAndHookMethod(WebView.class, "loadData", String.class, String.class, String.class, new XC_MethodHook() {
//            @Override protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                XposedBridge.log("we are in loadData!");
//            }});
//        findAndHookMethod(WebView.class, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
//            @Override protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                XposedBridge.log("we are in loadData!");
//            }});

        if (PreferenceList.DrawText) {
            findAndHookMethod(Canvas.class, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
        }


    }
}


