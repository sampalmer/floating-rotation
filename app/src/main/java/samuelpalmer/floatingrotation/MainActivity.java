package samuelpalmer.floatingrotation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import samuelpalmer.floatingrotation.service.MainService;

public class MainActivity extends AppCompatActivity {

	public static Button startButton, stopButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startButton = (Button)findViewById(R.id.btnStartService);
		stopButton = (Button)findViewById(R.id.btnMsg);

		startButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				startService(new Intent(MainActivity.this, MainService.class));
			}

		});

		stopButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopService(new Intent(MainActivity.this, MainService.class));
			}

		});
	}

}
