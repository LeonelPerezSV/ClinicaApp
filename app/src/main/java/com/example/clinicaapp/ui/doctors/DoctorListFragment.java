package com.example.clinicaapp.ui.doctors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinicaapp.R;
import com.example.clinicaapp.data.entities.Doctor;
import com.example.clinicaapp.databinding.FragmentDoctorListBinding;
import com.example.clinicaapp.viewmodel.DoctorViewModel;

import java.util.List;

public class DoctorListFragment extends Fragment implements DoctorAdapter.OnDoctorClick {

    private FragmentDoctorListBinding binding;
    private DoctorViewModel viewModel;
    private DoctorAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDoctorListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        adapter = new DoctorAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        // Observa la lista
        viewModel.getAll().observe(getViewLifecycleOwner(), (List<Doctor> list) -> {
            adapter.submit(list);
            binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // FAB -> Form create
        binding.fabAdd.setOnClickListener(v -> openForm(-1));

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder tgt) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                Doctor item = adapter.getAt(vh.getAdapterPosition());
                viewModel.deleteById(item.getId());
                Toast.makeText(getContext(), "Doctor eliminado", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.recycler);

        // Back press (opcional)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requireActivity().finish(); }
        });
    }

    private void openForm(int id) {
        Fragment f = DoctorFormFragment.newInstance(id);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, f)
                .addToBackStack(null)
                .commit();
    }

    @Override public void onClick(Doctor item) { openForm(item.getId()); }
}
