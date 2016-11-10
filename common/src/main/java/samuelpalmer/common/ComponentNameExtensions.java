package samuelpalmer.common;

import android.content.ComponentName;
import android.content.pm.PackageItemInfo;
import android.view.accessibility.AccessibilityEvent;

public final class ComponentNameExtensions {
	public static ComponentName makeFrom(PackageItemInfo packageItem) {
		return makeRelative(packageItem.packageName, packageItem.name);
	}

	public static ComponentName tryMakeFrom(AccessibilityEvent event) {
		if (event.getPackageName() != null && event.getClassName() != null)
			return makeRelative(
					event.getPackageName().toString(),
					event.getClassName().toString()
			);

		return null;
	}

	public static ComponentName makeRelative(String packageName, String className) {
		boolean isRelativeClassName = className.startsWith(".");
		String fullClassName = isRelativeClassName ? packageName + className : className;
		return new ComponentName(packageName, fullClassName);
	}
}
