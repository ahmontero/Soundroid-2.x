package com.siahmsoft.soundroid.sdk7;

import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class DropBoxActivity extends AbstractListActivity {
    private static final String[] PROJECTION_COLUMNS = new String[] {
        TracksStore.Track.TITLE,
        TracksStore.Track.USER_NAME,
        TracksStore.Track.PLAYBACK_COUNT,
        TracksStore.Track.DOWNLOAD_COUNT,
        TracksStore.Track.SHARING};

    private static final int[] VIEW_MAPPINGS = new int[] {
        R.id.track_title,
        R.id.track_username,
        R.id.track_plays,
        R.id.track_downloads,
        R.id.track_sharing};

    @Override
    void onAbstractCreate(Bundle savedInstance) {
        new RetrieveDropBoxTracksTask().execute();
    }

    private class RetrieveDropBoxTracksTask extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {
        private final ProgressDialog dialog = new ProgressDialog(DropBoxActivity.this);
        int i = 0;

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {

            final ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();

            Soundroid.getSc().retrieveDropBoxTracks(new ResultListener<ArrayList<Track>>() {

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

            SimpleAdapter adapter = new SimpleAdapter(DropBoxActivity.this, result, R.layout.tracklist_item_no_artwork, PROJECTION_COLUMNS, VIEW_MAPPINGS);
            setListAdapter(adapter);
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