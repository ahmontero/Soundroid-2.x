package com.siahmsoft.soundroid.sdk7;



import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

public class BrowserView extends Activity {

    final class DemoJavaScriptInterface {

        DemoJavaScriptInterface() {
        }

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        public void clickOnAndroid() {
            mHandler.post(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:wave()");
                }
            });

        }
    }

    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(LOG_TAG, message);
            result.confirm();
            return true;
        }
    }

    private static final String LOG_TAG = "WebView";

    private Handler mHandler = new Handler();

    private WebView mWebView;

    String url;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        url = getIntent().getData().toString();
        setContentView(R.layout.web_view_activity);
        boolean newUser = getIntent().getBooleanExtra("signup", false);

        if(newUser){
            TextView tv =(TextView)findViewById(R.id.text_webview);
            tv.setText("signup");
        }else{
            TextView tv =(TextView)findViewById(R.id.text_webview);
            tv.setText("login");
        }

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);

        //mWebView.setWebChromeClient(new MyWebChromeClient());

        //mWebView.addJavascriptInterface(new DemoJavaScriptInterface(), "demo");

        mWebView.loadUrl(url);
    }

}