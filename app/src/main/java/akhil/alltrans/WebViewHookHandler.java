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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;

public class WebViewHookHandler extends XC_MethodHook implements OriginalCallable {
    private WebView webView;

    public void callOriginalMethod(final CharSequence translatedString, final Object userData) {
        String stringToBeTrans = (String) userData;
        String script = "function getAllTextNodes() {\n" +
                "    var result = [];\n" +
                "    var ignore = {\n" +
                "        \"STYLE\": 0,\n" +
                "        \"SCRIPT\": 0,\n" +
                "        \"NOSCRIPT\": 0,\n" +
                "        \"IFRAME\": 0,\n" +
                "        \"OBJECT\": 0\n" +
                "    };\n" +
                "\n" +
                "    (function scanSubTree(node) {\n" +
                "        if (node.tagName in ignore)\n" +
                "            return;\n" +
                "        if (node.childNodes.length)\n" +
                "            for (var i = 0; i < node.childNodes.length; i++)\n" +
                "                scanSubTree(node.childNodes[i]);\n" +
                "        else if (node.nodeType == Node.TEXT_NODE)\n" +
                "            result.push(node);\n" +
                "    })(document);\n" +
                "\n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "all = getAllTextNodes();\n" +
                "\n" +
                "for (var i = 0, max = all.length; i < max; i++) {\n" +
                "    if (all[i].nodeValue.trim() != '')\n" +
                "        if(all[i].nodeValue == \"" + stringToBeTrans + "\")\n" +
                "            all[i].nodeValue = \"" + translatedString + "\";\n" +
                "}";
        webView.evaluateJavascript(script, null);
    }
    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam mParam) throws Throwable {
        Log.i("AllTrans", "AllTrans: we are in onPageFinished!");

        webView = (WebView) mParam.args[0];

        String script1 = "console.log(\" AllTrans HTMLCODE \");console.log(document.body.outerHTML)";
        webView.evaluateJavascript(script1, null);

        String script = "function getAllTextNodes() {\n" +
                "    var result = [];\n" +
                "    var ignore = {\n" +
                "        \"STYLE\": 0,\n" +
                "        \"SCRIPT\": 0,\n" +
                "        \"NOSCRIPT\": 0,\n" +
                "        \"IFRAME\": 0,\n" +
                "        \"OBJECT\": 0\n" +
                "    };\n" +
                "\n" +
                "    (function scanSubTree(node) {\n" +
                "        if (node.tagName in ignore)\n" +
                "            return;\n" +
                "        if (node.childNodes.length)\n" +
                "            for (var i = 0; i < node.childNodes.length; i++)\n" +
                "                scanSubTree(node.childNodes[i]);\n" +
                "        else if (node.nodeType == Node.TEXT_NODE)\n" +
                "            result.push(node);\n" +
                "    })(document);\n" +
                "\n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "all = getAllTextNodes();\n" +

                "\n" +
                "for (var i = 0, max = all.length; i < max; i++) {\n" +
                "    if (all[i].nodeValue.trim() != '')\n" +
                "        injectedObject.showLog(all[i].nodeValue);\n" +
                "}";


        //Insert debug statements to see why it cant get to showLog
        webView.evaluateJavascript(script, null);
//        "\n" +
//                "function isASCII(str) {\n" +
//                "    return /^[\\x00-\\xFF]*$/.test(str);\n" +
//                "}\n" +
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showLog(final String stringArgs) {
        Log.i("AllTrans", "AllTrans: in WebView Showlog " + stringArgs);
        Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);

        GetTranslate getTranslate = new GetTranslate();
        getTranslate.stringToBeTrans = stringArgs;
        getTranslate.originalCallable = this;
        getTranslate.userData = stringArgs;
        getTranslate.canCallOriginal = true;

        if (SetTextHookHandler.isNotWhiteSpace(getTranslate.stringToBeTrans)) {

            GetTranslateToken getTranslateToken = new GetTranslateToken();
            getTranslateToken.getTranslate = getTranslate;

            if (PreferenceList.Caching) {
                alltrans.cacheAccess.acquireUninterruptibly();
                if (alltrans.cache.containsKey(stringArgs)) {
                    final String translatedString = alltrans.cache.get(stringArgs);
                    Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    alltrans.cacheAccess.release();

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callOriginalMethod(translatedString, stringArgs);
                        }
                    }, PreferenceList.Delay);

                    return;
                } else {
                    alltrans.cacheAccess.release();
                }
            }

            getTranslateToken.doAll();

        }
    }
}
