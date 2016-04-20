package com.example.anurag.sensordemo;

import android.bluetooth.BluetoothAdapter;
import android.media.MediaRecorder;
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
import java.util.Timer;
import java.util.TimerTask;


public class Sound extends ActionBarActivity {

    TGDevice mTGDevice;
    BluetoothAdapter mBtAdapter;
    TextView mTVpoor_signal;

    int meditationValue = 0, attentionValue = 0;

    File file;
    FileWriter writer;
    Long millis;
    Button startSound, stopSound;
    Boolean checkStart = false, poorSignalCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter != null) {
            mTGDevice = new TGDevice(mBtAdapter, handler);
            mTGDevice.connect(true);
            mTVpoor_signal = (TextView) super.findViewById(R.id.poorSignalValueForSound);
        }


        mTVpoor_signal = (TextView) super.findViewById(R.id.poorSignalValueForSound);

        MediaRecorder recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new RecorderTask(recorder), 0, 500);

        recorder.setOutputFile("/dev/null");

        try {
            recorder.prepare();
            recorder.start();
        } catch(IllegalStateException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startSound = (Button) this.findViewById(R.id.SoundStart);
        startSound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (poorSignalCheck) {
                    checkStart = true;
                    startSound.setText("Started");
                } else {
                    Toast.makeText(getApplicationContext(), "Neurosky headset ain't connected. Please try again!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        stopSound = (Button) this.findViewById(R.id.stopSound);
        stopSound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkStart = false;
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startSound.setText("Start");
                stopSound.setText("Stopped");
            }
        });
        file = new File("/sdcard/AttentionAssist/SoundSensor.csv");
        try {
            writer = new FileWriter(file);
            writer.write("TimeStamp,SoundSensorValue,AttentionValue,MeditationValue\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RecorderTask extends TimerTask {
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;

        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int amplitude = recorder.getMaxAmplitude();
                    double amplitudeDb = 20 * Math.log10((double)Math.abs(amplitude));

                    millis = System.currentTimeMillis();
                    if (checkStart) {
                        try {
                            writer = new FileWriter(file, true);
                            writer.write(millis + "," + amplitudeDb + "," + attentionValue + "," + meditationValue + "\n");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sound, menu);
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