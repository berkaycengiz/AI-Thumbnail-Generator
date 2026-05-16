package com.example.ai_thumbnail_generator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.ui.MainViewModel;
import com.example.ai_thumbnail_generator.ui.ThumbnailAdapter;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private EditText etVideoTitle;
    private RadioGroup rgRatio;
    private Button btnGenerate;
    private ImageView ivPreview;
    private View progressOverlay;
    private TextView tvProgressStatus;
    private ThumbnailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        etVideoTitle = findViewById(R.id.etVideoTitle);
        rgRatio = findViewById(R.id.rgRatio);
        btnGenerate = findViewById(R.id.btnGenerate);
        ivPreview = findViewById(R.id.ivPreview);
        progressOverlay = findViewById(R.id.progressOverlay);
        tvProgressStatus = findViewById(R.id.tvProgressStatus);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);

        // Setup RecyclerView
        adapter = new ThumbnailAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe ViewModel State
        viewModel.getIsLoading().observe(this, loading -> {
            progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnGenerate.setEnabled(!loading);
        });

        viewModel.getStatusMessage().observe(this, status -> {
            tvProgressStatus.setText(status);
            if (status.startsWith("Error:")) {
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLastGenerated().observe(this, entity -> {
            if (entity != null) {
                ivPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(entity.localUri).into(ivPreview);
                Toast.makeText(this, "Thumbnail Generated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHistory().observe(this, list -> {
            adapter.setItems(list);
        });

        // Click Listener
        btnGenerate.setOnClickListener(v -> {
            String title = etVideoTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etVideoTitle.setError("Please enter a title");
                return;
            }

            int checkedId = rgRatio.getCheckedRadioButtonId();
            String ratio = "16:9";
            if (checkedId == R.id.rb11) ratio = "1:1";
            else if (checkedId == R.id.rb916) ratio = "9:16";

            viewModel.generateThumbnail(title, ratio);
        });
    }
}