package ca.polymtl.inf8405.inf8405tp1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ActiviteJeu extends AppCompatActivity implements IObserver {

    /// bitmask indiquant quel niveaux sont déverrouillés
    int mDeverrouille = 0;

    /// largeur de chaque case en dp
    final private int MLargeurCase = 150;

    /// nombre de cases par ligne
    private int mCasesParLigne = 7;

    /// niveau de jeu en cours
    private int mNiveau = 0;

    /// l'objet représantant l'affichage de la zone. Toutes les cases sont ses enfants.
    private GridLayout mZoneDeJeu;

    /// la grille de jeu dans la logique de jeu
    private Grille mGrille;

    ///affichage de chaque case
    private View[] mCases = new View[64];


    /// récupère les valeurs de l'activité appelante, puis appelle setupNiveau pour l'initialisation.
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

    /// fonction principale de la classe, effectue le setup initial du niveau, à chaque changement de niveau
    private void setupNiveau(int niveau)
    {
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

        // détruire les anciens views enfants, si présents
        mZoneDeJeu.removeAllViews();

        mZoneDeJeu.setColumnCount(mCasesParLigne);
        mZoneDeJeu.setRowCount(mCasesParLigne);

        // Set le nom du nouveau niveau à l'écran
        setNomNiveau(mNiveau);
        // Set le nombre de connexions à l'écran
        setNombreConnexionsDisplay(mGrille.getNombreConnexions());

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

    /// réinitialise la partie à 0
    public void reinitialiserPartie(View view)
    {
        setupNiveau(mNiveau);
    }

    /// fonction qui calcule les coordonnées dans la grille à partir des coordonnées touch (x)
    private int getIndexFromXPosition(float xCoord)
    {
        if (xCoord >= mZoneDeJeu.getWidth())
            return mCasesParLigne -1;
        else if (xCoord < 0)
            return 0;

        int index = (int) (xCoord * mCasesParLigne / mZoneDeJeu.getWidth());
        return index;
    }

    /// fonction qui calcule les coordonnées dans la grille à partir des coordonnées touch (y)
    private int getIndexFromYPosition(float yCoord)
    {
        if (yCoord >= mZoneDeJeu.getHeight())
            return mCasesParLigne -1;
        else if (yCoord < 0)
            return 0;

        int index = (int)(yCoord * mCasesParLigne / mZoneDeJeu.getHeight());
        return index;
    }

    /// Objet contrôleur de touch. Forward l'information à la logique de jeu
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

    /// fonction magique pour debug, pour pouvoir gagner immédiatement la partie
    /// rendre le bouton magique visible pour tester (laissé exprès pour le correcteur)
    public void gagnerPartie(View view)
    {
        gagnerPartie();
    }

    /// Appelé lorsque la partie est terminée pour donner l'option au joueur de séléctionner la prochaine partie
    public void gagnerPartie()
    {
        Intent intent = new Intent(this, ActiviteFinPartie.class);
        intent.putExtra("deverrouille", mDeverrouille);
        intent.putExtra("niveauCourant", mNiveau);
        startActivityForResult(intent, 0);
    }

    /// Handler du retour de l'écran de sélection de partie
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode)
        {
            case 0: // code pour fin partie
                // code négatif: on réinitialise (au cas ou il ne veut pas vraiment quitter) et on passe à la fonction quitter
                if (resultCode < 0)
                {
                    setupNiveau(mNiveau);
                    quitterPartie();
                }
                // code de niveau: on lance ce niveau
                else if (resultCode > 0 && resultCode <=6)
                {
                    setupNiveau(resultCode);
                }
                // l'usager a fait back: on réinitialise
                else if (resultCode == RESULT_CANCELED)
                {
                    setupNiveau(mNiveau);
                }
                // code supérieur à 6: on réinitalise le niveau 6
                else
                {
                    setupNiveau(6);
                }

                break;
            default:
                System.out.println("Request Code: " + requestCode + " Result Code: " + resultCode);
                break;
        }
    }

    /// contrôleur de bouton quitter
    public void quitterJeuClique(View view)
    {
        quitterPartie();
    }


    /// gère la sortie du jeu, proposant la boite dialog de confirmation
    private void quitterPartie()
    {
        // Ce n'est pas la peine de créer une classe séparer, on gère le alert ici.

        String titre = getString(R.string.quitAlertTitle);
        String texte = getString(R.string.quitAlertText);
        String quitter = getString(R.string.quit);
        String annuler = getString(R.string.annuler);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    finish();
                }
                else
                {
                    dialog.dismiss();
                }
            }
        };

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(titre);
        alertDialog.setMessage(texte);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, quitter,listener);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, annuler, listener);
        alertDialog.show();
    }

    /// change le texte affichant la partie en cours
    private void setNomNiveau(int niveau)
    {
        TextView t = (TextView) findViewById(R.id.textNiveau);
        switch (niveau)
        {
            case 1:
                t.setText(R.string.facile1);
                break;
            case 2:
                t.setText(R.string.facile2);
                break;
            case 3:
                t.setText(R.string.facile3);
                break;
            case 4:
                t.setText(R.string.difficile1);
                break;
            case 5:
                t.setText(R.string.difficile2);
                break;
            case 6:
                t.setText(R.string.difficile3);
                break;
            default:
                t.setText("??");
                break;
        }
    }

    /// Affiche le nombre de connexions présentements faites
    private void setNombreConnexionsDisplay(int nbConnexion)
    {
        ((TextView) findViewById(R.id.nbConnexion)).setText(Integer.toString(nbConnexion));
    }



    /// fonction de callback IObserver lorsque la logique de jeu détecte une victoire
    public void notifyVictoire()
    {
        gagnerPartie();
    }

    /// fonction de callback IObserver lorsque la logique de jeu détecte qu'une case a changé
    public void notifyCase(Case c)
    {
        int backgroundImageID = mGrille.getBackgroundIdALaCase(c.posX, c.posY);
        mCases[c.posY*mCasesParLigne + c.posX].setBackground(ContextCompat.getDrawable(getApplicationContext(), backgroundImageID));
    }

    /// fonction de callback IObserver lorsque la logique de jeu détecte que le nombre de connexions à changé
    public void notifyNbConnexion(int nbConnexion)
    {
        setNombreConnexionsDisplay(nbConnexion);
    }

}