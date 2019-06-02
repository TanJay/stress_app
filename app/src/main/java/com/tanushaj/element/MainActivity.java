package com.tanushaj.element;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SessionFragment.OnFragmentInteractionListener {

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
                                selectedFragment = HomeFragment.newInstance("hh","hh");
                                break;
                            case R.id.navigation_artists:
                                selectedFragment = SessionFragment.newInstance();
                                break;
                            case R.id.navigation_albums:
                                selectedFragment = ProfileFragment.newInstance("pp","pp");
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
        transaction.replace(R.id.container, HomeFragment.newInstance("hh","hh"));
        transaction.commit();

        //Used to select an item programmatically
        bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
