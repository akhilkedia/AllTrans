package akhil.alltrans;

/**
 * Created by akhil on 24/2/16.
 */

import android.app.Application;
import android.content.Context;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static XC_MethodReplacement newHook = new HookHandler();
    public static HashMap<String, String> cache = new HashMap<String, String>();
    public static Context context;

    public static boolean FindEnglish(String abc) {
        boolean isEnglish = true;
        char c;
        int val;
        for (int i = 0; i < abc.length(); i++) {
            val = abc.charAt(i);
            if (val > 256) {
                isEnglish = false;
                break;
            }
        }
        return isEnglish;
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        //Check Package is SeoulBus
        if (!lpparam.packageName.equals("com.astroframe.seoulbus")
                && !lpparam.packageName.equals("com.nhn.android.nmap")
                && !lpparam.packageName.equals("com.kakao.taxi")
                && !lpparam.packageName.equals("com.fineapp.yogiyo")
                && !lpparam.packageName.equals("com.thezumapp.and")
                && !lpparam.packageName.equals("com.dgfood.info")
                && !lpparam.packageName.equals("com.cgv.android.movieapp")
                && !lpparam.packageName.equals("com.Circusar.MrPizzaAR"))
            return;
        XposedBridge.log("AllTrans: In Package " + lpparam.packageName);

        appOnCreateHook appOnCreateHook = new appOnCreateHook();
        findAndHookMethod(Application.class, "onCreate", appOnCreateHook);

        //Hook all Text String methods
        findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, newHook);
        //findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
        //findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,float.class, float.class, Paint.class, textMethodHook);


    }
}

class appOnCreateHook extends XC_MethodHook {
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
