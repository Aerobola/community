package com.aerobola.apps.community;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

isloggedIn();

    }

    private void isloggedIn() {
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null){

           checkSubmission(user);
        }else {
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

    }
    private void checkSubmission(FirebaseUser user) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        Source source = Source.CACHE;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.getBoolean("submitted")==true){
                            startActivity(new Intent(SplashActivity.this,UserProfileActivity.class));
                            finish();
                        }else {
                            startActivity(new Intent(SplashActivity.this,CompleteProfile.class));
                            finish();
                        }
                    } else {

                    }
                } else {

                }
            }
        });

    }
}
