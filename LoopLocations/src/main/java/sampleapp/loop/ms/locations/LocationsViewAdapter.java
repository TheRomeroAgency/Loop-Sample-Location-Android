package sampleapp.loop.ms.locations;

/**
 * Created on 6/1/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ms.loop.loopsdk.profile.KnownLocation;
import ms.loop.loopsdk.profile.Label;
import ms.loop.loopsdk.profile.Visit;
import ms.loop.loopsdk.profile.Visits;

/**
 * Created on 5/30/16.
 *
 *
 */
public class LocationsViewAdapter extends ArrayAdapter<KnownLocation> {

    Context context;
    int layoutResourceId;
    List<KnownLocation> locations = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE (MM/dd)", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public LocationsViewAdapter(Context context, int layoutResourceId, List<KnownLocation> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        update(data);
    }

    public void update(List<KnownLocation> data) {

        locations.clear();
        for (KnownLocation location:data) {
            //if (location.score > 0) {
                locations.add(location);
            //}
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TripHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TripHolder();
            holder.txtLocantionName = (TextView)row.findViewById(R.id.locationName);
            holder.txtLastVisited = (TextView)row.findViewById(R.id.txtlastvisited);

            holder.txtLastVisitEnterTime = (TextView)row.findViewById(R.id.lastvisitentertime);
            holder.txtLastVisitExitTime = (TextView)row.findViewById(R.id.lastvisitexittime);
            holder.txtVisitDuration = (TextView)row.findViewById(R.id.visitduration);

            holder.locationIcon = (ImageView)row.findViewById(R.id.locationicon);
            holder.txtVisitCount = (TextView) row.findViewById(R.id.visitcount);

            row.setTag(holder);
            row.setClickable(true);
        }
        else {
            holder = (TripHolder)row.getTag();
        }


        if (locations.isEmpty()) return row;

        final KnownLocation location = locations.get(position);
        if (location == null ) return row;

        String locationLabel = getLocationLabels(location);
        holder.txtLocantionName.setText(locationLabel);
        holder.locationIcon.setImageResource(locationLabel.equalsIgnoreCase("work")? R.drawable.work : R.drawable.home);
        holder.txtLastVisited.setText(String.format(Locale.US, "%s", dateFormat.format(location.updatedAt)));
        holder.txtVisitCount.setText(""+location.visits.size());

        List <Visit> visits = location.visits.getVisits();

        if (visits.size() > 0){

            Visit visit = visits.get(visits.size() -  1);
            holder.txtLastVisitEnterTime.setText(timeFormat.format(new Date(visit.startTime)));
            holder.txtLastVisitExitTime.setText(timeFormat.format(new Date(visit.endTime)));
            holder.txtVisitDuration.setText(getVisitDuration(visit));
        }

        final String locationLabelTemp = locationLabel;
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri gmmIntentUri = Uri.parse(String.format(Locale.US, "geo:0,0?q=%f,%f(%s)", location.latDegrees, location.longDegrees, locationLabelTemp));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });
        return row;
    }

    static class TripHolder {
        ImageView locationIcon;
        TextView txtLocantionName;
        TextView txtLastVisited;
        TextView txtLastVisitEnterTime;
        TextView txtLastVisitExitTime;
        TextView txtVisitDuration;
        TextView txtVisitCount;
    }

    public String getVisitInfo(KnownLocation knownLocation) {

        String enterExitInfo = String.format(Locale.US, "Entered at %s", dateFormat.format(new Date(knownLocation.lastEnteredAt)));

        if (knownLocation.lastExitedAt != -1) {
            enterExitInfo += String.format(Locale.US, " and exited at %s", dateFormat.format(new Date(knownLocation.lastExitedAt)));
        }
        return enterExitInfo;
    }

    public String getVisitDuration(Visit visit)
    {
        long diffInSeconds = (visit.endTime - visit.startTime) / 1000;

        long diff[] = new long[] {0, 0, 0 };
        diff[2] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        diff[0] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;

        return String.format(Locale.US,
                "%s%d:%s%d:%s%d",
                diff[0] < 9 ? "0" : "",
                diff[0],
                diff[1] < 9 ? "0": "",
                diff[1],
                diff[2] < 9 ? "0":"",
                diff[2]);
    }

    private String getLocationLabels(KnownLocation location) {

        StringBuffer locationLabel = new StringBuffer();
        if (location.hasLabels()){
            for (Label label:location.labels) {
                String tmpLabel = label.name;
                if (tmpLabel.equals("work")) tmpLabel = "Work";
                if (tmpLabel.equals("home")) tmpLabel = "Home";

                if (!TextUtils.isEmpty(locationLabel.toString())){
                    locationLabel.append(", ");
                }
                locationLabel.append(tmpLabel);

            }
        }
        else {
            return "Unknown";
        }

        return locationLabel.toString();
    }
}
