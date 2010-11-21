package com.siahmsoft.soundroid.sdk7;

import com.siahmsoft.soundroid.sdk7.provider.tracks.SoundcloudTracksStore;
import com.siahmsoft.soundroid.sdk7.services.MediaPlayerService;
import com.siahmsoft.soundroid.sdk7.services.UploadService;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map.Entry;


public class Soundroid extends Application {

    public static final SoundcloudTracksStore sc;

    private static MediaPlayerService mBoundMediaPlayerService;
    private static UploadService mBoundUploadService;

    private boolean mMediaPlayerServiceIsBound;

    private static final String TAG = "Soundroid";
    public static final String PACKAGE_NAME = "com.siahmsoft.soundroid.sdk7";


    private static HashMap<String, Integer> trackLicenseMap;
    private static HashMap<String, Integer> trackTypeMap;
    private static HashMap<String, Integer> trackVisibilityMap;

    static {
        sc = new SoundcloudTracksStore();

        //temporalmente para no tener que hacer el baile oauth cada vez que arranque la aplicación
        //sc.setCredentials("ippZ2FvBombd3FaO8wvIg", "GyBJmRHajdC4uP7YMZbsl2piDwIDsINZjZbo9NaQs");

        trackLicenseMap = new HashMap<String, Integer>();
        trackLicenseMap.put("no-rights-reserved", 0);
        trackLicenseMap.put("all-rights-reserved", 1);
        trackLicenseMap.put("cc-by", 2);
        trackLicenseMap.put("cc-by-nc", 3);
        trackLicenseMap.put("cc-by-nd", 4);
        trackLicenseMap.put("cc-by-sa", 5);
        trackLicenseMap.put("cc-by-nc-nd", 6);
        trackLicenseMap.put("cc-by-nc-sa", 7);

        trackTypeMap = new HashMap<String, Integer>();
        trackTypeMap.put("cover", 0);
        trackTypeMap.put("demo", 1);
        trackTypeMap.put("djset", 2);
        trackTypeMap.put("in progress", 3);
        trackTypeMap.put("live", 4);
        trackTypeMap.put("loop", 5);
        trackTypeMap.put("mashup", 6);
        trackTypeMap.put("original", 7);
        trackTypeMap.put("part", 8);
        trackTypeMap.put("podcast", 9);
        trackTypeMap.put("reedit", 10);
        trackTypeMap.put("remix", 11);
        trackTypeMap.put("sample", 12);

        trackVisibilityMap = new HashMap<String, Integer>();
        trackVisibilityMap.put("public", 0);
        trackVisibilityMap.put("private", 1);
    }

    private ServiceConnection mConnectionMediaPlayerService  = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundMediaPlayerService = ((MediaPlayerService.MediaPlayerBinder)service).getService();

            // Tell the user about this for our demo.
            //Toast.makeText(Soundroid.this, "Service Connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundMediaPlayerService = null;

            //Toast.makeText(Soundroid.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private ServiceConnection mConnectionUploadService  = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundUploadService = ((UploadService.UploadServiceBinder)service).getService();

            // Tell the user about this for our demo.
            // Toast.makeText(Soundroid.this, "Service Connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundUploadService = null;

            //Toast.makeText(Soundroid.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();

        mMediaPlayerServiceIsBound = bindService(new Intent(Soundroid.this, MediaPlayerService.class), mConnectionMediaPlayerService, Context.BIND_AUTO_CREATE);
        bindService(new Intent(Soundroid.this, UploadService.class), mConnectionUploadService, Context.BIND_AUTO_CREATE);
    }

    public static SoundcloudTracksStore getSc(){
        return sc;
    }

    public static MediaPlayerService getMediaPlayerService(){
        return mBoundMediaPlayerService;
    }

    public static UploadService getUploadService(){
        return mBoundUploadService;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (mMediaPlayerServiceIsBound) {
            // Detach our existing connection.
            unbindService(mConnectionMediaPlayerService);
            mConnectionMediaPlayerService = null;
            mMediaPlayerServiceIsBound = false;

            unbindService(mConnectionUploadService);
            mConnectionUploadService = null;
        }
    }

    /**
     * Constructs the version string of the application.
     *
     * @param context the context to use for getting package info
     * @return the versions string of the application
     */
    public static String getVersionString(Context context) {
        // Get a version string for the app.
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(PACKAGE_NAME, 0);
            return PACKAGE_NAME + ":" + String.valueOf(pi.versionCode);
        } catch (NameNotFoundException e) {

            Log.d(TAG, "Could not retrieve package info", e);

            throw new RuntimeException(e);
        }
    }

    public static String getTrackLicenseValue(String index) {
        if(index == null) {
            return null;
        }

        for(Entry<String, Integer> entry : trackLicenseMap.entrySet()){
            if(entry.getValue() == Integer.valueOf(index)){
                return entry.getKey();
            }
        }
        return null;
    }

    public static HashMap<String, Integer> getTrackLicenseMap() {
        return trackLicenseMap;
    }

    public static HashMap<String, Integer> getTrackTypeMap() {
        return trackTypeMap;
    }

    public static String getTrackTypeValue(String index) {
        if(index == null) {
            return null;
        }

        for(Entry<String, Integer> entry : trackTypeMap.entrySet()){
            if(entry.getValue() == Integer.valueOf(index)){
                return entry.getKey();
            }
        }
        return null;
    }

    public static HashMap<String, Integer> getTrackVisibilityMap() {
        return trackVisibilityMap;
    }

    public static String getTrackVisibilityValue(String index) {
        if(index == null) {
            return null;
        }

        for(Entry<String, Integer> entry : trackVisibilityMap.entrySet()){
            if(entry.getValue() == Integer.valueOf(index)){
                return entry.getKey();
            }
        }
        return null;
    }
}