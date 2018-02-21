package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView statusView;
    private TextView idView;
    private TextView nameView;
    private TextView postalCodeView;
    private TextView identifNoView;
    private ImageView mImageView;
    private TextView pleaseUpload;
    private Spinner mSpinner;
    private Button takePhotoButton;
    private Button verifyEmailButton;
    private StorageReference storageRef;
    private Bitmap imageBitmap;

    final String TAG = "DED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://authentication-24160.firebaseio.com/");
        DatabaseReference usersRef = mDatabase.child("users");
        //Map<String, User> users = new HashMap<>();

        statusView = (TextView) findViewById(R.id.Login_Status_TextView);
        idView = (TextView) findViewById(R.id.Login_ID_TextView);
        nameView = (EditText) findViewById(R.id.Login_FirstName_EditText);
        postalCodeView = (EditText) findViewById(R.id.Login_PostalCode_EditText);
        identifNoView = (EditText) findViewById(R.id.Login_IdentifNo_EditText);
        mImageView = (ImageView) findViewById(R.id.Login_ImageView);
        pleaseUpload = (TextView) findViewById(R.id.Login_pleaseUpload_TextView);
        mSpinner = (Spinner) findViewById(R.id.Login_Identification_spinner);
        takePhotoButton = (Button) findViewById(R.id.Login_TakePhoto_button);
        verifyEmailButton = (Button) findViewById(R.id.verify_email_button);

        Intent intent = getIntent();
        idView.setText(intent.getStringExtra("ID"));
        statusView.setText(intent.getStringExtra("E-mail"));

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.identification_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser()!=null&&mAuth.getCurrentUser().isEmailVerified()) verifyEmailButton.setEnabled(false);
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
                            Toast.makeText(LoginActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(LoginActivity.this,
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
        if (i == R.id.Login_TakePhoto_button) {
            /*Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);*/
            dispatchTakePictureIntent();
            takePhotoButton.setText("Retake Photo");
        }
        if (i == R.id.Login_UploadImage_button) {
            uploadImagetoFirebase();
        }
        if (i == R.id.Login_Submit_button) {
            if (validateForm()) {
                //writeNewUser(nameView.toString(), postalCodeView.toString(), identifNoView.toString();
                //usersRef.setValueAsync(users);
                mAuth.signOut();
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            } else validateForm();
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

        String userID = mAuth.getCurrentUser().getUid();

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
                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(getApplicationContext(), "Image Upload Success", Toast.LENGTH_SHORT);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
//                        Toast.makeText(getActivity(), "Image Upload failed",
//                                Toast.LENGTH_LONG).show();
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

    private boolean validateForm() {
        boolean valid = true;

        if (mImageView.getVisibility()==View.GONE) {
            pleaseUpload.setText("Please upload an image of you holding your identification documents for verification");
            pleaseUpload.setVisibility(View.VISIBLE);
        } else pleaseUpload.setVisibility(View.GONE);

        TextView mfirstName = (TextView) findViewById(R.id.Login_FirstName_EditText);
        String firstName = mfirstName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            mfirstName.setError("Required.");
            valid = false;
        } else {
            mfirstName.setError(null);
        }

        return valid;
    }

       /* users.put("alanisawesome", new User("June 23, 1912", "Alan Turing"));
        users.put("gracehop", new User("December 9, 1906", "Grace Hopper"));

        usersRef.setValueAsync(users);*/

    private void writeNewUser(String fullName, String postalCode, String identifNo) {
        User user = new User(fullName, postalCode, identifNo);
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);

    }
}