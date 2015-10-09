package samuelpalmer.floatingrotation.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.floatingrotation.R;
import samuelpalmer.floatingrotation.service.FloatingButton.Listener;

public class MainService extends Service {

	private static final String TOGGLE = "TOGGLE";

	private RotationState rotationState;
	private FloatingButton floatingButton;
	private PersistedState state;

	private final RotationState.Listener rotationChanged = new RotationState.Listener() {
		@Override
		public void rotationChanged() {
			setForegroundNotification();
			floatingButton.setImage(buttonImage());
		}
	};

	private final Listener buttonClickListener = new Listener() {
		@Override
		public void clicked() {
			rotationState.toggle();
		}
	};

	private int buttonImage() {
		return rotationState.isPortrait() ? R.mipmap.ic_portrait_button : R.mipmap.ic_landscape_button;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

		rotationState = new RotationState(this, rotationChanged);
		state = new PersistedState(this);

		if (state.getButtonVisibility())
			showButton();

		setForegroundNotification();
	}

	private void showButton() {
		floatingButton = new FloatingButton(this, buttonClickListener, buttonImage(), state);
	}

	private void hideButton() {
		floatingButton.destroy();
		floatingButton = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (floatingButton != null) {
			hideButton();
		}

		if (rotationState != null) {
			rotationState.destroy();
			rotationState = null;
		}
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
			.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null && intent.getAction().equals(TOGGLE)) {
			boolean becomingVisible = floatingButton == null;
			state.setButtonVisibility(becomingVisible);

			if (becomingVisible)
				showButton();
			else
				hideButton();
		}

		return Service.START_STICKY;
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
