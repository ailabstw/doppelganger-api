package tw.ailabs.doppelganger;

import com.android.volley.VolleyError;

/**
 * Created by tony on 2017/9/18.
 */

public interface IFaceApiCallback {
    void onApiCompleted(String apiType, Object result);
    void onApiError(String apiType, VolleyError error);
}
