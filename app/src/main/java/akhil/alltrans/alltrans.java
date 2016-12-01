package akhil.alltrans;


import android.app.Application;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class alltrans implements IXposedHookLoadPackage {
    public static final Semaphore cacheAccess = new Semaphore(1, true);
    public static final Semaphore hookAccess = new Semaphore(1, true);
    public static XC_MethodReplacement newHook = new SetTextHookHandler();
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

        XSharedPreferences pref = new XSharedPreferences(alltrans.class.getPackage().getName(), "AllTransPref");
        pref.makeWorldReadable();
        pref.reload();
        if (!pref.getBoolean(lpparam.packageName, false))
            return;

        //Android System Webview - com.google.android.webview
        XposedBridge.log("AllTrans: In Package " + lpparam.packageName);

        appOnCreateHookHandler appOnCreateHookHandler = new appOnCreateHookHandler();
        findAndHookMethod(Application.class, "onCreate", appOnCreateHookHandler);

        //Hook all Text String methods
        findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, boolean.class, int.class, newHook);
        findAndHookMethod(TextView.class, "setHint", CharSequence.class, newHook);

        findAndHookMethod(WebViewClient.class, "onPageFinished", WebView.class, String.class, new WebViewHookHandler());
        findAndHookMethod(WebView.class, "loadUrl", String.class, Map.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                XposedBridge.log("AllTrans: we are in loadurl with headers!");
            }
        });
//        findAndHookMethod(WebView.class, "loadData", String.class, String.class, String.class, new XC_MethodHook() {
//            @Override protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                XposedBridge.log("we are in loadData!");
//            }});
//        findAndHookMethod(WebView.class, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
//            @Override protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                XposedBridge.log("we are in loadData!");
//            }});


        //findAndHookMethod(Canvas.class, "drawText", CharSequence.class, int.class, int.class, float.class, float.class, Paint.class, newHook);
        //findAndHookMethod(Canvas.class, "drawText", String.class, float.class, float.class, Paint.class, newHook);
        //findAndHookMethod(Canvas.class, "drawText", String.class, int.class, int.class, float.class, float.class, Paint.class, newHook);


    }
}


