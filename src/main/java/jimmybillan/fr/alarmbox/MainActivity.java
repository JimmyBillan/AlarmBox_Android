package jimmybillan.fr.alarmbox;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity/* implements ServiceListenEnv.ServiceCallbacks*/ {

  public String domain = "172.20.5.251";
  //  public String domain = "127.0.0.1";

    ServiceListenEnv mService;
    TextView level_soundTV;
    Handler handler = new Handler();
    private SwipeRefreshLayout mySwiper;
    private WebView webView;


    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

    private WebView myWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    public Handler getHandler(){
        return handler;
    }


    private class mWebChromeClient extends WebChromeClient {

        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {

            // Double check that we don't have any existing callbacks
            if(mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            // Set up the take picture intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:"+   photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            // Set up the intent to get an existing image
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            // Set up the intents for the Intent chooser
            Intent[] intentArray;
            if(takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebChromeClient(new mWebChromeClient());
        myWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("url", Uri.parse(url).getHost());
                if (Uri.parse(url).getHost().equals(domain)) {
                    // This is my web site, so do not override; let my WebView load the page
                    return false;
                }
                // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
            }
        });
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        myWebView.loadUrl("http://" + domain + ":3000");



        Runnable runnable = new Runnable() {
            public void run() {

                Boolean isActive = false;
                while(true) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Boolean tmp = Config.isAirplaneModeOn(getApplicationContext());
                    if(tmp){
                        if(!isActive){
                            getHandler().post(new Runnable() {
                                public void run() {

                                    ((Button) findViewById(R.id.btn_start_plane_mode)).setText(getResources().getString(R.string.planeActive));
                                    ((Button) findViewById(R.id.btn_start_plane_mode)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_airplanemode_active_white_48dp), null, null, null);

                                }
                            });
                             isActive = true;
                        }
                    }else{
                        if(isActive){
                            getHandler().post(new Runnable() {
                                public void run() {
                                    ((Button) findViewById(R.id.btn_start_plane_mode)).setText(getResources().getString(R.string.planeInactive));
                                    ((Button) findViewById(R.id.btn_start_plane_mode)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_airplanemode_inactive_white_48dp), null, null, null);
                                }
                            });

                            isActive = false;
                             }

                    }
                }
            }
        };

        new Thread(runnable).start();

        findViewById(R.id.btn_start_sleep_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ServiceListenEnv.isServiceRunning) {
                    stopService(new Intent(getApplicationContext(), ServiceListenEnv.class));
                } else {
                    startService(new Intent(getApplicationContext(), ServiceListenEnv.class));
                }

            }
        });

        findViewById(R.id.btn_start_plane_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT < 17) {
                    try {
                        Intent intentAirplaneMode = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                        intentAirplaneMode.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentAirplaneMode);
                    } catch (ActivityNotFoundException e) {
                        Log.e("exception", e + "");
                    }
                } else {
                    Intent intent1 = new Intent("android.settings.WIRELESS_SETTINGS");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            }
        });

        findViewById(R.id.imageButtonRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               myWebView.reload();
            }
        });


    }






    @Override
    protected void onStop(){
        super.onStop();
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // We set the WebViewClient to ensure links are consumed by the WebView rather
        // than passed to a browser if it can
        myWebView.setWebViewClient(new WebViewClient());
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
                // If there is not data, then we may have taken a photo
                if(mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
        return;
    }


}
