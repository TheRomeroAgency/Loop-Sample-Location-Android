package sampleapp.loop.ms.locations;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import ms.loop.loopsdk.providers.LoopLocationProvider;
import ms.loop.loopsdk.signal.SignalConfig;

public class NavigationViewAdapter extends BaseAdapter {
    private String[] listData;
    private LayoutInflater layoutInflater;
    private Context context;

    private static String Loop_URL = "https://www.loop.ms/";


    public NavigationViewAdapter(Context aContext, String[] listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
        context = aContext;
    }

    @Override
    public int getCount() {
        return listData.length;
    }

    @Override
    public Object getItem(int position) {
        return listData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawerview, null);
            holder = new ViewHolder();
            holder.menuTitle = (TextView) convertView.findViewById(R.id.menutitle);
            holder.locationswitch = (SwitchCompat) convertView.findViewById(R.id.switch_compat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.menuTitle.setText(listData[position]);

        if (holder.menuTitle.getText().equals(context.getResources().getString(R.string.locationtracker))) {
            holder.locationswitch.setVisibility(View.VISIBLE);

            boolean isChecked = SampleAppApplication.getBooleanSharedPrefValue(context.getApplicationContext(), "AppTracking", true);
            holder.locationswitch.setChecked(isChecked);
            holder.locationswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    boolean isLocationOn = SampleAppApplication.isLocationTurnedOn(context);
                    if (isChecked) {
                        if (!isLocationOn) {
                            SampleAppApplication.openLocationServiceSettingPage(context);
                        }
                        LoopLocationProvider.start(SignalConfig.SIGNAL_SEND_MODE_BATCH);
                    } else {
                        LoopLocationProvider.stop();
                    }
                    SampleAppApplication.setSharedPrefValue(context.getApplicationContext(), "AppTracking", isChecked);
                }
            });
        } else {
            holder.locationswitch.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMenuClickEvent(v, (String) holder.menuTitle.getText());
            }
        });
        return convertView;
    }

    public void handleMenuClickEvent(View v, String menuTitle){
        if (menuTitle.equals(context.getResources().getString(R.string.clearalllocations))) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }
        else  if (menuTitle.equals(context.getResources().getString(R.string.learnmore))) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Loop_URL));
            context.startActivity(browserIntent);
        }
    }
    static class ViewHolder {
        TextView menuTitle;
        SwitchCompat locationswitch;
    }
}
