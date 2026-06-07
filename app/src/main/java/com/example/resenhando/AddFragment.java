package com.example.resenhando;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AddFragment extends Fragment {

    private ActivityResultLauncher<Intent> imagemLauncher;
    private ImageView imagem;
    private Uri imagemUri;

    public AddFragment() {
        super(R.layout.fragment_add);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Prepara o app para abrir a galeria do celular e receber a imagem que o usuário escolher
        imagemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imagemUri = result.getData().getData();
                        imagem.setImageURI(imagemUri);
                        imagem.setPadding(0,0,0,0);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        TextInputEditText titulo = view.findViewById(R.id.titulo);
        TextInputEditText descricao = view.findViewById(R.id.descricao);
        imagem = view.findViewById(R.id.imagem);
        Button salvarBtn = view.findViewById(R.id.salvarResenha);

        // Abre a galeria ao clicar no quadro da foto
        imagem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagemLauncher.launch(intent);
        });

        salvarBtn.setOnClickListener(v -> {
            float qntEstrelas = ratingBar.getRating();
            String tituloStr = titulo.getText() != null ? titulo.getText().toString() : "";
            String descricaoStr = descricao.getText() != null ? descricao.getText().toString() : "";

            // Verificações
            if (tituloStr.isEmpty()) {
                Snackbar.make(requireView(), "Preencha o título!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            } else if (qntEstrelas == 0) {
                Snackbar.make(requireView(), "Selecione pelo menos uma estrela!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#B00020"))
                        .setTextColor(Color.WHITE)
                        .show();
                fecharTeclado();
            } else {
                // Inserir a resenha no banco de dados
                try (DatabaseHelper dbHelper = new DatabaseHelper(getContext())) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put("titulo", tituloStr);
                    values.put("descricao", descricaoStr);
                    values.put("qnt_estrelas", qntEstrelas);
                    String caminhoImagem = imagemUri != null ? salvarImagemNoApp(imagemUri) : null;
                    values.put("caminho_imagem", caminhoImagem);

                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences("sessao", Context.MODE_PRIVATE);
                    int userId = prefs.getInt("id", 0);
                    values.put("usuario_id", userId);

                    db.insert("resenhas", null, values);
                    db.close();

                    Snackbar.make(requireView(), "Resenha salva!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor("#2E7D32"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();

                    // Leva para a página inicial
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, new HomeFragment())
                                .commit();
                    }, 1000);


                } catch (Exception e) {
                    Snackbar.make(requireView(), "Erro ao salvar: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#B00020"))
                            .setTextColor(Color.WHITE)
                            .show();
                    fecharTeclado();
                }
            }
        });
    }

    private String salvarImagemNoApp(Uri uri) {
        try {
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("sessao", Context.MODE_PRIVATE);
            int userId = prefs.getInt("id", 0);

            File pasta = new File(requireContext().getFilesDir(), "imagens/usuario_" + userId);
            if (!pasta.exists()) {
                boolean criada = pasta.mkdirs(); // cria a pasta se não existir
                if (!criada) return null; // pasta existe
            }

            String nomeArquivo = "img_" + +System.currentTimeMillis() + ".jpg"; // randomiza o nome da foto
            File arquivo = new File(pasta, nomeArquivo);

            // Copia a imagem byte a byte
            InputStream input = requireContext().getContentResolver().openInputStream(uri);
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
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}
