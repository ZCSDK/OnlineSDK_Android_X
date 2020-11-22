package com.sobot.online.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.common.utils.SobotLogUtils;
import com.sobot.common.utils.SobotNetworkUtils;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotStringUtils;
import com.sobot.online.base.SobotOnlineBaseActivity;
import com.sobot.online.weight.toast.SobotToastUtil;


@SuppressLint("SetJavaScriptEnabled")
public class OnlineWebViewActivity extends SobotOnlineBaseActivity implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private RelativeLayout sobot_rl_net_error;

    private TextView sobot_btn_reconnect;
    private TextView sobot_txt_loading;
    private TextView sobot_textReConnect;

    private String mUrl = "";
    private LinearLayout sobot_webview_toolsbar;
    private ImageView sobot_webview_goback;
    private ImageView sobot_webview_forward;
    private ImageView sobot_webview_reload;
    private ImageView sobot_webview_copy;

    //根据冲入的url判断是否url   true:是；false:不是
    private boolean isUrlOrText = true;

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_activity_webview");
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra("url"))) {
                mUrl = getIntent().getStringExtra("url");
                isUrlOrText = SobotStringUtils.isURL(mUrl);
            }
        } else {
            mUrl = savedInstanceState.getString("url");
            isUrlOrText = SobotStringUtils.isURL(mUrl);
        }
    }

    @Override
    protected void initView() {
        setHearderTitle("");
        mWebView = (WebView) findViewById(getResId("sobot_mWebView"));
        mProgressBar = (ProgressBar) findViewById(getResId("sobot_loadProgress"));
        sobot_rl_net_error = (RelativeLayout) findViewById(getResId("sobot_rl_net_error"));
        sobot_webview_toolsbar = (LinearLayout) findViewById(getResId("sobot_webview_toolsbar"));
        sobot_btn_reconnect = (TextView) findViewById(getResId("sobot_btn_reconnect"));
        sobot_btn_reconnect.setText(SobotResourceUtils.getResString(OnlineWebViewActivity.this, "online_click_reload"));
        sobot_btn_reconnect.setOnClickListener(this);
        sobot_textReConnect = (TextView) findViewById(getResId("sobot_textReConnect"));
        sobot_textReConnect.setText(SobotResourceUtils.getResString(OnlineWebViewActivity.this, "online_current_no_network"));
        sobot_webview_goback = (ImageView) findViewById(getResId("sobot_webview_goback"));
        sobot_webview_forward = (ImageView) findViewById(getResId("sobot_webview_forward"));
        sobot_webview_reload = (ImageView) findViewById(getResId("sobot_webview_reload"));
        sobot_webview_copy = (ImageView) findViewById(getResId("sobot_webview_copy"));
        sobot_webview_goback.setOnClickListener(this);
        sobot_webview_forward.setOnClickListener(this);
        sobot_webview_reload.setOnClickListener(this);
        sobot_webview_copy.setOnClickListener(this);
        sobot_webview_goback.setEnabled(false);
        sobot_webview_forward.setEnabled(false);


        displayInNotchByPadding(mWebView);

        resetViewDisplay();
        initWebView();
        if (isUrlOrText) {
            //加载url
            mWebView.loadUrl(mUrl);
            sobot_webview_copy.setVisibility(View.VISIBLE);
        } else {
            //修改图片高度为自适应宽度
            mUrl = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "    <head>\n" +
                    "        <meta charset=\"utf-8\">\n" +
                    "        <title></title>\n" +
                    "        <style>\n" +
                    "            img{\n" +
                    "                width: auto;\n" +
                    "                height:auto;\n" +
                    "                max-height: 100%;\n" +
                    "                max-width: 100%;\n" +
                    "            }\n" +
                    "        </style>\n" +
                    "    </head>\n" +
                    "    <body>" + mUrl + "  </body>\n" +
                    "</html>";
            //显示文本内容
            mWebView.loadData(mUrl, "text/html; charset=UTF-8", null);
            sobot_webview_copy.setVisibility(View.GONE);
        }
        SobotLogUtils.i("webViewActivity---" + mUrl);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {
        if (view == sobot_btn_reconnect) {
            if (!TextUtils.isEmpty(mUrl)) {
                resetViewDisplay();
            }
        } else if (view == sobot_webview_forward) {
            mWebView.goForward();
        } else if (view == sobot_webview_goback) {
            mWebView.goBack();
        } else if (view == sobot_webview_reload) {
            mWebView.reload();
        } else if (view == sobot_webview_copy) {
            copyUrl(mUrl);
        }
    }

    private void copyUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            SobotLogUtils.i("API是大于11");
            android.content.ClipboardManager cmb = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(url);
            cmb.getText();
        } else {
            SobotLogUtils.i("API是小于11");
            android.text.ClipboardManager cmb = (android.text.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(url);
            cmb.getText();
        }
        SobotToastUtil.showCustomToast(getSobotActivity(), getResString("online_ctrl_v_success"));
    }

    /**
     * 根据有无网络显示不同的View
     */
    private void resetViewDisplay() {
        if (SobotNetworkUtils.isConnected()) {
            mWebView.setVisibility(View.VISIBLE);
            sobot_webview_toolsbar.setVisibility(View.VISIBLE);
            sobot_rl_net_error.setVisibility(View.GONE);
        } else {
            mWebView.setVisibility(View.GONE);
            sobot_webview_toolsbar.setVisibility(View.GONE);
            sobot_rl_net_error.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NewApi")
    private void initWebView() {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            } catch (Exception e) {
                //ignor
            }
        }
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //检测到下载文件就打开系统浏览器
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri content = Uri.parse(url);
                intent.setData(content);
                startActivity(intent);
                finish();
            }
        });
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(16);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置可以使用localStorage
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
//        mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

        //关于webview的http和https的混合请求的，从Android5.0开始，WebView默认不支持同时加载Https和Http混合模式。
        // 在API>=21的版本上面默认是关闭的，在21以下就是默认开启的，直接导致了在高版本上面http请求不能正确跳转。
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        // 应用可以有数据库
        mWebView.getSettings().setDatabaseEnabled(true);

        // 应用可以有缓存
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //注释的地方是打开其它应用，比如qq
                /*if (url.startsWith("http") || url.startsWith("https")) {
                    return false;
                } else {
                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(in);
                    return true;
                }*/
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                sobot_webview_goback.setEnabled(mWebView.canGoBack());
                sobot_webview_forward.setEnabled(mWebView.canGoForward());
                if (isUrlOrText && !mUrl.replace("http://", "").replace("https://", "").equals(view.getTitle())) {
                    setTitle(view.getTitle());
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                SobotLogUtils.i("网页--title---：" + title);
                if (isUrlOrText && !mUrl.replace("http://", "").replace("https://", "").equals(title)) {
                    setHearderTitle(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 0 && newProgress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                } else if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                chooseAlbumPic();
                return true;
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            final ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //被摧毁前缓存一些数据
        outState.putString("url", mUrl);
        super.onSaveInstanceState(outState);
    }

    private static final int REQUEST_CODE_ALBUM = 0x0111;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    public Context getContext() {
        return OnlineWebViewActivity.this;
    }

    /**
     * 选择相册照片
     */
    private void chooseAlbumPic() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), REQUEST_CODE_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ALBUM) {
            if (uploadMessage == null && uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                //一定要返回null,否则<input file> 就是没有反应
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;

                }
            }

            if (resultCode == RESULT_OK) {
                Uri imageUri = null;
                switch (requestCode) {
                    case REQUEST_CODE_ALBUM:

                        if (data != null) {
                            imageUri = data.getData();
                        }
                        break;
                }

                //上传文件
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(imageUri);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                    uploadMessageAboveL = null;
                }
            }
        }
    }
}