package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksManager;
import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;
import com.siahmsoft.soundroid.sdk7.services.UploadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;



/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.
 */
public class TrackEditorActivity extends Activity {
    private Uri uriTrack;

    private Button fileUploadButton;
    private Button artworkUploadButton;
    private Button submitButton;

    private EditText mTitle;
    private EditText mTrackFile;
    private EditText mTrackArtwork;
    private EditText mDescription;
    private EditText mBpm;
    private EditText mGenre;
    private EditText mLabelName;
    private EditText mTags;

    private Spinner mTrackVisibility;
    private Spinner mTrackType;
    private Spinner mTrackLicense;

    private CheckBox mDownloadable;

    private String filePath;
    private String imagePath;

    Track track;

    boolean isEdit;

    private static final String TAG = "TrackEditorActivity";

    private UploadService mBoundUploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.track_editor);

        mBoundUploadService = Soundroid.getUploadService();

        isEdit = Intent.ACTION_EDIT.equals(getIntent().getAction());

        fileUploadButton = (Button) findViewById(R.id.track_browse_file);
        fileUploadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isMediaMounted()){
                    Intent intent = new Intent(TrackEditorActivity.this, MusicListActivity.class);
                    startActivityForResult(intent, 111);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(TrackEditorActivity.this);

                    builder.setMessage("You must insert the sd card to select a file");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TrackEditorActivity.this.finish();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        artworkUploadButton = (Button) findViewById(R.id.track_browse_file_artwork);
        artworkUploadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isMediaMounted()){
                    Intent intent = new Intent(TrackEditorActivity.this, ImageListActivity.class);
                    startActivityForResult(intent, ImageListActivity.REQUEST_CODE_OK_IMAGE);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(TrackEditorActivity.this);

                    builder.setMessage("You must insert the sd card to select a file");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TrackEditorActivity.this.finish();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        submitButton = (Button) findViewById(R.id.track_submit);

        submitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString();

                if(isEdit){
                    sendToUploadService();
                }else{
                    //Si viene a null o como cadena vacía, es que ha habido un error al seleccionar la canción
                    if(filePath == null || "".equals(filePath) || title == null || "".equals(title)){
                        // Whoops, unknown action!  Bail.
                        Log.e(TAG, "Path to track not valid, exiting");
                        finish();
                        return;
                    }else{
                        sendToUploadService();
                    }
                }
            }
        });

        // The text view for our note, identified by its ID in the XML file.
        mTrackFile = (EditText) findViewById(R.id.track_file);
        mTrackArtwork = (EditText) findViewById(R.id.track_artwork);
        mTitle = (EditText) findViewById(R.id.track_title);
        mDescription = (EditText) findViewById(R.id.track_description);
        mBpm = (EditText) findViewById(R.id.track_bpm);
        mGenre = (EditText) findViewById(R.id.track_genre);
        mLabelName = (EditText) findViewById(R.id.track_label);
        mTags = (EditText) findViewById(R.id.track_tags);
        mDownloadable = (CheckBox) findViewById(R.id.track_downloadable);

        mTrackVisibility = (Spinner)findViewById(R.id.track_visibility);
        mTrackType = (Spinner)findViewById(R.id.track_type);
        mTrackLicense = (Spinner)findViewById(R.id.track_license);

        //mTrackType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(isEdit){
            uriTrack = getIntent().getData();
            track = TracksManager.findTrack(getContentResolver(), uriTrack);

            mTitle.setText(track.getmTitle());
            mDescription.setText(track.getmDescription());
            mBpm.setText(String.valueOf(track.getmBpm()));
            mGenre.setText(track.getmGenre());
            mLabelName.setText(track.getmLabelName());
            mTags.setText(track.getmTagList());

            if("true".equals(track.getmDownloadable())){
                mDownloadable.setChecked(true);
            }else{
                mDownloadable.setChecked(false);
            }

            mTrackVisibility.setSelection(Soundroid.getTrackVisibilityMap().get(track.getmSharing()) == null? 0 : Soundroid.getTrackVisibilityMap().get(track.getmSharing()));
            mTrackType.setSelection(Soundroid.getTrackTypeMap().get(track.getmTrackType()) == null? 0 : Soundroid.getTrackTypeMap().get(track.getmTrackType()));
            mTrackLicense.setSelection(Soundroid.getTrackLicenseMap().get(track.getmLicense()) == null? 0 : Soundroid.getTrackLicenseMap().get(track.getmLicense()));
        }
    }

    private void sendToUploadService(){
        //Rellenar la canción con los datos de la pantalla

        Bundle extras = new Bundle();

        extras.putString(Track.ID_TRACK, isEdit?String.valueOf(track.getmIdTrack()) : "0");
        extras.putString(Track.TRACK_PATH, filePath);
        extras.putString(Track.ARTWORK_PATH, imagePath);
        extras.putString(Track.TITLE, mTitle.getText().toString());
        extras.putString(Track.TITLE, mTitle.getText().toString());
        extras.putString(Track.DESCRIPTION, mDescription.getText().toString());
        extras.putString(Track.BPM, mBpm.getText().toString());
        extras.putString(Track.GENRE, mGenre.getText().toString());
        extras.putString(Track.LABEL_NAME, mLabelName.getText().toString());
        extras.putString(Track.TAG_LIST, mTags.getText().toString());
        extras.putString(Track.DOWNLOADABLE, mDownloadable.isChecked()?"true":"false");

        extras.putString(Track.LICENSE, (String)mTrackLicense.getSelectedItem());
        extras.putString(Track.LICENSE_ID, String.valueOf(mTrackLicense.getSelectedItemId()));

        extras.putString(Track.SHARING, (String)mTrackVisibility.getSelectedItem());
        extras.putString(Track.SHARING_ID, String.valueOf(mTrackVisibility.getSelectedItemId()));

        extras.putString(Track.TRACK_TYPE, (String)mTrackType.getSelectedItem());
        extras.putString(Track.TRACK_TYPE_ID, String.valueOf(mTrackType.getSelectedItemId()));

        Intent i = new Intent();
        i.putExtras(extras);
        setResult(RESULT_OK, i);
        TrackEditorActivity.this.finish();
        //startService(new Intent(this, UploadService.class).putExtras(extras));
        //TrackEditorActivity.this.finish();

        //startService(new Intent(this, UploadService.class).putExtras(extras));
        /* mBoundUploadService.uploadTrack(extras);
        TrackEditorActivity.this.finish();*/

        //new UploadTrack().execute(extras); //FUNCIONA
        //TrackEditorActivity.this.finish();
    }

    private boolean isMediaMounted(){

        /*Here we should check another states as:
            MEDIA_NOFS,
            MEDIA_UNMOUNTABLE,
            MEDIA_BAD_REMOVAL,
            MEDIA_CHECKING,
            MEDIA_REMOVED

         */
        boolean res = false;
        res = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        return res;
    }

    //Es posible merjorar esto: la actividad que edita las canciones, cuando se le da a elegir archivo para subir, lo qu edebería hacer es
    //por una parte guardar en la base de datos la ruta del tema que se h aseleccionado, y por otra devolver el nombre del archivo seleccionado.
    //En esta activity se recoge el nombre seleccionado y se muestra en pantalla, pero cuando se pulsa en subir el archivo,
    //se recoge la ruta del archivo que se guardó anteriormente.
    //De esta forma se ahorran dos variables aquí, la del archivo de música y la del archivo de imagen.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //por si acaso alguna de las activities que lanzamos deben devolver algún valor

        if (data != null) {
            if (resultCode == RESULT_OK) {
                if("com.siahmsoft.soundroid.sdk7.MusicListActivity".equals(data.getComponent().getClassName())){
                    filePath = data.getExtras().getString("filePath");

                    mTrackFile.setText(data.getExtras().getString("fileName"));
                }else if("com.siahmsoft.soundroid.sdk7.ImageListActivity".equals(data.getComponent().getClassName())){
                    imagePath = data.getExtras().getString("filePath");

                    mTrackArtwork.setText(data.getExtras().getString("fileName"));
                }
            }
        }

    }

    private class UploadTrack extends AsyncTask<Bundle, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Bundle... params) {

            Bundle bundle = params[0];

            mBoundUploadService.uploadTrack(bundle);
            //TrackEditorActivity.this.finish();

            /* Soundroid.getSc().uploadTrack(bundle, new ResultListener<Track>() {

                @Override
                public void onTrackReceived(Track track) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSuccess(Track result) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onError(Exception e) {
                    // TODO Auto-generated method stub

                }
            });*/

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }
}