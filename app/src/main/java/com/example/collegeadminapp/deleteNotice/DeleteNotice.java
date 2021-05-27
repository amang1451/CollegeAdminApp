package com.example.collegeadminapp.deleteNotice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.collegeadminapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteNotice extends AppCompatActivity {

    private RecyclerView deleteNoticeRV;
    private ProgressBar progressBar;
    private ArrayList<NoticeData> list;
    private NoticeAdapter adapter;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_notice);
        deleteNoticeRV=findViewById(R.id.deleteNoticeRV);
        progressBar=findViewById(R.id.progressBar);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Notice");

        deleteNoticeRV.setLayoutManager(new LinearLayoutManager(this));
        deleteNoticeRV.setHasFixedSize(true);

        getNotice();
    }

    private void getNotice() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list=new ArrayList<>();
                for(DataSnapshot dp:snapshot.getChildren())
                {
                    NoticeData data=dp.getValue(NoticeData.class);
                    list.add(data);
                }

                adapter=new NoticeAdapter(DeleteNotice.this,list);
                adapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                deleteNoticeRV.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeleteNotice.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}