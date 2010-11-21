package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.provider.oauth.OauthStore;
import com.siahmsoft.soundroid.sdk7.provider.oauth.OauthsManager;
import com.siahmsoft.soundroid.sdk7.provider.oauth.SoundcloudOauth;
import com.siahmsoft.soundroid.sdk7.provider.oauth.OauthStore.Oauth;
import com.siahmsoft.soundroid.sdk7.provider.tracks.SoundcloudTracksStore.OnTokenRetrieved;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;

/**
 * Esta clase deberia mostrar una pantalla inicial con el logo de la aplicación y la versión. Debe haecr todo el baile Oauth.
 *
 * @author Antonio
 *
 */
public class LogoActivity extends Activity {

    private final static String TAG = "LogoActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.logo_activity);

        EditText splash = (EditText)findViewById(R.id.splash);
        splash.setEnabled(false);
        splash.setCursorVisible(false);
        splash.setSelected(false);

        EditText version = (EditText)findViewById(R.id.version);
        version.setEnabled(false);
        version.setCursorVisible(false);
        version.setSelected(false);
        version.setText("Version: " + Soundroid.getVersionString(this).split(":")[1]);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();

        if (intent.getData() == null) {
            intent.setData(Oauth.CONTENT_URI);
        }

        if(!SoundcloudOauth.CALLBACK_URL_SCHEMA.equals(intent.getScheme())){

            new FetchToken().execute();

        }else{
            //Si es un callback es porque el usuario acaba de permitir a la aplicación acceder a su cuenta de soundcloud
            //Por tanto tenemos que guardar el access token y el token secret para poderlo usar las siguientes veces

            new RetieveAndSaveToken().execute();
        }
    }

    private class FetchToken extends AsyncTask<Void, Integer, Oauth> {

        @Override
        protected OauthStore.Oauth doInBackground(Void... params) {
            Oauth oauth = null;
            if(OauthsManager.tokensExists(getContentResolver(), "Soundroid")){
                oauth = OauthsManager.findTokens(getContentResolver(), "Soundroid");
            }
            return oauth;
        }

        @Override
        protected void onPostExecute(Oauth token) {
            if(token == null){
                //hay que ir a por el request token
                Soundroid.getSc().retrieveUserAuthorizationUrl(new OnTokenRetrieved() {

                    @Override
                    public void handleResponse(String token) {

                        //se debe mostrar la página para que el usuario permita el acceso a la aplicación
                        //una vez aceptado el acceso, soundcloud devolverá un callback que debe ser interceptado por soundroid
                        //que lanzará de nuevo esta activity y permitirá recuperar el access token, token secret y demás.

                        Intent browserViewActivity = new Intent(getApplicationContext(), BrowserView.class);
                        browserViewActivity.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        browserViewActivity.setData(Uri.parse(token));
                        startActivity(browserViewActivity);
                        //LogoActivity.this.finish();???
                    }
                });
            }else{
                Soundroid.getSc().setCredentials(token.getmAccessToken(), token.getmSecretToken());
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
                LogoActivity.this.finish();
            }
        }
    }

    private class RetieveAndSaveToken extends AsyncTask<Void, Integer, OauthStore.Oauth> {

        OauthStore.Oauth tokenSaved;

        @Override
        protected OauthStore.Oauth doInBackground(Void... params) {

            Soundroid.getSc().retrieveAccessToken(new OnTokenRetrieved() {

                @Override
                public void handleResponse(String token) {


                    OauthStore.Oauth tokens = new OauthStore.Oauth();
                    tokens.setmAccessToken(token);
                    tokens.setmSecretToken(Soundroid.getSc().getSecretToken());
                    tokens.setmAppName("Soundroid");
                    tokenSaved = OauthsManager.saveToken(getContentResolver(), tokens);
                }
            });

            return tokenSaved;
        }

        @Override
        protected void onPostExecute(Oauth tokenSaved) {
            if(tokenSaved != null){
                Intent i = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(i);
                LogoActivity.this.finish();
            }else{
                Log.e(TAG, "Error al guardar el token");
                LogoActivity.this.finish();
            }
        }
    }
}