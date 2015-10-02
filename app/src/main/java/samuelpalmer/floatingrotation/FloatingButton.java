package samuelpalmer.floatingrotation;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class FloatingButton {

	private final Context context;
	private final WindowManager windowManager;
	private final ImageView view;
	private final LayoutParams layout;
	private Point availableSpace = new Point();

	public FloatingButton(Context context, boolean isPortrait, final Listener listener) {
		this.context = context;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		view = new ImageView(context);
		setIsPortrait(isPortrait);

		this.layout = new WindowManager.LayoutParams(
			LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT,
			LayoutParams.TYPE_PHONE,
			LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
		this.layout.gravity = Gravity.TOP | Gravity.LEFT;
		this.layout.x = 0;
		this.layout.y = 100;

		updateWindowSize();

		windowManager.addView(view, this.layout);

		final GestureDetector gestureDetector = new GestureDetector(context, new OnGestureListener() {

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

				offset_x = x_cord - layout.x;
				offset_y = y_cord - layout.y;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				listener.clicked();
				return true;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				int x_cord = (int) e2.getRawX();
				int y_cord = (int) e2.getRawY();

				layout.x = x_cord - offset_x;
				layout.y = y_cord - offset_y;
				wrap();

				windowManager.updateViewLayout(view, layout);

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

		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		context.registerComponentCallbacks(configChangedCallback);
	}

	public void destroy() {
		context.unregisterComponentCallbacks(configChangedCallback);
		windowManager.removeView(view);
	}

	public void setIsPortrait(boolean isPortrait) {
		view.setImageResource(isPortrait ? R.mipmap.ic_portrait_button : R.mipmap.ic_landscape_button);
	}

	public void toggleVisibility() {
		view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
	}

	private final ComponentCallbacks configChangedCallback = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			environmentChanged();
		}

		@Override
		public void onLowMemory() {
		}
	};

	private void wrap() {
		layout.x = Math.min(Math.max(layout.x, 0), maxX());
		layout.y = Math.min(Math.max(layout.y, 0), maxY());
	}

	private int maxX() {
		return availableSpace.x - 1 - view.getWidth();
	}

	private int maxY() {
		return availableSpace.y - 1 - view.getHeight();
	}

	private void updateWindowSize() {
		windowManager.getDefaultDisplay().getSize(availableSpace);
	}

	private void environmentChanged() {
		double xProportion = (double) layout.x / maxX();
		double yProportion = (double) layout.y / maxY();

		updateWindowSize();

		layout.x = (int)Math.round(xProportion * maxX());
		layout.y = (int)Math.round(yProportion * maxY());

		windowManager.updateViewLayout(view, layout);
	}

	public interface Listener {
		void clicked();
	}

}
