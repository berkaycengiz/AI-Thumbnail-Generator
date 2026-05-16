package com.example.ai_thumbnail_generator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_thumbnail_generator.R;

public class DiscoverFragment extends Fragment {

    private MainViewModel viewModel;
    private ThumbnailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        RecyclerView rvDiscover = view.findViewById(R.id.rvDiscover);
        // Staggered grid for Pinterest look
        rvDiscover.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new ThumbnailAdapter();
        rvDiscover.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getHistory().observe(getViewLifecycleOwner(), list -> {
            // For now, showing all history. Later we can filter by 'is_public'
            adapter.setItems(list);
        });

        return view;
    }
}
