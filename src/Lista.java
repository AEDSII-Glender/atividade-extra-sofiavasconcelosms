import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public class Lista<E extends Comparable<E>> implements IMedicao {

    private Celula<E> primeiro;
    private Celula<E> ultimo;
    private int tamanho;
    private long comparacoes;
    private long inicio;
    private long termino;

    private Celula<E> atual;

    public Lista() {

        Celula<E> sentinela = new Celula<>();

        this.primeiro = this.ultimo = sentinela;
        this.tamanho = 0;
        this.atual = null;
    }

    public boolean vazia() {
        return (this.primeiro == this.ultimo);
    }

    public void inserir(E novo, int posicao) {

        Celula<E> anterior, novaCelula, proximaCelula;

        if ((posicao < 0) || (posicao > this.tamanho))
            throw new IndexOutOfBoundsException("Não foi possível inserir o item: a posição é inválida!");

        anterior = this.primeiro;
        for (int i = 0; i < posicao; i++)
            anterior = anterior.getProximo();

        novaCelula = new Celula<>(novo);

        proximaCelula = anterior.getProximo();

        novaCelula.setAnterior(anterior);

        novaCelula.setProximo(proximaCelula);

        anterior.setProximo(novaCelula);

        if (proximaCelula != null) {
            proximaCelula.setAnterior(novaCelula);
        }

        if (posicao == this.tamanho)
            this.ultimo = novaCelula;

        this.tamanho++;
    }

    public void inserirFinal(E novo) {

        Celula<E> novaCelula = new Celula<>(novo);

        this.ultimo.setProximo(novaCelula);

        novaCelula.setAnterior(this.ultimo);

        this.ultimo = novaCelula;

        this.tamanho++;
    }

    private E removerProxima(Celula<E> anterior) {

        Celula<E> celulaRemovida, proximaCelula;

        celulaRemovida = anterior.getProximo();

        if (celulaRemovida == null) {
            throw new NoSuchElementException("Não há próxima célula para remover.");
        }

        proximaCelula = celulaRemovida.getProximo();

        anterior.setProximo(proximaCelula);

        if (proximaCelula != null) {
            proximaCelula.setAnterior(anterior);
        }

        celulaRemovida.setProximo(null);
        celulaRemovida.setAnterior(null);

        if (celulaRemovida == this.ultimo)
            this.ultimo = anterior;

        this.tamanho--;

        return (celulaRemovida.getItem());
    }

    public E remover(int posicao) {

        Celula<E> anterior;

        if (vazia())
            throw new IllegalStateException("A lista está vazia!");

        if ((posicao < 0) || (posicao >= this.tamanho))
            throw new IndexOutOfBoundsException("A posição informada é inválida!");

        anterior = this.primeiro;
        for (int i = 0; i < posicao; i++)
            anterior = anterior.getProximo();

        return (removerProxima(anterior));
    }

    public E remover(E elemento) {

        Celula<E> anterior;

        if (vazia())
            throw new IllegalStateException("A lista está vazia!");

        anterior = this.primeiro;
        while ((anterior.getProximo() != null) && !(anterior.getProximo().getItem().equals(elemento)))
            anterior = anterior.getProximo();

        if (anterior.getProximo() == null)
            throw new NoSuchElementException("Item não encontrado!");
        else {
            return (removerProxima(anterior));
        }
    }

    public E obterMusica(int posicao) {

        if (vazia())
            throw new IllegalStateException("A lista está vazia!");

        if ((posicao < 0) || (posicao >= this.tamanho))
            throw new IndexOutOfBoundsException("A posição informada é inválida!");

        Celula<E> aux = this.primeiro.getProximo();
        for (int i = 0; i < posicao; i++) {
            aux = aux.getProximo();
        }
        return aux.getItem();
    }

    public E pesquisar(E procurado) {

        Celula<E> aux;
        comparacoes = 0;
        inicio = System.nanoTime();

        aux = this.primeiro.getProximo();

        while (aux != null) {
            comparacoes++;
            if (aux.getItem().equals(procurado)) {
                termino = System.nanoTime();
                return aux.getItem();
            }
            aux = aux.getProximo();
        }

        throw new NoSuchElementException("Item não encontrado!");
    }

    public void ordenarPorParticionamento(Comparator<E> comparador) {
        inicio = System.nanoTime();
        comparacoes = 0;

        if (tamanho > 1) {
            quickSort(this.primeiro.getProximo(), this.ultimo, comparador);
        }
        termino = System.nanoTime();
    }

    public void ordenarIterativo(Comparator<E> comparador) {
        if (tamanho < 2)
            return;

        boolean trocou;
        do {
            trocou = false;
            Celula<E> atual = primeiro.getProximo();

            while (atual != null && atual.getProximo() != null) {
                if (comparador.compare(atual.getItem(), atual.getProximo().getItem()) > 0) {

                    E temp = atual.getItem();
                    atual.setItem(atual.getProximo().getItem());
                    atual.getProximo().setItem(temp);

                    trocou = true;
                }
                atual = atual.getProximo();
            }
        } while (trocou);
    }

    private void quickSort(Celula<E> inicio, Celula<E> fim, Comparator<E> comparador) {
        if (fim != null && inicio != fim.getProximo() && inicio != fim) {

            Celula<E> pivoCelula = particionar(inicio, fim, comparador);
            quickSort(inicio, pivoCelula.getAnterior(), comparador);
            quickSort(pivoCelula.getProximo(), fim, comparador);
        }
    }

    private Celula<E> particionar(Celula<E> inicio, Celula<E> fim, Comparator<E> comparador) {

        E pivo = fim.getItem(); 
        Celula<E> i = inicio.getAnterior(); 
        for (Celula<E> j = inicio; j != fim; j = j.getProximo()) {
            comparacoes++;
            if (comparador.compare(j.getItem(), pivo) <= 0) { 
                if (i == null || i == this.primeiro) {
                    i = inicio;
                } else {
                    i = i.getProximo();
                }
                E temp = i.getItem();
                i.setItem(j.getItem());
                j.setItem(temp);
            }
        }
        if (i == null || i == this.primeiro) {
            i = inicio;
        } else {
            i = i.getProximo();
        }
        E temp = i.getItem();
        i.setItem(fim.getItem());
        fim.setItem(temp);

        return i;
    }

    public Celula<E> getPrimeiraMusica() {
        this.atual = this.primeiro.getProximo();
        return this.atual;
    }

    public Celula<E> getUltimaMusica() {
        this.atual = this.ultimo;
        return this.atual;
    }

    public Celula<E> getProximaMusica() {
        if (this.atual != null && this.atual.getProximo() != null) {
            this.atual = this.atual.getProximo();
            return this.atual;
        }
        return null;
    }

    public Celula<E> getMusicaAnterior() {
        if (this.atual != null && this.atual.getAnterior() != this.primeiro) {
            this.atual = this.atual.getAnterior();
            return this.atual;
        }
        return null;
    }

    @Override
    public String toString() {

        Celula<E> aux;
        String listaString = "A playlist está vazia!\n";

        if (!vazia()) {
            listaString = "--- PLAYLIST ATUAL ---\n";

            aux = this.primeiro.getProximo();

            while (aux != null) {
                listaString += aux.getItem() + "\n";
                aux = aux.getProximo();
            }
            listaString += "----------------------\n";
        }
        return listaString;
    }

    public int tamanho() {
        return tamanho;
    }

    // Métodos IMedicao
    public long getComparacoes() {
        return comparacoes;
    }

    public double getTempo() {
        return (termino - inicio) / 1_000_000.0;
    }

    public int contarRepeticoes(Predicate<E> condicional) {
        int repeticoes = 0;
        Celula<E> aux = primeiro.getProximo();
        while (aux != null) {
            if (condicional.test(aux.getItem())) {
                repeticoes++;
            }
            aux = aux.getProximo();
        }
        return repeticoes;
    }

    public double calcularValorTotal(Function<E, Double> extrator) {

        Celula<E> aux;
        double soma = 0;

        if (vazia())
            throw new IllegalStateException("A lista está vazia!");

        aux = primeiro.getProximo();
        while (aux != null) {
            soma += extrator.apply(aux.getItem());
            aux = aux.getProximo();
        }
        return (soma);
    }
}