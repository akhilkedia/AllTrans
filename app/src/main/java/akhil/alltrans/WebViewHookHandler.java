package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class WebViewHookHandler extends XC_MethodHook implements OriginalCallable {
    public MethodHookParam methodHookParam;
    public WebView webView;

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
                "function isASCII(str) {\n" +
                "    return /^[\\x00-\\xFF]*$/.test(str);\n" +
                "}\n" +
                "\n" +
                "for (var i = 0, max = all.length; i < max; i++) {\n" +
                "    if (all[i].nodeValue.trim() != '')\n" +
                "    \tif(!isASCII(all[i].nodeValue))\n" +
                "    \t\tif(all[i].nodeValue == \"" + stringToBeTrans + "\")\n" +
                "        \t\tall[i].nodeValue = \"" + translatedString + "\";\n" +
                "}";
        webView.evaluateJavascript(script, null);
    }
    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam mParam) throws Throwable {
        XposedBridge.log("AllTrans: we are in onPageFinished!");

        methodHookParam = mParam;
        webView = (WebView) methodHookParam.args[0];
        webView.addJavascriptInterface(this, "injectedObject");

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
                "        injectedObject.showlog(all[i].nodeValue);\n" +
                "}";


        //Insert debug statements to see why it cant get to showlog
        webView.evaluateJavascript(script, null);
//        "\n" +
//                "function isASCII(str) {\n" +
//                "    return /^[\\x00-\\xFF]*$/.test(str);\n" +
//                "}\n" +
    }

    @JavascriptInterface
    public void showlog(final String stringArgs) {
        Log.i("AllTrans", "AllTrans: in WebView Showlog " + stringArgs);
        Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " Recognized non-english string: " + stringArgs);

        GetTranslate getTranslate = new GetTranslate();
        getTranslate.stringToBeTrans = stringArgs;
        getTranslate.originalCallable = this;
        getTranslate.userData = stringArgs;
        getTranslate.canCallOriginal = false;

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
