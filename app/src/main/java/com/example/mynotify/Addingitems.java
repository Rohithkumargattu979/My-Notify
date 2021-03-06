package com.example.mynotify;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Addingitems extends AppCompatActivity {

    private Button Addtodatabase;
    private ImageButton mSelectimage;
    private Firebase mRef;
    private EditText Itemname;
    private EditText Itemnumber;
    private EditText Itemdescription;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;
    private Uri mImageuri = null;
    private StorageReference mStorage;
    String userId;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        setContentView(R.layout.activity_addingitems);
        Addtodatabase = (Button) findViewById(R.id.addtodatabase);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        Itemname = (EditText) findViewById(R.id.itemnamestudent);
        Itemnumber = (EditText) findViewById(R.id.itemnumberstudent);
        mDatabase = FirebaseDatabase.getInstance().getReference().child(userId).child("Student").child("item");
        mProgress = new ProgressDialog(this);
        Itemdescription = (EditText) findViewById(R.id.itemdescription);
        mSelectimage = (ImageButton) findViewById(R.id.imageselectstudent);
        mStorage = FirebaseStorage.getInstance().getReference();


        mSelectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent, GALLERY_REQUEST);

            }
        });




        Addtodatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                additem();

            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            assert data != null;
            mImageuri = data.getData();
            mSelectimage.setImageURI(mImageuri);
        }
    }

    private void additem() {

        mProgress.setMessage("Adding item");
        mProgress.show();

        String itemname = Itemname.getText().toString().trim();
        String itemnumber = Itemnumber.getText().toString().trim();
        String itemdescription = Itemdescription.getText().toString().trim();

        if(!TextUtils.isEmpty(itemname) && !TextUtils.isEmpty(itemnumber) && !TextUtils.isEmpty(itemdescription) && mImageuri != null){


            StorageReference filepath = mStorage.child("Images").child(mImageuri.getLastPathSegment());
            filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    DatabaseReference additems = mDatabase.push();
                                    additems.child("Name").setValue(itemname);
                                    additems.child("Quantity").setValue(itemnumber);
                                    additems.child("Description").setValue(itemdescription);
                                    additems.child("Image").setValue(imageUrl);
                                    mProgress.dismiss();
                                    //createNewPost(imageUrl);
                                }
                            });
                        }
                    }
                }});

        }
        else if(TextUtils.isEmpty(itemname)){
            Itemname.setError("Name is required");
            return;
        }
        else if(TextUtils.isEmpty(itemnumber)){
            Itemname.setError("Quantity is required");
            return;
        }
        else if(TextUtils.isEmpty(itemdescription)){
            Itemname.setError("Description is required");
            return;
        }
        else if(mImageuri == null){
            Itemname.setError("Image is required");
            return;
        }




    }
}
