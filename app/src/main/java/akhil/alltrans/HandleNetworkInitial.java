package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.Semaphore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;

/**
 * Created by akhil on 13/6/16.
 */
public class HandleNetworkInitial implements Callback {
    private static String userCredentials;
    private static long lastExpireTime;
    private final Semaphore available = new Semaphore(1, true);
    public HandleNetworkLater handleNetworkLater;

    public void doAll() {
        available.acquireUninterruptibly();
        long time = System.currentTimeMillis();
        if (time > lastExpireTime) {
            Log.i("AllTrans", "AllTrans: Entering get new token for string : " + handleNetworkLater.stringToBeTrans);
            getNewToken();
        } else {
            available.release();
            doInBackground();
        }
    }

    public void getNewToken() {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=alltranstestid1&client_secret=01234567890123456789&scope=http%3A%2F%2Fapi.microsofttranslator.com");
            //RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=alltranskunal&client_secret=o96DhR3OinA9ABssZ9EYrUsF3TtnmeGAkJT4YWMg&scope=http%3A%2F%2Fapi.microsofttranslator.com");
            Request request = new Request.Builder()
                    .url("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();

            client.newCall(request).enqueue(this);

        } catch (Exception e) {
            Log.e("AllTrans", "AllTrans: Got error in getting new token as : " + Log.getStackTraceString(e));
        }
    }

    public void doInBackground() {
        try {
            String baseURL = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text=";
            String languageURL = "&from=ko&to=en";
            String fullURL = baseURL + URLEncoder.encode(handleNetworkLater.stringToBeTrans, "UTF-8") + languageURL;
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(fullURL)
                    .get()
                    .addHeader("authorization", "Bearer " + userCredentials)
                    .build();

            client.newCall(request).enqueue(handleNetworkLater);
        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alltrans.hookAccess.acquireUninterruptibly();
                    unhookMethod(handleNetworkLater.methodHookParam.method, alltrans.newHook);
                    handleNetworkLater.tv.setText(handleNetworkLater.stringToBeTrans);
                    hookMethod(handleNetworkLater.methodHookParam.method, alltrans.newHook);
                    alltrans.hookAccess.release();
                }
            });
        }
    }

    public void onResponse(Call call, Response response) {
        try {
            String result = response.body().string();
            response.body().close();
            Log.i("AllTrans", "AllTrans: Got request result as : " + result);
            JsonParser jsonparser = new JsonParser();
            JsonObject jsonobject = jsonparser.parse(result).getAsJsonObject();
            userCredentials = jsonobject.get("access_token").getAsString();
            Log.i("AllTrans", "AllTrans: Set User Credentials as : " + userCredentials);
            lastExpireTime = System.currentTimeMillis() + 550000;
        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting token as : " + Log.getStackTraceString(e));
        } finally {
            available.release();
            doInBackground();
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("AllTrans", "AllTrans: Got error in getting token as : " + Log.getStackTraceString(e));
        available.release();
        doInBackground();
    }
}
