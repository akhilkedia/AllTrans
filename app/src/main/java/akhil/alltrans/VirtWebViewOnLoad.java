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
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.FileOutputStream;

import static android.content.Context.MODE_APPEND;

public class VirtWebViewOnLoad implements OriginalCallable {

    public void callOriginalMethod(final CharSequence translatedString, final Object userData) {
        WebHookUserData2 webHookUserData2 = (WebHookUserData2) userData;
        WebView webView = webHookUserData2.webView;
        final String originalString = utils.javaScriptEscape(webHookUserData2.stringArgs);
        final String newString = utils.javaScriptEscape(translatedString.toString());
        if (newString == null || newString.equals(originalString)){
            return;
        }
        utils.debugLog("In callOriginalMethod webView. Trying to replace -" + originalString + "-with-" + newString);
        String script = "var AllTransPlaceholderTypes = {\n" +
                "    'date': 0,\n" +
                "    'datetime-local': 0,\n" +
                "    'email': 0,\n" +
                "    'month': 0,\n" +
                "    'number': 0,\n" +
                "    'password': 0,\n" +
                "    'tel': 0,\n" +
                "    'time': 0,\n" +
                "    'url': 0,\n" +
                "    'week': 0,\n" +
                "    'search': 0,\n" +
                "    'text': 0,\n" +
                "};\n" +
                "\n" +
                "var AllTransInputTypes = {\n" +
                "  'button': 0,\n" +
                "  'reset': 0,\n" +
                "  'submit': 0,\n" +
                "};\n" +
                "\n" +
                "function allTransGetAllTextNodes(tempDocument) {\n" +
                "  var result = [];\n" +
                "  var ignore = {\n" +
                "    'STYLE': 0,\n" +
                "    'SCRIPT': 0,\n" +
                "    'NOSCRIPT': 0,\n" +
                "    'IFRAME': 0,\n" +
                "    'OBJECT': 0,\n" +
                "  };\n" +
                "  (function scanSubTree(node) {\n" +
                "    if (node.tagName in ignore) {\n" +
                "      return;\n" +
                "    }\n" +
                "    if (node.tagName && node.tagName.toLowerCase() == 'input' && (node.type in AllTransInputTypes || node.type in AllTransPlaceholderTypes)) {\n" +
                "      result.push(node);\n" +
                "    }\n" +
                "    if (node.childNodes.length) {\n" +
                "      for (var i = 0; i < node.childNodes.length; i++) {\n" +
                "        scanSubTree(node.childNodes[i]);\n" +
                "      }\n" +
                "    } else if (node.nodeType == 3 || node.nodeType == 1) {\n" +
                "      result.push(node);\n" +
                "    }\n" +
                "  })(tempDocument);\n" +
                "  return result;\n" +
                "}\n" +
                "\n" +
                "function allTransDoReplaceAllFinal(all) {\n" +
                "  for (var i = 0, max = all.length; i < max; i++) {\n" +
                "    if (all[i].tagName && all[i].tagName.toLowerCase() == 'input' && all[i].type in AllTransPlaceholderTypes) {\n" +
                "      if (all[i].placeholder == \"" + originalString + "\") {\n" +
                "        all[i].placeholder = \"" + newString + "\";\n" +
                "      continue;\n" +
                "      }\n" +
                "    }\n" +
                "    if (all[i].nodeType == 1 && all[i].childNodes.length == 0) {\n" +
                "      if (all[i].nodeValue == \"" + originalString + "\") {\n" +
                "        all[i].nodeValue = \"" + newString + "\";\n" +
                "      }\n" +
                "    } else if (all[i].nodeType == 3 && all[i].nodeValue.trim() != \"\") {\n" +
                "      if (all[i].nodeValue == \"" + originalString + "\") {\n" +
                "        all[i].nodeValue = \"" + newString + "\";\n" +
                "      }\n" +
                "    }\n" +
                "    if (all[i].tagName && all[i].tagName.toLowerCase() == 'input' && all[i].type in AllTransInputTypes) {\n" +
                "      if (all[i].value == \"" + originalString + "\") {\n" +
                "        all[i].value = \"" + newString + "\";\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "for (var j = 0; j < window.frames.length; j++) {\n" +
                "  all = allTransGetAllTextNodes(window.frames[j].document);\n" +
                "  allTransDoReplaceAllFinal(all);\n" +
                "}\n" +
                "\n" +
                "all = allTransGetAllTextNodes(window.document);\n" +
                "allTransDoReplaceAllFinal(all);";

        myEvaluateJavaScript(webView, script);
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void afterOnLoadMethod(WebView webView) throws Throwable {
        utils.debugLog("we are in onPageFinished!");

        String scriptFrames = "console.log(\"AllTrans: Frames is \"+window.frames.length)";
        myEvaluateJavaScript(webView, scriptFrames);

        // String script1 = "console.log(\" AllTrans HTMLCode \");injectedObject.WriteHTML(document.body.outerHTML);";
        // webView.evaluateJavascript(script1, null);

        String script = "console.log('AllTrans: JavaScript is Indeed Enabled');\n" +
                "\n" +
                "var AllTransPlaceholderTypes = {\n" +
                "    'date': 0,\n" +
                "    'datetime-local': 0,\n" +
                "    'email': 0,\n" +
                "    'month': 0,\n" +
                "    'number': 0,\n" +
                "    'password': 0,\n" +
                "    'tel': 0,\n" +
                "    'time': 0,\n" +
                "    'url': 0,\n" +
                "    'week': 0,\n" +
                "    'search': 0,\n" +
                "    'text': 0,\n" +
                "};\n" +
                "\n" +
                "var AllTransInputTypes = {\n" +
                "    'button': 0,\n" +
                "    'reset': 0,\n" +
                "    'submit': 0,\n" +
                "};\n" +
                "\n" +
                "function allTransGetAllTextNodes(tempDocument) {\n" +
                "    var result = [];\n" +
                "    var ignore = {\n" +
                "        'STYLE': 0,\n" +
                "        'SCRIPT': 0,\n" +
                "        'NOSCRIPT': 0,\n" +
                "        'IFRAME': 0,\n" +
                "        'OBJECT': 0,\n" +
                "    };\n" +
                "    (function scanSubTree(node) {\n" +
                "        if (node.tagName in ignore) {\n" +
                "            return;\n" +
                "        }\n" +
                "        if (node.tagName && node.tagName.toLowerCase() == 'input' && (node.type in AllTransInputTypes || node.type in AllTransPlaceholderTypes)) {\n" +
                "            result.push(node);\n" +
                "        }\n" +
                "        if (node.childNodes.length) {\n" +
                "            for (var i = 0; i < node.childNodes.length; i++) {\n" +
                "                scanSubTree(node.childNodes[i]);\n" +
                "            }\n" +
                "        } else if (node.nodeType == 3 || node.nodeType == 1) {\n" +
                "            result.push(node);\n" +
                "        }\n" +
                "    })(tempDocument);\n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "function allTransDoReplaceAll(all) {\n" +
                "    for (var i = 0, max = all.length; i < max; i++) {\n" +
                "        if (all[i].tagName && all[i].tagName.toLowerCase() == 'input' && all[i].type in AllTransPlaceholderTypes) {\n" +
                "            injectedObject.showLog(all[i].placeholder, webView);\n" +
                "            continue;\n" +
                "        }\n" +
                "        if (all[i].nodeType == 1 && all[i].childNodes.length == 0) {\n" +
                "            injectedObject.showLog(all[i].nodeValue, webView);\n" +
                "        } else if (all[i].nodeType == 3 && all[i].nodeValue.trim() != '') {\n" +
                "            injectedObject.showLog(all[i].nodeValue, webView);\n" +
                "        }\n" +
                "        if (all[i].tagName && all[i].tagName.toLowerCase() == 'input' && all[i].type in AllTransInputTypes) {\n" +
                "            injectedObject.showLog(all[i].value, webView);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "function doAll() {\n" +
                "    for (var j = 0; j < window.frames.length; j++) {\n" +
                "        all = allTransGetAllTextNodes(window.frames[j].document);\n" +
                "        allTransDoReplaceAll(all);\n" +
                "    }\n" +
                "\n" +
                "    all = allTransGetAllTextNodes(window.document);\n" +
                "    allTransDoReplaceAll(all);\n" +
                "    window.alreadyTranslating = false\n" +
                "}\n" +
                "\n" +
                "MutationObserver = window.MutationObserver || window.WebKitMutationObserver;\n" +
                "\n" +
                "var observer = new MutationObserver(function(mutations, observer) {\n" +
                "    if (window.alreadyTranslating != true){\n" +
                "        window.alreadyTranslating = true;\n" +
                "        setTimeout(doAll, 500);\n" +
                "    }\n" +
                "});\n" +
                "\n" +
                "observer.observe(document, {\n" +
                "  subtree: true,\n" +
                "  childList: true\n" +
                "});\n" +
                "setTimeout(doAll, " + PreferenceList.DelayWebView + ");";

        myEvaluateJavaScript(webView, script);
//        "\n" +
//                "function isASCII(str) {\n" +
//                "    return /^[\\x00-\\xFF]*$/.test(str);\n" +
//                "}\n" +
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showLog(final String stringArgs, WebView webView) {
        if (stringArgs == null){
            return;
        }
        utils.debugLog("in WebView Showlog " + stringArgs);
        utils.debugLog("In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);

        final GetTranslate getTranslate = new GetTranslate();
        getTranslate.stringToBeTrans = stringArgs;
        getTranslate.originalCallable = this;
        getTranslate.userData = new WebHookUserData2(webView, stringArgs);
        getTranslate.canCallOriginal = true;

        if (SetTextHookHandler.isNotWhiteSpace(getTranslate.stringToBeTrans)) {

            GetTranslateToken getTranslateToken = new GetTranslateToken();
            getTranslateToken.getTranslate = getTranslate;

            if (PreferenceList.Caching) {
                alltrans.cacheAccess.acquireUninterruptibly();
                if (alltrans.cache.containsKey(stringArgs) && alltrans.cache.get(stringArgs) != null) {
                    final String translatedString = alltrans.cache.get(stringArgs);
                    utils.debugLog("In Thread " + Thread.currentThread().getId() + " found string in cache: " + stringArgs + " as " + translatedString);
                    alltrans.cacheAccess.release();

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            assert translatedString != null;
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

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void WriteHTML(String html) {
        try {
            FileOutputStream fileOutputStream = alltrans.context.openFileOutput("HTMLPage", MODE_APPEND);
            alltrans.cacheAccess.acquireUninterruptibly();
            fileOutputStream.write(html.getBytes());
            fileOutputStream.close();
            alltrans.cacheAccess.release();
        } catch (Throwable e) {
            utils.debugLog("Exception while writing HTML" + e);
        }
    }

    public void myEvaluateJavaScript(WebView webView, String script) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    utils.debugLog("Got result of running js script as " + value);
                }
            });
        } else {
            webView.loadUrl("javascript:" + script);
        }
    }
}

class WebHookUserData2 {
    public final WebView webView;
    public final String stringArgs;

    public WebHookUserData2(WebView webViewIn, String stringArgsIn) {
        webView = webViewIn;
        stringArgs = stringArgsIn;
    }
}
