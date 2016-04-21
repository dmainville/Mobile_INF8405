package ca.polymtl.squatr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {

    private Button mGotoMapButton;
    private Button mLeaderboardButton;
    private Button mSettingsButton;
    private Button mPracticeMinigame1Button;
    private Button mPracticeMinigame2Button;
    private TextView mTbBatterie;

    private int initialBatterieLevel = -1;

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

        //Écouté le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch(Exception e){ }
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
            mTbBatterie.setText("Consommation de batterie : "+consommation+"%");
        }
    };

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
                    intent.putExtra("Battery", initialBatterieLevel);
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
                intent.putExtra("Battery", initialBatterieLevel);
                startActivity(intent);
            }
        });

        mSettingsButton = (Button) findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                intent.putExtra("Battery", initialBatterieLevel);
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
                intent.putExtra("Battery", initialBatterieLevel);
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
                intent.putExtra("Battery", initialBatterieLevel);
                startActivity(intent);
            }
        });
    }
}
