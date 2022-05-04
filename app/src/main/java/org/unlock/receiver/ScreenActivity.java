package org.unlock.receiver;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.arbelkilani.clock.Clock;

public class ScreenActivity extends AppCompatActivity {
    View overlay;
    CardView btn1, btn2, btn3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overlay = LayoutInflater.from(this).inflate(R.layout.floatxml, null);


        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}


        setContentView(overlay);



        //floatText = mFloatingView.findViewById(R.id.floatTxt);
        Clock clock = overlay.findViewById(R.id.clock);
        //clock.setClockBackground(R.drawable.background_1);
        clock.setShowSecondsNeedle(true);
        clock.setShowHoursValues(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean useWallpaper = false;
        useWallpaper = sp.getBoolean("use_wallpaper", false);

        if (useWallpaper) {

            if (ActivityCompat.checkSelfPermission(MainActivity.act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //requestPermission();
                //                                        int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    ActivityCompat.requestPermissions(MainActivity.act, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                }else{
                    Toast.makeText(this,"File permission not provided",Toast.LENGTH_SHORT).show();
                }

                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

                final Drawable wallpaperDrawable = wallpaperManager.getBuiltInDrawable();

                ImageView img = overlay.findViewById(R.id.background_img);
                img.setImageDrawable(wallpaperDrawable);
            }else{

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

        btn1 = overlay.findViewById(R.id.btn1);
        btn2 = overlay.findViewById(R.id.btn2);
        btn3 = overlay.findViewById(R.id.btn3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        //MainActivity.act.finish();
    }
}
