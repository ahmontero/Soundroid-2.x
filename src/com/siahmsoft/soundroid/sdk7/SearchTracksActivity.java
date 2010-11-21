package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;
import com.siahmsoft.soundroid.sdk7.util.EndlessAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class SearchTracksActivity extends ListActivity {

    ArrayList<Track> result;
    MediaPlayerService mBoundMediaPlayerService;
    static int offset = 0;
    static int limit = 20;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoundMediaPlayerService = Soundroid.getMediaPlayerService();
        result = new ArrayList<Track>();

        setupViews();
    }

    private void setupViews() {
        setContentView(R.layout.search_tracks_activity);

        ImageButton searchButton = (ImageButton)findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.filter);
    }

    public void sendQuery(View v){
        offset = 0;
        result.clear();
        setListAdapter( new DemoAdapter(result));
    }

    class DemoAdapter extends EndlessAdapter {

        private RotateAnimation rotate = null;

        public DemoAdapter(ArrayList<Track> aa) {
            super(new ArrayAdapter<Track>(SearchTracksActivity.this,
                    R.layout.row,
                    android.R.id.text1,
                    result));

            rotate = new RotateAnimation(	0f,
                    						360f,
                    						Animation.RELATIVE_TO_SELF,
                    						0.5f,
                    						Animation.RELATIVE_TO_SELF,
                    						0.5f);
            rotate.setDuration(300);
            rotate.setRepeatMode(Animation.RESTART);
            rotate.setRepeatCount(Animation.INFINITE);
        }

        @Override
        protected View getPendingView(ViewGroup parent) {
            /* View row=getLayoutInflater().inflate(R.layout.tracklist_item_no_artwork, null);

            View child=row.findViewById(R.id.track_title);*/
            /*child.setVisibility(View.GONE);*/

            /*child=row.findViewById(R.id.track_artwork);
			child.setVisibility(View.GONE);*/

            /*child=row.findViewById(R.id.track_downloads);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_image_downloads);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_image_plays);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_playing_now);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_plays);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_sharing);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_title);
            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.track_username);
            child.setVisibility(View.GONE);*/

            /*child=row.findViewById(R.id.throbber);
            child.setVisibility(View.VISIBLE);
            child.startAnimation(rotate);

            return row;*/

            View row=getLayoutInflater().inflate(R.layout.row, null);

            View child=row.findViewById(android.R.id.text1);

            child.setVisibility(View.GONE);

            child=row.findViewById(R.id.throbber);
            child.setVisibility(View.VISIBLE);
            child.startAnimation(rotate);

            return row;
        }


        @Override
        protected void rebindPendingView(int position, View row) {

            /*if(position >= getWrappedAdapter().getCount()){
                ImageView throbber=(ImageView)row.findViewById(R.id.throbber);
                throbber.setVisibility(View.GONE);
                throbber.clearAnimation();
            }else{*/
            rebindView(position, row);
            //}
        }

        void rebindView(int position, View row){
            //Track res = (Track) getWrappedAdapter().getItem(position);
            //{userpermalink=mehdi-aouinet, useruri=http://api.soundcloud.com/users/680313, upload=null, license=all-rights-reserved, genre=, releaseyear=null, tracktype=, downloadcount=0, release=, originalformat=mp3, purchaseurl=null, labelid=null, idtrack=2028450, commentcount=0, waveformurl=http://waveforms.soundcloud.com/5oH1Q1tnVrBc_m.png, videourl=null, duration=166932, playbackcount=0, username=Mehdi Aouinet, title=Strike right, releasemonth=null, artworkpath=null, streamable=true, isrc=, createdat=2010/03/16 23:20:47 +0000, sharing=public, licenseid=null, bpm=0.0, tracktypeid=null, downloadable=false, permalinkurl=http://soundcloud.com/mehdi-aouinet/strike-right-1, userid=680313, userfavorite=null, userplaybackcount=0, permalink=strike-right-1, uri=http://api.soundcloud.com/tracks/2028450, userpermalinkurl=http://soundcloud.com/mehdi-aouinet, streamurl=http://media.soundcloud.com/stream/5oH1Q1tnVrBc, trackpath=null, releaseday=null, sharingid=null, taglist=, keysignature=, artworkurl=null, description=, labelname=}

            /* View child=row.findViewById(R.id.track_title);
            ((TextView)child).setText(res.getmTitle());
            child.setVisibility(View.VISIBLE);*/

            /*child=row.findViewById(R.id.track_artwork);
			child.setVisibility(View.VISIBLE);*/

            /* child=row.findViewById(R.id.track_downloads);
            ((TextView)child).setText(String.valueOf(res.getmDownloadCount()));
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_image_downloads);
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_image_plays);
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_playing_now);
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_plays);
            ((TextView)child).setText(String.valueOf(res.getmPlaybackCount()));
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_sharing);
            ((TextView)child).setText(res.getmSharing());
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_title);
            ((TextView)child).setText(res.getmTitle());
            child.setVisibility(View.VISIBLE);

            child=row.findViewById(R.id.track_username);
            ((TextView)child).setText(res.getmUsername());
            child.setVisibility(View.VISIBLE);*/

            /* ImageView throbber=(ImageView)row.findViewById(R.id.throbber);
            throbber.setVisibility(View.GONE);
            throbber.clearAnimation();*/
            //}else{

            //View child=row.findViewById(R.id.no_more_data);
            //child.setVisibility(View.VISIBLE);
            //}

            View child=row.findViewById(android.R.id.text1);
            child.setVisibility(View.VISIBLE);
            ((TextView)child).setText(getWrappedAdapter().getItem(position).toString());

            child=row.findViewById(R.id.throbber);
            child.setVisibility(View.GONE);
            child.clearAnimation();
        }

        boolean mFinal = true;

        @Override
        protected boolean cacheInBackground() {

            EditText searchText = (EditText)findViewById(R.id.searchText);
            String textABuscar = searchText.getText().toString();

            Soundroid.getSc().searchTracks(textABuscar, offset, limit, new ResultListener<ArrayList<Track>>() {

                @Override
                public void onError(Exception e) {
                    e.toString();
                    mFinal = false;
                }

                @Override
                public void onSuccess(ArrayList<Track> result) {

                    if(result.size() < limit && offset == 0 ){
                        SearchTracksActivity.this.result.addAll(result);
                        mFinal = false;
                    }else if( result.size() < limit){
                        mFinal = false;
                    }else if(result.size() == limit){
                        SearchTracksActivity.this.result.addAll(result);
                        offset++;
                    }
                }

                @Override
                public void onTrackReceived(Track track) {
                    //SearchTracksActivity.this.aux.add(track);
                }
            });

            return mFinal;
        }

        @Override
        protected void appendCachedData() {
            /*ArrayAdapter<Track> la = (ArrayAdapter<Track>)getWrappedAdapter();
            for(Track track : SearchTracksActivity.this.aux){
                la.add(track);
            }*/
        }

        @Override
        protected void hideTrobber(int position, View row) {
            View child=row.findViewById(android.R.id.text1);
            child=row.findViewById(R.id.throbber);
            child.setVisibility(View.GONE);
            child.clearAnimation();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Track track = (Track)getListAdapter().getItem(position);

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
}

/*
private class DownloadDataTask extends AsyncTask<Void, Integer, Boolean> {
boolean mFinal = true;

@Override
protected Boolean doInBackground(Void... params) {

	EditText searchText = (EditText)findViewById(R.id.searchText);
	String textABuscar = searchText.getText().toString();

	Soundroid.getSc().searchTracks(textABuscar, offset, limit, new ResultListener<ArrayList<Track>>() {

		@Override
		public void onError(Exception e) {
			e.toString();
		}

		@Override
		public void onSuccess(ArrayList<Track> result) {

			//tracks.addAll(result);
			offset++;

			if(result.size() == 0){
				mFinal = false;
			}else{
				mFinal = true;
			}
		}

		@Override
		public void onTrackReceived(Track track) {
			tracks.add(track);
		}
	});

	return mFinal;
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
 */