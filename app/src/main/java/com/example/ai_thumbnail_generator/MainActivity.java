package com.example.ai_thumbnail_generator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.ai_thumbnail_generator.ui.DiscoverFragment;
import com.example.ai_thumbnail_generator.ui.GenerateFragment;
import com.example.ai_thumbnail_generator.ui.LibraryFragment;
import com.example.ai_thumbnail_generator.ui.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        com.example.ai_thumbnail_generator.utils.SessionManager sessionManager = new com.example.ai_thumbnail_generator.utils.SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btnGenerate = findViewById(R.id.nav_btn_generate);
        View btnLibrary = findViewById(R.id.nav_btn_library);
        View btnDiscover = findViewById(R.id.nav_btn_discover);
        View btnSettings = findViewById(R.id.nav_btn_settings);

        ImageView iconGenerate = findViewById(R.id.nav_icon_generate);
        ImageView iconLibrary = findViewById(R.id.nav_icon_library);
        ImageView iconDiscover = findViewById(R.id.nav_icon_discover);
        ImageView iconSettings = findViewById(R.id.nav_icon_settings);

        TextView textGenerate = findViewById(R.id.nav_text_generate);
        TextView textLibrary = findViewById(R.id.nav_text_library);
        TextView textDiscover = findViewById(R.id.nav_text_discover);
        TextView textSettings = findViewById(R.id.nav_text_settings);

        int colorSelected = androidx.core.content.ContextCompat.getColor(this, R.color.primary_electric);
        int colorUnselected = androidx.core.content.ContextCompat.getColor(this, R.color.nav_unselected);

        btnGenerate.setOnClickListener(v -> {
            updateNavUI(iconGenerate, textGenerate, iconLibrary, textLibrary, iconDiscover, textDiscover, iconSettings, textSettings, colorSelected, colorUnselected, 0);
            switchFragment(new GenerateFragment());
        });

        btnLibrary.setOnClickListener(v -> {
            updateNavUI(iconGenerate, textGenerate, iconLibrary, textLibrary, iconDiscover, textDiscover, iconSettings, textSettings, colorSelected, colorUnselected, 1);
            switchFragment(new LibraryFragment());
        });

        btnDiscover.setOnClickListener(v -> {
            updateNavUI(iconGenerate, textGenerate, iconLibrary, textLibrary, iconDiscover, textDiscover, iconSettings, textSettings, colorSelected, colorUnselected, 2);
            switchFragment(new DiscoverFragment());
        });

        btnSettings.setOnClickListener(v -> {
            updateNavUI(iconGenerate, textGenerate, iconLibrary, textLibrary, iconDiscover, textDiscover, iconSettings, textSettings, colorSelected, colorUnselected, 3);
            switchFragment(new SettingsFragment());
        });

        // Retrieve shared MainViewModel to control the global floating loading card
        com.example.ai_thumbnail_generator.ui.MainViewModel viewModel = 
                new androidx.lifecycle.ViewModelProvider(this).get(com.example.ai_thumbnail_generator.ui.MainViewModel.class);

        com.google.android.material.card.MaterialCardView cardLoading = findViewById(R.id.card_loading);
        ProgressBar loadingSpinner = findViewById(R.id.loading_spinner);
        ImageView loadingIcon = findViewById(R.id.loading_icon);
        TextView loadingText = findViewById(R.id.loading_text);

        viewModel.getGenerationStatus().observe(this, status -> {
            String msg = viewModel.getStatusMessage().getValue();
            updateLoadingCard(status, msg, cardLoading, loadingSpinner, loadingIcon, loadingText);
        });

        if (savedInstanceState == null) {
            btnGenerate.performClick();
        }
    }

    private void updateLoadingCard(com.example.ai_thumbnail_generator.ui.MainViewModel.GenStatus status, String message,
                                   com.google.android.material.card.MaterialCardView card,
                                   ProgressBar spinner, ImageView icon, TextView text) {
        if (status == com.example.ai_thumbnail_generator.ui.MainViewModel.GenStatus.IDLE) {
            // Spring/slide out downwards out of view
            card.animate()
                .translationY(1000f)
                .setDuration(400)
                .withEndAction(() -> card.setVisibility(View.GONE))
                .start();
        } else {
            // Apply theme styling and details according to status
            if (status == com.example.ai_thumbnail_generator.ui.MainViewModel.GenStatus.PROCESSING) {
                spinner.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(this, R.color.primary_electric)));
                text.setText(message != null && !message.isEmpty() ? message : "AI is crafting your thumbnail...");
            } else if (status == com.example.ai_thumbnail_generator.ui.MainViewModel.GenStatus.COMPLETED) {
                spinner.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_checkmark);
                icon.setColorFilter(android.graphics.Color.parseColor("#10B981")); // Emerald green success tint
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981")));
                text.setText("Your thumbnail is ready!");
            } else if (status == com.example.ai_thumbnail_generator.ui.MainViewModel.GenStatus.FAILED) {
                spinner.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_alert);
                icon.setColorFilter(android.graphics.Color.parseColor("#EF4444")); // Crimson red error tint
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444")));
                text.setText("Generation failed.");
            }

            if (card.getVisibility() != View.VISIBLE) {
                card.setTranslationY(1000f);
                card.setVisibility(View.VISIBLE);
                card.animate()
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)) // Parity with React-Native spring
                    .start();
            }
        }
    }

    private void updateNavUI(ImageView iG, TextView tG, ImageView iL, TextView tL, ImageView iD, TextView tD, ImageView iS, TextView tS, int sel, int unsel, int index) {
        iG.setColorFilter(unsel); tG.setTextColor(unsel); tG.setTypeface(null, android.graphics.Typeface.NORMAL);
        iL.setColorFilter(unsel); tL.setTextColor(unsel); tL.setTypeface(null, android.graphics.Typeface.NORMAL);
        iD.setColorFilter(unsel); tD.setTextColor(unsel); tD.setTypeface(null, android.graphics.Typeface.NORMAL);
        iS.setColorFilter(unsel); tS.setTextColor(unsel); tS.setTypeface(null, android.graphics.Typeface.NORMAL);

        if (index == 0) { iG.setColorFilter(sel); tG.setTextColor(sel); tG.setTypeface(null, android.graphics.Typeface.BOLD); }
        else if (index == 1) { iL.setColorFilter(sel); tL.setTextColor(sel); tL.setTypeface(null, android.graphics.Typeface.BOLD); }
        else if (index == 2) { iD.setColorFilter(sel); tD.setTextColor(sel); tD.setTypeface(null, android.graphics.Typeface.BOLD); }
        else if (index == 3) { iS.setColorFilter(sel); tS.setTextColor(sel); tS.setTypeface(null, android.graphics.Typeface.BOLD); }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}