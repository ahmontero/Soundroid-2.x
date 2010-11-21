package com.siahmsoft.soundroid.sdk7;

import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractListActivity extends ListActivity {

    MediaPlayerService mBoundMediaPlayerService;
    Track mTrack;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //Aquí debería reproducir la canción
        Object o =  l.getSelectedItem();
        Track track = null;

        if(o instanceof HashMap){
            HashMap<String, String> hashMap = (HashMap) getListAdapter().getItem(position);

            if (hashMap == null) {
                //For some reason the requested item isn't available, do nothing
                return ;
            }

            track = TracksStore.Track.fromMap(hashMap);

        }else if(o instanceof Cursor){
            Cursor cursor = (Cursor) getListAdapter().getItem(position);

            if (cursor == null) {
                //For some reason the requested item isn't available, do nothing
                return ;
            }

            track = TracksStore.Track.fromCursor(cursor);
        }

        mTrack = track;

        //onListItemClick();

        try {
            mBoundMediaPlayerService.playSong(track);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //public abstract void onListItemClick();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoundMediaPlayerService = Soundroid.getMediaPlayerService();

        onAbstractCreate(savedInstanceState);
    }

    abstract void onAbstractCreate(Bundle savedInstance);
}