package com.aerobola.apps.community;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements  Validator.ValidationListener {
TextView goto_signin_page;

    private FirebaseAuth mAuth;
    @Length(min = 3, message = "Enter atleast 3 characters.")
    private EditText firstname_et;


    @Length(min = 3, message = "Enter atleast 3 characters.")
    private EditText lastname_et;

    @NotEmpty
    @Email
    private EditText email_et;


    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC,message = "Enter at least 8 characters, a number and a letter")
    private EditText password_et;


@ConfirmPassword
    private EditText confirmPassword_et;


    private Spinner role_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        firstname_et=(EditText)findViewById(R.id.firstname_et);
        lastname_et=(EditText)findViewById(R.id.lastname_et);
        email_et=(EditText)findViewById(R.id.email_et);
        password_et=(EditText)findViewById(R.id.password_et);
        confirmPassword_et=(EditText)findViewById(R.id.confirmPassword_et);
        role_spinner=(Spinner)findViewById(R.id.role_spinner);
       final Validator validator = new Validator(this);
        validator.setValidationListener(this);
Button register_button=(Button)findViewById(R.id.bt_signup);
register_button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        validator.validate();
    }
});
        goto_signin_page=(TextView)findViewById(R.id.tv2);
        goto_signin_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        mAuth.createUserWithEmailAndPassword(email_et.getText().toString(), password_et.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        add_data(user);


                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void add_data(final FirebaseUser user) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first, middle, and last name
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("uid",user.getUid());
        userdata.put("fullname", firstname_et.getText().toString()+" "+lastname_et.getText().toString());
        userdata.put("role",role_spinner.getSelectedItem().toString());
        userdata.put("activated",false);
        userdata.put("submitted",false);
        userdata.put("admin",false);







// Add a new document with a generated ID
        db.collection("users").document(user.getUid())
                .set(userdata)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {


                       user.sendEmailVerification();
                       mAuth.signOut();
                       startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                       finish();

                       Toast.makeText(RegisterActivity.this, "Account created! Please verify your email.", Toast.LENGTH_SHORT).show();
                   }
               }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
