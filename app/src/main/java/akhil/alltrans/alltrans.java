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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.unhookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;


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
    Object AppsFilterThis = null;


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if ("com.android.providers.settings".equals(lpparam.packageName)) {
            XposedBridge.log("AllTrans: got settings provider package ");
            if (!settingsHooked) {
                hookSettings(lpparam);
                settingsHooked = true;
            }
        }


        // TODO: Comment this line later
        utils.debugLog("in package beginning : " + lpparam.packageName);


//        if (lpparam.packageName.equals("android")) {
////            String CLASS_PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
////            Class<?> packageManagerClass = findClass(CLASS_PACKAGE_MANAGER_SERVICE, lpparam.classLoader);
//            String APPS_FILTER = "com.android.server.pm.AppsFilter";
//            Class<?> packageManagerClass = findClass(APPS_FILTER, lpparam.classLoader);
//            de.robv.android.xposed.XposedBridge.log("Found class APPS_FILTER");
//            hookAllConstructors(packageManagerClass, new XC_MethodHook() {
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        XposedBridge.log("Inside Hooked constructors APPS_FILTER");
//                        AppsFilterThis = param.thisObject;
//                        Field mForceQueryable = findField(packageManagerClass, "mForceQueryable");
//                        XposedBridge.log("Found field mForceQueryable inside APPS_FILTER" + mForceQueryable.toString());
//                        ArraySet<Integer> mForceQueryableCast = (ArraySet<Integer>) mForceQueryable.get(AppsFilterThis);
//                        String listString = mForceQueryableCast.stream().map(Object::toString)
//                                .collect(Collectors.joining(", "));
//                        XposedBridge.log("Found field mForceQueryable inside APPS_FILTER" + listString);
//                    }
//
//                }
//            });
//            de.robv.android.xposed.XposedBridge.log("Hooked constructors packageManagerClass");
//        }
        try {
            baseRecordingCanvas = findClass("android.graphics.BaseRecordingCanvas", lpparam.classLoader);
        } catch (Throwable e){
            utils.debugLog("Cannot find baseRecordingCanvas");
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
        Class<?> clsSet = Class.forName("com.android.providers.settings.SettingsProvider", false, lpparam.classLoader);

        // Bundle call(String method, String arg, Bundle extras)
        Method mCall = clsSet.getMethod("call", String.class, String.class, Bundle.class);
        XposedBridge.log("AllTrans: Got method to hook settings ");
        XposedBridge.hookMethod(mCall, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("AllTrans: beforeHookedMethod call Settings: ");
                try {
                    String method = (String) param.args[0];
                    String arg = (String) param.args[1];
                    Bundle extras = (Bundle) param.args[2];

                    if ("xlua".equals(method)) {
                        XposedBridge.log("got method xlua ");
                        long ident = Binder.clearCallingIdentity();
                        try {
                            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
                            Context context = (Context) mGetContext.invoke(param.thisObject);

//                            Context newContext = createContextForUser(context, 10160);
//                            utils.debugLog(newContext.getPackageName());
                            String packageName = "com.towneers.www";

                            XposedBridge.log("AllTrans: Trying to allow blocking ");
                            XposedHelpers.callStaticMethod(Binder.class, "allowBlockingForCurrentThread");
//                            XposedBridge.log("AllTrans: Trying to hook BinderProxy ");
//                            Method allowBlocking = XposedHelpers.findMethodExact(Binder.class.getCanonicalName(), lpparam.classLoader, "allowBlockingForCurrentThread");
//                            // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/BinderProxy.java
//                            Class<?> clsSet1 = Class.forName("android.os.BinderProxy", false, lpparam.classLoader);
//
//                            Method mCall1 = Binder.getMethod("transact", int.class, Parcel.class, Parcel.class, int.class);
//                            XposedBridge.log("AllTrans: Got method to hook transact ");
//                            XC_MethodHook binderHook = new XC_MethodHook() {
//                                @Override
//                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                                    XposedBridge.log("AllTrans: beforeHookedMethod transact ");
//                                    int flags = (int) param.args[3];
//                                    XposedBridge.log("AllTrans: beforeHookedMethod original flags " + flags);
//                                    param.args[3] = (Integer) 1;
//                                    XposedBridge.log("AllTrans: beforeHookedMethod transact set args to 1");
//                                }
//                            };
//                            XposedBridge.hookMethod(mCall1, binderHook);
                            Cursor cursor = context.getContentResolver().query(Uri.parse("content://akhil.alltrans.sharedPrefProvider/" + packageName), null, null, null, null);
//                            unhookMethod(mCall1, binderHook);

                            XposedBridge.log("AllTrans: Trying to disallow blocking ");
                            utils.debugLog("Successfully got getContentResolver for package " + packageName);

                            Bundle result = new Bundle();
                            if (cursor == null || !cursor.moveToFirst()) {
                                XposedBridge.log("Cursor cannot move to first");
                            }
                            result.putString("value", cursor.getString(cursor.getColumnIndex("sharedPreferences")));
                            param.setResult(result);
                            XposedHelpers.callStaticMethod(Binder.class, "defaultBlockingForCurrentThread");
                            XposedBridge.log("AllTrans: setting call result");
//                            param.setResult(XProvider.call(context, arg, extras));
                        } catch (IllegalArgumentException ex) {
                            XposedBridge.log("Error: " + ex.getMessage());
                            param.setThrowable(ex);
                        } catch (Throwable ex) {
                            XposedBridge.log(Log.getStackTraceString(ex));
                            XposedBridge.log(ex);
                            param.setResult(null);
                        }
                        Binder.restoreCallingIdentity(ident);
                    }
                } catch (Throwable ex) {
                    XposedBridge.log(Log.getStackTraceString(ex));
                }
            }
        });

        // Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        Method mQuery = clsSet.getMethod("query", Uri.class, String[].class, String.class, String[].class, String.class);
        XposedBridge.hookMethod(mQuery, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                utils.debugLog("beforeHookedMethod mQuery Settings: ");
                try {
                    Uri uri = (Uri) param.args[0];
                    if (uri.toString().contains("alltransuri")){

                        XposedBridge.log("AllTrans: got projection xlua ");
                        long ident = Binder.clearCallingIdentity();
                        try {
                            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
                            Context context = (Context) mGetContext.invoke(param.thisObject);

                            XposedBridge.log("AllTrans: Trying to allow blocking ");
                            XposedHelpers.callStaticMethod(Binder.class, "allowBlockingForCurrentThread");
                            String packageName = "com.towneers.www";
                            Cursor cursor = context.getContentResolver().query(Uri.parse("content://akhil.alltrans.sharedPrefProvider/" + packageName), null, null, null, null);
                            param.setResult(cursor);
                            XposedHelpers.callStaticMethod(Binder.class, "defaultBlockingForCurrentThread");
                            XposedBridge.log("AllTrans: setting query result");
//                            param.setResult(XProvider.query(context, projection[0].split("\\.")[1], selection));
                        } catch (Throwable ex) {
                            XposedBridge.log(Log.getStackTraceString(ex));
//                            XposedBridge.log(ex);
                            param.setResult(null);
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                } catch (Throwable ex) {
                    XposedBridge.log(Log.getStackTraceString(ex));
//                    XposedBridge.log(ex);
                }
            }
        });
    }

    static Context createContextForUser(Context context, int userid) throws Throwable {

        // public UserHandle(int h)
        Class<?> clsUH = Class.forName("android.os.UserHandle");
        utils.debugLog("Got class clsUH");
        Constructor<?> cUH = clsUH.getDeclaredConstructor(int.class);
        utils.debugLog("Got Constructor");
        UserHandle uh = (UserHandle) cUH.newInstance(userid);
        utils.debugLog("Got UserHandle");

        // public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
        Method c = Context.class.getDeclaredMethod("createPackageContextAsUser", String.class, int.class, UserHandle.class);
        utils.debugLog("Got getDeclaredMethod");
        Context newContext = (Context) c.invoke(context, "android", 0, uh);
        utils.debugLog("Got newContext");
        return newContext;
    }
}


