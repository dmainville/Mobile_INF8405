package ca.polymtl.squatr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {

    private Button mGotoMapButton;
    private Button mLeaderboardButton;
    private Button mSettingsButton;
    private Button mPracticeMinigame1Button;
    private Button mPracticeMinigame2Button;

    String mUsername;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        // assign views to references and set event listeners
        addViewsAndEventListeners();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUsername = sharedPref.getString("Username", "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101)
            mUsername = sharedPref.getString("Username", "");
    }

    private void addViewsAndEventListeners()
    {
        mGotoMapButton = (Button) findViewById(R.id.gotoMapButton);
        mGotoMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUsername.isEmpty())
                    Toast.makeText(getApplicationContext(), "Creer un nom d'utilisateur dans le menu Parametre avant de debuter!", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(MainMenuActivity.this, MapsActivity.class);
                    intent.putExtra("Username", mUsername);
                    startActivity(intent);
                }
            }
        });

        mLeaderboardButton = (Button) findViewById(R.id.leaderboardButton);
        mLeaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LeaderboardActivity.class);
                intent.putExtra("Username", mUsername);
                startActivity(intent);
            }
        });

        mSettingsButton = (Button) findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        mPracticeMinigame1Button = (Button) findViewById(R.id.practiceMinigame1Button);
        mPracticeMinigame1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, MazeGame.class);
                intent.putExtra("flag", "Pratique");
                intent.putExtra("highscore", 0);
                startActivity(intent);
            }
        });

        mPracticeMinigame2Button = (Button) findViewById(R.id.practiceMinigame2Button);
        mPracticeMinigame2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LightGame.class);
                intent.putExtra("flag", "Pratique");
                intent.putExtra("highscore", 0);
                startActivity(intent);
            }
        });
    }
}
