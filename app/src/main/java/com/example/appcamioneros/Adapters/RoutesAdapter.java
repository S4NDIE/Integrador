package com.example.appcamioneros.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcamioneros.Models.Routes;
import com.example.appcamioneros.R;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder>{


    private List<Routes> routes;
    private int layout;
    private OnItemClickListener itemClickListener;

    public RoutesAdapter(List<Routes> routes, int layout, OnItemClickListener itemClickListener) {
        this.routes = routes;
        this.layout = layout;
        this.itemClickListener = itemClickListener;
    }

    public void updateList(List<Routes> mChatDetails) {
        this.routes = mChatDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(this.layout, parent, false);
        ViewHolder vh =  new ViewHolder(v);
        return  vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(this.routes.get(position), this.itemClickListener);
    }

    @Override
    public int getItemCount() {
        return this.routes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView danger_level;
        private TextView origin;
        private TextView destination;
        private TextView distance;
        private TextView speed;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.danger_level = (TextView) itemView.findViewById(R.id.danger_level);
            this.origin = (TextView) itemView.findViewById(R.id.origin);
            this.destination = (TextView) itemView.findViewById(R.id.destination);
            this.distance = (TextView) itemView.findViewById(R.id.distance);
            this.speed = (TextView) itemView.findViewById(R.id.speed);

        }

        public void bind(final Routes route, final OnItemClickListener itemClickListener) {
            name.setText(route.getRoute_name());
            danger_level.setText((" "+route.getDanger_level()));
            origin.setText(route.getOrigin());
            destination.setText(route.getDestination());
            distance.setText(" "+ route.getDistance());
            speed.setText(" "+route.getSpeed());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onItemClick(route, getAdapterPosition());
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(Routes route, int position);
    }
}
