package com.example.ai_thumbnail_generator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.LoginActivity;
import com.example.ai_thumbnail_generator.R;
import com.example.ai_thumbnail_generator.utils.SessionManager;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SessionManager sessionManager = new SessionManager(requireContext());
        
        ImageView ivProfile = view.findViewById(R.id.ivProfilePic);
        TextView tvName = view.findViewById(R.id.tvUserName);
        View btnLogout = view.findViewById(R.id.btnLogout);

        tvName.setText(sessionManager.getUserName());
        
        String profilePicUrl = sessionManager.getProfilePic();
        if (!profilePicUrl.isEmpty()) {
            Glide.with(this)
                    .load(profilePicUrl)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(ivProfile);
        }

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
