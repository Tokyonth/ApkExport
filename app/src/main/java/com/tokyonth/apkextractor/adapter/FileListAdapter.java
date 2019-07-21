package com.tokyonth.apkextractor.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.data.FileItemInfo;

public class FileListAdapter extends BaseAdapter {

    private List<FileItemInfo> list;
    private LayoutInflater inflater;
    private int selectedPosition = -1;

    private onRadioButtonClickedListener monRadioButtonClicked;

    public FileListAdapter(List<FileItemInfo> list, Context context) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        this.selectedPosition = -1;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setList(List<FileItemInfo> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    public void setSelected(int position) {
        this.selectedPosition = position;
        this.notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return this.selectedPosition;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_item_folderitem, parent, false);
            holder = new ViewHolder();
            // holder.img=(ImageView)convertView.findViewById(R.id.item_folderitem_icon);
            holder.tv_filename = (TextView) convertView.findViewById(R.id.item_folderitem_name);
            holder.ra_selector = (RadioButton) convertView.findViewById(R.id.item_folderitem_selecor);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_filename.setText(list.get(position).file.getName().toString());

        if (position == this.selectedPosition) {
            holder.ra_selector.setChecked(true);
        } else {
            holder.ra_selector.setChecked(false);
        }

        holder.ra_selector.setOnClickListener(v -> {
            if (monRadioButtonClicked != null) {
                monRadioButtonClicked.onClick(position);
            }
        });

        return convertView;
    }

    public interface onRadioButtonClickedListener {
        public void onClick(int position);
    }

    public void setOnRadioButtonClickListener(onRadioButtonClickedListener monRadioButtonClicked) {
        this.monRadioButtonClicked = monRadioButtonClicked;
    }

    private static final class ViewHolder {
        //	ImageView img;
        TextView tv_filename;
        RadioButton ra_selector;
    }

}

