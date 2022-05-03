package org.unlock.receiver;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.arbelkilani.clock.Clock;

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
        //floatText = mFloatingView.findViewById(R.id.floatTxt);
        Clock clock = overlay.findViewById(R.id.clock);
        //clock.setClockBackground(R.drawable.background_1);
        clock.setShowSecondsNeedle(true);
        clock.setShowHoursValues(true);

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

            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //
                requestPermission();
                //                                        int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

            ImageView img = overlay.findViewById(R.id.background_img);
            img.setImageDrawable(wallpaperDrawable);
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

        setListeners();

    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                ((Activity) getApplicationContext()).startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                ((Activity) getApplicationContext()).startActivityForResult(intent, 2296);
            }
        } else {
            ActivityCompat.requestPermissions(((Activity) getApplicationContext()),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    public void setListeners() {

        overlay.findViewById(R.id.ll).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        return true;

                    case MotionEvent.ACTION_UP:

                        //Toast.makeText(getApplicationContext(),initialX+" "+params.x,Toast.LENGTH_SHORT).show();
                        try {
                            //wm.removeView(overlay);
                        } catch (Exception e) {
                        }
                        //onDestroy();
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        return true;
                }
                return false;
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
