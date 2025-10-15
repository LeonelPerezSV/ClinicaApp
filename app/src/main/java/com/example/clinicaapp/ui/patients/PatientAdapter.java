package com.example.clinicaapp.ui.patients;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.Patient;
import java.util.*;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {

    public interface OnPatientClick { void onClick(Patient item); }

    private final OnPatientClick listener;
    private final List<Patient> data = new ArrayList<>();

    public PatientAdapter(OnPatientClick listener) { this.listener = listener; }

    public void submit(List<Patient> items) {
        data.clear(); if (items != null) data.addAll(items); notifyDataSetChanged();
    }

    public Patient getAt(int pos) { return data.get(pos); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Patient p = data.get(pos);
        ((TextView) h.itemView.findViewById(android.R.id.text1)).setText(p.getFirstName() + " " + p.getLastName());
        ((TextView) h.itemView.findViewById(android.R.id.text2)).setText(p.getEmail() + " · " + p.getPhone());
        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
}
