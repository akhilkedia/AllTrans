package akhil.alltrans;

import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class WebViewHookHandler extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        XposedBridge.log("AllTrans: we are in onPageFinished!");
        WebView webView = (WebView) param.args[0];
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
                "        all[i].nodeValue = \"hello\";\n" +
                "}";
        webView.evaluateJavascript(script, null);
    }
}
