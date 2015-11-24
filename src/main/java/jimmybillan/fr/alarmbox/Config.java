package jimmybillan.fr.alarmbox;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by user on 23/11/15.
 */
abstract class Config {

    static String filePath = "dataAlarm";

    public static boolean isFileStorageExist(){
        return (new File(filePath)).exists();
    }

    public static void initDataStorage(Context context){

    }
}
