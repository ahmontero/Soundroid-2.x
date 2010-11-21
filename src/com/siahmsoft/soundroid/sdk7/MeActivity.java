package com.siahmsoft.soundroid.sdk7;



import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.drawable.FastBitmapDrawable;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksManager;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;
import com.siahmsoft.soundroid.sdk7.services.UploadService;
import com.siahmsoft.soundroid.sdk7.util.BitmapUtils;
import com.siahmsoft.soundroid.sdk7.util.ImageUtilities;
import com.siahmsoft.soundroid.sdk7.util.ImportUtilities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MeActivity extends ListActivity implements OnItemClickListener {

    private static final String TAG = "MeActivity";
    MediaPlayerService mBoundMediaPlayerService;

    private static final String[] PROJECTION_COLUMNS = new String[] {
        TracksStore.Track._ID,
        TracksStore.Track.ID_TRACK,
        TracksStore.Track.TITLE,
        TracksStore.Track.USER_NAME,
        TracksStore.Track.PLAYBACK_COUNT,
        TracksStore.Track.DOWNLOAD_COUNT,
        TracksStore.Track.SHARING,
        TracksStore.Track.ARTWORK_URL};

    private static final int[] VIEW_MAPPINGS = new int[] {
        R.id.track_title,
        R.id.track_username,
        R.id.track_plays,
        R.id.track_downloads,
        R.id.track_sharing,
        R.id.track_artwork};

    // Menu item ids
    public static final int MENU_ITEM_EDIT = Menu.FIRST;
    public static final int MENU_ITEM_SHARE = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 3;
    public static final int MENU_ITEM_REFRESH = Menu.FIRST + 4;
    public static final int EDIT_ID = Menu.FIRST + 5;

    private UploadService mBoundUploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoundMediaPlayerService = Soundroid.getMediaPlayerService();
        mBoundUploadService = Soundroid.getUploadService();

        //getWindow().setBackgroundDrawable(null);

        new FetchTracks().doInBackground();

        Intent intent = getIntent();

        if (intent.getData() == null) {
            intent.setData(Track.CONTENT_URI);
        }

        setupViews();
        handleSearchQuery(getIntent());
    }

    private void handleSearchQuery(Intent queryIntent) {
        final String queryAction = queryIntent.getAction();
        /* if (Intent.ACTION_SEARCH.equals(queryAction)) {
            onSearch(queryIntent);
        } else*/ if (Intent.ACTION_VIEW.equals(queryAction)) {
            final Intent viewIntent = new Intent(Intent.ACTION_VIEW, queryIntent.getData());
            startActivity(viewIntent);
        }
    }

    //    private void onSearch(Intent intent) {
    //        final String queryString = intent.getStringExtra(SearchManager.QUERY);
    //        getListView().setFilterText(queryString);
    //    }

    public Drawable getDrawable(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            InputStream is = null;
            try {
                is = (InputStream) url.getContent();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (is != null){
                Drawable d = Drawable.createFromStream(is, "src");
                return d;
            }
            else{
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupViews() {
        Cursor c = TracksManager.getMeTracks(getContentResolver());
        startManagingCursor(c);

        MySimpleCursorAdapter adapter = new MySimpleCursorAdapter(MeActivity.this, R.layout.tracklist_item, c, PROJECTION_COLUMNS, VIEW_MAPPINGS);

        Button bv = new Button(this);
        bv.setText("Refresh");
        bv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new FetchTracks().execute();
            }
        });
        getListView().addHeaderView(bv, null, false);

        //Continue the activity
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        setListAdapter(adapter);

        //Set up all the listeners
        getListView().setOnCreateContextMenuListener(this);
        getListView().setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageUtilities.cleanupCache();
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

        // Setup the menu header --> COLUMN_INDEX_TITLE
        /* String title = cursor.getString(cursor.getColumnIndex(Track.TITLE));
        menu.setHeaderTitle(title); */

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_EDIT, 0, "Edit");
        menu.add(0, MENU_ITEM_SHARE, 0, "Share");
        menu.add(0, MENU_ITEM_DELETE, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {

            case MENU_ITEM_EDIT: {
                // Edit the note that the context menu is for
                Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);

                String action = getIntent().getAction();

                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
                    // The caller is waiting for us to return a note selected by
                    // the user. The have clicked on one, so return it now.
                    setResult(RESULT_OK, new Intent().setData(uri));
                } else {
                    // Launch activity to view/edit the currently selected item
                    startActivity(new Intent(Intent.ACTION_EDIT, uri));
                }
                return true;
            }

            case MENU_ITEM_SHARE: {
                // Share
                //startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);

                Intent shareTracks = new Intent(MeActivity.this, ShareTrackActivity.class);
                shareTracks.setData(ContentUris.withAppendedId(getIntent().getData(), info.id));
                //Aqui tiene que pasarse el id de la cancion en Soundcloud!!
                //Habria que recorrer el cursor y obtener la columna adecuada

                startActivity(shareTracks);

                return true;
            }

            case MENU_ITEM_DELETE: {
                // Delete the note that the context menu is for
                final Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
                final Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);

                if (cursor == null) {
                    // For some reason the requested item isn't available, do nothing
                    return false;
                }

                createAlertDialog(uri).show();

                return true;
            }
        }
        return false;
    }

    private AlertDialog createAlertDialog(final Uri uri){
        return new AlertDialog.Builder(this)
        .setTitle("Delete track confirmation")
        .setMessage("Are you sure that you want delete this track?")
        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                // do nothing – it will close on its own
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new DeleteTracks().execute(uri);
                new FetchTracks().execute();
            }
        }).create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, "Insert").setShortcut('3', 'a').setIcon(android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, "Edit Prefs").setIcon(R.drawable.equalizer).setAlphabeticShortcut('e');

        // Generate any additional actions that can be performed on the
        // overall list. In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, MeActivity.class), null, intent, 0, null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_INSERT:
                /*
                // Launch activity to insert a new item
                Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData());
                i.putExtra("bundle", new Bundle());
                //startActivityForResult(i, 555);
                startActivity(i);*/

                Intent i = new Intent(this, TrackEditorActivity.class);
                startActivityForResult(i, 424);

                /*Bundle bundle = new Bundle();
                bundle.putInt("idTrack", soundcloudTrack.getmIdTrack());
                i.putExtras(bundle);*/

                return true;

            case EDIT_ID:
                startActivity(new Intent(this, EditPreferences.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection. This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Build menu... always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0, items);

            // Give a shortcut to the edit action.
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //por si acaso alguna de las activities que lanzamos deben devolver algún valor

        if (data != null) {
            if (resultCode == RESULT_OK) {
                //new UploadTrack().execute(data.getExtras());
                startService(new Intent(MeActivity.this, UploadService.class).putExtras(data.getExtras()));

                //mBoundUploadService.uploadTrack(data.getExtras());
            }
        }
    }

    private class DeleteTracks extends AsyncTask<Uri, Void, Void>{
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];

            mBoundUploadService.deleteTrack(uri);

            return null;
        }
    }

    private class FetchTracks extends AsyncTask<Void, Integer, Void>{
        private final ProgressDialog dialog = new ProgressDialog(MeActivity.this);

        @Override
        protected Void doInBackground(Void... arg0) {

            //!TracksManager.getAllTracks(getContentResolver()).moveToFirst()

            TracksManager.deleteAllTrack(getContentResolver());

            Soundroid.getSc().retrieveMeTracks(new ResultListener<ArrayList<Track>>() {

                @Override
                public void onSuccess(ArrayList<Track> result) {

                    //                  for(Track track : result){
                    //                      TracksManager.saveTrack(getContentResolver(), track);
                    //                  }
                }

                @Override
                public void onError(Exception e) {
                    e.toString();

                }

                @Override
                public void onTrackReceived(Track track) {
                    if(!TracksManager.trackExists(getContentResolver(), String.valueOf(track.getmIdTrack()))){
                        TracksManager.saveTrack(getContentResolver(), track);
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setIndeterminate(true);
            this.dialog.setCancelable(false);
            this.dialog.setMessage("Refreshing Data...");
            this.dialog.show();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... args) {
            Bitmap bmImg = null;
            String imageUrl = args[0];

            //mini  -> 16x16
            //small -> 32x32
            //badge -> 47x47
            imageUrl = imageUrl.replace("large", "badge");

            bmImg = BitmapUtils.loadBitmap(imageUrl);

            return bmImg;
        }
    }

    class MySimpleCursorAdapter extends SimpleCursorAdapter{

        public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //super.bindView(view, context, cursor);
            // actual logic to populate row from Cursor goes here
            String url = cursor.getString(cursor.getColumnIndex(TracksStore.Track.ARTWORK_URL));
            Track track = TracksStore.Track.fromCursor(cursor);

            FastBitmapDrawable bitMap = ImageUtilities.getCachedCover(track.getInternalId(), null);

            if(bitMap == null){
                //Drawable drawable = getDrawable(url);
                Bitmap bitmap = new DownloadImageTask().doInBackground(url);
                if(bitmap != null && ImportUtilities.addTrackCoverToCache(track, bitmap)){
                    Drawable drawable = Drawable.createFromPath("/sdcard/soundroid/tracks/" +  track.getInternalId());
                    ImageView icono = (ImageView) view.findViewById(R.id.track_artwork);
                    icono.setImageDrawable(drawable);
                }

            }else{
                Drawable drawable = Drawable.createFromPath("/sdcard/soundroid/tracks/" +  track.getInternalId());
                if(drawable != null) {
                    ImageView icono = (ImageView) view.findViewById(R.id.track_artwork);
                    icono.setImageDrawable(drawable);
                }
            }

            String title = cursor.getString(cursor.getColumnIndex(TracksStore.Track.TITLE));
            String username = cursor.getString(cursor.getColumnIndex(TracksStore.Track.USER_NAME));
            String playbackCount = cursor.getString(cursor.getColumnIndex(TracksStore.Track.PLAYBACK_COUNT));
            String downloadCount = cursor.getString(cursor.getColumnIndex(TracksStore.Track.DOWNLOAD_COUNT));
            String sharing = cursor.getString(cursor.getColumnIndex(TracksStore.Track.SHARING));

            TextView vTitle = (TextView) view.findViewById(R.id.track_title);
            vTitle.setText(title);

            TextView vUsername = (TextView) view.findViewById(R.id.track_username);
            vUsername.setText(username);

            TextView vPlaybackCount = (TextView) view.findViewById(R.id.track_plays);
            vPlaybackCount.setText(playbackCount);

            TextView vDownloadCount = (TextView) view.findViewById(R.id.track_downloads);
            vDownloadCount.setText(downloadCount);

            TextView vSharing = (TextView) view.findViewById(R.id.track_sharing);
            vSharing.setText(sharing);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.tracklist_item, null);
            return row;
        }
    }

    class DeleteObject{
        Track track;
        long id;

        DeleteObject(Track track, long id){
            this.track = track;
            this.id = id;
        }

        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public Track getTrack() {
            return track;
        }
        public void setTrack(Track track) {
            this.track = track;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        Track mTrack = TracksManager.findTrack(getContentResolver(), uri);

        try {
            mBoundMediaPlayerService.playSong(mTrack);
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
}