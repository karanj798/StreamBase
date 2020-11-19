package com.example.streambase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    EditText search;
    ListView listOfResults;
    private RequestQueue queue;
    private JSONArray cache;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        if (mActionBarToolbar != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        search = (EditText) findViewById(R.id.search);
        listOfResults = (ListView) findViewById(R.id.listOfResults);
        queue = Volley.newRequestQueue(this);
        typeface = getResources().getFont(R.font.roboto_medium2);


//        search.setOnKeyListener((view, i, keyEvent) -> {
//            // if Enter key is pressed invoke Volley
//            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
//                StringRequest getMovie = searchNameStringRequest(search.getText().toString());
//                queue.add(getMovie);
//                return true;
//            }
//            return false;
//        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                StringRequest getMovie = searchNameStringRequest(s.toString());
                queue.add(getMovie);
            }
        });


        listOfResults.setVisibility(View.INVISIBLE);

        listOfResults.setOnItemClickListener((parent, view, position, id) -> {
            try {
                JSONObject selected = cache.getJSONObject(position);
                Intent intent = new Intent(SearchActivity.this, MediaInfoActivity.class);
                intent.putExtra("selected", selected.toString());
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private StringRequest searchNameStringRequest(String nameSearch) {
        String url = getString(R.string.url) + nameSearch + "&country=ca";
        // ERROR
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    // SUCCESS
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            cache = result.getJSONArray("results");
                            JSONArray results = result.getJSONArray("results");
                            ArrayList<String> moviesOrShows = new ArrayList<>();
                            if (results.length() == 0) {
                                moviesOrShows.add("No Results");
                            } else {
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject movieOrShow = results.getJSONObject(i);
                                    moviesOrShows.add(movieOrShow.getString("name"));
                                }
                            }

                            ListAdapter adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, moviesOrShows) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent){
                                    /// Get the Item from ListView
                                    View view = super.getView(position, convertView, parent);

                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                                    // Set the text size 25 dip for ListView each item
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                                    tv.setTypeface(typeface);

                                    // Return the view
                                    return view;
                                }
                            };
                            listOfResults.setVisibility(View.VISIBLE);
                            listOfResults.setAdapter(adapter);

                        } catch (JSONException e) {
                            Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                (Response.ErrorListener) error -> {
                    // display a simple message on the screen
                    Toast.makeText(SearchActivity.this, "Utelly is not responding", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  headers = new HashMap<>();
                headers.put("x-rapidapi-host", getString(R.string.host));
                headers.put("x-rapidapi-key", getString(R.string.key));
                return headers;
            }
        };
    }
}