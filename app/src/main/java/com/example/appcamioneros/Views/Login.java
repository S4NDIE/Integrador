package com.example.appcamioneros.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.appcamioneros.Models.MessagesError;
import com.example.appcamioneros.Models.User;
import com.example.appcamioneros.Models.UserLogin;
import com.example.appcamioneros.R;
import com.example.appcamioneros.Services.ApiService;
import com.google.gson.Gson;

import java.security.Policy;
import java.sql.SQLOutput;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private EditText txtUserName, txtPassword;
    private ProgressBar progressBar;
    private ApiService service;
    private Retrofit retrofit;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.buttonLogin);
        btnRegister = findViewById(R.id.buttonRegister);
        txtPassword = findViewById(R.id.editTextPassword);
        txtUserName = findViewById(R.id.editTextEmail);
        progressBar = findViewById(R.id.progressBar);
        pref = getSharedPreferences("user",Login.MODE_PRIVATE);
        retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.Server))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogin();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegister();
            }
        });
    }

    private void onLogin() {
        progressBar.setVisibility(View.VISIBLE);
        if(!txtUserName.getText().toString().isEmpty() && !txtPassword.getText().toString().isEmpty()) {
            UserLogin user = new UserLogin(txtUserName.getText().toString(), txtPassword.getText().toString());
            Call<User> response = service.loginUser(user);
            response.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    System.out.println(response);
                    if(response.isSuccessful()){
                        Toast.makeText(Login.this,response.body().getEmail(), Toast.LENGTH_LONG).show();
                        User user = response.body();
                        saveUser(user.getId());
                        Intent intentRegister = new Intent(Login.this, MainActivity.class);
                        startActivity(intentRegister);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(Login.this, "Error en el inicio de sesion", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(Login.this, "Error en el inicio de sesion", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            Toast.makeText(Login.this, "Todos los campos son requeridos", Toast.LENGTH_LONG).show();
        }

    }

    private void saveUser(int id) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("id", id);
        editor.putString("polyline", "");
        editor.putInt("danger",0);
        editor.commit();
    }

    private void onRegister() {
        Intent intenRegister = new Intent(this, Register.class);
        startActivity(intenRegister);
    }
}
