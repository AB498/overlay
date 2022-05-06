package org.unlock.receiver;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Objects;

public class ScreenListenerService extends Service {

    private static final String ACTION_STOP_LISTEN = "action_stop_listen";

    BroadcastReceiver screenReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_LISTEN.equals(intent.getAction())) {
            stopForeground(true);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor ed = sp.edit();

            ed.putBoolean("on", false);

            stopSelf();
            return START_NOT_STICKY;
        }

        startRunningInForeground();
        registerBroadcastReceivers();
        return START_STICKY;
    }

    private void startRunningInForeground() {

        //if more than or equal to 26
        if (Build.VERSION.SDK_INT >= 26) {

            //if more than 26
            if (Build.VERSION.SDK_INT > 26) {
                String CHANNEL_ONE_ID = "sensor.inspect.screenservice";
                String CHANNEL_ONE_NAME = "Screen service";
                NotificationChannel notificationChannel = null;
                notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                        CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.createNotificationChannel(notificationChannel);
                }

                Intent stopSelf = new Intent(this, ScreenListenerService.class);
                stopSelf.setAction(ACTION_STOP_LISTEN);
                PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, 0);

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setChannelId(CHANNEL_ONE_ID)
                        .setContentTitle("Running")
                        .setContentText("Unlock handler is running")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setLargeIcon(icon)
                        .addAction(R.drawable.ic, "Stop", pStopSelf)
                        .build();

                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                startForeground(101, notification);
            }
            //if version 26
            else {
                startForeground(101, updateNotification());

            }
        }
        //if less than version 26
        else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Running")
                    .setContentText("Unlock handler is running")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setOngoing(true).build();

            startForeground(101, notification);
        }
    }

    private Notification updateNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        return new NotificationCompat.Builder(this)
                .setContentTitle("Activity log")
                .setTicker("Ticker")
                .setContentText("recording of data is on going")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
    }

    private void registerBroadcastReceivers() {
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case Intent.ACTION_SCREEN_ON:
  ///                      Toast.makeText(getApplicationContext(), "on", Toast.LENGTH_SHORT).show();

                        break;
                    case Intent.ACTION_SCREEN_OFF:

                        Intent i = new Intent(getApplicationContext(), OverlayScreen.class);
                        startService(i);

                        break;
                }
            }
        };

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(screenReceiver, screenFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenReceiver);
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
