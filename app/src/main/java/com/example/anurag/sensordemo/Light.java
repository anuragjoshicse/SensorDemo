package com.example.anurag.sensordemo;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Light extends ActionBarActivity {

    TGDevice mTGDevice;
    BluetoothAdapter mBtAdapter;
    TextView mTVpoor_signal;

    TextView textLIGHT_available;

    int meditationValue = 0, attentionValue = 0;

    float onLightSensorChangedValue = 0;

    File file;
    FileWriter writer;
    Long millis;
    Button startLight, stopLight;

    Boolean checkStart = false, poorSignalCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter != null) {
            mTGDevice = new TGDevice(mBtAdapter, handler);
            mTGDevice.connect(true);
            mTVpoor_signal = (TextView) super.findViewById(R.id.poorSignalValueForLight);
        }

        textLIGHT_available= (TextView)super.findViewById(R.id.lightAvailable);
        mTVpoor_signal= (TextView)super.findViewById(R.id.poorSignalValueForLight);

        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null) {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT Available");
            mySensorManager.registerListener(
                    LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            textLIGHT_available.setText("Sensor.TYPE_LIGHT NOT Available");
        }
        startLight = (Button) super.findViewById(R.id.startLight);
        startLight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (poorSignalCheck) {
                    checkStart = true;
                    startLight.setText("Started");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Neurosky headset ain't connected. Please try again!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        stopLight = (Button) super.findViewById(R.id.stopLight);
        stopLight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStart = false;
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startLight.setText("Start");
                stopLight.setText("Stopped");
            }
        });
        file = new File("/sdcard/AttentionAssist/LightSensor.csv");
        try {
            writer = new FileWriter(file);
            writer.write("TimeStamp,LightSensorReadings,AttentionValue,MeditationValue\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;

                        case TGDevice.STATE_CONNECTING:
                            break;
                        case TGDevice.STATE_CONNECTED:
                            mTGDevice.start();

                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            mTGDevice.close();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                        case TGDevice.STATE_NOT_PAIRED://test paired
                        default:
                            break;
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    Log.d("Hello EEG ", "Poor Signal: " + msg.arg1);
                    mTVpoor_signal.setText("Poor Signal: " + msg.arg1);
                    if(msg.arg1==0)
                    {
                        poorSignalCheck = true;
                    }
                case TGDevice.MSG_ATTENTION:
                    attentionValue = msg.arg1;
                    break;
                case TGDevice.MSG_RAW_DATA:
                    break;
                case TGDevice.MSG_MEDITATION:
                    meditationValue = msg.arg1;
                    break;
                case TGDevice.MSG_BLINK:
                    break;
                case TGDevice.MSG_EEG_POWER:  //add to screen
                    TGEegPower ep = (TGEegPower) msg.obj;
                default:
                    break;
            }

        }
    };

    private final SensorEventListener LightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                onLightSensorChangedValue = event.values[0];
                millis = System.currentTimeMillis();
                if (checkStart) {
                    try {
                        writer = new FileWriter(file, true);
                        writer.write(millis + "," + onLightSensorChangedValue + "," + attentionValue + "," + meditationValue + "\n");
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_light, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
