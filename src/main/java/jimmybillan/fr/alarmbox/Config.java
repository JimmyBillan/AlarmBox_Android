package jimmybillan.fr.alarmbox;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    public static void initDataStorage(Context context, int x){

        String s = x+"/";
        try
        {
            File traceFile = new File(context.getExternalFilesDir(null), "TraceFile.txt");
            if (!traceFile.exists())
                traceFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true /*append*/));
            writer.write(s);
            writer.close();

        }
        catch (IOException e)
        {
            Log.i("d", "Unable to write to the TraceFile.txt file.");
        }

    }


}
