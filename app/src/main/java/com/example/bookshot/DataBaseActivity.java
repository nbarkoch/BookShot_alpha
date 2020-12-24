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

public class DataBaseActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Book> books;
    SimpleAdapter adapter;
    View hidingResults;
    ProgressBar progressBar;
    int MY_SOCKET_TIMEOUT_MS = 10000;
    private static String JSON_URL_DATABASE =
            "https://eyivmcjz91.execute-api.us-east-2.amazonaws.com/v1?type=1";
    JSONArray booksArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base);
        setTitle("ספרים במאגר:");
        hidingResults = findViewById(R.id.hidingResults);
        books = new ArrayList<>();
        recyclerView = findViewById(R.id.booksDataBaseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new SimpleAdapter(getApplicationContext(), books, new SimpleAdapter.ItemClickListener() {
            @Override
            public void onInfoClick(View view, int position) {
                moveToBookActivity(position);
            }
        });
        recyclerView.setAdapter(adapter);
        extractBooks();
    }

    private void moveToBookActivity(int position) {
        Intent intent = new Intent(this, BookActivity.class);
        try {
            JSONObject bookObj = booksArray.getJSONObject(position);
            intent.putExtra("jsonBookObject", bookObj.toString());
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void extractBooks() {
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, JSON_URL_DATABASE, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject contactObj = new JSONObject(String.valueOf(response));
                    JSONObject resultObj = contactObj.getJSONObject("Result");
                    booksArray = resultObj.getJSONArray("books");
                    for (int i = 0; i < booksArray.length(); ++i) {
                        JSONObject bookObj = booksArray.getJSONObject(i);
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