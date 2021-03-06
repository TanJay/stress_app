package com.tanushaj.element;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SessionFragment.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener, StressChartFragment.OnFragmentInteractionListener {
    public String TAG = "ELEMENT_TAG";
    public String TAG_NAME = "ELEMENT_TAG";
    public String PREFERENCE_NAME = "element";
    List<WearableHRV> list = new ArrayList<>();
    Interpreter tflite;
    QuoteAlert alert;

    private boolean mIsBound = false;
    private ConsumerService mConsumerService = null;
    NotificationManager notificationManager;
    NotificationChannel mChannel;
    int NOTIFICATION_ID = 234;
    String CHANNEL_ID = "my_channel_01";
    CharSequence name = "my_channel";
    String Description = "This is my channel";
    List<Float> rRs = new ArrayList<>();
    Python py;
    boolean showQuote = true;
    boolean isOnDeviceML = false;
    private StressViewModel stressViewModel;
    private List<StressItem> stressItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tflite = new Interpreter(loadModelFile(getAssets(), "element_lite.tflite"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        py = Python.getInstance();


        notificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

        stressViewModel = ViewModelProviders.of(MainActivity.this).get(StressViewModel.class);
//        StressItem item = new StressItem(DateConverter.fromDate(new Date(System.currentTimeMillis())));
//        stressViewModel.insert(item);
//        stressViewModel.deleteAll();
//        stressViewModel.getAllWords().observe(this, new Observer<List<StressItem>>() {
//
//            @Override
//            public void onChanged(@Nullable final List<StressItem> words) {
//                // Update the cached copy of the words in the adapter.
////                adapter.setWords(words);
//                Log.d("Taaa", String.valueOf(words.size()));
//            }
//        });
//        Log.d("Taaa", String.valueOf(stressViewModel.getAllWords()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
//            notificationManager.createNotificationChannel(mChannel);

        }


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
                new IntentFilter("hrm_info_event"));


        LocalBroadcastManager.getInstance(this).registerReceiver(disconnectReceiver,
                new IntentFilter("disconnect_event"));


        setupSharedPreferences();


    }


    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        showQuote = sharedPreferences.getBoolean("display_quote",true);
        isOnDeviceML = sharedPreferences.getBoolean("on_device_learning",true);
        if(showQuote) {
            alert = new QuoteAlert();
            getQuote(this);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int message = intent.getIntExtra("message", -1);
            if(message == 1) mConsumerService.sendData("stopservice");
            if(message == 2) mConsumerService.sendData("startservice");
        }
    };


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
            rRs.add(Float.valueOf(items[1]));
            if (list.size() == 100){
                if(!isOnDeviceML) predictDataByApi(list);
                else predictDataByOnDeviceML(rRs);
//                Log.d("Taaaa", py.getModule("main").callAttr("say_my_name", rRs).toString());
                list.clear();
                rRs.clear();
            }

        }
    };

    public void predictDataByOnDeviceML(List<Float> array){
        float[] arr = new float[array.size()+1];
        int i = 0;
        for (Iterator<Float> iterator = array.iterator(); iterator.hasNext(); i++) {
            arr[i] = iterator.next();
        }
        String arrq;
        try {
            arrq = py.getModule("main").callAttr("process", arr).toString();
            Log.d("Taaa", arrq);
            try {
                JSONArray obj = new JSONArray(arrq);
                JSONObject feature = obj.getJSONObject(0);
                JSONObject time = obj.getJSONObject(1);
                float HR = (float) feature.getDouble("mean_hr");
                float Seconds = 10.0f;
                float SDNN = (float) feature.getDouble("sdnn");
                float rmssd = (float) feature.getDouble("rmssd");
                float pNN50 = (float) feature.getDouble("pnni_50");
                float AVNN = (float) feature.getDouble("mean_nni");
                float TP = (float) time.getDouble("total_power");
                float LF = (float) time.getDouble("lf");
                float HF = (float) time.getDouble("hf");
                float VLF = (float) time.getDouble("vlf");
                float LF_HF = (float) time.getDouble("lf_hf_ratio");
                float[] inputArr = {HR, Seconds, SDNN, rmssd, pNN50, AVNN, TP, LF, HF, VLF, LF_HF};
                float[][] outputArr = new float[1][2];
                tflite.run(inputArr, outputArr);
                Log.d("Element-OnDeviceML-O", Arrays.toString(outputArr[0]));
                try {
                    int message = 1;
                    if (outputArr[0][0] == 0.0f) {
                        message = 0;
                    }
                    Intent intent = new Intent("stress_detection_event");
                    intent.putExtra("message", message);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    if (message > 0) {
                        Toast.makeText(getApplicationContext(), "Stress", Toast.LENGTH_LONG).show();

                        showNotification("Element", "Your are now stressed please listen to some beats");
                    } else {
                        Toast.makeText(getApplicationContext(), "No Stress", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG_NAME, "Response");
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Register Failed Json", Toast.LENGTH_LONG).show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch (PyException e) {
            e.printStackTrace();
        }
    }


    public void predictDataByApi(List<WearableHRV> hrvData) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://51.158.175.210:2001/hrv/";
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
                Log.d(TAG_NAME, "Response");
                try {
                    int message = response.getInt("stress_prediction");
                    Intent intent = new Intent("stress_detection_event");
                    intent.putExtra("message", message);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    if(message > 0){
                        Toast.makeText(getApplicationContext(), "Stress", Toast.LENGTH_LONG).show();



                        showNotification("Element", "Your are now stressed please listen to some beats");
                    }else{
                        Toast.makeText(getApplicationContext(), "No Stress", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG_NAME, "Response");
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

    private void showNotification(String title, String message){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
//                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentText(message);

        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
        stackBuilder.addNextIntent(resultIntent);
        stackBuilder.addParentStack(MainActivity.class);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

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

    /** Memory-map the model file in Assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }





    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mConsumerService.sendData("stopservice");
    }

    // SAAgent
    @Override
    protected void onDestroy() {
//        mConsumerService.sendData("stopservice");
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
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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
        if (mIsBound && mConsumerService != null) {
            mConsumerService.findPeers();
            Log.d(TAG_NAME, "Finding Peers");
        }
    }

    public static void updateTextView(final String str) {
//        mTextView.setText(str);
        Log.d("TAGGGGGGG", "updateText");

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("display_quote")) {
            Log.d("Taaaa", String.valueOf(sharedPreferences.getBoolean("display_quote",true)));
        }else if(key.equals("on_device_learning")){
            isOnDeviceML = sharedPreferences.getBoolean("on_device_learning",true);
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
