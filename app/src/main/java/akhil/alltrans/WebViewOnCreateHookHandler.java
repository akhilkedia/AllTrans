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
import android.util.Log;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;

class WebViewOnCreateHookHandler extends XC_MethodHook {
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    protected void afterHookedMethod(MethodHookParam methodHookParam) {
        Log.i("AllTrans", "AllTrans: we are after webview Constructor!");
        ((WebView) methodHookParam.thisObject).addJavascriptInterface(alltrans.webViewHookHandler, "injectedObject");
        ((WebView) methodHookParam.thisObject).addJavascriptInterface(methodHookParam.thisObject, "webView");
    }
}

