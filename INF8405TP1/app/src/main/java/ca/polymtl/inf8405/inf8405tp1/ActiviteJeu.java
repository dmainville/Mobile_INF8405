package ca.polymtl.inf8405.inf8405tp1;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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

    private int mNiveau = 1;

    private TableLayout mZoneDeJeu;
    private Grille mGrille;


    private View[] mCases = new View[64];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_jeu);

        // avant tout, set la zone de jeu
        mZoneDeJeu = (TableLayout) findViewById(R.id.zoneDeJeu);


        // Chercher les valeurs passées par l'activité précédente
        int niveau = (int) getIntent().getExtras().get("niveau");

        // Appeler la méthode pour faire le setup de la zone de jeu
        setupNiveau(niveau);

        mZoneDeJeu.setOnTouchListener(mTouchListener);

    }


    private void setupNiveau(int niveau)
    {
        // TODO: détruire ancienne table

        // sauvegarder le nouveau niveau
        mNiveau = niveau;

        // calculer le nombre de cases par ligne
        if (niveau >=4)
            mCasesParLigne = 8;
        else
            mCasesParLigne = 7;

        // créer la grille dans la logique de jeu, en se passant comme observer
        mGrille = new Grille(mNiveau, this);
        System.out.println(mGrille.toString());


        // Dynamiquement ajouter des lignes et cellules au tableau
        for (int i = 0; i < mCasesParLigne; ++i)
        {
            TableRow row = new TableRow(this);

            // Ajouter les paramètres
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);


            // Ajouter les cellules
            for (int j = 0; j < mCasesParLigne; ++j)
            {
                final int offset = i*mCasesParLigne+j;
                mCases[offset] = new ImageView(this);
                mCases[offset].setMinimumWidth(MLargeurCase);
                mCases[offset].setMinimumHeight(MLargeurCase);


                // faire l'appel pour chercher le bon background

                int backgroundImageID = mGrille.getBackgroundIdALaCase(j, i);
                mCases[offset].setBackground(ContextCompat.getDrawable(getApplicationContext(),backgroundImageID));

                row.addView(mCases[offset]);
            }

            mZoneDeJeu.addView(row);

        }
    }


    private int getIndexFromXPosition(float xCoord)
    {
        if (xCoord >= mZoneDeJeu.getWidth())
            return mCasesParLigne -1;
        else if (xCoord < 0)
            return 0;

        int index = (int) (xCoord * mCasesParLigne / mZoneDeJeu.getWidth());
        return index;
    }

    private int getIndexFromYPosition(float yCoord)
    {
        if (yCoord >= mZoneDeJeu.getHeight())
            return mCasesParLigne -1;
        else if (yCoord < 0)
            return 0;

        int index = (int)(yCoord * mCasesParLigne / mZoneDeJeu.getHeight());
        return index;
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                //TODO: nettoyer
                String myString = "Touch " + Integer.toString(motionEvent.getAction()) + "; " + Float.toString(motionEvent.getX()) + "," + Float.toString(motionEvent.getY());
                System.out.println(myString);
                System.out.println(getIndexFromXPosition(motionEvent.getX()) + " " +
                        getIndexFromYPosition(motionEvent.getY()));

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

        int backgroundImageID = mGrille.getBackgroundIdALaCase(c.posX, c.posY);
        mCases[c.posY*mCasesParLigne + c.posX].setBackground(ContextCompat.getDrawable(getApplicationContext(),backgroundImageID));
    }
}