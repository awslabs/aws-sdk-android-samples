package com.amazonaws.kinesisvideo.demoapp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Simple array adapter. Extracts the string representation from the items
 * using the supplied ToString interface implementation
 */
public class ToStringArrayAdapter<T> extends ArrayAdapter<T> {

    private final ToString<T> mToString;
    private final int mItemLayoutId;
    private final int mTextViewId;

    public ToStringArrayAdapter(final Context context,
                                final int itemLayoutId,
                                final int textViewId,
                                final ToString<T> toString,
                                final T[] items) {
        super(context, itemLayoutId, textViewId, items);
        mToString = toString;
        mItemLayoutId = itemLayoutId;
        mTextViewId = textViewId;
    }

    public ToStringArrayAdapter(final Context context,
                                final int itemLayoutId,
                                final int textViewId,
                                final ToString<T> toString,
                                final List<T> items) {
        super(context, itemLayoutId, textViewId, items);
        mToString = toString;
        mItemLayoutId = itemLayoutId;
        mTextViewId = textViewId;
    }

    @NonNull
    @Override
    public View getView(final int itemIndex,
                        @Nullable final View itemView,
                        @NonNull final ViewGroup parentView) {
        return baseGetView(itemIndex, itemView, parentView);
    }

    @Override
    public View getDropDownView(final int itemIndex,
                                @Nullable final View itemView,
                                @NonNull final ViewGroup parentView) {
        return baseGetView(itemIndex, itemView, parentView);
    }

    private View baseGetView(final int itemIndex, final @Nullable View itemView, final @NonNull ViewGroup parentView) {
        final T item = getItem(itemIndex);

        final ToStringViewHolder viewHolder = itemView == null
                ? createViewHolder(parentView)
                : (ToStringViewHolder) itemView.getTag();

        viewHolder.setText(getContext(), itemIndex, item);
        return viewHolder.getView();
    }

    private ToStringViewHolder createViewHolder(final ViewGroup parentView) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View view = layoutInflater.inflate(mItemLayoutId, parentView, false);

        final ToStringViewHolder viewHolder = new ToStringViewHolder(mToString, view, mTextViewId);
        view.setTag(viewHolder);
        return viewHolder;
    }
}
