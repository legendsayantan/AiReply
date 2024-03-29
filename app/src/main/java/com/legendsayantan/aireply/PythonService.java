package com.legendsayantan.aireply;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonService extends Service {
    public static boolean status = false;
    static Python py;
    static PyObject console;
    static File db;
    static PyObject textOutputStream;
    static String code,code2,code3;
    static Context context;
    static PyObject init;
    static PyObject sys;
    static PyObject io;
    static PyObject obj;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        context = getApplicationContext();
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(getApplicationContext()));
            System.out.println("Starting python");
        }
        py = Python.getInstance();
        db = new File(getFilesDir(),"db.sqlite3");
        init = py.getModule("_init_");
        init.callAttr("start",db.getAbsolutePath());
        obj = init.get("chatbot");
        status = true;
        System.out.println("initiated.");
        try {
            MainActivity.c.setVisibility(View.GONE);
        }catch (Exception ignored){}
    }

    public static String getResponse(String string){
        String out = "";
        try {
            out = obj.callAttrThrows("get_response",string).toString();
        } catch (Throwable throwable) {
            System.err.println("ERROR\n"+throwable.getMessage());
            throwable.printStackTrace();
        }
        return out;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = getPackageName()+".background";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    String readAsset(String file){
        String data = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(file), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                data=data+"\n"+mLine;
            }
        } catch (IOException e) {
            showText(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) { }
            }
        }
        return data;
    }
    public void showText(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
}
