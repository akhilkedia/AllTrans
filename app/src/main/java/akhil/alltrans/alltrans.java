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

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
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


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // TODO: Comment this line later
        utils.debugLog("in package beginning : " + lpparam.packageName);

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
}


