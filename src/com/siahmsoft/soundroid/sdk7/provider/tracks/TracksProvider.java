package com.siahmsoft.soundroid.sdk7.provider.tracks;



import com.siahmsoft.soundroid.sdk7.R;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracksProvider extends ContentProvider {
    private static final String LOG_TAG = "TracksProvider";

    private static final String AUTHORITY = "com.siahmsoft.soundroid.provider.track.TrackProvider";

    private static final String DATABASE_NAME = "tracks.db";
    private static final int DATABASE_VERSION = 1;

    private static final int SEARCH = 1;
    private static final int TRACKS = 2;
    private static final int TRACK_ID = 3;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "tracks", TRACKS);
        URI_MATCHER.addURI(AUTHORITY, "tracks/#", TRACK_ID);
    }

    private static final HashMap<String, String> SUGGESTION_PROJECTION_MAP;
    static {
        SUGGESTION_PROJECTION_MAP = new HashMap<String, String>();

        SUGGESTION_PROJECTION_MAP.put(	SearchManager.SUGGEST_COLUMN_TEXT_1,
                TracksStore.Track.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        SUGGESTION_PROJECTION_MAP.put(	SearchManager.SUGGEST_COLUMN_TEXT_2,
                TracksStore.Track.USER_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);

        SUGGESTION_PROJECTION_MAP.put(	SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                TracksStore.Track._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

        SUGGESTION_PROJECTION_MAP.put(TracksStore.Track._ID, TracksStore.Track._ID);
    }

    private SQLiteOpenHelper mOpenHelper;

    private Pattern[] mKeyPrefixes;
    private Pattern[] mKeySuffixes;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        switch (URI_MATCHER.match(uri)) {
            case TRACKS:
                count = db.delete("tracks", selection, selectionArgs);
                break;
            case TRACK_ID:
                String segment = uri.getPathSegments().get(1);
                count = db.delete("tracks", TracksStore.Track._ID + "=" + segment +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TRACKS:
                //		"vnd.android.cursor.dir/vnd.com.siahmsoft.provider.tracks";
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.siahmsoft.provider.tracks";
            case TRACK_ID:
                //		"vnd.android.cursor.item/vnd.com.siahmsoft.provider.tracks";
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.siahmsoft.provider.tracks";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
            values.put(TracksStore.Track.TITLE, keyFor(values.getAsString(TracksStore.Track.TITLE)));
        } else {
            values = new ContentValues();
        }

        if (URI_MATCHER.match(uri) != TRACKS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert("tracks", TracksStore.Track.TITLE, values);
        if (rowId > 0) {
            Uri insertUri = ContentUris.withAppendedId(TracksStore.Track.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return insertUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri)) {
            case SEARCH:
                qb.setTables("tracks");
                String query = uri.getLastPathSegment();
                if (!TextUtils.isEmpty(query)) {
                    qb.appendWhere(TracksStore.Track.USER_NAME + " LIKE ");
                    qb.appendWhereEscapeString('%' + query + '%');
                    qb.appendWhere(" OR ");
                    qb.appendWhere(TracksStore.Track.TITLE + " LIKE ");
                    qb.appendWhereEscapeString('%' + query + '%');
                }
                qb.setProjectionMap(SUGGESTION_PROJECTION_MAP);
                break;
            case TRACKS:
                qb.setTables("tracks");
                break;
            case TRACK_ID:
                qb.setTables("tracks");
                qb.appendWhere("_id=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = TracksStore.Track.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String keyFor(String name) {
        if (name == null) {
            name = "";
        }

        name = name.trim().toLowerCase();

        if (mKeyPrefixes == null) {
            final Resources resources = getContext().getResources();
            final String[] keyPrefixes = resources.getStringArray(R.array.prefixes);
            final int count = keyPrefixes.length;

            mKeyPrefixes = new Pattern[count];
            for (int i = 0; i < count; i++) {
                mKeyPrefixes[i] = Pattern.compile("^" + keyPrefixes[i] + "\\s+");
            }
        }

        if (mKeySuffixes == null) {
            final Resources resources = getContext().getResources();
            final String[] keySuffixes = resources.getStringArray(R.array.suffixes);
            final int count = keySuffixes.length;

            mKeySuffixes = new Pattern[count];
            for (int i = 0; i < count; i++) {
                mKeySuffixes[i] = Pattern.compile("\\s*" + keySuffixes[i] + "$");
            }
        }

        final Pattern[] prefixes = mKeyPrefixes;
        for (Pattern prefix : prefixes) {
            final Matcher matcher = prefix.matcher(name);
            if (matcher.find()) {
                name = name.substring(matcher.end());
                break;
            }
        }

        final Pattern[] suffixes = mKeySuffixes;
        for (Pattern suffix : suffixes) {
            final Matcher matcher = suffix.matcher(name);
            if (matcher.find()) {
                name = name.substring(0, matcher.start());
                break;
            }
        }

        return name;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE tracks ("
                    + TracksStore.Track._ID + " INTEGER PRIMARY KEY, "
                    + TracksStore.Track.ARTWORK_PATH + " TEXT, "
                    + TracksStore.Track.ARTWORK_URL + " TEXT, "
                    + TracksStore.Track.BPM + " INTEGER, "
                    + TracksStore.Track.COMMENT_COUNT + " INTEGER, "
                    + TracksStore.Track.CREATED_AT + " TEXT, "
                    + TracksStore.Track.DESCRIPTION + " TEXT, "
                    + TracksStore.Track.DOWNLOAD_COUNT + " INTEGER, "
                    + TracksStore.Track.DOWNLOADABLE + " TEXT, "
                    + TracksStore.Track.DURATION + " INTEGER, "
                    + TracksStore.Track.GENRE + " TEXT, "
                    + TracksStore.Track.ID_TRACK + " INTEGER, "
                    + TracksStore.Track.ISRC + " TEXT, "
                    + TracksStore.Track.KEY_SIGNATURE + " TEXT,"
                    + TracksStore.Track.LABEL_ID + " TEXT,"
                    + TracksStore.Track.LABEL_NAME + " TEXT,"
                    + TracksStore.Track.LAST_MODIFIED + " INTEGER, "
                    + TracksStore.Track.LICENSE + " TEXT,"
                    + TracksStore.Track.LICENSE_ID + " TEXT,"
                    + TracksStore.Track.ORIGINAL_FORMAT + " TEXT,"
                    + TracksStore.Track.PERMALINK + " TEXT,"
                    + TracksStore.Track.PERMALINK_URL + " TEXT,"
                    + TracksStore.Track.PLAYBACK_COUNT + " INTEGER,"
                    + TracksStore.Track.PURCHASE_URL + " TEXT,"
                    + TracksStore.Track.RELEASE + " TEXT,"
                    + TracksStore.Track.RELEASE_DAY + " TEXT,"
                    + TracksStore.Track.RELEASE_MONTH + " TEXT,"
                    + TracksStore.Track.RELEASE_YEAR + " TEXT,"
                    + TracksStore.Track.SHARING + " TEXT,"
                    + TracksStore.Track.SHARING_ID + " TEXT,"
                    + TracksStore.Track.STREAM_URL + " TEXT,"
                    + TracksStore.Track.STREAMABLE + " TEXT,"
                    + TracksStore.Track.TAG_LIST + " TEXT,"
                    + TracksStore.Track.TITLE + " TEXT,"
                    + TracksStore.Track.TRACK_PATH + " TEXT,"
                    + TracksStore.Track.TRACK_TYPE + " TEXT,"
                    + TracksStore.Track.TRACK_TYPE_ID + " TEXT,"
                    + TracksStore.Track.UPLOAD + " TEXT,"
                    + TracksStore.Track.URI + " TEXT,"
                    + TracksStore.Track.USER_FAVORITE + " TEXT,"
                    + TracksStore.Track.USER_ID + " TEXT,"
                    + TracksStore.Track.USER_NAME + " TEXT,"
                    + TracksStore.Track.USER_PERMALINK + " TEXT,"
                    + TracksStore.Track.USER_PERMALINK_URL + " TEXT,"
                    + TracksStore.Track.USER_PLAYBACK_COUNT + " INTEGER,"
                    + TracksStore.Track.USER_URI + " TEXT,"
                    + TracksStore.Track.VIDEO_URL + " TEXT,"
                    + TracksStore.Track.WAVEFORM_URL + " TEXT"
                    + ");");

            db.execSQL("CREATE INDEX trackIndexTitle ON tracks(" + TracksStore.Track.TITLE + ");");
            db.execSQL("CREATE INDEX trackIndexUsername ON tracks(" + TracksStore.Track.USER_NAME + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                    newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS tracks");
            onCreate(db);
        }
    }
}