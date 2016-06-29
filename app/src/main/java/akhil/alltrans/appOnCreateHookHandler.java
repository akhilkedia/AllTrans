package akhil.alltrans;

import android.app.Application;

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

        try {
            FileInputStream fileInputStream = alltrans.context.openFileInput("AllTransCache");
            ObjectInputStream s = new ObjectInputStream(fileInputStream);
            alltrans.cacheAccess.acquireUninterruptibly();
            alltrans.cache = (HashMap<String, String>) s.readObject();
            alltrans.cacheAccess.release();
            s.close();
        } catch (Exception e) {
            alltrans.cacheAccess.acquireUninterruptibly();
            alltrans.cache = new HashMap<String, String>(10000);
            alltrans.cacheAccess.release();
        }

        MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }

    @Override
    protected void afterHookedMethod(MethodHookParam methodHookParam) {

    }
}