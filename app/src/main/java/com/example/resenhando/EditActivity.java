    package com.example.resenhando;

    import android.app.Activity;
    import android.content.ContentValues;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.graphics.BitmapFactory;
    import android.graphics.Color;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.RatingBar;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.snackbar.Snackbar;
    import com.google.android.material.textfield.TextInputEditText;

    import java.io.File;
    import java.io.FileOutputStream;
    import java.io.InputStream;
    import java.io.OutputStream;

    public class EditActivity extends AppCompatActivity {
        private ImageView imagem;
        private Uri imagemUri;
        private ActivityResultLauncher<Intent> imagemLauncher;
        private int id;


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit);

            // Pega o id enviado do card clicado para alterar
            id = getIntent().getIntExtra("id", -1);
            if(id == -1) {
                finish(); // Se não tiver o id não inicia a edição
                return;
            }

            TextInputEditText titulo = findViewById(R.id.editTitulo);
            TextInputEditText descricao = findViewById(R.id.editDescricao);
            RatingBar ratingBar = findViewById(R.id.editRatingBar);
            imagem = findViewById(R.id.editImagem);
            Button salvarBtn = findViewById(R.id.editarResenha);
            Button voltarBtn = findViewById(R.id.editarVoltarBtn);

            try(DatabaseHelper dbHelper = new DatabaseHelper(this)){
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // Usa um ponteiro para procurar a resenha
                Cursor cursor = db.rawQuery("SELECT * FROM resenhas WHERE id = ?", new String[]{String.valueOf(id)});

                if(cursor.moveToFirst()) {
                    // Pega o valor dos itens guardados no banco e coloca na tela
                    titulo.setText(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                    descricao.setText(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
                    ratingBar.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow("qnt_estrelas")));

                    // Pega o caminho da imagem guardado no banco
                    String caminhoImagem = cursor.getString(cursor.getColumnIndexOrThrow("caminho_imagem"));
                    if(caminhoImagem != null) {
                        // Coloca na tela
                        imagem.setImageBitmap(BitmapFactory.decodeFile(caminhoImagem));
                        imagem.setPadding(0,0,0,0);
                    }
                }
                cursor.close();
            } catch (Exception e) {
                Snackbar.make(findViewById(android.R.id.content), "Erro ao carregar: " + e.getMessage(), Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            }

            // Prepara o app para abrir a galeria do celular e receber a imagem que o usuário escolher
            imagemLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imagemUri = result.getData().getData();
                            imagem.setImageURI(imagemUri);
                            imagem.setPadding(0, 0, 0, 0);
                        }
                    }
            );

            // Abre a galeria ao clicar no quadro da foto
            imagem.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagemLauncher.launch(intent);
            });

            salvarBtn.setOnClickListener(v -> {
                String tituloStr = titulo.getText() != null ? titulo.getText().toString() : "";
                String descricaoStr = descricao.getText() != null ? descricao.getText().toString() : "";
                float qntEstrelas = ratingBar.getRating();

                // Verificações
                if (tituloStr.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), "Preencha o título!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor("#B00020"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();
                } else if (qntEstrelas == 0) {
                    Snackbar.make(findViewById(android.R.id.content), "Selecione pelo menos uma estrela!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor("#B00020"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();
                } else {
                    // Atualizar a resenha no banco de dados
                    try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put("titulo", tituloStr);
                        values.put("descricao", descricaoStr);
                        values.put("qnt_estrelas", qntEstrelas);

                        if (imagemUri != null) {
                            String caminhoImagem = salvarImagemNoApp(imagemUri);
                            values.put("caminho_imagem", caminhoImagem);
                        }

                        db.update("resenhas", values, "id = ?", new String[]{String.valueOf(id)});
                        db.close();

                        Snackbar.make(findViewById(android.R.id.content), "Resenha atualizada!", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(Color.parseColor("#2E7D32"))
                                .setTextColor(Color.WHITE)
                                .show();
                        fecharTeclado();
                        // volta para a home após salvar
                        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000);

                    } catch (Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), "Erro ao salvar: " + e.getMessage(), Snackbar.LENGTH_LONG)
                                .setBackgroundTint(Color.parseColor("#B00020"))
                                .setTextColor(Color.WHITE)
                                .show();
                        fecharTeclado();
                    }
                }
            });

            // Caso desista de realizar a alteração
            voltarBtn.setOnClickListener(v -> finish());
        }

        private String salvarImagemNoApp(Uri uri) {
            try {
                SharedPreferences prefs = getSharedPreferences("sessao", Context.MODE_PRIVATE);
                int userId = prefs.getInt("id", 0);

                File pasta = new File(getFilesDir(), "imagens/usuario_" + userId);
                if (!pasta.exists()) {
                    boolean criada = pasta.mkdirs(); // cria a pasta se não existir
                    if (!criada) return null; // pasta existe
                }

                String nomeArquivo = "img_" + System.currentTimeMillis() + ".jpg"; // randomiza o nome da foto
                File arquivo = new File(pasta, nomeArquivo);

                // Copia a imagem byte a byte
                InputStream input = getContentResolver().openInputStream(uri);
                if (input == null) return null;
                OutputStream output = new FileOutputStream(arquivo);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.close();
                input.close();

                return arquivo.getAbsolutePath(); // retorna o caminho
            } catch (Exception e) {
                return null;
            }
        }

        private void fecharTeclado() {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }
