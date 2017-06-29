package com.satyre.cpmaptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Satyre on 28/06/2017.
 */

public class GenericAdapter<T> extends ArrayAdapter<T> {

    public interface AdapterListener<T> {
        public void setView(View view, T object);
    }

    private AdapterListener<T> listener;
    private LayoutInflater inflater;
    private int layoutId;

    public GenericAdapter(Context context, int resource, AdapterListener<T> listener) {
        super(context, resource);
        layoutId = resource;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
    }

    public GenericAdapter(Context context, int resource, T[] objects, AdapterListener<T> listener) {
        super(context, resource, objects);
        layoutId = resource;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
    }

    public GenericAdapter(Context context, int resource, List<T> objects, AdapterListener<T> listener) {
        super(context, resource, objects);
        layoutId = resource;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(layoutId, parent, false);

        listener.setView(convertView, getItem(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    //comment to unreverse list
    @Override
    public T getItem(int position) {
        return super.getItem(getCount() - position - 1);
    }
}
