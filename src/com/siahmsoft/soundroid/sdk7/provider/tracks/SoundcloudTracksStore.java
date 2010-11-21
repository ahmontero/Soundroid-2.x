package com.siahmsoft.soundroid.sdk7.provider.tracks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import oauth.signpost.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.provider.oauth.SoundcloudOauth;
import com.siahmsoft.soundroid.sdk7.util.CookieStore;
import com.siahmsoft.soundroid.sdk7.util.ImageUtilities;

public class SoundcloudTracksStore extends TracksStore {
	private static final String API_REST_HOST = "api.soundcloud.com";
	private static final String API_TRACKS_REST_URL = "/tracks";
	private static final String API_DROPBOX_REST_URL = "http://api.soundcloud.com/events.json?filter=shared_to";

	private static final String API_ITEM_LOOKUP = "q";

	private static final String API_REQUEST_TYPE = ".json";

	private SoundcloudOauth soundcloudOauth;

	/*
	 * Offset defaults to 0 and limit defaults to 50, if a larger value for
	 * limit is passed it is set to 50. If you receive fewer items than you
	 * asked for you are at the end of the collection.
	 */
	private static final String PARAM_MAX_RESULTS = "limit";
	private static final String PARAM_START_INDEX = "offset";

	private static final String VALUE_MAX_RESULTS = "10";
	private static final String VALUE_START_INDEX = "0";

	private static final String RESPONSE_TAG_ID = "id";
	private static final String RESPONSE_TAG_USER_ID = "user_id";
	private static final String RESPONSE_TAG_PERMALINK = "permalink";
	private static final String RESPONSE_TAG_CREATED_AT = "created_at";
	private static final String RESPONSE_TAG_PLAYBACK_COUNT = "playback_count";
	private static final String RESPONSE_TAG_DOWNLOAD_COUNT = "download_count";
	private static final String RESPONSE_TAG_DURATION = "duration";
	private static final String RESPONSE_TAG_COMMENT_COUNT = "comment_count";
	private static final String RESPONSE_TAG_DESCRIPTION = "description";
	private static final String RESPONSE_TAG_BPM = "bpm";
	private static final String RESPONSE_TAG_STREAMABLE = "streamable";
	private static final String RESPONSE_TAG_DOWNLOADABLE = "downloadable";
	private static final String RESPONSE_TAG_GENRE = "genre";
	private static final String RESPONSE_TAG_RELEASE = "release";
	private static final String RESPONSE_TAG_RELEASE_YEAR = "release_year";
	private static final String RESPONSE_TAG_RELEASE_MONTH = "release_month";
	private static final String RESPONSE_TAG_RELEASE_DAY = "release_day";
	private static final String RESPONSE_TAG_ISRC = "isrc";
	private static final String RESPONSE_TAG_TAG_LIST = "tag_list";
	private static final String RESPONSE_TAG_LABEL_ID = "label_id";
	private static final String RESPONSE_TAG_LABEL_NAME = "label_name";
	private static final String RESPONSE_TAG_VIDEO_URL = "video_url";
	private static final String RESPONSE_TAG_TRACK_TYPE = "track_type";
	private static final String RESPONSE_TAG_KEY_SIGNATURE = "key_signature";
	private static final String RESPONSE_TAG_SHARING = "sharing";
	private static final String RESPONSE_TAG_TITLE = "title";
	private static final String RESPONSE_TAG_ORIGINAL_FORMAT = "original_format";
	private static final String RESPONSE_TAG_LICENSE = "license";
	private static final String RESPONSE_TAG_URI = "uri";
	private static final String RESPONSE_TAG_PERMALINK_URL = "permalink_url";
	private static final String RESPONSE_TAG_PURCHASE_URL = "purchase_url";
	private static final String RESPONSE_TAG_ARTWORK_URL = "artwork_url";
	private static final String RESPONSE_TAG_WAVEFORM_URL = "waveform_url";
	private static final String RESPONSE_TAG_USER = "user";
	private static final String RESPONSE_TAG_USER_URI = "uri";
	private static final String RESPONSE_TAG_USER_USERNAME = "username";
	private static final String RESPONSE_TAG_USER_PERMALINK = "permalink";
	private static final String RESPONSE_TAG_USER_PERMALINK_URL = "permalink_url";
	private static final String RESPONSE_TAG_STREAM_URL = "stream_url";

