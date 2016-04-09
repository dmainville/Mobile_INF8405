package polymtl.mazegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GameEnded extends AppCompatActivity {

    TextView gameEnded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_ended);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        boolean gameWon = (boolean)bundle.get("GameWon");
        gameEnded = (TextView) findViewById(R.id.gameEndedTextView);
        if(gameWon)
            gameEnded.setText("Game Won!");
        else
            gameEnded.setText("Game Lost");
    }
}
