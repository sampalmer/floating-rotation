package samuelpalmer.floatingrotation;

import android.content.Context;

import samuelpalmer.common.contentprovidersharedpreferences.ContentProviderSharedPreferences;

public class ApplicationState {

	private static final String ENABLED = "enabled";

	private final ContentProviderSharedPreferences preferences;

	public ApplicationState(Context context) {
		preferences = new ContentProviderSharedPreferences(context, context.getString(R.string.content_authority), "application");
	}

	public boolean isEnabled() {
		return preferences.getBoolean(ENABLED, false);
	}

	public void setEnabled(boolean enabled) {
		preferences.edit().putBoolean(ENABLED, enabled).apply();
	}

}
