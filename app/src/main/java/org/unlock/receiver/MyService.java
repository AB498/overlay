package org.unlock.receiver;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class MyService extends AccessibilityService {
    public static String lastPackage = "";
    public static String currPackage = "";
    static String log;
    static String lastLog, lastReq = "";
    static Activity ctx;
    static boolean screenOn = true;
    private static MyService mMS;
    int mDebugDepth = 0;
    boolean txtTaken = false;
    AccessibilityNodeInfo mNodeInfo;

    public static MyService getInstance() {
        return mMS;
    }

    public static void launchAct() {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onServiceConnected() {

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        mMS = this;

        AccessibilityServiceInfo info = getServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED | AccessibilityEvent.TYPE_WINDOWS_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        //info.packageNames = new String[]{"com.example.android.myFirstApp", "com.example.android.mySecondApp"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);

    }
public static void strt(){
    Intent fullScreenIntent = new Intent(mMS, ScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mMS.getApplicationContext().startActivity(fullScreenIntent);

}
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!screenOn) {

            Intent fullScreenIntent = new Intent(this, ScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(fullScreenIntent);
            //fullScreenIntent.putExtra(Constants.NOTIFICATION_IDS, notificationId);
            //return PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
lastPackage=(String) event.getPackageName();

        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //Log.d("Foreground App", event.getPackageName().toString());
            currPackage = (String) event.getPackageName();

            Toast.makeText(getApplicationContext(), event.getPackageName() + " " + lastPackage, Toast.LENGTH_LONG).show();
            if (!event.getPackageName().equals(lastPackage) && !event.getPackageName().equals("org.unlock.receiver")) {
                if (isMyServiceRunning(OverlayScreen.class))
                ;//stopService(new Intent(getApplicationContext(), OverlayScreen.class));

            }

        }
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