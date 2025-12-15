import java.util.EmptyStackException;

public class Historico {

    private class NoHistorico {
        Musica item;
        NoHistorico proximo;

        public NoHistorico(Musica item) {
            this.item = item;
            this.proximo = null;
        }
    }

    private NoHistorico topo; 
    private int tamanho;

    public Historico() {
        this.topo = null;
        this.tamanho = 0;
    }

    public boolean estaVazio() {
        return this.topo == null;
    }

    public void adicionarMusica(Musica musica) {
        NoHistorico novoNo = new NoHistorico(musica);
        novoNo.proximo = this.topo;
        this.topo = novoNo;
        this.tamanho++;
        
    }

    public Musica voltarReproducao() {
        if (estaVazio()) {
            throw new EmptyStackException();
        }
        Musica musicaAnterior = this.topo.item;
        this.topo = this.topo.proximo;
        this.tamanho--;
        
        // 
        return musicaAnterior;
    }
    
    public int tamanho() {
        return tamanho;
    }

    public String exibirHistorico() {
        if (estaVazio()) {
            return "O histórico de reprodução está vazio.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("--- HISTÓRICO DE REPRODUÇÃO ---\n");
        sb.append("(Ordem: Mais Recente para Mais Antiga)\n");

        NoHistorico atual = this.topo;
        int i = 1;
        while (atual != null) {
            sb.append(String.format("%d. %s\n", i++, atual.item.toString()));
            atual = atual.proximo;
        }
        sb.append("-----------------------------\n");
        return sb.toString();
    }
}