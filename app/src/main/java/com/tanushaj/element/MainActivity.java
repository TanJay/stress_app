package com.tanushaj.element;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tanushaj.element.fragments.HomeFragment;
import com.tanushaj.element.fragments.ProfileFragment;
import com.tanushaj.element.fragments.SessionFragment;
import com.tanushaj.element.models.QuoteDto;
import com.tanushaj.element.models.WearableHRV;
import com.tanushaj.element.services.ConsumerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SessionFragment.OnFragmentInteractionListener {
    public String TAG = "ELEMENT_TAG";
    public String TAG_NAME = "ELEMENT_TAG";
    public String PREFERENCE_NAME = "element";
 List<WearableHRV> list = new ArrayList<>();
    Interpreter tflite;
        QuoteAlert alert;

    private boolean mIsBound = false;
    private ConsumerService mConsumerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PugNotification.with(getApplicationContext())
                .load()
                .title("asd")
                .message("asdas")
                .bigTextStyle("asdas")
                .smallIcon(R.drawable.pugnotification_ic_launcher)
                .largeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.pugnotification_ic_launcher))
                .simple()
                .build();

//        PugNotification.with(MainActivity.this)
//                .load()
//                .title("Element")
//                .message("Google")
//                .bigTextStyle("Stress Level")
//                .smallIcon(R.drawable.pugnotification_ic_launcher)
//                .largeIcon(R.drawable.pugnotification_ic_launcher)
//                .flags(Notification.DEFAULT_ALL)
//                .simple()
//                .build();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_songs:
                                selectedFragment = SessionFragment.newInstance();
                                break;
                            case R.id.navigation_artists:
                                selectedFragment = HomeFragment.newInstance("hh", "hh");
                                break;
                            case R.id.navigation_albums:
                                selectedFragment = ProfileFragment.newInstance("pp", "pp");
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, HomeFragment.newInstance("hh", "hh"));
        transaction.commit();

        //Used to select an item programmatically
        bottomNavigationView.getMenu().getItem(2).setChecked(true);




        mIsBound = bindService(new Intent(MainActivity.this, ConsumerService.class), mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG_NAME, "Find");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));


        alert = new QuoteAlert();
        getQuote(this);

    }

    private void getQuote(final Activity activity){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "https://favqs.com/api/qotd";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG_NAME, "Response");
                try {
                    JSONObject jsonObject = response.getJSONObject("quote");
                    String body = jsonObject.getString("body");
                    String author = jsonObject.getString("author");
                    alert.showDialog(activity, new QuoteDto(author, body));
//                    textView.setText(String.format("%s - %s -", body, author));
//                    switchScreen();
//                    startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Register Failed Json" , Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_NAME, error.getMessage());
                Toast.makeText(getApplicationContext(), "Register Failed1", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
//            Log.d("receiver", "Got message: " + message);
            String[] items = message.split(",");
            WearableHRV hrv = new WearableHRV(items[0], Integer.valueOf(items[1]), Float.parseFloat(items[2]));
            //Date:2019-6-4 1:30:5,rrInterval:0,HR: -3
            list.add(hrv);
            if (list.size() == 50){
                predictDataByApi(list);
                list.clear();
            }

        }
    };

    public void predictDataByApi(List<WearableHRV> hrvData) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://51.158.175.210:8081/hrv/";
//                final String url = "http://3491ba42.ngrok.io";

//        Log.d(TAG_NAME, "None");

        JSONArray hrvs = new JSONArray();
        for (WearableHRV hrv : hrvData) {
            JSONObject object = new JSONObject();
            try {
                object.put("heartbeat", hrv.getHR());
                object.put("hrv", hrv.getrR());
                object.put("timestamp", hrv.getDateTime());
                hrvs.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("rr_data", hrvs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("TAGGG",  jsonBody.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
//                Log.d(TAG_NAME, "Response");
                try {
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    PugNotification.with(MainActivity.this)
                            .load()
                            .title("Element")
                            .message(message)
                            .bigTextStyle("Stress Level")
                            .smallIcon(R.drawable.pugnotification_ic_launcher)
                            .largeIcon(R.drawable.pugnotification_ic_launcher)
                            .flags(Notification.DEFAULT_ALL)
                            .simple()
                            .build();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Register Failed Json", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_NAME, "Error " + error);
                Toast.makeText(getApplicationContext(), "Register Failed1", Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", getToken());
                map.put("Content-Type", "application/json");
                return map;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }

    private String getToken(){
        SharedPreferences prefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        String loggedIn = prefs.getString("token", "");
        return loggedIn;
    }


    public static void addMessage(String data) {
        Log.d("TAGGGGGGG", data);
//        mMessageAdapter.addMessage(new Message(data));
//        String[] items = data.split(",");
//        WearableHRV hrv = new WearableHRV(items[0], Integer.valueOf(items[1]), Float.parseFloat(items[2]));
//       //Date:2019-6-4 1:30:5,rrInterval:0,HR: -3
//        list.add(hrv);
//        if (list.size() == 10){
//            predictDataByApi(list);
//        }



    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // SAAgent
    @Override
    protected void onDestroy() {
        // Clean up connections
        if (mIsBound == true && mConsumerService != null) {
            if (mConsumerService.closeConnection() == false) {
                updateTextView("Disconnected");
//                mMessageAdapter.clear();
            }
        }
        // Un-bind service
        if (mIsBound) {
            unbindService(mConnection);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            updateTextView("onServiceConnected");
            Log.d("TAGGGGGGG", "updateText");
            connectDevice();


        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            mIsBound = false;
            Log.d("TAGGGGGGG", "updateText");

            updateTextView("onServiceDisconnected");
        }
    };

    private void connectDevice() {
        if (mIsBound == true && mConsumerService != null) {
            mConsumerService.findPeers();
            Log.d(TAG_NAME, "Finding Peers");
            if (mConsumerService.sendData("Hello1 Accessory!")) {
                addMessage("Sent:Hello1 Accessory!");
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void updateTextView(final String str) {
//        mTextView.setText(str);
        Log.d("TAGGGGGGG", "updateText");

    }


    private static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }

}
