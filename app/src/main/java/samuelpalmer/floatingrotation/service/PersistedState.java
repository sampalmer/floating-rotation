package samuelpalmer.floatingrotation.service;

import android.content.Context;
import android.content.SharedPreferences;

class PersistedState {

	private static final String BUTTON_VISIBILITY = "button_visibility";
	public static final String BUTTON_X_POSITION_PERCENTAGE = "button_x_position_percentage";
	protected static final String BUTTON_Y_POSITION_PERCENTAGE = "button_y_position_percentage";

	private final SharedPreferences preferences;

	public PersistedState(Context context) {
		preferences = context.getSharedPreferences("service", Context.MODE_PRIVATE);
	}

	public boolean getButtonVisibility() {
		return preferences.getBoolean(BUTTON_VISIBILITY, true);
	}

	public void setButtonVisibility(boolean value) {
		preferences.edit().putBoolean(BUTTON_VISIBILITY, value).apply();
	}

	public float getButtonXPositionPercentage() {
		return preferences.getFloat(BUTTON_X_POSITION_PERCENTAGE, .5f);
	}

	public float getButtonYPositionPercentage() {
		return preferences.getFloat(BUTTON_Y_POSITION_PERCENTAGE, .5f);
	}

	public void setButtonPositionPercentages(float x, float y) {
		preferences.edit()
			.putFloat(BUTTON_X_POSITION_PERCENTAGE, x)
			.putFloat(BUTTON_Y_POSITION_PERCENTAGE, y)
		.apply();
	}

}
