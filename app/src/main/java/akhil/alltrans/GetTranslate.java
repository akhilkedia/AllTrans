package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class GetTranslate implements Callback {
    public String stringToBeTrans;
    public String translatedString;
    public OriginalCallable originalCallable;
    public boolean canCallOriginal;
    public Object userData;

    @Override
    public void onResponse(Call call, Response response) {
        try {
            String result = response.body().string();
            response.body().close();

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + "  Got request result as : " + result);
            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " In GetTranslate, setting: " + stringToBeTrans + "got response as " + result);
            translatedString = result.substring(result.indexOf('>') + 1, result.lastIndexOf('<'));
            translatedString = StringEscape.XMLUnescape(translatedString);

            if (PreferenceList.Caching) {
                alltrans.cacheAccess.acquireUninterruptibly();
                alltrans.cache.put(stringToBeTrans, translatedString);
                alltrans.cacheAccess.release();
            }

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " In GetTranslate, setting: " + stringToBeTrans + " to :" + translatedString);

        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
            translatedString = stringToBeTrans;
        } finally {
//            try {
//                if(PreferenceList.Delay>0)
//                    Thread.sleep(PreferenceList.Delay);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " In GetTranslate calling callOriginalMethod with argument - " + translatedString);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    originalCallable.callOriginalMethod(translatedString, userData);
                }
            });
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
        translatedString = stringToBeTrans;

        if (canCallOriginal) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    originalCallable.callOriginalMethod(translatedString, userData);
                }
            }, PreferenceList.Delay);
        }
    }
}