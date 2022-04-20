package com.legendsayantan.aireply;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil extends MainActivity {
    private static int BUFFER_SIZE = 1024;
    public static float progress = 0;
    static long i = 0,j = 0;


    private static void createNewFile(String path) {
        int lastSep = path.lastIndexOf(File.separator);
        if (lastSep > 0) {
            String dirPath = path.substring(0, lastSep);
            makeDir(dirPath);
        }
        File file = new File(path);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void copyFile(String sourcePath, String destPath,ProgressBar progressbar) {
        progress = 0;
        if (!isExistFile(sourcePath)) return;
        new File(new File(destPath).getParent()).mkdirs();
        createNewFile(destPath);

        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destPath, false);

            byte[] buff = new byte[1024];
            int length = 0;

            while ((length = fis.read(buff)) > 0) {
                fos.write(buff, 0, length);
                i = i + length;
                progressbar.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public static void copyFile(String sourcePath, String destPath) {
        progress = 0;
        if (!isExistFile(sourcePath)) return;
        new File(new File(destPath).getParent()).mkdirs();
        createNewFile(destPath);

        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destPath, false);

            byte[] buff = new byte[1024];
            int length = 0;

            while ((length = fis.read(buff)) > 0) {
                fos.write(buff, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void moveFile(String sourcePath, String destPath) {
        copyFile(sourcePath, destPath);
        deleteFile(sourcePath, true);
    }
    public static void moveFile(String sourcePath, String destPath,ProgressBar progressBar) {
        copyFile(sourcePath,destPath,progressBar);
        deleteFile(sourcePath, true);

    }

    public static void deleteFile(String path, boolean bool) {
        if (bool) {
            File file = new File(path);
            if (!file.exists())
                if (file.isFile()) {
                    file.delete();
                }

            File[] fileArr = file.listFiles();
            if (fileArr != null) {
                for (File subFile : fileArr) {
                    if (subFile.isDirectory()) {
                        deleteFile(subFile.getAbsolutePath(), true);
                    }
                    if (subFile.isFile()) {
                        subFile.delete();
                    }
                }
            }

            file.delete();
        }
    }

    public static boolean isExistFile(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void makeDir(String path) {
        if (!isExistFile(path)) {
            File file = new File(path);
            file.mkdirs();
        }
    }

    public static void listDir(String path, ArrayList<String> list) {
        File dir = new File(path);
        if (!dir.exists() || dir.isFile()) return;

        File[] listFiles = dir.listFiles();
        if (listFiles == null || listFiles.length <= 0) return;

        if (list == null) return;
        list.clear();
        for (File file : listFiles) {
            list.add(file.getAbsolutePath());
        }
    }

    public static boolean isDirectory(String path) {
        if (!isExistFile(path)) return false;
        return new File(path).isDirectory();
    }

    public static boolean isFile(String path) {
        if (!isExistFile(path)) return false;
        return new File(path).isFile();
    }

    public static long getFileLength(String path) {
        if (!isExistFile(path)) return 0;
        return new File(path).length();
    }
    public static void installapk(File apkFile, Context context) {
        if (apkFile.exists()) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (SDK_INT >= Build.VERSION_CODES.N) {
                install.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
                Uri apkUri =
                        FileProvider.getUriForFile(context, context.getPackageName()+".fileProvider", apkFile);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                context.startActivity(install);

            } else {
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
                context.startActivity(install);
            }
        } else {
            System.out.println("apk doesnot exist.");
        }
    }


    public void downloadFile(String url, String filepath, String title, String desc, Context context) {
        if (new File(filepath).exists()) {
            Toast.makeText(context, "This file is already downloaded.", Toast.LENGTH_LONG).show();
        } else {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            // in order for this if to run, you must use the android 3.2 to compile your app
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle(title);
            request.setDescription(desc);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filepath);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            manager.enqueue(request);
            final BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    installapk(new File(filepath), context);

                }
            };
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            Toast.makeText(context, "Download Started,check downloads when completed.", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean zip(String sourcePath, String toLocation,ProgressBar progressBar) {
        final int BUFFER = 1024;

        progress = 0;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            boolean f = new File("storage/emulated/0/Android/Backify").mkdirs();
            createNewFile(toLocation);
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length(),progressBar);
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                     i = i + data.length;
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress((int) progress);
                        }
                    });
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            deleteFile(toLocation, true);
            return false;
        }
        return true;
    }

    private static void zipSubFolder(ZipOutputStream out, File folder,
                                     int basePathLength, ProgressBar progressBar) throws IOException {
        File[] fileList = folder.listFiles();


        final int BUFFER = 2048;

        BufferedInputStream origin = null;
        if(fileList!=null)
        for (File file : fileList) {

            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength, progressBar);

            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                     i = i + data.length;

                    progressBar.setProgress((int) progress);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }


    public static boolean unzip(File zipFile, File targetDirectory) {
        progress = 0;
        try {
            targetDirectory.mkdirs();
            ZipInputStream zis = null;
            zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(zipFile)));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                            i = i + 512;

                        }
                    } finally {
                        fout.close();
                    }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
                }
            } finally {
                zis.close();
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean unzipNltk(InputStream zipFile, File targetDirectory) {
        progress = 0;
        try {
            targetDirectory.mkdirs();
            ZipInputStream zis = null;
            zis = new ZipInputStream(
                    new BufferedInputStream(zipFile));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                            i = i + 512;

                        }
                    } finally {
                        fout.close();
                    }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
                }
            } finally {
                zis.close();
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static long getDirectorySize(File dir) {
        long length = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getDirectorySize(file);
            }
        }
        return length;
    }


    public static void listfiles(String directoryName, ArrayList<String> files) {
        File directory = new File(directoryName);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    listfiles(file.getAbsolutePath(), files);
                }
            }
    }




    public static String unitFromBytes(long bytes) {
        String unit = "bytes";
        float data;
        if (bytes > 1024) {
            bytes = bytes / 1024;
            unit = "KB";
            if (bytes > 1024) {
                bytes = bytes / 1024;
                unit = "MB";
                if (bytes > 1024) {
                    data = (float) bytes / 1024;
                    unit = "GB";
                    return data + " " + unit;
                } else return bytes + " " + unit;
            } else return bytes + " " + unit;
        } else return bytes + " " + unit;
    }

    public static void shareFile(String path, Activity This) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        if(path.endsWith(".apks"))
            sendIntent.setType(URLConnection.guessContentTypeFromName(new File(path.replace(".apks",".zip")).getName()));
        else
            sendIntent.setType(URLConnection.guessContentTypeFromName(new File(path).getName()));
        This.startActivity(sendIntent);
    }

    public static File getApk(ResolveInfo resolveInfo) {
        File file = new File(resolveInfo.activityInfo.applicationInfo.publicSourceDir);
        return file;
    }

    public void createDir(String path, Uri treeUri) {
        if (treeUri == null) {
            return;
        }
        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(getApplicationContext(), treeUri);
        document.createDirectory(path);
    }

}

