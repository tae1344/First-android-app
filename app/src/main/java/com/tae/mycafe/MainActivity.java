package com.tae.mycafe;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tae.mycafe.ui.favorite.FavoriteFragment;
import com.tae.mycafe.ui.home.HomeFragment;
import com.tae.mycafe.ui.serch.NearbyFragment;
import com.tae.mycafe.ui.serch.RegionFragment;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 4개의 메뉴에 들어갈 Fragment들
    private HomeFragment homeFragment = new HomeFragment();
    private NearbyFragment nearbyFragment= new NearbyFragment();
    private RegionFragment regionFragment = new RegionFragment();
    private FavoriteFragment favoriteFragment = new FavoriteFragment();

    public static final String TAG = "MyTagName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 구글 플레이스 api 초기화
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyASEkAIx20DKeEh-PCSn3LljnUfGgU7-Wo");
        }
/*
        // 구글 플레이스 자동완성 구현 -->
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                Log.d(TAG, "Place: " + place.getName() + ", " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d(TAG, "An error occurred: " + status);
            }
        });*/
        // 구글 플레이스 자동완성 구현 --<

        // 하단 nav
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 첫 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_view, homeFragment).commitAllowingStateLoss();

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home: {
                        transaction.replace(R.id.fragment_view, homeFragment).commit();
                        break;
                    }
                    case R.id.navigation_nearby: {
                        transaction.replace(R.id.fragment_view, nearbyFragment).commit();
                        break;
                    }
                    /*case R.id.navigation_region: {
                        transaction.replace(R.id.fragment_view, regionFragment).commit();
                        break;
                    }
                    case R.id.navigation_favorite: {
                        transaction.replace(R.id.fragment_view, favoriteFragment).commitAllowingStateLoss();
                        break;
                    }*/
                }
                return true;
            }
        });
    }


}
