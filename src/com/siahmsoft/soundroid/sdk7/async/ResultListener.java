package com.siahmsoft.soundroid.sdk7.async;

import com.siahmsoft.soundroid.sdk7.provider.tracks.TracksStore.Track;

public interface ResultListener<T> {

    void onError(Exception e);

    void onSuccess(T result);

    void onTrackReceived(Track track);
}
