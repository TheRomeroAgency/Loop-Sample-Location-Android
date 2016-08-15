package sampleapp.loop.ms.locations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shuaib on 8/14/16.
 */
public class CustomListAdapter extends BaseAdapter {
    private String [] listData;
    private LayoutInflater layoutInflater;

    public CustomListAdapter(Context aContext, String[] listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawerview, null);
            holder = new ViewHolder();
            holder.menuTitle = (TextView) convertView.findViewById(R.id.menutitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.menuTitle.setText(listData[position]);
        return convertView;
    }

    static class ViewHolder {
        TextView menuTitle;
    }
}
