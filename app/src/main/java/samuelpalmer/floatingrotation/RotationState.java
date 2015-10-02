package samuelpalmer.floatingrotation;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.Surface;

public class RotationState {

	private final ContentResolver contentResolver;

	public RotationState(Context context) {
		contentResolver = context.getContentResolver();
	}

	public void toggle(){
		int currentUserRotation = Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
		int desiredUserRotation = getNextRotation(currentUserRotation);

		Settings.System.putInt(contentResolver, Settings.System.USER_ROTATION, desiredUserRotation);
	}

	private int getNextRotation(int currentUserRotation) {
		if (currentUserRotation == Surface.ROTATION_0 || currentUserRotation == Surface.ROTATION_180)
			return Surface.ROTATION_90;
		else
			return Surface.ROTATION_0;
	}

}
