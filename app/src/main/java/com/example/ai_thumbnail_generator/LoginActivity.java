package com.example.ai_thumbnail_generator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_thumbnail_generator.network.Constants;
import com.example.ai_thumbnail_generator.network.SupabaseAuthService;
import com.example.ai_thumbnail_generator.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private View btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.loginProgressBar);
        btnGoogle = findViewById(R.id.btn_google_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    String name = account.getDisplayName();
                    String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";
                    authenticateWithSupabase(account.getIdToken(), name, photoUrl);
                } else {
                    showLoading(false);
                }
            } catch (ApiException e) {
                showLoading(false);
                Log.e(TAG, "Google sign in failed. Code: " + e.getStatusCode() + " Message: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void authenticateWithSupabase(String idToken, String userName, String profilePic) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SupabaseAuthService authService = retrofit.create(SupabaseAuthService.class);
        SupabaseAuthService.IdTokenRequest request = new SupabaseAuthService.IdTokenRequest(idToken);

        authService.signInWithIdToken(Constants.SUPABASE_KEY, request).enqueue(new Callback<SupabaseAuthService.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseAuthService.AuthResponse> call, Response<SupabaseAuthService.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseAuthService.AuthResponse authResponse = response.body();
                    sessionManager.saveSession(
                            authResponse.access_token,
                            authResponse.refresh_token,
                            authResponse.user.id,
                            userName,
                            profilePic
                    );
                    goToMainActivity();
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseAuthService.AuthResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGoogle.setEnabled(!isLoading);
        btnGoogle.setAlpha(isLoading ? 0.5f : 1.0f);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
