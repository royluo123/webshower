package com.web.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;

/**
 * 
 * <b>CustomWebView简介:</b>
 * <p>
 * 一个更加有效的Webview实现
 * </p>
 * 
 * <b>功能描述:</b>
 * <p>
 * 
 * </p>
 * 
 * <b>修改历史</b>
 * <p>
 * <ol>
 * <li>创建（Added by lizf on 2012-10-24）</li>
 * </ol>
 * </p>
 * 
 * @author lizf
 * @version 1.0
 */
public class CustomWebView extends WebView {

	private int mProgress = 100;
	private boolean mIsLoading = false;
	private String mLoadedUrl;
	private static boolean mBoMethodsLoaded = false;
	private static Method mOnPauseMethod = null;
	private static Method mOnResumeMethod = null;
	private static Method mSetFindIsUp = null;
	private static Method mNotifyFindDialogDismissed = null;

	public static String USER_AGENT_DEFAULT = "";
	public static String USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.44 Safari/534.7";

	public CustomWebView(Context context) {
		super(context);
		initializeOptions();
		loadMethods();
	}

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeOptions();
		loadMethods();
	}

	@SuppressWarnings("deprecation")
	public void initializeOptions() {
		WebSettings settings = getSettings();
		// User settings
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSaveFormData(true);
		settings.setSavePassword(true);
		settings.setDefaultZoom(ZoomDensity.valueOf(ZoomDensity.MEDIUM.toString()));
		settings.setUserAgentString(USER_AGENT_DEFAULT);
		CookieManager.getInstance().setAcceptCookie(true);
		if (Build.VERSION.SDK_INT <= 7) {
			settings.setPluginsEnabled(true);
		} else {
			settings.setPluginState(PluginState.ON_DEMAND);
		}
		settings.setSupportZoom(true);
		// Technical settings
		settings.setSupportMultipleWindows(true);
		setLongClickable(true);
		setScrollbarFadingEnabled(true);
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setDrawingCacheEnabled(true);
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();

		// Enable / disable zoom support in case of multiple pointer, e.g.
		// enable zoom when we have two down pointers, disable with one pointer
		// or when pointer up.
		// We do this to prevent the display of zoom controls, which are not
		// useful and override over the right bubble.
		if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_POINTER_DOWN)
				|| (action == MotionEvent.ACTION_POINTER_1_DOWN) || (action == MotionEvent.ACTION_POINTER_2_DOWN)
				|| (action == MotionEvent.ACTION_POINTER_3_DOWN)) {
			if (ev.getPointerCount() > 1) {
				this.getSettings().setBuiltInZoomControls(true);
				this.getSettings().setSupportZoom(true);
			} else {
				this.getSettings().setBuiltInZoomControls(false);
				this.getSettings().setSupportZoom(false);
			}
		} else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_POINTER_UP)
				|| (action == MotionEvent.ACTION_POINTER_1_UP) || (action == MotionEvent.ACTION_POINTER_2_UP)
				|| (action == MotionEvent.ACTION_POINTER_3_UP)) {
			this.getSettings().setBuiltInZoomControls(false);
			this.getSettings().setSupportZoom(false);
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void loadUrl(String url) {
		mLoadedUrl = url;
		super.loadUrl(url);
	}

	public void setProgress(int progress) {
		mProgress = progress;
	}

	public int getProgress() {
		return mProgress;
	}

	public void notifyPageStarted() {
		mIsLoading = true;
	}

	public void notifyPageFinished() {
		mProgress = 100;
		mIsLoading = false;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public String getLoadedUrl() {
		return mLoadedUrl;
	}

	public void resetLoadedUrl() {
		mLoadedUrl = null;
	}

	public boolean isSameUrl(String url) {
		if (url != null) {
			return url.equalsIgnoreCase(this.getUrl());
		}
		return false;
	}

	public void doOnPause() {
		if (mOnPauseMethod != null) {
			try {
				mOnPauseMethod.invoke(this);
			} catch (IllegalArgumentException e) {
				Log.e("CustomWebView", "doOnPause(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e("CustomWebView", "doOnPause(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e("CustomWebView", "doOnPause(): " + e.getMessage());
			}
		}
	}

	public void doOnResume() {
		if (mOnResumeMethod != null) {
			try {
				mOnResumeMethod.invoke(this);
			} catch (IllegalArgumentException e) {
				Log.e("CustomWebView", "doOnResume(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e("CustomWebView", "doOnResume(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e("CustomWebView", "doOnResume(): " + e.getMessage());
			}
		}
	}

	public void doSetFindIsUp(boolean value) {
		if (mSetFindIsUp != null) {
			try {
				mSetFindIsUp.invoke(this, value);
			} catch (IllegalArgumentException e) {
				Log.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e("CustomWebView", "doSetFindIsUp(): " + e.getMessage());
			}
		}
	}

	public void doNotifyFindDialogDismissed() {
		if (mNotifyFindDialogDismissed != null) {
			try {
				mNotifyFindDialogDismissed.invoke(this);
			} catch (IllegalArgumentException e) {
				Log.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e("CustomWebView", "doNotifyFindDialogDismissed(): " + e.getMessage());
			}
		}
	}

	/**
	 * Load static reflected methods.
	 */
	private void loadMethods() {
		if (!mBoMethodsLoaded) {
			try {
				mOnPauseMethod = WebView.class.getMethod("onPause");
				mOnResumeMethod = WebView.class.getMethod("onResume");
			} catch (SecurityException e) {
				Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mOnPauseMethod = null;
				mOnResumeMethod = null;
			} catch (NoSuchMethodException e) {
				Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mOnPauseMethod = null;
				mOnResumeMethod = null;
			}
			try {
				mSetFindIsUp = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
				mNotifyFindDialogDismissed = WebView.class.getMethod("notifyFindDialogDismissed");
			} catch (SecurityException e) {
				Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mSetFindIsUp = null;
				mNotifyFindDialogDismissed = null;
			} catch (NoSuchMethodException e) {
				Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
				mSetFindIsUp = null;
				mNotifyFindDialogDismissed = null;
			}
			mBoMethodsLoaded = true;
		}
	}
}
