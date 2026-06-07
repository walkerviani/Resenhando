package com.example.resenhando;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

// Adapter é a ponte entre a lista de dados (Resenha) e o RecyclerView que exibe na tela
public class ResenhaAdapter extends RecyclerView.Adapter<ResenhaAdapter.ViewHolder> {

    private List<Resenha> lista;
    private OnListaVaziaListener listener; // Callback para avisar quando a lista ficar vazia

    // Construtor sem listener (usado quando não precisa saber se a lista ficou vazia)
    public ResenhaAdapter(List<Resenha> lista) {
        this.lista = lista;
    }

    // Construtor com listener (usado na HomeFragment para exibir mensagem de lista vazia)
    public ResenhaAdapter(List<Resenha> lista, OnListaVaziaListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    // Chamado pelo RecyclerView quando precisa criar um novo card visualmente
    // Infla (carrega) o layout XML do item e retorna um ViewHolder com ele
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resenha, parent, false);
        return new ViewHolder(view);
    }

    // Chamado pelo RecyclerView para preencher os dados em cada card
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resenha resenha = lista.get(position); // position é o índice da resenha na lista

        // Preenche os campos do card com os dados da resenha
        holder.titulo.setText(resenha.getTitulo());
        holder.estrelas.setRating(resenha.getQntEstrelas());

        boolean temDescricao = resenha.getDescricao() != null && !resenha.getDescricao().isEmpty();
        boolean temImagem = resenha.getCaminhoImagem() != null;

        // Some o layout inteiro se não tiver nem imagem nem a descrição
        if(temDescricao || temImagem) {
            holder.layout.setVisibility(View.VISIBLE);
        } else {
            holder.layout.setVisibility(View.GONE);
        }

        if(temDescricao) {
            holder.descricao.setVisibility(View.VISIBLE);
            holder.descricao.setText(resenha.getDescricao());
        } else {
            holder.descricao.setVisibility(View.GONE);
        }

        // Se a resenha tiver uma imagem, carrega do armazenamento e exibe no card
        if (temImagem) {
            holder.imagem.setVisibility(View.VISIBLE);
            holder.imagem.setImageBitmap(BitmapFactory.decodeFile(resenha.getCaminhoImagem()));
        } else {
            holder.imagem.setVisibility(View.GONE);
        }

        holder.editarBtn.setOnClickListener(v -> { // Inicia a pagina de edição e envia o id
            Intent intent = new Intent(v.getContext(), EditActivity.class);
            intent.putExtra("id", resenha.getId());
            v.getContext().startActivity(intent);
        });

        holder.excluirBtn.setOnClickListener(v -> {
            // Cria um dialogo para prevenir caso clique sem querer
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Excluir resenha")
                    .setMessage("Tem certeza que deseja excluir essa resenha?")
                    .setNegativeButton("Cancelar", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setPositiveButton("Excluir", ((dialog, which) -> {
                        // Pega o id da resenha
                        int posicaoAtual = holder.getBindingAdapterPosition();
                        if (posicaoAtual != RecyclerView.NO_ID) {

                            // Exclui a resenha
                            try (DatabaseHelper dbHelper = new DatabaseHelper(v.getContext())) {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete("resenhas", "id = ?", new String[]{String.valueOf(resenha.getId())});
                                db.close();

                                lista.remove(posicaoAtual);
                                notifyItemRemoved(posicaoAtual);

                                // Marca que a página está vazia caso essa tenha sido a unica resenha
                                if (lista.isEmpty() && listener != null) {
                                    listener.onListaVazia();
                                }

                            } catch (Exception e) {
                                Snackbar.make(v, "Erro ao excluir: " + e.getMessage(), Snackbar.LENGTH_LONG)
                                        .setBackgroundTint(Color.parseColor("#B00020"))
                                        .setTextColor(Color.WHITE)
                                        .show();
                            }
                        }
                    }))
                    .show();
        });
    }

    // Retorna o total de itens da lista para o RecyclerView saber quantos cards criar
    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ViewHolder guarda as referências dos componentes visuais de cada card
    // Evita que o RecyclerView fique buscando os componentes repetidamente (melhora performance)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem;
        TextView titulo;
        RatingBar estrelas;
        TextView descricao;
        ImageButton editarBtn, excluirBtn;
        LinearLayout layout; // Parte de cima do card. Onde fica a imagem e a descrição

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagem = itemView.findViewById(R.id.cardImagem);
            titulo = itemView.findViewById(R.id.cardTitulo);
            estrelas = itemView.findViewById(R.id.cardEstrelas);
            descricao = itemView.findViewById(R.id.cardDescricao);
            editarBtn = itemView.findViewById(R.id.editarResenha);
            excluirBtn = itemView.findViewById(R.id.excluirResenha);
            layout = itemView.findViewById(R.id.cardSuperior);
        }
    }

    // Interface que a HomeFragment implementa para ser avisada quando a lista ficar vazia
    public interface OnListaVaziaListener {
        void onListaVazia();
    }

    // Substitui toda a lista por uma nova e atualiza o RecyclerView inteiro
    // Usado na busca para exibir apenas os resultados filtrados
    public void atualizarLista(List<Resenha> novaLista) {
        lista = novaLista;
        notifyDataSetChanged();
    }
}