package com.siahmsoft.soundroid.sdk7.util;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * Adapter that delegates to a wrapped LisAdapter, much as
 * CursorWrapper delegates to a wrapped Cursor.
 */
public class AdapterWrapper extends BaseAdapter {
    private ListAdapter wrapped=null;

    /**
     * Constructor wrapping a supplied ListAdapter
     */
    public AdapterWrapper(ListAdapter wrapped) {
        super();

        this.wrapped=wrapped;

        wrapped.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    /**
     * Get the data item associated with the specified
     * position in the data set.
     * @param position Position of the item whose data we want
     */
    @Override
    public Object getItem(int position) {
        return wrapped.getItem(position);
    }

    /**
     * How many items are in the data set represented by this
     * Adapter.
     */
    @Override
    public int getCount() {
        return wrapped.getCount();
    }

    /**
     * Returns the number of types of Views that will be
     * created by getView().
     */
    @Override
    public int getViewTypeCount() {
        return wrapped.getViewTypeCount();
    }

    /**
     * Get the type of View that will be created by getView()
     * for the specified item.
     * @param position Position of the item whose data we want
     */
    @Override
    public int getItemViewType(int position) {
        return wrapped.getItemViewType(position);
    }

    /**
     * Are all items in this ListAdapter enabled? If yes it
     * means all items are selectable and clickable.
     */
    @Override
    public boolean areAllItemsEnabled() {
        return wrapped.areAllItemsEnabled();
    }

    /**
     * Returns true if the item at the specified position is
     * something selectable.
     * @param position Position of the item whose data we want
     */
    @Override
    public boolean isEnabled(int position) {
        return wrapped.isEnabled(position);
    }

    /**
     * Get a View that displays the data at the specified
     * position in the data set.
     * @param position Position of the item whose data we want
     * @param convertView View to recycle, if not null
     * @param parent ViewGroup containing the returned View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return wrapped.getView(position, convertView, parent);
    }

    /**
     * Get the row id associated with the specified position
     * in the list.
     * @param position Position of the item whose data we want
     */
    @Override
    public long getItemId(int position) {
        return wrapped.getItemId(position);
    }

    /**
     * Returns the ListAdapter that is wrapped by the endless
     * logic.
     */
    protected ListAdapter getWrappedAdapter() {
        return wrapped;
    }
}