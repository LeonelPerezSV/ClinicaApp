package com.example.clinicaapp.ui.prescriptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.Prescription;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

    public interface OnPrescriptionClick {
        void onClick(Prescription item);
    }

    private final OnPrescriptionClick listener;
    private List<Prescription> items = new ArrayList<>();

    public PrescriptionAdapter(OnPrescriptionClick listener) {
        this.listener = listener;
    }

    public void submit(List<Prescription> list) {
        items = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Prescription getAt(int pos) {
        return items.get(pos);
    }

    @NonNull
    @Override
    public PrescriptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionAdapter.ViewHolder h, int position) {
        Prescription p = items.get(position);

        TextView title = h.itemView.findViewById(android.R.id.text1);
        TextView subtitle = h.itemView.findViewById(android.R.id.text2);

        title.setText("Receta #" + p.getId());
        subtitle.setText("Paciente ID: " + p.getPatientId() + " · Fecha: " + p.getDate());

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
