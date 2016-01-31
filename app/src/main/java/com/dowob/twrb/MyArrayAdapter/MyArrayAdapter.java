package com.dowob.twrb.MyArrayAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public abstract class MyArrayAdapter<T> extends ArrayAdapter<T> {

    private Context _context;
    private int _resource, _dropDownViewResource;
    private List<T> _items;

    public MyArrayAdapter(Context context, int resource, List<T> items) {
        super(context, resource);
        _context = context;
        _resource = resource;
        setDropDownViewResource(_resource);
        _items = items;
    }

    public int getResource() {
        return _resource;
    }

    public int getDropDownViewResource() {
        return _dropDownViewResource;
    }

    @Override
    public void setDropDownViewResource(int resource) {
        super.setDropDownViewResource(resource);
        _dropDownViewResource = resource;
    }

    public Context getContext() {
        return _context;
    }

    @Override
    public T getItem(int position) {
        return _items.get(position);
    }

    public List<T> getItems() {
        return _items;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return _items.size();
    }

    protected View inflateView(ViewGroup parent) {
        View convertView;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(getResource(), parent, false);
        return convertView;
    }

    protected View createView(int position, View convertView, ViewGroup parent, ViewHolder viewHolder) {
        if (convertView == null) {
            convertView = inflateView(parent);
            findView(convertView, viewHolder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        setView(viewHolder, getItem(position));
        return convertView;
    }

    protected abstract void findView(View view, ViewHolder viewHolder);

    protected abstract void setView(ViewHolder viewHolder, T t);

    static public class ViewHolder {

    }

}
