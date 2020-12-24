package com.example.bookshot;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MultiCatalogActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Book> books;
    int MY_SOCKET_TIMEOUT_MS = 10000;
    private static String JSON_URL =
            "https://eyivmcjz91.execute-api.us-east-2.amazonaws.com/v1?";
    GroupAdapter adapter;
    String imageName;
    View hidingResults;
    JSONArray groupsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_catalog);
        setTitle("רשימת ספרים:");
        hidingResults = findViewById(R.id.hidingResults);
        books = new ArrayList<>();
        recyclerView = findViewById(R.id.booksMultiList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new GroupAdapter(getApplicationContext(), books, new GroupAdapter.ItemGroupClickListener() {
            @Override
            public void onChooseClick(View view, int position) {
                Toast.makeText(view.getContext(), "Choose Click for " + books.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMoreInfoClick(View view, int position) {
                moveToCatalogActivity(groupsArray, position);
            }
        });
        recyclerView.setAdapter(adapter);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imageName = bundle.getString("fileName");
            extractBooks(JSON_URL + "type=2&file_name=" + imageName + "&multiple=1");
        }
    }

    private void moveToCatalogActivity(JSONArray groupsArray, int position) {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.putExtra("switchMultiple", 1);
        intent.putExtra("jsonGroupArray", groupsArray.toString());
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void extractBooks(String json_request) {
        books.clear();
        final ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        //String tempURL = "https://eyivmcjz91.execute-api.us-east-2.amazonaws.com/v1?type=2&file_name=JPEG_20201215_202010_454915597.jpg";
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
                    groupsArray = resultObj.getJSONArray("groups");
                    for (int i = 0; i < groupsArray.length(); ++i) {
                        JSONArray booksArray = groupsArray.getJSONObject(i).getJSONArray("books");
                        // filed "data" is an array of optional books with the same attributes
                        JSONObject bookObj = booksArray.getJSONObject(0);
                        //Define your logic here to pass the data on results
                        Book book = new Book();
                        book.setTitle(bookObj.getString("title").toString());
                        book.setAuthors(bookObj.getString("authors".toString()));
                        book.setPageCount(bookObj.getString("pageCount".toString()));
                        book.setBookImage(bookObj.getString("imageLink"));
                        book.setDescription(bookObj.getString("description".toString()));
                        book.setPublishDate(bookObj.getString("publishDate"));
                        book.setSelfLink(bookObj.getString("selfLink"));
                        books.add(book);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                hidingResults.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag", "onErrorResponse: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonArrayRequest);
    }
}