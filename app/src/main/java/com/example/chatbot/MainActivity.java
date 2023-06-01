package com.example.chatbot;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    LinearLayout layoutList;
    AppDatabase db;

    Button insert,fetch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutList = findViewById(R.id.laylist);
        insert = findViewById(R.id.insert);
        fetch = findViewById(R.id.fetch);

        View v = getLayoutInflater().inflate(R.layout.lay_question,null,false);
        TextView textView = v.findViewById(R.id.answer);
        EditText editText = v.findViewById(R.id.qn);
        Button submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAi(MainActivity.this,editText.getText().toString(),textView);
            }
        });

        layoutList.addView(v);
        db = AppDatabase.getInstance(this);

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch();
            }
        });


    }

    private void fetch(){
        for(User u:db.userDao().getAllUsers()){
            Log.d("gfhdbvhdv",u.getName()+" answer = "+u.getAnswer());
        }
    }

    private void insert(String qn,String answer){

        UserDao userDao = db.userDao();
        User user = new User(qn,answer);
        userDao.insert(user);

    }
    private void openAi(Context context, String prompt,TextView textView){
//        progressBar.setVisibility(View.GONE);
        RequestQueue queue = Volley.newRequestQueue(context);
        String apiKey = "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";
        String url = "https://api.openai.com/v1/engines/text-davinci-003/completions";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", prompt);
            jsonBody.put("max_tokens", 1087);
            jsonBody.put("n", 1);
            jsonBody.put("stop", JSONObject.NULL);
            jsonBody.put("temperature", 0.5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response here
                        try {
                            JSONArray choices = response.getJSONArray("choices");
                            JSONObject firstChoice = choices.getJSONObject(0);
                            String generatedText = firstChoice.getString("text");
                            Log.d("OpenAIDATA", generatedText);
                            textView.setText(generatedText);
                            insert(prompt,generatedText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error here
                        Log.e("OpenAI", error.toString());
//                        progressBar.setVisibility(View.GONE);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + apiKey);
                headers.put("Content-Type", "application/json");
                return headers;
            }


        };
        queue.add(request);

    }


}