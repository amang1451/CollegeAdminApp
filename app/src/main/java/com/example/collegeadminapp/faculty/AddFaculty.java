package com.example.collegeadminapp.faculty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.collegeadminapp.R;
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

public class AddFaculty extends AppCompatActivity {
    private ImageView addFacultyImage;
    private EditText addFacultyName,addFacultyEmail,addFacultyPost;
    private Spinner addFacultyCategory;
    private Button addFacultyButton;
    private ProgressDialog pd;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private final int req=1;
    private Bitmap bitmap=null;
    private String branch;
    String[] branches=new String[]{"Select Branch","Computer Science & Engineering","Information Technology","Electronics & Communication Engineering",
    "Electrical Engineering","Mechanical Engineering","Civil Enigineering","Chemical Engineering","Applied Science","Master of Business Administration"};

    private String name,email,post,downloadUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        addFacultyImage=(ImageView)findViewById(R.id.addFacultyImage);
        addFacultyName=(EditText)findViewById(R.id.addFacultyName);
        addFacultyEmail=(EditText)findViewById(R.id.addFacultyEmail);
        addFacultyPost=(EditText)findViewById(R.id.addFacultyPost);
        addFacultyCategory=(Spinner)findViewById(R.id.addFacultyCategory);
        addFacultyButton=(Button)findViewById(R.id.addFacultyButton);
        pd=new ProgressDialog(this);

        addFacultyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        addFacultyCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,branches));

        addFacultyCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                branch=addFacultyCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addFacultyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVaidation();
            }
        });

    }

    private void checkVaidation() {
        name=addFacultyName.getText().toString();
        email=addFacultyEmail.getText().toString();
        post=addFacultyPost.getText().toString();
        if(name.isEmpty())
        {
            addFacultyName.setError("Please Enter Name");
            addFacultyName.requestFocus();
        }
        else if(email.isEmpty())
        {
            addFacultyEmail.setError("Please Enter Email");
            addFacultyEmail.requestFocus();
        }
        else if(post.isEmpty())
        {
            addFacultyPost.setError("Please Enter Post");
            addFacultyPost.requestFocus();
        }
        else if(branch.equals("Select Branch"))
        {
            Toast.makeText(this, "Please Provide branch", Toast.LENGTH_SHORT).show();
        }
        else if(bitmap==null){
            pd.setTitle("Please Wait");
            pd.setMessage("uploading....");
            pd.show();
            insertData();
        }
        else
        {
            pd.setTitle("Please Wait");
            pd.setMessage("uploading....");
            pd.show();
            insertImage();
        }

    }

    private void insertImage() {
        storageReference= FirebaseStorage.getInstance().getReference();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] finalImage=baos.toByteArray();
        final StorageReference filePath=storageReference.child("faculty").child(finalImage+"jpeg");
        final UploadTask uploadTask=filePath.putBytes(finalImage);
        uploadTask.addOnCompleteListener(AddFaculty.this,new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                    insertData();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(AddFaculty.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }
        });
    }

    private void insertData() {
        databaseReference= FirebaseDatabase.getInstance().getReference().child("faculty");
        databaseReference=databaseReference.child(branch);
        final String uniqueKey=databaseReference.push().getKey();
        FacultyData fd=new FacultyData(name,email,post,downloadUrl,uniqueKey);
        databaseReference.child(uniqueKey).setValue(fd).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(AddFaculty.this, "Faculty Added Successfully. ", Toast.LENGTH_SHORT).show();
                addFacultyImage.setImageResource(R.drawable.avatar);
                bitmap=null;
                addFacultyName.setText(null);
                addFacultyEmail.setText(null);
                addFacultyPost.setText(null);
                addFacultyCategory.setSelection(0);
                addFacultyName.requestFocus();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddFaculty.this, "Faculty not Added.", Toast.LENGTH_SHORT).show();
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
            Uri uri=data.getData();
            try {
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addFacultyImage.setImageBitmap(bitmap);
        }
    }

}


