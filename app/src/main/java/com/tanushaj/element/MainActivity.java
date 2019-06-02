package com.tanushaj.element;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.samsung.android.sdk.accessory.SAAgentV2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SessionFragment.OnFragmentInteractionListener {
    public String TAG = "ELEMENT_TAG";
    public String TAG_NAME = "ELEMENT_TAG";
    public String PREFERENCE_NAME = "element";

    Interpreter tflite;

    private static MessageAdapter mMessageAdapter;
    private ConsumerService mConsumerService = null;
    private SAAgentV2.RequestAgentCallback mAgentCallback = new SAAgentV2.RequestAgentCallback() {
        @Override
        public void onAgentAvailable(SAAgentV2 agent) {
            mConsumerService = (ConsumerService) agent;
        }

        @Override
        public void onError(int errorCode, String message) {
            Log.e(TAG, "Agent initialization error: " + errorCode + ". ErrorMsg: " + message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_songs:
                                selectedFragment = HomeFragment.newInstance("hh", "hh");
                                break;
                            case R.id.navigation_artists:
                                selectedFragment = SessionFragment.newInstance();
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


        mMessageAdapter = new MessageAdapter();
        SAAgentV2.requestAgent(getApplicationContext(), ConsumerService.class.getName(), mAgentCallback);

//        mMessageListView.setAdapter(mMessageAdapter);

    }

    public void predictDataByApi(WearableHRV[] hrvData) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://51.158.175.210:8081/hrv/";
        Log.d(TAG_NAME, "None");

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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG_NAME, "Response");
                try {
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Register Failed Json", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG_NAME, error.getMessage());
                Toast.makeText(getApplicationContext(), "Register Failed1", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", getToken());
                map.put("Content-Type", "application/json");
                return super.getHeaders();
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
        mMessageAdapter.addMessage(new Message(data));
    }

    public static void updateTextView(final String str) {
//        mTextView.setText(str);
    }

    @Override
    protected void onDestroy() {
        // Clean up connections
        if (mConsumerService != null) {
            if (mConsumerService.closeConnection() == false) {
//                updateTextView("Disconnected");
                mMessageAdapter.clear();
            }
            mConsumerService.releaseAgent();
            mConsumerService = null;
        }
        super.onDestroy();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class MessageAdapter extends BaseAdapter {
        private static final int MAX_MESSAGES_TO_DISPLAY = 20;
        private List<Message> mMessages;

        public MessageAdapter() {
            mMessages = Collections.synchronizedList(new ArrayList<Message>());
        }

        void addMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
                        mMessages.remove(0);
                        mMessages.add(msg);
                    } else {
                        mMessages.add(msg);
                    }
                    notifyDataSetChanged();
//                    mMessageListView.setSelection(getCount() - 1);
                }
            });
        }

        void clear() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.clear();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageRecordView = null;
            if (inflator != null) {
//                messageRecordView = inflator.inflate(R.layout.message, null);
//                TextView tvData = (TextView) messageRecordView.findViewById(R.id.tvData);
//                Message message = (Message) getItem(position);
//                tvData.setText(message.data);
            }
            return messageRecordView;
        }
    }

    private static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }

}
