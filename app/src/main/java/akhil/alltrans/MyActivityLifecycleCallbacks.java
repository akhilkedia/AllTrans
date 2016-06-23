package akhil.alltrans;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by akhil on 23/6/16.
 */
public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

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
        try {
            XposedBridge.log("AllTrans: trying to write cache");
            FileOutputStream fileOutputStream = alltrans.context.openFileOutput("AllTransCache", 0);
            ObjectOutputStream s = new ObjectOutputStream(fileOutputStream);
            alltrans.cacheAccess.acquireUninterruptibly();
            s.writeObject(alltrans.cache);
            alltrans.cacheAccess.release();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            XposedBridge.log("AllTrans: Got error in onActivityDestroyed: " + sw.toString());
        }

    }
}

