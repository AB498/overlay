package org.unlock.receiver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference myPref = findPreference("color_picker");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                int color = sharedPreferences.getInt("color_picker", Color.RED);
                ColorPickerDialog.newBuilder().setColor(color).show(getActivity());
                return false;
            }
        });

        Preference start = findPreference("on");
        start.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean on = sharedPreferences.getBoolean("on", false);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.v("App", "Build Version Greater than or equal to M: " + Build.VERSION_CODES.M);
                        checkDrawOverlayPermission();
                    } else {
                    }
                }
                return false;
            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, 321);
        }
    }
}
