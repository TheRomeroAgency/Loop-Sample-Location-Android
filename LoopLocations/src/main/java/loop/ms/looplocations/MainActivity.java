package loop.ms.looplocations;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ms.loop.loopsdk.core.LoopSDK;
import ms.loop.loopsdk.profile.Drive;
import ms.loop.loopsdk.profile.Drives;
import ms.loop.loopsdk.profile.IProfileDownloadCallback;
import ms.loop.loopsdk.profile.IProfileItemChangedCallback;
import ms.loop.loopsdk.profile.KnownLocation;
import ms.loop.loopsdk.profile.Label;
import ms.loop.loopsdk.profile.Locations;
import ms.loop.loopsdk.profile.Trip;
import ms.loop.loopsdk.providers.LoopLocation;
import ms.loop.loopsdk.providers.LoopLocationProvider;
import ms.loop.loopsdk.signal.Signal;
import ms.loop.loopsdk.util.LoopDate;
import ms.loop.loopsdk.util.LoopError;

public class MainActivity extends AppCompatActivity {

    private Locations knowLocations;
    private BroadcastReceiver mReceiver;
    private LocationsViewAdapter locationsViewAdapter;

    private ListView locationsListView;
    private Switch locationSwitch;
    private TextView locationText;
    private TextView currentLocationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        knowLocations = Locations.createAndLoad(Locations.class, KnownLocation.class);

        List<KnownLocation> locations = knowLocations.sortedByScore();
        locationsViewAdapter = new LocationsViewAdapter(this,
                R.layout.locationview, locations);

        locationsListView = (ListView)findViewById(R.id.locationlist);

        locationsListView.setAdapter(locationsViewAdapter);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.onInitialized");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (LoopSDK.isInitialized()) {
                    loadKnownLocations();
                    updateCurrentLocation();
                }
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

        locationSwitch = (Switch) this.findViewById(R.id.locationswitch);
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked && !isLocationTurnedOn())
                    openLocationServiceSettingPage(MainActivity.this);

                else if (!isChecked && isLocationTurnedOn())
                    openLocationServiceSettingPage(MainActivity.this);
            }
        });

        locationText = (TextView) this.findViewById(R.id.txtlocationtracking);
        currentLocationText = (TextView) this.findViewById(R.id.txtcurrentlocation);

        final NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this);
        notification.setContentTitle("LOOP notification").setContentText("").setSmallIcon(R.drawable.home);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        knowLocations.registerItemChangedCallback("Locations", new IProfileItemChangedCallback() {
            @Override
            public void onItemChanged(String entityId) {
                KnownLocation knownLocation = knowLocations.byEntityId(entityId);
                if (knownLocation.hasLabels()){
                    Label label = knownLocation.labels.getLabels().get(0);
                    if (label.name.equals("home")){
                        // Send a notification
                        notification.setContentTitle("You are at Home");
                        mNotifyMgr.notify(100, notification.build());

                    }

                    if (label.name.equals("work")){
                        // Send a notification
                        notification.setContentTitle("You are at Work").setSmallIcon(R.drawable.work);
                        mNotifyMgr.notify(100, notification.build());
                    }
                }
            }

            @Override
            public void onItemAdded(String entityId) {
                KnownLocation knownLocation = knowLocations.byEntityId(entityId);
                if (knownLocation.hasLabels()){
                    Label label = knownLocation.labels.getLabels().get(0);
                    if (label.name.equals("home")){
                        notification.setContentTitle("Home generated - you are at home");
                        mNotifyMgr.notify(100, notification.build());
                    }

                    if (label.name.equals("work")){
                        notification.setContentTitle("Home generated - you are at work").setSmallIcon(R.drawable.work);
                        mNotifyMgr.notify(100, notification.build());
                    }
                }
            }

            @Override
            public void onItemRemoved(String entityId) {
            }
        });
    }

    public void updateCurrentLocation() {
        if (LoopSDK.isInitialized()) {
            LoopLocation loopLocation = LoopLocationProvider.getLastLocation();
            if (loopLocation == null) return;
            currentLocationText.setText(String.format(Locale.US, "Current Location: %.5f, %.5f", loopLocation.getLatitude(), loopLocation.getLongitude()));
        }
    }

    public void loadKnownLocations()
    {
        final List<KnownLocation> locations = knowLocations.sortedByScore();
        if (locations.size() > 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    locationsViewAdapter.update(locations);
                }
            });
        }
       else {
            if (!LoopSDK.isInitialized()) return;
            knowLocations.download(true, new IProfileDownloadCallback() {
                @Override
                public void onProfileDownloadComplete(int i) {
                    updateLocationsInUI();
                }
                @Override
                public void onProfileDownloadFailed(LoopError loopError) {}
            });
        }
    }

    public void updateLocationsInUI()
    {
        final List<KnownLocation> locations = knowLocations.sortedByScore();
        runOnUiThread(new Runnable() {
            public void run() {
                locationsViewAdapter.update(locations);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isLocationTurnedOn()) {
            locationText.setText(R.string.loop_enabled);
            locationSwitch.setChecked(true);
        } else {
            locationText.setText(R.string.turn_on_loop);
            locationSwitch.setChecked(false);
        }

        loadKnownLocations();

        if (LoopSDK.isInitialized()) {
            LoopSDK.forceSync();
        }
        updateCurrentLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
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
        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent myIntent = new Intent(this, SettingActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void openLocationServiceSettingPage(Context context)
    {
        final Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (locationIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(locationIntent);
        }
    }

    public boolean isLocationTurnedOn() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnbaled = false;

        try {
            locationEnbaled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationEnbaled) {
                locationEnbaled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception ex) {
        }
        return locationEnbaled;
    }
}
