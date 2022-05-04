package org.unlock.receiver;

import android.Manifest;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

public class OverlayScreen extends Service {

    WindowManager wm;
    WindowManager.LayoutParams params;
    View overlay;
    CardView btn1, btn2, btn3;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ViewGroup wrapper = new FrameLayout(this) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    onDestroy();
                    return true;
                }

                return super.dispatchKeyEvent(event);
            }

        };

        overlay = LayoutInflater.from(this).inflate(R.layout.floatxml, wrapper);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    PixelFormat.TRANSLUCENT);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean useWallpaper = false;
        useWallpaper = sp.getBoolean("use_wallpaper", false);

        if (useWallpaper) {

            if (ActivityCompat.checkSelfPermission(MainActivity.act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    ActivityCompat.requestPermissions(MainActivity.act, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    Toast.makeText(this, "File permission not provided", Toast.LENGTH_SHORT).show();
                }
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                final Drawable wallpaperDrawable = wallpaperManager.getBuiltInDrawable();
                ImageView img = overlay.findViewById(R.id.background_img);
                img.setImageDrawable(wallpaperDrawable);
            } else {
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                ImageView img = overlay.findViewById(R.id.background_img);
                img.setImageDrawable(wallpaperDrawable);
            }
        } else {
            ImageView img = overlay.findViewById(R.id.background_img);
            int col = sp.getInt("color_picker", Color.RED);
            img.setBackgroundColor(col);
        }
        wm.addView(wrapper, params);

        btn1 = overlay.findViewById(R.id.btn1);
        btn2 = overlay.findViewById(R.id.btn2);
        btn3 = overlay.findViewById(R.id.btn3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                onDestroy();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                onDestroy();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                onDestroy();
            }
        });

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        try {
            wm.removeView(overlay);
        } catch (Exception e) {
        }

        stopSelf();
    }

}
