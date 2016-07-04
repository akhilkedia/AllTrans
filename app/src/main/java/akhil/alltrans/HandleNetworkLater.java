package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class HandleNetworkLater implements Callback {
    public String stringToBeTrans;
    public String translatedString;
    XC_MethodHook.MethodHookParam methodHookParam;

    @Override
    public void onResponse(Call call, Response response) {
        try {
            String result = response.body().string();
            response.body().close();

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + "  Got request result as : " + result);
            translatedString = result.substring(result.toString().indexOf('>') + 1, result.toString().lastIndexOf('<'));
            translatedString = StringEscape.XMLUnescape(translatedString);

            alltrans.cacheAccess.acquireUninterruptibly();
            alltrans.cache.put(stringToBeTrans, translatedString);
            alltrans.cacheAccess.release();

            Log.i("AllTrans", "AllTrans: In Thread " + Thread.currentThread().getId() + " In HandleNetworkLater, setting: " + stringToBeTrans + " to :" + translatedString);

        } catch (java.io.IOException e) {
            Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
            translatedString = stringToBeTrans;
        } finally {
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    SetTextHookHandler.callOriginalMethod(methodHookParam, translatedString);
                }
            });
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.e("AllTrans", "AllTrans: Got error in getting translation as : " + Log.getStackTraceString(e));
        translatedString = stringToBeTrans;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                SetTextHookHandler.callOriginalMethod(methodHookParam, translatedString);
            }
        });
    }
}