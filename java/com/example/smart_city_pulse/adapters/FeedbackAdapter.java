package com.example.smart_city_pulse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.models.Feedback;
import com.example.smart_city_pulse.utils.Utilities;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private List<Feedback> feedbackList;
    private OnFeedbackClickListener clickListener;

    public FeedbackAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        
        holder.serviceTypeText.setText(feedback.getServiceType());
        holder.commentText.setText(feedback.getComment());
        holder.ratingBar.setRating(feedback.getRating());
        holder.dateText.setText(Utilities.formatDate(feedback.getCreatedAt()));

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFeedbackClick(feedback);
            }
        });

        // Set long click listener for officers to manage feedback
        holder.cardView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFeedbackLongClick(feedback);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public void updateFeedbackList(List<Feedback> newFeedbackList) {
        this.feedbackList = newFeedbackList;
        notifyDataSetChanged();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView serviceTypeText;
        TextView commentText;
        RatingBar ratingBar;
        TextView dateText;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.feedbackCard);
            serviceTypeText = itemView.findViewById(R.id.serviceTypeText);
            commentText = itemView.findViewById(R.id.commentText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    public interface OnFeedbackClickListener {
        void onFeedbackClick(Feedback feedback);
        void onFeedbackLongClick(Feedback feedback);
    }

    public void setOnFeedbackClickListener(OnFeedbackClickListener listener) {
        this.clickListener = listener;
    }

    // Helper method to get average rating
    public float getAverageRating() {
        if (feedbackList.isEmpty()) {
            return 0f;
        }
        float total = 0f;
        for (Feedback feedback : feedbackList) {
            total += feedback.getRating();
        }
        return total / feedbackList.size();
    }

    // Helper method to get rating distribution
    public int[] getRatingDistribution() {
        int[] distribution = new int[5]; // 5 possible ratings (1-5)
        for (Feedback feedback : feedbackList) {
            int rating = (int) feedback.getRating();
            if (rating >= 1 && rating <= 5) {
                distribution[rating - 1]++;
            }
        }
        return distribution;
    }

    // Helper method to get feedback count by service type
    public int getFeedbackCountByService(String serviceType) {
        int count = 0;
        for (Feedback feedback : feedbackList) {
            if (feedback.getServiceType().equals(serviceType)) {
                count++;
            }
        }
        return count;
    }
}
