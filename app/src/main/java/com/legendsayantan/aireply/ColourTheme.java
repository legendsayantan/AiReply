package com.legendsayantan.aireply;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

public class ColourTheme {
    private static Drawable drawable;
    private static int lightColor;
    private static int darkColor;
    private static int dominantColor;
    private static int vibrantColor;
    private static boolean nightUi;
    private static Activity activity;

    public ColourTheme(Activity activity) {
        Drawable drawable1 = null;
        ColourTheme.activity = activity;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            drawable1 = wallpaperManager.peekDrawable();
            if (drawable1 == null) {
                System.out.println("Null returned at peekDrawable");
                drawable1 = wallpaperManager.getDrawable();
            }
        }else{
            System.out.println("Theme error due to no permission");
        }
        drawable = drawable1;
        Bitmap iconBitmap = ((BitmapDrawable) drawable).getBitmap();
        Palette iconPalette = Palette.from(iconBitmap).maximumColorCount(16).generate();
        lightColor = iconPalette.getLightVibrantColor(
                ColorUtils.blendARGB(
                        iconPalette.getDominantColor(iconPalette.getVibrantColor(0x000000)),
                        activity.getResources().getColor(R.color.softwhite),
                        0.5F));
        darkColor = iconPalette.getDarkVibrantColor(
                ColorUtils.blendARGB(
                        lightColor,
                        activity.getResources().getColor(R.color.softblack),
                        0.5F));
        System.out.println("Colour distance "+getDistance(lightColor,darkColor));
        if(getDistance(lightColor,darkColor)<=10.0){
            lightColor = ColorUtils.blendARGB(lightColor,activity.getResources().getColor(R.color.softwhite),0.5F);
        }
        dominantColor = iconPalette.getDominantColor(iconPalette.getVibrantColor(0x000000));
        if(getDistance(dominantColor,iconPalette.getVibrantColor(0x000000))<=10.0){
            dominantColor = ColorUtils.blendARGB(darkColor,activity.getResources().getColor(R.color.softblack),0.5F);
        }
        int uiFlags =
                activity.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        nightUi = uiFlags == Configuration.UI_MODE_NIGHT_YES;
        vibrantColor = iconPalette.getVibrantColor(
                ColorUtils.blendARGB(
                        vibrantColor,
                        nightUi?
                                iconPalette.getVibrantColor(activity.getResources().getColor(R.color.softblack)):
                                iconPalette.getVibrantColor(activity.getResources().getColor(R.color.softwhite)),
                        0.5F));
    }

    public static Drawable getDrawable() {
        return drawable;
    }

    public static int getLightColor() {
        return lightColor;
    }

    public static int getDarkColor() {
        return darkColor;
    }

    public static int getDominantColor() {
        return dominantColor;
    }

    public static int getVibrantColor() {
        return vibrantColor;
    }

    public static void setActivity(Activity activity) {
        ColourTheme.activity = activity;
    }

    public static void initBackground(View view){
        if(nightUi){
            view.setBackgroundColor(darkColor);
        }else{
            view.setBackgroundColor(lightColor);
        }
    }
    public static void initText(TextView view){
        if(nightUi){
            view.setTextColor(lightColor);
        }else{
            view.setText(darkColor);
        }
    }
    public static void initTextView(TextView view){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(nightUi){
                    view.setBackgroundColor(darkColor);
                    view.setTextColor(lightColor);
                }else{
                    view.setBackgroundColor(lightColor);
                    view.setTextColor(darkColor);
                }
            }
        });
    }
    public static void initImageView(ImageView view){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(nightUi){
                    view.setBackgroundColor(darkColor);
                    view.setColorFilter(lightColor);
                }else{
                    view.setBackgroundColor(lightColor);
                    view.setColorFilter(darkColor);
                }
            }
        });
    }
    public static void initContainer(View parent){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent.setBackground(drawable);
            }
        });
    }
    private float getHue(int color) {
        int R = (color >> 16) & 0xff;
        int G = (color >>  8) & 0xff;
        int B = (color      ) & 0xff;
        float[] colorHue = new float[3];
        ColorUtils.RGBToHSL(R, G, B, colorHue);
        return colorHue[0];
    }
    private float getDistance(int color1, int color2) {
        float avgHue = (getHue(color1) + getHue(color2))/2;
        return Math.abs(getHue(color1) - avgHue);
    }
}
