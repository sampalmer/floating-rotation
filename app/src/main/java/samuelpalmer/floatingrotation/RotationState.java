package samuelpalmer.floatingrotation;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.Surface;
import android.view.WindowManager;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.NaturalOrientationDetector;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.Screen;

public class RotationState {

	private final ContentResolver contentResolver;
	private final NaturalOrientationDetector naturalOrientation;
	private final Listener listener;

	public RotationState(Context context, Listener listener) {
		this.listener = listener;
		contentResolver = context.getContentResolver();
		naturalOrientation = new NaturalOrientationDetector(new Screen((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)));
	}

	public void toggle(){
		Settings.System.putInt(contentResolver, Settings.System.USER_ROTATION, getNextRotation());
		listener.rotationChanged();
	}

	public boolean isPortrait() {
		return isNatural() == naturalOrientation.deviceIsNaturallyPortrait();
	}

	private Orientation currentRotation() {
		int raw = Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
		return new AndroidSurfaceRotation(raw).orientation();
	}

	private int getNextRotation() {
		if (isNatural())
			return Surface.ROTATION_90;
		else
			return Surface.ROTATION_0;
	}

	private boolean isNatural() {
		return currentRotation().isNaturalOrUpsideDown();
	}

	public interface Listener {
		void rotationChanged();
	}

}
