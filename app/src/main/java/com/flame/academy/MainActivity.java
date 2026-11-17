package com.flame.academy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

	// URL web app Apps Script FLAME MUSIC ACADEMY
	private static final String APP_URL =
			"https://script.google.com/macros/s/AKfycbyl0xCH7bR8E1tBNdf3NGJJp3fKsavSGcqjPWcZLLxVFV8zap1jLXzwQcg1mwLcqmxB9g/exec";

	private static final int FILE_CHOOSER_REQUEST = 1001;

	private WebView webView;
	private ProgressBar progress;
	private ValueCallback<Uri[]> fileCallback;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setStatusBarColor(Color.parseColor("#0B0B0F"));
		getWindow().setNavigationBarColor(Color.parseColor("#0B0B0F"));

		FrameLayout root = new FrameLayout(this);
		root.setBackgroundColor(Color.parseColor("#0B0B0F"));

		webView = new WebView(this);
		webView.setBackgroundColor(Color.parseColor("#0B0B0F"));
		root.addView(webView, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
		progress.setIndeterminate(true);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 12);
		root.addView(progress, lp);

		setContentView(root);

		WebSettings s = webView.getSettings();
		s.setJavaScriptEnabled(true);
		s.setDomStorageEnabled(true);
		s.setDatabaseEnabled(true);
		s.setLoadWithOverviewMode(true);
		s.setUseWideViewPort(true);
		s.setMediaPlaybackRequiresUserGesture(false);
		s.setJavaScriptCanOpenWindowsAutomatically(true);
		s.setSupportMultipleWindows(false);
		s.setCacheMode(WebSettings.LOAD_DEFAULT);

		CookieManager cm = CookieManager.getInstance();
		cm.setAcceptCookie(true);
		cm.setAcceptThirdPartyCookies(webView, true);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				Uri uri = request.getUrl();
				String scheme = uri.getScheme();
				if ("http".equals(scheme) || "https".equals(scheme)) {
					return false; // tetap di dalam aplikasi
				}
				// mailto:, tel:, whatsapp:, intent: dll dibuka aplikasi lain
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, uri));
				} catch (Exception ignored) { }
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				progress.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				progress.setVisibility(View.GONE);
			}

			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				if (request.isForMainFrame()) {
					progress.setVisibility(View.GONE);
					view.loadDataWithBaseURL(null, offlineHtml(), "text/html", "UTF-8", null);
				}
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
											 FileChooserParams params) {
				if (fileCallback != null) fileCallback.onReceiveValue(null);
				fileCallback = callback;
				try {
					startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
				} catch (Exception e) {
					fileCallback = null;
					return false;
				}
				return true;
			}
		});

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(APP_URL);
		}
	}

	private String offlineHtml() {
		return "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
				+ "<style>body{background:#0b0b0f;color:#f5f5f7;font-family:sans-serif;display:flex;"
				+ "flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center}"
				+ "a{margin-top:18px;padding:12px 22px;background:#ff5a1f;color:#fff;border-radius:10px;text-decoration:none}</style>"
				+ "</head><body><div style='font-size:48px'>&#128293;</div>"
				+ "<h3>FLAME MUSIC ACADEMY</h3><p>Tidak ada koneksi internet.</p>"
				+ "<a href='" + APP_URL + "'>Coba lagi</a></body></html>";
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
			fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
			fileCallback = null;
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		webView.onPause();
		CookieManager.getInstance().flush();
	}

	@Override
	protected void onResume() {
		super.onResume();
		webView.onResume();
	}
}
