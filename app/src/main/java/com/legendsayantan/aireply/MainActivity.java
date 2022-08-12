package com.legendsayantan.aireply;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    static boolean manageStorage = false;
    static JobScheduler jobScheduler;
    static ConstraintLayout c;
    static AsyncTask<Void, Void, Void> task;
    static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        c = findViewById(R.id.loader);
        activity = this;
    }
    public static void createInit(){
        task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                initialize(activity);
                return null;
            }
        };
    }
    public static void startInit() {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static void initialize(Activity activity){
        boolean nltk;
        try {
            nltk = nltkVerify();
        }catch (IOException e) {
            nltk=false;
        }

        if (storageAccess() && nltk && databasePresent()) {
            scheduleJob(activity.getApplicationContext());
        } else {
            activity.startActivity(new Intent(activity.getApplicationContext(),InitialiseActivity.class));
        }
    }
    public static boolean storageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(manageStorage) return Environment.isExternalStorageManager() && ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            else return ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    public static boolean nltkVerify() throws IOException {
        File f = new File(activity.getFilesDir(), "nltk_data");
        InputStream in = activity.getAssets().open("nltk_data.zip");
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry entry;
        long size = 0;
        while ((entry = zipInputStream.getNextEntry())!=null){
            size = size + entry.getSize();
        }
        System.out.println(FileUtil.getDirectorySize(f)+" --- "+size);
        return FileUtil.getDirectorySize(f)==size;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context
                    .getSystemService(JOB_SCHEDULER_SERVICE);
        }
        ComponentName componentName = new ComponentName(context,
                JobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setOverrideDeadline(0)
                .setPersisted(true).build();
        jobScheduler.schedule(jobInfo);
    }

    @Override
    protected void onResume() {
        c.setVisibility(View.VISIBLE);
        createInit();
        startInit();
        if(storageAccess()){
            new ColourTheme(activity);
            ColourTheme.initContainer(c);
            TextView textView = findViewById(R.id.tv1);
            TextView textView2 = findViewById(R.id.tv2);
            TextView textView3 = findViewById(R.id.tv3);
            TextView textView4 = findViewById(R.id.tv4);
            TextView textView5 = findViewById(R.id.tv5);
            TextView textView6 = findViewById(R.id.tv6);
            ImageView imageView1 = findViewById(R.id.imageView);
            ImageView imageView2 = findViewById(R.id.imageView2);
            ImageView imageView3 = findViewById(R.id.imageView3);
            imageView3.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),ChatActivity.class)));
            ColourTheme.initContainer(findViewById(R.id.container));
            ColourTheme.initTextView(textView);
            ColourTheme.initTextView(textView2);
            ColourTheme.initTextView(textView3);
            ColourTheme.initTextView(textView4);
            ColourTheme.initTextView(textView5);
            ColourTheme.initTextView(textView6);
            ColourTheme.initImageView(imageView1);
            ColourTheme.initImageView(imageView2);
            ColourTheme.initImageView(imageView3);
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(PythonService.status){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    c.setVisibility(View.GONE);
                                }
                            });
                            this.cancel();
                        }else throw new Exception("to be catched");
                    } catch (Exception ignored) {}
                }
            },1000,1000);
        }
        super.onResume();
    }
    public static boolean databasePresent(){
        return new File(activity.getFilesDir(),"db.sqlite3").exists();
    }
}