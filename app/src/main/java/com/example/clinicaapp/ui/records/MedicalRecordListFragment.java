package com.example.clinicaapp.ui.records;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.*;
import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.MedicalRecord;
import com.example.clinicaapp.databinding.FragmentMedicalRecordListBinding;
import com.example.clinicaapp.viewmodel.MedicalRecordViewModel;
import java.util.List;

public class MedicalRecordListFragment extends Fragment implements MedicalRecordAdapter.OnRecordClick {

    private FragmentMedicalRecordListBinding binding;
    private MedicalRecordViewModel viewModel;
    private MedicalRecordAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMedicalRecordListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MedicalRecordViewModel.class);
        adapter = new MedicalRecordAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        //Se llama al método correcto getAllRecords()
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), (List<MedicalRecord> list) -> {
            adapter.submit(list);
            binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Ocultar botón "Agregar expediente" (solo visualización y edición desde la lista de pacientes)
        binding.fabAdd.setVisibility(View.GONE);

        binding.recycler.setOnLongClickListener(v -> {
            Toast.makeText(getContext(),
                    "El expediente no puede eliminarse directamente, depende del paciente.",
                    Toast.LENGTH_LONG).show();
            return true;
        });
    }

    //Navega al formulario del expediente usando el NavController de Jetpack.
     //Le pasa el ID del paciente, no el del expediente, para seguir la nueva lógica.
    private void openRecordForPatient(int patientId) {
        Bundle args = new Bundle();
        args.putInt("patient_id", patientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.medicalRecordFormFragment, args);
    }

    //Al hacer clic en un expediente de la lista, se navega al
     //formulario pasándole el ID del paciente al que pertenece.
    @Override
    public void onClick(MedicalRecord item) {
        openRecordForPatient(item.getPatientId());
    }
}
