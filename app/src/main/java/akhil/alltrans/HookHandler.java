package akhil.alltrans;

import android.widget.TextView;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;

/**
 * Created by akhil on 13/6/16.
 */
public class HookHandler extends XC_MethodReplacement {
    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        //XposedBridge.log("AllTrans: Called Replacement");
        if (methodHookParam.args[0] != null) {
            String abc = methodHookParam.args[0].toString();
            TextView tv = (TextView) methodHookParam.thisObject;
            //XposedBridge.log("AllTrans: the string recieved is: " + abc);
            if (!alltrans.FindEnglish(abc)) {
                XposedBridge.log("AllTrans: Recognized non-english string: " + abc);

                alltrans.cacheAccess.acquireUninterruptibly();
                if (alltrans.cache.containsKey(abc)) {
                    String translatedString = alltrans.cache.get(abc);
                    XposedBridge.log("AllTrans: found string in cache: " + abc + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    unhookMethod(methodHookParam.method, alltrans.newHook);
                    tv.setText(translatedString);
                    hookMethod(methodHookParam.method, alltrans.newHook);
                    return null;
                } else {
                    alltrans.cacheAccess.release();
                }

                HandleNetworkLater handleNetworkLater = new HandleNetworkLater();
                handleNetworkLater.tv = tv;
                handleNetworkLater.stringToBeTrans = abc;
                handleNetworkLater.methodHookParam = methodHookParam;
                HandleNetworkInitial handleNetworkInitial = new HandleNetworkInitial();
                handleNetworkInitial.handleNetworkLater = handleNetworkLater;

                unhookMethod(handleNetworkLater.methodHookParam.method, alltrans.newHook);
                handleNetworkLater.tv.setText(handleNetworkLater.stringToBeTrans);
                hookMethod(handleNetworkLater.methodHookParam.method, alltrans.newHook);

                handleNetworkInitial.doAll();
            } else {
                unhookMethod(methodHookParam.method, alltrans.newHook);
                tv.setText(abc);
                hookMethod(methodHookParam.method, alltrans.newHook);
            }
        }
        return null;
    }
}