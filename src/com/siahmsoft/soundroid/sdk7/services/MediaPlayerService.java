package com.siahmsoft.soundroid.sdk7.services;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.siahmsoft.soundroid.sdk7.PlayerActivity;
import com.siahmsoft.soundroid.sdk7.Soundroid;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;

public class MediaPlayerService extends Service {

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class MediaPlayerBinder extends Binder {
		public MediaPlayerService getService() {
			return MediaPlayerService.this;
		}
	}

	/**
	 * Inner class for handling interruption on incoming phone calls.
	 */
	private class TelephoneCallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_IDLE
					&& musicInterruptedByCall && !mMediaPlayer.isPlaying()) {
				musicInterruptedByCall = false;
				mMediaPlayer.start();
			} else { // state ringing or off hook
				if (mMediaPlayer.isPlaying()) {
					musicInterruptedByCall = true;
					mMediaPlayer.pause();
				}
			}
		}
	}

	public static final String MEDIAPLAYER_SERVICE = "com.siahmsoft.soundroid.services.MediaPlayerService.SERVICE";

	private boolean musicInterruptedByCall = false;

	private MediaPlayer mMediaPlayer;

	private Runnable onBufferingUpdateListener;
	private Runnable onCompletionListener;
	private Runnable onErrorListener;
	private Runnable onInfoListener;
	private Runnable onPreparedListener;
	private Runnable onSeekCompleteListener;

	private NotificationManager mNM;

	private TracksStore.Track soundcloudTrack = null;

	/**
	 * Handles interrupting calls.
	 */
	private TelephoneCallStateListener phoneListener;

	private TelephonyManager telephonyManager;

	private final IBinder mBinder = new MediaPlayerBinder();

	private static boolean isPaused;

	// @Override
	// public int onStartCommand(Intent intent, int flags, int startId) {
	// Log.i("LocalService", "Received start id " + startId + ": " + intent);
	// // We want this service to continue running until it is explicitly
	// // stopped, so return sticky.
	// return START_STICKY;
	// }

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Log.i("LocalService", "Received start id " + startId + ": " + intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		return mMediaPlayer.getDuration();
	}

	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onBufferingUpdate(Runnable r) {
		onBufferingUpdateListener = r;
	}

	public void onCompletion(Runnable r) {
		onCompletionListener = r;
	}

	private void configureMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioTrack.MODE_STREAM);

		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mMediaPlayer.isPlaying()) {
					mp.stop();
				}

				mp.reset();

				if (onCompletionListener != null) {
					onCompletionListener.run();
				}
			}
		});

		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				if (onPreparedListener != null) {
					onPreparedListener.run();
					mp.start();
				} else {
					mp.start();
				}
			}
		});

		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// configureMediaPlayer();
				if (onErrorListener != null) {
					onErrorListener.run();
					return true;
				} else {
					return false;
				}

			}
		});

		mMediaPlayer
				.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

					@Override
					public void onBufferingUpdate(MediaPlayer mp, int percent) {
						if (onBufferingUpdateListener != null) {
							onBufferingUpdateListener.run();
						}
					}
				});

		mMediaPlayer.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				if (onInfoListener != null) {
					onInfoListener.run();
					return true;
				} else {
					return false;
				}
			}
		});

		mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (onSeekCompleteListener != null) {
					onSeekCompleteListener.run();
				}
			}
		});
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioTrack.MODE_STREAM);

		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mMediaPlayer.isPlaying()) {
					mp.stop();
				}

				mp.reset();

				if (onCompletionListener != null) {
					onCompletionListener.run();
				}
			}
		});

		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				if (onPreparedListener != null) {
					onPreparedListener.run();
					mp.start();
				} else {
					mp.start();
				}
			}
		});

		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// configureMediaPlayer();
				if (onErrorListener != null) {
					onErrorListener.run();
					return true;
				} else {
					return false;
				}

			}
		});

		mMediaPlayer
				.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

					@Override
					public void onBufferingUpdate(MediaPlayer mp, int percent) {
						if (onBufferingUpdateListener != null) {
							onBufferingUpdateListener.run();
						}
					}
				});

		mMediaPlayer.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				if (onInfoListener != null) {
					onInfoListener.run();
					return true;
				} else {
					return false;
				}
			}
		});

		mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (onSeekCompleteListener != null) {
					onSeekCompleteListener.run();
				}
			}
		});

		phoneListener = new TelephoneCallStateListener();
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		// Notification notification = new Notification(R.drawable.stat_sample,
		// "Media Player Service has been killed", System.currentTimeMillis());
		// startForeground(R.string.local_service_stopped, notification);
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
	}

	public void onError(Runnable r) {
		onErrorListener = r;
	}

	public void onInfoListener(Runnable r) {
		onInfoListener = r;
	}

	public void onPrepared(Runnable r) {
		onPreparedListener = r;
	}

	public void onSeekComplete(Runnable r) {
		onSeekCompleteListener = r;
	}

	public void pause() {
		mMediaPlayer.pause();
		isPaused = true;
	}

	public void playSong(TracksStore.Track track)
			throws IllegalArgumentException, IllegalStateException, IOException {

		if (track != null) {
			if (soundcloudTrack == null) {
				soundcloudTrack = track;
				String url = track.getmStreamUrl();
				// TODO Ver porquŽ la url de la canci—n viene a null
				if (url != null) {
					// mMediaPlayer = getMediaPlayer();
					if ("private".equals(soundcloudTrack.getmSharing())
							&& "true".equals(track.getmStreamable())) {
						url = Soundroid.getSc().sign(url);
						// mMediaPlayer.reset();
						mMediaPlayer.setDataSource(url);
						mMediaPlayer.prepare();
						mMediaPlayer.start();
						showNotification(track.getmTitle());
					} else {
						showNotification("Track no streamable");
					}

				}

			} else {
				if (track.getmIdTrack() == soundcloudTrack.getmIdTrack()) {
					if (mMediaPlayer.isPlaying() == false) {

						if (isPaused) {
							mMediaPlayer.start();
						} else {
							this.soundcloudTrack = track;
							String url = track.getmStreamUrl();
							// mMediaPlayer = getMediaPlayer();
							// mMediaPlayer.reset();
							if ("private".equals(soundcloudTrack.getmSharing())) {
								url = Soundroid.getSc().sign(url);
							}
							mMediaPlayer.setDataSource(url);
							mMediaPlayer.prepare();
							mMediaPlayer.start();
							showNotification(track.getmTitle());
						}

					} else {
						mMediaPlayer.pause();
						isPaused = true;
					}
				} else {
					this.soundcloudTrack = track;
					String url = track.getmStreamUrl();
					mMediaPlayer.stop();
					// mMediaPlayer = getMediaPlayer();
					mMediaPlayer.reset();
					if ("private".equals(soundcloudTrack.getmSharing())) {
						url = Soundroid.getSc().sign(url);
					}
					mMediaPlayer.setDataSource(url);
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					showNotification(track.getmTitle());
				}
			}
		}

		/*
		 * if(track == null){ if(soundcloudTrack == null){
		 * 
		 * }else{ if(mMediaPlayer.isPlaying()){ mMediaPlayer.pause(); }else{
		 * 
		 * if(isPaused){ mMediaPlayer.start(); }else{ mMediaPlayer.reset();
		 * 
		 * String url = soundcloudTrack.getmStreamUrl();
		 * 
		 * if("private".equals(soundcloudTrack.getmSharing())){ url =
		 * Soundroid.getSc().sign(url); }
		 * 
		 * mMediaPlayer.setDataSource(url); mMediaPlayer.prepareAsync();
		 * mMediaPlayer.start(); } } }
		 * 
		 * }else{ //la primera vez String url = track.getmStreamUrl();
		 * 
		 * if("private".equals(track.getmSharing())){ url =
		 * Soundroid.getSc().sign(url); }
		 * 
		 * if(soundcloudTrack == null){ mMediaPlayer.setDataSource(url);
		 * mMediaPlayer.prepareAsync(); this.soundcloudTrack = track;
		 * playSong(track); showNotification(track.getmTitle());
		 * 
		 * }else{ if(soundcloudTrack.getmIdTrack() == track.getmIdTrack()){
		 * //estaba reproduciendose la cancion --> hay que pausarla
		 * if(mMediaPlayer.isPlaying()){ mMediaPlayer.pause(); }else{
		 * mMediaPlayer.start(); /* if(isPaused){ mMediaPlayer.reset();
		 * mMediaPlayer.setDataSource(url); mMediaPlayer.prepareAsync();
		 * this.soundcloudTrack = track; mMediaPlayer.start();
		 * showNotification(track.getmTitle()); }else{ mMediaPlayer.reset();
		 * mMediaPlayer.setDataSource(url); mMediaPlayer.prepareAsync();
		 * mMediaPlayer.start(); }
		 */
		/*
		 * } }else{ //reproduce nuevo tema if(mMediaPlayer.isPlaying()){
		 * stopSong(); playSong(track); soundcloudTrack = track; }else{
		 * if(!(soundcloudTrack.getmIdTrack() == track.getmIdTrack()) &&
		 * !isPaused){ //soundcloudTrack = null; mMediaPlayer.reset();
		 * mMediaPlayer.setDataSource(url); mMediaPlayer.prepareAsync();
		 * mMediaPlayer.start(); soundcloudTrack = track;
		 * 
		 * }else{ playSong(track); } } } } }
		 */
	}

	public void seekTo(int position) {
		// if(mMediaPlayer.isPlaying()){
		mMediaPlayer.seekTo(position);
		// }
	}

	public void setLooping(boolean looping) {
		mMediaPlayer.setLooping(looping);
	}

	public void setVolume(float leftVolume, float rightVolume) {
		mMediaPlayer.setVolume(leftVolume, rightVolume);
	}

	public void stopSong() throws IllegalArgumentException,
			IllegalStateException, IOException {
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			soundcloudTrack = null;
			hideNotification();
		} else {
			mMediaPlayer.reset();
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification(String text) {
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(
				android.R.drawable.ic_media_play, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent i = new Intent(this, PlayerActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putInt("idTrack", soundcloudTrack.getmIdTrack());
		i.putExtras(bundle);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, "Now playing...", text,
				contentIntent);

		// We show this for as long as our service is processing a command.
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(android.R.string.ok, notification);
	}

	private void hideNotification() {
		mNM.cancel(android.R.string.ok);
	}
}