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
                Log.e("AllTrans", "AllTrans: Got error in reading cache " + Log.getStackTraceString(e));
                alltrans.cacheAccess.acquireUninterruptibly();
                alltrans.cache = new HashMap<>(10000);
                alltrans.cacheAccess.release();
            }
        }

        MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }

}