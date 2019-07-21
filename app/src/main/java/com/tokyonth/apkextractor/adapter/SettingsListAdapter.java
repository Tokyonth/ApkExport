package com.tokyonth.apkextractor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.bean.SettingsContentBean;

import java.util.List;

public class SettingsListAdapter extends ArrayAdapter<SettingsContentBean> {

    private int resourceId;

    public SettingsListAdapter(Context context, int resource,List<SettingsContentBean> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsContentBean bean = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView textView = view.findViewById(R.id.tv_name);
        ImageView imageView = view.findViewById(R.id.iv_icon);
        textView.setText(bean.getName());
        Log.d("----------->", bean.getName());
        imageView.setImageResource(bean.getImage());
        return view;
    }
}

