package ca.polymtl.squatr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;

public class MazeGame extends AppCompatActivity {

    private int highscore;
    private String flag;
    private Chronometer chrono;
    private int[][] mazeMapVertical;
    private int[][] mazeMapHorizontal;
    private int currentX, currentY;
    private Pair<Integer, Integer> mapSize;
    private final Pair<Integer, Integer> finalPos = new Pair<>(7,7);
    private Pair<Integer, Integer>[] fakeFinalPos;
    private TextView mTbBatterie;
    private int initialBatterieLevel = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_game);

        Bundle data = getIntent().getExtras();
        highscore = data.getInt("highscore");
        flag = data.getString("flag");
        initialBatterieLevel = data.getInt("Battery");

        GridLayout zoneDeJeu = (GridLayout) findViewById(R.id.zoneDeJeu);
        chrono = (Chronometer) findViewById(R.id.chronometer);

        mazeMapVertical = new int[][]{
                {1,0,0,0,1,0,0},
                {0,1,1,0,1,1,1},
                {1,0,1,0,0,1,0},
                {0,1,0,0,0,1,0},
                {0,0,1,0,0,0,0},
                {1,0,1,0,0,0,0},
                {0,1,0,1,0,0,0},
                {0,0,0,0,0,0,0}
        };
        mazeMapHorizontal = new int[][]{
                {0,0,1,0,0,0,0,0},
                {0,1,0,0,0,0,0,0},
                {1,0,0,1,1,1,0,1},
                {0,1,1,1,0,0,1,0},
                {0,1,0,1,1,1,1,1},
                {1,0,1,0,1,1,0,1},
                {0,0,0,1,0,1,1,1}
        };

        mapSize = new Pair<>(8,8);
        //noinspection unchecked
        fakeFinalPos = new Pair[]{
                new Pair(4,0),
                new Pair(7,0),
                new Pair(2,1),
                new Pair(4,1),
                new Pair(0,2),
                new Pair(5,3),
                new Pair(3,4),
                new Pair(0,5),
                new Pair(7,5),
                new Pair(0,6),
                new Pair(0,7),
                new Pair(7,6),
                new Pair(3,7)
        };
        MazeView maze = new MazeView(this.getApplicationContext());
        if(zoneDeJeu != null)
            zoneDeJeu.addView(maze);

        //Écouté le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onStop() {
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch(Exception e){ }

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

    public class MazeView extends View implements SensorEventListener {

        private int width, height, lineWidth;
        float cellWidth, cellHeight;
        float totalCellWidth, totalCellHeight;
        private final Paint line, red, green, darkgrey, background;
        final Context context;
        final SensorManager sensorManager;
        final Sensor accelerometer;

        public MazeView(Context context){
            super(context);
            this.context = context;
            line = new Paint();
            line.setColor(Color.GRAY);
            red = new Paint();
            red.setColor(Color.RED);
            green = new Paint();
            green.setColor(Color.GREEN);
            darkgrey = new Paint();
            darkgrey.setColor(Color.DKGRAY);
            background = new Paint();
            background.setColor(Color.WHITE);
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            setFocusable(true);
            this.setFocusableInTouchMode(true);

            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start();
        }

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            width = (w < h)?w:h;
            height = width;         //for now square mazes
            lineWidth = 1;          //for now 1 pixel wide walls
            cellWidth = (width - ((float)mapSize.first*lineWidth)) / mapSize.first;
            totalCellWidth = cellWidth+lineWidth;
            cellHeight = (height - ((float)mapSize.second*lineWidth)) / mapSize.second;
            totalCellHeight = cellHeight + lineWidth;
            red.setTextSize(cellHeight * 0.75f);
            green.setTextSize(cellHeight * 0.75f);
            super.onSizeChanged(w, h, oldw, oldh);
        }

        protected void onDraw(Canvas canvas) {
            //fill in the background
            canvas.drawRect(0, 0, width, height, background);

            //iterate over the boolean arrays to draw walls
            for(int i = 0; i < mapSize.first; i++) {
                for(int j = 0; j < mapSize.second; j++){
                    float x = j * totalCellWidth;
                    float y = i * totalCellHeight;
                    if(j < mapSize.first - 1 && mazeMapVertical[i][j] == 1) {
                        //we'll draw a vertical line
                        canvas.drawLine(x + cellWidth,   //start X
                                y,               //start Y
                                x + cellWidth,   //stop X
                                y + cellHeight,  //stop Y
                                line);
                    }
                    if(i < mapSize.second - 1 && mazeMapHorizontal[i][j] == 1) {
                        //we'll draw a horizontal line
                        canvas.drawLine(x,               //startX
                                y + cellHeight,  //startY
                                x + cellWidth,   //stopX
                                y + cellHeight,  //stopY
                                line);
                    }
                }
            }
            //draw the ball
            canvas.drawCircle((currentX * totalCellWidth) + (cellWidth / 2),   //x of center
                    (currentY * totalCellHeight) + (cellWidth / 2),  //y of center
                    (cellWidth * 0.45f),                           //radius
                    red);
            //draw the finishing point indicator
            canvas.drawCircle((finalPos.first * totalCellWidth) + (cellWidth / 2),   //x of center
                    (finalPos.second * totalCellHeight) + (cellWidth / 2),  //y of center
                    (cellWidth * 0.45f),                           //radius
                    green);
            //Dessine les fausses positions finales
            for (Pair<Integer, Integer> circle : fakeFinalPos){
                canvas.drawCircle((circle.first * totalCellWidth)+(cellWidth/2),
                        (circle.second * totalCellHeight) + (cellWidth/2),
                        (cellWidth * 0.45f),
                        darkgrey);
            }
        }

        //Direction = 1 => Up
        //Direction = 2 => Right
        //Direction = 3 => Down
        //Direction = 4 => Left
        public void MoveBall(int direction){
            boolean moved = false;
            switch (direction){
                case 1:
                    //Move Up
                    if(currentY != 0) {
                        if (mazeMapHorizontal[currentY - 1][currentX] != 1) {
                            currentY--;
                            moved = true;
                        }
                    }
                    break;
                case 2:
                    //Move Right
                    if(currentX != 7) {
                        if (mazeMapVertical[currentY][currentX] != 1) {
                            currentX++;
                            moved = true;
                        }
                    }
                    break;
                case 3:
                    //Move Down
                    if(currentY != 7) {
                        if (mazeMapHorizontal[currentY][currentX] != 1) {
                            currentY++;
                            moved = true;
                        }
                    }
                    break;
                case 4:
                    //Move Left
                    if(currentX != 0) {
                        if (mazeMapVertical[currentY][currentX - 1] != 1) {
                            currentX--;
                            moved = true;
                        }
                    }
                    break;
            }
            if(moved){
                if(currentX==finalPos.first && currentY==finalPos.second)
                    EndGame(true);

                for(Pair<Integer,Integer> pos : fakeFinalPos){
                    if(currentX == pos.first && currentY == pos.second)
                        EndGame(false);
                }

                invalidate();
            }
        }

        public void EndGame(boolean won){
            double time = SystemClock.elapsedRealtime() - chrono.getBase();
            chrono.stop();
            sensorManager.unregisterListener(this);

            Intent data = new Intent();
            if(won && (time<highscore || highscore==0)) {
                data.putExtra("highscore", time);
                System.out.println("New highscore"+time);
            }
            else
                data.putExtra("highscore", 0);

            data.putExtra("flag", flag);
            setResult(RESULT_OK, data);
            finish();
        }

        int cmp = 0;
        @Override
        public void onSensorChanged(SensorEvent event) {
            //To slow down sensor input (Could be done in registerlistener for API level>18)
            cmp++;
            if(cmp == 25) {
                cmp = 0;

                if (event.values[0] > 3) {
                    MoveBall(3);
                }
                if (event.values[0] < -3) {
                    MoveBall(1);
                }

                if (event.values[1] > 3) {
                    MoveBall(2);
                }
                if (event.values[1] < -3) {
                    MoveBall(4);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
