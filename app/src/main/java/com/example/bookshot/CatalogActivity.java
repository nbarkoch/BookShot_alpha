package com.example.bookshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView chosenRecyclerView;
    List<Book> books;
    List<Book> chosenBooks;
    EditText searchEditText;
    private static String JSON_URL =
            "https://eyivmcjz91.execute-api.us-east-2.amazonaws.com/v1?";
    CustomAdapter adapter;
    CustomAdapter chosenAdapter;
    String imageName;
    View hidingResults;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        setTitle("רשימת ספרים:");
        hidingResults = findViewById(R.id.hidingResults);
        progressBar = findViewById(R.id.progressbar);
        // ####################
        books = new ArrayList<>();
        recyclerView = findViewById(R.id.booksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new CustomAdapter(getApplicationContext(), books, new CustomAdapter.ItemClickListener() {
            @Override
            public void onChooseClick(View view, int position) {
                Toast.makeText(view.getContext(), "Choose Click for " + books.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoClick(View view, int position) {
                Toast.makeText(view.getContext(), "Info Click for " + books.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
        // #########################
        chosenBooks = new ArrayList<>();
        chosenRecyclerView = findViewById(R.id.chosenBook);
        chosenRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chosenAdapter = new CustomAdapter(getApplicationContext(), chosenBooks, new CustomAdapter.ItemClickListener() {
            @Override
            public void onChooseClick(View view, int position) {
                Toast.makeText(view.getContext(), "Choose Click for " + books.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoClick(View view, int position) {
                Toast.makeText(view.getContext(), "Info Click for " + books.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        chosenRecyclerView.setAdapter(chosenAdapter);
        // #########################
        searchEditText = findViewById(R.id.searchEditText);
        Bundle bundle = getIntent().getExtras();
        int switchMultiBook = 0;
        if (bundle != null) {
            imageName = bundle.getString("fileName");
            switchMultiBook = bundle.getInt("switchMultiple");
        }

        if (switchMultiBook == 1) {
            String stringJsonArray = bundle.getString("jsonGroupArray");
            int position = bundle.getInt("position");
            try {
                JSONArray groupArray = new JSONArray(stringJsonArray);
                JSONObject groupObj = groupArray.getJSONObject(position);
                searchEditText.setText(groupObj.getString("search_term"));
                JSONArray booksArray = groupObj.getJSONArray("books");
                extractBooks(booksArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
            chosenAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            hidingResults.setVisibility(View.GONE);
        } else {
            extractBooksByRequest(JSON_URL + "type=2&file_name=" + imageName + "&multiple=" + switchMultiBook);
        }

    }


    private void extractBooks(JSONArray booksArray) throws JSONException {
        for (int i = 0; i < booksArray.length(); i++) {
            // filed "data" is an array of optional books with the same attributes
            JSONObject bookObj = booksArray.getJSONObject(i);
            //Define your logic here to pass the data on results
            Book book = new Book();
            book.setTitle(bookObj.getString("title").toString());
            book.setAuthors(bookObj.getString("authors".toString()));
            book.setPageCount(bookObj.getString("pageCount".toString()));
            book.setBookImage(bookObj.getString("imageLink"));
            book.setDescription(bookObj.getString("description".toString()));
            book.setPublishDate(bookObj.getString("publishDate"));
            book.setSelfLink(bookObj.getString("selfLink"));
            if (i == 0)
                chosenBooks.add(book);
            else
                books.add(book);
        }
    }

    private void extractBooksByRequest(String json_request) {
        books.clear();
        chosenBooks.clear();
        progressBar.setVisibility(View.VISIBLE);
        Log.i("request URL is: ", json_request);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, json_request, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject contactObj = new JSONObject(String.valueOf(response));
                    JSONObject resultObj = contactObj.getJSONObject("Result");
                    if (!resultObj.getBoolean("found_book")) {
                        return;
                    }
                    JSONObject groupObj = resultObj.getJSONArray("groups").getJSONObject(0);
                    searchEditText.setText(groupObj.getString("search_term"));
                    JSONArray booksArray = groupObj.getJSONArray("books");
                    extractBooks(booksArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                chosenAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                hidingResults.setVisibility(View.GONE);
                Log.i("koko", "gold len" + chosenRecyclerView.getAdapter().getItemCount() + "white len" + recyclerView.getAdapter().getItemCount());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag", "onErrorResponse: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
        queue.add(jsonArrayRequest);
    }

    public void searchButton_onClick(View view) {
        String bookTitle = searchEditText.getText().toString();
        try {
            bookTitle = encodeValue(searchEditText.getText().toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        extractBooksByRequest(JSON_URL + "type=3&title=" + bookTitle);
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}