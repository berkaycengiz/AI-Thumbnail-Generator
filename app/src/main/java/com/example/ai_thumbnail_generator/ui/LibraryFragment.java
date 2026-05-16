package com.example.ai_thumbnail_generator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_thumbnail_generator.R;

public class LibraryFragment extends Fragment {

    private MainViewModel viewModel;
    private ThumbnailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        RecyclerView rvLibrary = view.findViewById(R.id.rvLibrary);
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ThumbnailAdapter();
        rvLibrary.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getHistory().observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
        });

        return view;
    }
}
