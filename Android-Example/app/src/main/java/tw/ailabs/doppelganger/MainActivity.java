package tw.ailabs.doppelganger;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IFaceApiCallback {
    private static final String TAG = "MainActivity";
    private Button mShareButton;
    private TextView mScoreAlike;
    private TextView mResultText;
    private TextView mPrivacyTerms;
    private TextView mStartTip;
    private RelativeLayout mScreenShotArea;
    private TextView mPoweredText;
    private ImageView mPoweredIcon;

    protected boolean mbAuthorized = false;

    private final static int PHOTO_FROM_CAMERA = 0;
    private final static int PHOTO_FROM_FILE = 1;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_SELECT_PHOTO = 2;

    private static final int REQUEST_STREAM  = 1;
    private static String[] PERMISSIONS_STREAM = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int mCurrentSetPhoto;
    private String mCurrentPhotoPath;
    private byte[][] mImageByteArray = new byte[2][];
    private ImageView[] mImageViews = new ImageView[2];
    private ProgressBar mLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Optionally, you can just use the default CookieManager
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);

        setContentView(R.layout.activity_main);
        mShareButton = findViewById(R.id.share_button);
        mShareButton.setVisibility(View.INVISIBLE);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File snapshot = takeScreenshot();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);

                // Continue only if the File was successfully created
                if (snapshot != null) {
                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            "tw.ailabs.doppelganger.fileprovider",
                            snapshot);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    shareIntent.setType("image/jpeg");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));
                }
            }
        });

        mLoadingProgress = findViewById(R.id.loading_progress);
        mLoadingProgress.setVisibility(View.INVISIBLE);

        mPoweredText = findViewById(R.id.powered_by_ailabs);
        mPoweredIcon = findViewById(R.id.powered_by_ailabs_icon);
        mScreenShotArea = findViewById(R.id.screen_shot_area);

        mImageViews[Utils.LEFT_PHOTO] = findViewById(R.id.left_photo);
        mImageViews[Utils.LEFT_PHOTO].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentSetPhoto = Utils.LEFT_PHOTO;
                showClickPhotoAction();
            }
        });

        mImageViews[Utils.RIGHT_PHOTO] = findViewById(R.id.right_photo);
        mImageViews[Utils.RIGHT_PHOTO].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentSetPhoto = Utils.RIGHT_PHOTO;
                showClickPhotoAction();
            }
        });

        mScoreAlike = findViewById(R.id.score_alike);
        mScoreAlike.setVisibility(View.INVISIBLE);

        mResultText = findViewById(R.id.result_text);
        mResultText.setVisibility(View.INVISIBLE);

        mStartTip = findViewById(R.id.start_tip);

        mPrivacyTerms = findViewById(R.id.main_privacy_terms);
        mPrivacyTerms.setPaintFlags(mPrivacyTerms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mPrivacyTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEBVIEW_URL_EXTRA,
                        getResources().getString(R.string.privacy_terms_url));
                intent.putExtra(WebViewActivity.WEBVIEW_TITLE_EXTRA,
                        getResources().getString(R.string.privacy_terms));
                startActivity(intent);
            }
        });

        verifyPermissions();
    }

    public void verifyPermissions() {
        Log.d(TAG, "verifyPermissions");
        int CAMERA_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WRITE_EXTERNAL_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (CAMERA_permission != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STREAM,
                    REQUEST_STREAM
            );
            mbAuthorized = false;
            showRequestPermission();
        } else {
            mbAuthorized = true;
        }
    }

    protected void showRequestPermission() {
        Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.need_permission), Snackbar.LENGTH_LONG)
                .setAction("auth", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        verifyPermissions();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STREAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mbAuthorized = true;

            } else {
                showRequestPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            Glide.with(this).asBitmap().load(mCurrentPhotoPath).into(mImageViews[mCurrentSetPhoto]);

            // data part
            try {
                File file = new File(mCurrentPhotoPath);
                FileInputStream fileInputStream = new FileInputStream(file);
                mImageByteArray[mCurrentSetPhoto] = IOUtils.toByteArray(fileInputStream);
//                if(mImageByteArray[0] != null && mImageByteArray[1] != null)
                    FaceApis.getInstance(this).sendFaceAnalyze(mImageByteArray, mCurrentSetPhoto, this);
                mLoadingProgress.setVisibility(View.VISIBLE);
                mScoreAlike.setVisibility(View.INVISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(resultCode == RESULT_OK && requestCode == REQUEST_SELECT_PHOTO && data != null) {
            Uri originalUri = data.getData();
            Glide.with(this).load(originalUri).into(mImageViews[mCurrentSetPhoto]);

            // data part
            try {
                mImageByteArray[mCurrentSetPhoto] = IOUtils.toByteArray(getContentResolver().openInputStream(originalUri));
//                if(mImageByteArray[0] != null && mImageByteArray[1] != null)
                    FaceApis.getInstance(this).sendFaceAnalyze(mImageByteArray, mCurrentSetPhoto, this);
                mLoadingProgress.setVisibility(View.VISIBLE);
                mScoreAlike.setVisibility(View.INVISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showClickPhotoAction() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_photo_source)
                .setItems(R.array.photo_source_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch(which) {
                            case PHOTO_FROM_CAMERA:
                                dispatchTakePictureIntent();
                                break;
                            case PHOTO_FROM_FILE:
                                dispatchSelectPictureIntent();
                                break;
                        }

                    }
                });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "tw.ailabs.doppelganger.fileprovider",
                        photoFile);

                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Utils.getDefaultSaveDir(this);
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchSelectPictureIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/jpeg");
        startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    @Override
    public void onApiCompleted(String apiType, Object result) {
        if(apiType.equals(FaceApis.API_SEND_FACE_ANALYZE)) {
            NetworkResponse response = (NetworkResponse) result;
            String json_text = new String(response.data);
            Log.d(TAG, "response: \n" + json_text);
            JSONObject json;
            try {
                json = new JSONObject(json_text);
                final int score = json.optInt("result");
                Log.i(TAG, "score = "+score);
                final String message = json.optString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingProgress.setVisibility(View.INVISIBLE);
                        mStartTip.setVisibility(View.INVISIBLE);
                        mScoreAlike.setVisibility(View.VISIBLE);
                        mScoreAlike.setText(String.valueOf(score));
                        // make this description text editable and focus
                        mResultText.setVisibility(View.VISIBLE);
                        mResultText.setText(message);
                        mResultText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mResultText, InputMethodManager.SHOW_IMPLICIT);
                        mShareButton.setVisibility(View.VISIBLE);
                    }
                });
            } catch (JSONException e) {
//                e.printStackTrace();
                Log.i(TAG, "json_text content: "+json_text);
            }
        }
    }

    @Override
    public void onApiError(String apiType, final VolleyError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    private File takeScreenshot() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        try {
            // image naming and path to include sd card appending name you choose for file
            String mPath = Utils.getDefaultSaveDir(this) + "/" + getResources().getString(R.string.app_name)+ "-"+ timeStamp + ".jpg";

            // create bitmap screen capture
            mPoweredText.setVisibility(View.VISIBLE);
            mPoweredIcon.setVisibility(View.VISIBLE);
            mShareButton.requestFocus();

            mScreenShotArea.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mScreenShotArea.getDrawingCache());
            mScreenShotArea.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            mPoweredText.setVisibility(View.INVISIBLE);
            mPoweredIcon.setVisibility(View.INVISIBLE);
            galleryAddPic(mPath);
            return imageFile;
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
        return null;
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
