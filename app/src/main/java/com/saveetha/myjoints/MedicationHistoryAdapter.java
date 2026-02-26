package com.saveetha.myjoints;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationHistoryAdapter
        extends RecyclerView.Adapter<MedicationHistoryAdapter.MedViewHolder> {

    // 🔹 Callback interface for delete
    public interface OnDeleteClickListener {
        void onDelete(MedicationHistoryItem item);
    }

    private final List<MedicationHistoryItem> items;
    private final OnDeleteClickListener deleteListener;

    // 🔹 Updated constructor
    public MedicationHistoryAdapter(
            List<MedicationHistoryItem> items,
            OnDeleteClickListener deleteListener
    ) {
        this.items = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_medication_history,
                        parent,
                        false
                );
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MedViewHolder holder,
            int position
    ) {
        MedicationHistoryItem item = items.get(position);

        // ✅ Existing bindings (unchanged)
        holder.tvEmoji.setText("💊");
        holder.tvHeaderTitle.setText("Medications");
        holder.tvMedName.setText(item.getName());
        holder.tvDose.setText("Dose: " + item.getDose());
        holder.tvPeriod.setText("Period: " + item.getPeriod());

        // ✅ Delete button click
        holder.btnDeleteMedication.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // =====================================================
    // VIEW HOLDER
    // =====================================================
    static class MedViewHolder extends RecyclerView.ViewHolder {

        TextView tvEmoji, tvHeaderTitle, tvMedName, tvDose, tvPeriod;
        ImageView btnDeleteMedication;

        MedViewHolder(@NonNull View itemView) {
            super(itemView);

            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDose = itemView.findViewById(R.id.tvDose);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);

            // 🔹 Delete icon
            btnDeleteMedication =
                    itemView.findViewById(R.id.btnDeleteMedication);
        }
    }
}