	private final ImageLoader mLoader;

	private static class SoundcloudImageLoader implements ImageLoader {
		public ImageUtilities.ExpiringBitmap load(String url) {
			final String cookie = CookieStore.get().getCookie(url);
			return ImageUtilities.load(url, cookie);
		}
	}

	public SoundcloudTracksStore() {
		super(API_REST_HOST);
		mLoader = new SoundcloudImageLoader();
		soundcloudOauth = new SoundcloudOauth(API_REST_HOST);
	}

	// TODO Mirar que se lo podria pasar en el primer argumento
	@Override
	Track createTrack() {
		return new Track("", mLoader);
	}

	@Override
	Uri buildFindTrackQuery(String id) {
		// http://api.soundcloud.com/tracks/481167.json
		return Uri.parse("http://api.soundcloud.com/tracks/" + id
				+ API_REQUEST_TYPE);
	}

	@Override
	Uri buildSearchTracksQuery(String query, int offset, int limit) {

		// http://api.soundcloud.com/tracks.json?q=quano&offset=0&limit=1

		String sOffset = offset == 0 ? VALUE_START_INDEX : String
				.valueOf(offset);
		String sLimit = limit == 0 ? VALUE_MAX_RESULTS : String.valueOf(limit);

		StringBuffer uri = new StringBuffer();
		uri.append("http://api.soundcloud.com/tracks" + API_REQUEST_TYPE + "?");
		uri.append(API_ITEM_LOOKUP + "=" + Uri.encode(query));
		uri.append("&" + PARAM_START_INDEX + "=" + sOffset);
		uri.append("&" + PARAM_MAX_RESULTS + "=" + sLimit);

		return Uri.parse(uri.toString());
	}

	@Override
	Uri buildDropBoxTracksQuery() {
		return Uri.parse("http://api.soundcloud.com/events" + API_REQUEST_TYPE
				+ "?filter=shared_to");
	}

	@Override
	Uri buildMeTracksQuery() {
		return Uri.parse("http://api.soundcloud.com/me/tracks"
				+ API_REQUEST_TYPE);
	}

	@Override
	void parseArrayResponse(InputStream in, ResponseParserArray responseParser)
			throws IOException {
		try {
			String result = convertStreamToString(in);
			responseParser.parseResponse(new JSONArray(result));
		} catch (JSONException e) {
			final IOException ioe = new IOException(
					"Could not parse the response");
			ioe.initCause(e);
			throw ioe;
		} catch (IOException e) {
			final IOException ioe = new IOException(
					"Could not parse the response");
			ioe.initCause(e);
			throw ioe;
		}
	}

	@Override
	void parseSingleResponse(InputStream in, ResponseParserSingle responseParser)
			throws IOException {
		try {
			String result = convertStreamToString(in);
			responseParser.parseResponse(new JSONObject(result));
		} catch (JSONException e) {
			final IOException ioe = new IOException(
					"Could not parse the response");
			ioe.initCause(e);
			throw ioe;
		} catch (IOException e) {
			final IOException ioe = new IOException(
					"Could not parse the response");
			ioe.initCause(e);
			throw ioe;
		}
	}

