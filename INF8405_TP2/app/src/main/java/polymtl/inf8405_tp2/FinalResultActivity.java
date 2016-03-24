package polymtl.inf8405_tp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class FinalResultActivity extends AppCompatActivity {

    //UI
    private Button mBtnRetour;
    private TextView mLblDate;
    private TextView mLblLocation;
    private TextView mLblDescription;
    private ImageView mImgDescription;
    private TextView mLblBatterie;

    //Profile
    private UserProfile mCurrentProfile;

    //Batterie
    private int InitialBatterieLevel;
    private boolean BatterieOnce = false; //Afficher la modification de la batterie seulement une fois

    //Référence a la base de données
    private Firebase mFirebaseVoteRef;
    private Firebase mFirebaseGroupsRef;
    private Firebase mFirebaseDescriptionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_result);

        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");
        InitialBatterieLevel = (int) getIntent().getExtras().get("batterie");

        System.out.println("FINAL BATTERIE INITIAL : "+InitialBatterieLevel);

        mBtnRetour = (Button) findViewById(R.id.btnRecommencer);
        mLblDate = (TextView) findViewById(R.id.lblDate);
        mLblLocation = (TextView) findViewById(R.id.lblLocation);
        mLblDescription = (TextView) findViewById(R.id.lblDescription);
        mImgDescription = (ImageView) findViewById(R.id.imgDescription);
        mLblBatterie = (TextView) findViewById(R.id.tbBatterie);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        Firebase.setAndroidContext(this);
        mFirebaseGroupsRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups");

        mFirebaseDescriptionRef= new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("Description");


        //Cacher le bouton de reset si on est pas admin
        if(!mCurrentProfile.organizer)
            mBtnRetour.setVisibility(View.INVISIBLE);

        mBtnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tuer le groupe lorsqu'on a fini
                mFirebaseGroupsRef.child(mCurrentProfile.groupName).removeValue();
                gotoMainActivity();
            }
        });

        mFirebaseVoteRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("VotesDate");

        //Récupérer l'information de date et l'afficher
        mFirebaseVoteRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                for (DataSnapshot child : snapshot.getChildren())
                {

                    System.out.println("CHILD KEY : "+child.getKey().toString());
                    if(child.getKey().toString().equals("voteResult"))
                    {
                        System.out.println("RESULT DATE FOUND : "+child.getValue().toString());
                        mLblDate.setText( child.getValue().toString() );
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        //Récupéré la description et la photo
        mFirebaseDescriptionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.hasChild("Image")) {
                    Bitmap mBitmap = stringToBitmap(snapshot.child("Image").getValue().toString());
                    if(mBitmap!=null)
                        mImgDescription.setImageBitmap(mBitmap);
                }

                if (snapshot.hasChild("Text")) {
                    mLblDescription.setText(snapshot.child("Text").getValue().toString());
                }
            }

                @Override
                public void onCancelled (FirebaseError firebaseError){
                }

        });

    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {

            if(!BatterieOnce)
            {
                BatterieOnce = true;
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                System.out.println("BATTERIE : "+level);
                mLblBatterie.setText("Batterie consommé par l'application : "+(InitialBatterieLevel-level)+"%");
            }

        }
    };

    public void gotoMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // On arrête cette activité lorsqu'on passe au prochain parce qu'on n'en a plus besoin
        finish();
    }

    private Bitmap stringToBitmap(String in){
        if (in != null && ! in.isEmpty())
        {
            byte[] bytes = Base64.decode(in, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
            return null;
    }


}
