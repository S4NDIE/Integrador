package com.example.appcamioneros.Services;

import com.example.appcamioneros.Models.Routes;
import com.example.appcamioneros.Models.User;
import com.example.appcamioneros.Models.UserLogin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("LogIn")
    Call<User> loginUser(@Body UserLogin user);
//
    @GET("listRoutes/{company}")
    Call<List<Routes>> routesCompany(@Path("company") int company_identifier);

    @GET("listRoutes")
    Call<List<Routes>> routes();

    @GET("driverDetails/{id}")
    Call<User> getProfileUser(@Path("id") int id);
//    @DELETE("user/services/{id}")
//    Call<Void> cancelService(@Path("id") int idService, @Header("Authorization") String authHeader);
}


