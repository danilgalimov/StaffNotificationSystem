package com.staffns.staffnotificationsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 0;
    TextView textView, textView2;
    String token, login, pass;
    Boolean autologin;
    SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_LOGIN = "login";
    public static final String APP_PREFERENCES_PASS = "pass";
    public static final String APP_PREFERENCES_TOKEN = "token";
    public static final String APP_PREFERENCES_AUTOLOGIN = "autologin";
    public static final String APP_PREFERENCES_X = "X";
    public static final String APP_PREFERENCES_Y = "Y";
    Button button;
    double X, Y;
    int id;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        login = mSettings.getString(APP_PREFERENCES_LOGIN, "");
        pass = mSettings.getString(APP_PREFERENCES_PASS, "");
        autologin = mSettings.getBoolean(APP_PREFERENCES_AUTOLOGIN, false);
        X = mSettings.getFloat(APP_PREFERENCES_X, 0);
        Y = mSettings.getFloat(APP_PREFERENCES_Y, 0);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        button = findViewById(R.id.button);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                token = task.getResult();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(APP_PREFERENCES_TOKEN, token);
                editor.apply();
            }
        });
        token = mSettings.getString(APP_PREFERENCES_TOKEN, "");
        getFull_name();
        automaticDataUpdate();

        AuthActivity authActivity = new AuthActivity();
        authActivity.setData(login, pass);
    }

    public void onClickCompleteOrder(View view) {
        NetworkService.getInstance().getJSONApi().completeOrder(login, pass, id).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {}
            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        NetworkService.getInstance().getJSONApi().automaticDataUpdate(login, pass, X, Y, token).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                Order order = response.body();
                textView.setText(order.getPlace() + "\n" + order.getPhone_number() + "\n" + order.getDescription());
                id = order.getId();
                button.setVisibility(Button.VISIBLE);
                button.setEnabled(true);
            }
            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                button.setVisibility(Button.INVISIBLE);
                button.setEnabled(false);
                textView.setText("В данный момент, выполняемых заявок нет.");
                throwable.printStackTrace();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    public void changeUser(View view) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_AUTOLOGIN, false);
        editor.putString(APP_PREFERENCES_LOGIN, "");
        editor.putString(APP_PREFERENCES_PASS, "");
        editor.putString(APP_PREFERENCES_TOKEN, "");
        editor.apply();
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void getFull_name() {
        NetworkService.getInstance().getJSONApi().getFull_name(login, pass).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                Order order = response.body();
                if (order != null) {
                    textView2.setText("Здравствуйте " + order.getFull_name());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void automaticDataUpdate() {
        Thread thread = new Thread(() -> {
            while (true) {
                NetworkService.getInstance().getJSONApi().automaticDataUpdate(login, pass, X, Y, token).enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                        Order order = response.body();
                        textView.setText(order.getPlace() + "\n" + order.getPhone_number() + "\n" + order.getDescription());
                        id = order.getId();
                        button.setVisibility(Button.VISIBLE);
                        button.setEnabled(true);

                    }

                    @Override
                    public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                        button.setVisibility(Button.INVISIBLE);
                        button.setEnabled(false);
                        textView.setText("В данный момент, выполняемых заявок нет.");
                        throwable.printStackTrace();
                    }
                });
                try {
                    Thread.sleep(15000);
                } catch (Exception ignored) {}
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void dataUpdate(View view) {
        NetworkService.getInstance().getJSONApi().automaticDataUpdate(login, pass, X, Y, token).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                Order order = response.body();
                textView.setText(order.getPlace() + "\n" + order.getPhone_number() + "\n" + order.getDescription());
                id = order.getId();
                button.setVisibility(Button.VISIBLE);
                button.setEnabled(true);
            }
            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable throwable) {
                button.setVisibility(Button.INVISIBLE);
                button.setEnabled(false);
                textView.setText("В данный момент, выполняемых заявок нет.");
                throwable.printStackTrace();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            int permissionStatus = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                locationUpdate(location);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {
            int permissionStatus = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                locationUpdate(locationManager.getLastKnownLocation(provider));
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private void locationUpdate(Location location) {
        if (location == null) return;
        X = formatLocationX(location);
        Y = formatLocationY(location);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putFloat(APP_PREFERENCES_X, (float) X);
        editor.putFloat(APP_PREFERENCES_Y, (float) Y);
        editor.apply();
    }

    private double formatLocationX(Location location) {
        return location.getLatitude();
    }

    private double formatLocationY(Location location) {
        return location.getLongitude();
    }

    public void locationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public void buttonExit(View view) {
        finishAffinity();
    }
}