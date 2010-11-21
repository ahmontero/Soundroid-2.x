package com.siahmsoft.soundroid.sdk7.provider.oauth;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class OauthProvider extends ContentProvider {
	private static final String LOG_TAG = "OauthProvider";

	private static final String AUTHORITY = "com.siahmsoft.soundroid.provider.oauth.OauthProvider";

	private static final String DATABASE_NAME = "oauths.db";
	private static final int DATABASE_VERSION = 1;

	private static final int SEARCH = 1;
	private static final int OAUTHS = 2;
	private static final int OAUTH_ID = 3;

	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
		URI_MATCHER.addURI(AUTHORITY, "oauths", OAUTHS);
		URI_MATCHER.addURI(AUTHORITY, "oauths/#", OAUTH_ID);
	}

	private static final HashMap<String, String> SUGGESTION_PROJECTION_MAP;
	static {
		SUGGESTION_PROJECTION_MAP = new HashMap<String, String>();

		SUGGESTION_PROJECTION_MAP.put(	SearchManager.SUGGEST_COLUMN_TEXT_1,
				OauthStore.Oauth.APP_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);

		SUGGESTION_PROJECTION_MAP.put(OauthStore.Oauth._ID, OauthStore.Oauth._ID);
	}

	private SQLiteOpenHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case OAUTHS:
			count = db.delete("oauths", selection, selectionArgs);
			break;
		case OAUTH_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete("oauths", OauthStore.Oauth._ID + "=" + segment +
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
		case OAUTHS:
			return "vnd.android.cursor.dir/vnd.com.siahmsoft.provider.oauths";
		case OAUTH_ID:
			return "vnd.android.cursor.item/vnd.com.siahmsoft.provider.oauths";
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = new ContentValues(initialValues);
			values.put(OauthStore.Oauth.APP_NAME, values.getAsString(OauthStore.Oauth.APP_NAME));
			values.put(OauthStore.Oauth.ACCESS_TOKEN, values.getAsString(OauthStore.Oauth.ACCESS_TOKEN));
			values.put(OauthStore.Oauth.SECRET_TOKEN, values.getAsString(OauthStore.Oauth.SECRET_TOKEN));
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != OAUTHS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert("oauths", OauthStore.Oauth.APP_NAME, values);
		if (rowId > 0) {
			Uri insertUri = ContentUris.withAppendedId(OauthStore.Oauth.CONTENT_URI, rowId);
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
			qb.setTables("oauths");
			String query = uri.getLastPathSegment();
			if (!TextUtils.isEmpty(query)) {
				qb.appendWhere(OauthStore.Oauth.APP_NAME + " LIKE ");
				qb.appendWhereEscapeString('%' + query + '%');
			}
			qb.setProjectionMap(SUGGESTION_PROJECTION_MAP);
			break;
		case OAUTHS:
			qb.setTables("oauths");
			break;
		case OAUTH_ID:
			qb.setTables("oauths");
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = OauthStore.Oauth.DEFAULT_SORT_ORDER;
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

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE oauths ("
					+ OauthStore.Oauth._ID + " INTEGER PRIMARY KEY, "
					+ OauthStore.Oauth.APP_NAME + " TEXT, "
					+ OauthStore.Oauth.ACCESS_TOKEN + " TEXT, "
					+ OauthStore.Oauth.SECRET_TOKEN + " TEXT "
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
					newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS oauths");
			onCreate(db);
		}
	}
}