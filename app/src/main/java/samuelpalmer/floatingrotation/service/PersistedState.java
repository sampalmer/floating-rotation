package samuelpalmer.floatingrotation.service;

import android.content.Context;

import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.FloatSetting;
import samuelpalmer.common.settings.application.Settings;

class PersistedState extends Settings {

	public PersistedState(Context context) {
		super(context.getSharedPreferences("service", Context.MODE_PRIVATE));
	}

	public Setting<Boolean> buttonVisibility() {
		return new Setting<>(new BooleanSetting(), "button_visibility", true);
	}

	public Setting<Float> buttonXPositionPercentage() {
		return new Setting<>(new FloatSetting(), "button_x_position_percentage", .5f);
	}

	public Setting<Float> buttonYPositionPercentage() {
		return new Setting<>(new FloatSetting(), "button_y_position_percentage", .5f);
	}

	public Setting<Boolean> originalSystemAutoRotation() {
		return new Setting<>(new BooleanSetting(), "original_system_auto_rotation", null);
	}

}
