package ca.polymtl.squatr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    Button mGotoMapButton;
    Button mLeaderboardButton;
    Button mSettingsButton;
    Button mPracticeMinigame1Button;
    Button mPracticeMinigame2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        // assign views to references and set event listeners
        addViewsAndEventListeners();
    }

    private void addViewsAndEventListeners()
    {
        mGotoMapButton = (Button) findViewById(R.id.gotoMapButton);
        mGotoMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                Intent intent = new Intent(MainMenu.this, SettingsActivity.class);
                startActivity(intent);
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
}
