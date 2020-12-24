package com.example.bookshot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onImageButtonClick(View view) {
        Intent intent = new Intent(this, ImageActivity.class);
        startActivity(intent);
    }

    public void onSearchButtonClick(View view) {
        Intent intent = new Intent(this, CatalogActivity.class);
        startActivity(intent);
    }

    public void onDataBaseButtonClick(View view) {
        Intent intent = new Intent(this, DataBaseActivity.class);
        startActivity(intent);
    }

}