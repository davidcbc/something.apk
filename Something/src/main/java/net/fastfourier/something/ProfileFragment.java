package net.fastfourier.something;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.fastlibrary.util.FastUtils;

import net.fastfourier.something.request.ProfileRequest;
import net.fastfourier.something.util.Constants;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ProfileFragment extends FastFragment {
    public WebView profileView;

    public ProfileFragment() {
        super(R.layout.profile_fragment, R.menu.post_reply);
    }

    private int userid = 0;
    private String pageHtml = "";
    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        profileView = (WebView) frag.findViewById(R.id.profile_webview);
        initWebview();
        startRefresh();
    }

    private static final String PROFILE_REQUEST_TAG = "profile_request";
    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        profileView.stopLoading();
        FastVolley.cancelRequestByTag(PROFILE_REQUEST_TAG);
        queueRequest(new ProfileRequest(userid, pageListener, errorListener), PROFILE_REQUEST_TAG);

    }

    private Response.Listener<ProfileRequest.ProfileData> pageListener = new Response.Listener<ProfileRequest.ProfileData>() {
        @Override
        public void onResponse(ProfileRequest.ProfileData response) {

            Log.e("response html",response.htmlData);
            profileView.loadDataWithBaseURL(Constants.BASE_URL, response.htmlData, "text/html", "utf-8", null);
            //profileView.loadData(response.htmlData, "text/html", "utf-8");
            pageHtml = response.htmlData;
            //post update to threadlist/activity
            SomeApplication.bus.post(response);


            invalidateOptionsMenu();

        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();

            profileView.onResume();
        profileView.resumeTimers();


    }
    public void loadProfile(int userid) {
        this.userid = userid;
        startRefresh();
    }

    private void initWebview() {
        profileView.getSettings().setJavaScriptEnabled(true);
        profileView.setWebChromeClient(chromeClient);
        profileView.setWebViewClient(webClient);

        //profileView.setBackgroundColor(Color.BLACK);

        registerForContextMenu(profileView);
    }


    private WebChromeClient chromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: " + newProgress);
            setProgress(newProgress);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("WebChromeClient", consoleMessage.lineNumber()+" - "+consoleMessage.messageLevel()+" - "+consoleMessage.message());
            return true;
        }
    };

    private WebViewClient webClient = new WebViewClient(){
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("WebView", "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("WebView", "onPageFinished: " + url);
            setProgress(100);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("WebView", "onLoadResource: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("WebView", "shouldOverrideUrlLoading: "+url);
            FastUtils.startUrlIntent(getActivity(), url);
            return true;
        }
    };
}
