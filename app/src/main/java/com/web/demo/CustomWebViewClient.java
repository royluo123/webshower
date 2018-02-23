package com.web.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Convenient extension of WebViewClient.
 */
public class CustomWebViewClient extends WebViewClient {
	private PageListener mPageListener;

	public void setPageListener(PageListener pageListener) {
		this.mPageListener = pageListener;
	}

	public CustomWebViewClient(Context context) {
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		((CustomWebView) view).notifyPageFinished();
		mPageListener.onPageFinished(view, url);
		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		((CustomWebView) view).notifyPageStarted();
		mPageListener.onPageStarted(url);
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return false;
	}

	@Override
	public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, final String host, final String realm) {
	}
}
