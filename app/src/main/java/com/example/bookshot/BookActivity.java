package com.example.bookshot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookActivity extends AppCompatActivity {

    TextView bookTitle, bookAuthors, pageCount, publishDate, description;
    ImageView bookImage;
    Button chooseButton;
    JSONObject bookObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        bookTitle = findViewById(R.id.bookTitle);
        bookAuthors = findViewById(R.id.bookAuthor);
        bookImage = findViewById(R.id.bookImage);
        pageCount = findViewById(R.id.pageCount);
        publishDate = findViewById(R.id.publishDate);
        description = findViewById(R.id.description);
        chooseButton = findViewById(R.id.chooseButton);
        Bundle bundle = getIntent().getExtras();
        String stringJsonObject = bundle.getString("jsonBookObject");
        try {
            bookObj = new JSONObject(stringJsonObject);
            extractBook();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractBook() throws JSONException {
        bookTitle.setText(bookObj.getString("title"));
        bookAuthors.setText(bookObj.getString("authors"));
        pageCount.setText(bookObj.getString("pageCount"));
        Picasso.get().load(bookObj.getString("imageLink")).into(this.bookImage);
        description.setText(bookObj.getString("description"));
        publishDate.setText(bookObj.getString("publishDate"));
    }

    public void onChooseClick(View view) {

    }
}