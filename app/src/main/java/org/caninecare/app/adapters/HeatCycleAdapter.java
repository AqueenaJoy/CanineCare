package org.caninecare.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.caninecare.app.R;
import org.caninecare.app.models.HeatCycle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HeatCycleAdapter extends RecyclerView.Adapter<HeatCycleAdapter.ViewHolder> {

    private List<HeatCycle> cycles;

    public HeatCycleAdapter(List<HeatCycle> cycles) {
        this.cycles = cycles;
    }

    public void updateData(List<HeatCycle> newCycles) {
        this.cycles = newCycles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heat_cycle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeatCycle cycle = cycles.get(position);
        
        holder.tvPredictionType.setText(cycle.getPredictionType());
        holder.tvPredictionValue.setText(String.format(Locale.getDefault(), 
            "%.1f %s", cycle.getPredictionValue(), cycle.getPredictionUnit()));
        holder.tvEstimatedDate.setText("Est. Date: " + formatDate(cycle.getEstimatedDate()));
        holder.tvFertilityStatus.setText(cycle.getFertilityStatus());
        holder.tvCreatedAt.setText("Recorded: " + formatDate(cycle.getCreatedAt()));
        
        // Set color based on alert level
        int color = getColorForAlertLevel(cycle.getAlertLevel());
        holder.cardView.setCardBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return cycles.size();
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "N/A";
        }
        
        try {
            // Try parsing ISO format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            try {
                // Try parsing date only format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException ex) {
                return dateStr;
            }
        }
    }

    private int getColorForAlertLevel(String alertLevel) {
        if (alertLevel == null) {
            return Color.parseColor("#E3F2FD"); // Light blue
        }
        
        switch (alertLevel.toLowerCase()) {
            case "high":
                return Color.parseColor("#FFEBEE"); // Light red
            case "medium":
                return Color.parseColor("#FFF3E0"); // Light orange
            case "low":
            default:
                return Color.parseColor("#E8F5E9"); // Light green
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvPredictionType;
        TextView tvPredictionValue;
        TextView tvEstimatedDate;
        TextView tvFertilityStatus;
        TextView tvCreatedAt;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvPredictionType = itemView.findViewById(R.id.tvPredictionType);
            tvPredictionValue = itemView.findViewById(R.id.tvPredictionValue);
            tvEstimatedDate = itemView.findViewById(R.id.tvEstimatedDate);
            tvFertilityStatus = itemView.findViewById(R.id.tvFertilityStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}
