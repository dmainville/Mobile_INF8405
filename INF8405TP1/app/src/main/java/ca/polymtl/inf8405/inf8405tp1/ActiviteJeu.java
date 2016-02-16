package ca.polymtl.inf8405.inf8405tp1;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ActiviteJeu extends AppCompatActivity implements IObserver {

    final private int MLargeurCase = 150;
    private int mCasesParLigne = 7;

    private int mNiveau = 71;

    private TableLayout mZoneDeJeu;
    private Grille mGrille;


    private View[] mCases = new View[64];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_jeu);



        // Chercher les valeurs passées par l'activité précédente
        mNiveau = (int) getIntent().getExtras().get("niveau");
        mCasesParLigne = (int)mNiveau / 10;

        mZoneDeJeu = (TableLayout) findViewById(R.id.zoneDeJeu);

        // Appeler la méthode pour faire le setup de la zone de jeu
        setupNiveau(mNiveau);







        mZoneDeJeu.setOnTouchListener(mTouchListener);

    }


    private void setupNiveau(int niveau)
    {
        // TODO: ajuster syntaxe niveau
        // TODO: envoyer un message au modèle pour lui dire dans quel niveau nous sommes


        // Dynamiquement ajouter des lignes et cellules au tableau
        for (int i = 0; i < mCasesParLigne; ++i)
        {
            TableRow row = new TableRow(this);

            // Ajouter les paramètres
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            row.setBackgroundColor(Color.BLACK);

            // Ajouter les cellules
            for (int j = 0; j < mCasesParLigne; ++j)
            {
                final int offset = i*mCasesParLigne+j;
                mCases[offset] = new ImageView(this);
                mCases[offset].setMinimumWidth(MLargeurCase);
                mCases[offset].setMinimumHeight(MLargeurCase);

                // TODO: faire l'appel pour chercher le bon background
                mCases[offset].setBackgroundColor(Color.RED);

                row.addView(mCases[offset]);
            }

            mZoneDeJeu.addView(row);

        }


        //TODO: sauvegarder le nouveau niveau, puis appeler setTailleZone
    }

    private void setTailleZone(int puzzleSize) {
        mCasesParLigne = puzzleSize;

        //TODO: modifier structure de la table

    }

    private int getIndexFromXPosition(float xCoord)
    {
        //TODO:
        return 0;
    }

    private int getIndexFromYPosition(float yCoord)
    {
        //TODO:
        return 0;
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                String myString = "Touch " + Integer.toString(motionEvent.getAction()) + "; " + Float.toString(motionEvent.getX()) + "," + Float.toString(motionEvent.getY());
                //Toast.makeText(view.getContext(), myString, Toast.LENGTH_SHORT).show();
                System.out.println(myString);

                // calculer l'index dans le tableau du point courant

                mGrille.CliqueCase(
                        getIndexFromXPosition(motionEvent.getX()),
                        getIndexFromYPosition(motionEvent.getY()),
                        ETypeClique.Click
                );

            }

            else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
            {
                mGrille.CliqueCase(
                        getIndexFromXPosition(motionEvent.getX()),
                        getIndexFromYPosition(motionEvent.getY()),
                        ETypeClique.Drag);
            }

            System.out.println("Height: " + mZoneDeJeu.getHeight());
            System.out.println("Width: " + mZoneDeJeu.getWidth());

            return true;
        }
    };

    /// Appelé lorsque la partie est terminée pour donner l'option au joueur de séléctionner la prochaine partie
    public void gagnerPartie(View view)
    {
        Intent intent = new Intent(this, ActiviteFinPartie.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        //TODO: gérer le setup avec les nouvelles donnée ou quitter si quit.
        System.out.println("Request Code: " + requestCode + " Result Code: " + resultCode);
    }

    public void quitterJeuClique(View view)
    {
        //Todo: confirmer, détruire, retourner au menu principal
        System.out.println("quitting game");
    }



    public void notifyVictoire()
    {
        //TODO: faire le setup de la nouvelle partie
        // Prompt à l'usager : Quel niveau (next ou antérieur ou replay) ou Quitter
        // Envoyer message au modèle de charger la nouvelle partie.
        // Mettre à jour le display du numéro de partie
    }

    public void notifyCase(Case c)
    {
        //TODO: modifier la mCase(index);
    }
}