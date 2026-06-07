package com.example.resenhando;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextInputEditText email = findViewById(R.id.loginInputEditText);
        TextInputEditText senha = findViewById(R.id.senhaLoginInputEditText);
        Button cadastroBtn = findViewById(R.id.cadastrarBtn);
        Button entrarBtn = findViewById(R.id.entrarBtn);

        // Verifica se o usuário já está logado
        SharedPreferences prefs = getSharedPreferences("sessao", MODE_PRIVATE);
        if (prefs.getBoolean("logado", false)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        // Verifica o login
        entrarBtn.setOnClickListener(v -> {
            // Pegar o valor dos campos
            String emailStr = String.valueOf(email.getText());
            String senhaStr = String.valueOf(senha.getText());

            if(emailStr.isEmpty()) {
                Snackbar.make(v, "Preencha o email!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            } else if (senhaStr.isEmpty()) {
                Snackbar.make(v, "Preencha a senha!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            } else {
                try(DatabaseHelper dbHelper = new DatabaseHelper(v.getContext())) {
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    // Usa um ponteiro para procurar o login
                    Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE email = ?",new String[]{emailStr});

                    // Se achou
                    if(cursor.moveToFirst()){
                        String senhaUsuario = cursor.getString(cursor.getColumnIndexOrThrow("senha"));
                        // Compara a senha
                        if(!senhaStr.equals(senhaUsuario)){
                            Snackbar.make(v, "Senha Inválida!", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(Color.parseColor("#B00020"))
                                    .setTextColor(Color.WHITE)
                                    .show();
                            fecharTeclado();
                        } else {
                            // Salva a sessão
                            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                            prefs.edit().putBoolean("logado", true)
                                    .putInt("id", userId)
                                    .apply();

                            // Mostrar para o usuário que o login deu certo
                            Snackbar.make(v, "Login com sucesso!", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(Color.parseColor("#2E7D32"))
                                    .setTextColor(Color.WHITE)
                                    .show();
                            fecharTeclado();
                            // Sair do login e ir para a página home automaticamente
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Intent intent = new Intent(this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }, 1500);
                        }

                    } else {
                        Snackbar.make(v, "Login inválido!", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(Color.parseColor("#B00020"))
                                .setTextColor(Color.WHITE)
                                .show();
                        fecharTeclado();
                    }
                    cursor.close();
                    db.close();
                } catch (Exception e) {
                    Snackbar.make(v, "Erro ao realizar o login: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#B00020"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();
                }
            }
        });

        // Leva para a página de cadastro
        cadastroBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void fecharTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}