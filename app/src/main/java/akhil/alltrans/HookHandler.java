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
            unhookMethod(methodHookParam.method, alltrans.newhook);
            if (!alltrans.FindEnglish(abc)) {
                XposedBridge.log("AllTrans: Recognized non-english string: " + abc);
                HandleNetworkLater handleNetworkLater = new HandleNetworkLater();
                handleNetworkLater.tv = tv;
                handleNetworkLater.stringToBeTrans = abc;
                handleNetworkLater.methodHookParam = methodHookParam;
                HandleNetworkInitial handleNetworkInitial = new HandleNetworkInitial();
                handleNetworkInitial.handleNetworkLater = handleNetworkLater;
                unhookMethod(handleNetworkLater.methodHookParam.method, alltrans.newhook);
                handleNetworkLater.tv.setText(handleNetworkLater.stringToBeTrans);
                hookMethod(handleNetworkLater.methodHookParam.method, alltrans.newhook);
                handleNetworkInitial.doAll();
            } else {
                tv.setText(abc);
            }
            hookMethod(methodHookParam.method, alltrans.newhook);
        }
        return null;
    }
}