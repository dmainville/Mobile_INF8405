package ca.polymtl.squatr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    Button mSaveButton;
    EditText usernameEditText;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        addViewsAndEventListeners();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usernameEditText.setText(sharedPref.getString("Username", ""));
    }

    private void addViewsAndEventListeners() {
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: save username locally
                editor = sharedPref.edit();
                editor.putString("Username", usernameEditText.getText().toString());
                editor.commit();
                finish();
            }
        });
    }
}
