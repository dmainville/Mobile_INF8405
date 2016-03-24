package polymtl.inf8405_tp2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class WaitingRoomActivity extends AppCompatActivity {

    private UserProfile mCurrentProfile;
    private int InitialBatterieLevel;


    private ListView mReadyUsersList;

    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mArrayList;

    private Button mAdminStartButton;
    private Firebase myFirebaseGroupRef;
    private ValueEventListener mValueEventListener; //< On garde une référece pour pouvoir l'enlever

    private boolean removeLocalUserOnFinish = true; //< Indique si on va enlever l'usager de la liste des ready, par exemple si l'usager fait back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        // assigner la référence pour la liste
        mReadyUsersList = (ListView) findViewById(R.id.listView);
        mArrayList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mArrayList);
        mReadyUsersList.setAdapter(mArrayAdapter);

        // recueillir les valeurs passées
        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");
        InitialBatterieLevel = (int) getIntent().getExtras().get("batterie");

        // mettre le bouton continue invisible. On le rendra visible si la BD dit qu'on est admin
        mAdminStartButton = (Button) findViewById(R.id.adminStartButton);
        mAdminStartButton.setVisibility(View.INVISIBLE);

        //setup la connexion à la BD
        Firebase.setAndroidContext(this);

        myFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName);

        // S'ajouter à la BD si on n'est pas déjà là.
        // Ajouter l'usager à la liste des usagers prêts. La valeur deviendra vraie s'ils sont toujours présents quand l'admin débute la séance.

        myFirebaseGroupRef.child("members").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (!currentData.hasChild(mCurrentProfile.getSanitizedEmail()) || !((Boolean)currentData.child(mCurrentProfile.getSanitizedEmail()).getValue()).equals(Boolean.TRUE))
                {
                    currentData.child(mCurrentProfile.getSanitizedEmail()).setValue(false);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });


        // Si l'usager a coché la case organizer, devenir admin s'il n'y en a pas déjà un sur la bd
        if (mCurrentProfile.organizer) {
            // On décoche le fait qu'on est admin, parce qu'il faut que la bd le confirme
            mCurrentProfile.organizer = false;

            myFirebaseGroupRef.child("organizer").runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    // S'il n'y a pad d'organisateur, on se met comme organisateur
                    if (currentData.getValue() == null) {
                        currentData.setValue(mCurrentProfile.getSanitizedEmail());
                    } else {
                    }
                    return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
                }
                @Override
                public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                    //This method will be called once with the results of the transaction.
                }
            });
        }




        // Ajouter un event listener pour s'il y a changement aux membres prêts pour ce groupe
        myFirebaseGroupRef.addValueEventListener(mValueEventListener = new ValueEventListener() {
            //TODO: Opter plutot pour onChildAdded et onChildChanged pour éviter d'avoir à réécrire la liste complète à chaque fois?
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Ajouter un listener pour savoir si l'admin a décidé de passer au vote.
                if (snapshot.hasChild("readyState")) {
                    // On vérifie si notre valeur a été mis à true
                    if (((Boolean) snapshot.child("members/" + mCurrentProfile.getSanitizedEmail()).getValue()).equals(Boolean.TRUE)) {
                        startVoteMapActivity();
                    } else if ((boolean) snapshot.child("readyState").getValue() == true) {
                        // Move to next activity
                        Toast.makeText(WaitingRoomActivity.this, "Le vote est déjà commencé pour ce groupe.", Toast.LENGTH_LONG).show();
                    }
                }

                if (snapshot.hasChild("members")) {
                    mArrayList.clear();
                    /*for (DataSnapshot prop : snapshot.child("members").getChildren()) {
                        if (prop.getKey() == "latitude")
                            mCurrentProfile.meetingLatitude += (Double) prop.getValue();//true+true?
                        if (prop.getKey() == "longitude")
                            mCurrentProfile.meetingLongitude += (Double) prop.getValue();
                    }*/
                    mArrayList.addAll(((Map<String, Object>) snapshot.child("members").getValue()).keySet());
                    mArrayAdapter.notifyDataSetChanged();
                }

                // Si la BD dit qu'on est admin, afficher le bouton
                if (snapshot.hasChild("organizer")) {
                    if (snapshot.child("organizer").getValue().toString().equals(mCurrentProfile.getSanitizedEmail())) {
                        mAdminStartButton.setVisibility(View.VISIBLE);
                        // remettre la valeur à true si la BD la confirmé
                        mCurrentProfile.organizer = true;
                        ((TextView) findViewById(R.id.organizerTextView)).setText(snapshot.child("organizer").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });



        // Ajouter un event listener pour le bouton pour l'admin
        mAdminStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Marquer, dans la BD, que le groupe est prêt pour passer au vote
                // Trigger l'événement chez tous les clients.
                myFirebaseGroupRef.child("readyState").setValue(true);
                for (String user : mArrayList)
                {
                    myFirebaseGroupRef.child("members").child(user).setValue(true);
                }

                myFirebaseGroupRef.child("VotesDate").child("votes1").setValue(0);
                myFirebaseGroupRef.child("VotesDate").child("votes2").setValue(0);
                myFirebaseGroupRef.child("VotesDate").child("votes3").setValue(0);
                myFirebaseGroupRef.child("VotesMap").child("votes1").setValue(0);
                myFirebaseGroupRef.child("VotesMap").child("votes2").setValue(0);
                myFirebaseGroupRef.child("VotesMap").child("votes3").setValue(0);
            }
        });
    }


    private void startVoteMapActivity()
    {
        Intent intent = new Intent(this, VoteMapActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        intent.putExtra("batterie", InitialBatterieLevel);
        // TODO: est-ce qu'on pass la liste comme ça ou on le re-fetch dans la BD?
        intent.putExtra("users", mArrayList);
        startActivity(intent);
        // On arrête cette activité lorsqu'on passe au prochain parce qu'on n'en a plus besoin
        removeLocalUserOnFinish = false;
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Enlever les listeners
        myFirebaseGroupRef.removeEventListener(mValueEventListener);

        // Enlever l'usager des ready si on ne passe pas au vote
        if (removeLocalUserOnFinish)
        {
            myFirebaseGroupRef.child("members").child(mCurrentProfile.getSanitizedEmail()).removeValue();
            //Si on était admin, s'enlever aussi comme admin
            if (mCurrentProfile.organizer)
            {
                myFirebaseGroupRef.child("organizer").removeValue();
            }
        }
        /*
        Query q = myFirebaseGroupRef;
        q.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                System.out.println("LEAVING QUERY");
                if(!snapshot.hasChild("readyState"))
                {
                    myFirebaseGroupRef.child("members").child(mCurrentProfile.getSanitizedEmail()).removeValue();
                    System.out.println("REMOVED USER");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });

        System.out.println("ACTIVITY STOPPED, WAITING ROOM");*/
    }

}
