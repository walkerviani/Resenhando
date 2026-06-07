package com.example.resenhando;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
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

import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);

        TextInputEditText nome = findViewById(R.id.nomeInputEditText);
        TextInputEditText dataNascimento = findViewById(R.id.dataInputEditText);
        TextInputEditText email = findViewById(R.id.emailInputEditText);
        TextInputEditText senha = findViewById(R.id.senhaInputEditText);
        TextInputEditText senhaConfirmacao = findViewById(R.id.confirmSenhaInputEditText);
        Button cadastrarBtn = findViewById(R.id.confirmarCadastro);
        Button voltarButton = findViewById(R.id.cadastroVoltarBtn);

        // Abre o calendário quando o usuário clicar no botão
        dataNascimento.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                // A string que ficará no input
                String data = String.format(Locale.getDefault(),"%02d/%02d/%04d", day, month + 1, year);
                dataNascimento.setText(data);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        cadastrarBtn.setOnClickListener(v -> {
            // Pegar o valor dos campos
            String nomeStr = String.valueOf(nome.getText());
            String emailStr = String.valueOf(email.getText());
            String senhaStr = String.valueOf(senha.getText());
            String senhaConfirmStr = String.valueOf(senhaConfirmacao.getText());
            String dataNascimentoStr = String.valueOf(dataNascimento.getText());

            // Verificações
            if(nomeStr.isEmpty()) {
                Snackbar.make(v, "Preencha o nome!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }
            else if(dataNascimentoStr.isEmpty()) {
                Snackbar.make(v, "Preencha a data de nascimento!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }
            else if(emailStr.isEmpty()) {
                Snackbar.make(v, "Preencha o email!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }
            else if(senhaStr.isEmpty()) {
                Snackbar.make(v, "Preencha a senha!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }
            else if(senhaConfirmStr.isEmpty()) {
                Snackbar.make(v, "Confirme a senha!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }
            else if(!senhaStr.equals(senhaConfirmStr)){
                Snackbar.make(v, "As senhas não devem ser diferente!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            } else {
                // Adicionar no banco SQLite
                try(DatabaseHelper dbHelper = new DatabaseHelper(v.getContext())) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("nome", nomeStr);
                    values.put("data_nascimento", dataNascimentoStr);
                    values.put("email", emailStr);
                    values.put("senha", senhaStr);

                    db.insert("usuarios", null, values);
                    db.close();

                    // Mostrar para o usuário que deu certo
                    Snackbar.make(v, "Cadastro completo!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor("#2E7D32"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();

                    // Sair automaticamente
                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);

                }catch (Exception e) {
                    Snackbar.make(v, "Erro ao realizar o cadastro: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#B00020"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();
                }
            }
        });

        // Caso o usuário queira cancelar o cadastro
        voltarButton.setOnClickListener(v -> finish());
    }

    private void fecharTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
