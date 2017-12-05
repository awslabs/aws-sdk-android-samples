package com.amazonaws.kinesisvideo.demoapp.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.amazonaws.kinesisvideo.demoapp.ui.adapter.ToString;
import com.amazonaws.kinesisvideo.demoapp.ui.adapter.ToStringArrayAdapter;

import java.util.List;

public class StringSpinnerWidget<T> {

    private Spinner mSpinner;

    public interface ItemSelectedListener<T> {
        void itemSelected(final T item);
    }

    public StringSpinnerWidget(final Context context,
                               final View rootView,
                               final int spinnerId,
                               final List<T> items) {
        mSpinner = (Spinner) rootView.findViewById(spinnerId);

        final ArrayAdapter adapter = new ArrayAdapter<T>(
                context,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                items);

        mSpinner.setAdapter(adapter);
    }

    public StringSpinnerWidget(final Context context,
                               final View rootView,
                               final int spinnerId,
                               final ToString<T> toString,
                               final List<T> items) {
        mSpinner = (Spinner) rootView.findViewById(spinnerId);

        final ToStringArrayAdapter adapter = new ToStringArrayAdapter<T>(
                context,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                toString,
                items);

        mSpinner.setAdapter(adapter);
    }

    public T getSelectedItem() {
        return (T) mSpinner.getSelectedItem();
    }

    public void setItemSelectedListener(final ItemSelectedListener<T> itemSelectedListener) {
        mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> spinnerView,
                                       final View itemView,
                                       final int itemIndex,
                                       final long itemId) {
                itemSelectedListener.itemSelected(getSelectedItem());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
                itemSelectedListener.itemSelected(getSelectedItem());
            }
        });
    }

    public int getCount() {
        return mSpinner.getCount();
    }

    public T getItem(final int itemIndex) {
        return (T) mSpinner.getAdapter().getItem(itemIndex);
    }

    public void selectItem(final int itemIndex) {
        mSpinner.setSelection(itemIndex);
    }
}
