package com.siahmsoft.soundroid.sdk7;


import com.siahmsoft.soundroid.sdk7.provider.oauth.OauthsManager;
import com.siahmsoft.soundroid.sdk7.util.ImageUtilities;
import com.siahmsoft.soundroid.sdk7.util.ImportUtilities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Preference p1 = getPreferenceScreen().findPreference("buttonUnauth");
        final Preference p2 = getPreferenceScreen().findPreference("buttonDeleteCache");

        String res = OauthsManager.findOauthTokens(getContentResolver(), "Soundroid");

        if(res == null){
            p1.setEnabled(false);
        }

        if(ImportUtilities.getCacheDirectory().isDirectory() && ImportUtilities.getCacheDirectory().list().length > 0){
            p2.setEnabled(true);
        }else{
            p2.setEnabled(false);
        }

        p1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean res = OauthsManager.deleteTokens(getContentResolver(), "Soundroid");

                if(res){
                    p1.setEnabled(false);
                }else{
                    p1.setEnabled(true);
                }

                return true;
            }

        });


        p2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean res = ImageUtilities.deleteAllCachedCover();

                if(res){
                    p2.setEnabled(false);
                }else{
                    p2.setEnabled(true);
                }

                return true;
            }
        });
    }
}