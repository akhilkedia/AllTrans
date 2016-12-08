package akhil.alltrans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

/**
 * Created by akhil on 8/12/16.
 */

public class DrawTextHookHandler extends XC_MethodReplacement implements OriginalCallable {

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint        the Paint to set the text size for
     * @param desiredWidth the desired width
     * @param text         the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float originalSize, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = originalSize;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        if (desiredWidth < bounds.width()) {
            // Calculate the desired size as a proportion of our testTextSize.
            float desiredTextSize = testTextSize * desiredWidth / bounds.width();

            // Set the paint for that size.
            paint.setTextSize(desiredTextSize);
        } else {
            paint.setTextSize(originalSize);
        }
    }

    public static Paint copyPaint(Paint paint, Canvas canvas, String text) {
        Paint myPaint = new Paint();
        myPaint.set(paint);
        myPaint.setTextSize(paint.getTextSize());
        myPaint.setColor(paint.getColor());
        setTextSizeForWidth(myPaint, paint.getTextSize(), canvas.getWidth(), text);
        return myPaint;
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
            Paint tempPaint = (Paint) myargs[myargs.length - 1];
            Canvas tempCanvas = (Canvas) methodHookParam.thisObject;
            myargs[myargs.length - 1] = copyPaint(tempPaint, tempCanvas, myargs[0].toString());
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

            if (SetTextHookHandler.isNotWhiteSpace(stringArgs)) {
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
