package com.example.resenhando;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private ResenhaAdapter adapter;
    private List<Resenha> listaCompleta; // Guarda todas as resenhas para filtrar localmente

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onResume() {
        super.onResume();

        RecyclerView recyclerView = requireView().findViewById(R.id.recyclerView);
        TextView txtVazio = requireView().findViewById(R.id.txtInfoPesquisa);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaCompleta = carregarResenhas(); // Carrega todas as resenhas do banco

        // Inicia o adapter com lista vazia — só exibe resultados após digitar
        // O listener de lista vazia é vazio ({}) porque nesse fragment não precisa tratar esse caso
        adapter = new ResenhaAdapter(new ArrayList<>(), () -> {});
        recyclerView.setAdapter(adapter);

        // Exibe mensagem inicial orientando o usuário a digitar
        txtVazio.setText("Digite algo para pesquisar!");
        txtVazio.setVisibility(View.VISIBLE);

        TextInputEditText searchEditText = requireView().findViewById(R.id.searchEditText);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    // Campo vazio: limpa a lista e exibe mensagem inicial
                    adapter.atualizarLista(new ArrayList<>());
                    recyclerView.setVisibility(View.GONE);
                    txtVazio.setText("Digite algo para pesquisar!");
                    txtVazio.setVisibility(View.VISIBLE);
                } else {
                    // Tem texto: filtra a lista pelo que foi digitado
                    filtrar(s.toString(), recyclerView, txtVazio);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Filtra a listaCompleta pelo título e atualiza o adapter com os resultados
    private void filtrar(String texto, RecyclerView recyclerView, TextView txtVazio) {
        List<Resenha> listaFiltrada = new ArrayList<>();

        // Percorre todas as resenhas e adiciona as que contém o texto no título
        // toLowerCase garante que a busca não seja case-sensitive (ex: "céu" acha "Céu")
        for (Resenha resenha : listaCompleta) {
            if (resenha.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(resenha);
            }
        }
        adapter.atualizarLista(listaFiltrada); // Atualiza o RecyclerView com os resultados

        if (listaFiltrada.isEmpty()) {
            // Nenhum resultado: esconde o RecyclerView e exibe mensagem
            recyclerView.setVisibility(View.GONE);
            txtVazio.setText("Nenhuma resenha encontrada!");
            txtVazio.setVisibility(View.VISIBLE);
        } else {
            // Tem resultados: exibe o RecyclerView e esconde a mensagem
            recyclerView.setVisibility(View.VISIBLE);
            txtVazio.setVisibility(View.GONE);
        }
    }

    // Busca todas as resenhas do banco e retorna como lista
    private List<Resenha> carregarResenhas() {
        List<Resenha> lista = new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("sessao", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", 0);

        Cursor cursor = db.rawQuery("SELECT * FROM resenhas WHERE usuario_id = ?", new String[]{String.valueOf(userId)});

        // Percorre cada linha retornada pelo banco e cria um objeto Resenha
        while(cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
            float estrelas = cursor.getFloat(cursor.getColumnIndexOrThrow("qnt_estrelas"));
            String imagem = cursor.getString(cursor.getColumnIndexOrThrow("caminho_imagem"));
            String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
            lista.add(new Resenha (id, titulo, estrelas, imagem, descricao));
        }

        cursor.close();
        db.close();
        return lista;
    }
}