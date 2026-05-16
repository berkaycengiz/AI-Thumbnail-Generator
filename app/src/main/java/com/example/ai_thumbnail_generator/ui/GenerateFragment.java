package com.example.ai_thumbnail_generator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.R;

public class GenerateFragment extends Fragment {

    private MainViewModel viewModel;
    private EditText etVideoTitle;
    private RadioGroup rgRatio;
    private ImageView ivPreview;
    private View previewCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate, container, false);

        etVideoTitle = view.findViewById(R.id.etVideoTitle);
        rgRatio = view.findViewById(R.id.rgRatio);
        ivPreview = view.findViewById(R.id.ivPreview);
        previewCard = view.findViewById(R.id.previewCard);
        Button btnGenerate = view.findViewById(R.id.btnGenerate);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        btnGenerate.setOnClickListener(v -> {
            String title = etVideoTitle.getText().toString().trim();
            if (title.isEmpty()) return;

            int checkedId = rgRatio.getCheckedRadioButtonId();
            String ratio = "16:9";
            if (checkedId == R.id.rb11) ratio = "1:1";
            else if (checkedId == R.id.rb916) ratio = "9:16";

            // NOT: Google Login entegrasyonu tamamlandığında 'userId' dinamik olacak
            viewModel.generateThumbnail(title, ratio);
        });

        viewModel.getLastGeneratedUri().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                previewCard.setVisibility(View.VISIBLE);
                Glide.with(this).load(uri).into(ivPreview);
            }
        });

        return view;
    }
}
