package com.example.ai_thumbnail_generator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ai_thumbnail_generator.R;
import com.google.android.material.button.MaterialButton;

public class ThumbnailGenerateFragment extends Fragment {

    private MainViewModel viewModel;
    private String selectedRatio = "16:9";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_generate_thumbnail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        EditText etTopic = view.findViewById(R.id.et_topic);
        MaterialButton btnGenerate = view.findViewById(R.id.btn_generate);
        TextView tvStatus = view.findViewById(R.id.tv_status);

        MaterialButton btn169 = view.findViewById(R.id.btn_ratio_16_9);
        MaterialButton btn11 = view.findViewById(R.id.btn_ratio_1_1);
        MaterialButton btn916 = view.findViewById(R.id.btn_ratio_9_16);

        View.OnClickListener ratioListener = v -> {
            btn169.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));
            btn11.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));
            btn916.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));

            MaterialButton b = (MaterialButton) v;
            b.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.primary_electric));
            selectedRatio = b.getText().toString();
        };

        btn169.setOnClickListener(ratioListener);
        btn11.setOnClickListener(ratioListener);
        btn916.setOnClickListener(ratioListener);

        btnGenerate.setOnClickListener(v -> {
            String topic = etTopic.getText().toString().trim();
            if (!topic.isEmpty()) {
                viewModel.generateThumbnail(topic, selectedRatio, "thumbnail");
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), tvStatus::setText);
    }
}
