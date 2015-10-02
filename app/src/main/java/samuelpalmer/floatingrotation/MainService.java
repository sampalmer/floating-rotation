package samuelpalmer.floatingrotation;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class MainService extends Service {
	private WindowManager windowManager;
	private ImageView chatheadView;
	private Point szWindow = new Point();
	private LayoutParams params;

	private void handleStart(){
		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatheadView = new ImageView(this);
		chatheadView.setImageResource(R.mipmap.ic_portrait_button);

		this.params = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT,
				LayoutParams.TYPE_PHONE,
				LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		this.params.gravity = Gravity.TOP | Gravity.LEFT;
		this.params.x = 0;
		this.params.y = 100;

		updateWindowSize();

		windowManager.addView(chatheadView, this.params);

		final GestureDetector gestureDetector = new GestureDetector(MainService.this, new OnGestureListener() {

			public int offset_x;
			public int offset_y;

			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				int x_cord = (int) e.getRawX();
				int y_cord = (int) e.getRawY();

				offset_x = x_cord - MainService.this.params.x;
				offset_y = y_cord - MainService.this.params.y;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				toggle();
				return true;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				int x_cord = (int) e2.getRawX();
				int y_cord = (int) e2.getRawY();

				MainService.this.params.x = x_cord - offset_x;
				MainService.this.params.y = y_cord - offset_y;

				wrap();

				windowManager.updateViewLayout(chatheadView, MainService.this.params);

				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}
		});
		gestureDetector.setIsLongpressEnabled(false);

		chatheadView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		startForeground(1, new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_notification_portrait)
			.setContentTitle(getResources().getString(R.string.app_name))
			.setContentText("Touch to toggle floating button")
			.setContentIntent(PendingIntent.getService(this, 2, new Intent(this, MainService.class).setAction("TOGGLE"), PendingIntent.FLAG_UPDATE_CURRENT))
			.build());
	}

	private void wrap() {
		params.x = Math.min(Math.max(params.x, 0), szWindow.x - 1 - chatheadView.getWidth());
		params.y = Math.min(Math.max(params.y, 0), szWindow.y - 1 - chatheadView.getHeight());
	}

	private void updateWindowSize() {
		if (Build.VERSION.SDK_INT >= 13) {
			windowManager.getDefaultDisplay().getSize(szWindow);
		} else {
			int w = windowManager.getDefaultDisplay().getWidth();
			int h = windowManager.getDefaultDisplay().getHeight();
			szWindow.set(w, h);
		}

		wrap();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		environmentChanged();
	}

	private void environmentChanged() {
		updateWindowSize();
		windowManager.updateViewLayout(chatheadView, params);
	}

	private void toggle(){
		int currentUserRotation = Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
		int desiredUserRotation = getNextRotation(currentUserRotation);

		Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, desiredUserRotation);
	}

	private int getNextRotation(int currentUserRotation) {
		if (currentUserRotation == Surface.ROTATION_0 || currentUserRotation == Surface.ROTATION_180)
			return Surface.ROTATION_90;
		else
			return Surface.ROTATION_0;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(startId == Service.START_STICKY) {
			handleStart();
			return super.onStartCommand(intent, flags, startId);
		}else{
			if (intent.getAction() != null && intent.getAction().equals("TOGGLE"))
				chatheadView.setVisibility(chatheadView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

			return Service.START_NOT_STICKY;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (chatheadView != null)
			windowManager.removeView(chatheadView);
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
}
