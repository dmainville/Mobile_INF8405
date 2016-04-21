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
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private Button mSaveButton;
    private EditText usernameEditText;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private TextView mTbBatterie;
    private int initialBatterieLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        addViewsAndEventListeners();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernameEditText.setText(sharedPref.getString("Username", ""));

        Bundle data = getIntent().getExtras();
        initialBatterieLevel = data.getInt("Battery");

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
            mTbBatterie.setText("Consommation de batterie : "+consommation+"%");
        }
    };

    private void addViewsAndEventListeners() {
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: save username locally
                editor = sharedPref.edit();
                editor.putString("Username", usernameEditText.getText().toString());
                editor.apply();
                finish();
            }
        });
    }
}
