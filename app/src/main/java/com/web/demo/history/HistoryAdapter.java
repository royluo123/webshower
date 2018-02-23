package com.web.demo.history;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.roy.webshower.R;

public class HistoryAdapter extends BaseAdapter {
	private static final String TAG = "HistoryAdapter";
	private LayoutInflater mListContainer;// 视图容器
	private List<History> mHistory;

	public HistoryAdapter(Context context, List<History> history) {
		mListContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		mHistory = history;
	}

	public int getCount() {
		return mHistory.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = mListContainer.inflate(R.layout.history_list_item, null);
			listItemView = new ListItemView();
			// 获取控件对象
			listItemView.historyTextTitle = (TextView) convertView.findViewById(R.id.text_title);
			listItemView.historyTextLink = (TextView) convertView.findViewById(R.id.text_link);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		Log.d(TAG, "The current listview item's:" + position + ", tilte= " + mHistory.get(position).getTitle());
		listItemView.historyTextTitle.setText(mHistory.get(position).getTitle());
		listItemView.historyTextLink.setText(mHistory.get(position).getUrlLink());
		return convertView;
	}

	static class ListItemView {
		// 自定义控件集合
		public TextView historyTextTitle;
		public TextView historyTextLink;
	}
}
