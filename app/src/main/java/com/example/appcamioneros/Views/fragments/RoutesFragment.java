package com.example.appcamioneros.Views.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.appcamioneros.Adapters.RoutesAdapter;
import com.example.appcamioneros.Models.Routes;
import com.example.appcamioneros.R;
import com.example.appcamioneros.Services.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoutesFragment extends Fragment {
    private Retrofit retrofit;
    private ApiService service;
    private RecyclerView routesView;
    private Context context;
    private SwipeRefreshLayout routesRefresh;
    private List<Routes> routes;
    RoutesAdapter routesAdapter;
    private SharedPreferences pref;

    public RoutesFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        context = view.getContext();
        retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.Server))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
        pref = getContext().getSharedPreferences("user", getContext().MODE_PRIVATE);
        routes = new ArrayList<Routes>();
        routesView = view.findViewById(R.id.routesRecycler);
        routesRefresh = view.findViewById(R.id.routesRefresh);
        routesRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRoutes();
            }
        });
        routesAdapter = new RoutesAdapter(routes, R.layout.recycler_view_routes, new RoutesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Routes route, int position) {
                onRoute(route,position);
            }
        });
        routesView.setHasFixedSize(false);
        routesView.setItemAnimator(new DefaultItemAnimator());
        routesView.setLayoutManager(new LinearLayoutManager(context));
        routesView.setAdapter(routesAdapter);
//        Routes route1 = new Routes("1","qpz]tpwkMyBiAmD_CaBw@wC?m@z@MpA???|@GjAVh@Jd@IpAHd@yA\\kCA{A~@a@dBX|Af@|@FxA@lAk@~@oALeBYqAE","Universidad", 1, "Manizales","Universidad",100,50,"1");
//        Routes route2 = new Routes("2","am{]f|vkM\\mB`BBr@Op@Ad@E@X?\\?l@Dt@?X]TKT@LDXVL\\@TSj@t@l@\\ZZ","Cable", 2, "Manizales","Estrella",100,50,"1");
//        routes.add(route1);
//        routes.add(route2);
        Parcelable recyclerViewState;
        recyclerViewState = routesView.getLayoutManager().onSaveInstanceState();
        routesAdapter.updateList(routes);
        routesAdapter.notifyDataSetChanged();
        routesView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        getRoutes();
    }

    private void getRoutes(){
        routesRefresh.setRefreshing(false);
        Call<List<Routes>> response = service.routes();
        response.enqueue(new Callback<List<Routes>>() {
            @Override
            public void onResponse(Call<List<Routes>> call, Response<List<Routes>> response) {
                if(response.isSuccessful()) {
                    routes = response.body();
                    Parcelable recyclerViewState;
                    recyclerViewState = routesView.getLayoutManager().onSaveInstanceState();
                    routesAdapter.updateList(routes);
                    routesAdapter.notifyDataSetChanged();
                    routesView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                } else {
                    Toast.makeText(getContext(),"Problemas en solicitar las rutas", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Routes>> call, Throwable t) {
                Toast.makeText(getContext(),"Algo salio mal intentalo mas tarde", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onRoute(Routes route, int position){
        Toast.makeText(getContext(),"La ruta "+route.getRoute_name()+" se ha cargado en el capa", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("polyline",route.getPolylines_file());
        editor.putInt("danger", route.getDanger_level());
        editor.apply();
        editor.commit();
    }
}
