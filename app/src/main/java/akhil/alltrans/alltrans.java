package akhil.alltrans;

/**
 * Created by akhil on 24/2/16.
 */

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.widget.TextView;

import com.karumi.dexter.Dexter;

import java.io.File;
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
    public static HashMap<String, String> cache = null;

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


        File folder = new File(Environment.getExternalStorageDirectory() + "/AllTrans");
        File cacheFile = new File(folder, lpparam.packageName);
        if (cacheFile.exists()) {
            ObjectInputStream s = new ObjectInputStream(new FileInputStream(cacheFile));
            cacheAccess.acquireUninterruptibly();
            cache = (HashMap<String, String>) s.readObject();
            cacheAccess.release();
            s.close();
        } else {
            cacheAccess.acquireUninterruptibly();
            cache = new HashMap<String, String>(10000);
            cacheAccess.release();
        }

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
    }

    @Override
    protected void afterHookedMethod(MethodHookParam methodHookParam) {
        XposedBridge.log("AllTrans: in OnCreate of Application");
        Application application = (Application) methodHookParam.thisObject;
        Context context = application.getApplicationContext();
        Dexter.initialize(context);
        MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        application.registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }
}
