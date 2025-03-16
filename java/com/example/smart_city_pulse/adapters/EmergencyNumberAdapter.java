package com.example.smart_city_pulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.models.EmergencyContact;

import java.util.List;

public class EmergencyNumberAdapter extends RecyclerView.Adapter<EmergencyNumberAdapter.EmergencyNumberViewHolder> {
    private List<EmergencyContact> contacts;
    private OnCallClickListener callClickListener;

    public EmergencyNumberAdapter(List<EmergencyContact> contacts, OnCallClickListener listener) {
        this.contacts = contacts;
        this.callClickListener = listener;
    }

    @NonNull
    @Override
    public EmergencyNumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_number, parent, false);
        return new EmergencyNumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyNumberViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        
        holder.nameText.setText(contact.getName());
        holder.numberText.setText(contact.getNumber());
        holder.descriptionText.setText(contact.getDescription());
        holder.iconImage.setImageResource(contact.getIconResourceId());

        holder.callButton.setOnClickListener(v -> {
            if (callClickListener != null) {
                callClickListener.onCallClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class EmergencyNumberViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView iconImage;
        TextView nameText;
        TextView numberText;
        TextView descriptionText;
        ImageView callButton;

        public EmergencyNumberViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.emergencyCard);
            iconImage = itemView.findViewById(R.id.emergencyIcon);
            nameText = itemView.findViewById(R.id.emergencyName);
            numberText = itemView.findViewById(R.id.emergencyNumber);
            descriptionText = itemView.findViewById(R.id.emergencyDescription);
            callButton = itemView.findViewById(R.id.callButton);
        }
    }

    public interface OnCallClickListener {
        void onCallClick(EmergencyContact contact);
    }
}
