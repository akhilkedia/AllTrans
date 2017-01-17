package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;


public class SetTextHookHandler extends XC_MethodReplacement implements OriginalCallable {


    public static boolean isNotWhiteSpace(String abc) {
        return !(abc == null || "".equals(abc)) && !abc.matches("^\\s*$");
    }

    public void callOriginalMethod(CharSequence translatedString, Object userData) {

        MethodHookParam methodHookParam = (MethodHookParam) userData;
        alltrans.hookAccess.acquireUninterruptibly();
        unhookMethod(methodHookParam.method, alltrans.setTextHook);
        Method myMethod = (Method) methodHookParam.method;
        myMethod.setAccessible(true);
        Object[] myArgs = methodHookParam.args;

        if (myMethod.getName().equals("setText")) {
            //if((myMethod.getName()=="setText")) {
            if (myArgs[0].getClass().equals(AlteredCharSequence.class)) {
                myArgs[0] = AlteredCharSequence.make(translatedString, null, 0, 0);
            } else if (myArgs[0].getClass().equals(CharBuffer.class)) {
                CharBuffer charBuffer = CharBuffer.allocate(translatedString.length() + 1);
                charBuffer.append(translatedString);
                myArgs[0] = charBuffer;
            } else if (myArgs[0].getClass().equals(SpannableString.class)) {
                myArgs[0] = new SpannableString(translatedString);
            } else if (myArgs[0].getClass().equals(SpannedString.class)) {
                myArgs[0] = new SpannedString(translatedString);
            } else if (myArgs[0].getClass().equals(String.class)) {
                myArgs[0] = translatedString.toString();
            } else if (myArgs[0].getClass().equals(StringBuffer.class)) {
                myArgs[0] = new StringBuffer(translatedString);
            } else if (myArgs[0].getClass().equals(StringBuilder.class)) {
                myArgs[0] = new StringBuilder(translatedString);
            } else {
                myArgs[0] = new SpannableStringBuilder(translatedString);
            }
        } else {
            myArgs[0] = TextUtils.stringOrSpannedString(translatedString);
        }

        try {
            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Invoking original function " + methodHookParam.method.getName() + " and setting text to " + myArgs[0].toString());
            myMethod.invoke(methodHookParam.thisObject, myArgs);
        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in invoking method as : " + Log.getStackTraceString(e));
        }
        hookMethod(methodHookParam.method, alltrans.setTextHook);
        alltrans.hookAccess.release();
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        if (methodHookParam.args[0] != null) {
            String stringArgs = methodHookParam.args[0].toString();

            if (isNotWhiteSpace(stringArgs)) {

                Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
                GetTranslate getTranslate = new GetTranslate();
                getTranslate.stringToBeTrans = stringArgs;
                getTranslate.originalCallable = this;
                getTranslate.userData = methodHookParam;
                getTranslate.canCallOriginal = true;

                GetTranslateToken getTranslateToken = new GetTranslateToken();
                getTranslateToken.getTranslate = getTranslate;

                callOriginalMethod(stringArgs, methodHookParam);

                alltrans.cacheAccess.acquireUninterruptibly();
                if (PreferenceList.Caching && alltrans.cache.containsKey(stringArgs)) {
                    String translatedString = alltrans.cache.get(stringArgs);
                    Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    final String finalString = translatedString;
                    final MethodHookParam finalMethodHookParam = methodHookParam;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                callOriginalMethod(finalString, finalMethodHookParam);
                            }
                    }, PreferenceList.Delay);

                    return null;
                } else {
                    alltrans.cacheAccess.release();
                }

                getTranslateToken.doAll();
            } else {
                callOriginalMethod(stringArgs, methodHookParam);
            }
        }
        return null;
    }

}
