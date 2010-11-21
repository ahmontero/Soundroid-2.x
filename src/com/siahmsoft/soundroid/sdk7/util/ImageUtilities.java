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

import com.siahmsoft.soundroid.sdk7.drawable.FastBitmapDrawable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class ImageUtilities {
    private static final String LOG_TAG = "ImageUtilities";

    private static final boolean FLAG_DECODE_BITMAP_WITH_SKIA = false;

    private static final FastBitmapDrawable NULL_DRAWABLE = new FastBitmapDrawable(null);

    // TODO: Use a concurrent HashMap to support multiple threads
    private static final HashMap<String, SoftReference<FastBitmapDrawable>> sArtCache = new HashMap<String, SoftReference<FastBitmapDrawable>>();

    private static SimpleDateFormat sLastModifiedFormat;

    private ImageUtilities() {
    }

    /**
     * A Bitmap associated with its last modification date. This can be used to check
     * whether the book covers should be downloaded again.
     */
    public static class ExpiringBitmap {
        public Bitmap bitmap;
        public Calendar lastModified;
    }

    /**
     * Deletes the specified drawable from the cache. Calling this method will remove
     * the drawable from the in-memory cache and delete the corresponding file from the
     * external storage.
     *
     * @param id The id of the drawable to delete from the cache
     */
    public static void deleteCachedCover(String id) {
        new File(ImportUtilities.getCacheDirectory(), id).delete();
        sArtCache.remove(id);
    }

    public static boolean deleteAllCachedCover() {
        boolean res = true;

        File f = ImportUtilities.getCacheDirectory();

        if(f.isDirectory()){
            for(File file : f.listFiles()){
                res = res && file.delete();
            }
        }

        res = res && ImportUtilities.getCacheDirectory().delete();
        sArtCache.clear();

        return res;
    }

    /**
     * Retrieves a drawable from the book covers cache, identified by the specified id.
     * If the drawable does not exist in the cache, it is loaded and added to the cache.
     * If the drawable cannot be added to the cache, the specified default drwaable is
     * returned.
     *
     * @param id The id of the drawable to retrieve
     * @param defaultCover The default drawable returned if no drawable can be found that
     *         matches the id
     *
     * @return The drawable identified by id or defaultCover
     */
    public static FastBitmapDrawable getCachedCover(String id, FastBitmapDrawable defaultCover) {
        FastBitmapDrawable drawable = null;

        SoftReference<FastBitmapDrawable> reference = sArtCache.get(id);
        if (reference != null) {
            drawable = reference.get();
        }

        if (drawable == null || drawable == NULL_DRAWABLE) {
            final Bitmap bitmap = loadCover(id);
            if (bitmap != null) {
                drawable = new FastBitmapDrawable(bitmap);
            } else {
                drawable = NULL_DRAWABLE;
            }

            sArtCache.put(id, new SoftReference<FastBitmapDrawable>(drawable));
        }

        return drawable == NULL_DRAWABLE ? defaultCover : drawable;
    }

    /**
     * Removes all the callbacks from the drawables stored in the memory cache. This
     * method must be called from the onDestroy() method of any activity using the
     * cached drawables. Failure to do so will result in the entire activity being
     * leaked.
     */
    public static void cleanupCache() {
        for (SoftReference<FastBitmapDrawable> reference : sArtCache.values()) {
            final FastBitmapDrawable drawable = reference.get();
            if (drawable != null) {
                drawable.setCallback(null);
            }
        }
    }

    /**
     * Loads an image from the specified URL.
     *
     * @param url The URL of the image to load.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    public static ExpiringBitmap load(String url) {
        return load(url, null);
    }

    /**
     * Loads an image from the specified URL with the specified cookie.
     *
     * @param url The URL of the image to load.
     * @param cookie The cookie to use to load the image.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    public static ExpiringBitmap load(String url, String cookie) {
        ExpiringBitmap expiring = new ExpiringBitmap();

        final HttpGet get = new HttpGet(url);
        if (cookie != null) {
            get.setHeader("cookie", cookie);
        }

        HttpEntity entity = null;
        try {
            final HttpResponse response = HttpManager.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                setLastModified(expiring, response);

                entity = response.getEntity();

                InputStream in = null;
                OutputStream out = null;

                try {
                    in = entity.getContent();
                    if (FLAG_DECODE_BITMAP_WITH_SKIA) {
                        expiring.bitmap = BitmapFactory.decodeStream(in);
                    } else {
                        final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                        out = new BufferedOutputStream(dataStream, IOUtilities.IO_BUFFER_SIZE);
                        IOUtilities.copy(in, out);
                        out.flush();

                        final byte[] data = dataStream.toByteArray();
                        expiring.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                } finally {
                    IOUtilities.closeStream(in);
                    IOUtilities.closeStream(out);
                }
            }
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
        } finally {
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                }
            }
        }

        return expiring;
    }

    private static void setLastModified(ExpiringBitmap expiring, HttpResponse response) {
        expiring.lastModified = null;

        final Header header = response.getFirstHeader("Last-Modified");
        if (header == null) {
            return;
        }

        if (sLastModifiedFormat == null) {
            sLastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        }

        final Calendar calendar = GregorianCalendar.getInstance();
        try {
            calendar.setTime(sLastModifiedFormat.parse(header.getValue()));
            expiring.lastModified = calendar;
        } catch (ParseException e) {
            // Ignore
        }
    }


    private static Bitmap loadCover(String id) {
        final File file = new File(ImportUtilities.getCacheDirectory(), id);
        if (file.exists()) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(file);
                return BitmapFactory.decodeStream(stream, null, null);
            } catch (FileNotFoundException e) {
                // Ignore
            } finally {
                IOUtilities.closeStream(stream);
            }
        }
        return null;
    }
}