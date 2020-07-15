package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.es.findsoccerplayers.adapter.SettingsAdapter;
import com.es.findsoccerplayers.models.SettingsElement;

import java.util.ArrayList;
import java.util.List;

public class ActivitySettings extends AppCompatActivity {
    final List<SettingsElement> settElemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settElemList.add(new SettingsElement(R.drawable.ic_account,"Account","View and modify account options"));
        settElemList.add(new SettingsElement(R.drawable.ic_edit_location_24,"Edit search range","Modify the searching distance for the available matches"));
        settElemList.add(new SettingsElement(R.drawable.ic_log_out_24,"Log out","Disconnect from your account"));

        RecyclerView mRecyclerView = findViewById(R.id.settings_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        SettingsAdapter mAdapter = new SettingsAdapter(settElemList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SettingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position == 0){
                    startActivity(new Intent(getApplicationContext(), ActivityAccount.class));
                }else if(position == 1){
                    startActivity(new Intent(getApplicationContext(), ActivitySetLocation.class));
                }else if(position == 2){
                    Utils.showUnimplementedToast(getApplicationContext());
                }
            }
        });

    }
}