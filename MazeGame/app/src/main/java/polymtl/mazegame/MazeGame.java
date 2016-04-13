package polymtl.mazegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MazeGame extends AppCompatActivity {

    private int[][] mazeMapVertical;
    private int[][] mazeMapHorizontal;
    private int currentX, currentY;
    private Pair<Integer, Integer> mapSize;
    private Pair<Integer, Integer> finalPos = new Pair<>(7,7);
    private Pair<Integer, Integer>[] fakeFinalPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_game);

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
        setContentView(maze);
    }

    public class MazeView extends View implements SensorEventListener{

        private int width, height, lineWidth;
        float cellWidth, cellHeight;
        float totalCellWidth, totalCellHeight;
        private Paint line, red, black, darkgrey, background;
        Context context;
        SensorManager sensorManager;
        Sensor gyro;
        boolean firstTilt = true;
        float[] firstTiltValues;


        public MazeView(Context context){
            super(context);
            this.context = context;
            line = new Paint();
            line.setColor(Color.GRAY);
            red = new Paint();
            red.setColor(Color.RED);
            black = new Paint();
            black.setColor(Color.BLACK);
            darkgrey = new Paint();
            darkgrey.setColor(Color.DKGRAY);
            background = new Paint();
            background.setColor(Color.WHITE);
            firstTiltValues = new float[2];
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
            setFocusable(true);
            this.setFocusableInTouchMode(true);
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
            black.setTextSize(cellHeight * 0.75f);
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
                    black);
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

                /*for(Pair<Integer,Integer> pos : fakeFinalPos){
                    if(currentX == pos.first && currentY == pos.second)
                        EndGame(false);
                }*/

                invalidate();
            }
        }

        public void EndGame(boolean won){
            Intent endGame = new Intent(context, GameEnded.class);
            endGame.putExtra("GameWon", won);
            startActivity(endGame);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
//            if(firstTilt){
//                firstTilt = false;
//                firstTiltValues[0] = event.values[0];
//                firstTiltValues
//            }

            if(event.values[0]<-2)
                MoveBall(2);
            if(event.values[0]>2)
                MoveBall(4);
            if(event.values[1]<-2)
                MoveBall(3);
            if(event.values[1]>2)
                MoveBall(1);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
