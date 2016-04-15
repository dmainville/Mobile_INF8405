package ca.polymtl.squatr;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.SystemClock;
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
    private Sensor sensor;
    private Random random = new Random();

    private TextView intensiteTextView;
    private TextView totalReactionTimeTextView;
    private Chronometer reactionTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_game);

        Bundle data = getIntent().getExtras();
        highscore = data.getInt("highscore");
        flag = data.getString("flag");

        intensiteTextView = (TextView) findViewById(R.id.intensiteTextView);
        totalReactionTimeTextView = (TextView) findViewById(R.id.totalReactionTimeTextView);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        reactionTime = (Chronometer) findViewById(R.id.chrono);
        totalReactionTime = 0;

        lightOn = true;
        waiting = true;
        intensiteTextView.setText("Attender la prochaine instruction! (Dévoiler la caméra avant en l'attendant!");
        Task task = new Task();
        task.execute();
    }

    float lastValue;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(lightOn && !waiting && event.values[0] > 2){
            reactionTime.stop();
            if(lastValue > 2) {
                intensiteTextView.setText("Vous deviez attendre la prochaine instruction! (Dévoiler la caméra avant en l'attendant!)");
                totalReactionTime += 1000;
            } else {
                intensiteTextView.setText("Attender la prochaine instruction dans votre position acutelle!");
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
                intensiteTextView.setText("Vous deviez attendre la prochaine instruction! (Cacher la caméra avant en l'attendant!)");
                totalReactionTime += 1000;
            } else {
                intensiteTextView.setText("Attender la prochaine instruction dans votre position acutelle!");
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

    public class Task extends AsyncTask<Void, Void, Void> {

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
                intensiteTextView.setText("Off!");
            }
            else {
                lightOn = true;
                intensiteTextView.setText("On!");
            }

            waiting = false;
        }


    }
}
