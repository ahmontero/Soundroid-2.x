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

package com.siahmsoft.soundroid.sdk7.util;

import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImportUtilities {
    private static final String CACHE_DIRECTORY = "soundroid/tracks";
    private static final String IMPORT_FILE = "tracks.txt";

    private ImportUtilities() {
    }

    public static File getCacheDirectory() {
        return IOUtilities.getExternalFile(CACHE_DIRECTORY);
    }

    public static boolean addTrackCoverToCache(TracksStore.Track track, Bitmap bitmap) {
        File cacheDirectory;
        try {
            cacheDirectory = ensureCache();
        } catch (IOException e) {
            return false;
        }

        File coverFile = new File(cacheDirectory, track.getInternalId());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(coverFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            IOUtilities.closeStream(out);
        }

        return true;
    }

    private static File ensureCache() throws IOException {
        File cacheDirectory = getCacheDirectory();
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
            new File(cacheDirectory, ".nomedia").createNewFile();
        }
        return cacheDirectory;
    }
}