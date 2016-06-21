package samuelpalmer.floatingrotation.service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.notifications.NotificationButtons;
import samuelpalmer.common.notifications.NotificationStyles;
import samuelpalmer.floatingrotation.MainActivity;
import samuelpalmer.floatingrotation.R;

public class MainService extends Service {

	private static final String TOGGLE_BUTTON_VISIBILITY = "TOGGLE_BUTTON_VISIBILITY";
	private static final String TOGGLE_SCREEN_ROTATION = "TOGGLE_SCREEN_ROTATION";

	private RotationState rotationState;
	private FloatingButtonManager buttonManager;
	private LockScreenMonitor lockScreenMonitor;
	private PersistedState state;
	private Integer actionButtonColour;

	@Override
	public void onCreate() {
		super.onCreate();

		state = new PersistedState(this);

		Boolean originalAutoRotation = state.originalSystemAutoRotation().tryGet();
		if (originalAutoRotation == null) { //If the app previously crashed, we might still know the value
			boolean value = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
			state.originalSystemAutoRotation().set(value);
		}

		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

		rotationState = new RotationState(this, rotationChanged);
		buttonManager = new FloatingButtonManager(this, buttonImage(), buttonClickListener, state);
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

		boolean originalAutoRotation = state.originalSystemAutoRotation().get();
		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, originalAutoRotation ? 1 : 0);
		state.originalSystemAutoRotation().remove();
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

		@Override
		public void longPressed() {
			toggleButtonVisibility();

			//I want to make sure the user realises what's happened
			AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
				Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(33);
			}
			else
				Toast.makeText(MainService.this, "Button hidden", Toast.LENGTH_SHORT).show();
		}
	};

	private void toggleButtonVisibility() {
		buttonManager.toggleVisibility();
		setForegroundNotification();
	}

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
		int color = ContextCompat.getColor(this, R.color.colorPrimary);

		NotificationBuilderCompat builder = NotificationBuilderCompat.create(this)
				.setSmallIcon(rotationState.isPortrait() ? R.drawable.ic_notification_portrait : R.drawable.ic_notification_landscape)
				.setContentTitle("Rotation")
				.setContentIntent(PendingIntent.getActivity(this, 2, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
				.setColor(color)
				.setWhen(0) //For devices that don't support 'show when'
				.setCategory(NotificationCompat.CATEGORY_STATUS)
				.setVisibility(NotificationCompat.VISIBILITY_SECRET)
				.setPriority(lockScreenMonitor.isOnLockScreen() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_DEFAULT);

		if (actionButtonColour == null) {
			NotificationStyles styles = new NotificationStyles(this, R.drawable.ic_action_toggle_rotation, color);
			actionButtonColour = styles.getActionButtonColour();
		}

		RemoteViews contentView = new NotificationButtons(this, R.layout.notification, builder.createContentView(), actionButtonColour)
				.configureButton(
						R.id.toggle_button_visibility,
						buttonManager.isVisible() ? R.drawable.ic_action_visible : R.drawable.ic_action_invisible,
						PendingIntent.getService(this, 2, new Intent(this, MainService.class).setAction(TOGGLE_BUTTON_VISIBILITY), PendingIntent.FLAG_UPDATE_CURRENT),
						"Toggle button visibility"
				)
				.configureButton(
						R.id.toggle_rotation,
						R.drawable.ic_action_toggle_rotation,
						PendingIntent.getService(this, 2, new Intent(this, MainService.class).setAction(TOGGLE_SCREEN_ROTATION), PendingIntent.FLAG_UPDATE_CURRENT),
						"Toggle screen rotation"
				)
				.getContentView();

		builder.setCustomContentView(contentView);

		Notification notification = builder.build();

		startForeground(1, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			switch (intent.getAction()) {
				case TOGGLE_BUTTON_VISIBILITY:
					toggleButtonVisibility();
					break;
				case TOGGLE_SCREEN_ROTATION:
					rotationState.toggle();
					break;
				default:
					throw new RuntimeException("Unknown action: " + intent.getAction());
			}

		return Service.START_STICKY;
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
