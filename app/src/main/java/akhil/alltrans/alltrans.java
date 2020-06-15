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
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static final Semaphore hookAccess = new Semaphore(1, true);
    @SuppressLint("StaticFieldLeak")
    private static final DrawTextHookHandler drawTextHook = new DrawTextHookHandler();
    @SuppressLint("StaticFieldLeak")
    public static VirtWebViewOnLoad virtWebViewOnLoad = new VirtWebViewOnLoad();
    public static HashMap<String, String> cache = new HashMap<>();
    @SuppressLint("StaticFieldLeak")
    public static Context context;


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // TODO: Comment this line later
        utils.debugLog("in package beginning : " + lpparam.packageName);
        XSharedPreferences globalPref = new XSharedPreferences(alltrans.class.getPackage().getName(), "AllTransPref");
        globalPref.reload();
        if (!globalPref.getBoolean("Enabled", false))
            return;
        if (!globalPref.getBoolean(lpparam.packageName, false))
            return;

        utils.Debug = globalPref.getBoolean("Debug", false);
        utils.Rooted = globalPref.getBoolean("Rooted", false);
        utils.debugLog("In package : " + lpparam.packageName);
        if (!utils.Rooted) {
            utils.debugLog("We are in Taichi/VirtualXposed for package : " + lpparam.packageName);
        }

        XSharedPreferences localPref = new XSharedPreferences(alltrans.class.getPackage().getName(), lpparam.packageName);
        localPref.reload();
        PreferenceList.getPref(globalPref, localPref, lpparam.packageName);
        utils.debugLog("Alltrans is Enabled for Package " + lpparam.packageName);


        // Hook Application onCreate
        appOnCreateHookHandler appOnCreateHookHandler = new appOnCreateHookHandler();
        findAndHookMethod(Application.class, "onCreate", appOnCreateHookHandler);

        AttachBaseContextHookHandler attachBaseContextHookHandler = new AttachBaseContextHookHandler();
        findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, attachBaseContextHookHandler);

        //Hook all Text String methods
        SetTextHookHandler setTextHook = new SetTextHookHandler();
        if (PreferenceList.SetText)
            findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, setTextHook);
        if (PreferenceList.SetHint)
            findAndHookMethod(TextView.class, "setHint", CharSequence.class, setTextHook);

        if (PreferenceList.LoadURL) {
            // Hook WebView Constructor to inject JS object
            findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, int.class, Map.class, boolean.class, new WebViewOnCreateHookHandler());
            if (!utils.Rooted) {
                findAndHookMethod(WebView.class, "setWebViewClient", WebViewClient.class, new WebViewSetClientHookHandler());
            } else {
                findAndHookMethod(WebViewClient.class, "onPageFinished", WebView.class, String.class, new WebViewOnLoadHookHandler());
            }
        }


        if (PreferenceList.DrawText) {
            findAndHookMethod(Canvas.class, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, float.class, float.class, Paint.class, drawTextHook);
            findAndHookMethod(Canvas.class, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, drawTextHook);
        }


    }
}


