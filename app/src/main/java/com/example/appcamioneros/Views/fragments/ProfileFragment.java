package com.example.appcamioneros.Views.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appcamioneros.Adapters.RoutesAdapter;
import com.example.appcamioneros.Models.Routes;
import com.example.appcamioneros.Models.User;
import com.example.appcamioneros.R;
import com.example.appcamioneros.Services.ApiService;
import com.example.appcamioneros.Views.Login;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.style.light.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private Retrofit retrofit;
    private ApiService service;
    private Context context;
    private TextView firstName, lastName, email, phone;
    private Button buttonLogout;
    private User user;
    private SharedPreferences pref;

    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.Server))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
        pref = getContext().getSharedPreferences("user",getContext().MODE_PRIVATE);
        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogout();
            }
        });
        getUser();
        return view;
    }

    private void getUser(){
        Call<User> response = service.getProfileUser(pref.getInt("id",1));
        response.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    user = response.body();
                    firstName.setText(user.getFirst_name());
                    lastName.setText(user.getLast_name());
                    email.setText(user.getEmail());
                    phone.setText(" "+ user.getDocument());
                } else {
                    Toast.makeText(getContext(), "Error al obtener el perfil", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void onLogout() {
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Intent intent = new Intent(getContext(), Login.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
