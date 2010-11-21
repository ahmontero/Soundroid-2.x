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

package com.siahmsoft.soundroid.sdk7.provider.oauth;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class OauthsManager {

    private static String sOauthIdSelection;
    private static String sOauthAppNameSelection;

    private static String[] sArguments1 = new String[1];
    private static String[] sArguments2 = new String[2];

    private static final String[] PROJECTION_APPNAME = new String[] { OauthStore.Oauth.APP_NAME };

    private static final String[] PROJECTION_ID = new String[] { OauthStore.Oauth._ID };

    static {
        StringBuilder selection = new StringBuilder();
        selection.append(OauthStore.Oauth._ID);
        selection.append("=?");
        sOauthIdSelection = selection.toString();

        selection = new StringBuilder();
        selection.append(OauthStore.Oauth.APP_NAME);
        selection.append("=?");
        sOauthAppNameSelection = selection.toString();
    }


    public static OauthStore.Oauth saveToken(ContentResolver resolver, OauthStore.Oauth tokens) {

        final Uri uri = resolver.insert(OauthStore.Oauth.CONTENT_URI, tokens.getContentValues());

        if (uri != null) {
            return findTokens(resolver, uri);
        }

        return null;
    }

    public static String findOauthTokens(ContentResolver contentResolver, String appName) {
        String internalId = null;
        Cursor c = null;

        try {
            final String[] arguments1 = sArguments1;
            arguments1[0] = appName;
            c = contentResolver.query(OauthStore.Oauth.CONTENT_URI, PROJECTION_APPNAME, sOauthAppNameSelection, arguments1, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    internalId = c.getString(c.getColumnIndexOrThrow(OauthStore.Oauth.APP_NAME));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return internalId;
    }

    public static boolean tokensExists(ContentResolver contentResolver, String appName) {
        boolean exists;
        Cursor c = null;

        try {
            final String[] arguments1 = sArguments1;
            arguments1[0] = appName;
            c = contentResolver.query(OauthStore.Oauth.CONTENT_URI, PROJECTION_APPNAME, sOauthAppNameSelection, arguments1, null);
            exists = c.getCount() > 0;

        } finally {
            if (c != null) {
                c.close();
            }
        }

        return exists;
    }

    public static boolean deleteTokens(ContentResolver contentResolver, String appName) {
        final String[] arguments1 = sArguments1;
        arguments1[0] = appName;
        int count = contentResolver.delete(OauthStore.Oauth.CONTENT_URI, sOauthAppNameSelection, arguments1);
        return count > 0;
    }

    public static OauthStore.Oauth findTokens(ContentResolver contentResolver, String appName) {
        OauthStore.Oauth oauth = null;
        Cursor c = null;

        try {
            sArguments1[0] = appName;
            c = contentResolver.query(OauthStore.Oauth.CONTENT_URI, null, sOauthAppNameSelection, sArguments1, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    oauth = OauthStore.Oauth.fromCursor(c);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return oauth;
    }

    public static OauthStore.Oauth findTokens(ContentResolver contentResolver, Uri data) {
        OauthStore.Oauth oauth = null;
        Cursor c = null;

        try {
            c = contentResolver.query(data, null, null, null, null);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    oauth = OauthStore.Oauth.fromCursor(c);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return oauth;
    }
}