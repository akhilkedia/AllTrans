package akhil.alltrans;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.unhookMethod;

/**
 * Created by akhil on 13/6/16.
 */
public class HandleNetworkLater implements Callback {
    public TextView tv;
    public String stringToBeTrans;
    public String translatedString;
    XC_MethodHook.MethodHookParam methodHookParam;

    @Override
    public void onResponse(Call call, Response response) {
        try {
            String result = response.body().string();
            response.body().close();
            XposedBridge.log("AllTrans: Got request result as : " + result);
            translatedString = result.substring(result.toString().indexOf('>') + 1, result.toString().lastIndexOf('<'));
            translatedString = StringEscape.XMLUnescape(translatedString);
            XposedBridge.log("AllTrans: In HandleNetworkLater, setting: " + stringToBeTrans + " to :" + translatedString);

        } catch (java.io.IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            XposedBridge.log("AllTrans: Got error in getting translation as : " + sw.toString());
            translatedString = stringToBeTrans;
        } finally {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    unhookMethod(methodHookParam.method, alltrans.newhook);
                    tv.setText(translatedString);
                    hookMethod(methodHookParam.method, alltrans.newhook);
                }
            });
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        XposedBridge.log("AllTrans: Got error in getting translation as : " + sw.toString());
        translatedString = stringToBeTrans;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                unhookMethod(methodHookParam.method, alltrans.newhook);
                tv.setText(translatedString);
                hookMethod(methodHookParam.method, alltrans.newhook);
            }
        });
    }
}