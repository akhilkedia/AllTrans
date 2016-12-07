package akhil.alltrans;

import android.graphics.Paint;
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

    public static Paint copyPaint(Paint paint) {
        //Todo: Copy more values from paint
        Paint newPaint = new Paint();
        Paint myPaint = new Paint();
        myPaint.setTextSize(paint.getTextSize());
        myPaint.setColor(paint.getColor());

        return myPaint;
    }

    public static boolean isNotWhiteSpace(String abc) {
        if (abc == null || "".equals(abc))
            return false;
//        boolean isEnglish = true;
//        char c;
//        int val;
//        for (int i = 0; i < abc.length(); i++) {
//            val = abc.charAt(i);
//            if (val > 256) {
//                isEnglish = false;
//                break;
//            }
//        }
//        return isEnglish;
        return !abc.matches("^\\s*$");
    }

    public void callOriginalMethod(CharSequence translatedString, Object userData) {

        MethodHookParam methodHookParam = (MethodHookParam) userData;
        alltrans.hookAccess.acquireUninterruptibly();
        unhookMethod(methodHookParam.method, alltrans.newHook);
        Method mymethod = (Method) methodHookParam.method;
        mymethod.setAccessible(true);
        Object[] myargs = methodHookParam.args;


        if (mymethod.getName().equals("setText") || mymethod.getName().equals("drawText")) {
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
        } else if (mymethod.getName().equals("setHint")) {
            myargs[0] = TextUtils.stringOrSpannedString(translatedString);
        } else {
            myargs[0] = new SpannableStringBuilder(translatedString);
        }

        if (mymethod.getName().equals("drawText")) {
            myargs[myargs.length - 1] = copyPaint((Paint) myargs[myargs.length - 1]);
            if (myargs[1].getClass().equals(int.class)) {
                myargs[1] = 0;
                myargs[2] = translatedString.length();
            }
        }

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
            String stringArgs = methodHookParam.args[0].toString();

            if (isNotWhiteSpace(stringArgs)) {
                if (methodHookParam.method.getName().equals("drawText")) {
                    Log.i("AllTrans", "AllTrans: Canvas: Found string for canvas drawText : " + methodHookParam.args[0].toString());
                }

                Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
                GetTranslate getTranslate = new GetTranslate();
                getTranslate.stringToBeTrans = stringArgs;
                getTranslate.originalCallable = this;
                getTranslate.userData = methodHookParam;
                getTranslate.canCallOriginal = !methodHookParam.method.getName().equals("drawText");

                GetTranslateToken getTranslateToken = new GetTranslateToken();
                getTranslateToken.getTranslate = getTranslate;

                if (!methodHookParam.method.getName().equals("drawText")) {
                    callOriginalMethod(stringArgs, methodHookParam);
                }

                alltrans.cacheAccess.acquireUninterruptibly();
                if (PreferenceList.Caching && alltrans.cache.containsKey(stringArgs)) {
                    String translatedString = alltrans.cache.get(stringArgs);
                    Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    final String finalString = translatedString;
                    final MethodHookParam finalMethodHookParam = methodHookParam;

                    if (methodHookParam.method.getName().equals("drawText")) {
                        callOriginalMethod(finalString, finalMethodHookParam);
                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                callOriginalMethod(finalString, finalMethodHookParam);
                            }
                        }, PreferenceList.Delay);
                    }
                    return null;
                } else {
                    alltrans.cacheAccess.release();
                    if (methodHookParam.method.getName().equals("drawText")) {
                        callOriginalMethod(stringArgs, methodHookParam);
                    }
                }

                getTranslateToken.doAll();
            } else {
                callOriginalMethod(stringArgs, methodHookParam);
            }
        }
        return null;
    }
}
