package polymtl.inf8405_tp2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class WaitingRoomActivity extends AppCompatActivity {

    final private int REQUEST_CODE_VOTE_MAP_ACTIVITY = 1;

    private UserProfile mCurrentProfile;
    private ListView mReadyUsersList;

    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mArrayList;

    private Button mAdminStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        // assigner la référence pour la liste
        mReadyUsersList = (ListView) findViewById(R.id.listView);
        mArrayList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mArrayList);
        mReadyUsersList.setAdapter(mArrayAdapter);

        // assigner la référence pour le bouton
        //TODO: faire ceci seulement si on est admin. Sinon, rendre le bouton invisible.
        mAdminStartButton = (Button) findViewById(R.id.adminStartButton);

        // recueillir les valeurs passées
        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");


        //setup la connexion à la BD
        Firebase.setAndroidContext(this);

        final Firebase myFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName);

        // TODO: chercher la liste des usagers qui sont déjà prêts (créer une fonction pour ça et remplacer le contenu de onDataChange pour qu'il appelle cette fonction)


        // Ajouter un event listener pour s'il y a changement aux membres prêts pour ce groupe
        myFirebaseGroupRef.addValueEventListener(new ValueEventListener() {
            //TODO: Opter plutot pour onChildAdded et onChildChanged pour éviter d'avoir à réécrire la liste complète à chaque fois
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //TODO: Ajouter un listener pour savoir si l'admin a décidé de passer au vote.
                if (snapshot.hasChild("readyState"))
                {
                    if((Boolean) snapshot.child("readyState").getValue())
                    {
                        // Move to next activity
                        startVoteMapActivity();
                        // TODO: Stop this current activity? (remove from history stack)
                    }
                }


                // TODO: Il faut effectuer un fetch au début, au cas où il y aurait déjà du monde ready.

                if (snapshot.hasChild("members"))
                {
                    mArrayList.clear();
                    mArrayList.addAll(((Map<String, Object>) snapshot.child("members").getValue()).keySet());
                    mArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        // Ajouter l'usager à la liste des usagers prêts (la value est triviale)
        myFirebaseGroupRef.child("members").child(mCurrentProfile.getSanitizedEmail()).setValue(true);


        // Ajouter un event listener pour le bouton pour l'admin
        mAdminStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Marquer, dans la BD, que le groupe est prêt pour passer au vote
                // Trigger l'événement chez tous les clients.
                myFirebaseGroupRef.child("readyState").setValue(true);
            }
        });
    }

    // TODO: Si l'usager quitte cette activité avant que le meeting soit appelé, on doit l'enlver de
    // liste des ready

    private void startVoteMapActivity()
    {
        Intent intent = new Intent(this, VoteMapActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        // TODO: est-ce qu'on pass la liste comme ça ou on le re-fetch dans la BD?
        intent.putExtra("users", mArrayList);
        // TODO: est-ce qu'on a besoin du result?
        startActivityForResult(intent, REQUEST_CODE_VOTE_MAP_ACTIVITY);
    }


}
