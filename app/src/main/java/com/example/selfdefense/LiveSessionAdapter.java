package com.example.selfdefense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LiveSessionAdapter extends RecyclerView.Adapter<LiveSessionAdapter.LiveSessionViewHolder> {

    private Context context;
    private List<LiveSession> liveSessionList;
    private OnSessionClickListener onSessionClickListener;

    // Interface for click listener
    public interface OnSessionClickListener {
        void onSessionClick(LiveSession session);
    }

    // Constructor
    public LiveSessionAdapter(Context context, List<LiveSession> liveSessionList, OnSessionClickListener listener) {
        this.context = context;
        this.liveSessionList = liveSessionList;
        this.onSessionClickListener = listener;
    }

    @NonNull
    @Override
    public LiveSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new LiveSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveSessionViewHolder holder, int position) {
        LiveSession session = liveSessionList.get(position);

        // Bind data to the views
        holder.tvSessionTitle.setText(session.getTitle());
        holder.tvSessionHost.setText("Host: " + session.getHostName());
        holder.tvSessionDateTime.setText("Date: " + session.getDateTime());
        holder.tvSessionType.setText(session.getSessionType());

        // Set background color for the session type tag
        if (session.getSessionType().equalsIgnoreCase("online")) {
            holder.tvSessionType.setBackgroundResource(R.drawable.tag_background_online); // Use different drawable for online
        } else {
            holder.tvSessionType.setBackgroundResource(R.drawable.tag_background_offline); // Use different drawable for offline
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> onSessionClickListener.onSessionClick(session));
    }

    @Override
    public int getItemCount() {
        return liveSessionList.size();
    }

    // ViewHolder class
    public static class LiveSessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvSessionHost, tvSessionDateTime, tvSessionType;

        public LiveSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tv_session_title);
            tvSessionHost = itemView.findViewById(R.id.tv_session_host);
            tvSessionDateTime = itemView.findViewById(R.id.tv_session_datetime);
            tvSessionType = itemView.findViewById(R.id.tv_session_type);
        }
    }
}
