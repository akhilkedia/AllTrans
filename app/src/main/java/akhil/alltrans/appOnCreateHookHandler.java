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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

class appOnCreateHookHandler extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam methodHookParam) {
        XposedBridge.log("AllTrans: in OnCreate of Application");
        Application application = (Application) methodHookParam.thisObject;
        alltrans.context = application.getApplicationContext();

        if (PreferenceList.Caching) {
            try {
                FileInputStream fileInputStream = alltrans.context.openFileInput("AllTransCache");
                ObjectInputStream s = new ObjectInputStream(fileInputStream);
                alltrans.cacheAccess.acquireUninterruptibly();
                //noinspection unchecked
                alltrans.cache = (HashMap<String, String>) s.readObject();
                alltrans.cacheAccess.release();
                Log.i("AllTrans", "AllTrans: Successfully read old cache");
                s.close();
            } catch (Exception e) {
                Log.i("AllTrans", "AllTrans: Could not read cache ");
                alltrans.cacheAccess.acquireUninterruptibly();
                alltrans.cache = new HashMap<>(10000);
                alltrans.cacheAccess.release();
            }
        }

        MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }

}