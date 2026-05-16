package com.example.ai_thumbnail_generator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

        if (savedInstanceState == null) {
            btnGenerate.performClick();
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