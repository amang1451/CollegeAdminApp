package com.example.collegeadminapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.collegeadminapp.deleteNotice.DeleteNotice;
import com.example.collegeadminapp.deleteNotice.UploadNotice;
import com.example.collegeadminapp.faculty.UpdateFaculty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CardView uploadNotice,addGalleryImage,addEbook,faculty,deleteNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadNotice=(CardView)findViewById(R.id.addNotice);
        addGalleryImage=(CardView)findViewById(R.id.addGalleryImage);
        addEbook=(CardView)findViewById(R.id.addEbook);
        faculty=(CardView)findViewById(R.id.faculty);
        deleteNotice=(CardView)findViewById(R.id.deleteNotice);

        uploadNotice.setOnClickListener(this);
        addGalleryImage.setOnClickListener(this);
        addEbook.setOnClickListener(this);
        faculty.setOnClickListener(this);
        deleteNotice.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.addNotice:
                intent=new Intent(MainActivity.this, UploadNotice.class);
                startActivity(intent);
                break;
            case R.id.addGalleryImage:
                intent=new Intent(MainActivity.this,UploadImages.class);
                startActivity(intent);
                break;
            case R.id.addEbook:
                intent=new Intent(MainActivity.this,UplaodEbook.class);
                startActivity(intent);
                break;
            case R.id.faculty:
                intent=new Intent(MainActivity.this, UpdateFaculty.class);
                startActivity(intent);
                break;
            case R.id.deleteNotice:
                intent=new Intent(MainActivity.this, DeleteNotice.class);
                startActivity(intent);
                break;
        }
    }
}