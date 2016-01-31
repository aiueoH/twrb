package com.dowob.twrb.MyArrayAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dowob.twrb.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DateArrayAdapter extends MyArrayAdapter<Date> {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd E");

    public DateArrayAdapter(Context context, int resource, List<Date> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = convertView == null ? new ViewHolder() : null;
        return createView(position, convertView, parent, viewHolder);
    }

    @Override
    protected void findView(View view, MyArrayAdapter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).textView = (TextView) view.findViewById(R.id.textView);
    }

    @Override
    protected void setView(MyArrayAdapter.ViewHolder viewHolder, Date date) {
        ((ViewHolder) viewHolder).textView.setText(dateFormat.format(date.getTime()));
    }

    static class ViewHolder extends MyArrayAdapter.ViewHolder {
        TextView textView;
    }
}
