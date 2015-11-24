package jimmybillan.fr.alarmbox;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by user on 23/11/15.
 */
public class ServiceListenEnv extends Service implements SensorEventListener {

    MediaRecorder recorder;
    private boolean isRecording = false;

    /* Afficher niveau de son
    private ServiceCallbacks serviceCallbacks;

    public interface ServiceCallbacks {
        void updatSoundLevel(Integer lvl);
        Handler getHandler();
    }


    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }*/

    public class LocalBinder extends Binder {
        ServiceListenEnv getService(){
            return ServiceListenEnv.this;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public void onCreate(){
        Log.i("service", "onCreate");
        if (!isRecording) {
            recorder = new MediaRecorder();

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


            try {
                recorder.setOutputFile("/dev/null");
                recorder.prepare();
                recorder.start();
                isRecording = true;
                Log.i("service", isRecording+"");// we are currently recording
                getDB();

            } catch (IllegalStateException e) {
                e.printStackTrace();
                microphoneAlreadyInUse();
            } catch (IOException e) {
                e.printStackTrace();
                releaseRecorder();
            }

        } else {
            releaseRecorder();
        }

    }

    private void releaseRecorder() {
        Log.i("service", "releaseRecorder");
        if (recorder != null) {
            isRecording = false; // stop recording
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    private void microphoneAlreadyInUse(){
        Toast toast = Toast.makeText(getApplicationContext(), "Une application utilise déjà le microphone", Toast.LENGTH_SHORT);
        toast.show();
        recorder.release();
        recorder = null;
    }

    public void getDB(){

        Runnable runnable = new Runnable() {
            public void run() {
                    while(isRecording) {
                        try {
                            Thread.sleep(100);
                            int x = recorder.getMaxAmplitude();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                       /*
                       Afficher niveau de son
                       if (serviceCallbacks != null) {
                            serviceCallbacks.getHandler().post(new Runnable() {
                            public void run() {
                                int x = recorder.getMaxAmplitude();

                                    serviceCallbacks.updatSoundLevel(x);


                            }});
                        }*/
                    }

            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("onDestroy", "onStartCommand");
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        Log.i("onDestroy", "override onDestroy");
        releaseRecorder();
        //mySensorManager.unregisterListener(this);
        super.onDestroy();
    }


}
