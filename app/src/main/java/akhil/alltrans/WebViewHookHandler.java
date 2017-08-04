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
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;

public class WebViewHookHandler extends XC_MethodHook implements OriginalCallable {

    public void callOriginalMethod(final CharSequence translatedString, final Object userData) {
        WebHookUserData webHookUserData = (WebHookUserData) userData;
        final String originalString = StringEscape.javaScriptEscape(webHookUserData.stringArgs);
        final String newString = StringEscape.javaScriptEscape(translatedString.toString());
        WebView webView = webHookUserData.webView;
        Log.i("AllTrans", "AllTrans: In callOriginalMethod webView. Trying to replace -" + originalString + "-with-" + newString);
        String script = "function getAllTextNodes(tempDocument) {\n" +
                " var result = [];\n" +
                " var ignore = {\n" +
                "  \"STYLE\": 0,\n" +
                "  \"SCRIPT\": 0,\n" +
                "  \"NOSCRIPT\": 0,\n" +
                "  \"IFRAME\": 0,\n" +
                "  \"OBJECT\": 0,\n" +
                " };\n" +
                " (function scanSubTree(node) {\n" +
                "  if (node.tagName in ignore) {\n" +
                "   return;\n" +
                "  }\n" +
                "  if (node.childNodes.length) {\n" +
                "   for (var i = 0; i < node.childNodes.length; i++) {\n" +
                "    scanSubTree(node.childNodes[i]);\n" +
                "   }\n" +
                "  } else if (node.nodeType == 3 || node.nodeType == 1) {\n" +
                "   result.push(node);\n" +
                "  }\n" +
                " })(tempDocument);\n" +
                " return result;\n" +
                "}\n" +
                "\n" +
                "function doReplaceAll(all){\n" +
                " for (var i = 0, max = all.length; i < max; i++) {\n" +
                "  if (all[i].nodeType == 1 && all[i].childNodes.length == 0) {\n" +
                "        if (all[i].nodeValue == \"" + originalString + "\") {\n" +
                "            all[i].nodeValue = \"" + newString + "\";\n" +
                "        }\n" +
                "  }\n" +
                "  else if (all[i].nodeType == 3 && all[i].nodeValue.trim() != '') {\n" +
                "        if (all[i].nodeValue == \"" + originalString + "\") {\n" +
                "            all[i].nodeValue = \"" + newString + "\";\n" +
                "        }\n" +
                "  }\n" +
                " }\n" +
                "}\n" +
                "\n" +
                "for (var j = 0; j < window.frames.length; j++) { \n" +
                " all = getAllTextNodes(window.frames[j].document);\n" +
                " doReplaceAll(all);\n" +
                "}\n" +
                "all = getAllTextNodes(window.document);\n" +
                "doReplaceAll(all);";
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.i("AllTrans", "AllTrans: we did replace-" + originalString);
            }
        });
    }
    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam mParam) throws Throwable {
        Log.i("AllTrans", "AllTrans: we are in onPageFinished!");

        WebView webView = (WebView) mParam.args[0];
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String scriptFrames = "console.log(\"AllTrans: Frames is \"+window.frames.length)";
        webView.evaluateJavascript(scriptFrames, null);
        String script1 = "console.log(\" AllTrans HTMLCODE \");console.log(document.body.outerHTML);";
        webView.evaluateJavascript(script1, null);

        String script = "function getAllTextNodes(e){var l=[],o={STYLE:0,SCRIPT:0,NOSCRIPT:0,IFRAME:0,OBJECT:0};return function e(d){if(!(d.tagName in o))if(d.childNodes.length)for(var n=0;n<d.childNodes.length;n++)e(d.childNodes[n]);else 3!=d.nodeType&&1!=d.nodeType||l.push(d)}(e),l}function doReplaceAll(e){for(var l=0,o=e.length;l<o;l++)1==e[l].nodeType&&0==e[l].childNodes.length?injectedObject.showLog(e[l].nodeValue,webView):3==e[l].nodeType&&\"\"!=e[l].nodeValue.trim()&&injectedObject.showLog(e[l].nodeValue,webView)}console.log(\"AllTrans: JavaScript is Indeed Enabled\");for(var j=0;j<window.frames.length;j++)all=getAllTextNodes(window.frames[j].document),doReplaceAll(all);all=getAllTextNodes(window.document),doReplaceAll(all);";

        //Insert debug statements to see why it cant get to showLog
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.i("AllTrans", "AllTrans: we did evaluate the Javascript!");
            }
        });
//        "\n" +
//                "function isASCII(str) {\n" +
//                "    return /^[\\x00-\\xFF]*$/.test(str);\n" +
//                "}\n" +
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showLog(final String stringArgs, WebView webView) {
        Log.i("AllTrans", "AllTrans: in WebView Showlog " + stringArgs);
        Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);

        final GetTranslate getTranslate = new GetTranslate();
        getTranslate.stringToBeTrans = stringArgs;
        getTranslate.originalCallable = this;
        getTranslate.userData = new WebHookUserData(webView, stringArgs);
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
                            callOriginalMethod(translatedString, getTranslate.userData);
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

class WebHookUserData {
    public WebView webView;
    public String stringArgs;

    public WebHookUserData(WebView webViewIn, String stringArgsIn) {
        webView = webViewIn;
        stringArgs = stringArgsIn;
    }
}
