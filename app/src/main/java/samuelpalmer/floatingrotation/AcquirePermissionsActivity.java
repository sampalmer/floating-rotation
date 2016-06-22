package samuelpalmer.floatingrotation;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

@TargetApi(Build.VERSION_CODES.M)
public class AcquirePermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SYSTEM_SETTINGS = 1;
    private boolean requestSystemSettings = true;

    private static final int REQUEST_CODE_DRAW_OVERLAYS = 2;
    private boolean requestDrawOverlays = true;

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acquire_permission);

        permissionManager = new PermissionManager(this);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });
    }

    private Uri packageUri() {
        return Uri.fromParts("package", AcquirePermissionsActivity.this.getPackageName(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SYSTEM_SETTINGS)
            requestSystemSettings = false;
        else if (requestCode == REQUEST_CODE_DRAW_OVERLAYS)
            requestDrawOverlays = false;

        requestPermissions();
    }

    private void requestPermissions() {
        if (permissionManager.canWriteSystemSettings())
            requestSystemSettings = false;
        if (permissionManager.canDrawOverlays())
            requestDrawOverlays = false;

        if (requestSystemSettings)
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri()), REQUEST_CODE_SYSTEM_SETTINGS);
        else if (requestDrawOverlays)
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri()), REQUEST_CODE_DRAW_OVERLAYS);
        else {
            if (permissionManager.canWriteSystemSettings() && permissionManager.canDrawOverlays()) {
                ApplicationState applicationState = new ApplicationState(this);
                applicationState.enabled.set(true);
                new ServiceController(this, applicationState).updateServiceState();
            }
            finish();
        }
    }
}
