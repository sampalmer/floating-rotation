package samuelpalmer.floatingrotation.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.floatingrotation.ApplicationState;
import samuelpalmer.floatingrotation.ServiceController;

public class ServiceStateUpdatingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		new ServiceController(context, new ApplicationState(context)).updateServiceState();
	}

}
