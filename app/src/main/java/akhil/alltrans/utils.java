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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

class utils {
    public static boolean Debug = true;

    public static boolean isExpModuleActive(Context context) {

        boolean isExp = false;
        if (context == null) {
            throw new IllegalArgumentException("context must not be null!!");
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = null;
            try {
                result = contentResolver.call(uri, "active", null, null);
            } catch (RuntimeException e) {
                // TaiChi is killed, try invoke
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable e1) {
                    return false;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }

            if (result == null) {
                return false;
            }
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    public static List<String> getExpApps(Context context) {
        Bundle result;
        try {
            result = context.getContentResolver().call(Uri.parse("content://me.weishu.exposed.CP/"), "apps", null, null);
        } catch (Throwable e) {
            return Collections.emptyList();
        }

        if (result == null) {
            return Collections.emptyList();
        }
        List<String> list = result.getStringArrayList("apps");
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public static void debugLog(String str) {
        if (Debug) {
            if (str.length() > 3900) {
                Log.i("AllTrans", "AllTrans: " + str.substring(0, 3900));
                debugLog(str.substring(3900));
            } else {
                Log.i("AllTrans", "AllTrans: " + str);
            }
        }
    }

    public static void tryHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback){
        try{
            findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable e){
            utils.debugLog("Cannot hook method - " + clazz.getCanonicalName() + " - " + methodName + Log.getStackTraceString(e));
        }
    }

    public static void tryHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback){
        try{
            findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable e){
            utils.debugLog("Cannot hook method - " + className + " - " + methodName + Log.getStackTraceString(e));
        }
    }

    public static String XMLUnescape(String s) {
        String retVal = s.replaceAll("&amp;", "&");
        retVal = retVal.replaceAll("&quot;", "\"");
        retVal = retVal.replaceAll("&apos;", "'");
        retVal = retVal.replaceAll("&lt;", "<");
        retVal = retVal.replaceAll("&gt;", ">");
        retVal = retVal.replaceAll("&#xD;", "\r");
        retVal = retVal.replaceAll("&#xA;", "\n");

        retVal = retVal.replaceAll("&amp;", "&");
        retVal = retVal.replaceAll("&quot;", "\"");
        retVal = retVal.replaceAll("&apos;", "'");
        retVal = retVal.replaceAll("&lt;", "<");
        retVal = retVal.replaceAll("&gt;", ">");
        retVal = retVal.replaceAll("&#xD;", "\r");
        retVal = retVal.replaceAll("&#xA;", "\n");
        return retVal;
    }

    // Taken from Apache Commons Lang 2.6 at -
    // https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/src-html/org/apache/commons/lang/StringEscapeUtils.html#line.146
    public static String javaScriptEscape(String str) {
        if (str == null) {
            return null;
        }
        StringWriter writer = new StringWriter(str.length() * 2);
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                writer.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                writer.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                writer.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        writer.write('\\');
                        writer.write('b');
                        break;
                    case '\n':
                        writer.write('\\');
                        writer.write('n');
                        break;
                    case '\t':
                        writer.write('\\');
                        writer.write('t');
                        break;
                    case '\f':
                        writer.write('\\');
                        writer.write('f');
                        break;
                    case '\r':
                        writer.write('\\');
                        writer.write('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            writer.write("\\u00" + hex(ch));
                        } else {
                            writer.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        writer.write('\\');
                        writer.write('\'');
                        break;
                    case '"':
                        writer.write('\\');
                        writer.write('"');
                        break;
                    case '\\':
                        writer.write('\\');
                        writer.write('\\');
                        break;
                    case '/':
                        writer.write('\\');
                        writer.write('/');
                        break;
                    default:
                        writer.write(ch);
                        break;
                }
            }
        }
        return writer.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
}
