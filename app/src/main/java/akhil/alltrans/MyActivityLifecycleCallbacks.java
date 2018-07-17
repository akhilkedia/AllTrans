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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (PreferenceList.Caching) {
            try {
                utils.debugLog("trying to write cache");
                FileOutputStream fileOutputStream = alltrans.context.openFileOutput("AllTransCache", 0);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                alltrans.cacheAccess.acquireUninterruptibly();
                objectOutputStream.writeObject(alltrans.cache);
                objectOutputStream.close();
                alltrans.cacheAccess.release();

            } catch (Exception e) {
                utils.debugLog("Got error in onActivityDestroyed: " + Log.getStackTraceString(e));
            }
        }

    }
}

