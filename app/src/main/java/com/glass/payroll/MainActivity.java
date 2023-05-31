package com.glass.payroll;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

    //testing
    private static String location = "";
    private final int RC_SIGN_IN = 9002;
    private final Map<String, String> STATE_MAP = new HashMap<>();
    private final locationCallback locationCallback = new locationCallback();
    private FusedLocationProviderClient mFusedLocationClient;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView profileView;
    private TextView profileName, balance, date;
    private FragmentManager fragmentManager;
    private SharedPreferences preferences;
    private FrameLayout content_frame;
    private boolean update = false;

    static Truck truck;

    static Trailer trailer;

    private MainViewModel model;


    @Override
    public void newSettlement(final Settlement settlement, final boolean transfer) {
        if (transfer) {
            settlement.setPayout(MainActivity.this.settlement.getPayout());
            settlement.setFixed(MainActivity.this.settlement.getFixed());
        }
        model.add(Utils.calculate(settlement));
        FragmentOverview fragmentOverview = (FragmentOverview) fragmentManager.findFragmentByTag("overview");
        if (fragmentOverview == null)
            handleMenuNavigation(navigationView.getMenu().findItem(R.id.overview), false, false);
        showSnack("New settlement created", Snackbar.LENGTH_LONG);
    }

    void handleSettlementData() {
        if (settlement.getId() == 0) {
            handleMenuNavigation(null, false, false);
        } else {
            handleMenuNavigation(navigationView.getMenu().getItem(1), false, false);
        }

    }

    private void signInSheet() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            model.setUserId(user.getUid());
            model.settlementLiveData().observe(this, settlement -> {
                MainActivity.this.settlement = settlement;
                balance.setText("Bal: $" + settlement.getBalance());
            });
            if (preferences.getBoolean("migrate", true)) {
                returnSettlement();
            } else {
                handleSettlementData();
            }
        } else {
            handleMenuNavigation(null, false, false);
        }
        updateUserInfoAndDisplay(user);
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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String dataString = response.body().string();
                        final JSONObject data = new JSONObject(dataString);
                        runOnUiThread(() -> {
                            try {
                                preferences.edit().putBoolean("migrate", false).apply();
                                if (data.getBoolean("available")) {
                                    ArrayList<Settlement> x = Utils.returnSettlementArray(data.getJSONArray("settlement").toString());
                                    model.add(x);
                                    signInSheet();
                                }
                            } catch (JSONException e) {
                                Log.e("onResponse()", e.getMessage());
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("JSONException", e.getMessage());
                    } finally {
                        response.close();
                    }
                } else {
                    runOnUiThread(() -> showSnack("Offline Mode", Snackbar.LENGTH_INDEFINITE));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {//location permission
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) requestLocationUpdates();
            }
        }
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
                        if (address.getCountryCode().equals("US"))
                            MainActivity.location = city.trim() + ", " + getAbbreviationFromUSState(state);
                        else
                            MainActivity.location = address.getLocality() + ", " + address.getCountryCode();
                    }
                }
            }
        }
    }

    private void requestLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, getMainLooper());
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
        profileView = headerLayout.findViewById(R.id.profile_photo);
        profileName = headerLayout.findViewById(R.id.profile_name);
        profileView.setOnClickListener(view -> {
            vibrate();
            if (mAuth.getCurrentUser() != null) {
                preferences.edit().putString(user.getUid(), null).apply();
                getSharedPreferences("stamps", MODE_PRIVATE).edit().clear().apply();
                signOut();
            } else signIn();
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            vibrate();
            handleMenuNavigation(menuItem, true, true);
            return true;
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_auth)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        fragmentManager = getSupportFragmentManager();
        signInSheet();
        STATE_MAP.putAll(createStateShorts());
        if (locationGranted()) requestLocationUpdates();
        date.setText(Utils.toShortDateSpelled(System.currentTimeMillis()));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void handleMenuNavigation(MenuItem menuItem, boolean close, boolean physical) {
        if (physical) vibrate();
        if (close && drawerLayout.isDrawerOpen(Gravity.LEFT)) drawerLayout.closeDrawers();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (menuItem != null) {
            if (menuItem.getItemId() != R.id.new_settlement) menuItem.setChecked(true);
            switch (menuItem.getItemId()) {
                case R.id.new_settlement:
                    new NewSettlementFragment().show(fragmentManager, "newSettlement");
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
            }
        } else {
            if (navigationView.getCheckedItem() != null)
                navigationView.getCheckedItem().setChecked(false);
            if (user == null) {
                balance.setText("Welcome to Payroll");
                transaction.replace(R.id.content_frame, new FragmentWelcome(), "welcome");
            } else {
                if (settlement.getId() == 0) {
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

    private void handleGrouping() {
        if (user == null) {
            balance.setText("Welcome to Payroll");
            navigationView.getMenu().setGroupEnabled(R.id.settlement_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.navigation_group, false);
            navigationView.getMenu().setGroupEnabled(R.id.record_group, false);
        } else {
            if (settlement.getId() == 0) {
                balance.setText("Getting Started");
                if (!update) navigationView.getMenu().setGroupEnabled(R.id.settlement_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.navigation_group, false);
                navigationView.getMenu().setGroupEnabled(R.id.record_group, false);
            } else {
                if (!update) navigationView.getMenu().setGroupEnabled(R.id.settlement_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.navigation_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.record_group, true);
                navigationView.getMenu().setGroupEnabled(R.id.backup_group, true);
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
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if (navigationView.getCheckedItem() == null || user == null) {
            super.onBackPressed();
            return;
        }
        if (navigationView.getCheckedItem().getItemId() == R.id.overview) {
            super.onBackPressed();
            return;
        }
        if (settlement.getId() != 0)
            handleMenuNavigation(navigationView.getMenu().getItem(1), false, false);
        else super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Utils.getVersion(getApplicationContext()) < dataSnapshot.getValue(int.class)) {
                    update = true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(Objects.requireNonNull(task.getResult(ApiException.class)));
            } catch (ApiException e) {
                Log.e("onActivityResult()", e.getMessage());
            }
        }
    }

    private void signIn() {
        if (navigationView.getCheckedItem() != null)
            navigationView.getCheckedItem().setChecked(false);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void signOut() {
        preferences.edit().clear().apply();
        user = null;
        settlement = new Settlement();
        handleMenuNavigation(null, false, false);
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> signInSheet());
        showSnack("Sign Out Successful", Snackbar.LENGTH_LONG);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                showSnack("Sign In Successful", Snackbar.LENGTH_LONG);
                signInSheet();
            } else {
                Log.e("firebaseAuthWithGoogle()", task.getException().toString());
            }
        });
    }

    private void updateUserInfoAndDisplay(FirebaseUser user) {
        if (user != null) {
            profileName.setText(user.getDisplayName());
            Glide.with(MainActivity.this).load(Objects.requireNonNull(user.getPhotoUrl()).toString().replace("96", "400")).circleCrop().into(profileView);
        } else {
            profileName.setText(getString(R.string.log_in_or_out_text));
            profileView.setImageResource(R.drawable.fingerprint);
        }
    }

    @Override
    public String returnLocation() {
        if (!locationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            location = "";
            return "";
        } else return location;
    }

    @Override
    public boolean locationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(5);
    }

    @Override
    public void newLoad(Load load, int index) {
        NewLoadFragment fi = new NewLoadFragment();
        if (load != null) {
            Bundle bundle = new Bundle();
            bundle.putString("load", gson.toJson(load));
            bundle.putInt("index", index);
            fi.setArguments(bundle);
        }
        fi.show(fragmentManager, "newLoad");
    }

    @Override
    public void newFuel(Fuel fuel, int index) {
        NewFuelFragment fi = new NewFuelFragment();
        if (fuel != null) {
            Bundle bundle = new Bundle();
            bundle.putString("fuel", gson.toJson(fuel));
            bundle.putInt("index", index);
            fi.setArguments(bundle);
        }
        fi.show(fragmentManager, "newFuel");
    }

    @Override
    public void newFixed(Cost cost, int index) {
        NewFixedFragment fi = new NewFixedFragment();
        if (cost != null) {
            Bundle bundle = new Bundle();
            bundle.putString("cost", gson.toJson(cost));
            bundle.putInt("index", index);
            fi.setArguments(bundle);
        }
        fi.show(fragmentManager, "newFixed");
    }

    @Override
    public void newMisc(Cost cost, int index) {
        NewMiscFragment fi = new NewMiscFragment();
        if (cost != null) {
            Bundle bundle = new Bundle();
            bundle.putString("cost", gson.toJson(cost));
            bundle.putInt("index", index);
            fi.setArguments(bundle);
        }
        fi.show(fragmentManager, "newMisc");
    }

    @Override
    public void navigate(int i) {
        handleMenuNavigation(navigationView.getMenu().getItem(i), false, false);
    }

    @Override
    public void group() {
        handleGrouping();
    }

    @Override
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void updateOdometer() {
        FragmentOdometer odometerFragment = new FragmentOdometer();
        odometerFragment.show(fragmentManager, "odometer");
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
