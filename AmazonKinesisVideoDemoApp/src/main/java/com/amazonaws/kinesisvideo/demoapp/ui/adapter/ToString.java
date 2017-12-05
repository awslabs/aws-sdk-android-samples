package com.amazonaws.kinesisvideo.demoapp.ui.adapter;

import android.content.Context;

/**
 * Interface to extract string representation from an item.
 * Used in conjunction with ToStringArrayAdapter
 */
public interface ToString<T> {
    String toString(final Context context, final int itemIndex, final T item);
}
