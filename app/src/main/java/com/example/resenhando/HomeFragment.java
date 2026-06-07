package com.example.resenhando;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public HomeFragment(){
        super(R.layout.fragment_home);
    }

    @Override
    public void onResume() {
        super.onResume();

        RecyclerView recyclerView = requireView().findViewById(R.id.recyclerView);
        TextView txtVazio = requireView().findViewById(R.id.txtVazio);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        try {
            List<Resenha> lista = carregarResenhas();
            ResenhaAdapter adapter = new ResenhaAdapter(lista, () -> {
                recyclerView.setVisibility(View.GONE);
                txtVazio.setVisibility(View.VISIBLE); // Começa com o texto: (Nenhuma resenha encontrada!) em caso de erro
            });
            recyclerView.setAdapter(adapter);

            // Se a lista estiver vazia apresenta: (Nenhuma resenha encontrada!)
            if(lista.isEmpty()){
                recyclerView.setVisibility(View.GONE);
                txtVazio.setVisibility(View.VISIBLE);
            } else { // Se a lista não está vazia mostra os itens
                recyclerView.setVisibility(View.VISIBLE);
                txtVazio.setVisibility(View.GONE);
            }
        }catch (Exception e){
            Snackbar.make(requireView(), "Erro ao carregar resenhas: " + e.getMessage(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.parseColor("#B00020"))
                    .setTextColor(Color.WHITE)
                    .show();
        }

    }

    private List<Resenha> carregarResenhas() {
        List<Resenha> lista = new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("sessao", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", 0);

        // Pega as resenhas
        Cursor cursor = db.rawQuery("SELECT * FROM resenhas WHERE usuario_id = ?", new String[]{String.valueOf(userId)});

        // Cria uma lista com todos os valores de cada uma das resenhas
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
