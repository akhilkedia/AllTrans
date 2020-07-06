/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.text.MeasuredText;
import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

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
        // Get the bounds of the text, using our testTextSize.
        float desiredTextSize = originalSize;
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        while(bounds.width() > desiredWidth) {
            desiredTextSize -= 1;
            paint.setTextSize(desiredTextSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
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
        Method myMethod = (Method) methodHookParam.method;
        myMethod.setAccessible(true);
        Object[] myArgs = methodHookParam.args;

        if (myArgs.length != 0 && myArgs[0]!= null) {
            if (myArgs[0].getClass().equals(char[].class)) {
                myArgs[0] = translatedString.toString().toCharArray();
            } else if (myArgs[0].getClass().equals(AlteredCharSequence.class)) {
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
            } else if (myArgs[0].getClass().equals(MeasuredText.class)) {
                myArgs[0] = new MeasuredText.Builder(translatedString.toString().toCharArray()).build();
            } else {
                myArgs[0] = translatedString;
            }
        }

        Paint tempPaint = (Paint) myArgs[myArgs.length - 1];
        Canvas tempCanvas = (Canvas) methodHookParam.thisObject;
        if (myArgs[0]!= null) {
            myArgs[myArgs.length - 1] = copyPaint(tempPaint, tempCanvas, myArgs[0].toString());
        }
        if (myArgs[1].getClass().equals(int.class) || myArgs[1].getClass().equals(Integer.class) ) {
            myArgs[1] = 0;
            myArgs[2] = translatedString.length();
        }
        if (myArgs.length >= 5 && myArgs[3].getClass().equals(int.class) || myArgs[1].getClass().equals(Integer.class)) {
            myArgs[3] = 0;
            myArgs[4] = translatedString.length();
        }
//
        alltrans.hookAccess.acquireUninterruptibly();
        boolean unhookedSuccessfully = false;
        try {
            unhookMethod(methodHookParam.method, alltrans.drawTextHook);
            unhookedSuccessfully = true;
            try {
                utils.debugLog("In Thread " + Thread.currentThread().getId() + " Invoking original function " + methodHookParam.method.getName() + " and setting text to " + myArgs[0].toString());
                XposedBridge.invokeOriginalMethod(myMethod, methodHookParam.thisObject, myArgs);
            } catch (Throwable e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                utils.debugLog("Got error in invoking method as : " + sw);
                String classTypes = "";
                for (int i = 0; i < myArgs.length; i++) {
                    classTypes = classTypes + "Class:" + myArgs[i].getClass().getCanonicalName() + "Value:" + myArgs[i];
                }
                utils.debugLog("Params for above error are - " + classTypes);
            }
        } catch (Throwable e) {
            utils.debugLog("Cannot unhook drawtext for some reason" + Log.getStackTraceString(e));
        }
        if (unhookedSuccessfully) {
            try {
                hookMethod(methodHookParam.method, alltrans.drawTextHook);
            } catch (Throwable e) {
                utils.debugLog("Cannot re-hook drawtext for some reason" + Log.getStackTraceString(e));
            }
        }
        alltrans.hookAccess.release();
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) {
        try {
            if (methodHookParam.args[0] == null) {
                callOriginalMethod(null, methodHookParam);
                return null;
            }
            String stringArgs = "";
            if (methodHookParam.args[0].getClass() == char[].class) {
                stringArgs = new String((char[]) methodHookParam.args[0]);
            } else {
                stringArgs = methodHookParam.args[0].toString();
                if (methodHookParam.args[1].getClass().equals(int.class) || methodHookParam.args[1].getClass().equals(Integer.class)) {
                    if (methodHookParam.args[0].getClass() == char[].class) {
                        stringArgs = stringArgs.substring((int) methodHookParam.args[1], (int) methodHookParam.args[1] + (int) methodHookParam.args[2]);
                    } else {
                        stringArgs = stringArgs.substring((int) methodHookParam.args[1], (int) methodHookParam.args[2]);
                    }
                }
            }

            if (!SetTextHookHandler.isNotWhiteSpace(stringArgs)) {
                callOriginalMethod(stringArgs, methodHookParam);
                return null;
            }
            utils.debugLog("Canvas: Found string for canvas drawText : " + methodHookParam.args[0].toString());

            utils.debugLog("In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
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
                utils.debugLog("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                alltrans.cacheAccess.release();
                callOriginalMethod(translatedString, methodHookParam);
                return null;
            } else {
                alltrans.cacheAccess.release();
                callOriginalMethod(stringArgs, methodHookParam);
            }
            getTranslateToken.doAll();
            return null;
        } catch (Throwable e) {
            utils.debugLog("Some Exception in drawText replaceHook - " + Log.getStackTraceString(e));
            return null;
        }
    }
}
