package samuelpalmer.floatingrotation.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.floatingrotation.R;

public class MainService extends Service {

	private static final String TOGGLE = "TOGGLE";

	private RotationState rotationState;
	private FloatingButtonManager buttonManager;
	private LockScreenMonitor lockScreenMonitor;

	@Override
	public void onCreate() {
		super.onCreate();

		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

		rotationState = new RotationState(this, rotationChanged);
		buttonManager = new FloatingButtonManager(this, buttonImage(), buttonClickListener);
		lockScreenMonitor = new LockScreenMonitor(this);

		lockScreenMonitor.subscribe(notificationUpdater);

		setForegroundNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		lockScreenMonitor.unsubscribe(notificationUpdater);
		lockScreenMonitor = null;

		buttonManager.destroy();
		buttonManager = null;

		rotationState.destroy();
		rotationState = null;
	}

	private final RotationState.Listener rotationChanged = new RotationState.Listener() {
		@Override
		public void rotationChanged() {
			setForegroundNotification();
			buttonManager.setImage(buttonImage());
		}
	};

	private final FloatingButton.Listener buttonClickListener = new FloatingButton.Listener() {
		@Override
		public void clicked() {
			rotationState.toggle();
		}
	};

	private final EventHandler notificationUpdater = new EventHandler() {
		@Override
		public void update() {
			setForegroundNotification();
		}
	};

	private int buttonImage() {
		return rotationState.isPortrait() ? R.mipmap.ic_portrait_button : R.mipmap.ic_landscape_button;
	}

	private void setForegroundNotification() {
		//noinspection deprecation
		startForeground(1, new NotificationCompat.Builder(this)
			.setSmallIcon(rotationState.isPortrait() ? R.drawable.ic_notification_portrait : R.drawable.ic_notification_landscape)
			.setContentTitle(getResources().getString(R.string.app_name))
			.setContentText("Touch to toggle floating button")
			.setContentIntent(PendingIntent.getService(this, 2, new Intent(this, MainService.class).setAction(TOGGLE), PendingIntent.FLAG_UPDATE_CURRENT))
			.setColor(getResources().getColor(R.color.colorPrimary))
			.setWhen(0) //For devices that don't support 'show when'
			.setCategory(NotificationCompat.CATEGORY_STATUS)
			.setVisibility(NotificationCompat.VISIBILITY_SECRET)
			.setPriority(lockScreenMonitor.isOnLockScreen() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_DEFAULT)
			.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null && intent.getAction().equals(TOGGLE))
			buttonManager.toggleVisibility();

		return Service.START_STICKY;
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
