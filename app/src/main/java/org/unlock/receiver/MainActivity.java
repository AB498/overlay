package org.unlock.receiver;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

    public final static int REQUEST_CODE = -1010101;
    static TextView tv;
    public static Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.act=this;

        if (getIntent().getStringExtra("crash") != null) {
            String mLog = getIntent().getStringExtra("crashInfo");
            ScrollView contentView = new ScrollView(this);
            contentView.setFillViewport(true);
            LinearLayout hw = new LinearLayout(this);
            TextView textView = new TextView(this);
            int padding = 10;
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(mLog);
            textView.setTextIsSelectable(true);
            textView.setTypeface(Typeface.MONOSPACE);
            hw.addView(textView);
            contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            setContentView(contentView);
            return;
        } else {
            Thread.setDefaultUncaughtExceptionHandler(
                    new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, Throwable throwable) {
                            String fullStackTrace;
                            StringWriter sw = new StringWriter();
                            throwable.printStackTrace(new PrintWriter(sw));
                            fullStackTrace = sw.toString();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("crashInfo", fullStackTrace);
                            intent.putExtra("crash", "true");
                            startActivity(intent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }
                    });
        }
        setContentView(R.layout.activity_main);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();

        boolean on = sp.getBoolean("on", false);

        if (on) {
            if (!isMyServiceRunning(ScreenListenerService.class)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(this, ScreenListenerService.class));
                } else {
                    Intent intent = new Intent(this, ScreenListenerService.class);
                    //startService(intent);

                    startService(intent);
                }
            }

        } else {
            if (isMyServiceRunning(ScreenListenerService.class)) {
                stopService(new Intent(this, ScreenListenerService.class));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.v("App", "Build Version Greater than or equal to M: " + Build.VERSION_CODES.M);
            checkDrawOverlayPermission();
        } else {
            Log.v("App", "OS Version Less than M");
            //No need for Permission as less then M OS.
        }

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_holder, new SettingsFragment()).commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        Log.v("App", "Package Name: " + getApplicationContext().getPackageName());

        // Check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Log.v("App", "Requesting Permission" + Settings.canDrawOverlays(getApplicationContext()));
            // if not construct intent to request permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            // request permission via start activity for result
            startActivityForResult(intent, REQUEST_CODE); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission
        } else {
            Log.v("App", "We already have permission for it.");
            // disablePullNotificationTouch();
            // Do your stuff, we got permission captain
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.edit().putInt("color_picker", color).commit();
        Toast.makeText(this, "" + color, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}