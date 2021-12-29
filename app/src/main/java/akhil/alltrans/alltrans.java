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
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findClass;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static final Semaphore hookAccess = new Semaphore(1, true);
    @SuppressLint("StaticFieldLeak")
    public static final DrawTextHookHandler drawTextHook = new DrawTextHookHandler();
    @SuppressLint("StaticFieldLeak")
    public static final NotificationHookHandler notifyHook = new NotificationHookHandler();
    @SuppressLint("StaticFieldLeak")
    public static final VirtWebViewOnLoad virtWebViewOnLoad = new VirtWebViewOnLoad();
    public static HashMap<String, String> cache = new HashMap<>();
    @SuppressLint("StaticFieldLeak")
//    TODO: Maybe change to using WeakReference?
    public static Context context = null;
    public static Class baseRecordingCanvas = null;
    public static boolean settingsHooked = false;


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if ("com.android.providers.settings".equals(lpparam.packageName)) {
            XposedBridge.log("AllTrans: got settings provider package ");
            if (!settingsHooked) {
                hookSettings(lpparam);
                settingsHooked = true;
            }
        }

        // TODO: Comment this line later
        XposedBridge.log("in package beginning : " + lpparam.packageName);

        try {
            baseRecordingCanvas = findClass("android.graphics.BaseRecordingCanvas", lpparam.classLoader);
        } catch (Throwable e){
            XposedBridge.log("Cannot find baseRecordingCanvas");
        }

//        Hook Application onCreate
        appOnCreateHookHandler appOnCreateHookHandler = new appOnCreateHookHandler();
        utils.tryHookMethod(Application.class, "onCreate", appOnCreateHookHandler);

//        Possibly change to android.app.Instrumentation.newActivity()
        AttachBaseContextHookHandler attachBaseContextHookHandler = new AttachBaseContextHookHandler();
        utils.tryHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, attachBaseContextHookHandler);

    }

    private void hookSettings(final LoadPackageParam lpparam) throws Throwable {

        XposedBridge.log("AllTrans: Trying to hook settings ");
        // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsProvider/src/com/android/providers/settings/SettingsProvider.java
        @SuppressLint("PrivateApi") Class<?> clsSet = Class.forName("com.android.providers.settings.SettingsProvider", false, lpparam.classLoader);

        XposedBridge.log("AllTrans: Got method to hook settings ");
        // Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        Method mQuery = clsSet.getMethod("query", Uri.class, String[].class, String.class, String[].class, String.class);
        XposedBridge.hookMethod(mQuery, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("beforeHookedMethod mQuery Settings: ");
                try {
                    Uri uri = (Uri) param.args[0];
                    if (uri.toString().contains("alltransProxyProviderURI")){

                        XposedBridge.log("AllTrans: got projection xlua ");
                        long ident = Binder.clearCallingIdentity();
                        try {
                            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
                            Context context = (Context) mGetContext.invoke(param.thisObject);

                            XposedBridge.log("AllTrans: Trying to allow blocking ");
                            XposedHelpers.callStaticMethod(Binder.class, "allowBlockingForCurrentThread");

                            XposedBridge.log("AllTrans: Old URI " + uri.toString());
                            String new_uri_string = uri.toString().replace("content://settings/system/alltransProxyProviderURI/", "content://akhil.alltrans.");
                            Uri new_uri = Uri.parse(new_uri_string);
                            XposedBridge.log("AllTrans: New URI " + new_uri.toString());
                            
                            Cursor cursor = context.getContentResolver().query(new_uri, null, null, null, null);
                            param.setResult(cursor);

                            XposedHelpers.callStaticMethod(Binder.class, "defaultBlockingForCurrentThread");
                            XposedBridge.log("AllTrans: setting query result");
                        } catch (Throwable ex) {
                            XposedBridge.log(Log.getStackTraceString(ex));
                            param.setResult(null);
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                } catch (Throwable ex) {
                    XposedBridge.log(Log.getStackTraceString(ex));
                }
            }
        });
    }
}


