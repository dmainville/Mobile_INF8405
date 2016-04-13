package ca.polymtl.squatr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    Button mGotoMapButton;
    Button mLeaderboardButton;
    Button mSettingsButton;
    Button mPracticeMinigame1Button;
    Button mPracticeMinigame2Button;

    String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        // assign views to references and set event listeners
        addViewsAndEventListeners();
        // TODO: load saved username from file
    }

    private void addViewsAndEventListeners()
    {
        mGotoMapButton = (Button) findViewById(R.id.gotoMapButton);
        mGotoMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: prevent action if username is null
                Intent intent = new Intent(MainMenuActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        mLeaderboardButton = (Button) findViewById(R.id.leaderboardButton);
        mLeaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mSettingsButton = (Button) findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
                // TODO: send username in intent (loaded from file)
            }
        });

        mPracticeMinigame1Button = (Button) findViewById(R.id.practiceMinigame1Button);
        mPracticeMinigame1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPracticeMinigame2Button = (Button) findViewById(R.id.practiceMinigame2Button);
        mPracticeMinigame2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // TODO: when returning from preferences, either receive new username or reload it from file
}
