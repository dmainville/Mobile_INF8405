package polymtl.inf8405_tp2;
import android.content.Context;

import com.firebase.client.Firebase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by David on 2016-03-15.
 */
public class UserProfile implements Serializable{

    //Consts
    static String PROFILE_FILE = "userProfile.txt";

    String groupName;
    String email;
    String preferences; //Comma separated format
    Boolean organizer;
    //TODO add Some var type de save the current location

    public UserProfile()
    {

    }

    public String getSanitizedEmail()
    {
        return email.replaceAll("[\\.#$\\[\\]/]", "");
    }

    public Boolean SaveProfile(Context applicationContext)
    {
        String filename = PROFILE_FILE;
        Properties props = this.ToProperties();
        FileOutputStream outputStream;


        try {
            outputStream = applicationContext.openFileOutput(filename, Context.MODE_PRIVATE);
            //outputStream.write(string.getBytes());
            props.store(outputStream, null);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public Boolean SaveProfileRemotely(Context applicationContext)
    {
        // Nous n'avons pas vraiment besons d'associer le contexte à Firebase
        // parce qu'on ne lit jamais le résultat. Donc on peut le faire ici
        // au lieu de le mettre dans onCreate de l'applicationContext.
        // On peut changer ceci si nécessaire.
        Firebase.setAndroidContext(applicationContext);

        Firebase myFirebaseRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/");

        // Utiliser l'adresse courriel comme clé, en enlevant les caractères non acceptés de Firebase '.', '#', '$', '[', ']', '/'
        // TODO: On devrait échapper ASCII 0-31 + 127
        myFirebaseRef.child("UserProfiles").child(getSanitizedEmail()).setValue(this.ToProperties());

        return true;
    }

    public Boolean LoadProfile(InputStream in)
    {
        Properties props = new Properties();

        try {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Unable to read from stream");
            e.printStackTrace();
            return false;
        }

        groupName = props.getProperty("groupname");
        email = props.getProperty("email");
        organizer = Boolean.valueOf(props.getProperty("organizer"));
        preferences = props.getProperty("preferences");

        return true;
    }

    public Properties ToProperties()
    {
        Properties result = new Properties();
        result.setProperty("groupname", groupName);
        result.setProperty("email", email);
        result.setProperty("organizer", String.valueOf(organizer));
        result.setProperty("preferences", preferences);

        return result;
    }
}
