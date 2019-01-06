package com.aerobola.apps.community;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {
TextView goto_signup_page;
    private FirebaseAuth mAuth;
    @NotEmpty
    private EditText email_et;


    @NotEmpty
    private EditText password_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth=FirebaseAuth.getInstance();
        email_et=(EditText)findViewById(R.id.email_et) ;
        password_et=(EditText)findViewById(R.id.password_et) ;
        final Validator validator = new Validator(this);
        validator.setValidationListener(this);
        Button login_button=(Button)findViewById(R.id.bt_login);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
validator.validate();
            }
        });

        goto_signup_page=(TextView)findViewById(R.id.tv2);
        goto_signup_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    private boolean emailVerified(FirebaseUser user) {

        return user.isEmailVerified() ;
    }

    @Override
    public void onValidationSucceeded() {
        mAuth.signInWithEmailAndPassword(email_et.getText().toString(), password_et.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(emailVerified(user)) {

                           checkSubmission(user);
                        }else {
                            Toast.makeText(LoginActivity.this, "Please verify your email.", Toast.LENGTH_SHORT).show();

                            mAuth.signOut();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkSubmission(FirebaseUser user) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.getBoolean("submitted")==true){
                            startActivity(new Intent(LoginActivity.this,UserProfileActivity.class));
                            finish();
                        }else {
                            startActivity(new Intent(LoginActivity.this,CompleteProfile.class));
                            finish();
                        }
                    } else {

                    }
                } else {

                }
            }
        });

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
