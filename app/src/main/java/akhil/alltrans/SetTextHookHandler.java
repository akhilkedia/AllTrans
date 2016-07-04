package akhil.alltrans;

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


public class SetTextHookHandler extends XC_MethodReplacement {
    public static void callOriginalMethod(MethodHookParam methodHookParam, CharSequence translatedString) {
        alltrans.hookAccess.acquireUninterruptibly();
        unhookMethod(methodHookParam.method, alltrans.newHook);
        Method mymethod = (Method) methodHookParam.method;
        mymethod.setAccessible(true);
        Object[] myargs = methodHookParam.args;

        if ((mymethod.getName() == "setText") || (mymethod.getName() == "drawText")) {
            //if((mymethod.getName()=="setText")) {
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
            }
        } else if (mymethod.getName() == "setHint") {
            myargs[0] = TextUtils.stringOrSpannedString(translatedString);
        }
//        if(mymethod.getName()=="drawText"){
//            if(myargs[1].getClass().equals(int.class)){
//                int start = 0;
//                int end = translatedString.length();
//                myargs[1] = start;
//                myargs[2] = end;
//            }
//        }

        try {
            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Invoking original function " + methodHookParam.method.getName() + " and setting text to " + myargs[0].toString());
            mymethod.invoke(methodHookParam.thisObject, myargs);
        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in invoking method as : " + Log.getStackTraceString(e));
        }
        hookMethod(methodHookParam.method, alltrans.newHook);
        alltrans.hookAccess.release();
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
        if (methodHookParam.args[0] != null) {
            String abc = methodHookParam.args[0].toString();

            if (!alltrans.FindEnglish(abc)) {
                if (methodHookParam.method.getName().equals("drawText")) {
                    Log.i("AllTrans", "AllTrans: Canvas: Found string for canvas drawText : " + methodHookParam.args[0].toString());
                }

                Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + abc);

                HandleNetworkLater handleNetworkLater = new HandleNetworkLater();
                handleNetworkLater.stringToBeTrans = abc;
                handleNetworkLater.methodHookParam = methodHookParam;
                HandleNetworkInitial handleNetworkInitial = new HandleNetworkInitial();
                handleNetworkInitial.handleNetworkLater = handleNetworkLater;

                //if(!methodHookParam.method.getName().equals("drawText")) {
//                try {
//                    Thread.sleep(500);
//                    callOriginalMethod(methodHookParam, abc);
//                } catch (InterruptedException e) {
//                   e.printStackTrace();
//                }
                callOriginalMethod(methodHookParam, abc);
                // }

                alltrans.cacheAccess.acquireUninterruptibly();
                if (alltrans.cache.containsKey(abc)) {
                    String translatedString = alltrans.cache.get(abc);
                    Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " found string in cache: " + abc + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    callOriginalMethod(methodHookParam, translatedString);
                    return null;
                } else {
                    alltrans.cacheAccess.release();
                }

                handleNetworkInitial.doAll();
            } else {
                callOriginalMethod(methodHookParam, abc);
            }
        }
        return null;
    }
}