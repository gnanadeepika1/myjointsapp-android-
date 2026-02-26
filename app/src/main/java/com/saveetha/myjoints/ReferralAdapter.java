package com.saveetha.myjoints;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReferralAdapter
        extends RecyclerView.Adapter<ReferralAdapter.ReferralViewHolder> {

    public interface OnDeleteClick {
        void onDelete(ReferralItem item);
    }

    private final List<ReferralItem> data;
    private final OnDeleteClick deleteClick;

    public ReferralAdapter(List<ReferralItem> data, OnDeleteClick deleteClick) {
        this.data = data;
        this.deleteClick = deleteClick;
    }

    @NonNull
    @Override
    public ReferralViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_referral_card, parent, false);
        return new ReferralViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReferralViewHolder holder, int position) {

        ReferralItem item = data.get(position);

        holder.tvMessage.setText(item.getMessage());
        holder.tvPatientId.setText("Patient ID: " + item.getPatientId());

        holder.btnDelete.setOnClickListener(v ->
                deleteClick.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ReferralViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage, tvPatientId;
        ImageView btnDelete;

        ReferralViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvReferralMessage);
            tvPatientId = itemView.findViewById(R.id.tvReferralPatientId);
            btnDelete = itemView.findViewById(R.id.btnDeleteReferral);
        }
    }
}
