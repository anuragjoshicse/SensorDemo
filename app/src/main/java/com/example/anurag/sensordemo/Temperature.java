package com.example.anurag.sensordemo;

import android.bluetooth.BluetoothAdapter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Temperature extends ActionBarActivity {

    TGDevice mTGDevice;
    BluetoothAdapter mBtAdapter;
    TextView mTVpoor_signal;

    TextView textTEMPERATURE_available;

    int meditationValue = 0, attentionValue = 0;

    float onTemperatureSensorChangedValue = 0;

    File file;
    FileWriter writer;
    Long millis;
    Button startTemp, stopTemp;

    Boolean checkStart = false, poorSignalCheck = false;

    SensorManager mySensorManager;
    Sensor tempSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_temperature);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter != null) {
            mTGDevice = new TGDevice(mBtAdapter, handler);
            mTGDevice.connect(true);
            mTVpoor_signal = (TextView) super.findViewById(R.id.poorSignalValueForTemperature);
        }

        textTEMPERATURE_available = (TextView) super.findViewById(R.id.tempAvailable);
        mTVpoor_signal= (TextView)super.findViewById(R.id.poorSignalValueForTemperature);

        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        tempSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (tempSensor != null) {
            textTEMPERATURE_available.setText("Sensor.TYPE_LIGHT Available");
            mySensorManager.registerListener(
                    AmbientTemperatureSensorListener,
                    tempSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            textTEMPERATURE_available.setText("Sensor.TYPE_AMBIENT_TEMPERATURE NOT Available");
        }
        startTemp = (Button) super.findViewById(R.id.startTemp);
        startTemp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (poorSignalCheck) {
                    checkStart = true;
                    startTemp.setText("Started");
                }
                else {
                    Toast.makeText(getApplicationContext(), "   Neurosky headset ain't connected. Please try again!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        stopTemp = (Button) super.findViewById(R.id.stopTemp);
        stopTemp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStart = false;
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startTemp.setText("Start");
                stopTemp.setText("Stopped");
            }
        });
        file = new File("/sdcard/AttentionAssist/TemperatureSensor.csv");
        try {
            writer = new FileWriter(file);
            writer.write("TimeStamp,TemperatureReadings,AttentionValue,MeditationValue\n");
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

    private final SensorEventListener AmbientTemperatureSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                onTemperatureSensorChangedValue = event.values[0];
                millis = System.currentTimeMillis();
                if (checkStart) {
                    try {
                        writer = new FileWriter(file, true);
                        writer.write(millis + "," + onTemperatureSensorChangedValue + "," + attentionValue + "," + meditationValue + "\n");
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
        getMenuInflater().inflate(R.menu.menu_temperature, menu);
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
