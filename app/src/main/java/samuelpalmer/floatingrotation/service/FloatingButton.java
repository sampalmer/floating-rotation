package samuelpalmer.floatingrotation.service;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

class FloatingButton {

	private final Context context;
	private final PersistedState storage;
	private WindowManager windowManager;
	private final ImageView view;
	private LayoutParams layout;
	private Point availableSpace = new Point();
	private boolean initialised;

	public FloatingButton(final Context context, final Listener listener, int imageResource, PersistedState state) {
		this.context = context;
		this.storage = state;

		view = new ImageView(context) {
			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				if (oldw == 0 && oldh == 0 && !initialised) {
					updateWindowSize();
					setPosition(storage.getButtonXPositionPercentage(), storage.getButtonYPositionPercentage());

					final GestureDetector gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {

						private Long originalEventTime;
						private int original_x;
						private int original_y;

						@Override
						public boolean onSingleTapUp(MotionEvent e) {
							listener.clicked();
							return true;
						}

						@Override
						public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
							if (originalEventTime == null || e1.getEventTime() != originalEventTime) {
								original_x = layout.x;
								original_y = layout.y;
								originalEventTime = e1.getEventTime();
							}

							layout.x = original_x + Math.round(e2.getRawX() - e1.getRawX());
							layout.y = original_y + Math.round(e2.getRawY() - e1.getRawY());
							wrap();

							windowManager.updateViewLayout(view, layout);

							return true;
						}

					}) {
						@Override
						public boolean onTouchEvent(MotionEvent ev) {
							boolean handled = super.onTouchEvent(ev);
							if ((ev.getAction() & (MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL)) != 0 && !handled)
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

					initialised = true;
				}
			}
		};
		setImage(imageResource);

		layout = new WindowManager.LayoutParams(
			LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT,
			LayoutParams.TYPE_PHONE,
			LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
		layout.gravity = Gravity.TOP | Gravity.LEFT;
		layout.alpha = 0.5f;

		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		//Should happen first so the view gets dimensions
		windowManager.addView(view, layout);
	}

	public void destroy() {
		if (initialised) {
			context.unregisterComponentCallbacks(configChangedCallback);
			windowManager.removeView(view);
		}
	}

	public void setImage(int imageResource) {
		view.setImageResource(imageResource);
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
		windowManager.updateViewLayout(view, layout);
	}

	private void savePosition() {
		storage.setButtonPositionPercentages((float)xProportion(), (float)yProportion());
	}

	public interface Listener {
		void clicked();
	}

}
