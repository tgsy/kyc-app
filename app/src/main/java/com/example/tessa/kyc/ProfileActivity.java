package com.example.tessa.kyc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends BaseActivity implements
        View.OnClickListener {
    private static final int COMPANY_COUNT = 1004;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference usersRef;
    private String userID;
    private Uri downloadUri;
    private Uri filePath;

    private TextView emailView;
    private TextView statusView;
    private TextView idView;
    private EditText nameView;
    private EditText postalCodeView;
    private EditText identifNoView;
    private EditText ddView;
    private EditText mmView;
    private EditText yyyyView;
    private ImageView mImageView;
    private TextView pleaseUpload;
    private ImageButton takePhotoButton;
    private Button verifyEmailButton;
    private StorageReference storageRef;
    private Bitmap imageBitmap;

    private String imageUrl;
    private String imageKey;

    final String RegisterURL = "https://kyc-project.herokuapp.com/register_kyc";

    JSONObject token;
    JSONObject tokenjson;

    final String TAG = "DED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabase.child("users").child(userID);

        emailView = (TextView) findViewById(R.id.Profile_Email_TextView);
        statusView = (TextView) findViewById(R.id.Profile_Status_TextView);
        idView = (TextView) findViewById(R.id.Profile_ID_TextView);
        nameView = (EditText) findViewById(R.id.Profile_FirstName_EditText);
        postalCodeView = (EditText) findViewById(R.id.Profile_PostalCode_EditText);
        identifNoView = (EditText) findViewById(R.id.Profile_IdentifNo_EditText);
        ddView = (EditText) findViewById(R.id.Profile_DoB_Day_EditText);
        mmView = (EditText) findViewById(R.id.Profile_DoB_Month_EditText);
        yyyyView = (EditText) findViewById(R.id.Profile_DoB_Year_EditText);
        mImageView = (ImageView) findViewById(R.id.Profile_ImageView);
        pleaseUpload = (TextView) findViewById(R.id.Profile_pleaseUpload_TextView);
        takePhotoButton = (ImageButton) findViewById(R.id.Profile_TakePhoto_button);
        verifyEmailButton = (Button) findViewById(R.id.verify_email_button);

        tokenjson = new JSONObject();
        token = new JSONObject();

        Intent intent = getIntent();
        emailView.setText(intent.getStringExtra("E-mail"));
        idView.setText(intent.getStringExtra("ID"));

        if (mAuth.getCurrentUser().isEmailVerified())
            statusView.setText("[VERIFIED]");
        else
            statusView.setText("[UNVERIFIED]");

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser()!=null &&
                mAuth.getCurrentUser().isEmailVerified())
            verifyEmailButton.setEnabled(false);

        if (mAuth.getCurrentUser().isEmailVerified())
            verifyEmailButton.setVisibility(View.INVISIBLE);

        //downloadImageFromFirebase();

    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verify_email_button).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        findViewById(R.id.verify_email_button).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.verify_email_button) {
            sendEmailVerification();
        }
        if (i == R.id.Profile_TakePhoto_button) {
            dispatchTakePictureIntent();
        }
        if (i == R.id.Profile_Submit_button) {
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
                        "Registration Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainLoggedInActivity.class);
                startActivity(intent);
                finish();
            } else {
                validateForm();
            }
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Log.i("WHAT", "dispatchtakepic()");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.i("WHAT", "created image file");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("WHAT", "io exception");
                ex.printStackTrace();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i("WHAT", "continues");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                Log.i("WHAT", "got Uri: "+photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("WHAT", "took pictureintent: ");
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                Log.i("WHAT", "startedactivityforresult ");
            }
        }
    }

    String mCurrentPhotoPath;
    Uri file;

    private File createImageFile() throws IOException {
        Log.i("WHAT", "createimagefile()");
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
        Log.i("WHAT", "currentphotopath="+mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("WHAT", "onactivityresult()");
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
            pleaseUpload.setText(getResources().getString(R.string.validate_image_upload));
            pleaseUpload.setVisibility(View.VISIBLE);
            valid = false;
        } else
            pleaseUpload.setVisibility(View.GONE);
        return valid;
    }

    private boolean validateForm() {
        boolean valid = true;

        TextView mfirstName = (TextView) findViewById(R.id.Profile_FirstName_EditText);
        String firstName = mfirstName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            mfirstName.setError("Required.");
            valid = false;
        } else {
            mfirstName.setError(null);
        }

        return valid;
    }

    private void writeNewUser(String fullName, String postalCode, String identifNo, String dob) {
        User nUser = new User(fullName, postalCode, identifNo, dob);
        usersRef.setValue(nUser);

        int count = 1000;
        while (count<=COMPANY_COUNT) {
            usersRef.child("company").child(String.valueOf(count)).setValue(false);
            count++;
        }

        usersRef.child("status").setValue(0);
        usersRef.child("uid").setValue(mAuth.getCurrentUser().getUid());
        usersRef.child("token_access").setValue(0);
        usersRef.child("email").setValue(emailView.getText());
    }

    @Override
    public void onBackPressed() {
        mAuth.signOut();
        super.onBackPressed();
    }
}