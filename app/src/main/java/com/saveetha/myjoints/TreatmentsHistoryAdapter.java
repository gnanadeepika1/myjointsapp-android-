package com.saveetha.myjoints;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TreatmentsHistoryAdapter
        extends RecyclerView.Adapter<TreatmentsHistoryAdapter.ViewHolder> {

    public interface OnDeleteClick {
        void onDelete(TreatmentRecord record);
    }

    private final List<TreatmentRecord> list;
    private final OnDeleteClick deleteClick;

    public TreatmentsHistoryAdapter(
            List<TreatmentRecord> list,
            OnDeleteClick deleteClick
    ) {
        this.list = list;
        this.deleteClick = deleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int position) {

        TreatmentRecord r = list.get(position);

        h.tvMedicine.setText("Medicine: " + r.getMedicationName());
        h.tvDose.setText("Dose: " + r.getDose());
        h.tvRoute.setText("Route: " + r.getRoute());
        h.tvFrequency.setText(
                "Frequency: " + r.getFrequencyNumber()
                        + " (" + r.getFrequencyText() + ")"
        );
        h.tvDuration.setText("Duration: " + r.getDuration());

        // Patient ID is intentionally NOT displayed

        h.btnDelete.setOnClickListener(v ->
                deleteClick.onDelete(r)
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMedicine, tvDose, tvRoute,
                tvFrequency, tvDuration;
        ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicine = itemView.findViewById(R.id.tvMedicine);
            tvDose = itemView.findViewById(R.id.tvDose);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnDelete = itemView.findViewById(R.id.btnDeleteTreatment);
        }
    }
}
