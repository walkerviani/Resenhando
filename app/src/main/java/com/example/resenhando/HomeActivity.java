package com.example.resenhando;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // carrega o HomeFragment ao iniciar
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();

        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton addButton = findViewById(R.id.addButton);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton logoutButton = findViewById(R.id.logoutButton);

        // Inicia o fragment (tela inicial)
        homeButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        });

        // Inicia o fragment (tela de criação de resenhas)
        addButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AddFragment())
                    .commit();
        });

        // Inicia o fragment (tela de pesquisa)
        searchButton.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new SearchFragment())
                    .commit();
        });

        logoutButton.setOnClickListener(v -> {
            // Cria um dialogo para prevenir caso clique sem querer
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Sair da conta")
                    .setMessage("Tem certeza que deseja sair da conta atual?")
                    .setNegativeButton("Cancelar", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setPositiveButton("Sair", ((dialog, which) -> {
                        // Termina a sessão do usuário
                        SharedPreferences prefs = getSharedPreferences("sessao", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        // Volta para o login
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }))
                    .show();
        });
    }
}
