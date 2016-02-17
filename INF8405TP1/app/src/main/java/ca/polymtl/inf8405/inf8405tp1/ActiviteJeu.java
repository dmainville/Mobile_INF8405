package ca.polymtl.inf8405.inf8405tp1;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

public class ActiviteJeu extends AppCompatActivity implements IObserver {

    // bitmask indiquant quel niveaux sont déverrouillés
    int mDeverrouille = 1;

    final private int MLargeurCase = 150;
    private int mCasesParLigne = 7;

    private int mNiveau = 0;

    private GridLayout mZoneDeJeu;
    private Grille mGrille;


    private View[] mCases = new View[64];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_jeu);

        // avant tout, set la zone de jeu
        mZoneDeJeu = (GridLayout) findViewById(R.id.zoneDeJeu);


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

        // indiquer que le nouveau niveau est dorénavant déverrouillé
        mDeverrouille |= (0b01 << (niveau -1));

        // calculer le nombre de cases par ligne
        if (niveau >=4)
            mCasesParLigne = 8;
        else
            mCasesParLigne = 7;

        // créer la grille dans la logique de jeu, en se passant comme observer
        mGrille = new Grille(mNiveau, this);


        mZoneDeJeu.setColumnCount(mCasesParLigne);
        mZoneDeJeu.setRowCount(mCasesParLigne);


        // Dynamiquement ajouter des lignes et cellules au tableau
        for (int i = 0; i < mCasesParLigne; ++i)
        {


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

                mZoneDeJeu.addView(mCases[offset]);
            }

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
        intent.putExtra("deverrouille", mDeverrouille);
        intent.putExtra("niveauCourant", mNiveau);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        //TODO: gérer le setup avec les nouvelles donnée ou quitter si quit.
        switch (requestCode)
        {
            case 0: // code pour fin partie

                break;
            default:
                System.out.println("Request Code: " + requestCode + " Result Code: " + resultCode);
                break;
        }
    }

    public void quitterJeuClique(View view)
    {
        quitterPartie();
    }

    private void quitterPartie()
    {
        // TODO: gérer popup confirmation
        //Todo: confirmer, détruire, retourner au menu principal

        finish();
    }

    public void notifyVictoire()
    {
        //TODO: faire le setup de la nouvelle partie
        // voir gangerPartie
        // Prompt à l'usager : Quel niveau (next ou antérieur ou replay) ou Quitter
        // Envoyer message au modèle de charger la nouvelle partie.
        // Mettre à jour le display du numéro de partie
    }

    public void notifyCase(Case c)
    {
        int backgroundImageID = mGrille.getBackgroundIdALaCase(c.posX, c.posY);
        mCases[c.posY*mCasesParLigne + c.posX].setBackground(ContextCompat.getDrawable(getApplicationContext(),backgroundImageID));
    }
}