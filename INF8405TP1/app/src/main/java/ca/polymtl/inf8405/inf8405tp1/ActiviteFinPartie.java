package ca.polymtl.inf8405.inf8405tp1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ActiviteFinPartie extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_fin_partie);
    }

    public void facile1Clique(View view)
    {
        setResult(71);
        finish();
    }
}
