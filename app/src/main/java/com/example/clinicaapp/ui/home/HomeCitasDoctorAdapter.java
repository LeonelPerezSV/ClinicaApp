package com.example.clinicaapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.Appointment;
import java.util.List;

public class HomeCitasDoctorAdapter extends RecyclerView.Adapter<HomeCitasDoctorAdapter.ViewHolder> {

    private final List<Appointment> citas;

    public HomeCitasDoctorAdapter(List<Appointment> citas) {
        this.citas = citas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_doctor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment c = citas.get(position);
        holder.tvPaciente.setText("👤 " + c.getPatientName());
        holder.tvHora.setText("🕒 " + c.getTime());
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaciente, tvHora;
        public ViewHolder(View itemView) {
            super(itemView);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvHora = itemView.findViewById(R.id.tvHora);
        }
    }
}
