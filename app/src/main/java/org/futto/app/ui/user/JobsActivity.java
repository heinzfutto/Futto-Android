package org.futto.app.ui.user;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fasterxml.jackson.core.TreeNode;
import com.google.api.client.http.HttpResponse;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.PersistentData;

import java.io.IOException;

public class JobsActivity extends SessionActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_jobs);

//        sendSession();
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mWebView = new WebView(this);
        String url = "http://www.findyourdreamjob.org/Home.aspx?sessionID="+
                PersistentData.getPassword()+EncryptionEngine.safeHash(PersistentData.getPatientID());
        mWebView.loadUrl(url);
//        System.out.println(url);
//        System.out.println(PersistentData.getPassword());
//        System.out.println(EncryptionEngine.safeHash(PersistentData.getPassword()));
//        System.out.println(PersistentData.getPatientID());
//        System.out.println(EncryptionEngine.safeHash(PersistentData.getPatientID()));
//        mWebView.loadUrl("http://www.findyourdreamjob.org/");
//        http://www.findyourdreamjob.org/Home.aspx?sessionID=jZae727K08KaOmKSgOaGzww_XVqGr_PKEgIMkjrcbJI=5tiY8qfXWFnatDwNH9SzXgAlVDktfb5hvqswLVJh7s0=
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        this.setContentView(mWebView);
    }

    private void sendSession(){
        DefaultHttpClient httpclient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("http://www.findyourdreamjob.org/");
        try {
            httpclient.execute(httpget, localContext);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
