package polymtl.inf8405_tp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class WaitingRoomActivity extends AppCompatActivity {

    UserProfile mCurrentProfile;
    ListView mReadyUsersList;

    ArrayAdapter<String> mArrayAdapter;
    ArrayList<String> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        // assigner la référence
        mReadyUsersList = (ListView) findViewById(R.id.listView);
        mArrayList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mArrayList);
        mReadyUsersList.setAdapter(mArrayAdapter);


        // recueillir les valeurs passées
        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");

        //setup la connexion à la BD
        Firebase.setAndroidContext(this);

        Firebase myFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName);

        // TODO: chercher la liste des usagers qui sont déjà prêts (créer une fonction pour ça et remplacer le contenu de onDataChange pour qu'il appelle cette fonction)


        // Ajouter un event listener pour s'il y a changement aux membres prêts pour ce groupe
        myFirebaseGroupRef.addValueEventListener(new ValueEventListener() {
            //TODO: Opter plutot pour onChildAdded et onChildChanged pour éviter d'avoir à réécrire la liste complète à chaque fois
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // TODO: Il faut effectuer un fetch au début, au cas où il y aurait déjà du monde ready.
                mArrayList.clear();
                mArrayList.addAll(((Map<String, Object>)snapshot.getValue()).keySet());
                mArrayAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        // Ajouter l'usager à la liste des usagers prêts (la value est triviale)
        myFirebaseGroupRef.child(mCurrentProfile.getSanitizedEmail()).setValue(true);
    }

    // TODO: Si l'usager quitte cette activité avant que le meeting soit appelé, on doit l'enlver de
    // liste des ready
}
