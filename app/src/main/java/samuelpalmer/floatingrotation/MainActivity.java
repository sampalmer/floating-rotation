package samuelpalmer.floatingrotation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;

import samuelpalmer.common.eventhandling.EventHandler;

public class MainActivity extends AppCompatActivity {

	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			updateEnabledSwitch();
		}
	};

	private ApplicationState state;
	private ServiceController serviceController;
	private PermissionManager permissionManager;
	private Switch enabledSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		permissionManager = new PermissionManager(this);
		state = new ApplicationState(this);
		serviceController = new ServiceController(this, state);

		enabledSwitch = (Switch) findViewById(R.id.enabled);

		enabledSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!enabledSwitch.isChecked() || hasRequiredPermissions()) {
					setServiceEnabled(enabledSwitch.isChecked());
				} else {
					enabledSwitch.setChecked(false);
					startActivity(new Intent(MainActivity.this, AcquirePermissionsActivity.class));
				}
			}
		});
	}

	private void updateEnabledSwitch() {
		enabledSwitch.setChecked(state.enabled.get());
	}

	@Override
	protected void onStart() {
		super.onStart();

		updateEnabledSwitch();
		state.enabled.subscribe(subscriber);
	}

	@Override
	protected void onStop() {
		super.onStop();

		state.enabled.unsubscribe(subscriber);
	}

	@Override
	protected void onResume() {
		super.onResume();

		serviceController.updateServiceState();
	}

	private boolean hasRequiredPermissions() {
		return permissionManager.canDrawOverlays() && permissionManager.canWriteSystemSettings();
	}

	private void setServiceEnabled(boolean enabled) {
		state.enabled.set(enabled);
		serviceController.updateServiceState();
	}
}
