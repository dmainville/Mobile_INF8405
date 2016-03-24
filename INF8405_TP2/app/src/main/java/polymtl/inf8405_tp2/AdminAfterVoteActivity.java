package polymtl.inf8405_tp2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firebase.client.Firebase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AdminAfterVoteActivity extends AppCompatActivity {

    final private int REQUEST_CODE_PICTURE = 1;
    final private int REQUEST_CODE_IMAGE = 2;
    final private int REQUEST_CODE_GOTO_FINAL_ROOM = 3;


    // UI references.
    private EditText mTbDescription;
    private ImageView mPhoto;
    private Button mBtnOk;

    //Profile
    private UserProfile mCurrentProfile;

    //Référence a la base de données
    private Firebase mFirebaseGroupRef;

    Bitmap imageDescription = null; //Référence au bitmap sélectionné

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_after_vote);

        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");

        mBtnOk = (Button) findViewById(R.id.btnOk);
        mTbDescription = (EditText) findViewById(R.id.tbDescription);
        mPhoto = (ImageView) findViewById(R.id.imgDescription);

        Firebase.setAndroidContext(this);
        mFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName);

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Storé dans la bd la description et la photo
                mFirebaseGroupRef.child("Description").child("Text").setValue(mTbDescription.getText().toString());
                if(imageDescription==null)
                    mFirebaseGroupRef.child("Description").child("Image").setValue("");
                else
                    mFirebaseGroupRef.child("Description").child("Image").setValue(UserProfile.bitmapToString(imageDescription));

                //Changer d'activité
                gotoFinalRestultActivity();

            }
        });
    }

    public void gotoFinalRestultActivity()
    {
        Intent intent = new Intent(this, FinalResultActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        startActivity(intent);

        // On arrête cette activité lorsqu'on passe au prochain parce qu'on n'en a plus besoin
        finish();
    }


    public void selectPicture(){
        final CharSequence[] items = { "Prendre une photo", "Choisir une image", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminAfterVoteActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Prendre une photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CODE_PICTURE);
                } else if (items[item].equals("Choisir une image")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            REQUEST_CODE_IMAGE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_CODE_IMAGE:
                case REQUEST_CODE_PICTURE:
                    // Transformer l'image retournée en un bitmap, en la downsizant au préalable
                    Uri selectedImageURL = data.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImageURL); //throws FileNotFoundException

                        // Chercher la taille de l'image sans la décoder
                        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
                        bfOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(imageStream, null, bfOptions);

                        //réduire la taille
                        final int TARGET_SIZE = 400;

                        // On cherche l'échelle de réduction pour que le width et le heigh soient plus petit que TARGET_SIZE
                        int scale = 1;
                        System.out.println(bfOptions.outHeight + " =========== " + bfOptions.outWidth);
                        for ( ; bfOptions.outWidth / scale > TARGET_SIZE || bfOptions.outWidth / scale > TARGET_SIZE; scale*=2);

                        // On ouvre l'image
                        BitmapFactory.Options bfOptions2 = new BitmapFactory.Options();
                        bfOptions2.inSampleSize = scale;
                        Bitmap imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageURL), null, bfOptions2);
                        imageDescription = imageBitmap;

                        // Modifier l'image de description
                        mPhoto.setImageBitmap(imageBitmap);

                        // Fermer le stream
                        imageStream.close(); //trhows IOException
                    }
                    catch (FileNotFoundException e)
                    {
                        System.err.println("Image File not found");
                        Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace(); //close stream failed
                    }
                    break;
                default:
                    break;
            }

        }
    }
}
