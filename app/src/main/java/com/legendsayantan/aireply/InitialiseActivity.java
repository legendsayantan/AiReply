package com.legendsayantan.aireply;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import static com.legendsayantan.aireply.MainActivity.manageStorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.borutsky.neumorphism.NeumorphicFrameLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InitialiseActivity extends AppCompatActivity {
    static Activity activity;
    ArrayList<ImageView> imageViews;
    ArrayList<TextView> textViews;
    boolean nltk;
    long lastBack=System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialise);
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        imageViews = new ArrayList<>();
        textViews = new ArrayList<>();
        imageViews.add(findViewById(R.id.imageView1));
        imageViews.add(findViewById(R.id.imageView2));
        imageViews.add(findViewById(R.id.imageView3));
        imageViews.add(findViewById(R.id.imageView4));
        textViews.add(findViewById(R.id.textView1));
        textViews.add(findViewById(R.id.textView2));
        textViews.add(findViewById(R.id.textView3));
        textViews.add(findViewById(R.id.textView4));
        textViews.add(findViewById(R.id.tv1));
        textViews.add(findViewById(R.id.textView));
        activity=this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }
    public void refresh(){
        try {
            nltk = nltkVerify();
        } catch (IOException e) {
            nltk = false;
        }
        if (storageAccess()) {
            ActivityCompat.requestPermissions(activity,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1003);
            imageViews.get(0).setImageResource(R.drawable.ic_baseline_done_24);
            textViews.get(0).setOnClickListener(view -> {});
            if(nltk){
                imageViews.get(1).setImageResource(R.drawable.ic_baseline_done_24);
                textViews.get(1).setOnClickListener(view -> {});
                if(databasePresent()){
                    imageViews.get(2).setImageResource(R.drawable.ic_baseline_done_24);
                    textViews.get(2).setOnClickListener(view -> {});
                    imageViews.get(3).setImageResource(R.drawable.ic_baseline_arrow_forward_24);
                    textViews.get(3).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                }else{
                    textViews.get(2).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            textViews.get(2).setOnClickListener(view1 -> {});
                            Toast.makeText(getApplicationContext(),"Installing module, please wait",Toast.LENGTH_LONG).show();
                            if(copyFileFromAsset("db.sqlite3",new File(getFilesDir(),"db.sqlite3"))){
                                Toast.makeText(getApplicationContext(),"Successfully installed AI module",Toast.LENGTH_LONG).show();
                                refresh();
                            }else{
                                Toast.makeText(getApplicationContext(),"Install Failed AI module",Toast.LENGTH_LONG).show();
                                imageViews.get(2).setImageResource(R.drawable.ic_baseline_close_24);
                            }
                        }
                    });
                }
            }else{
                textViews.get(1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        textViews.get(1).setOnClickListener(view1 -> {});
                        Toast.makeText(getApplicationContext(),"Installing , please wait",Toast.LENGTH_LONG).show();
                        try {
                            unZipNltk();
                        } catch (IOException e) {
                            imageViews.get(1).setImageResource(R.drawable.ic_baseline_close_24);
                            Toast.makeText(getApplicationContext(),"Install failed, please try again",Toast.LENGTH_LONG).show();
                            refresh();
                        }
                    }
                });
            }
        }else{
            textViews.get(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(manageStorage){//Manage permission
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                            Toast.makeText(getApplicationContext(),"Find "+getResources().getString(R.string.app_name)+" and enable storage permissions.",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            new Timer().scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    if(Environment.isExternalStorageManager()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ActivityCompat.requestPermissions(activity,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
                                            }
                                        });
                                        this.cancel();
                                    }
                                }
                            },2000,1000);
                        }else{
                            ActivityCompat.requestPermissions(activity,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
                        }
                    }else{
                        ActivityCompat.requestPermissions(activity,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1002&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            refreshTheme();
            refresh();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public boolean storageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(manageStorage) return Environment.isExternalStorageManager() && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
            else return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    public boolean nltkVerify() throws IOException {
        File f = new File(getFilesDir(), "nltk_data");
        InputStream in = getAssets().open("nltk_data.zip");
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry entry;
        long size = 0;
        while ((entry = zipInputStream.getNextEntry())!=null){
            size = size + entry.getSize();
        }
        return FileUtil.getDirectorySize(f)==size;
    }
    public void unZipNltk() throws IOException {
        InputStream inputStream = getAssets().open("nltk_data.zip");
        File folder = getFilesDir();
        File nltk = new File(folder,"nltk_data");
        if(nltk.exists())nltk.delete();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(FileUtil.unzipNltk(inputStream,folder)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Installed nltk dependencies.",Toast.LENGTH_LONG).show();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageViews.get(1).setImageResource(R.drawable.ic_baseline_close_24);
                            Toast.makeText(getApplicationContext(),"Install failed, please try again",Toast.LENGTH_LONG).show();
                            refresh();
                        }
                    });
                }
            }
        }).start();
    }
    @Override
    public void onBackPressed() {
        if(storageAccess()&&nltk&&databasePresent()) super.onBackPressed();
        else if(System.currentTimeMillis()-3000<lastBack) {
            finishAffinity();
            finish();
        } else {
                 Toast.makeText(getApplicationContext(),"Go back again to exit app",Toast.LENGTH_LONG).show();
                 lastBack=System.currentTimeMillis();
        }
    }

    @Override
    protected void onResume() {
        refreshTheme();
        super.onResume();
    }
    public void refreshTheme(){
        if(storageAccess()){
            new ColourTheme(this);
            if(ColourTheme.getDrawable()!=null) {
                for(ImageView i : imageViews){
                    ColourTheme.initImageView(i);
                }
                for(TextView t : textViews){
                    ColourTheme.initTextView(t);
                }
                ColourTheme.initContainer(findViewById(R.id.container));
            }
        }
    }
    public boolean databasePresent(){
        File folder = getFilesDir();
        File dbFile = new File(folder,"db.sqlite3");
        return dbFile.exists();
    }
    public boolean copyFileFromAsset(String filename, File dst){
        boolean[] result = {true};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getAssets().open(filename+"-shm");
                    OutputStream outputStream = new FileOutputStream(dst+"-shm");
                    copyFile(inputStream,outputStream);
                } catch (IOException e) {
                    result[0] = false;
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getAssets().open(filename+"-wal");
                    OutputStream outputStream = new FileOutputStream(dst+"-wal");
                    copyFile(inputStream,outputStream);
                } catch (IOException e) {
                    result[0] = false;
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getAssets().open(filename);
                    OutputStream outputStream = new FileOutputStream(dst);
                    copyFile(inputStream,outputStream);
                } catch (IOException e) {
                    result[0] = false;
                }
            }
        }).start();
        return result[0];
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}