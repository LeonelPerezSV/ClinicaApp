package com.example.clinicaapp.ui.prescriptions;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.Prescription;
import java.util.*;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.VH> {

    public interface OnPrescriptionClick { void onClick(Prescription item); }
    private final OnPrescriptionClick listener;
    private final List<Prescription> data = new ArrayList<>();

    public PrescriptionAdapter(OnPrescriptionClick listener) { this.listener = listener; }

    public void submit(List<Prescription> items) { data.clear(); if (items != null) data.addAll(items); notifyDataSetChanged(); }
    public Prescription getAt(int pos) { return data.get(pos); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Prescription p = data.get(pos);
        ((TextView) h.itemView.findViewById(android.R.id.text1)).setText("RX #" + p.getId() + " · " + p.getMedication());
        ((TextView) h.itemView.findViewById(android.R.id.text2)).setText("Paciente " + p.getPatientId() + " · Doctor " + p.getDoctorId() + " · " + p.getDate());
        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
}
