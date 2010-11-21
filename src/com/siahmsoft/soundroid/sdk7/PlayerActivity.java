package com.siahmsoft.soundroid.sdk7;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;
import com.siahmsoft.soundroid.sdk7.util.BitmapUtils;
import com.siahmsoft.soundroid.sdk7.util.Helper;

/**
 * 
 * Esta clase tiene que mostrar el reproductor de mp3 de la aplicación
 * 
 * @author Antonio
 * 
 */
public class PlayerActivity extends Activity {

	/* INNER CLASSES */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... args) {
			Bitmap bmImg = null;
			String imageUrl = args[0];
			bmImg = BitmapUtils.loadBitmap(imageUrl);

			return bmImg;
		}
	}

	private SeekBar seekBar;
	private TextView elapsedTime;
	private TextView duration;
	private TextView artist;
	private TextView trackname;
	private ImageView artwork;
	private ImageButton playPause;
	private ImageButton shuffle;
	private Handler handler = new Handler();
	private MediaPlayerService mBoundMediaPlayerService;

	Timer mTimer;

	TracksStore.Track track;

	StringBuilder sFormatBuilder = new StringBuilder();
	Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
	String durationformat = "%2$d:%5$02d";

	/** Formatting optimization to avoid creating many temporary objects. */
	final Object[] sTimeArgs = new Object[5];

	public Drawable getDrawable(String imgUrl) {
		try {
			if (imgUrl != null && !"null".equals(imgUrl)) {
				URL url = new URL(imgUrl);
				InputStream is = null;
				try {
					is = (InputStream) url.getContent();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Drawable d = null;
				if (is != null) {
					d = Drawable.createFromStream(is, "src");
				}
				return d;
			} else {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.media_player);

		mBoundMediaPlayerService = Soundroid.getMediaPlayerService();

		Bundle bundle = getIntent().getExtras();

		int idTrack = bundle.getInt("idTrack");

		Soundroid.getSc().getTrack(String.valueOf(idTrack),
				new ResultListener<Track>() {

					@Override
					public void onSuccess(Track result) {
						track = result;
					}

					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onTrackReceived(Track track) {
						// TODO Auto-generated method stub

					}
				});

		seekBar = (SeekBar) findViewById(R.id.seekbar);
		elapsedTime = (TextView) findViewById(R.id.time);
		duration = (TextView) findViewById(R.id.duration);
		playPause = (ImageButton) findViewById(R.id.playpause);
		shuffle = (ImageButton) findViewById(R.id.shuffle);
		artist = (TextView) findViewById(R.id.mp_artist);
		trackname = (TextView) findViewById(R.id.mp_trackname);
		artwork = (ImageView) findViewById(R.id.artwork);

		if (track.getmArtworkUrl() != null) {
			/*
			 * Bitmap bm = new
			 * DownloadImageTask().doInBackground(track.getmArtworkUrl());
			 * artwork.setImageBitmap(bm);
			 */
			Drawable d = getDrawable(track.getmArtworkUrl());
			if (d != null) {
				artwork.setImageDrawable(d);
			}
		}

		artist.setText(track.getmTitle().split("-")[0]);
		trackname.setText(track.getmTitle().split("-")[1]);

		playPause.setImageResource(android.R.drawable.ic_media_play);
		if (mBoundMediaPlayerService.isPlaying()) {
			playPause.setImageResource(android.R.drawable.ic_media_pause);
		}

		shuffle.setImageResource(android.R.drawable.star_big_on);

		shuffle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AddToFavouritesTask().execute();
			}
		});

		playPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mBoundMediaPlayerService.isPlaying()) {
					playPause
							.setImageResource(android.R.drawable.ic_media_play);
				} else {
					playPause
							.setImageResource(android.R.drawable.ic_media_pause);
				}

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
		});

		int secs = track.getmDuration() / 1000;

		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = secs / 60 % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;

		String trackDuration = sFormatter.format(durationformat, timeArgs)
				.toString();

		duration.setText(trackDuration);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				if (fromTouch) {
					mBoundMediaPlayerService.seekTo(progress);
				}
				// update the progress time
				progress /= Helper.MILLISECONDS_IN_SECOND;
				int mins = progress / Helper.SECONDS_IN_MINUTE;
				progress -= mins * Helper.SECONDS_IN_MINUTE;
				elapsedTime.setText(Helper.getTime(mins, progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		mBoundMediaPlayerService.onCompletion(new Runnable() {

			@Override
			public void run() {
				// handler.post(new Runnable() {
				// @Override
				// public void run() {
				playPause.setImageResource(android.R.drawable.ic_media_play);
				updateDisplay();
				mTimer.cancel();
				// }
				// });
			}
		});

		mTimer = new Timer();

		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						updateDisplay();
					}
				});
			}
		}, 0, 1000);
	}

	private void updateDisplay() {
		seekBar.setMax(mBoundMediaPlayerService.getDuration());
		seekBar.setProgress(mBoundMediaPlayerService.getCurrentPosition());
		// updatePauseButton(service.isPlaying());
	}

	private class AddToFavouritesTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(
				PlayerActivity.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.dialog.setIndeterminate(true);
			this.dialog.setCancelable(false);
			this.dialog.setMessage("Adding to favourites...");
			this.dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			Soundroid.getSc().addToFavorites(
					String.valueOf(track.getmIdTrack()));

			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
		}
	}
}