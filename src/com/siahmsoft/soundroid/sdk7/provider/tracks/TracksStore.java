package com.siahmsoft.soundroid.sdk7.provider.tracks;

import com.siahmsoft.soundroid.sdk7.Soundroid;
import com.siahmsoft.soundroid.sdk7.async.ResultListener;
import com.siahmsoft.soundroid.sdk7.services.UploadService;
import com.siahmsoft.soundroid.sdk7.util.HttpManager;
import com.siahmsoft.soundroid.sdk7.util.ImageUtilities;

import oauth.signpost.http.HttpRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.BaseColumns;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TracksStore {
    static final String LOG_TAG = "TracksStore";

    protected final String mHost;

    UploadService mBoundUploadService;

    public enum ImageSize {
        // SWATCH,
        // SMALL,
        THUMBNAIL,
        TINY,
        // MEDIUM,
        // LARGE
    }

    public TracksStore(String mHost) {
        this.mHost = mHost;
    }

    public static class Comment {
        private int mId;
        private String mBody;
        private String mTimeStamp;
        private int mUserId;
        private int mTrackId;
        private String mCreatedAt;
        private String mUri;

        public int getmId() {
            return mId;
        }
        public String getmBody() {
            return mBody;
        }
        public String getmTimeStamp() {
            return mTimeStamp;
        }
        public int getmUserId() {
            return mUserId;
        }
        public int getmTrackId() {
            return mTrackId;
        }
        public String getmCreatedAt() {
            return mCreatedAt;
        }
        public String getmUri() {
            return mUri;
        }
    }

    public static class Track implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://com.siahmsoft.soundroid.provider.track.TrackProvider/tracks");

        public static final String ARTWORK_PATH = "artworkpath";
        public static final String ARTWORK_URL = "artworkurl";
        public static final String BPM = "bpm";
        public static final String COMMENT_COUNT = "commentcount";
        public static final String CREATED_AT = "createdat";
        public static final String DESCRIPTION = "description";
        public static final String DOWNLOAD_COUNT = "downloadcount";
        public static final String DOWNLOADABLE ="downloadable";
        public static final String DURATION = "duration";
        public static final String GENRE = "genre";
        public static final String ID_TRACK = "idtrack";
        public static final String ISRC = "isrc";
        public static final String KEY_SIGNATURE = "keysignature";
        public static final String LABEL_ID = "labelid";
        public static final String LABEL_NAME = "labelname";
        public static final String LICENSE = "license";
        public static final String LICENSE_ID = "licenseid";
        public static final String ORIGINAL_FORMAT = "originalformat";
        public static final String PERMALINK = "permalink";
        public static final String PERMALINK_URL = "permalinkurl";
        public static final String PLAYBACK_COUNT = "playbackcount";
        public static final String PURCHASE_URL = "purchaseurl";
        public static final String RELEASE = "release";
        public static final String RELEASE_DAY = "releaseday";
        public static final String RELEASE_MONTH = "releasemonth";
        public static final String RELEASE_YEAR = "releaseyear";
        public static final String SHARING = "sharing";
        public static final String SHARING_ID = "sharingid";
        public static final String STREAM_URL = "streamurl";
        public static final String STREAMABLE = "streamable";
        public static final String TAG_LIST = "taglist";
        public static final String TITLE = "title";
        public static final String TRACK_PATH = "trackpath";
        public static final String TRACK_TYPE = "tracktype";
        public static final String TRACK_TYPE_ID = "tracktypeid";
        public static final String UPLOAD = "upload";
        public static final String URI = "uri";
        public static final String USER_FAVORITE = "userfavorite";
        public static final String USER_ID = "userid";
        public static final String USER_NAME = "username";
        public static final String USER_PERMALINK = "userpermalink";
        public static final String USER_PERMALINK_URL = "userpermalinkurl";
        public static final String USER_PLAYBACK_COUNT = "userplaybackcount";
        public static final String USER_URI = "useruri";
        public static final String VIDEO_URL = "videourl";
        public static final String WAVEFORM_URL = "waveformurl";

        public static final String LAST_MODIFIED = "last_modified";

        public static final String DEFAULT_SORT_ORDER = PLAYBACK_COUNT + " DESC";
        //		public static final String DEFAULT_SORT_ORDER = PLAYBACK_COUNT + " DESC, " + DOWNLOAD_COUNT + " DESC";

        String mWaveFormUrl;
        String mVideoUrl;
        String mUserUri;
        int    mUserPlaybackCount;
        String mUserPermalinkUrl;
        String mUserPermalink;
        String mUsername;
        String mUserId;
        String mUserFavorite;
        String mUri;
        String mUpload;
        String mTrackTypeId;
        String mTrackType;
        String mTrackPath;
        String mTitle;
        String mTagList;
        String mStreamable;
        String mArtworkPath;
        String mArtworkUrl;
        float  mBpm;
        int    mCommentCount;
        String mCreatedAt;
        String mDescription;
        int    mDownloadCount;
        String mDownloadable;
        int    mDuration;
        String mGenre;
        int    mIdTrack;
        String mIsrc;
        String mKeySignature;
        String mLabelId;
        String mLabelName;
        String mLicense;
        String mLicenseId;
        String mOriginalFormat;
        String mPermalink;
        String mPermalinkUrl;
        int    mPlaybackCount;
        String mPurchaseUrl;
        String mRelease;
        String mReleaseDay;
        String mReleaseMonth;
        String mReleaseYear;
        String mSharing;
        String mSharingId;
        String mStreamUrl;

        List<Comment> mComments;

        private String mStorePrefix;
        private ImageLoader mLoader;
        Map<ImageSize, String> mImages;
        Calendar mLastModified;

        Track() {
            this("", null);
        }

        Track(String storePrefix, ImageLoader loader){
            mStorePrefix = storePrefix;
            mLoader = loader;
            mImages = new HashMap<ImageSize, String>(6);
            mComments = new ArrayList<Comment>();
        }

        public Bitmap loadCover(ImageSize size) {
            final String url = mImages.get(size);
            if (url == null) {
                return null;
            }

            final ImageUtilities.ExpiringBitmap expiring;
            if (mLoader == null) {
                expiring = ImageUtilities.load(url);
            } else {
                expiring = mLoader.load(url);
            }
            mLastModified = expiring.lastModified;

            return expiring.bitmap;
        }
        public static Track fromCursor(Cursor c) {
            final Track track = new Track();

            track.mArtworkPath = c.getString(c.getColumnIndexOrThrow(ARTWORK_PATH));
            track.mArtworkUrl = c.getString(c.getColumnIndexOrThrow(ARTWORK_URL));
            track.mBpm = c.getInt(c.getColumnIndexOrThrow(BPM));
            track.mCommentCount = c.getInt(c.getColumnIndexOrThrow(COMMENT_COUNT));
            track.mCreatedAt = c.getString(c.getColumnIndexOrThrow(CREATED_AT));
            track.mDescription = c.getString(c.getColumnIndexOrThrow(DESCRIPTION));
            track.mDownloadable = c.getString(c.getColumnIndexOrThrow(DOWNLOADABLE));
            track.mDownloadCount = c.getInt(c.getColumnIndexOrThrow(DOWNLOAD_COUNT));
            track.mDuration = c.getInt(c.getColumnIndexOrThrow(DURATION));
            track.mGenre = c.getString(c.getColumnIndexOrThrow(GENRE));
            track.mIdTrack = c.getInt(c.getColumnIndexOrThrow(ID_TRACK));
            track.mIsrc = c.getString(c.getColumnIndexOrThrow(ISRC));
            track.mKeySignature = c.getString(c.getColumnIndexOrThrow(KEY_SIGNATURE));
            track.mLabelId = c.getString(c.getColumnIndexOrThrow(LABEL_ID));
            track.mLabelName = c.getString(c.getColumnIndexOrThrow(LABEL_NAME));
            track.mLicense = c.getString(c.getColumnIndexOrThrow(LICENSE));
            track.mLicenseId = c.getString(c.getColumnIndexOrThrow(LICENSE_ID));
            track.mOriginalFormat = c.getString(c.getColumnIndexOrThrow(ORIGINAL_FORMAT));
            track.mPermalink = c.getString(c.getColumnIndexOrThrow(PERMALINK));
            track.mPermalinkUrl = c.getString(c.getColumnIndexOrThrow(PERMALINK_URL));
            track.mPlaybackCount = c.getInt(c.getColumnIndexOrThrow(PLAYBACK_COUNT));
            track.mPurchaseUrl = c.getString(c.getColumnIndexOrThrow(PURCHASE_URL));
            track.mRelease = c.getString(c.getColumnIndexOrThrow(RELEASE));
            track.mReleaseDay = c.getString(c.getColumnIndexOrThrow(RELEASE_DAY));
            track.mReleaseMonth = c.getString(c.getColumnIndexOrThrow(RELEASE_MONTH));
            track.mReleaseYear = c.getString(c.getColumnIndexOrThrow(RELEASE_YEAR));
            track.mSharing = c.getString(c.getColumnIndexOrThrow(SHARING));
            track.mSharingId = c.getString(c.getColumnIndexOrThrow(SHARING_ID));
            track.mStreamable = c.getString(c.getColumnIndexOrThrow(STREAMABLE));
            track.mStreamUrl = c.getString(c.getColumnIndexOrThrow(STREAM_URL));
            track.mTagList = c.getString(c.getColumnIndexOrThrow(TAG_LIST));
            track.mTitle = c.getString(c.getColumnIndexOrThrow(TITLE));
            track.mTrackPath = c.getString(c.getColumnIndexOrThrow(TRACK_PATH));
            track.mTrackType = c.getString(c.getColumnIndexOrThrow(TRACK_TYPE));
            track.mTrackTypeId = c.getString(c.getColumnIndexOrThrow(TRACK_TYPE_ID));
            track.mUpload = c.getString(c.getColumnIndexOrThrow(UPLOAD));
            track.mUri = c.getString(c.getColumnIndexOrThrow(URI));
            track.mUserFavorite = c.getString(c.getColumnIndexOrThrow(USER_FAVORITE));
            track.mUserId = c.getString(c.getColumnIndexOrThrow(USER_ID));
            track.mUsername = c.getString(c.getColumnIndexOrThrow(USER_NAME));
            track.mUserPermalink = c.getString(c.getColumnIndexOrThrow(USER_PERMALINK));
            track.mUserPermalinkUrl = c.getString(c.getColumnIndexOrThrow(USER_PERMALINK_URL));
            track.mUserPlaybackCount = c.getInt(c.getColumnIndexOrThrow(USER_PLAYBACK_COUNT));
            track.mUserUri = c.getString(c.getColumnIndexOrThrow(USER_URI));
            track.mVideoUrl = c.getString(c.getColumnIndexOrThrow(VIDEO_URL));
            track.mWaveFormUrl = c.getString(c.getColumnIndexOrThrow(WAVEFORM_URL));

            final Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow(LAST_MODIFIED)));
            track.mLastModified = calendar;

            return track;
        }

        public static Track fromMap(HashMap<String, String> c) {
            final Track track = new Track();

            track.mArtworkPath = c.get(ARTWORK_PATH);
            track.mArtworkUrl = c.get(ARTWORK_URL);
            track.mBpm = Float.parseFloat(c.get(BPM)== null || "".equals(c.get(BPM)) ?"0.0":c.get(BPM));
            track.mCommentCount = Integer.parseInt(c.get(COMMENT_COUNT)== null?"0":c.get(COMMENT_COUNT));
            track.mCreatedAt = c.get(CREATED_AT);
            track.mDescription = c.get(DESCRIPTION);
            track.mDownloadable = c.get(DOWNLOADABLE);
            track.mDownloadCount = Integer.parseInt(c.get(DOWNLOAD_COUNT)== null || "".equals(c.get(DOWNLOAD_COUNT))?"0":c.get(DOWNLOAD_COUNT));
            track.mDuration = Integer.parseInt(c.get(DURATION)== null || "".equals(c.get(DURATION))?"0":c.get(DURATION));
            track.mGenre = c.get(GENRE);
            track.mIdTrack = Integer.parseInt(c.get(ID_TRACK)== null || "".equals(c.get(ID_TRACK))?"0":c.get(ID_TRACK));
            track.mIsrc = c.get(ISRC);
            track.mKeySignature = c.get(KEY_SIGNATURE);
            track.mLabelId = c.get(LABEL_ID);
            track.mLabelName = c.get(LABEL_NAME);
            track.mOriginalFormat = c.get(ORIGINAL_FORMAT);
            track.mPermalink = c.get(PERMALINK);
            track.mPermalinkUrl = c.get(PERMALINK_URL);
            track.mPlaybackCount = Integer.parseInt(c.get(PLAYBACK_COUNT) == null || "".equals(c.get(PLAYBACK_COUNT))?"0":c.get(PLAYBACK_COUNT));
            track.mPurchaseUrl = c.get(PURCHASE_URL);
            track.mRelease = c.get(RELEASE);
            track.mReleaseDay = c.get(RELEASE_DAY);
            track.mReleaseMonth = c.get(RELEASE_MONTH);
            track.mReleaseYear = c.get(RELEASE_YEAR);
            track.mStreamable = c.get(STREAMABLE);
            track.mStreamUrl = c.get(STREAM_URL);
            track.mTagList = c.get(TAG_LIST);
            track.mTitle = c.get(TITLE);
            track.mTrackPath = c.get(TRACK_PATH);
            track.mUpload = c.get(UPLOAD);
            track.mUri = c.get(URI);
            track.mUserFavorite = c.get(USER_FAVORITE);
            track.mUserId = c.get(USER_ID);
            track.mUsername = c.get(USER_NAME);
            track.mUserPermalink = c.get(USER_PERMALINK);
            track.mUserPermalinkUrl = c.get(USER_PERMALINK_URL);
            track.mUserPlaybackCount = Integer.parseInt(c.get(USER_PLAYBACK_COUNT)== null || "".equals(c.get(USER_PLAYBACK_COUNT))?"0":c.get(USER_PLAYBACK_COUNT));
            track.mUserUri = c.get(USER_URI);
            track.mVideoUrl = c.get(VIDEO_URL);
            track.mWaveFormUrl = c.get(WAVEFORM_URL);

            track.mLicenseId = c.get(LICENSE_ID);
            track.mLicense = Soundroid.getTrackLicenseValue(track.mLicenseId);

            track.mSharingId = c.get(SHARING_ID);
            track.mSharing = Soundroid.getTrackVisibilityValue(track.mSharingId);

            track.mTrackTypeId = c.get(TRACK_TYPE_ID);
            track.mTrackType = Soundroid.getTrackTypeValue(track.mTrackTypeId);

            return track;
        }

        public static Track fromBundle(Bundle extras) {
            final Track track = new Track();

            track.mArtworkPath = extras.getString(ARTWORK_PATH);
            track.mArtworkUrl = extras.getString(ARTWORK_URL);
            track.mBpm = Float.parseFloat(extras.getString(BPM)== null || "".equals(extras.getString(BPM)) ?"0.0":extras.getString(BPM));
            track.mCommentCount = Integer.parseInt(extras.getString(COMMENT_COUNT)== null?"0":extras.getString(COMMENT_COUNT));
            track.mCreatedAt = extras.getString(CREATED_AT);
            track.mDescription = extras.getString(DESCRIPTION);
            track.mDownloadable = extras.getString(DOWNLOADABLE);
            track.mDownloadCount = Integer.parseInt(extras.getString(DOWNLOAD_COUNT)== null || "".equals(extras.getString(DOWNLOAD_COUNT))?"0":extras.getString(DOWNLOAD_COUNT));
            track.mDuration = Integer.parseInt(extras.getString(DURATION)== null || "".equals(extras.getString(DURATION))?"0":extras.getString(DURATION));
            track.mGenre = extras.getString(GENRE);
            track.mIdTrack = Integer.parseInt(extras.getString(ID_TRACK)== null || "".equals(extras.getString(ID_TRACK))?"0":extras.getString(ID_TRACK));
            track.mIsrc = extras.getString(ISRC);
            track.mKeySignature = extras.getString(KEY_SIGNATURE);
            track.mLabelId = extras.getString(LABEL_ID);
            track.mLabelName = extras.getString(LABEL_NAME);
            track.mOriginalFormat = extras.getString(ORIGINAL_FORMAT);
            track.mPermalink = extras.getString(PERMALINK);
            track.mPermalinkUrl = extras.getString(PERMALINK_URL);
            track.mPlaybackCount = Integer.parseInt(extras.getString(PLAYBACK_COUNT) == null || "".equals(extras.getString(PLAYBACK_COUNT))?"0":extras.getString(PLAYBACK_COUNT));
            track.mPurchaseUrl = extras.getString(PURCHASE_URL);
            track.mRelease = extras.getString(RELEASE);
            track.mReleaseDay = extras.getString(RELEASE_DAY);
            track.mReleaseMonth = extras.getString(RELEASE_MONTH);
            track.mReleaseYear = extras.getString(RELEASE_YEAR);
            track.mStreamable = extras.getString(STREAMABLE);
            track.mStreamUrl = extras.getString(STREAM_URL);
            track.mTagList = extras.getString(TAG_LIST);
            track.mTitle = extras.getString(TITLE);
            track.mTrackPath = extras.getString(TRACK_PATH);
            track.mUpload = extras.getString(UPLOAD);
            track.mUri = extras.getString(URI);
            track.mUserFavorite = extras.getString(USER_FAVORITE);
            track.mUserId = extras.getString(USER_ID);
            track.mUsername = extras.getString(USER_NAME);
            track.mUserPermalink = extras.getString(USER_PERMALINK);
            track.mUserPermalinkUrl = extras.getString(USER_PERMALINK_URL);
            track.mUserPlaybackCount = Integer.parseInt(extras.getString(USER_PLAYBACK_COUNT)== null || "".equals(extras.getString(USER_PLAYBACK_COUNT))?"0":extras.getString(USER_PLAYBACK_COUNT));
            track.mUserUri = extras.getString(USER_URI);
            track.mVideoUrl = extras.getString(VIDEO_URL);
            track.mWaveFormUrl = extras.getString(WAVEFORM_URL);

            track.mLicenseId = extras.getString(LICENSE_ID);
            track.mLicense = Soundroid.getTrackLicenseValue(track.mLicenseId);

            track.mSharingId = extras.getString(SHARING_ID);
            track.mSharing = Soundroid.getTrackVisibilityValue(track.mSharingId);

            track.mTrackTypeId = extras.getString(TRACK_TYPE_ID);
            track.mTrackType = Soundroid.getTrackTypeValue(track.mTrackTypeId);

            return track;
        }

        public ContentValues getContentValues() {
            //final SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy");
            final ContentValues values = new ContentValues();

            values.put(ARTWORK_PATH, mArtworkPath);
            values.put(ARTWORK_URL, mArtworkUrl);
            values.put(BPM, mBpm);
            values.put(COMMENT_COUNT, mCommentCount);
            values.put(CREATED_AT, mCreatedAt);
            values.put(DESCRIPTION, mDescription);
            values.put(DOWNLOADABLE, mDownloadable);
            values.put(DOWNLOAD_COUNT, mDownloadCount);
            values.put(DURATION, mDuration);
            values.put(GENRE, mGenre);
            values.put(ID_TRACK, mIdTrack);
            values.put(ISRC, mIsrc);
            values.put(KEY_SIGNATURE, mKeySignature);
            values.put(LABEL_ID, mLabelId);
            values.put(LABEL_NAME, mLabelName);
            values.put(LICENSE, mLicense);
            values.put(LICENSE_ID, mLicenseId);
            values.put(ORIGINAL_FORMAT, mOriginalFormat);
            values.put(PERMALINK, mPermalink);
            values.put(PERMALINK_URL, mPermalinkUrl);
            values.put(PLAYBACK_COUNT, mPlaybackCount);
            values.put(PURCHASE_URL, mPurchaseUrl);
            values.put(RELEASE, mRelease);
            values.put(RELEASE_DAY, mReleaseDay);
            values.put(RELEASE_MONTH, mReleaseMonth);
            values.put(RELEASE_YEAR, mReleaseYear);
            values.put(SHARING, mSharing);
            values.put(SHARING_ID, mSharingId);
            values.put(STREAMABLE, mStreamable);
            values.put(STREAM_URL, mStreamUrl);
            values.put(TAG_LIST, mTagList);
            values.put(TITLE, mTitle);
            values.put(TRACK_PATH, mTrackPath);
            values.put(TRACK_TYPE, mTrackType);
            values.put(TRACK_TYPE_ID, mTrackTypeId);
            values.put(UPLOAD, mUpload);
            values.put(URI, mUri);
            values.put(USER_FAVORITE, mUserFavorite);
            values.put(USER_ID, mUserId);
            values.put(USER_NAME, mUsername);
            values.put(USER_PERMALINK, mUserPermalink);
            values.put(USER_PERMALINK_URL, mUserPermalinkUrl);
            values.put(USER_PLAYBACK_COUNT, mUserPlaybackCount);
            values.put(USER_URI, mUserUri);
            values.put(VIDEO_URL, mVideoUrl);
            values.put(WAVEFORM_URL, mWaveFormUrl);

            if (mLastModified != null) {
                values.put(LAST_MODIFIED, mLastModified.getTimeInMillis());
            }

            return values;
        }

        public HashMap<String, String> getMapValues() {
            //final SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy");
            final HashMap<String, String> values = new HashMap<String, String>();

            values.put(ARTWORK_PATH, mArtworkPath);
            values.put(ARTWORK_URL, mArtworkUrl);
            values.put(BPM, String.valueOf(mBpm));
            values.put(COMMENT_COUNT, String.valueOf(mCommentCount));
            values.put(CREATED_AT, mCreatedAt);
            values.put(DESCRIPTION, mDescription);
            values.put(DOWNLOADABLE, mDownloadable);
            values.put(DOWNLOAD_COUNT, String.valueOf(mDownloadCount));
            values.put(DURATION, String.valueOf(mDuration));
            values.put(GENRE, mGenre);
            values.put(ID_TRACK, String.valueOf(mIdTrack));
            values.put(ISRC, mIsrc);
            values.put(KEY_SIGNATURE, mKeySignature);
            values.put(LABEL_ID, mLabelId);
            values.put(LABEL_NAME, mLabelName);
            values.put(LICENSE, mLicense);
            values.put(LICENSE_ID, mLicenseId);
            values.put(ORIGINAL_FORMAT, mOriginalFormat);
            values.put(PERMALINK, mPermalink);
            values.put(PERMALINK_URL, mPermalinkUrl);
            values.put(PLAYBACK_COUNT, String.valueOf(mPlaybackCount));
            values.put(PURCHASE_URL, mPurchaseUrl);
            values.put(RELEASE, mRelease);
            values.put(RELEASE_DAY, mReleaseDay);
            values.put(RELEASE_MONTH, mReleaseMonth);
            values.put(RELEASE_YEAR, mReleaseYear);
            values.put(SHARING, mSharing);
            values.put(SHARING_ID, mSharingId);
            values.put(STREAMABLE, mStreamable);
            values.put(STREAM_URL, mStreamUrl);
            values.put(TAG_LIST, mTagList);
            values.put(TITLE, mTitle);
            values.put(TRACK_PATH, mTrackPath);
            values.put(TRACK_TYPE, mTrackType);
            values.put(TRACK_TYPE_ID, mTrackTypeId);
            values.put(UPLOAD, mUpload);
            values.put(URI, mUri);
            values.put(USER_FAVORITE, mUserFavorite);
            values.put(USER_ID, mUserId);
            values.put(USER_NAME, mUsername);
            values.put(USER_PERMALINK, mUserPermalink);
            values.put(USER_PERMALINK_URL, mUserPermalinkUrl);
            values.put(USER_PLAYBACK_COUNT, String.valueOf(mUserPlaybackCount));
            values.put(USER_URI, mUserUri);
            values.put(VIDEO_URL, mVideoUrl);
            values.put(WAVEFORM_URL, mWaveFormUrl);

            if (mLastModified != null) {
                values.put(LAST_MODIFIED, String.valueOf(mLastModified.getTimeInMillis()));
            }

            return values;
        }

        public String getInternalId() {
            return mIdTrack + mUserId;
        }

        public String getmWaveFormUrl() {
            return mWaveFormUrl;
        }

        public String getmVideoUrl() {
            return mVideoUrl;
        }

        public String getmUserUri() {
            return mUserUri;
        }

        public int getmUserPlaybackCount() {
            return mUserPlaybackCount;
        }

        public String getmUserPermalinkUrl() {
            return mUserPermalinkUrl;
        }

        public String getmUserPermalink() {
            return mUserPermalink;
        }

        public String getmUsername() {
            return mUsername;
        }

        public String getmUserId() {
            return mUserId;
        }

        public String getmUserFavorite() {
            return mUserFavorite;
        }

        public String getmUri() {
            return mUri;
        }

        public String getmUpload() {
            return mUpload;
        }

        public String getmTrackTypeId() {
            return mTrackTypeId;
        }

        public String getmTrackType() {
            return mTrackType;
        }

        public String getmTrackPath() {
            return mTrackPath;
        }

        public String getmTitle() {
            return mTitle;
        }

        public String getmTagList() {
            return mTagList;
        }

        public String getmStreamable() {
            return mStreamable;
        }

        public String getmArtworkPath() {
            return mArtworkPath;
        }

        public String getmArtworkUrl() {
            return mArtworkUrl;
        }

        public float getmBpm() {
            return mBpm;
        }

        public int getmCommentCount() {
            return mCommentCount;
        }

        public String getmCreatedAt() {
            return mCreatedAt;
        }

        public String getmDescription() {
            return mDescription;
        }

        public int getmDownloadCount() {
            return mDownloadCount;
        }

        public String getmDownloadable() {
            return mDownloadable;
        }

        public int getmDuration() {
            return mDuration;
        }

        public String getmGenre() {
            return mGenre;
        }

        public int getmIdTrack() {
            return mIdTrack;
        }

        public String getmIsrc() {
            return mIsrc;
        }

        public String getmKeySignature() {
            return mKeySignature;
        }

        public String getmLabelId() {
            return mLabelId;
        }

        public String getmLabelName() {
            return mLabelName;
        }

        public String getmLicense() {
            return mLicense;
        }

        public String getmLicenseId() {
            return mLicenseId;
        }

        public String getmOriginalFormat() {
            return mOriginalFormat;
        }

        public String getmPermalink() {
            return mPermalink;
        }

        public String getmPermalinkUrl() {
            return mPermalinkUrl;
        }

        public int getmPlaybackCount() {
            return mPlaybackCount;
        }

        public String getmPurchaseUrl() {
            return mPurchaseUrl;
        }

        public String getmRelease() {
            return mRelease;
        }

        public String getmReleaseDay() {
            return mReleaseDay;
        }

        public String getmReleaseMonth() {
            return mReleaseMonth;
        }

        public String getmReleaseYear() {
            return mReleaseYear;
        }

        public String getmSharing() {
            return mSharing;
        }

        public String getmSharingId() {
            return mSharingId;
        }

        public String getmStreamUrl() {
            return mStreamUrl;
        }

        public String getmStorePrefix() {
            return mStorePrefix;
        }

        public ImageLoader getmLoader() {
            return mLoader;
        }

        public Map<ImageSize, String> getmImages() {
            return mImages;
        }

        public Calendar getmLastModified() {
            return mLastModified;
        }

        @Override
        public String toString() {
            return this.mTitle + " - " + this.mUsername;
        }
    }

    abstract String signRequest(String request);

    abstract HttpRequest signRequest(Object request);

    abstract HttpRequest signRequest(HttpRequest request);


    public void retrieveMeTracks(ResultListener<ArrayList<Track>> listener) {
        final Uri uri = buildMeTracksQuery();
        getTracksfromUri(uri, listener);
    }

    public void retrieveDropBoxTracks(ResultListener<ArrayList<Track>> listener) {
        final Uri uri = buildDropBoxTracksQuery();
        getTracksfromUri(uri, listener);
    }

    public void retrieveFavoritesTracks(ResultListener<ArrayList<Track>> listener) {
        final Uri uri = Uri.parse("http://api.soundcloud.com/me/favorites.json");
        getTracksfromUri(uri, listener);
    }

    public void searchTracks(String query, int offset, int limit, ResultListener<ArrayList<Track>> listener) {
        final Uri uri = buildSearchTracksQuery(query, offset, limit);
        getTracksfromUri(uri, listener);
    }

    /**
     * Usarlo cuando el resultado va a ser un array de elementos Track
     *
     * @param uri
     * @param listener
     */
    private void getTracksfromUri(Uri uri, final ResultListener<ArrayList<Track>> listener){
        final HttpGet get = new HttpGet(uri.toString());
        final ArrayList<Track> tracks = new ArrayList<Track>();

        try {
            executeRequest(get, new ResponseHandler() {
                public void handleResponse(InputStream in) throws IOException {
                    parseArrayResponse(in, new ResponseParserArray() {
                        @Override
                        public void parseResponse(JSONArray parser) {
                            parseTracks(parser, tracks, listener);
                        }
                    });
                }
            });

        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not perform search with query: " , e);
            listener.onError(e);
        }

        return;
    }

    public void getTrack(String id, ResultListener<Track> listener) {
        final Uri uri = buildFindTrackQuery(id);
        getTrackFromUri(uri, listener);
    }
    /**
     * Usarlo cuando el resultado va a ser un elemento Track
     *
     * @param uri
     * @param listener
     */
    private void getTrackFromUri(Uri uri, final ResultListener<Track> listener){
        final HttpGet get = new HttpGet(uri.toString());
        final Track track = createTrack();

        try {
            executeRequest(get, new ResponseHandler() {
                public void handleResponse(InputStream in) throws IOException {
                    parseSingleResponse(in, new ResponseParserSingle() {
                        @Override
                        public void parseResponse(JSONObject parser) throws JSONException {
                            parseTrack(parser, track, listener);
                        }
                    });
                }
            });

        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not perform search with query: " , e);
            listener.onError(e);
        }

        return;
    }

    public void shareTrack(String idTrack,  ArrayList<String> mails, ResultListener<Track> listener) {
        final Uri uri = buildShareTrackQuery(idTrack);

        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null , Charset.forName(HTTP.UTF_8));

        for(String mail: mails){
            if(mail != null && !"".equals(mail)){
                ContentBody sharedTo;
                try {
                    sharedTo = new StringBody(mail.trim());
                    entity.addPart("track[shared_to][emails][][address]", sharedTo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        final HttpPut put = new HttpPut(uri.toString());
        signRequest(put);
        put.setEntity(entity);

        try {
            executeRequestNotSigned(put, new ResponseHandler() {
                public void handleResponse(InputStream in) throws IOException {
                    parseSingleResponse(in, new ResponseParserSingle() {
                        @Override
                        public void parseResponse(JSONObject parser) throws JSONException {
                            int i = 0;
                            i++;
                        }
                    });
                }
            });

        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not perform search with query: " , e);
            listener.onError(e);
        }

    }

    public void uploadTrack(Bundle bundle, ResultListener<Track> listener) {

        Track track = TracksStore.Track.fromBundle(bundle);

        SystemClock.sleep(1500);

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
                signRequest(filePost);
                filePost.setEntity(entity);

                executeRequestNotSigned(filePost, new ResponseHandler() {
                    public void handleResponse(InputStream in) throws IOException {
                        parseSingleResponse(in, new ResponseParserSingle() {
                            @Override
                            public void parseResponse(JSONObject parser) throws JSONException {
                                int i = 0;
                                i++;
                            }
                        });
                    }
                });
            }catch(Exception e){

            }
            /* mBoundUploadService = Soundroid.getUploadService();
            mBoundUploadService.uploadTrack(track);*/
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
                signRequest(filePut);

                filePut.setEntity(entity);

                executeRequestNotSigned(filePut, new ResponseHandler() {
                    public void handleResponse(InputStream in) throws IOException {
                        parseSingleResponse(in, new ResponseParserSingle() {
                            @Override
                            public void parseResponse(JSONObject parser) throws JSONException {
                                int i = 0;
                                i++;
                            }
                        });
                    }
                });
            }catch(Exception e){

            }
        }
    }

    abstract Uri buildShareTrackQuery(String idTrack);
    abstract Uri buildDropBoxTracksQuery();
    abstract Uri buildMeTracksQuery();
    abstract Uri buildDeleteTrackQuery(String idTrackSoundcloud);
    abstract void parseTracks(JSONArray JSONTracks, ArrayList<Track> tracks, ResultListener<ArrayList<Track>> listener);
    abstract void parseTrack(JSONObject JSONTrack, Track track, ResultListener<Track> listener);

    public void deleteTrack(String idTrackSoundcloud, ResultListener<Track> listener) {
        final Uri uri = buildDeleteTrackQuery(idTrackSoundcloud);
        deleteTrack(uri, listener);
    }

    private void deleteTrack(Uri uri, final ResultListener<Track> listener){
        final HttpDelete delete = new HttpDelete(uri.toString());
        try {
            executeRequest(delete, new ResponseHandler() {
                public void handleResponse(InputStream in) throws IOException {
                    parseSingleResponse(in, new ResponseParserSingle() {
                        @Override
                        public void parseResponse(JSONObject parser) throws JSONException {
                            //parseTrack(parser, track, listener);
                            listener.onSuccess((Track)new Object());//TODO VER COMO ARREGLAR ESTO
                        }
                    });
                }
            });

        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not perform search with query: " , e);
            listener.onError(e);
        }

        return;
    }

    /**
     * Parses a book from the XML input stream.
     *
     * @param parser The JSON parser to use to parse the book.
     * @param book The track object to put the parsed data in.
     *
     * @return True if the book could correctly be parsed, false otherwise.
     */
    abstract boolean parseTrack(JSONObject parser, Track track) throws JSONException;

    abstract boolean parseAddtoFavoritesResponse(JSONObject parser, Track track) throws JSONException;

    /**
     * Creates an instance of {@link org.curiouscreature.android.shelves.provider.BooksStore.Book}
     * with this book store's name.
     *
     * @return A new instance of Book.
     */
    Track createTrack() {
        return new Track();
    }

    /**
     * Constructs the query used to search for books. The query can be any combination
     * of keywords. The store is free to interpret the keywords in any way.
     *
     * @param query A free form text query to search for books.
     *
     * @return The Uri to the list of books matching the query.
     */
    abstract Uri buildSearchTracksQuery(String query, int offset, int limit);

    /**
     * Constructs the query used to find a book identified by its id. The unique
     * identifier should be either the EAN (ISBN-13) or ISBN (ISBN-10) of the book
     * to find.
     *
     * @param id The EAN or ISBN of the book to find.
     *
     * @return The Uri to the books details for this book store.
     */
    abstract Uri buildFindTrackQuery(String id);


    /**
     * Executes an HTTP request on a REST web service. If the response is ok, the content
     * is sent to the specified response handler.
     *
     * @param host
     * @param get The GET request to executed.
     * @param handler The handler which will parse the response.
     *
     * @throws java.io.IOException
     */
    private void executeRequest(HttpHost host, HttpGet get, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            final HttpResponse response = HttpManager.execute(host, get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequest(HttpGet get, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            signRequest(get);
            final HttpResponse response = HttpManager.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequest(HttpPut put, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            signRequest(put);
            final HttpResponse response = HttpManager.execute(put);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequestNotSigned(HttpPut put, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            SystemClock.sleep(500);
            final HttpResponse response = HttpManager.execute(put);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequestNotSigned(HttpPost post, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            SystemClock.sleep(500);
            final HttpResponse response = HttpManager.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequest(HttpDelete delete, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            signRequest(delete);
            final HttpResponse response = HttpManager.execute(delete);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    private void executeRequestNoSigned(HttpGet get, ResponseHandler handler) throws IOException {

        HttpEntity entity = null;
        try {
            final HttpResponse response = HttpManager.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                final InputStream in = entity.getContent();
                handler.handleResponse(in);
            }
        } finally {
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    public void addToFavorites(String idTrack){
        //"me/favorites/" + TRACK_ID

        final Uri uri = Uri.parse("http://api.soundcloud.com/me/favorites/" + idTrack + ".json");

        final HttpPut put = new HttpPut(uri.toString());
        final Track track = createTrack();
        final boolean[] result = new boolean[1];

        try {
            executeRequest(put, new ResponseHandler() {
                public void handleResponse(InputStream in) throws IOException {

                    parseFavoritesResponse(in, new ResponseAddToFavorites() {

                        @Override
                        public void parseResponse(JSONObject response) throws JSONException {
                            result[0] = parseTrack(response, track);

                        }
                    });
                    //					parseSingleResponse(in, new ResponseParserSingle() {
                    //						@Override
                    //						public void parseResponse(JSONObject jsonTrack) throws JSONException {
                    //							result[0] = parseTrack(jsonTrack, track);
                    //						}
                    //					});
                }
            });

            //return result[0] ? track : null;
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not add to favorites the track with id: " + idTrack);
        }
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

    /**
     * Response parser. When the request returns a valid response, this parser
     * is invoked to process the XML response.
     */
    static interface ResponseParserArray {
        /**
         * Processes the JSON response sent by the web service after a successful request.
         *
         * @param parser The parser containing the XML responses.
         *
         * @throws org.xmlpull.v1.XmlPullParserException
         * @throws java.io.IOException
         */
        public void parseResponse(JSONArray parser) throws JSONException;
    }


    /**
     * Response parser. When the request returns a valid response, this parser
     * is invoked to process the XML response.
     */
    static interface ResponseParserSingle {
        /**
         * Processes the JSON response sent by the web service after a successful request.
         *
         * @param parser The parser containing the XML responses.
         *
         * @throws org.xmlpull.v1.XmlPullParserException
         * @throws java.io.IOException
         */
        public void parseResponse(JSONObject parser) throws JSONException;
    }

    static interface ResponseAddToFavorites{
        public void parseResponse(JSONObject parser) throws JSONException;
    }

    abstract void parseArrayResponse(InputStream in, ResponseParserArray responseParser) throws IOException;

    abstract void parseSingleResponse(InputStream in, ResponseParserSingle responseParser) throws IOException;

    abstract void parseFavoritesResponse(InputStream in, ResponseAddToFavorites responseParser) throws IOException;

    /**
     * Interface used to load images with an expiring date. The expiring date is handled by
     * the image cache to check for updated images from time to time.
     */
    static interface ImageLoader {
        /**
         * Load the specified URL as a Bitmap and associates an expiring date to it.
         *
         * @param url The URL of the image to load.
         *
         * @return The Bitmap decoded from the URL and an expiration date.
         */
        public ImageUtilities.ExpiringBitmap load(String url);
    }

    protected String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the
         * BufferedReader return null which means there's no more data to
         * read. Each line will appended to a StringBuilder and returned as
         * String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw e;
            }
        }
        return sb.toString();
    }
}