package samuelpalmer.floatingrotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

	private ApplicationState state;
	private ServiceController serviceController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		state = new ApplicationState(this);
		serviceController = new ServiceController(this, state);

		Switch enabled = (Switch) findViewById(R.id.enabled);
		enabled.setChecked(state.isEnabled());
		enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setEnabled(isChecked);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		serviceController.updateServiceState();
	}

	private void setEnabled(boolean enabled) {
		state.setEnabled(enabled);
		serviceController.updateServiceState();
	}

}
