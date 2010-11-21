package com.siahmsoft.soundroid.sdk7.provider.oauth;

import java.io.IOException;
import java.io.InputStream;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpRequest;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public abstract class OauthStore {
	static final String LOG_TAG = "OauthStore";

	//Soundroid
	private OAuthConsumer consumer;

	//Soundcloud
	private OAuthProvider provider;

	private String mCallbackUrl;

	public OauthStore(OAuthConsumer consumer, OAuthProvider provider, String callBackUrl) {
		this.consumer = consumer;
		this.provider = provider;
		this.mCallbackUrl = callBackUrl;
	}

	public static class Oauth implements BaseColumns{
		public static final Uri CONTENT_URI = Uri.parse("content://com.siahmsoft.soundroid.provider.oauth.OauthProvider/oauths");

		public static final String APP_NAME = "appname";
		public static final String ACCESS_TOKEN = "accesstoken";
		public static final String SECRET_TOKEN = "secrettoken";

		public static final String DEFAULT_SORT_ORDER = APP_NAME + " ASC";

		String mAppName;
		String mAccessToken;
		String mSecretToken;


		public static Oauth fromCursor(Cursor c) {
			final Oauth oauth = new Oauth();

			oauth.mAppName = c.getString(c.getColumnIndexOrThrow(APP_NAME));
			oauth.mAccessToken = c.getString(c.getColumnIndexOrThrow(ACCESS_TOKEN));
			oauth.mSecretToken = c.getString(c.getColumnIndexOrThrow(SECRET_TOKEN));

			return oauth;
		}

		public ContentValues getContentValues() {
			final ContentValues values = new ContentValues();

			values.put(APP_NAME, mAppName);
			values.put(ACCESS_TOKEN, mAccessToken);
			values.put(SECRET_TOKEN, mSecretToken);

			return values;
		}

		public String getmAccessToken() {
			return mAccessToken;
		}

		public void setmAccessToken(String mAccessToken) {
			this.mAccessToken = mAccessToken;
		}

		public String getmSecretToken() {
			return mSecretToken;
		}

		public void setmSecretToken(String mSecretToken) {
			this.mSecretToken = mSecretToken;
		}

		public String getmAppName() {
			return mAppName;
		}

		public void setmAppName(String mAppName) {
			this.mAppName = mAppName;
		}
	}

	public String getUserAuthorizationUrl() {
		String loginUrl = null;

		try {
			//Soundcloud aún tiene OAuth 1.0, por eso se le pasa null
			//Si yo usara 1.0a se le tiene que pasar la url de callback
			loginUrl = provider.retrieveRequestToken(this.consumer, null);

			//Esta línea se tiene que borrar cuando se configure Oauth 1.0a
			loginUrl = loginUrl + mCallbackUrl;

			//String requestToken = loginUrl.substring(loginUrl.indexOf("=") + 1, loginUrl.indexOf("&"));
			return loginUrl;

		} catch (Exception e) {
			Log.e(LOG_TAG, "Error retrieving request token: ", e);
		}

		return null;
	}

	public void retrieveAccessToken(){
		try {
			//Se le pasa null porque uso Oauth 1.0 y Soundcloud es compatible aún.
			//Si fuera  1.0a se le tiene que pasar el pincode como en Twitter
			this.provider.retrieveAccessToken(this.consumer, null);

		} catch (Exception e) {
			Log.e(LOG_TAG, "Error retrieving access token: ", e);
			return ;
		}
	}

	public void setCredentials(String accessToken, String tokenSecret){
		consumer.setTokenWithSecret(accessToken, tokenSecret);
	}

	public HttpRequest sign(Object request){
		try {
			return consumer.sign(request);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error signing the request: " + request, e);
		}

		return null;
	}

	public HttpRequest sign(HttpRequest request){
		try {
			return consumer.sign(request);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error signing the request: " + request, e);
		}

		return null;
	}

	public String sign(String url){
		try {
			return consumer.sign(url);
		} catch (OAuthMessageSignerException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (OAuthExpectationFailedException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (OAuthCommunicationException e) {
			Log.e(LOG_TAG, e.getMessage());
		}

		return null;
	}

	/**
	 * Response handler used with {@link BooksStore#executeRequest(org.apache.http.HttpHost,
	 * org.apache.http.client.methods.HttpGet, BooksStore.ResponseHandler)}.
	 * The handler is invoked when a response is sent by the server. The response is made
	 * available as an input stream.
	 */
	static interface ResponseHandler {
		/**
		 * Processes the responses sent by the HTTP server following a GET request.
		 *
		 * @param in The stream containing the server's response.
		 *
		 * @throws java.io.IOException
		 */
		public void handleResponse(InputStream in) throws IOException;
	}

	public String getAccessToken() {
		return consumer.getToken();
	}

	public String getSecretToken() {
		return consumer.getTokenSecret();
	}
}