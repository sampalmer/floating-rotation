package samuelpalmer.floatingrotation;

import android.content.Context;

import samuelpalmer.common.contentprovidersharedpreferences.ContentProviderSharedPreferences;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.Settings;

public class ApplicationState extends Settings {

	public ApplicationState(Context context) {
		super(new ContentProviderSharedPreferences(context, context.getString(R.string.content_authority), "application"));
	}

	public final Setting<Boolean> enabled = new Setting<>(new BooleanSetting(), "enabled", false);

}
