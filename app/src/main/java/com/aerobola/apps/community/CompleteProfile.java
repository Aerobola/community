package com.aerobola.apps.community;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteProfile extends AppCompatActivity implements Validator.ValidationListener {

    private  ImageView profileImageView;
    @Length(min = 3, message = "Enter atleast 3 characters.")
    private MaterialEditText fullnameEditText;

    private RadioButton maleRadio,femaleRadio;

    @Checked
    private RadioGroup genderRadioGroup;

    @NotEmpty
    private MaterialEditText batchEditText;

    @Length(min = 10,message = "ID must have 10 characters.")
    private MaterialEditText classIDEditText;

    @NotEmpty
    private  MaterialEditText sessionEditText;

    @NotEmpty
    private  MaterialEditText phoneEditText;

    @NotEmpty
    private  MaterialEditText birthdateEditText;




   private Button submitButton, changePhotoButton;
   @ServerTimestamp
   private Date birthdate_timestamp;

   private Spinner bloodGroupSpinner;

private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        mAuth=FirebaseAuth.getInstance();
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        fullnameEditText=(MaterialEditText)findViewById(R.id.fullnameEditText);
        genderRadioGroup=(RadioGroup)findViewById(R.id.genderRadioGroup);
        batchEditText=(MaterialEditText)findViewById(R.id.batchEditText);
        classIDEditText=(MaterialEditText)findViewById(R.id.classIDEditText);
        sessionEditText=(MaterialEditText)findViewById(R.id.seesionEditText);
        phoneEditText=(MaterialEditText)findViewById(R.id.phoneEditText);
        birthdateEditText=(MaterialEditText)findViewById(R.id.birthdateEditText);
        submitButton=(Button)findViewById(R.id.submitButton);
        changePhotoButton=(Button)findViewById(R.id.changePhotoButton);
        maleRadio = (RadioButton) findViewById(R.id.maleRadio);
        femaleRadio=(RadioButton) findViewById(R.id.femaleRadio);
        bloodGroupSpinner=(Spinner)findViewById(R.id.bloddGroupSpinner);

        //editable=false is depricated so
        birthdateEditText.setKeyListener(null);

        //Calling all registered data from firebase database;
        getRegisteredData();

        //Intialize the data picker for birthdate edittext
        datePicker();


        changePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)


                        .start(CompleteProfile.this);
            }
        });

        final Validator validator = new Validator(this);
        validator.setValidationListener(this);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

    }

    private void getRegisteredData() {
        final DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(mAuth.getCurrentUser().getUid());
        Source source = Source.CACHE;
        docRef.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try{
                            fullnameEditText.setText(document.get("fullname").toString());

                            //getting gender value
                            if(document.get("gender").toString().equals("Male")){
                                maleRadio.setSelected(true);
                            }else {
                                femaleRadio.setSelected(true);
                            }

                            batchEditText.setText(document.get("batch").toString());
                            classIDEditText.setText(document.get("class_id").toString());
                            sessionEditText.setText(document.get("session").toString());
                            phoneEditText.setText(document.get("phone").toString());



                            SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");

                            birthdateEditText.setText(format1.format(document.getDate("birthdate")));
                            birthdate_timestamp=document.getDate("birthdate");
                            bloodGroupSpinner.setSelection(((ArrayAdapter<String>)bloodGroupSpinner.getAdapter()).getPosition(document.get("blood_group").toString()));


                        }catch (Exception e){
                            fullnameEditText.setText(document.get("fullname").toString());
                        }



                    } else {

                    }
                } else {

                }
            }
        });
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
            mAuth.signOut();
            startActivity(new Intent(CompleteProfile.this,LoginActivity.class));
            finish();

            return true;
        }
      return   super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidationSucceeded() {
        uploadImage();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


               profileImageView.setImageURI(resultUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        Bitmap scaled_bitmap=scaleDown(bitmap, 200, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaled_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

       final StorageReference profileImagesRef = storage.getReference().child("profile_images/"+mAuth.getCurrentUser().getUid()+".jpg");

        UploadTask uploadTask = profileImagesRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return profileImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
              String    profileImageUri = task.getResult().toString();
              add_data(profileImageUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }


    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    private void add_data(String profileImageURI) {


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first, middle, and last name
        Map<String, Object> userdata = new HashMap<>();

        userdata.put("fullname", fullnameEditText.getText().toString());
        userdata.put("phone", phoneEditText.getText().toString());
        userdata.put("batch",batchEditText.getText().toString());
        userdata.put("gender",getSelectedGender());
        userdata.put("class_id",classIDEditText.getText().toString());
        userdata.put("profile_image_uri",profileImageURI);
        userdata.put("birthdate",birthdate_timestamp);
        userdata.put("session",sessionEditText.getText().toString());
        userdata.put("blood_group",bloodGroupSpinner.getSelectedItem().toString());
        userdata.put("submitted",true);



// Add a new document with a generated ID
        // Add a new document with a generated ID
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .set(userdata)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                          startActivity(new Intent(CompleteProfile.this,UserProfileActivity.class));
                      finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CompleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedGender() {
        String gender = null;
        int id=genderRadioGroup.getCheckedRadioButtonId();
        if(id==maleRadio.getId()){
            gender="Male";
        }else if(id==femaleRadio.getId()){
            gender="Female";
        }

        return gender;
    }

    public void datePicker(){

        final Calendar myCalendar=Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                birthdate_timestamp=myCalendar.getTime();
                SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");

      birthdateEditText.setText(format1.format(myCalendar.getTime()));
            }

        };

        birthdateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new DatePickerDialog(CompleteProfile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                return false;
            }
        });


    }
}

