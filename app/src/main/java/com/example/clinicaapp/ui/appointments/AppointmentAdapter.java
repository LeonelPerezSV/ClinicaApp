package com.example.clinicaapp.ui.appointments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.data.entities.Appointment;
import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnAppointmentClick { void onClick(Appointment item); }
    private final OnAppointmentClick listener;
    private final List<Appointment> data = new ArrayList<>();

    public AppointmentAdapter(OnAppointmentClick listener) { this.listener = listener; }

    public void submit(List<Appointment> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public Appointment getAt(int pos) { return data.get(pos); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Appointment a = data.get(pos);
        ((TextView) h.itemView.findViewById(android.R.id.text1)).setText("Cita " + a.getDate() + " " + a.getTime() + " · " + a.getStatus());
        ((TextView) h.itemView.findViewById(android.R.id.text2)).setText("Paciente " + a.getPatientId() + " · Doctor " + a.getDoctorId());
        h.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder { VH(@NonNull View itemView) { super(itemView); } }
}
