package org.unlock.receiver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorPickerView;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference myPref=findPreference("color_picker");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                int color=sharedPreferences.getInt("color_picker",Color.RED);

                ColorPickerDialog.newBuilder().setColor(color).show(getActivity());

                return false;
            }
        });


        Preference start = findPreference("on");

        start.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                boolean on=sharedPreferences.getBoolean("on",false);
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
}
