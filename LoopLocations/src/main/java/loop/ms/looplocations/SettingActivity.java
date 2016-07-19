package loop.ms.looplocations;

import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ms.loop.loopsdk.core.LoopSDK;

public class SettingActivity extends AppCompatActivity {

    TextView txtUserId;
    TextView txtVersionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtUserId = (TextView)this.findViewById(R.id.userid);
        txtUserId.setText(String.format(Locale.US, "UserId: %s", LoopSDK.userId));

        txtUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(LoopSDK.userId);
                Toast.makeText(SettingActivity.this, "Id copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        txtVersionId = (TextView)this.findViewById(R.id.versionId);
        txtVersionId.setText(String.format(Locale.US, "App Version: %s/%s", BuildConfig.VERSION_NAME, LoopSDK.getLibraryVersion()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(myIntent);
        return super.onOptionsItemSelected(item);
    }
}
