package com.tokyonth.apkextractor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class CustomList extends ListView {

    private BaseAdapter mSelfAdapter;

    public CustomList(Context context) {
        super(context);
    }

    public CustomList(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * 删除ListView中上一次渲染的View，并添加新View。
     */
    private void buildList() {
        if (mSelfAdapter == null) {

        }

        if (getChildCount() > 0) {
            removeAllViews();
        }

        int count = mSelfAdapter.getCount();

        for(int i = 0 ; i < count ; i++) {
            View view = mSelfAdapter.getView(i, null, null);
            if (view != null) {
                addView(view, i);
            }
        }
    }

    public BaseAdapter getSelfAdapter() {
        return mSelfAdapter;
    }

    /**
     * 设置Adapter。
     *
     * @param selfAdapter
     */
    public void setSelfAdapter(BaseAdapter selfAdapter) {
        this.mSelfAdapter = selfAdapter;
        buildList();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}


