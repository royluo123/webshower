package com.web.demo;

import android.webkit.WebView;

/**
 * 
 * <b>PageListener简介:</b>
 * <p>
 * 页面加载状态监听器
 * </p>
 * 
 * <b>功能描述:</b>
 * <p>
 * 监听页面加载状态：加载开始、加载进度、加载结束、加载失败。
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
public interface PageListener {

	public void onPageStarted(String url);

	public void onPageProgress(String url);

	public void onPageFinished(WebView view,String url);

	public void onPageFailed(String url);
}
