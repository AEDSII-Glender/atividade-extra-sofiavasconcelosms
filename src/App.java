import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.EmptyStackException;

public class App {

    private static Lista<Musica> playlist;
    private static Historico historico;
    private static ABB<Integer, Musica> indexadorPorId;

    private static Scanner teclado;
    private static final String nomeArquivoDados = "musicas.txt";
    private static int quantasMusicas = 0;

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("\nDigite ENTER para continuar...");
        teclado.nextLine();
    }

    static void cabecalho() {
        System.out.println("AEDs II - SISTEMA DE PLAYLIST");
        System.out.println("============================");
    }

    static <T extends Number> T lerOpcao(String msg, Class<T> classe) {
        System.out.print(msg + " ");
        try {
            return classe.getConstructor(String.class)
                    .newInstance(teclado.nextLine());
        } catch (Exception e) {
            return null;
        }
    }

    static Integer lerOpcao(String msg) {
        return lerOpcao(msg, Integer.class);
    }

    private static void carregarMusicas() {
        indexadorPorId = new ABB<>();
        quantasMusicas = 0;

        System.out.printf("\nCarregando músicas do arquivo '%s'...\n", nomeArquivoDados);

        try (Scanner arquivo = new Scanner(
                new File(nomeArquivoDados), Charset.forName("UTF-8"))) {

            if (arquivo.hasNextLine()) {
                arquivo.nextLine();
            }

            while (arquivo.hasNextLine()) {
                String linha = arquivo.nextLine();
                Musica musica = Musica.criarDoTexto(linha);

                playlist.inserirFinal(musica);
                indexadorPorId.inserir(musica.getId(), musica);
                quantasMusicas++;
            }

            System.out.printf("%d músicas carregadas com sucesso!\n", quantasMusicas);

        } catch (IOException e) {
            System.err
                    .println("Erro ao ler o arquivo de músicas. Verifique se 'musicas.txt' existe na raiz do projeto.");
        } catch (Exception e) {
            System.err.println("Erro ao processar dados do arquivo: " + e.getMessage());
            System.err.println("Certifique-se que o método Musica.criarDoTexto(String) está correto.");
        }
    }

    private static void adicionarMusicaManualmente() {
        limparTela();
        cabecalho();
        System.out.println("--- ADICIONAR NOVA MÚSICA ---");

        System.out.print("Título: ");
        String titulo = teclado.nextLine();
        System.out.print("Artista: ");
        String artista = teclado.nextLine();
        Double duracao = lerOpcao("Duração (em minutos, ex: 3.50):", Double.class);

        if (duracao == null) {
            System.out.println("Duração inválida. Operação cancelada.");
            return;
        }

        try {
            Musica novaMusica = new Musica(titulo, artista, duracao);

            playlist.inserirFinal(novaMusica);
            indexadorPorId.inserir(novaMusica.getId(), novaMusica);
            quantasMusicas++;

            System.out.printf("Música '%s' adicionada e indexada (ID: %d).\n", titulo, novaMusica.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao adicionar música: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro ao indexar música: " + e.getMessage());
        }
    }

    private static void ordenarPlaylist() {
        limparTela();
        cabecalho();

        System.out.println("1 - Título (A-Z)");
        System.out.println("2 - Artista (A-Z)");
        System.out.println("3 - Duração (Crescente)");
        System.out.println("0 - Cancelar");

        Integer op = lerOpcao("Escolha:");
        if (op == null || op == 0)
            return;

        Comparator<Musica> comp;
        switch (op) {
            case 1 -> comp = Comparator.comparing(Musica::getTitulo);
            case 2 -> comp = Comparator.comparing(Musica::getArtista);
            case 3 -> comp = Comparator.comparing(Musica::getDuracao);
            default -> {
                System.out.println("Opção inválida.");
                return;
            }
        }

        playlist.ordenarPorParticionamento(comp);
        System.out.println("Playlist ordenada com sucesso!");
        System.out.println("Comparações: " + playlist.getComparacoes());
        System.out.println("Tempo: " + playlist.getTempo() + " ms");
    }

    private static void reproduzirEmOrdem() {
        if (playlist.vazia()) {
            System.out.println("Playlist vazia.");
            return;
        }
        Celula<Musica> atual = playlist.getPrimeiraMusica();
        historico.adicionarMusica(atual.getItem());

        Integer op;
        do {
            limparTela();
            cabecalho();
            System.out.println(" Reproduzindo: " + atual.getItem());

            System.out.println("\n1 - Próxima");
            System.out.println("2 - Anterior");
            System.out.println("0 - Parar");

            op = lerOpcao("Escolha:");
            if (op == null)
                continue;

            if (op == 1) {
                Celula<Musica> proxima = playlist.getProximaMusica();
                if (proxima != null) {
                    atual = proxima;
                    historico.adicionarMusica(atual.getItem());
                } else {
                    System.out.println("\nPlaylist finalizada. Deseja reiniciar? (S/N)");
                    String resp = teclado.nextLine();
                    if (resp.equalsIgnoreCase("s")) {
                        atual = playlist.getPrimeiraMusica();
                        historico.adicionarMusica(atual.getItem());
                    } else {
                        op = 0;
                    }
                }
            } else if (op == 2) {
                Celula<Musica> anterior = playlist.getMusicaAnterior();
                if (anterior != null) {
                    atual = anterior;
                    historico.adicionarMusica(atual.getItem());
                } else {
                    System.out.println("\nVocê já está no início da playlist.");
                    pausa();
                    // Garante que 'atual' aponta para o início
                    atual = playlist.getPrimeiraMusica();
                }
            }
        } while (op != 0);
        System.out.println("\nReprodução sequencial parada.");
    }

    private static void reproduzirAleatoria() {
        limparTela();
        cabecalho();
        Integer id = lerOpcao("Digite o ID da música:");
        if (id == null)
            return;

        try {
            Musica m = indexadorPorId.pesquisar(id);
            System.out.println(" Reproduzindo: " + m);
            historico.adicionarMusica(m);

            System.out.println("Comparações (ABB): " + indexadorPorId.getComparacoes());
            System.out.println("Tempo: " + indexadorPorId.getTempo() + " ms");

        } catch (NoSuchElementException e) {
            System.out.println("Música não encontrada no índice (ID: " + id + ").");
        }
    }

    private static void voltarReproducao() {
        limparTela();
        cabecalho();
        try {
            historico.voltarReproducao();

            Musica anterior = historico.voltarReproducao();
            historico.adicionarMusica(anterior);

            System.out.println("Voltando para: " + anterior);
        } catch (EmptyStackException e) {
            System.out.println("Histórico de reprodução vazio ou insuficiente para voltar uma música.");
        }
    }

    private static void exibirHistorico() {
        limparTela();
        cabecalho();
        System.out.println(historico.exibirHistorico());
    }

    private static void exibirPlaylist() {
        limparTela();
        cabecalho();
        System.out.println(playlist.toString());
    }

    static int menu() {
        limparTela();
        cabecalho();
        System.out.println("\n1 - Exibir Playlist");
        System.out.println("2 - Ordenar Playlist");
        System.out.println("3 - Reproduzir em Ordem (Navegação)");
        System.out.println("4 - Reproduzir por ID (Busca ABB)");
        System.out.println("5 - Voltar Reprodução (Histórico)");
        System.out.println("6 - Exibir Histórico Completo");
        System.out.println("7 - Adicionar Música Manualmente");
        System.out.println("0 - Sair");

        Integer op = lerOpcao("Opção:");
        return (op == null) ? -1 : op;
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        playlist = new Lista<>();
        historico = new Historico();

        carregarMusicas();

        int op;
        do {
            op = menu();
            switch (op) {
                case 1 -> exibirPlaylist();
                case 2 -> ordenarPlaylist();
                case 3 -> reproduzirEmOrdem();
                case 4 -> reproduzirAleatoria();
                case 5 -> voltarReproducao();
                case 6 -> exibirHistorico();
                case 7 -> adicionarMusicaManualmente();
                case 0 -> System.out.println("Encerrando...");
                default -> {
                    System.out.println("Opção inválida!");
                }
            }
            if (op != 0)
                pausa();
        } while (op != 0);

        teclado.close();
    }
}