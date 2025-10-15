package com.example.clinicaapp.ui.records;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMedicalRecordListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MedicalRecordViewModel.class);
        adapter = new MedicalRecordAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        viewModel.getAll().observe(getViewLifecycleOwner(), (List<MedicalRecord> list) -> {
            adapter.submit(list);
            binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        binding.fabAdd.setOnClickListener(v -> openForm(-1));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder v1, @NonNull RecyclerView.ViewHolder v2) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                MedicalRecord item = adapter.getAt(vh.getAdapterPosition());
                viewModel.deleteById(item.getId());
                Toast.makeText(getContext(), "Historial eliminado", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.recycler);
    }

    private void openForm(int id) {
        Fragment f = MedicalRecordFormFragment.newInstance(id);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, f)
                .addToBackStack(null)
                .commit();
    }

    @Override public void onClick(MedicalRecord item) { openForm(item.getId()); }
}
