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
import com.example.ai_thumbnail_generator.network.Models;
import com.example.ai_thumbnail_generator.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private MainViewModel viewModel;
    private ThumbnailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipeRefreshDiscover);
        RecyclerView rvDiscover = view.findViewById(R.id.rvDiscover);
        
        // Premium Staggered Pinterest Grid with 2 columns
        rvDiscover.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        
        adapter = new ThumbnailAdapter();
        adapter.setShowDeleteButton(false);
        adapter.setShowShareButton(false);
        rvDiscover.setAdapter(adapter);

        SessionManager sessionManager = new SessionManager(requireContext());
        adapter.setCurrentUserId(sessionManager.getUserId());
        
        // Handle interactions for items owned by current user appearing in community
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

            @Override
            public void onLikeClicked(Models.ThumbnailData item, boolean isLiked) {
                if (viewModel != null) {
                    if (isLiked) {
                        viewModel.likeThumbnail(item.id);
                    } else {
                        viewModel.unlikeThumbnail(item.id);
                    }
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
            List<Models.ThumbnailData> publicList = new ArrayList<>();
            if (list != null) {
                for (Models.ThumbnailData item : list) {
                    if (item.is_public) {
                        publicList.add(item);
                    }
                }
            }
            adapter.setItems(publicList);
            swipeRefresh.setRefreshing(false); // Stop loading spinner
        });

        return view;
    }
}
