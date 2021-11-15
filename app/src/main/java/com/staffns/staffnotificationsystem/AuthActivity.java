package com.staffns.staffnotificationsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_LOGIN = "login";
    public static final String APP_PREFERENCES_PASS = "pass";
    public static final String APP_PREFERENCES_AUTOLOGIN = "autologin";
    private SharedPreferences mSettings;

    EditText editText_login, editText_pass;
    String login, pass, answer;
    Boolean autologin;
    Authentication authentication;
    HttpURLConnection httpURLConnection;
    Button button_login;
    LinearLayout linearLayout;
    ScrollView scrollView2;

    public void setData(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        editText_login = findViewById(R.id.editText_login);
        editText_pass = findViewById(R.id.editText_password);

        button_login = findViewById(R.id.button_login);
        button_login.setEnabled(true);

        linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setVisibility(LinearLayout.INVISIBLE);

        scrollView2 = findViewById(R.id.scrollView2);
        scrollView2.setVisibility(ScrollView.VISIBLE);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        autologin = mSettings.getBoolean(APP_PREFERENCES_AUTOLOGIN, false);

        if (autologin) {

            login = mSettings.getString(APP_PREFERENCES_LOGIN, "");
            pass = mSettings.getString(APP_PREFERENCES_PASS, "");

            authentication = new Authentication();
            authentication.execute();
        }
    }

    public void onclick(View v) {

        login = editText_login.getText().toString();
        pass = editText_pass.getText().toString();

        authentication = new Authentication();
        authentication.execute();
    }

    private class Authentication extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            button_login.setEnabled(false);
            linearLayout.setVisibility(LinearLayout.VISIBLE);
            scrollView2.setVisibility(ScrollView.INVISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                httpURLConnection = (HttpURLConnection) new URL("http://staffns.com/mob_auth.php?login="+login+"&pass="+pass).openConnection();
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
            } catch (Exception exception) {
                Log.i("doInBackground", "Ошибка: " + exception.getMessage());
            }
            try {
                InputStream is = httpURLConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String bfr_st;
                while ((bfr_st = br.readLine()) != null) {
                    sb.append(bfr_st);
                }
                answer = sb.toString();
                is.close();
                br.close();
            } catch (Exception exception) {
                Log.i("doInBackground", "Ошибка: " + exception.getMessage());
            }
            finally {
                httpURLConnection.disconnect();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if (result.equals("1")) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(APP_PREFERENCES_AUTOLOGIN, true);
                    editor.putString(APP_PREFERENCES_LOGIN, login);
                    editor.putString(APP_PREFERENCES_PASS, pass);
                    editor.apply();
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    linearLayout.setVisibility(LinearLayout.INVISIBLE);
                    scrollView2.setVisibility(ScrollView.VISIBLE);
                    button_login.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception exception) {
                Log.i("onPostExecute", "Ошибка: " + exception.getMessage());
            }
        }
    }
}