	@Override
	boolean parseTrack(JSONObject jsonTrack, Track track) throws JSONException {

		JSONObject jsonUser;
		// JSONObject jsonEvent;

		if (jsonTrack.has("type")) {
			// jsonEvent = jsonTrack.getJSONObject("event");
			jsonTrack = jsonTrack.getJSONObject("track");

		}

		if (jsonTrack.has(RESPONSE_TAG_USER)) {
			jsonUser = jsonTrack.getJSONObject(RESPONSE_TAG_USER);

			track.mUsername = jsonUser.getString(RESPONSE_TAG_USER_USERNAME);
			track.mUserPermalink = jsonUser
					.getString(RESPONSE_TAG_USER_PERMALINK);
			track.mUserPermalinkUrl = jsonUser
					.getString(RESPONSE_TAG_USER_PERMALINK_URL);
			track.mUserUri = jsonUser.getString(RESPONSE_TAG_USER_URI);
		}

		track.mArtworkPath = jsonTrack.getString(RESPONSE_TAG_ARTWORK_URL);
		track.mArtworkUrl = jsonTrack.getString(RESPONSE_TAG_ARTWORK_URL);
		track.mBpm = Float.valueOf("null".equals(jsonTrack
				.getString(RESPONSE_TAG_BPM)) ? "0.0" : jsonTrack
				.getString(RESPONSE_TAG_BPM));
		track.mCommentCount = jsonTrack.getInt(RESPONSE_TAG_COMMENT_COUNT);
		track.mCreatedAt = jsonTrack.getString(RESPONSE_TAG_CREATED_AT);
		track.mDescription = jsonTrack.getString(RESPONSE_TAG_DESCRIPTION);
		track.mDownloadable = jsonTrack.getString(RESPONSE_TAG_DOWNLOADABLE);
		track.mDownloadCount = jsonTrack.getInt(RESPONSE_TAG_DOWNLOAD_COUNT);
		track.mDuration = Integer.valueOf("null".equals(jsonTrack
				.getString(RESPONSE_TAG_DURATION)) ? "0" : jsonTrack
				.getString(RESPONSE_TAG_DURATION));
		track.mGenre = jsonTrack.getString(RESPONSE_TAG_GENRE);
		track.mIdTrack = Integer.valueOf("null".equals(jsonTrack
				.getString(RESPONSE_TAG_ID)) ? "0" : jsonTrack
				.getString(RESPONSE_TAG_ID));
		track.mIsrc = jsonTrack.getString(RESPONSE_TAG_ISRC);
		track.mKeySignature = jsonTrack.getString(RESPONSE_TAG_KEY_SIGNATURE);
		track.mLabelId = jsonTrack.getString(RESPONSE_TAG_LABEL_ID);
		track.mLabelName = jsonTrack.getString(RESPONSE_TAG_LABEL_NAME);
		track.mLicense = jsonTrack.getString(RESPONSE_TAG_LICENSE);
		track.mOriginalFormat = jsonTrack
				.getString(RESPONSE_TAG_ORIGINAL_FORMAT);
		track.mPermalink = jsonTrack.getString(RESPONSE_TAG_PERMALINK);
		track.mPermalinkUrl = jsonTrack.getString(RESPONSE_TAG_PERMALINK_URL);
		track.mPlaybackCount = Integer.valueOf("null".equals(jsonTrack
				.getString(RESPONSE_TAG_PLAYBACK_COUNT)) ? "0" : jsonTrack
				.getString(RESPONSE_TAG_PLAYBACK_COUNT));
		track.mPurchaseUrl = jsonTrack.getString(RESPONSE_TAG_PURCHASE_URL);
		track.mRelease = jsonTrack.getString(RESPONSE_TAG_RELEASE);
		track.mReleaseDay = jsonTrack.getString(RESPONSE_TAG_RELEASE_DAY);
		track.mReleaseMonth = jsonTrack.getString(RESPONSE_TAG_RELEASE_MONTH);
		track.mReleaseYear = jsonTrack.getString(RESPONSE_TAG_RELEASE_YEAR);
		track.mSharing = jsonTrack.getString(RESPONSE_TAG_SHARING);
		track.mStreamable = jsonTrack.getString(RESPONSE_TAG_STREAMABLE);

		// TODO ver que hacer cuando la canci—n no es streamable
		if ("true".equals(track.mStreamable)) {
			track.mStreamUrl = jsonTrack.getString(RESPONSE_TAG_STREAM_URL);
		}

		track.mTagList = jsonTrack.getString(RESPONSE_TAG_TAG_LIST);
		track.mTitle = jsonTrack.getString(RESPONSE_TAG_TITLE);
		track.mTrackType = jsonTrack.getString(RESPONSE_TAG_TRACK_TYPE);
		track.mUri = jsonTrack.getString(RESPONSE_TAG_URI);
		track.mUserId = jsonTrack.getString(RESPONSE_TAG_USER_ID);

		track.mVideoUrl = jsonTrack.getString(RESPONSE_TAG_VIDEO_URL);
		track.mWaveFormUrl = jsonTrack.getString(RESPONSE_TAG_WAVEFORM_URL);

		return track.mIdTrack > 0 && track.mUserId != null;
	}

