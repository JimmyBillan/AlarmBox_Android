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
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class ServiceListenEnv extends Service implements SensorEventListener {

    private MediaRecorder recorder;
    private boolean isRecording = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private int[] oneSecondRecord;

    public static boolean isServiceRunning = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onCreate(){

        if (!isRecording) {
            recorder = new MediaRecorder();
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"serviceListenEnv");

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


            try {
                recorder.setOutputFile("/dev/null");
                recorder.prepare();
                recorder.start();
                isRecording = true;
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
        if (recorder != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        isServiceRunning = true;

        Runnable runnable = new Runnable() {
            public void run() {

                wakeLock.acquire();
                oneSecondRecord = new int[10];
                int i = 0;
                    while(isRecording) {
                        if(i == oneSecondRecord.length){
                            i = 0;
                            int average = 0;
                            for(int y = 0; y < oneSecondRecord.length - 1; y++){
                                average = average+ oneSecondRecord[y];
                            }
                            Config.initDataStorage(getApplicationContext(),Math.round(average/10));
                        }

                        try {
                            Thread.sleep(100);
                            if(recorder != null){
                                int x = recorder.getMaxAmplitude();
                                Log.i("lvl : ", x+"");
                                oneSecondRecord[i]= x;
                                i++;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
        super.onDestroy();
        wakeLock.release();
        isServiceRunning = false;
    }


}
