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
import com.example.ai_thumbnail_generator.network.Models;
import com.example.ai_thumbnail_generator.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private MainViewModel viewModel;
    private ThumbnailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipeRefreshLibrary);
        RecyclerView rvLibrary = view.findViewById(R.id.rvLibrary);
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ThumbnailAdapter();
        rvLibrary.setAdapter(adapter);

        SessionManager sessionManager = new SessionManager(requireContext());
        String currentUserId = sessionManager.getUserId();
        adapter.setCurrentUserId(currentUserId);

        // Bind interactive callbacks to notify ViewModel on toggle share or delete
        adapter.setOnThumbnailInteractionListener(new ThumbnailAdapter.OnThumbnailInteractionListener() {
            @Override
            public void onShareToggled(Models.ThumbnailData item) {
                if (viewModel != null) {
                    viewModel.togglePublic(item.id);
                }
            }

            @Override
            public void onDeleteClicked(Models.ThumbnailData item) {
                if (viewModel != null) {
                    viewModel.deleteThumbnail(item.id);
                }
            }

            @Override
            public void onCardClicked(Models.ThumbnailData item) {
                if (item.image_url != null && !item.image_url.isEmpty()) {
                    com.example.ai_thumbnail_generator.utils.ImageDetailDialog.show(requireContext(), item.image_url);
                }
            }

            @Override
            public void onDownloadClicked(Models.ThumbnailData item) {
                if (item.image_url != null && !item.image_url.isEmpty()) {
                    com.example.ai_thumbnail_generator.utils.ImageDownloader.download(requireContext(), item.image_url, item.original_title);
                }
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        // Swipe to Refresh gesture callback
        swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel != null) {
                viewModel.fetchHistory();
            }
        });

        viewModel.getHistory().observe(getViewLifecycleOwner(), list -> {
            List<Models.ThumbnailData> myThumbnails = new ArrayList<>();
            if (list != null) {
                for (Models.ThumbnailData item : list) {
                    if (item.user_id != null && item.user_id.equals(currentUserId)) {
                        myThumbnails.add(item);
                    }
                }
            }
            adapter.setItems(myThumbnails);
            swipeRefresh.setRefreshing(false); // Stop loading spinner
        });

        return view;
    }
}
