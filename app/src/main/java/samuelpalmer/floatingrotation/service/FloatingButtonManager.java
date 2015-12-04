package samuelpalmer.floatingrotation.service;

import android.content.Context;

import samuelpalmer.floatingrotation.service.FloatingButton.Listener;

public class FloatingButtonManager {

	private final PersistedState state;
	private final Listener buttonClickListener;
	private final Context context;
	private int imageResource;
	private FloatingButton floatingButton;

	public FloatingButtonManager(Context context, int imageResource, Listener buttonClickListener, PersistedState state) {
		this.context = context;
		this.state = state;
		this.imageResource = imageResource;
		this.buttonClickListener = buttonClickListener;

		if (this.state.buttonVisibility().get())
			showButton();
	}

	public void destroy() {
		if (isVisible())
			hideButton();
	}

	public void toggleVisibility() {
		boolean becomingVisible = !isVisible();

		if (becomingVisible)
			showButton();
		else
			hideButton();

		state.buttonVisibility().set(isVisible());
	}

	public void setImage(int imageResource) {
		this.imageResource = imageResource;

		if (floatingButton != null)
			floatingButton.setImage(imageResource);
	}

	public boolean isVisible() {
		return floatingButton != null;
	}

	private void showButton() {
		floatingButton = new FloatingButton(context, buttonClickListener, imageResource, state);
	}

	private void hideButton() {
		floatingButton.destroy();
		floatingButton = null;
	}
}
