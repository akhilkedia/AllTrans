package akhil.alltrans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;

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

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(originalSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        if (desiredWidth < bounds.width()) {
            // Calculate the desired size as a proportion of our testTextSize.
            float desiredTextSize = originalSize * desiredWidth / bounds.width();

            // Set the paint for that size.
            paint.setTextSize(desiredTextSize);
        } else {
            paint.setTextSize(originalSize);
        }
    }

    private static Paint copyPaint(Paint paint, Canvas canvas, String text) {
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
        unhookMethod(methodHookParam.method, alltrans.setTextHook);
        Method myMethod = (Method) methodHookParam.method;
        myMethod.setAccessible(true);
        Object[] myArgs = methodHookParam.args;

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

        Paint tempPaint = (Paint) myArgs[myArgs.length - 1];
            Canvas tempCanvas = (Canvas) methodHookParam.thisObject;
        myArgs[myArgs.length - 1] = copyPaint(tempPaint, tempCanvas, myArgs[0].toString());
        if (myArgs[1].getClass().equals(int.class)) {
            myArgs[1] = 0;
            myArgs[2] = translatedString.length();
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

            if (SetTextHookHandler.isNotWhiteSpace(stringArgs)) {
                Log.i("AllTrans", "AllTrans: Canvas: Found string for canvas drawText : " + methodHookParam.args[0].toString());

                Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
                GetTranslate getTranslate = new GetTranslate();
                getTranslate.stringToBeTrans = stringArgs;
                getTranslate.originalCallable = this;
                getTranslate.userData = methodHookParam;
                getTranslate.canCallOriginal = false;

                GetTranslateToken getTranslateToken = new GetTranslateToken();
                getTranslateToken.getTranslate = getTranslate;

                alltrans.cacheAccess.acquireUninterruptibly();
                if (PreferenceList.Caching && alltrans.cache.containsKey(stringArgs)) {
                    String translatedString = alltrans.cache.get(stringArgs);
                    Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    alltrans.cacheAccess.release();
                    callOriginalMethod(translatedString, methodHookParam);
                    return null;
                } else {
                    alltrans.cacheAccess.release();
                    callOriginalMethod(stringArgs, methodHookParam);
                }
                getTranslateToken.doAll();
            } else {
                callOriginalMethod(stringArgs, methodHookParam);
            }
        }
        return null;
    }

}
