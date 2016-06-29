package akhil.alltrans;

import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;


public class SetTextHookHandler extends XC_MethodReplacement {
    private static void callOriginalMethod(MethodHookParam methodHookParam, CharSequence translatedString) {
        alltrans.hookAccess.acquireUninterruptibly();
        unhookMethod(methodHookParam.method, alltrans.newHook);
        Method mymethod = (Method) methodHookParam.method;
        mymethod.setAccessible(true);
        Object[] myargs = methodHookParam.args;

        if (myargs[0].getClass().equals(AlteredCharSequence.class)) {
            myargs[0] = AlteredCharSequence.make(translatedString, null, 0, 0);
        } else if (myargs[0].getClass().equals(CharBuffer.class)) {
            CharBuffer charBuffer = CharBuffer.allocate(translatedString.length() + 1);
            charBuffer.append(translatedString);
            myargs[0] = charBuffer;
        } else if (myargs[0].getClass().equals(SpannableString.class)) {
            myargs[0] = new SpannableString(translatedString);
        } else if (myargs[0].getClass().equals(SpannedString.class)) {
            myargs[0] = new SpannedString(translatedString);
        } else if (myargs[0].getClass().equals(String.class)) {
            myargs[0] = translatedString.toString();
        } else if (myargs[0].getClass().equals(StringBuffer.class)) {
            myargs[0] = new StringBuffer(translatedString);
        } else if (myargs[0].getClass().equals(StringBuilder.class)) {
            myargs[0] = new StringBuilder(translatedString);
        } else {
            myargs[0] = new SpannableStringBuilder(translatedString);
            ;
        }

        try {
            mymethod.invoke(methodHookParam.thisObject, myargs);
        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in invoking method as : " + Log.getStackTraceString(e));
        }
        //tv.setText(translatedString);
        hookMethod(methodHookParam.method, alltrans.newHook);
        alltrans.hookAccess.release();
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        if (methodHookParam.args[0] != null) {
            String abc = methodHookParam.args[0].toString();
            TextView tv = (TextView) methodHookParam.thisObject;

            if (!alltrans.FindEnglish(abc)) {

                Log.i("AllTrans", "AllTrans: Recognized non-english string: " + abc);

                alltrans.cacheAccess.acquireUninterruptibly();
                if (alltrans.cache.containsKey(abc)) {
                    String translatedString = alltrans.cache.get(abc);
                    Log.i("AllTrans", "AllTrans: found string in cache: " + abc + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    callOriginalMethod(methodHookParam, translatedString);
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

                callOriginalMethod(methodHookParam, abc);

                handleNetworkInitial.doAll();
            } else {
                alltrans.hookAccess.acquireUninterruptibly();
                unhookMethod(methodHookParam.method, alltrans.newHook);

                tv.setText(abc);
                hookMethod(methodHookParam.method, alltrans.newHook);
                alltrans.hookAccess.release();
            }
        }
        return null;
    }
}