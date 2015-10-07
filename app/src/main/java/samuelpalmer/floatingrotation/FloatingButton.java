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
	private final PersistedState storage;
	private final WindowManager windowManager;
	private final ImageView view;
	private final LayoutParams layout;
	private Point availableSpace = new Point();

	public FloatingButton(Context context, PersistedState storage, final Listener listener, int imageResource) {
		this.context = context;
		this.storage = storage;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		updateWindowSize();

		view = new ImageView(context);
		setImage(imageResource);
		setIsVisible(storage.getButtonVisibility());

		this.layout = new WindowManager.LayoutParams(
			LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT,
			LayoutParams.TYPE_PHONE,
			LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
		this.layout.gravity = Gravity.TOP | Gravity.LEFT;
		this.layout.alpha = 0.5f;
		setPosition(storage.getButtonXPositionPercentage(), storage.getButtonYPositionPercentage());

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
		}) {
			@Override
			public boolean onTouchEvent(MotionEvent ev) {
				boolean handled = super.onTouchEvent(ev);
				if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP && !handled)
					savePosition();

				return handled;
			}
		};
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

	public void setImage(int imageResource) {
		view.setImageResource(imageResource);
	}

	public void toggleVisibility() {
		setIsVisible(!isVisible());
	}

	private void setIsVisible(boolean visible) {
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
		storage.setButtonVisibility(visible);
	}

	private boolean isVisible() {
		return view.getVisibility() == View.VISIBLE;
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
		double xProportion = xProportion();
		double yProportion = yProportion();

		updateWindowSize();

		setPosition(xProportion, yProportion);

		windowManager.updateViewLayout(view, layout);
	}

	private double xProportion() {
		return (double) layout.x / maxX();
	}

	private double yProportion() {
		return (double) layout.y / maxY();
	}

	private void setPosition(double xProportion, double yProportion) {
		layout.x = (int)Math.round(xProportion * maxX());
		layout.y = (int)Math.round(yProportion * maxY());
	}

	private void savePosition() {
		storage.setButtonPositionPercentages((float)xProportion(), (float)yProportion());
	}

	public interface Listener {
		void clicked();
	}

}