	@Override
	String signRequest(String url) {
		return soundcloudOauth.sign(url);
	}

	@Override
	public HttpRequest signRequest(Object request) {
		return soundcloudOauth.sign(request);
	}

	public void retrieveAccessToken(OnTokenRetrieved handler) {
		soundcloudOauth.retrieveAccessToken();
		handler.handleResponse(soundcloudOauth.getAccessToken());
	}

	public void retrieveUserAuthorizationUrl(OnTokenRetrieved handler) {
		handler.handleResponse(soundcloudOauth.getUserAuthorizationUrl());
	}

	public void setCredentials(String accessToken, String secretToken) {
		soundcloudOauth.setCredentials(accessToken, secretToken);
	}

	public String getSecretToken() {
		return soundcloudOauth.getSecretToken();
	}

	/**
	 * Response handler used with
	 * {@link BooksStore#executeRequest(org.apache.http.HttpHost, org.apache.http.client.methods.HttpGet, BooksStore.ResponseHandler)}
	 * . The handler is invoked when a response is sent by the server. The
	 * response is made available as an input stream.
	 */
	public static interface OnTokenRetrieved {
		/**
		 * Processes the responses sent by the HTTP server following a GET
		 * request.
		 * 
		 * @param in
		 *            The stream containing the server's response.
		 * 
		 * @throws java.io.IOException
		 */
		public void handleResponse(String token);
	}

	@Override
	HttpRequest signRequest(HttpRequest request) {
		return soundcloudOauth.sign(request);
	}

	public String sign(String url) {
		return soundcloudOauth.sign(url);
	}

	@Override
	boolean parseAddtoFavoritesResponse(JSONObject parser, Track track)
			throws JSONException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void parseFavoritesResponse(InputStream in,
			ResponseAddToFavorites responseParser) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	void parseTracks(JSONArray JSONTracks, final ArrayList<Track> tracks,
			final ResultListener<ArrayList<Track>> listener) {
		try {
			for (int i = 0; i < JSONTracks.length(); i++) {

				JSONObject JSONTrack = null;
				JSONTrack = (JSONObject) JSONTracks.get(i);
				final Track track = createTrack();

				if (parseTrack(JSONTrack, track)) {
					tracks.add(track);
					listener.onTrackReceived(track);
				}
			}

			listener.onSuccess(tracks);

		} catch (Exception e) {
			android.util.Log.e(LOG_TAG, "Could not perform parse response", e);
			listener.onError(e);
		}
	}

	@Override
	void parseTrack(JSONObject JSONTrack, final Track track,
			ResultListener<Track> listener) {
		try {
			if (parseTrack(JSONTrack, track)) {
				listener.onSuccess(track);
			}

		} catch (Exception e) {
			android.util.Log.e(LOG_TAG, "Could not perform parse response", e);
			listener.onError(e);
		}
	}

	@Override
	Uri buildDeleteTrackQuery(String idTrackSoundcloud) {
		StringBuffer uri = new StringBuffer();
		uri.append("http://api.soundcloud.com/me/tracks/" + idTrackSoundcloud);

		return Uri.parse(uri.toString());
	}

	@Override
	Uri buildShareTrackQuery(String idTrack) {
		StringBuffer uri = new StringBuffer();
		uri.append("http://api.soundcloud.com/tracks/" + idTrack);

		return Uri.parse(uri.toString());
	}
}