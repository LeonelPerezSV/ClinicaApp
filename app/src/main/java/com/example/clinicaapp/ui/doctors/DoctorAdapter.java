package com.example.clinicaapp.ui.doctors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.Doctor;
import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.VH> {

    public interface OnDoctorClick { void onClick(Doctor item); }

    private final OnDoctorClick listener;
    private final List<Doctor> data = new ArrayList<>();

    public DoctorAdapter(OnDoctorClick listener) { this.listener = listener; }

    public void submit(List<Doctor> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public Doctor getAt(int pos) { return data.get(pos); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Doctor d = data.get(pos);

        ((TextView) h.itemView.findViewById(android.R.id.text1))
                .setText(d.getName());

        ((TextView) h.itemView.findViewById(android.R.id.text2))
                .setText((d.getSpecialty() != null ? d.getSpecialty() : "-") +
                        " · " + (d.getEmail() != null ? d.getEmail() : "-"));

        h.itemView.setOnClickListener(v -> listener.onClick(d));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) { super(itemView); }
    }
}
