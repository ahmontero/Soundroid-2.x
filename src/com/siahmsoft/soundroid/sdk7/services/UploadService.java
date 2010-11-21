package com.siahmsoft.soundroid.sdk7.services;


import com.siahmsoft.soundroid.sdk7.MeActivity;
import com.siahmsoft.soundroid.sdk7.R;
import com.siahmsoft.soundroid.sdk7.Soundroid;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksManager;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.util.HttpManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class UploadService extends Service {

    private static final DefaultHttpClient sClient;

    static {
        sClient = HttpManager.newInstance();
    }

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private HandlerThread thread;
    static int totalUploads = 0;

    public static final String TAG = "com.siahmsoft.soundroid.services.UploadService.SERVICE";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle extras = (Bundle) msg.obj;
            uploadTrack(extras);
        }
    };

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class UploadServiceBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }

    private NotificationManager mNM;

    private final IBinder mBinder = new UploadServiceBinder();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.i(TAG, "Starting #" + startId + ": " + intent.getExtras());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent.getExtras();
        mServiceHandler.sendMessage(msg);
        Log.i(TAG, "Sending: " + msg);
    }

    public void deleteTrack(Uri uri){

        Track track = TracksManager.findTrack(getContentResolver(), uri);

        if(track != null){
            String id = uri.getLastPathSegment();
            int rowsDeleted = getContentResolver().delete(uri, null, null);

            if(rowsDeleted > 0){

                try {
                    final HttpDelete delete = new HttpDelete("http://api.soundcloud.com/me/tracks/" + track.getmIdTrack());
                    Soundroid.getSc().signRequest(delete);
                    HttpResponse response = HttpManager.newInstance().execute(delete);

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        showNotification("Track " + track.getmTitle() + " deleted");
                        Log.i(TAG, "Deleted " + rowsDeleted + " track with local id " + id + " and remote id " + track.getmIdTrack());

                    }else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
                        showNotification("Track " + track.getmTitle() + " not found");
                    }

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }else{
                showNotification("Track " + uri + " not found");
            }
        }
    }

    public void uploadTrack(Bundle bundle){

        Track track = TracksStore.Track.fromBundle(bundle);
        //SystemClock.sleep(1500);

        if(track.getmIdTrack() == 0){

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null , Charset.forName(HTTP.UTF_8));

            try {

                if(track.getmTrackPath() != null && !"".equals(track.getmTrackPath())){
                    ContentBody asset_Data =  new FileBody(new File(track.getmTrackPath()));
                    entity.addPart("track[asset_data]", asset_Data);
                }

                if(track.getmArtworkPath() != null && !"".equals(track.getmArtworkPath())){
                    ContentBody artworkData =  new FileBody(new File(track.getmArtworkPath()));
                    entity.addPart("track[artwork_data]", artworkData);
                }

                if(track.getmTitle() != null && !"".equals(track.getmTitle())){
                    entity.addPart("track[title]", new StringBody(track.getmTitle()));
                }

                if(track.getmDescription() != null && !"".equals(track.getmDescription())){
                    entity.addPart("track[description]", new StringBody(track.getmDescription()));
                }

                if(track.getmDownloadable() != null && !"".equals(track.getmDownloadable())){
                    entity.addPart("track[downloadable]", new StringBody(track.getmDownloadable()));
                }

                if(track.getmSharing() != null && !"".equals(track.getmSharing())){
                    entity.addPart("track[sharing]", new StringBody(track.getmSharing()));
                }

                if(!"".equals(track.getmBpm())){
                    entity.addPart("track[bpm]", new StringBody(String.valueOf(track.getmBpm())));
                }


                if(track.getmTagList() != null && !"".equals(track.getmTagList())){
                    entity.addPart("track[tag_list]", new StringBody(track.getmTagList()));
                }

                if(track.getmGenre() != null && !"".equals(track.getmGenre())){
                    entity.addPart("track[genre]", new StringBody(track.getmGenre()));
                }

                if(track.getmLicense() != null && !"".equals(track.getmLicense())){
                    entity.addPart("track[license]", new StringBody(track.getmLicense()));
                }

                if(track.getmLabelName() != null && !"".equals(track.getmLabelName())){
                    entity.addPart("track[label_name]", new StringBody(track.getmLabelName()));
                }

                if(track.getmTrackType() != null && !"".equals(track.getmTrackType())){
                    entity.addPart("track[track_type]", new StringBody(track.getmTrackType()));
                }

                HttpPost filePost = new HttpPost("http://api.soundcloud.com/tracks");
                Soundroid.getSc().signRequest(filePost);
                filePost.setEntity(entity);

                try {
                    //SystemClock.sleep(1000);
                    showNotification("Uploading track " + track.getmTitle());
                    final HttpResponse response = sClient.execute(filePost);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        showNotification("Track " + track.getmTitle() + " uploaded");
                        sClient.getConnectionManager().closeExpiredConnections();
                        totalUploads--;
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }catch(Exception e){

            }
        }else{//Edición de la canción

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null , Charset.forName(HTTP.UTF_8));
            try {

                if(track.getmTrackPath() != null && !"".equals(track.getmTrackPath())){
                    ContentBody asset_Data =  new FileBody(new File(track.getmTrackPath()));
                    entity.addPart("track[asset_data]", asset_Data);
                }

                if(track.getmArtworkPath() != null && !"".equals(track.getmArtworkPath())){
                    ContentBody artworkData =  new FileBody(new File(track.getmArtworkPath()));
                    entity.addPart("track[artwork_data]", artworkData);
                }

                if(track.getmTitle() != null){
                    entity.addPart("track[title]", new StringBody(track.getmTitle()));
                }

                if(track.getmDescription() != null){
                    entity.addPart("track[description]", new StringBody(track.getmDescription()));
                }

                if(track.getmDownloadable() != null){
                    entity.addPart("track[downloadable]", new StringBody(track.getmDownloadable()));
                }

                if(track.getmSharing() != null){
                    entity.addPart("track[sharing]", new StringBody(track.getmSharing()));
                }

                entity.addPart("track[bpm]", new StringBody(String.valueOf(track.getmBpm())));

                if(track.getmTagList() != null){
                    entity.addPart("track[tag_list]", new StringBody(track.getmTagList()));
                }

                if(track.getmGenre() != null){
                    entity.addPart("track[genre]", new StringBody(track.getmGenre()));
                }

                if(track.getmLicense() != null){
                    entity.addPart("track[license]", new StringBody(track.getmLicense()));
                }

                if(track.getmLabelName() != null){
                    entity.addPart("track[label_name]", new StringBody(track.getmLabelName()));
                }

                if(track.getmTrackType() != null){
                    entity.addPart("track[track_type]", new StringBody(track.getmTrackType()));
                }

                HttpPut filePut = new HttpPut("http://api.soundcloud.com/tracks/" + track.getmIdTrack() + ".json");
                Soundroid.getSc().signRequest(filePut);

                filePut.setEntity(entity);


                try {
                    //SystemClock.sleep(1000);
                    showNotification("Uploading track " + track.getmTitle());
                    final HttpResponse response = sClient.execute(filePut);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        showNotification("Track " + track.getmTitle() + " edited");
                        sClient.getConnectionManager().closeExpiredConnections();
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }catch(Exception e){

            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onDestroy() {

    }

    public void upload(Intent intent){
        totalUploads++;
        startService(intent);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String text) {
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.uploadtocloud, text, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        Intent i = new Intent(this, MeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /*Bundle bundle = new Bundle();
        bundle.putInt("idTrack", soundcloudTrack.getmIdTrack());
        i.putExtras(bundle);*/

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Uploading...", text, contentIntent);

        // We show this for as long as our service is processing a command.
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
        notification.number = totalUploads;

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(777, notification);
    }

    private void hideNotification() {
        mNM.cancel(android.R.string.ok);
    }
}