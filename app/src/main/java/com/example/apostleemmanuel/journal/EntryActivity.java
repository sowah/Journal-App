package com.example.apostleemmanuel.journal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.Random;

public class EntryActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mEntryTitle;
    private EditText mEntryContent;
    private Button mAddEntryBtn;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();


        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Entries");




        mProgress = new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mAddEntryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createEntry();
            }
        });
    }

    private void createEntry() {

        mProgress.setMessage("Adding to Diary....");
        final String title_val = mEntryTitle.getText().toString().trim();
        final String content_val = mEntryContent.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(content_val) && mImageUri != null){  //Check if all content is provided
            mProgress.show();

            StorageReference filepath = mStorage.child("Entry_images").child(mImageUri.getLastPathSegment()); // Provide Firebase filepath

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //Add file to Firebase
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri dowloadUri = taskSnapshot.getDownloadUrl(); //Get file url from firebase

                    DatabaseReference newEntry = mDatabase.push();

                    newEntry.child("title").setValue(title_val);
                    newEntry.child("content").setValue(content_val);
                    newEntry.child("image").setValue(dowloadUri.toString());
                    newEntry.child("uid").setValue(mCurrentUser.getUid());
                    newEntry.child("date").setValue(new Date().toString().substring(0, 10) + new Date().toString().substring(23, 28));

                    mProgress.dismiss();

                    startActivity(new Intent(EntryActivity.this, DisplayActivity.class));

                }
            });


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16,9)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                mSelectImage.setImageURI(resultUri);
                mImageUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(9);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
