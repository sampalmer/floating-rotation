package samuelpalmer.floatingrotation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

public class PermissionManager {

    private final Context context;

    public PermissionManager(Context context) {
        this.context = context;
    }

    public boolean canDrawOverlays() {
        return !(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context));
    }

    public boolean canWriteSystemSettings() {
        return !(Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(context));
    }

    public void abortService() {
        Intent intent = new Intent(context, AcquirePermissionsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Ids.PendingIntent.REQUEST_PERMISSIONS, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.error)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Permissions needed. Touch to fix.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager().notify(Ids.Notification.PERMISSION_NEEDED, notification);

        ApplicationState applicationState = new ApplicationState(context);
        applicationState.enabled.set(false);
        new ServiceController(context, applicationState).updateServiceState();
    }

    public void cancelNotification() {
        notificationManager().cancel(Ids.Notification.PERMISSION_NEEDED);
    }

    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
