package ca.polymtl.squatr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Random;

public class LightGame extends AppCompatActivity implements SensorEventListener{

    private boolean lightOn = true;
    private boolean waiting = true;
    private double totalReactionTime;
    private int cmp = 0;
    private int highscore;
    private String flag;

    private SensorManager sensorManager;
    private final Random random = new Random();
    private int initialBatterieLevel = -1;

    private TextView intensiteTextView;
    private TextView totalReactionTimeTextView;
    private Chronometer reactionTime;
    private TextView mTbBatterie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_game);

        Bundle data = getIntent().getExtras();
        highscore = data.getInt("highscore");
        flag = data.getString("flag");
        initialBatterieLevel = data.getInt("Battery");

        // Créer une boite dialogue pour afficher les instuctions
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(R.string.instructionsLightGame);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        initializeGame();
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void initializeGame()
    {
        intensiteTextView = (TextView) findViewById(R.id.intensiteTextView);
        totalReactionTimeTextView = (TextView) findViewById(R.id.totalReactionTimeTextView);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        reactionTime = (Chronometer) findViewById(R.id.chrono);
        totalReactionTime = 0;

        lightOn = true;
        waiting = true;
        intensiteTextView.setText(R.string.lightGameInstructionInit);
        Task task = new Task();
        task.execute();

        //Écouter le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onStop() {
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch(Exception ignored){ }
        super.onStop();
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {

            //Récupérer le niveau actuel de la batterie
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            if(initialBatterieLevel == -1)
                initialBatterieLevel = level;

            int consommation  = initialBatterieLevel-level;
            //Consommation de batterie : 0%

            //Afficher la différence avec le niveau initial
            mTbBatterie.setText("Batterie : "+consommation+"%");
        }
    };

    private float lastValue;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(lightOn && !waiting && event.values[0] > 2){
            reactionTime.stop();
            if(lastValue > 2) {
                intensiteTextView.setText(R.string.lightGameInstructionShouldUncover);
                totalReactionTime += 1000;
            } else {
                intensiteTextView.setText(R.string.lightGameInstructionKeepUncover);
                totalReactionTime += SystemClock.elapsedRealtime() - reactionTime.getBase();
            }
            totalReactionTimeTextView.setText(String.valueOf(totalReactionTime));
            cmp++;
            if(cmp < 5) {
                waiting = true;
                Task task = new Task();
                task.execute();
            } else {
                EndGame();
            }
        }

        if(!lightOn && !waiting && event.values[0] < 2){
            reactionTime.stop();
            if(lastValue < 2) {
                intensiteTextView.setText(R.string.lightGameInstructionShouldCover);
                totalReactionTime += 1000;
            } else {
                intensiteTextView.setText(R.string.lightGameInstructionKeepCover);
                totalReactionTime += SystemClock.elapsedRealtime() - reactionTime.getBase();
            }
            totalReactionTimeTextView.setText(String.valueOf(totalReactionTime));
            cmp++;
            if(cmp < 5) {
                waiting = true;
                Task task = new Task();
                task.execute();
            } else {
                EndGame();
            }
        }

        lastValue = event.values[0];
    }

    private void EndGame() {
        sensorManager.unregisterListener(this);

        Intent data = new Intent();
        if(totalReactionTime<highscore || highscore==0)
            data.putExtra("highscore", totalReactionTime);
        else
            data.putExtra("highscore", 0);

        data.putExtra("flag",flag);
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int waitingTime = random.nextInt(8000 - 1000) + 1000;

            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            reactionTime.setBase(SystemClock.elapsedRealtime());
            reactionTime.start();

            if (lightOn) {
                lightOn = false;
                intensiteTextView.setText(R.string.lightGameInstructionCover);
            }
            else {
                lightOn = true;
                intensiteTextView.setText(R.string.lightGameInstructionUncover);
            }

            waiting = false;
        }


    }
}
