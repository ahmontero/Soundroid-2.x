package com.siahmsoft.soundroid.sdk7;



import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Pantalla principal de la aplicación. Tiene 4 botones.
 *
 * @author Antonio
 *
 */
public class MainActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        addTab("Me", "Me", R.drawable.music, MeActivity.class);
        addTab("Search", "Search", R.drawable.cloud, SearchTracksActivity.class);

        //addTab("Dropbox", "Dropbox", android.R.drawable.ic_media_next, DropBoxActivity.class);
        addTab("Favorites", "Favorites", R.drawable.love, FavoritesActivity.class);

        getTabHost().setCurrentTab(0);
    }

    private void addTab(String tag, String label, int idDrawable,  Class<?> cls){
        Intent intent = new Intent().setClass(this, cls);
        TabHost.TabSpec spec = getTabHost().newTabSpec(tag).setIndicator(label, getResources().getDrawable(idDrawable)).setContent(intent);
        getTabHost().addTab(spec);
    }
}