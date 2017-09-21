package tw.ailabs.doppelganger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by tony on 2017/9/19.
 */

public class WebViewActivity extends AppCompatActivity {
    public static final String WEBVIEW_URL_EXTRA = "webview_url";
    public static final String WEBVIEW_TITLE_EXTRA = "webview_title";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        String url = intent.getStringExtra(WEBVIEW_URL_EXTRA);
        String title = intent.getStringExtra(WEBVIEW_TITLE_EXTRA);
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle(title); // set the top title

        WebView web = findViewById(R.id.webview);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl(url);
    }
}
