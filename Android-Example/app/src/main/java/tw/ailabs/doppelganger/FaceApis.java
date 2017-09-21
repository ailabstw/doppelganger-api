package tw.ailabs.doppelganger;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by tony on 2017/9/18.
 */

public class FaceApis {

    private final Context mContext;
    protected final RequestQueue mRequestQueue;
    private static final String TAG = "FaceApis";
    private volatile static FaceApis mRestApis;

    public static final String API_SEND_FACE_ANALYZE = "sendFaceAnalyze";

    public static final String LINE_END = "\r\n";
    public static final String FORM_BOUNDARY = "apiclient-" + System.currentTimeMillis();
    public static final String TWO_HYPHENS = "--";
    public static final int MAX_BUFFER_SIZE = 1024 * 1024;

    public static final String FACE_ANALYZE_API_URL = "https://api.ailabs.tw/doppelganger/upload";

    public static FaceApis getInstance(Context context) {
        if (mRestApis == null) {
            synchronized (FaceApis.class) {
                if (mRestApis == null) {
                    mRestApis = new FaceApis(context);
                }
            }
        }
        return mRestApis;
    }

    public FaceApis(Context context) {

        mContext = context;

        Log.d(TAG, "Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Enable tls");
            HttpStack stack;
            try {
                stack = new HurlStack(null, new TLSSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
                Log.d(TAG, "Could not create new stack for TLS v1.1/v1.2");
                stack = new HurlStack();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.d(TAG, "Could not create new stack for TLS v1.1/v1.2");
                stack = new HurlStack();
            }
            mRequestQueue = Volley.newRequestQueue(context, stack);
        } else {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
    }

    public void sendFaceAnalyze(final byte[][] data, final int photoIndex, final IFaceApiCallback callback) {
        final String apiType = API_SEND_FACE_ANALYZE;
        Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Log.d(TAG, apiType + " onResponse : " + response.toString());

                if (callback != null) {
                    callback.onApiCompleted(apiType, response);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    Log.e(TAG, apiType + " onErrorResponse : " + error.toString());
                }
                if (callback != null)
                    callback.onApiError(apiType, error);
            }
        };

        final String mimeType = "multipart/form-data;boundary=" + FORM_BOUNDARY;
        BaseVolleyRequest baseVolleyRequest = new BaseVolleyRequest(Request.Method.POST, FACE_ANALYZE_API_URL,
                listener, errorListener) {

            @Override
            public String getBodyContentType() {
                return mimeType;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);

                try {
                    String name, filename;
//                    for(int i = 0 ; i < 2 ; ++i) {
                        if (photoIndex == Utils.LEFT_PHOTO) {
                            name = "left";
                            filename = "left.jpg";
                        } else {
                            name = "right";
                            filename = "right.jpg";
                        }

                        dos.writeBytes(TWO_HYPHENS + FORM_BOUNDARY + LINE_END);
                        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
                                + filename + "\"" + LINE_END);
                        dos.writeBytes("Content-Type: image/jpeg" + LINE_END);
                        dos.writeBytes(LINE_END);

                        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(data[photoIndex]);
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        // send multipart form data necessary after file data...
                        dos.writeBytes(LINE_END);
//                    }
                    dos.writeBytes(TWO_HYPHENS + FORM_BOUNDARY + TWO_HYPHENS + LINE_END);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bos.toByteArray();
            }
        };

        mRequestQueue.add(baseVolleyRequest);
    }
}
