package samuelpalmer.floatingrotation;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import samuelpalmer.floatingrotation.service.MainService;

public class ServiceController {

	private final ApplicationState state;
	private final Context context;

	public ServiceController(Context context, ApplicationState state) {
		this.context = context;
		this.state = state;
	}

	public void updateServiceState() {
		if (state.isEnabled()) {
			if (context.startService(serviceIntent()) == null)
				throw new RuntimeException("Couldn't find service");
		}
		else {
			context.stopService(serviceIntent());
		}
	}

	@NonNull
	private Intent serviceIntent() {
		return new Intent(context, MainService.class);
	}

}
