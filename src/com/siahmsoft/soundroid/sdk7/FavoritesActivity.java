package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class FavoritesActivity extends ListActivity implements OnItemClickListener{
    ArrayList<HashMap<String, String>> res;
    SimpleAdapter adapter;
    MediaPlayerService mBoundMediaPlayerService;

    private static final String[] PROJECTION_COLUMNS = new String[] {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBoundMediaPlayerService = Soundroid.getMediaPlayerService();

        res = new ArrayList<HashMap<String, String>>();
        adapter = new SimpleAdapter(FavoritesActivity.this, res, R.layout.tracklist_item_no_artwork, PROJECTION_COLUMNS, VIEW_MAPPINGS);

        Button bv = new Button(this);
        bv.setText("Refresh");
        bv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new RetrieveDropBoxTracksTask().execute();
            }
        });
        getListView().addHeaderView(bv, null, false);

        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);

        new RetrieveDropBoxTracksTask().execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

        HashMap<String, String> hashMap = (HashMap)  getListView().getItemAtPosition(position);

        if (hashMap == null) {
            //For some reason the requested item isn't available, do nothing
            return ;
        }

        Track mTrack = TracksStore.Track.fromMap(hashMap);

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

    private class RetrieveDropBoxTracksTask extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {
        private final ProgressDialog dialog = new ProgressDialog(FavoritesActivity.this);
        int i = 0;

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {

            Soundroid.getSc().retrieveFavoritesTracks(new ResultListener<ArrayList<Track>>() {

                @Override
                public void onError(Exception e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSuccess(ArrayList<Track> result) {
                    for(Track track : result){
                        if(Boolean.valueOf(track.getmStreamable())){
                            res.add(track.getMapValues());
                            publishProgress(i++);
                        }
                    }
                }

                @Override
                public void onTrackReceived(Track result) {
                    // TODO Auto-generated method stub

                }
            });

            return res;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setCancelable(false);
            this.dialog.setMessage("Searching...");
            this.dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            dialog.incrementProgressBy(progress[0]);
        }
    }
}