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
    TextView level_soundTV;
    Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        level_soundTV = (TextView) findViewById(R.id.sound_level);

        findViewById(R.id.btn_start_sleep_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ServiceListenEnv.isServiceRunning){
                    stopService(new Intent(getApplicationContext(), ServiceListenEnv.class));
                }else{
                    startService(new Intent(getApplicationContext(),ServiceListenEnv.class));
                }

            }
        });


    }


    @Override
    protected void onStop(){
        super.onStop();
    }


}
