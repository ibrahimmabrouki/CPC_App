package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cpc.databinding.ActivityPatientHomePageBinding;

public class PatientHomePageActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityPatientHomePageBinding binding;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPatientHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        //userId = "4";
        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.patient_fragment_container, new patient_overview())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                selectedFragment = new patient_overview();
            }
            else if (id == R.id.nav_appointments) {
                selectedFragment = new patient_appointments();
            }
            else if (id == R.id.nav_records) {
                selectedFragment = new patient_records();
            }
            /*else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            }*/

            if (selectedFragment != null) {
                // Pass the userId to the fragment
                Bundle bundle = new Bundle();
                bundle.putString("user_id", userId); // Add the user_id to the bundle
                selectedFragment.setArguments(bundle);

                // Replace the current fragment with the selected one
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.patient_fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });



        /*DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_patient_home_page);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_global_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.patient_fragment_container);
            if (currentFragment instanceof RefreshableFragment) {
                ((RefreshableFragment) currentFragment).onRefresh();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_patient_home_page);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}