package org.unlock.receiver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.dd.CircularProgressButton;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference color_picker = findPreference("color_picker");
        color_picker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                int color = sharedPreferences.getInt("color_picker", Color.RED);
                ColorPickerDialog.newBuilder().setDialogId(1).setColor(color).show(getActivity());
                return false;
            }
        });

        Preference color_picker_clock = findPreference("color_picker_clock");
        color_picker_clock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                int color = sharedPreferences.getInt("color_picker_clock", Color.BLACK);
                ColorPickerDialog.newBuilder().setDialogId(2).setColor(color).show(getActivity());
                return false;
            }
        });



        Preference start = findPreference("on");
        start.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean on = sharedPreferences.getBoolean("on", false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getActivity())) {

                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getActivity().getPackageName()));
                        startActivityForResult(intent, 973);
                        Toast.makeText(getActivity(), "Enable Overlay Permission First!", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
                if (on) {
                    if (!isMyServiceRunning(ScreenListenerService.class)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getActivity().startForegroundService(new Intent(getActivity(), ScreenListenerService.class));
                        } else {
                            Intent intent = new Intent(getActivity(), ScreenListenerService.class);
                            getActivity().startService(intent);
                        }
                    }
                } else {
                    if (isMyServiceRunning(ScreenListenerService.class)) {
                        getActivity().stopService(new Intent(getActivity(), ScreenListenerService.class));
                    }
                }
                return false;
            }
        });

        Preference useWall = findPreference("use_wallpaper");
        useWall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                boolean on = sharedPreferences.getBoolean("use_wallpaper", false);
                if (on) {
                    if (!isMyServiceRunning(ScreenListenerService.class)) {

                        if (ActivityCompat.checkSelfPermission(MainActivity.act, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                                ActivityCompat.requestPermissions(MainActivity.act, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                            } else {
                                Toast.makeText(getActivity(), "Accept permissions manually", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                return false;
            }
        });

        Preference enableOverlay = findPreference("toggle_overlay");
        enableOverlay.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean on = sharedPreferences.getBoolean("toggle_overlay", false);
                if (on) {
                    // check if we already  have permission to draw over other apps
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(getActivity())) {

                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getActivity().getPackageName()));
                            startActivityForResult(intent, 973);
                        }
                    }
                } else {

                }
                return false;
            }
        });

        CircularProgressButton run = new CircularProgressButton(getActivity());

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, 321);
        }
    }
}
