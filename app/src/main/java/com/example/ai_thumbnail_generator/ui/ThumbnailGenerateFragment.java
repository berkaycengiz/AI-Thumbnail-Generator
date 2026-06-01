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

        MaterialButton btn169 = view.findViewById(R.id.btn_ratio_16_9);
        MaterialButton btn11 = view.findViewById(R.id.btn_ratio_1_1);
        MaterialButton btn916 = view.findViewById(R.id.btn_ratio_9_16);

        View.OnClickListener ratioListener = v -> {
            int activeStroke = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_electric);
            int inactiveStroke = android.graphics.Color.parseColor("#334155");
            int activeBg = android.graphics.Color.parseColor("#20818CF8"); // 20% Alpha primary_electric
            int inactiveBg = android.graphics.Color.parseColor("#0F172A");
            int activeText = android.graphics.Color.parseColor("#FFFFFF");
            int inactiveText = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_gray);

            // Reset all buttons to inactive styling
            for (MaterialButton btn : new MaterialButton[]{btn169, btn11, btn916}) {
                btn.setStrokeColor(android.content.res.ColorStateList.valueOf(inactiveStroke));
                btn.setBackgroundColor(inactiveBg);
                btn.setTextColor(inactiveText);
            }

            // Highlight selected button with glow styling
            MaterialButton b = (MaterialButton) v;
            b.setStrokeColor(android.content.res.ColorStateList.valueOf(activeStroke));
            b.setBackgroundColor(activeBg);
            b.setTextColor(activeText);
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



        // Prevent double clicking by disabling generate button during processing
        viewModel.getGenerationStatus().observe(getViewLifecycleOwner(), status -> {
            boolean isProcessing = (status == MainViewModel.GenStatus.PROCESSING);
            btnGenerate.setEnabled(!isProcessing);
            btnGenerate.setAlpha(isProcessing ? 0.6f : 1.0f);
            btnGenerate.setText(isProcessing ? "GENERATING..." : "GENERATE NOW");
        });
    }
}
