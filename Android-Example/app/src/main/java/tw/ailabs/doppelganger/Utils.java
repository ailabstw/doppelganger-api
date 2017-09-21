package tw.ailabs.doppelganger;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by tony on 2017/9/16.
 */

public class Utils {
    public static final int LEFT_PHOTO = 0;
    public static final int RIGHT_PHOTO = 1;

    public static File getDefaultSaveDir(Context context) {
        File appSaveDir =  new File(Environment.getExternalStorageDirectory(),
                context.getResources().getString(R.string.app_save_path));
        if (appSaveDir.exists() == false) {
            appSaveDir.mkdirs();
        }
        return appSaveDir;
    }
}
