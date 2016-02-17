package ca.polymtl.inf8405.inf8405tp1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActiviteFinPartie extends AppCompatActivity {

    int mNiveauCourant = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_fin_partie);

        Bundle extras = getIntent().getExtras();

        int deverrouille = extras.getInt("deverrouille");

        mNiveauCourant = extras.getInt("niveauCourant");

        // On donne accès aux boutons pour les niveaux déverrouillés

        System.out.println(deverrouille + "==================================");

        findViewById(R.id.facile1).setEnabled((deverrouille & 0b1) != 0);
        findViewById(R.id.facile2).setEnabled((deverrouille & 0b10) != 0);
        findViewById(R.id.facile3).setEnabled((deverrouille & 0b100) != 0);
        findViewById(R.id.difficile1).setEnabled((deverrouille & 0b1000) != 0);
        findViewById(R.id.difficile2).setEnabled((deverrouille & 0b10000) != 0);
        findViewById(R.id.difficile3).setEnabled((deverrouille & 0b100000) != 0);


    }

    public void facile1Clique(View view) {
        setResult(1);
        finish();
    }
    public void facile2Clique(View view)
    {
        setResult(2);
        finish();
    }
    public void facile3Clique(View view)
    {
        setResult(3);
        finish();
    }
    public void difficile1Clique(View view)
    {
        setResult(4);
        finish();
    }
    public void difficile2Clique(View view)
    {
        setResult(5);
        finish();
    }
    public void difficile3Clique(View view)
    {
        setResult(6);
        finish();
    }

    public void nextClique(View view)
    {
        setResult(mNiveauCourant + 1);
        finish();
    }

    public void quitterClique (View view)
    {
        setResult(-1);
        finish();
    }
}
