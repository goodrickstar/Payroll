package com.glass.payroll;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity implements MI {
    static final String SITE_URL = "http://23.111.159.2/~payroll/";
    static final OkHttpClient client = new OkHttpClient();
    static final Gson gson = new Gson();
    private Settlement settlement = new Settlement();
    static FirebaseUser user;
    private final Map<String, String> STATE_MAP = new HashMap<>();
    private final locationCallback locationCallback = new locationCallback();
    private FusedLocationProviderClient mFusedLocationClient;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView profileView;
    private TextView profileName, balance, date, email;
    private FragmentManager fragmentManager;
    private SharedPreferences preferences;
    private FrameLayout content_frame;
    private MainViewModel model;
    static Truck truck;
    static Trailer trailer;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(), new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
        @Override
        public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
            if (result.getResultCode() == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                showSnack("Sign In Successful", Snackbar.LENGTH_LONG);
                signInSheet();
                drawerLayout.close();
                model.add(new LocationString(user.getUid(), ""));
            }
        }
    });

    void handleSettlementData() {
        if (settlement.getId() == 0 || truck == null) {
            handleMenuNavigation(null, false, false);
        } else {
            handleMenuNavigation(navigationView.getMenu().findItem(R.id.overview), false, false);
        }
    }

    private void signInSheet() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName());
            email.setText(user.getEmail());
            Glide.with(MainActivity.this).load(Objects.requireNonNull(user.getPhotoUrl()).toString().replace("96", "400")).circleCrop().into(profileView);
            if (preferences.getBoolean("migrate", true)) {
                returnSettlement();
            } else {
                model.setUserId(user.getUid());
                final SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                final String settlementData = preferences.getString("zero", null);
                final String truckData = preferences.getString("truck", null);
                final String trailerData = preferences.getString("trailer", null);
                if (settlementData != null && truckData != null) {
                    settlement = gson.fromJson(settlementData, Settlement.class);
                    truck = gson.fromJson(truckData, Truck.class);
                    trailer = gson.fromJson(trailerData, Trailer.class);
                    handleSettlementData();
                }
                model.settlement().observe(this, settlement -> {
                    if (settlement != null) {
                        boolean handle = MainActivity.this.settlement.getId() == 0;
                        MainActivity.this.settlement = settlement;
                        balance.setText("Bal: " + Utils.formatValueToCurrencyWhole(settlement.getBalance()));
                        if (settlement.getBalance() > 0) balance.setTextColor(Color.WHITE);
                        else balance.setTextColor(Color.RED);
                        if (handle)
                            handleSettlementData();
                        preferences.edit().putString("zero", gson.toJson(settlement)).apply();
                    }
                });
                model.truck().observe(this, truck -> {
                    boolean handle = MainActivity.truck == null;
                    MainActivity.truck = truck;
                    if (handle)
                        handleGrouping();
                    preferences.edit().putString("truck", gson.toJson(truck)).apply();
                });
                model.trailer().observe(this, trailer -> {
                    MainActivity.trailer = trailer;
                    preferences.edit().putString("trailer", gson.toJson(trailer)).apply();
                });
            }
        } else {
            profileName.setText(getString(R.string.log_in_or_out_text));
            email.setText("");
            profileView.setImageResource(R.drawable.fingerprint);
            handleMenuNavigation(null, false, false);
        }
    }

    private void returnSettlement() {
        if (user == null) {
            handleMenuNavigation(null, false, false);
            return;
        }
        client.newCall(new Request.Builder().url(SITE_URL + "fetch_settlements.php").post(new FormBody.Builder().add("userId", user.getUid()).build()).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    handleGrouping();
                    showSnack("Offline Mode", Snackbar.LENGTH_INDEFINITE);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        String dataString = response.body().string();
                        final JSONObject data = new JSONObject(dataString);
                        preferences.edit().putBoolean("migrate", false).apply();
                        if (data.getBoolean("available")) {
                            ArrayList<Settlement> x = Utils.returnSettlementArray(data.getJSONArray("settlement").toString());
                            ArrayList<Settlement> settlements = new ArrayList<>();
                            long stamp = Instant.now().getEpochSecond();
                            for (int i = 0; i < x.size(); i++) {
                                Settlement work = Utils.calculate(x.get(i));
                                work = Utils.setQuarters(work);
                                work.setUserId(user.getUid());
                                work.setStamp(stamp);
                                stamp++;
                                settlements.add(work);
                            }
                            model.add(settlements);
                            runOnUiThread(() -> signInSheet());
                        }
                    } catch (JSONException | IOException e) {
                        Log.e("onResponse()", e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && Utils.permissionsAccepted(this)) requestLocationUpdates();
    }

    private void locationUpdated(final Location location) throws IOException {
        if (location != null) {
            final Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            final List<Address> addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country_code = address.getCountryCode();
                if (country_code != null) {
                    if (city != null && state != null) {
                        city = city.replaceAll("(?i)township", "");
                        model.add(new LocationString(user.getUid(), city.trim() + ", " + getAbbreviationFromUSState(state)));
                    }
                }
            }
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(10000)
                .setMaxUpdateDelayMillis(10000)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private boolean locationGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        setContentView(R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        FirebaseApp.initializeApp(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            View v = getLayoutInflater().inflate(R.layout.action_bar, null);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.menu);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setCustomView(v);
            balance = v.findViewById(R.id.balance);
            date = v.findViewById(R.id.label);
        }
        drawerLayout = findViewById(R.id.drawer_layout);
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        navigationView = findViewById(R.id.nav_view);
        content_frame = findViewById(R.id.content_frame);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        profileView = headerLayout.findViewById(R.id.profile_photo);
        profileName = headerLayout.findViewById(R.id.profile_name);
        email = headerLayout.findViewById(R.id.email_tv);
        profileView.setOnClickListener(view -> {
            Utils.vibrate(profileView);
            if (user != null) {
                signOut();
            } else signIn();
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Utils.vibrate(navigationView);
            handleMenuNavigation(menuItem, true, true);
            return true;
        });
        fragmentManager = getSupportFragmentManager();
        signInSheet();
        STATE_MAP.putAll(createStateShorts());
        if (locationGranted()) requestLocationUpdates();
        date.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
    }

    public void handleMenuNavigation(MenuItem menuItem, boolean close, boolean physical) {
        if (physical) Utils.vibrate(drawerLayout);
        if (close && drawerLayout.isDrawerOpen(Gravity.LEFT)) drawerLayout.closeDrawers();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (menuItem != null) {
            if (menuItem.getItemId() != R.id.new_settlement) menuItem.setChecked(true);
            switch (menuItem.getItemId()) {
                case R.id.new_settlement:
                    NewSettlementFragment fragment = new NewSettlementFragment();
                    fragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme_NoActionBar_FullScreenDialog);
                    fragment.show(fragmentManager, "newSettlement");
                    break;
                case R.id.overview:
                    transaction.replace(R.id.content_frame, new FragmentOverview(), "overview");
                    break;
                case R.id.loads:
                    transaction.replace(R.id.content_frame, new FragmentLoads(), "loads");
                    break;
                case R.id.fuel:
                    transaction.replace(R.id.content_frame, new FragmentFuel(), "fuel");
                    break;
                case R.id.fixed:
                    transaction.replace(R.id.content_frame, new FragmentFixed(), "fixed");
                    break;
                case R.id.payout:
                    transaction.replace(R.id.content_frame, new FragmentPayouts(), "payout");
                    break;
                case R.id.miscellaneous:
                    transaction.replace(R.id.content_frame, new FragmentMiscellaneous(), "miscellaneous");
                    break;
                case R.id.records:
                    transaction.replace(R.id.content_frame, new FragmentRecords(), "records");
                    break;
                case R.id.statistics:
                    transaction.replace(R.id.content_frame, new FragmentStatistics(), "statistics");
                    break;
                case R.id.backup:
                    transaction.replace(R.id.content_frame, new FragmentBackup(), "backup");
                    break;
                case R.id.equipment:
                    transaction.replace(R.id.content_frame, new FragmentEquipment(), "equipment");
                    break;
                case R.id.maintenance:
                    if (truck == null)
                        transaction.replace(R.id.content_frame, new FragmentEquipment(), "equipment");
                    else
                        transaction.replace(R.id.content_frame, new FragmentMaintenance(), "maintenance");
                    break;
            }
        } else {
            if (navigationView.getCheckedItem() != null)
                navigationView.getCheckedItem().setChecked(false);
            if (user == null) {
                balance.setText("Welcome to Payroll");
                transaction.replace(R.id.content_frame, new FragmentWelcome(), "welcome");
            } else {
                if (settlement.getId() == 0 || truck == null) {
                    balance.setText("Getting Started");
                    transaction.replace(R.id.content_frame, new FragmentIntro(), "intro");
                } else {
                    transaction.replace(R.id.content_frame, new FragmentOverview(), "overview");
                }
            }
        }
        if (!isFinishing()) transaction.commit();
        handleGrouping();
    }

    @Override
    public void handleGrouping() {
        if (user == null) {
            balance.setText("Welcome to Payroll");
            navigationView.getMenu().setGroupEnabled(R.id.settlement_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.navigation_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.equipment_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.record_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.backup_group, false);
        } else {
            navigationView.getMenu().setGroupEnabled(R.id.backup_group, true);
            if (settlement.getId() == 0 || truck == null) {
                balance.setText("Getting Started");
                navigationView.getMenu().setGroupEnabled(R.id.settlement_group, truck != null);
                navigationView.getMenu().setGroupEnabled(R.id.equipment_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.navigation_group, false);
                navigationView.getMenu().setGroupEnabled(R.id.record_group, false);
            } else {
                navigationView.getMenu().setGroupEnabled(R.id.settlement_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.navigation_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.equipment_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.record_group, true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) drawerLayout.openDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (navigationView.getCheckedItem().getItemId() == R.id.overview) {
            super.onBackPressed();
        } else {
            if (settlement.getId() != 0)
                handleMenuNavigation(navigationView.getMenu().findItem(R.id.overview), false, false);
            else handleMenuNavigation(null, false, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Utils.getVersion(getApplicationContext()) < dataSnapshot.getValue(int.class)) {
                    handleGrouping();
                    showSnack(getString(R.string.please_update), Snackbar.LENGTH_INDEFINITE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        final SharedPreferences system = getSharedPreferences("system", MODE_PRIVATE);
        if (system.getInt("peeked", 0) < 2) {
            drawerLayout.openDrawer(GravityCompat.START);
            model.executor().execute(() -> {
                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    Log.e("InterruptedException", e.getMessage());
                }
                runOnUiThread(() -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    SharedPreferences.Editor editor = system.edit();
                    editor.putInt("peeked", system.getInt("peeked", 0) + 1);
                    editor.apply();
                });
            });
        }
    }

    private void signIn() {
        if (navigationView.getCheckedItem() != null)
            navigationView.getCheckedItem().setChecked(false);
        //startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    preferences.edit().clear().apply();
                    settlement = new Settlement();
                    signInSheet();
                    showSnack("Sign Out Successful", Snackbar.LENGTH_LONG);
                });
    }

    @Override
    public void showSnack(String message, int length) {
        final Snackbar snackbar = Snackbar.make(content_frame, message, length);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (length == Snackbar.LENGTH_INDEFINITE) {
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.setAction("OKAY", view -> {
                if (message.equals(getString(R.string.please_update))) {
                    Utils.gotoPlayStore(this);
                }
                snackbar.dismiss();
            });
        }
        View v = snackbar.getView();
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        TextView textView = v.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void navigate(int i) {
        handleMenuNavigation(navigationView.getMenu().findItem(i), false, false);
    }

    private String getAbbreviationFromUSState(String state) {
        return STATE_MAP.getOrDefault(state, state);
    }

    private Map<String, String> createStateShorts() {
        Map<String, String> map = new HashMap<>();
        map.put("Alabama", "AL");
        map.put("Alaska", "AK");
        map.put("Alberta", "AB");
        map.put("Arizona", "AZ");
        map.put("Arkansas", "AR");
        map.put("British Columbia", "BC");
        map.put("California", "CA");
        map.put("Colorado", "CO");
        map.put("Connecticut", "CT");
        map.put("Delaware", "DE");
        map.put("District of Columbia", "DC");
        map.put("Florida", "FL");
        map.put("Georgia", "GA");
        map.put("Guam", "GU");
        map.put("Hawaii", "HI");
        map.put("Idaho", "ID");
        map.put("Illinois", "IL");
        map.put("Indiana", "IN");
        map.put("Iowa", "IA");
        map.put("Kansas", "KS");
        map.put("Kentucky", "KY");
        map.put("Louisiana", "LA");
        map.put("Maine", "ME");
        map.put("Manitoba", "MB");
        map.put("Maryland", "MD");
        map.put("Massachusetts", "MA");
        map.put("Michigan", "MI");
        map.put("Minnesota", "MN");
        map.put("Mississippi", "MS");
        map.put("Missouri", "MO");
        map.put("Montana", "MT");
        map.put("Nebraska", "NE");
        map.put("Nevada", "NV");
        map.put("New Brunswick", "NB");
        map.put("New Hampshire", "NH");
        map.put("New Jersey", "NJ");
        map.put("New Mexico", "NM");
        map.put("New York", "NY");
        map.put("Newfoundland", "NF");
        map.put("North Carolina", "NC");
        map.put("North Dakota", "ND");
        map.put("Northwest Territories", "NT");
        map.put("Nova Scotia", "NS");
        map.put("Nunavut", "NU");
        map.put("Ohio", "OH");
        map.put("Oklahoma", "OK");
        map.put("Ontario", "ON");
        map.put("Oregon", "OR");
        map.put("Pennsylvania", "PA");
        map.put("Prince Edward Island", "PE");
        map.put("Puerto Rico", "PR");
        map.put("Quebec", "QC");
        map.put("Rhode Island", "RI");
        map.put("Saskatchewan", "SK");
        map.put("South Carolina", "SC");
        map.put("South Dakota", "SD");
        map.put("Tennessee", "TN");
        map.put("Texas", "TX");
        map.put("Utah", "UT");
        map.put("Vermont", "VT");
        map.put("Virgin Islands", "VI");
        map.put("Virginia", "VA");
        map.put("Washington", "WA");
        map.put("West Virginia", "WV");
        map.put("Wisconsin", "WI");
        map.put("Wyoming", "WY");
        map.put("Yukon Territory", "YT");
        return map;
    }

    class locationCallback extends LocationCallback {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                try {
                    locationUpdated(location);
                } catch (IOException e) {
                    Log.e("onLocationResult()", e.getMessage());
                }
            }
        }
    }
}
