package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ProfileActivity extends BaseActivity implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference usersRef;
    private String userID;
    private Uri downloadUri;

    private TextView emailView;
    private TextView statusView;
    private TextView idView;
    private EditText nameView;
    private EditText postalCodeView;
    private EditText identifNoView;
    private ImageView mImageView;
    private TextView pleaseUpload;
    private Spinner mSpinner;
    private ImageButton takePhotoButton;
    private Button verifyEmailButton;
    private StorageReference storageRef;
    private Bitmap imageBitmap;

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
        mImageView = (ImageView) findViewById(R.id.Profile_ImageView);
        pleaseUpload = (TextView) findViewById(R.id.Profile_pleaseUpload_TextView);
        mSpinner = (Spinner) findViewById(R.id.Profile_Identification_spinner);
        takePhotoButton = (ImageButton) findViewById(R.id.Profile_TakePhoto_button);
        verifyEmailButton = (Button) findViewById(R.id.verify_email_button);

        Intent intent = getIntent();
        emailView.setText(intent.getStringExtra("E-mail"));
        idView.setText(intent.getStringExtra("ID"));
        if (mAuth.getCurrentUser().isEmailVerified()) statusView.setText("[VERIFIED]");
        else statusView.setText("[UNVERIFIED]");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.identification_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser()!=null &&
                mAuth.getCurrentUser().isEmailVerified())
            verifyEmailButton.setEnabled(false);

        if (mAuth.getCurrentUser().isEmailVerified()) verifyEmailButton.setVisibility(View.INVISIBLE);

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
                        // [START_EXCLUDE]
                        // Re-enable button
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
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.verify_email_button) {
            sendEmailVerification();
        }
        if (i == R.id.sign_out_button) {
            mAuth.signOut();
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
        if (i == R.id.Profile_TakePhoto_button) {
            dispatchTakePictureIntent();
//            takePhotoButton.setText("Retake Photo");
        }
        if (i == R.id.Profile_UploadImage_button) {
            if (validateImageUpload()) uploadImagetoFirebase();
            else validateImageUpload();
        }
        if (i == R.id.Profile_Submit_button) {
            if (validateForm()) {
                String fn = nameView.getText().toString();
                String pc = postalCodeView.getText().toString();
                String id = identifNoView.getText().toString();
                writeNewUser(fn, pc, id);
                Toast.makeText(ProfileActivity.this, "Submission Successful, Please wait for your physical token to be delivered", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            } else {
                validateForm();
            }
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImagetoFirebase(){

        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
        Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);

        // CALL THIS METHOD TO GET THE ACTUAL PATH
        File finalFile = new File(getRealPathFromURI(tempUri));

        Uri file = Uri.fromFile(finalFile);
        StorageReference identifImage = storageRef.child("images/"+userID+".jpg");

        identifImage.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Get a URL to the uploaded content
                        downloadUri = taskSnapshot.getDownloadUrl();
                        usersRef.push().setValue(downloadUri.toString());
                        //mDatabase.child(userID).push().setValue(downloadUri.toString());
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private boolean validateImageUpload() {
        boolean valid = true;

        if (mImageView.getVisibility()==View.GONE) {
            pleaseUpload.setText("Please upload an image of you holding your identification documents for verification");
            pleaseUpload.setVisibility(View.VISIBLE);
            valid = false;
        } else pleaseUpload.setVisibility(View.GONE);

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

    private void writeNewUser(String fullName, String postalCode, String identifNo) {
        //FirebaseUser user =  mAuth.getCurrentUser();
        //String userId = user.getUid();
        User nUser = new User(fullName, postalCode, identifNo);
        usersRef.setValue(nUser);
    }


}
