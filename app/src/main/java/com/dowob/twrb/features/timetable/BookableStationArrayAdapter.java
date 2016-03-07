package com.dowob.twrb.features.timetable;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookableStation;
import com.dowob.twrb.features.shared.MyArrayAdapter;

import java.util.List;

public class BookableStationArrayAdapter extends MyArrayAdapter<BookableStation> {

    public BookableStationArrayAdapter(Context context, int resource, List<BookableStation> bookableStations) {
        super(context, resource, bookableStations);
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
    protected void setView(MyArrayAdapter.ViewHolder viewHolder, BookableStation bookableStation) {
        ((ViewHolder) viewHolder).textView.setText(bookableStation.getName());
    }

    static class ViewHolder extends MyArrayAdapter.ViewHolder {
        TextView textView;
    }
}
