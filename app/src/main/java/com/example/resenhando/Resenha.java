package com.example.resenhando;

public class Resenha {
    private int id;
    private String titulo;
    private float qntEstrelas;
    private String caminhoImagem;
    private String descricao;

    public Resenha(int id, String titulo, float qntEstrelas, String caminhoImagem, String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.qntEstrelas = qntEstrelas;
        this.caminhoImagem = caminhoImagem;
        this.descricao = descricao;
    }

    public int getId() { return id; }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public float getQntEstrelas() {
        return qntEstrelas;
    }
}
