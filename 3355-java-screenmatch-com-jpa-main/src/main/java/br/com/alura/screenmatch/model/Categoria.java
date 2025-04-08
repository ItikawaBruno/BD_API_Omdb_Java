package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action","Ação"),
    COMEDIA("Comedy", "Comédia"),
    DRAMA("Drama","Drama"),
    ROMANCE("Romance","Romance"),
    CRIME("Crime","Crime");

    private String categoriaOmdb;

    private String categoriaProtugues;

    Categoria(String categoriaOmdb, String categoriaProtugues){
        this.categoriaOmdb=categoriaOmdb;
        this.categoriaProtugues = categoriaProtugues;
    }


    public static Categoria fromString(String texto){
        for (Categoria categoria : Categoria.values()){
            if (categoria.categoriaOmdb.equalsIgnoreCase(texto)){
                return categoria;
            }
        }
        throw  new IllegalArgumentException("Nenhuma categoria encontrada.");
    }

    public static Categoria fromPortugues(String texto){
        for (Categoria categoria : Categoria.values()){
            if (categoria.categoriaProtugues.equalsIgnoreCase(texto)){
                return categoria;
            }
        }
        throw  new IllegalArgumentException("Nenhuma categoria encontrada.");
    }

}
