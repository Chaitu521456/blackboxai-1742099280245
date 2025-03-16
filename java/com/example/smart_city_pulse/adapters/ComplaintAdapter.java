package com.example.smart_city_pulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.models.Complaint;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private List<Complaint> complaints;

    public ComplaintAdapter(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);
        holder.titleText.setText(complaint.getTitle());
        holder.descriptionText.setText(complaint.getDescription());
        holder.locationText.setText(complaint.getLocation());
        holder.statusText.setText(complaint.getStatus());
        holder.dateText.setText(complaint.getCreatedAt());

        // Set status color
        int statusColor;
        switch (complaint.getStatus()) {
            case Complaint.STATUS_IN_PROGRESS:
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case Complaint.STATUS_RESOLVED:
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                break;
            case Complaint.STATUS_REJECTED:
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
                break;
        }
        holder.statusText.setTextColor(statusColor);

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (onComplaintClickListener != null) {
                onComplaintClickListener.onComplaintClick(complaint);
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public void updateComplaints(List<Complaint> newComplaints) {
        this.complaints = newComplaints;
        notifyDataSetChanged();
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleText;
        TextView descriptionText;
        TextView locationText;
        TextView statusText;
        TextView dateText;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.complaintCard);
            titleText = itemView.findViewById(R.id.complaintTitle);
            descriptionText = itemView.findViewById(R.id.complaintDescription);
            locationText = itemView.findViewById(R.id.complaintLocation);
            statusText = itemView.findViewById(R.id.complaintStatus);
            dateText = itemView.findViewById(R.id.complaintDate);
        }
    }

    // Click listener interface
    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private OnComplaintClickListener onComplaintClickListener;

    public void setOnComplaintClickListener(OnComplaintClickListener listener) {
        this.onComplaintClickListener = listener;
    }
}
