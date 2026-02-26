package com.saveetha.myjoints;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvestigationHistoryAdapter
        extends RecyclerView.Adapter<InvestigationHistoryAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(InvestigationItem item);
    }

    private final Context context;
    private final List<InvestigationItem> data;
    private final OnDeleteClick onDelete;

    public InvestigationHistoryAdapter(
            Context context,
            List<InvestigationItem> data,
            OnDeleteClick onDelete
    ) {
        this.context = context;
        this.data = data;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context)
                .inflate(R.layout.item_investigation_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        InvestigationItem item = data.get(pos);
        h.tvTitle.setText(item.getTitle());
        h.llDetails.removeAllViews();

        for (String line : item.getDetails()) {
            TextView tv = new TextView(context);
            tv.setText(line);
            tv.setTextSize(15f);
            h.llDetails.addView(tv);
        }

        h.btnDelete.setOnClickListener(v -> onDelete.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        LinearLayout llDetails;
        ImageView btnDelete;

        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvInvestigationTitle);
            llDetails = v.findViewById(R.id.llDetails);
            btnDelete = v.findViewById(R.id.btnDeleteInvestigation);
        }
    }
}
