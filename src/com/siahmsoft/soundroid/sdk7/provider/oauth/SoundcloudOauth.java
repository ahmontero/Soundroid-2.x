package com.siahmsoft.soundroid.sdk7.provider.oauth;

import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class SoundcloudOauth extends OauthStore{

	private static final String CONSUMER_KEY = "YOUR_CONSUMER_KEY";
	private static final String CONSUMER_SECRET = "YOUR_CONSUMER_SECRET";

	public static final String AUTHORIZE_WEBSITE_URL = "http://soundcloud.com/oauth/authorize";
	public static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.soundcloud.com/oauth/request_token";
	public static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.soundcloud.com/oauth/access_token";

	public static final String CALLBACK_URL = "soundroid-app:///";
	public static final String CALLBACK_URL_SCHEMA = "soundroid-app";

	private String mHost;

	public SoundcloudOauth(String host){
		super(	new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET),
				new DefaultOAuthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL, AUTHORIZE_WEBSITE_URL),
				CALLBACK_URL);

		this.mHost = host;
	}
}