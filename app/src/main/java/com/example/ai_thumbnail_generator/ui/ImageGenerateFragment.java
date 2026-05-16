package com.example.ai_thumbnail_generator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ai_thumbnail_generator.R;
import com.google.android.material.button.MaterialButton;

public class ImageGenerateFragment extends Fragment {

    private MainViewModel viewModel;
    private String selectedRatio = "1:1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_generate_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        EditText etPrompt = view.findViewById(R.id.et_prompt_simple);
        MaterialButton btnGenerate = view.findViewById(R.id.btn_generate_simple);

        MaterialButton btn11 = view.findViewById(R.id.btn_img_ratio_1_1);
        MaterialButton btn169 = view.findViewById(R.id.btn_img_ratio_16_9);
        MaterialButton btn916 = view.findViewById(R.id.btn_img_ratio_9_16);

        View.OnClickListener ratioListener = v -> {
            btn11.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));
            btn169.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));
            btn916.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.text_gray));

            MaterialButton b = (MaterialButton) v;
            b.setStrokeColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.accent_cyan));
            selectedRatio = b.getText().toString();
        };

        btn11.setOnClickListener(ratioListener);
        btn169.setOnClickListener(ratioListener);
        btn916.setOnClickListener(ratioListener);

        btnGenerate.setOnClickListener(v -> {
            String prompt = etPrompt.getText().toString().trim();
            if (!prompt.isEmpty()) {
                viewModel.generateThumbnail(prompt, selectedRatio, "art");
            }
        });
    }
}
