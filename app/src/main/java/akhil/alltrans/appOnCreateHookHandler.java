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

import android.app.Application;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;

class appOnCreateHookHandler extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam methodHookParam) {
        utils.debugLog("AllTrans: in OnCreate of Application");
        Application application = (Application) methodHookParam.thisObject;

        try {
            if (alltrans.context != null) {
                utils.debugLog("AllTrans: returning because context already not null in appOnCreateHookHandler");
                return;
            }
            AttachBaseContextHookHandler.readPrefAndHook(application);
        } catch (Throwable e){
            utils.debugLog("Caught Exception in appOnCreateHookHandler " + Log.getStackTraceString(e));
        }

        MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }

}