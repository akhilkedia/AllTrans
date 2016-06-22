package akhil.alltrans;

/**
 * Created by akhil on 24/2/16.
 */

import android.content.Context;
import android.os.Environment;
import android.widget.TextView;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static XC_MethodReplacement newhook = new HookHandler();
    public Context mCurrentActivity = null;

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
        XposedBridge.log("AllTrans: In Package SeoulBus" + lpparam.packageName);

        //Hook all Text String methods
        findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, newhook);
        //findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
        //findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,float.class, float.class, Paint.class, textMethodHook);


        File folder = new File(Environment.getExternalStorageDirectory() + "/AllTrans");

        String path = folder.getPath();

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                XposedBridge.log("AllTrans: Cannot Make Directory " + path);
            } else {
                XposedBridge.log("AllTrans: Directory Made " + path);
            }
        } else {
            XposedBridge.log("AllTrans: Directory already exists" + path);
        }
        File httpCacheDir = new File(folder, "requestcache");
        if (!httpCacheDir.exists()) {
            long httpCacheSize = 50 * 1024 * 1024; // 50 MiB
        }
    }
}
