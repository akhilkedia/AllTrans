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
                Log.i("AllTrans", "AllTrans: trying to write cache");
                FileOutputStream fileOutputStream = alltrans.context.openFileOutput("AllTransCache", 0);
                ObjectOutputStream s = new ObjectOutputStream(fileOutputStream);
                alltrans.cacheAccess.acquireUninterruptibly();
                s.writeObject(alltrans.cache);
                alltrans.cacheAccess.release();

            } catch (Exception e) {
                Log.e("AllTrans", "AllTrans: Got error in onActivityDestroyed: " + Log.getStackTraceString(e));
            }
        }

    }
}

