package com.example.collegeadminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.io.File;
import java.util.HashMap;

public class UplaodEbook extends AppCompatActivity {
    private CardView uploadEbook;
    private EditText ebookTitle;
    private Button uploadEbookButton;
    private TextView ebookTextView;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog pd;
    private final int req=1;
    private Uri ebookData;
    private String ebookName,title,uniqueKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uplaod_ebook);

        uploadEbook=(CardView)findViewById(R.id.uploadEbook);
        ebookTitle=(EditText)findViewById(R.id.ebookTitle);
        uploadEbookButton=(Button)findViewById(R.id.uploadEbookButton);
        ebookTextView=(TextView)findViewById(R.id.ebookTextView);

        pd=new ProgressDialog(this);

        uploadEbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        uploadEbookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title=ebookTitle.getText().toString();
                if(title.isEmpty()){
                    ebookTitle.setError("Empty Title");
                    ebookTitle.requestFocus();
                }
                else if(ebookData==null)
                {
                    Toast.makeText(UplaodEbook.this, "Please Select Ebook to Upload", Toast.LENGTH_SHORT).show();
                }
                else{
                    uploadEbook();
                }
            }
        });
    }

    private void uploadEbook() {
        pd.setTitle("please wait...");
        pd.setMessage("uploading...");
        pd.show();
        storageReference= FirebaseStorage.getInstance().getReference();
        storageReference=storageReference.child("Ebooks/"+ebookName+"-"+System.currentTimeMillis());
        storageReference.putFile(ebookData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri uri=uriTask.getResult();
                uploadData(String.valueOf(uri));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UplaodEbook.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void uploadData(String downloadUrl) {
        databaseReference= FirebaseDatabase.getInstance().getReference();
        uniqueKey=databaseReference.child("Ebooks").push().getKey();
        HashMap data=new HashMap();
        data.put("ebookTitle",title);
        data.put("ebookUrl",downloadUrl);
        databaseReference.child("Ebooks").child(uniqueKey).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                Toast.makeText(UplaodEbook.this, "Ebook Uploaded Successfully", Toast.LENGTH_SHORT).show();
                ebookTextView.setText("No File Selected");
                ebookTitle.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UplaodEbook.this, "Ebook not Uploaded", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openGallery() {
        //StackOverflow
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("ap");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//        try {
//            startActivityForResult(
//                    Intent.createChooser(intent, "Select Ebook"),req);
//        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog


        Intent pickEbook=new Intent();

        pickEbook.setType("application/pdf");
        pickEbook.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(pickEbook,"Select Ebook"),req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==req&&resultCode==RESULT_OK){
            ebookData=data.getData();
//            Toast.makeText(this, ebookData.toString(), Toast.LENGTH_SHORT).show();
            if(ebookData.toString().startsWith("content://")){
                try {
                    Cursor cursor=null;
                    cursor=UplaodEbook.this.getContentResolver().query(ebookData,null,null,null,null);
                    if(cursor!=null && cursor.moveToFirst()){
                        ebookName=cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(ebookData.toString().startsWith("file://")){
                ebookName=new File(ebookData.toString()).getName();
            }
            ebookTextView.setText(ebookName);

        }
    }


}