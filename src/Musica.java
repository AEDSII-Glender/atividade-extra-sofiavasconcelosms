import java.util.Objects;

public class Musica implements Comparable<Musica> {

    private static int ultimoID = 10000;

    private int id;
    private String titulo;
    private String artista;
    private double duracao;

    public Musica(String titulo, String artista, double duracao) {

        if ((titulo.length() >= 3) && (artista.length() >= 3) && (duracao > 0.0)) {
            this.titulo = titulo;
            this.artista = artista;
            this.duracao = duracao;
            this.id = ultimoID++;
        } else {
            throw new IllegalArgumentException("Dados inválidos para a música. Verifique título, artista e duração.");
        }
    }

    public static Musica criarDoTexto(String linha) throws IllegalArgumentException {
        String[] dados = linha.split(";");

        if (dados.length != 3) {
            throw new IllegalArgumentException(
                    "Linha de arquivo em formato inválido: esperado 3 campos. Linha: " + linha);
        }

        String titulo = dados[0].trim();
        String artista = dados[1].trim();
        double duracao;

        try {
            duracao = Double.parseDouble(dados[2].trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Duração inválida na linha: " + linha + " -> " + dados[2]);
        }
        return new Musica(titulo, artista, duracao);
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getArtista() {
        return artista;
    }

    public double getDuracao() {
        return duracao;
    }

    @Override

    public String toString() {

        return String.format("%d - %s, por %s (Duração: %.2f)",
                id, titulo, artista, duracao);
    }

    @Override

    public int hashCode() {
        return id;
    }

    @Override

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Musica outraMusica = (Musica) obj;
        return id == outraMusica.id;
    }

    @Override

    public int compareTo(Musica outra) {
        return this.titulo.compareTo(outra.titulo);
    }
}