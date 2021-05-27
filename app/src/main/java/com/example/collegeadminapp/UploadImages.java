package com.example.collegeadminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UploadImages extends AppCompatActivity {

    private Spinner imageCategory;
    private CardView uploadGalleryImage;
    private Button uploadImageButton;
    private ImageView galleryImageView;

    private String category;
    private String downloadUrl;
    private final int req=1;
    private Bitmap bitmap;
    private ProgressDialog pd;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_images);

        pd=new ProgressDialog(this);



        imageCategory=(Spinner)findViewById(R.id.imageCategory);
        uploadGalleryImage=(CardView)findViewById(R.id.uploadGalleryImage);
        uploadImageButton=(Button)findViewById(R.id.uploadImageButton);
        galleryImageView=(ImageView)findViewById(R.id.galleryImageView);

        String[] items=new String[]{"Select Category","Convocation","Independence Day","Other Events"};
        imageCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,items));

        imageCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category=imageCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uploadGalleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap==null)
                {
                    Toast.makeText(UploadImages.this, "Please Select image", Toast.LENGTH_SHORT).show();
                }
                else if(category.equals("Select Category")){
                    Toast.makeText(UploadImages.this, "Please Select Image Category", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    pd.setMessage("uploading...");
                    pd.show();
                    uploadImage();
                }

            }
        });
    }

    private void uploadImage() {
        storageReference= FirebaseStorage.getInstance().getReference().child("gallery");
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] finalImage=baos.toByteArray();
        final StorageReference filePath;
        filePath=storageReference.child(finalImage+"jpeg");
        final UploadTask uploadTask=filePath.putBytes(finalImage);
        uploadTask.addOnCompleteListener(UploadImages.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                              @Override
                              public void onSuccess(Uri uri) {
                                  downloadUrl=String.valueOf(uri);
                                  UplaodData();
                              }
                          }) ;
                        }
                    });
                }
                else{
                    pd.dismiss();
                    Toast.makeText(UploadImages.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UplaodData() {
        databaseReference= FirebaseDatabase.getInstance().getReference().child("gallery");
        databaseReference=databaseReference.child(category);
        final String uniqueKey=databaseReference.push().getKey();
        databaseReference.child(uniqueKey).setValue(downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(UploadImages.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                galleryImageView.setImageBitmap(null);
                imageCategory.setSelection(0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadImages.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    private void openGallery() {
        Intent pickImage=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==req && resultCode==RESULT_OK)
        {
            Uri uri= data.getData();
            //convert uri to bitmap
            try {
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //set image to Image view
            galleryImageView.setImageBitmap(bitmap);
        }
    }
}