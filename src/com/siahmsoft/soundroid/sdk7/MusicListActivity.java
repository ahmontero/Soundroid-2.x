package com.siahmsoft.soundroid.sdk7;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;
import java.util.Locale;



public class MusicListActivity extends ListActivity {

    private static final String TAG = "MusicListActivity";

    public static final int MENU_ITEM_SELECT = Menu.FIRST;

    private static int lastSelected = -1;

    /** Optimizations to avoid creating many temporary objects. */
    static StringBuilder sFormatBuilder = new StringBuilder();
    static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    static String durationformat = "%2$d:%5$02d";

    /** Formatting optimization to avoid creating many temporary objects. */
    static final Object[] sTimeArgs = new Object[5];

    static final String[] projection = {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.IS_MUSIC,
        MediaStore.Audio.Media.DATA
    };

    static final String[] from = new String[] {
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DURATION
    };

    static final int[] to = new int[] {
        R.id.file_name,
        R.id.file_artist,
        R.id.file_format,
        R.id.file_size,
        R.id.file_duration
    };

    private MediaPlayer mMediaPlayer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        Cursor cursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE);


        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(	this,
                R.layout.music_browser,
                cursor,
                from,
                to);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean retval = false;

                if(columnIndex == 2){
                    int secs = cursor.getInt(columnIndex) / 1000;


                    sFormatBuilder.setLength(0);

                    final Object[] timeArgs = sTimeArgs;
                    timeArgs[0] = secs / 3600;
                    timeArgs[1] = secs / 60;
                    timeArgs[2] = secs / 60 % 60;
                    timeArgs[3] = secs;
                    timeArgs[4] = secs % 60;

                    TextView fileSize = (TextView)view;

                    fileSize.setText( "Duration: " + sFormatter.format(durationformat, timeArgs).toString());

                    retval = true;
                }

                if(columnIndex == 5){
                    String size = cursor.getString(columnIndex);

                    String sizef = android.text.format.Formatter.formatFileSize(MusicListActivity.this, Long.valueOf(size));

                    TextView fileSize = (TextView)view;
                    fileSize.setText("Size: " + sizef);

                    retval = true;
                }

                if (columnIndex == 7) {
                    String filePath = cursor.getString(7);

                    int a = filePath.lastIndexOf(".");
                    int b = filePath.length();

                    String extension = filePath.substring(a + 1, b).toLowerCase();

                    TextView textSize = (TextView) view;

                    textSize.setText(extension);

                    retval = true;
                }

                if (columnIndex == 1) {
                    String artist = cursor.getString(columnIndex);

                    TextView textSize = (TextView) view;
                    textSize.setText("<unknown>".equals(artist)? "<Unknow artist>" : artist);

                    retval = true;
                }

                return retval;
            }
        });

        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(4));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_SELECT, 0, "Select");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {

            case MENU_ITEM_SELECT: {
                Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

                String filePath = cursor.getString(7);
                String fileName = cursor.getString(4);

                int a = filePath.lastIndexOf(".");
                int b = filePath.length();

                String extension = filePath.substring(a + 1, b).toLowerCase();

                if("aiff".equals(extension) || "wav".equals(extension) || "flac".equals(extension) || "ogg".equals(extension) || "mp3".equals(extension) || "acc".equals(extension)){
                    Intent i = new Intent(MusicListActivity.this, MusicListActivity.class);
                    i.putExtra("fileName", fileName);
                    i.putExtra("filePath", filePath);

                    setResult(RESULT_OK, i);

                    MusicListActivity.this.finish();
                }else{
                    Toast.makeText(MusicListActivity.this, "You only can upload audio files with extension: aiff, wav, flac, ogg, mp3 or acc", Toast.LENGTH_LONG).show();
                }

                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        try {
            Cursor c = (Cursor)getListAdapter().getItem(position);

            int idTrack = c.getInt(0);

            if(lastSelected == -1){
                lastSelected = c.getInt(0);

                String path = c.getString(7);

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }else if(lastSelected == idTrack){
                mMediaPlayer.stop();
                mMediaPlayer.release();
                lastSelected = -1;
            }else{
                mMediaPlayer.release();

                lastSelected = c.getInt(0);

                String path = c.getString(7);

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error playing track " + ((Cursor)getListAdapter().getItem(position)).getString(7), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }
}