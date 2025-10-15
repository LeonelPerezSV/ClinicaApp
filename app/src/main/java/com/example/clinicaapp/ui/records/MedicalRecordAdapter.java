package com.example.clinicaapp.ui.records;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.MedicalRecord;
import java.util.*;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.VH> {

    public interface OnRecordClick { void onClick(MedicalRecord item); }
    private final OnRecordClick listener;
    private final List<MedicalRecord> data = new ArrayList<>();

    public MedicalRecordAdapter(OnRecordClick listener) { this.listener = listener; }

    public void submit(List<MedicalRecord> items) { data.clear(); if (items != null) data.addAll(items); notifyDataSetChanged(); }
    public MedicalRecord getAt(int pos) { return data.get(pos); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        MedicalRecord r = data.get(pos);
        ((TextView) h.itemView.findViewById(android.R.id.text1)).setText("Paciente " + r.getPatientId() + " · " + r.getSummary());
        ((TextView) h.itemView.findViewById(android.R.id.text2)).setText("Alergias: " + (r.getAllergies() == null ? "-" : r.getAllergies()));
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
}
