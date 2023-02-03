package com.artuok.appwork;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.artuok.appwork.dialogs.AnnouncementDialog;
import com.artuok.appwork.dialogs.PermissionDialog;
import com.artuok.appwork.fragmets.AlarmsFragment;
import com.artuok.appwork.fragmets.AveragesFragment;
import com.artuok.appwork.fragmets.AwaitingFragment;
import com.artuok.appwork.fragmets.CalendarFragment;
import com.artuok.appwork.fragmets.ChatFragment;
import com.artuok.appwork.fragmets.SettingsFragment;
import com.artuok.appwork.fragmets.SubjectsFragment;
import com.artuok.appwork.fragmets.homeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    //Navigation
    private BottomNavigationView navigation;
    private DrawerLayout drawerLayout;
    public Fragment currentFragment;
    private NavigationView navigationView;
    //fragments
    homeFragment homefragment = new homeFragment();
    AwaitingFragment awaitingFragment = new AwaitingFragment();
    CalendarFragment calendarFragment = new CalendarFragment();
    SubjectsFragment subjectsFragment = new SubjectsFragment();
    AveragesFragment averagesFragment = new AveragesFragment();
    AlarmsFragment alarmsFragment = new AlarmsFragment();
    ChatFragment chatFragment = new ChatFragment();


    SettingsFragment settingsFragment = new SettingsFragment();

    Fragment firstCurrentFragment = homefragment;
    Fragment secondCurrentFragment = awaitingFragment;
    Fragment thirdCurrentFragment = calendarFragment;
    Fragment fourCurrentFragment = subjectsFragment;


    //floating button
    FloatingActionButton actionButton;

    //Toolbar
    Toolbar toolbar;

    //Dialog
    Calendar alarmset;

    private static MainActivity instance;

    int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = findViewById(R.id.bottom_navigation);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        startFragment(homefragment);
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_list));
        toolbar.setNavigationOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        instance = this;

        actionButton = findViewById(R.id.floating_button);

        alarmset = Calendar.getInstance();
        PushDownAnim.setPushDownAnimTo(actionButton)
                .setScale(PushDownAnim.MODE_SCALE, 0.98f)
                .setDurationPush(100)
                .setOnClickListener(view -> {
                    Intent i = new Intent(this, CreateAwaitingActivity.class);
                    i.getIntExtra("requestCode", 2);

                    resultLauncher.launch(i);
                });

        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
        navigationView.setNavigationItemSelectedListener(mOnItemSelectedListener);


        if (getIntent().getExtras() != null)
            if (getIntent().getStringExtra("task").equals("do tasks"))
                navigation.setSelectedItemId(R.id.awaiting_fragment);
    }


    @Override
    protected void onResume() {
        navigateTo(position - 1);
        Preferences();
        super.onResume();
    }

    public void navigateTo(int n) {
        switch (n) {
            case 0:
                navigation.setSelectedItemId(R.id.homefragment);
                break;
            case 1:
                navigation.setSelectedItemId(R.id.awaiting_fragment);
                break;
            case 2:
                navigation.setSelectedItemId(R.id.calendar_fragment);
                break;
            case 3:
                navigation.setSelectedItemId(R.id.nav_subject);
                break;
            case 4:
                navigation.setSelectedItemId(R.id.homefragment);
                position = 5;
                LoadTextFragment(chatFragment, getString(R.string.chat));
                break;
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data.getIntExtra("requestCode", 0) == 3) {
                    } else if (data.getIntExtra("requestCode", 0) == 2) {
                        updateWidget();
                        notifyAllChanged();
                    }
                }
            }
    );

    public void updateWidget() {

    }

    public void showAnnouncement() {
        AnnouncementDialog dialog = new AnnouncementDialog();

        dialog.show(getSupportFragmentManager(), "announcement");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        return true;
    }

    boolean changesFromDrawer = false;

    NavigationView.OnNavigationItemSelectedListener mOnItemSelectedListener = item -> {
        drawerLayout.close();
        changesFromDrawer = true;
        navigation.setSelectedItemId(R.id.homefragment);


        switch (item.getItemId()) {
            case R.id.nav_averages:
                LoadFragment(averagesFragment);
                return true;
            case R.id.nav_settings:
                LoadTextFragment(settingsFragment, getString(R.string.settings_menu));
                return true;
            case R.id.nav_alarms:
                LoadFragment(alarmsFragment);
                return true;
            case R.id.nav_chat:
                position = 5;
                LoadTextFragment(chatFragment, getString(R.string.chat));
                return true;
        }

        return false;
    };

    NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener = item -> {

        changesFromDrawer = false;
        switch (item.getItemId()) {
            case R.id.homefragment:
                position = 1;
                LoadFragment(homefragment);
                return true;
            case R.id.awaiting_fragment:
                position = 2;
                LoadFragment(awaitingFragment);
                return true;
            case R.id.calendar_fragment:
                position = 3;
                LoadFragment(calendarFragment);
                return true;
            case R.id.nav_subject:
                position = 4;
                LoadFragment(subjectsFragment);
                return true;
        }

        return false;
    };

    private void startFragment(Fragment fragment) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction();
        transaction.replace(R.id.frameLayoutMain, fragment);
        if (position == 1) {
            firstCurrentFragment = fragment;
        } else if (position == 2) {
            secondCurrentFragment = fragment;
        } else if (position == 3) {
            thirdCurrentFragment = fragment;
        } else if (position == 4) {
            fourCurrentFragment = fragment;
        }
        currentFragment = fragment;
        transaction.commit();
    }

    void LoadTextFragment(Fragment fragment, String title) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction();
        if (fragment.isAdded()) {
            transaction
                    .hide(currentFragment)
                    .show(fragment);

        } else {
            transaction
                    .hide(currentFragment)
                    .add(R.id.frameLayoutMain, fragment);
        }

        if (title == getString(R.string.chat)) {
            actionButton.setImageResource(R.drawable.message_circle);
            PushDownAnim.setPushDownAnimTo(actionButton)
                    .setScale(PushDownAnim.MODE_SCALE, 0.98f)
                    .setDurationPush(100)
                    .setOnClickListener(view -> {
                        SharedPreferences sharedPreferences = getSharedPreferences("chat", MODE_PRIVATE);
                        boolean b = sharedPreferences.getBoolean("logged", false);
                        if (b) {
                            Intent i = new Intent(this, SelectActivity.class);
                            if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                startActivity(i);
                            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                                showOnContextUI();
                            } else {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                            }
                        } else {
                            Toast.makeText(this, "Login to be able to chat", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            actionButton.setImageResource(R.drawable.ic_baseline_add_24);
            PushDownAnim.setPushDownAnimTo(actionButton)
                    .setScale(PushDownAnim.MODE_SCALE, 0.98f)
                    .setDurationPush(100)
                    .setOnClickListener(view -> {
                        Intent i = new Intent(this, CreateAwaitingActivity.class);
                        i.getIntExtra("requestCode", 2);
                        resultLauncher.launch(i);
                    });
        }


        currentFragment = fragment;
        transaction.commit();

        ((TextView) toolbar.findViewById(R.id.title)).setText(title);
    }


    public void LoadFragment(Fragment fragment) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction();
        if (fragment.isAdded()) {
            transaction
                    .hide(currentFragment)
                    .show(fragment);

        } else {
            transaction
                    .hide(currentFragment)
                    .add(R.id.frameLayoutMain, fragment);
        }

        actionButton.setImageResource(R.drawable.ic_baseline_add_24);
        PushDownAnim.setPushDownAnimTo(actionButton)
                .setScale(PushDownAnim.MODE_SCALE, 0.98f)
                .setDurationPush(100)
                .setOnClickListener(view -> {
                    Intent i = new Intent(this, CreateAwaitingActivity.class);
                    i.getIntExtra("requestCode", 2);

                    resultLauncher.launch(i);
                });

        currentFragment = fragment;
        transaction.commit();

        ((TextView) toolbar.findViewById(R.id.title)).setText(MainActivity.this.getString(R.string.app_name));
    }

    public void notifyAllChanged() {
        if (homefragment.isAdded()) {
            homefragment.NotifyDataAdd();
        }
        if (awaitingFragment.isAdded()) {
            awaitingFragment.NotifyChanged();
        }
        if (calendarFragment.isAdded()) {
            calendarFragment.NotifyChanged();
        }
        if (averagesFragment.isAdded()) {
            averagesFragment.notifyDataChanged();
        }
    }

    public void notifyChanged(int pos) {
        if (homefragment.isAdded() && position != 1) {
            homefragment.NotifyDataChanged(pos);
        }
        if (awaitingFragment.isAdded() && position != 2) {
            awaitingFragment.NotifyChanged();
        }
        if (calendarFragment.isAdded() && position != 3) {
            calendarFragment.NotifyChanged();
        }
        if (averagesFragment.isAdded()) {
            averagesFragment.notifyDataChanged();
        }
    }

    void Preferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean b = sharedPreferences.getBoolean("DarkMode", false);

        if (b) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void showOnContextUI() {
        PermissionDialog dialog = new PermissionDialog();
        dialog.setTitleDialog(getString(R.string.required_permissions));
        dialog.setTextDialog(getString(R.string.permissions_read_contacts_description));
        dialog.setPositive((view, which) -> requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS));

        dialog.setNegative((view, which) -> {
            dialog.dismiss();
        });

        dialog.setDrawable(R.drawable.users);
        dialog.show(getSupportFragmentManager(), "permissions");
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Intent i = new Intent(this, SelectActivity.class);
                    startActivity(i);
                }
            });

    public static MainActivity getInstance() {
        return instance;
    }

    public void notifyCalendar() {
        calendarFragment.NotifyChanged();
    }
}