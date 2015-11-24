package jimmybillan.fr.alarmbox;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity/* implements ServiceListenEnv.ServiceCallbacks*/ {

    ServiceListenEnv mService;
    boolean mBound = false;
    TextView level_soundTV;
    Handler handler = new Handler();


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MainActivity", "onServiceConnected   ");
            ServiceListenEnv.LocalBinder binder=(ServiceListenEnv.LocalBinder)service;
            mService = binder.getService();
           // mService.setCallbacks(MainActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        level_soundTV = (TextView) findViewById(R.id.sound_level);

        findViewById(R.id.btn_start_sleep_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    closeBindServiceListenEnv();

                }else {
                    Intent intent = new Intent(getApplicationContext(),ServiceListenEnv.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                }

            }
        });

        Config.initDataStorage(this);
    }


    @Override
    protected void onStop(){
        super.onStop();
        closeBindServiceListenEnv();
    }

    private void closeBindServiceListenEnv(){
        if(mBound){
            Log.i("MainActivity", "Close bind");
            //mService.setCallbacks(null);
            unbindService(mConnection);
            mBound = false;
        }
    }

/*
    @Override
    public void updatSoundLevel(Integer lvl) {
        level_soundTV.setText(lvl+"");
    }

    @Override
    public Handler getHandler() {
        return handler;
    }*/
}
