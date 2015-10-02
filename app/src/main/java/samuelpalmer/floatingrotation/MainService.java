package samuelpalmer.floatingrotation;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.floatingrotation.FloatingButton.Listener;

public class MainService extends Service {

	private RotationState rotationState;
	private FloatingButton floatingButton;

	private RotationState.Listener rotationChanged = new RotationState.Listener() {
		@Override
		public void rotationChanged() {
			floatingButton.setIsPortrait(rotationState.isPortrait());
			setForegroundNotification();
		}
	};

	private void handleStart(){
		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

		rotationState = new RotationState(this, rotationChanged);
		floatingButton = new FloatingButton(this, rotationState.isPortrait(), new Listener() {
			@Override
			public void clicked() {
				rotationState.toggle();
			}
		});

		setForegroundNotification();
	}

	private void setForegroundNotification() {
		startForeground(1, new NotificationCompat.Builder(this)
			.setSmallIcon(rotationState.isPortrait() ? R.drawable.ic_notification_portrait : R.drawable.ic_notification_landscape)
			.setContentTitle(getResources().getString(R.string.app_name))
			.setContentText("Touch to toggle floating button")
			.setContentIntent(PendingIntent.getService(this, 2, new Intent(this, MainService.class).setAction("TOGGLE"), PendingIntent.FLAG_UPDATE_CURRENT))
			.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(startId == Service.START_STICKY) {
			handleStart();
			return super.onStartCommand(intent, flags, startId);
		}else{
			if (intent.getAction() != null && intent.getAction().equals("TOGGLE"))
				floatingButton.toggleVisibility();

			return Service.START_NOT_STICKY;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (floatingButton != null) {
			floatingButton.destroy();
			floatingButton = null;
		}

		if (rotationState != null)
			rotationState = null;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
