package akhil.alltrans;

import android.Manifest;
import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by akhil on 23/6/16.
 */
public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                writeCache();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
        }, Manifest.permission.CAMERA);

    }

    public void writeCache() {
        try {
            XposedBridge.log("AllTrans: trying to write cache");

            File folder = new File(Environment.getExternalStorageDirectory() + "/AllTrans");
            String path = folder.getPath();
            if (!folder.exists() && !folder.mkdirs()) {
                XposedBridge.log("AllTrans: Cannot Make Directory " + path);
            } else {
                XposedBridge.log("AllTrans: Directory done" + path);
            }

            File cacheFile = new File(folder, AndroidAppHelper.currentPackageName());
            ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(cacheFile));

            alltrans.cacheAccess.acquireUninterruptibly();
            s.writeObject(alltrans.cache);
            alltrans.cacheAccess.release();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            XposedBridge.log("AllTrans: Got error in onActivityDestroyed: " + sw.toString());
        }
    }
}

