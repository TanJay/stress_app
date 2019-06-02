package com.tanushaj.element;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LogInActivity extends AppCompatActivity {

    public String PREFERENCE_NAME = "element";

    EditText email;
    EditText password;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if(checkLoggedIn()) startActivity(new Intent(this, MainActivity.class));


        email = findViewById(R.id.signUpUserName);
        password = findViewById(R.id.signUpPassword);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken();
            }
        });
    }

    private Boolean checkLoggedIn(){
        SharedPreferences prefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        Boolean loggedIn = prefs.getBoolean("loggedin", false);
        return loggedIn;
    }

    private void savePreferences(String token){
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.putBoolean("loggedin", true);
        editor.apply();
    }

    private void getToken(){
        loginButton.setEnabled(false);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://51.158.175.210:8081/user/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email.getText());
            jsonBody.put("password", password.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String token = response.getString("token");
                    savePreferences(token);
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
                }
                loginButton.setEnabled(true);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loginButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
