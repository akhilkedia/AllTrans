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
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewClientWrapper extends WebViewClient {
    private final WebViewClient oriClient;

    public WebViewClientWrapper(WebViewClient ori) {
        super();
        oriClient = ori;
    }

    @Override
    public int hashCode() {
        //noinspection ResultOfMethodCallIgnored
        super.hashCode();
        return oriClient.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        //noinspection ResultOfMethodCallIgnored
        super.equals(obj);
        return oriClient.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new WebViewClientWrapper(oriClient);
    }

    @Override
    public String toString() {
        //noinspection ResultOfMethodCallIgnored
        super.toString();
        return oriClient.toString();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        super.shouldOverrideUrlLoading(view, url);
        return oriClient.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.shouldOverrideUrlLoading(view, request);
            return oriClient.shouldOverrideUrlLoading(view, request);
        } else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        oriClient.onPageStarted(view, url, favicon);
    }

//    private static void writeToSDFile(String html){
//
//        // Find the root of the external storage.
//        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
//
//        File root = android.os.Environment.getExternalStorageDirectory();
//
//        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
//
//        File dir = new File (root.getAbsolutePath() + "/download");
//        dir.mkdirs();
//        File file = new File(dir, "webview.html");
//
//        try {
//            FileOutputStream f = new FileOutputStream(file);
//            PrintWriter pw = new PrintWriter(f);
//            pw.println(html);
//            pw.flush();
//            pw.close();
//            f.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        oriClient.onPageFinished(view, url);
//        view.evaluateJavascript(
//                "(function() { return new XMLSerializer().serializeToString(document); })();",
//                new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String html) {
//                        writeToSDFile(html);
//                        utils.debugLog(html);
//                        // code here
//                    }
//                });
        try {
            alltrans.virtWebViewOnLoad.afterOnLoadMethod(view);
        } catch (Throwable e) {
            utils.debugLog("Got error in onActivityDestroyed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        oriClient.onLoadResource(view, url);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oriClient.onPageCommitVisible(view, url);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        super.shouldInterceptRequest(view, url);
        return oriClient.shouldInterceptRequest(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.shouldInterceptRequest(view, request);
            return oriClient.shouldInterceptRequest(view, request);
        } else {
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        super.onTooManyRedirects(view, cancelMsg, continueMsg);
        oriClient.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        oriClient.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oriClient.onReceivedError(view, request, error);
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oriClient.onReceivedHttpError(view, request, errorResponse);
        }
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        super.onFormResubmission(view, dontResend, resend);
        oriClient.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        oriClient.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        oriClient.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        super.onReceivedClientCertRequest(view, request);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            oriClient.onReceivedClientCertRequest(view, request);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
        oriClient.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        super.shouldOverrideKeyEvent(view, event);
        return oriClient.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        super.onUnhandledKeyEvent(view, event);
        oriClient.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        oriClient.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
        oriClient.onReceivedLoginRequest(view, realm, account, args);
    }
}
