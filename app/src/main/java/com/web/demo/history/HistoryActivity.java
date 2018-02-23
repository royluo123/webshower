package com.web.demo.history;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.roy.webshower.R;

public class HistoryActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "HistoryActivity";
	private HistoryAdapter mHistoryAdapter;
	private List<History> mHistoryList = new ArrayList<History>();

	private ListView mHistoryListView;

	public static final int HISTORY_VIEW_RESULT_CODE = 0;
	public static final int HISTORY_CLEARALL_RESULT_CODE = 1;
	public static final int HISTORY_BACK = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		mHistoryListView = (ListView) findViewById(R.id.listview);
		SQLiteDatabase historyDatabase = new HistoryDatabaseHelper(this).getReadableDatabase();
		try {
			String[] columns = new String[] { HistoryDatabaseHelper.ROW_URL_TITLE, HistoryDatabaseHelper.ROW_URL_LINK };
			Cursor cursor = historyDatabase.query(HistoryDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
			cursor.moveToLast();
			do {
				History history = new History();
				history.setTitle(cursor.getString(0));
				history.setUrlLink(cursor.getString(1));
				Log.d(TAG, cursor.getString(0) + "," + cursor.getString(1));
				mHistoryList.add(history);
			} while (cursor.moveToPrevious());
			cursor.close();
			mHistoryAdapter = new HistoryAdapter(this, mHistoryList);
			mHistoryListView.setAdapter(mHistoryAdapter);
			mHistoryAdapter.notifyDataSetChanged();
			mHistoryListView.setOnItemClickListener(this);
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		} finally {
			historyDatabase.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_history_clear:
			SQLiteDatabase historyDatabase = new HistoryDatabaseHelper(this).getWritableDatabase();
			try {
				historyDatabase.delete(HistoryDatabaseHelper.TABLE_NAME, null, null);
				mHistoryList.clear();
				mHistoryAdapter = new HistoryAdapter(this, mHistoryList);
				mHistoryListView.setAdapter(mHistoryAdapter);
				mHistoryAdapter.notifyDataSetChanged();
			} catch (Exception e) {
				Log.w(TAG, e.getMessage());
			} finally {
				historyDatabase.close();
			}
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(HISTORY_BACK);
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String linkUrl = mHistoryList.get(position).getUrlLink();
		Log.d(TAG, "The item position is " + position + ", link is:" + linkUrl);
		// 删除这条记录
		Intent intent = new Intent();
		intent.putExtra("linkUrl", linkUrl);

		setResult(HISTORY_VIEW_RESULT_CODE, intent);
		finish();
	}
}
