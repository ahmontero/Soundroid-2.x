/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siahmsoft.soundroid.sdk7.provider.tracks;

import com.siahmsoft.soundroid.sdk7.util.ImageUtilities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class TracksManager {
    static final int TRACK_ARTWORK_WIDTH = 100;
    static final int TRACK_ARTWORK_HEIGHT = 120;

    private static String sTrackIdSelection;
    private static String sTrackIdSoundcloudSelection;
    private static String sTrackSelection;

    private static String[] sArguments1 = new String[1];
    private static String[] sArguments1New = new String[1];
    private static String[] sArguments2 = new String[2];
    private static String[] sArguments4 = new String[4];

    private static final String[] PROJECTION_ID = new String[] { TracksStore.Track._ID };

    private static final String[] PROJECTION_ID_IDTRACK = new String[] { TracksStore.Track._ID, TracksStore.Track.ID_TRACK };

    private static final String[] PROJECTION_ID_IDTRACK_TITLE_USERNAME = new String[] {
        TracksStore.Track._ID,
        TracksStore.Track.ID_TRACK,
        TracksStore.Track.TITLE,
        TracksStore.Track.USER_NAME };

    static {
        StringBuilder selection = new StringBuilder();
        selection.append(TracksStore.Track._ID);
        selection.append("=?");
        sTrackIdSelection = selection.toString();

        selection = new StringBuilder();
        selection.append(sTrackIdSelection);
        selection.append(" OR ");
        selection.append(TracksStore.Track.ID_TRACK);
        selection.append("=?");
        sTrackIdSoundcloudSelection = selection.toString();

        selection = new StringBuilder();
        selection.append(sTrackIdSelection);
        selection.append(" OR ");
        selection.append(TracksStore.Track.ID_TRACK);
        selection.append("=? OR ");
        selection.append(TracksStore.Track.TITLE);
        selection.append("=? OR ");
        selection.append(TracksStore.Track.USER_NAME);
        selection.append("=?");
        sTrackSelection = selection.toString();
    }

    private TracksManager() {
    }

    public static String findSoundcloudTrackId(ContentResolver contentResolver, String idTrackSoundcloud) {
        String internalId = null;
        Cursor c = null;

        try {
            final String[] arguments2 = sArguments2;
            arguments2[0] = arguments2[1] = idTrackSoundcloud;
            c = contentResolver.query(TracksStore.Track.CONTENT_URI, PROJECTION_ID_IDTRACK, sTrackIdSoundcloudSelection, arguments2, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    internalId = c.getString(c.getColumnIndexOrThrow(TracksStore.Track.ID_TRACK));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return internalId;
    }

    public static boolean trackExists(ContentResolver contentResolver, String id) {
        boolean exists;
        Cursor c = null;

        try {
            final String[] arguments2 = sArguments2;
            arguments2[0] = arguments2[1] = id;
            c = contentResolver.query(TracksStore.Track.CONTENT_URI, PROJECTION_ID_IDTRACK, sTrackIdSoundcloudSelection, arguments2, null);
            exists = c.getCount() > 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return exists;
    }

    public static boolean deleteTrack(ContentResolver contentResolver, String trackId) {
        final String[] arguments1 = sArguments1;
        arguments1[0] = trackId;
        int count = contentResolver.delete(TracksStore.Track.CONTENT_URI, sTrackIdSelection, arguments1);
        ImageUtilities.deleteCachedCover(trackId);
        return count > 0;
    }

    public static boolean deleteAllTrack(ContentResolver contentResolver) {
        int count = contentResolver.delete(TracksStore.Track.CONTENT_URI, null, null);
        return count > 0;
    }

    public static TracksStore.Track findTrack(ContentResolver contentResolver, String trackId) {
        TracksStore.Track track = null;
        Cursor c = null;

        try {
            sArguments1[0] = trackId;
            c = contentResolver.query(TracksStore.Track.CONTENT_URI, null, sTrackIdSelection, sArguments1, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    track = TracksStore.Track.fromCursor(c);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return track;
    }

    public static TracksStore.Track findTrack(ContentResolver contentResolver, Uri data) {
        TracksStore.Track track = null;
        Cursor c = null;

        try {
            c = contentResolver.query(data, null, null, null, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    track = TracksStore.Track.fromCursor(c);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return track;
    }

    public static TracksStore.Track saveTrack(ContentResolver resolver, TracksStore.Track track) {

        if (track != null) {
            final Uri uri = resolver.insert(TracksStore.Track.CONTENT_URI, track.getContentValues());

            if (uri != null) {
                return findTrack(resolver, uri);
            }
        }

        return null;
    }

    public static boolean deleteSearchTracks(ContentResolver contentResolver){
        final String[] arguments1New = sArguments1New;
        arguments1New[0] = "1";
        int count = contentResolver.delete(TracksStore.Track.CONTENT_URI, null, arguments1New);
        return count > 0;
    }

    public static TracksStore.Track saveTrack(ContentResolver resolver, String idTrack) {

        final TracksStore.Track track = findTrack(resolver, idTrack);

        if (track != null) {
            final Uri uri = resolver.insert(TracksStore.Track.CONTENT_URI, track.getContentValues());

            if (uri != null) {
                return findTrack(resolver, uri);
            }
        }

        return null;
    }

    public static Cursor getMeTracks(ContentResolver contentResolver){
        return contentResolver.query(TracksStore.Track.CONTENT_URI, null, null, null, null);
    }
}