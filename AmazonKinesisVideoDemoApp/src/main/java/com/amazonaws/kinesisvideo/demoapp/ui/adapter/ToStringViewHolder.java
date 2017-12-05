package com.amazonaws.kinesisvideo.demoapp.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class ToStringViewHolder<T> {
    private final ToString<T> mToString;
    private final View mView;
    private final TextView mTextView;

    public ToStringViewHolder(final ToString<T> toString,
                              final View view,
                              final int textViewId) {
        mToString = toString;
        mView = view;
        mTextView = (TextView) view.findViewById(textViewId);
    }

    public void setText(final Context context, final int itemIndex, final T item) {
        final String itemString = mToString.toString(context, itemIndex, item);
        mTextView.setText(itemString);
    }

    public View getView() {
        return mView;
    }
}
