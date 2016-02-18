package ca.polymtl.inf8405.inf8405tp1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ActivitePageIntro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_page_intro);
    }

    public void facileClique(View view)
    {
        Intent monIntent = new Intent(this, ActiviteJeu.class);
        monIntent.putExtra("niveau", 1);
        startActivity(monIntent);
    }

    public void difficileClique(View view)
    {
        Intent monIntent = new Intent(this, ActiviteJeu.class);
        monIntent.putExtra("niveau", 4);
        startActivity(monIntent);
    }

    public void quitterClique(View view)
    {
        finish();
    }
}
