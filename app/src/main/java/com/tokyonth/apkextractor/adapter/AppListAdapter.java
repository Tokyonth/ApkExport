package com.tokyonth.apkextractor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tokyonth.apkextractor.R;
import com.tokyonth.apkextractor.data.AppItemInfo;
import com.tokyonth.apkextractor.widget.CircleImageView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.MyViewHolder> {

    private OnItemClickListener mItemClickListener;
    private OnLongClickListener mLongClickListener;

    private Context context;
    private List<AppItemInfo> applist;
    private boolean isMultiSelectMode = false;
    private boolean[] isSelected;
    private boolean[] ifshowedAnim;
    private boolean ifAnim;

    public AppListAdapter(Context context, List<AppItemInfo> applist, boolean ifAnim) {
        this.context = context;
        this.applist = applist;
        this.isSelected = new boolean[this.applist.size()];
        this.ifshowedAnim = new boolean[this.applist.size()];
        this.ifAnim = ifAnim;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setLongClickListener(OnLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnLongClickListener{
        boolean onLongClick(int position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_item_applist, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        AppItemInfo item = applist.get(position);
        holder.icon.setImageDrawable(item.getIcon());
        holder.label.setText(item.getAppName() + "(" + item.getVersion() + ")");
        holder.packagename.setText(item.getPackageName());
        if (item.isSystemApp) {
            holder.label.setTextColor(context.getResources().getColor(R.color.color_text_darkred));
        } else {
            holder.label.setTextColor(context.getResources().getColor(R.color.color_text_black));
        }
        holder.appsize.setText(Formatter.formatFileSize(context, item.getPackageSize()));
        if (this.isMultiSelectMode && this.isSelected != null) {
            if (position < this.isSelected.length) {
                holder.select.setChecked(this.isSelected[position]);
            }
            holder.select.setVisibility(View.VISIBLE);
            holder.appsize.setVisibility(View.GONE);
        } else {
            holder.select.setVisibility(View.GONE);
            holder.appsize.setVisibility(View.VISIBLE);
        }

        //设置点击和长按事件
        if (mItemClickListener != null){
            holder.itemView.setOnClickListener(view -> mItemClickListener.onItemClick(position));
        }
        if (mLongClickListener != null){
            holder.itemView.setOnLongClickListener(view -> mLongClickListener.onLongClick(position));
        }

    }

    @Override
    public int getItemCount() {
        return applist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView icon;
        private TextView label;
        private TextView packagename;
        private TextView appsize;
        private CheckBox select;

        public MyViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.appimg);
            label = (TextView) view.findViewById(R.id.appname);
            packagename = (TextView) view.findViewById(R.id.apppackagename);
            appsize = (TextView) view.findViewById(R.id.appsize);
            select = (CheckBox) view.findViewById(R.id.select);
        }

    }

    public void setMultiSelectMode(int position) {
        this.isSelected = new boolean[this.applist.size()];
        this.isMultiSelectMode = true;
    }

    public void cancelMutiSelectMode() {
        this.isMultiSelectMode = false;
        this.notifyDataSetChanged();
    }

    public void selectAll() {
        if (this.isSelected != null) {
            for (int i = 0; i < this.isSelected.length; i++) {
                this.isSelected[i] = true;
            }
            this.notifyDataSetChanged();
        }

    }

    public void deselectAll() {
        if (this.isSelected != null) {
            for (int i = 0; i < this.isSelected.length; i++) {
                this.isSelected[i] = false;
            }
            this.notifyDataSetChanged();
        }
    }

    public int getSelectedNum() {
        if (this.isMultiSelectMode && this.isSelected != null) {
            int num = 0;
            for (int i = 0; i < this.isSelected.length; i++) {
                if (this.isSelected[i]) {
                    num++;
                }
            }
            return num;
        } else {
            return 0;
        }
    }

    public long getSelectedAppsSize() {
        if (this.isSelected != null) {
            long size = 0;
            for (int i = 0; i < this.isSelected.length; i++) {
                if (this.isSelected[i]) {
                    size += this.applist.get(i).getPackageSize();
                }
            }
            return size;
        } else {
            return 0;
        }
    }

    public void onItemClicked(int position) {
        if (position < 0 || position > this.applist.size()) return;
        this.isSelected[position] = !this.isSelected[position];
        this.notifyDataSetChanged();
    }

    public boolean[] getIsSelected() {
        return this.isSelected;
    }

    public List<AppItemInfo> getAppList() {
        return this.applist;
    }

}

