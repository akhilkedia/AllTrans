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

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.AlteredCharSequence;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.nio.CharBuffer;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callMethod;


public class SetTextHookHandler extends XC_MethodReplacement implements OriginalCallable {


    public static boolean isNotWhiteSpace(String abc) {
        return !(abc == null || "".equals(abc)) && !abc.matches("^\\s*$");
    }

    public void callOriginalMethod(CharSequence translatedString, Object userData) {

        MethodHookParam methodHookParam = (MethodHookParam) userData;
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
            utils.debugLog("In Thread " + Thread.currentThread().getId() + " Invoking original function " + methodHookParam.method.getName() + " and setting text to " + myArgs[0].toString());
            XposedBridge.invokeOriginalMethod(myMethod, methodHookParam.thisObject, myArgs);

            utils.debugLog("In Thread " + Thread.currentThread().getId() + " Finished invoking original function " + methodHookParam.method.getName() + " and setting text to " + myArgs[0].toString());

        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in invoking method as : " + Log.getStackTraceString(e));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

        if (methodHookParam.args[0] == null) {
            return null;
        }

        String stringArgs = methodHookParam.args[0].toString();
        try {
            Boolean editable = (Boolean) callMethod(methodHookParam.thisObject, "getDefaultEditable");
            utils.debugLog("Is this Object is editable - " + editable);
            if (editable && !methodHookParam.method.getName().equals("setHint")) {
                utils.debugLog("Not translating " + stringArgs + " for editable TextView");
                callOriginalMethod(stringArgs, methodHookParam);
                return null;
            }
            if (!editable && PreferenceList.Scroll) {
                final TextView textView = (TextView) methodHookParam.thisObject;
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setSelected(true);
                textView.setMarqueeRepeatLimit(-1);
                MovementMethod alreadyScrolling = textView.getMovementMethod();
                if (alreadyScrolling == null) {
                    textView.setVerticalScrollBarEnabled(true);
                    textView.setMovementMethod(new ScrollingMovementMethod());
                    textView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in checking editable TextView : " + Log.getStackTraceString(e));
        }

        if (!isNotWhiteSpace(stringArgs)) {
            callOriginalMethod(stringArgs, methodHookParam);
            return null;
        }

        utils.debugLog("In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);
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
            utils.debugLog("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
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
        return null;
    }

}
