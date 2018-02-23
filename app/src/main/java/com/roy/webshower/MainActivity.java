package com.roy.webshower;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.web.demo.CustomWebView;
import com.web.demo.CustomWebViewClient;
import com.web.demo.HtmlJSInterface;
import com.web.demo.PageListener;
import com.web.demo.history.HistoryActivity;
import com.web.demo.history.HistoryDatabaseHelper;

/**
 * 
 * <b>MainActivity简介:</b>
 * <p>
 * 主Activity
 * </p>
 * 
 * <b>功能描述:</b>
 * <p>
 * 具体功能点描述，当添加新功能时，要求更新此描述
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
public class MainActivity extends Activity implements PageListener, View.OnClickListener, OnTouchListener, Observer {
	private static final String TAG = "MainActivity";
	private static final int HISTORY_REQUEST_CODE = 1;
	private static final String MAIN_PAGE_PATH = "file:///android_asset/startpage/hao123.htm";
	private String mExternalStoragePath = Environment.getExternalStorageDirectory() + "/ucdemo/";

	protected LayoutInflater mInflater;
	private Drawable mCircularProgress;
	private EditText mUrlEditText;
	private CustomWebView mCurrentWebView;
	private List<CustomWebView> mWebViews = new ArrayList<CustomWebView>();
	private ViewFlipper mViewFlipper;
	private ProgressBar mWebViewProgress;
	/** the tips to show the count of tabs */
	private TextView mNewTabTips;
	private ImageButton mNewTabButton, mRemoveTabBtn, mPreviousBtn, mNextBtn, mGoBtn;
	/** The first time the back keyboard pressed */
	private long mFirstKeyBackClickTime;
	/** The flag deciding the back keyboard pressed */
	private boolean mExitFlag;

	private String mCurrentLoadPageTitle;

	private String mCurrentHtmlSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setProgressBarVisibility(true);
		setContentView(R.layout.main);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Log.d(TAG, "Build:" + Build.VERSION.RELEASE);

		mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
		mUrlEditText = (EditText) findViewById(R.id.UrlText);
		mWebViewProgress = (ProgressBar) findViewById(R.id.WebViewProgress);

		mNewTabButton = (ImageButton) findViewById(R.id.NewTabBtn);
		mNewTabButton.setOnClickListener(this);

		mRemoveTabBtn = (ImageButton) findViewById(R.id.RemoveTabBtn);
		mRemoveTabBtn.setOnClickListener(this);

		mNewTabTips = (TextView) findViewById(R.id.newtab_txt_tips);

		mPreviousBtn = (ImageButton) findViewById(R.id.PreviousBtn);
		mPreviousBtn.setOnClickListener(this);
		mNextBtn = (ImageButton) findViewById(R.id.NextBtn);
		mNextBtn.setOnClickListener(this);

		mGoBtn = (ImageButton) findViewById(R.id.GoBtn);
		mGoBtn.setOnClickListener(this);

		// mCircularProgress = getResources().getDrawable(R.drawable.spinner);
		// mUrlEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
		// mCircularProgress, null);
		// ((AnimationDrawable) mCircularProgress).start();

		/** load welcome page */
		addTab(true, -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save_page:
			if (mCurrentHtmlSource != null) {
				new LoadImageAsyncTask().execute();
			} else {
				Toast.makeText(this, R.string.main_activity_page_loading, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.menu_history:
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, HistoryActivity.class);
			startActivityForResult(intent, HISTORY_REQUEST_CODE);
			break;
		case R.id.menu_bookmark:
			break;
		case R.id.menu_settings:
			break;
		case R.id.menu_about:
			break;
		case R.id.menu_exit:
			// TODO: if application is exsit ,it maybe have a bug
			finish();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	class LoadImageAsyncTask extends AsyncTask<String, Void, Void> {
		public LoadImageAsyncTask() {
		}

		@Override
		protected Void doInBackground(String... params) {
			File rootPath = new File(mExternalStoragePath);
			if (!rootPath.exists()) {
				rootPath.mkdir();
			}
			// 当前创建文件夹
			File htmlDir = new File(mExternalStoragePath + mCurrentLoadPageTitle);
			if (!htmlDir.exists()) {
				htmlDir.mkdir();
			}
			Document doc = Jsoup.parse(mCurrentHtmlSource);
			Elements elements = doc.getElementsByTag("img");
			if (elements.size() > 0) {
				try {
					String imgPath = elements.get(0).attr("src");
					String imgName = imgPath.substring(imgPath.lastIndexOf("/"));
					File imgFile = new File(htmlDir + imgName);
					URL url = new URL(imgPath);
					InputStream is = (InputStream) url.getContent();
					byte[] buffer = new byte[8192];
					int bytesRead;
					FileOutputStream fileOutput = new FileOutputStream(imgFile);
					while ((bytesRead = is.read(buffer)) != -1) {
						fileOutput.write(buffer, 0, bytesRead);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			for (int i = 0; i < elements.size(); i++) {
				Log.d(TAG, elements.get(i).attr("src"));
				String imgsPath = elements.get(i).attr("src");
				String imgName = imgsPath.substring(imgsPath.lastIndexOf("/"));
				elements.get(i).attr("src", "./" + mCurrentLoadPageTitle + imgName);
			}

			File file1 = new File(mExternalStoragePath + mCurrentLoadPageTitle + ".html");
			String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
			Log.d(TAG, "Webview cache path:" + appCachePath);
			try {
				FileOutputStream fos = new FileOutputStream(file1);
				fos.write(doc.html().getBytes());
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(MainActivity.this, R.string.main_activity_page_save_complete, Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!mExitFlag) {
				mFirstKeyBackClickTime = System.currentTimeMillis();
				Toast.makeText(this, R.string.main_acvitity_exit_toast, Toast.LENGTH_SHORT).show();
				mExitFlag = true;
			} else {
				long currentTime = System.currentTimeMillis();
				if (currentTime - mFirstKeyBackClickTime <= 2000) {
					finish();
				} else {
					mExitFlag = false;
					mFirstKeyBackClickTime = currentTime;
					Toast.makeText(this, R.string.main_acvitity_exit_toast, Toast.LENGTH_SHORT).show();
				}
			}
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case HISTORY_REQUEST_CODE:
			if (resultCode == HistoryActivity.HISTORY_VIEW_RESULT_CODE) {
				if (data.getStringExtra("linkUrl") != null && !data.getStringExtra("linkUrl").equalsIgnoreCase("")) {
					mCurrentWebView.loadUrl(data.getStringExtra("linkUrl"));
				}
			} else if (resultCode == HistoryActivity.HISTORY_CLEARALL_RESULT_CODE) {
				// TODO
			} else if (resultCode == HistoryActivity.HISTORY_BACK) {
				// TODO
			}
			break;
		default:
			break;
		}
	}

	public void onPageStarted(String url) {
		mWebViewProgress.setVisibility(View.VISIBLE);
		mCurrentHtmlSource = null;
	}

	public void onPageProgress(String url) {
	}

	public void onPageFinished(WebView view, String url) {
		mWebViewProgress.setVisibility(View.GONE);
		/** not savety */
		view.loadUrl("javascript:(function() { " + "window.HTMLOUT.setHtml('<html>'+"
				+ "document.getElementsByTagName('html')[0].innerHTML+'</html>');})();");
		mCurrentLoadPageTitle = view.getTitle();
	}

	public void onPageFailed(String url) {
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.NewTabBtn:
			addTab(true, -1);
			break;
		case R.id.RemoveTabBtn:
			removeCurrentTab();
			break;
		case R.id.PreviousBtn:
			navigatePrevious();
			break;
		case R.id.NextBtn:
			navigateNext();
			break;
		case R.id.GoBtn:
			if (mUrlEditText.getText() != null && !mUrlEditText.getText().toString().equalsIgnoreCase("")) {
				mCurrentWebView.loadUrl(mUrlEditText.getText().toString());
			} else {
				Toast.makeText(this, R.string.main_activity_page_input_url, Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Navigate to the previous page in history.
	 */
	private void navigatePrevious() {
		mUrlEditText.clearFocus();
		mCurrentWebView.goBack();
	}

	/**
	 * Navigate to the next page in history.
	 */
	private void navigateNext() {
		mUrlEditText.clearFocus();
		mCurrentWebView.goForward();
	}

	/**
	 * Remove the current tab.
	 */
	private void removeCurrentTab() {
		int removeIndex = mViewFlipper.getDisplayedChild();
		if (removeIndex > 0) {
			mCurrentWebView.doOnPause();
			synchronized (mViewFlipper) {
				mViewFlipper.removeViewAt(removeIndex);
				mViewFlipper.setDisplayedChild(removeIndex - 1);
				mWebViews.remove(removeIndex);
			}
			mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
			if (mWebViews.size() >= 2) {
				mNewTabTips.setText(String.valueOf(mWebViews.size()));
			} else {
				mNewTabTips.setVisibility(View.GONE);
			}
			mUrlEditText.clearFocus();
		} else {
		}
	}

	private void addTab(boolean navigateToHome, int parentIndex) {
		mUrlEditText.setFocusable(false);
		if (mWebViews.size() < 5) {
			RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.webview, mViewFlipper, false);
			mCurrentWebView = (CustomWebView) view.findViewById(R.id.webview);
			initializeCurrentWebView();
			synchronized (mViewFlipper) {
				if (parentIndex != -1) {
					mWebViews.add(parentIndex + 1, mCurrentWebView);
					mViewFlipper.addView(view, parentIndex + 1);
				} else {
					mWebViews.add(mCurrentWebView);
					Log.d(TAG, "The size of webviews is :" + mWebViews.size());
					mViewFlipper.addView(view);
				}
				mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view));
			}
			if (mWebViews.size() >= 2) {
				mNewTabTips.setVisibility(View.VISIBLE);
				mNewTabTips.setText(String.valueOf(mWebViews.size()));
			} else {
				mNewTabTips.setVisibility(View.GONE);
			}
		} else {
			Toast.makeText(this, R.string.main_activity_max_tabs, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Initialize a newly created WebView.
	 */
	private void initializeCurrentWebView() {
		CustomWebViewClient customWebViewClient = new CustomWebViewClient(this);
		customWebViewClient.setPageListener(this);
		mCurrentWebView.setWebViewClient(customWebViewClient);

		/**
		 * ------Bind the java Object to javaScript. Not suggestion to enable
		 * javaScript in the html------
		 **/
		HtmlJSInterface htmlJSInterface = new HtmlJSInterface();
		mCurrentWebView.addJavascriptInterface(htmlJSInterface, "HTMLOUT");
		htmlJSInterface.addObserver(this);

		mCurrentWebView.setOnTouchListener(this);
		mCurrentWebView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			}
		});

		mCurrentWebView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
			}
		});

		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onHideCustomView() {
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				((CustomWebView) view).setProgress(newProgress);
				mWebViewProgress.setProgress(newProgress);
			}

			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				super.onReceivedIcon(view, icon);
			}

			@Override
			public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				addTab(false, mViewFlipper.getDisplayedChild());
				transport.setWebView(mCurrentWebView);
				resultMsg.sendToTarget();
				return true;
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				SQLiteDatabase historyDatabase = null;
				try {
					historyDatabase = new HistoryDatabaseHelper(MainActivity.this).getWritableDatabase();
					if (!URLUtil.isFileUrl(view.getUrl())) {
						ContentValues initialValues = new ContentValues();
						initialValues.put(HistoryDatabaseHelper.ROW_URL_TITLE, title);
						initialValues.put(HistoryDatabaseHelper.ROW_URL_LINK, view.getUrl());
						historyDatabase.insert(HistoryDatabaseHelper.TABLE_NAME, null, initialValues);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (historyDatabase != null) {
						historyDatabase.close();
					}
				}
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
				return true;
			}
		});
		mCurrentWebView.loadUrl(MAIN_PAGE_PATH);
	}

	public boolean onTouch(View view, MotionEvent motionevent) {
		return false;
	}

	public void update(Observable observable, Object observation) {
		if (observable instanceof HtmlJSInterface) {
			String html = (String) observation;
			onHtmlChanged(html);
		}
	}

	private void onHtmlChanged(String html) {
		Log.d(TAG, "external storage path:" + Environment.getExternalStorageDirectory());
		mCurrentHtmlSource = html;
	}
}
