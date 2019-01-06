package com.aerobola.apps.community;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private RoundedImageView profileImageView;
    private Button editProfileButton;
    private TextView fullnameTextView, batchTextView, ageGenderTextView, classIDTextView, sessionTextView, bloodGroupTextView, phoneTextView, emailTextView,alertTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();

        //Initializing all views
        fullnameTextView = (TextView) findViewById(R.id.fullnameTextView);
        batchTextView = (TextView) findViewById(R.id.batchTextView);
        ageGenderTextView = (TextView) findViewById(R.id.ageGenderTextView);
        classIDTextView = (TextView) findViewById(R.id.classIDTextView);
        sessionTextView = (TextView) findViewById(R.id.sessionTextView);
        bloodGroupTextView = (TextView) findViewById(R.id.bloodGroupTextView);
        phoneTextView = (TextView) findViewById(R.id.phoneTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        profileImageView = (RoundedImageView) findViewById(R.id.profileImageView);
        alertTextView=(TextView)findViewById(R.id.alertTextView) ;

        editProfileButton=(Button)findViewById(R.id.editProfileButton) ;
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfileActivity.this,CompleteProfile.class));
            }
        });

        getRegisteredData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getRegisteredData() {
        final DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(mAuth.getCurrentUser().getUid());

        docRef.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Picasso.get().load(document.get("profile_image_uri").toString())
                                .centerCrop()
                                .resize(400, 400)
                                .into(profileImageView);
                        checkSubmission(document.getBoolean("submitted"));
                        fullnameTextView.setText(document.get("fullname").toString());
                        batchTextView.setText("Batch: " + document.get("batch").toString());
                        Date birthdate = document.getDate("birthdate");
                        Date now = new Date();
                        long diff = now.getTime() - birthdate.getTime();
                        long year = diff / 31556952000L;
                        ageGenderTextView.setText("Age: " + String.valueOf(year) + "+, " + document.get("gender").toString());
                        classIDTextView.setText("Class ID: "+document.get("class_id").toString());
                        sessionTextView.setText("Session: "+ document.get("session").toString());
                        bloodGroupTextView.setText("Blood Group: "+document.get("blood_group").toString());
                        phoneTextView.setText("Phone: "+document.get("phone").toString());
                        emailTextView.setText(mAuth.getCurrentUser().getEmail());



                    } else {

                    }
                } else {

                }
            }
        });
    }

    private void checkSubmission(Boolean isSubmitted) {
        if(isSubmitted){
            alertTextView.setText("Your profile is under review. Please wait or contact your CR.");
        }else {
            alertTextView.setText("Please complete your profile and submit for review");
            editProfileButton.setText("Complete");
        }

    }
}