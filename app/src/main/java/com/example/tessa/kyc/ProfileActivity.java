package com.example.tessa.kyc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends BaseActivity implements
        View.OnClickListener {
    private static final int COMPANY_COUNT = 1004;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String userID;
    private Uri downloadUri;
    private TextView infoView;
    private TextView emailView;
    private EditText nameView;
    private EditText postalCodeView;
    private EditText identifNoView;
    private EditText ddView;
    private EditText mmView;
    private EditText yyyyView;
    private ImageView mImageView;
    private TextView pleaseUpload;
    private StorageReference storageRef;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference statusRef = mDatabase.child("users").child("status");
        storageRef = FirebaseStorage.getInstance().getReference();
        usersRef = mDatabase.child("users").child(userID);

        infoView = (TextView) findViewById(R.id.Profile_Info_TextView);
        emailView = (TextView) findViewById(R.id.Profile_Email_TextView);
        TextView idView = (TextView) findViewById(R.id.Profile_ID_TextView);
        nameView = (EditText) findViewById(R.id.Profile_FullName_EditText);
        postalCodeView = (EditText) findViewById(R.id.Profile_PostalCode_EditText);
        identifNoView = (EditText) findViewById(R.id.Profile_IdentifNo_EditText);
        ddView = (EditText) findViewById(R.id.Profile_DoB_Day_EditText);
        mmView = (EditText) findViewById(R.id.Profile_DoB_Month_EditText);
        yyyyView = (EditText) findViewById(R.id.Profile_DoB_Year_EditText);
        mImageView = (ImageView) findViewById(R.id.Profile_ImageView);
        pleaseUpload = (TextView) findViewById(R.id.Profile_pleaseUpload_TextView);

        Intent intent = getIntent();
        emailView.setText(intent.getStringExtra("E-mail"));
        idView.setText(intent.getStringExtra("ID"));

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if ((long) dataSnapshot.getValue() == 3)
                        infoView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.Profile_TakePhoto_button) {
            dispatchTakePictureIntent();
        }
        else if (i == R.id.Profile_Submit_button) {
            if (validateForm()
                    && validateImageUpload()) {
                String fn = nameView.getText().toString();
                String pc = postalCodeView.getText().toString();
                String id = identifNoView.getText().toString();
                String dob = ddView.getText().toString() + "/" +
                        mmView.getText().toString() + "/" +
                        yyyyView.getText().toString();
                writeNewUser(fn, pc, id, dob);
                uploadImagetoFirebase();
                Toast.makeText(ProfileActivity.this,
                        "Submission Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainLoggedInActivity.class);
                startActivity(intent);
                finish();
            } else validateForm();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mImageView.setImageURI(Uri.fromFile(new File(mCurrentPhotoPath)));
            mImageView.setVisibility(View.VISIBLE);
        }

    }

    private void uploadImagetoFirebase() {
        file = Uri.fromFile(new File(mCurrentPhotoPath));

        StorageReference identifImage = storageRef.child("images/" + userID + ".jpg");

        identifImage.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Get a URL to the uploaded content
                        downloadUri = taskSnapshot.getDownloadUrl();
                        usersRef.child("image").setValue(downloadUri.toString());
                        Toast.makeText(getApplicationContext(), "Image Upload Successful",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getApplicationContext(), "Image Upload failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateImageUpload() {
        boolean valid = true;
        if (mImageView.getVisibility()==View.GONE) {
            pleaseUpload.setVisibility(View.VISIBLE);
            valid = false;
        } else
            pleaseUpload.setVisibility(View.GONE);
        return valid;
    }

    private boolean validateForm() {
        boolean valid = true;
        ArrayList<EditText> fields = new ArrayList<>();
        fields.add(nameView);
        fields.add(identifNoView);
        fields.add(postalCodeView);
        fields.add(ddView);
        fields.add(mmView);
        fields.add(yyyyView);

        for (EditText e : fields) {
            if (TextUtils.isEmpty(e.getText().toString())) {
                e.setError("Required.");
                valid = false;
            } else
                e.setError(null);
        }

        if (Integer.valueOf(ddView.getText().toString())>31 ||
                Integer.valueOf(ddView.getText().toString())<1 ||
                Integer.valueOf(mmView.getText().toString())>12 ||
                Integer.valueOf(mmView.getText().toString())<1 ||
                Integer.valueOf(yyyyView.getText().toString())>Calendar.getInstance().get(Calendar.YEAR) ||
                Integer.valueOf(yyyyView.getText().toString())<(Calendar.getInstance().get(Calendar.YEAR)-150)) {
            ddView.setError("Invalid date parameter");
            mmView.setError("Invalid date parameter");
            yyyyView.setError("Invalid date parameter");
            valid = false;
        }
        return valid;
    }

    private void writeNewUser(String fullName, String postalCode, String identifNo, String dob) {
        User nUser = new User(fullName, postalCode, identifNo.toUpperCase(), dob);
        usersRef.setValue(nUser);
        int count = 1000;
        while (count<=COMPANY_COUNT) {
            usersRef.child("company").child(String.valueOf(count)).setValue(false);
            count++;
        }
        usersRef.child("status").setValue(0);
        usersRef.child("uid").setValue(mAuth.getCurrentUser().getUid());
        usersRef.child("email").setValue(emailView.getText());
    }

    @Override
    public void onBackPressed() {
        mAuth.signOut();
        super.onBackPressed();
    }
}
