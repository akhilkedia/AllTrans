package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetTranslateToken implements Callback {
    private static String userCredentials;
    private static long lastExpireTime;
    private final Semaphore available = new Semaphore(1, true);
    public GetTranslate getTranslate;

    public void doAll() {
        available.acquireUninterruptibly();
        long time = System.currentTimeMillis();
        if (time > lastExpireTime) {
            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + "  Entering get new token for string : " + getTranslate.stringToBeTrans);
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
            String fullURL = baseURL + URLEncoder.encode(getTranslate.stringToBeTrans, "UTF-8") + languageURL;
            OkHttpClient client = new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(ConnectionSpec.CLEARTEXT)).build();

            Request request = new Request.Builder()
                    .url(fullURL)
                    .get()
                    .addHeader("authorization", "Bearer " + userCredentials)
                    .build();

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + "  Enqueing Request for new transaltion for : " + getTranslate.stringToBeTrans);
            client.newCall(request).enqueue(getTranslate);
        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
            if (getTranslate.canCallOriginal) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        getTranslate.originalCallable.callOriginalMethod(getTranslate.stringToBeTrans, getTranslate.userData);
                    }
                });
            }
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
            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + "  Set User Credentials as : " + userCredentials);
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
