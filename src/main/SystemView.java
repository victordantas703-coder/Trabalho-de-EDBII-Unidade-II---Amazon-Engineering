import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet; // usada para conferir os resultados
import java.util.function.Supplier;
import no.NoAVL;
import arvores.ArvoreAVL;
import arvores.ArvoreRubroNegra;
import arvores.Arvore;

/**
 * Classe principal do programa.
 *
 *   - Menu em que o usuário escolhe, em tempo de execução, qual árvore usar
 *     (AVL ou Rubro-Negra) e opera sobre ela de forma interativa;
 *   - Bateria de testes automáticos executada sobre as DUAS árvores, com os
 *     mesmos dados e as mesmas sequências de operações;
 *   - Comparativo de desempenho (tempo de inserção, busca e remoção, altura
 *     final, rotações e ajustes de balanceamento);
 *   - Demonstração visual.
 *
 * Argumentos: --testes, --n=VALOR, --sem-cor, --classica, --largura=VALOR, --ajuda
 */
public class SystemView {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final Scanner ENTRADA = new Scanner(System.in);

    private static final Supplier<Arvore> FABRICA_AVL = ArvoreAVL::new;
    private static final Supplier<Arvore> FABRICA_RN = ArvoreRubroNegra::new;
    private static final List<Supplier<Arvore>> FABRICAS = List.of(FABRICA_AVL, FABRICA_RN);

    private static int tamanhoBenchmark = 100_000;

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        Arvore.setRenderizador(Visualizador::desenhar); // liga as árvores ao visualizador
        Visualizador.larguraTerminal = Visualizador.detectarLargura();
        if (System.getenv("NO_COLOR") != null) Visualizador.usarCor = false;

        boolean soTestes = false;
        boolean estiloPorArgumento = false;
        for (String a : args) {
            if (a.equals("--testes")) soTestes = true;
            else if (a.equals("--sem-cor")) Visualizador.usarCor = false;
            else if (a.equals("--classica")) { Visualizador.modoBonsai = false; estiloPorArgumento = true; }
            else if (a.equals("--bonsai")) { Visualizador.modoBonsai = true; estiloPorArgumento = true; }
            else if (a.startsWith("--n=")) tamanhoBenchmark = Math.max(10, Integer.parseInt(a.substring(4)));
            else if (a.startsWith("--largura=")) Visualizador.larguraTerminal = Math.max(40, Integer.parseInt(a.substring(10)));
            else if (a.equals("--ajuda")) { ajuda(); return; }
        }

        if (soTestes) {
            executarTestes();
            comparativo(tamanhoBenchmark);
            System.exit(reprovados == 0 ? 0 : 1);
        }
        limparTela();
        if (!estiloPorArgumento) escolherEstilo();
        menuPrincipal();
    }

    private static void ajuda() {
        System.out.println("Uso: java SystemView [opções]");
        System.out.println("  --testes        executa testes + comparativo e sai (código de saída 1 se algum teste falhar)");
        System.out.println("  --n=VALOR       tamanho dos conjuntos do comparativo (padrão 100000)");
        System.out.println("  --sem-cor       desativa cores ANSI (rubro-negras: (7)=vermelho, [7]=preto)");
        System.out.println("  --bonsai        usa o estilo bonsai sem perguntar (raiz embaixo)");
        System.out.println("  --classica      usa o estilo clássico sem perguntar (raiz no topo)");
        System.out.println("  --largura=N     largura do terminal em colunas (detectada automaticamente)");
    }

    private static String ler(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return ENTRADA.hasNextLine() ? ENTRADA.nextLine().trim() : null;
    }

    private static String lerOpcao(String prompt) {
        String s = ler(prompt);
        if (s != null) limparTela();
        return s;
    }

    private static void limparTela() {
        System.out.flush();
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                return;
            }
        } catch (Exception ignorado) { /* usa a sequência ANSI abaixo */ }
        System.out.print("\u001B[H\u001B[2J\u001B[3J");
        System.out.flush();
    }

    private static int lerInteiro(String prompt, int padrao, int min, int max) {
        String s = ler(prompt);
        if (s == null || s.isEmpty()) return padrao;
        try {
            int v = Integer.parseInt(s);
            if (v >= min && v <= max) return v;
        } catch (NumberFormatException ignorado) { /* cai no aviso */ }
        System.out.println("  Valor inválido; usando " + padrao + ".");
        return padrao;
    }

    /** Converte uma linha como "10 20 x 30" em inteiros, avisando sobre os inválidos. */
    private static List<Integer> lerValores(String prompt) {
        String linha = ler(prompt);
        List<Integer> valores = new ArrayList<>();
        if (linha == null || linha.isEmpty()) return valores;
        for (String token : linha.split("[\\s,;]+")) {
            try {
                valores.add(Integer.parseInt(token));
            } catch (NumberFormatException e) {
                System.out.println("  Ignorando \"" + token + "\" (não é um inteiro).");
            }
        }
        return valores;
    }

    private static String formatarTempo(long ns) {
        if (ns < 1_000) return ns + " ns";
        if (ns < 1_000_000) return String.format(BR, "%.2f µs", ns / 1e3);
        if (ns < 1_000_000_000L) return String.format(BR, "%.3f ms", ns / 1e6);
        return String.format(BR, "%.3f s", ns / 1e9);
    }

    private static String ms(long ns) {
        return String.format(BR, "%,.3f", ns / 1e6);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println();
        System.out.println(" >- " + texto);
        System.out.println();
    }

    private static void menuPrincipal() {
        while (true) {
            titulo("ÁRVORES BALANCEADAS: AVL x RUBRO-NEGRA");
            System.out.println("  1) Modo interativo (escolher a árvore e operar sobre ela)");
            System.out.println("  2) Executar a bateria de testes nas duas árvores");
            System.out.println("  3) Comparativo de desempenho (tempos, altura, rotações, ajustes)");
            System.out.println("  0) Sair");
            String op = lerOpcao("Escolha: ");
            if (op == null) return;
            switch (op) {
                case "1": modoInterativo(); break;
                case "2": executarTestes(); break;
                case "3": comparativo(lerInteiro("Tamanho dos conjuntos (Padrão: " + tamanhoBenchmark + "): ",
                        tamanhoBenchmark, 10, 5_000_000)); break;
                case "0": return;
                default: System.out.println("  Opção inválida.");
            }
        }
    }

    private static void escolherEstilo() {
        titulo("Como deseja visualizar a árvore?");
        System.out.println();
        System.out.println("  1) Clássico: a raiz fica no topo");
        exemploEstilo(false);
        System.out.println("  2) Bonsai: a raiz fica embaixo e a árvore cresce para cima");
        exemploEstilo(true);
        System.out.println("  Obs.: árvores largas demais para a janela são desenhadas na horizontal automaticamente.");
        System.out.println();
        while (true) {
            String op = ler("Escolha (Padrão: 1): ");
            if (op == null || op.isEmpty() || op.equals("1")) { Visualizador.modoBonsai = false ; break; }
            if (op.equals("2")) { Visualizador.modoBonsai = true; break; }
            System.out.println("  Opção inválida.");
        }
        limparTela();
    }

    private static void exemploEstilo(boolean bonsai) {
        boolean anterior = Visualizador.modoBonsai;
        Visualizador.modoBonsai = bonsai;
        NoAVL raiz = new NoAVL(4);
        raiz.esquerda = new NoAVL(2);
        raiz.direita = new NoAVL(6);
        raiz.esquerda.esquerda = new NoAVL(1);
        raiz.esquerda.direita = new NoAVL(3);
        Visualizador.desenhar(raiz, "Exemplo", false);
        Visualizador.modoBonsai = anterior;
    }

    /** Tempo acumulado por tipo de operação na sessão interativa. */
    private static final class Metricas {
        final long[] ns = new long[3];     // 0 = inserção, 1 = busca, 2 = remoção
        final long[] qtd = new long[3];
        void somar(int tipo, long dur) { ns[tipo] += dur; qtd[tipo]++; }
    }

    private static Arvore escolherArvore() {
        titulo("Qual árvore deseja utilizar?");
        System.out.println("  1) Árvore AVL");
        System.out.println("  2) Árvore Rubro-Negra");
        System.out.println("  0) Voltar");
        while (true) {
            String op = lerOpcao("Escolha: ");
            if (op == null || op.equals("0")) return null;
            if (op.equals("1")) return FABRICA_AVL.get();
            if (op.equals("2")) return FABRICA_RN.get();
            System.out.println("  Opção inválida.");
        }
    }

    private static void modoInterativo() {
        Arvore arvore = escolherArvore();
        if (arvore == null) return;
        Metricas m = new Metricas();

        while (true) {
            titulo("Modo interativo: " + arvore.getNome()
                    + "  (" + arvore.getQuantidadeNos() + " nós)");
            System.out.println("   1) Inserir valor(es)            5) Visualizar a árvore");
            System.out.println("   2) Remover valor(es)            6) Estatísticas e tempos");
            System.out.println("   3) Buscar valor                 7) Inserir N valores aleatórios");
            System.out.println("   4) Percurso em ordem            8) Limpar a árvore");
            System.out.println("                                   9) Trocar de árvore");
            System.out.println("   0) Voltar ao menu principal");
            String op = lerOpcao("Escolha: ");
            if (op == null || op.equals("0")) return;

            switch (op) {
                case "1": interativoInserir(arvore, m); break;
                case "2": interativoRemover(arvore, m); break;
                case "3": interativoBuscar(arvore, m); break;
                case "4": {
                    List<Integer> lista = arvore.emOrdem();
                    System.out.println("  Em ordem (" + lista.size() + " elementos): "
                            + (lista.isEmpty() ? "(vazia)" : lista.toString()));
                    break;
                }
                case "5": arvore.imprimir(); break;
                case "6": estatisticas(arvore, m); break;
                case "7": interativoAleatorios(arvore, m); break;
                case "8": arvore.limpar(); arvore.resetarContadores(); m = new Metricas();
                    System.out.println("  Árvore esvaziada e contadores zerados."); break;
                case "9": {
                    Arvore outra = escolherArvore();
                    if (outra != null) { arvore = outra; m = new Metricas(); }
                    break;
                }
                default: System.out.println("  Opção inválida.");
            }
        }
    }

    private static void interativoInserir(Arvore arvore, Metricas m) {
        List<Integer> valores = lerValores("  Valor(es) a inserir (separados por espaço): ");
        if (valores.isEmpty()) return;
        long total = 0;
        int inseridos = 0;
        for (int v : valores) {
            long t0 = System.nanoTime();
            boolean inserido = arvore.inserir(v);
            long dur = System.nanoTime() - t0;
            if (!inserido) {
                System.out.println("  ! " + v + " já existe na árvore: inserção impedida");
                continue;
            }
            m.somar(0, dur);
            total += dur;
            inseridos++;
            if (valores.size() <= 12) System.out.println("  + " + v + " inserido");
        }
        System.out.println("  " + inseridos + " inserção(ões) em " + formatarTempo(total));
        arvore.imprimir();
    }

    private static void interativoRemover(Arvore arvore, Metricas m) {
        List<Integer> valores = lerValores("  Valor(es) a remover (separados por espaço): ");
        if (valores.isEmpty()) return;
        long total = 0;
        for (int v : valores) {
            long t0 = System.nanoTime();
            boolean ok = arvore.remover(v);
            long dur = System.nanoTime() - t0;
            m.somar(2, dur);
            total += dur;
            if (valores.size() <= 12) {
                System.out.println(ok ? "  - " + v + " removido"
                        : "  ! " + v + " não existe na árvore: nada foi alterado");
            }
        }
        System.out.println("  " + valores.size() + " remoção(ões) em " + formatarTempo(total));
        arvore.imprimir();
    }

    private static void interativoBuscar(Arvore arvore, Metricas m) {
        List<Integer> valores = lerValores("  Valor(es) a buscar: ");
        for (int v : valores) {
            long t0 = System.nanoTime();
            boolean achou = arvore.buscar(v);
            long dur = System.nanoTime() - t0;
            m.somar(1, dur);
            System.out.println(achou
                    ? "  > " + v + " encontrado em " + formatarTempo(dur)
                    : "  ! " + v + " não encontrado (" + formatarTempo(dur) + ")");
        }
    }

    private static void interativoAleatorios(Arvore arvore, Metricas m) {
        int n = lerInteiro("  Quantos valores aleatórios (Padrão: 10): ", 10, 1, 5_000_000);
        int max = lerInteiro("  Valor máximo (Padrão: 99): ", 99, 1, 100_000_000);
        Random r = new Random();
        long total = 0;
        int inseridos = 0;
        for (int i = 0; i < n; i++) {
            int v = 1 + r.nextInt(max);
            long t0 = System.nanoTime();
            boolean inserido = arvore.inserir(v);
            long dur = System.nanoTime() - t0;
            if (!inserido) continue;
            m.somar(0, dur);
            total += dur;
            inseridos++;
        }
        System.out.println("  " + inseridos + " inserções aleatórias em " + formatarTempo(total));
        if (inseridos < n) {
            System.out.println("  ! " + (n - inseridos) + " valor(es) sorteado(s) já existia(m) na árvore: inserção impedida");
        }
        arvore.imprimir();
    }

    private static void estatisticas(Arvore arvore, Metricas m) {
        System.out.println("  ── " + arvore.getNome() + " ──");
        System.out.println("  Nós ........................ " + arvore.getQuantidadeNos());
        System.out.println("  Altura final ............... " + arvore.getAlturaFinal()
                + "  (limite teórico para esta quantidade de nós: " + arvore.limiteTeoricoAltura() + ")");
        System.out.println("  Rotações ................... " + arvore.getRotacoes());
        System.out.println("  Ajustes de balanceamento ... " + arvore.getAjustesBalanceamento());
        String[] nomes = {"Inserções", "Buscas", "Remoções"};
        for (int i = 0; i < 3; i++) {
            if (m.qtd[i] == 0) continue;
            System.out.println("  " + String.format("%-10s", nomes[i]) + " " + m.qtd[i] + " op. em "
                    + formatarTempo(m.ns[i]) + "  (média " + formatarTempo(m.ns[i] / m.qtd[i]) + "/op.)");
        }
    }

    private static int aprovados = 0;
    private static int reprovados = 0;

    private static void verificar(String descricao, boolean ok) {
        if (ok) {
            aprovados++;
            System.out.println("  " + (Visualizador.usarCor ? "\u001B[32m>\u001B[0m" : ">") + " " + descricao);
        } else {
            reprovados++;
            System.out.println("  " + (Visualizador.usarCor ? "\u001B[31m! FALHOU:\u001B[0m" : "! FALHOU:") + " " + descricao);
        }
    }

    private static void verificar(String descricao, String erro) {
        verificar(erro == null ? descricao : descricao + "  - " + erro, erro == null);
    }

    /** Estrutura de referência (conjunto da biblioteca padrão) usada só para conferir os resultados. */
    private static final class Referencia {
        private final TreeSet<Integer> conjunto = new TreeSet<>();
        void inserir(int v) { conjunto.add(v); }
        boolean remover(int v) { return conjunto.remove(v); }
        boolean contem(int v) { return conjunto.contains(v); }
        int tamanho() { return conjunto.size(); }
        List<Integer> lista() { return new ArrayList<>(conjunto); }
    }

    /** Confere a árvore contra a referência. Devolve null se tudo bem, ou a descrição do problema. */
    private static String estadoConsistente(Arvore t, Referencia ref) {
        if (!t.verificarPropriedades()) return "propriedade violada: " + t.getErroVerificacao();
        if (!t.emOrdem().equals(ref.lista())) return "percurso em ordem diferente do esperado";
        if (t.getQuantidadeNos() != ref.tamanho()) return "quantidade de nós incorreta";
        int h = t.altura();
        if (h > t.limiteTeoricoAltura()) return "altura " + h + " acima do limite teórico " + t.limiteTeoricoAltura();
        return null;
    }

    private static List<Integer> sequencia(int n, String tipo, long semente) {
        List<Integer> l = new ArrayList<>();
        for (int i = 1; i <= n; i++) l.add(i);
        if (tipo.equals("decrescente")) Collections.reverse(l);
        if (tipo.equals("aleatoria")) Collections.shuffle(l, new Random(semente));
        return l;
    }

    private static void executarTestes() {
        aprovados = 0;
        reprovados = 0;
        for (Supplier<Arvore> f : FABRICAS) {
            Arvore amostra = f.get();
            titulo("TESTES: " + amostra.getNome());
            testeInsercao(f, "crescente", 1000);
            testeInsercao(f, "decrescente", 1000);
            testeInsercao(f, "aleatoria", 1000);
            testeBusca(f);
            testeRemocaoConfiguracoes(f);
            testeRemocaoInexistente(f);
            testeUmElemento(f);
            testeArvoreVazia(f);
            testeOperacoesMistas(f);
            testeRemocaoTotal(f);
        }
        testeEquivalencia();
        testeVisualizacao();

        titulo("RESULTADO DOS TESTES");
        System.out.println("  Aprovados: " + aprovados + "   Reprovados: " + reprovados);
        System.out.println(reprovados == 0 ? "  Todas as verificações passaram." : "  HÁ FALHAS: confira as linhas marcadas com \"! FALHOU\".");
    }

    private static void testeInsercao(Supplier<Arvore> f, String tipo, int n) {
        System.out.println("\n [Inserção em ordem " + (tipo.equals("aleatoria") ? "não ordenada" : tipo) + ", n=" + n + "]");
        Arvore t = f.get();
        Referencia ref = new Referencia();
        String erro = null;
        for (int v : sequencia(n, tipo, 7)) {
            t.inserir(v);
            ref.inserir(v);
            erro = estadoConsistente(t, ref); // verifica após cada inserção
            if (erro != null) { erro = "após inserir " + v + ": " + erro; break; }
        }
        verificar("Propriedades e percurso em ordem corretos após cada inserção", erro);
        verificar("Altura final " + t.getAlturaFinal() + " ≤ limite teórico " + t.limiteTeoricoAltura(),
                t.getAlturaFinal() <= t.limiteTeoricoAltura());
        verificar("Contadores registrados: rotações=" + t.getRotacoes() + ", ajustes=" + t.getAjustesBalanceamento(),
                t.getRotacoes() > 0 && t.getAjustesBalanceamento() > 0);
    }

    private static void testeBusca(Supplier<Arvore> f) {
        System.out.println("\n [Busca de elementos existentes e inexistentes]");
        Arvore t = f.get();
        for (int v = 0; v < 1000; v += 2) t.inserir(v); // só pares de 0 a 998
        boolean existentes = true, inexistentes = true;
        for (int v = 0; v < 1000; v += 2) existentes &= t.buscar(v);
        for (int v = 1; v < 1000; v += 2) inexistentes &= !t.buscar(v);
        verificar("Os 500 valores inseridos são encontrados", existentes);
        verificar("Os 500 valores ímpares (não inseridos) não são encontrados", inexistentes);
        verificar("Valores fora do intervalo (-5 e 5000) não são encontrados", !t.buscar(-5) && !t.buscar(5000));
        verificar("Extremos (0 e 998) são encontrados", t.buscar(0) && t.buscar(998));
    }

    private static void testeRemocaoConfiguracoes(Supplier<Arvore> f) {
        System.out.println("\n [Remoção de nós com diferentes configurações de filhos]");
        int[] vistos = new int[3]; // folhas, um filho, dois filhos
        String erro = null;
        String[] ordens = {"crescente", "decrescente", "aleatoria"};
        outer:
        for (int n = 1; n <= 24; n++) {
            for (String insercao : ordens) {
                for (String remocao : ordens) {
                    Arvore t = f.get();
                    Referencia ref = new Referencia();
                    for (int v : sequencia(n, insercao, n)) { t.inserir(v); ref.inserir(v); }
                    for (int v : sequencia(n, remocao, n * 31L)) {
                        int cfg = t.configuracaoFilhos(v);
                        vistos[cfg]++;
                        t.remover(v);
                        ref.remover(v);
                        erro = estadoConsistente(t, ref);
                        if (erro != null) {
                            erro = "n=" + n + " (ins. " + insercao + ", rem. " + remocao + ") ao remover " + v + ": " + erro;
                            break outer;
                        }
                    }
                }
            }
        }
        verificar("Propriedades preservadas após cada remoção (216 árvores, todas as ordens)", erro);
        verificar("Foram removidos nós folha (" + vistos[0] + " casos)", vistos[0] > 0);
        verificar("Foram removidos nós com um filho (" + vistos[1] + " casos)", vistos[1] > 0);
        verificar("Foram removidos nós com dois filhos (" + vistos[2] + " casos)", vistos[2] > 0);

        // remoção explícita da raiz de uma árvore conhecida
        Arvore t = f.get();
        Referencia ref = new Referencia();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80, 35, 65}) { t.inserir(v); ref.inserir(v); }
        for (int v : new int[]{50, 70, 30}) { t.remover(v); ref.remover(v); }
        verificar("Remoção de nós internos de uma árvore conhecida", estadoConsistente(t, ref));
    }

    private static void testeRemocaoInexistente(Supplier<Arvore> f) {
        System.out.println("\n [Remoção de elementos inexistentes]");
        Arvore t = f.get();
        Referencia ref = new Referencia();
        for (int v : new int[]{10, 20, 30, 40, 50}) { t.inserir(v); ref.inserir(v); }
        long rotAntes = t.getRotacoes(), ajAntes = t.getAjustesBalanceamento();
        boolean r1 = t.remover(25), r2 = t.remover(-1), r3 = t.remover(99);
        verificar("remover(25), remover(-1) e remover(99) devolvem false", !r1 && !r2 && !r3);
        verificar("A árvore permanece idêntica e válida", estadoConsistente(t, ref));
        verificar("Nenhuma rotação ou ajuste foi feito pela remoção inexistente",
                t.getRotacoes() == rotAntes && t.getAjustesBalanceamento() == ajAntes);

        Arvore vazia = f.get();
        verificar("Remover de uma árvore vazia devolve false e não quebra", !vazia.remover(7) && vazia.estaVazia());
    }

    private static void testeUmElemento(Supplier<Arvore> f) {
        System.out.println("\n [Operações em árvore com apenas um elemento]");
        Arvore t = f.get();
        t.inserir(42);
        verificar("Altura 1, um nó, propriedades válidas", t.altura() == 1 && t.getQuantidadeNos() == 1 && t.verificarPropriedades());
        verificar("Busca encontra 42 e não encontra 41", t.buscar(42) && !t.buscar(41));
        verificar("Percurso em ordem = [42]", t.emOrdem().equals(List.of(42)));
        verificar("Remover valor inexistente (43) não altera a árvore", !t.remover(43) && t.buscar(42));
        verificar("Remover 42 esvazia a árvore (altura 0)", t.remover(42) && t.estaVazia() && t.altura() == 0 && t.verificarPropriedades());
        t.inserir(1);
        verificar("Árvore esvaziada pode ser reutilizada", t.buscar(1) && t.verificarPropriedades());
    }

    private static void testeArvoreVazia(Supplier<Arvore> f) {
        System.out.println("\n [Operações em árvore vazia]");
        Arvore t = f.get();
        verificar("Busca em árvore vazia devolve false", !t.buscar(1));
        verificar("Percurso em ordem é vazio", t.emOrdem().isEmpty());
        verificar("Altura 0 e propriedades válidas", t.altura() == 0 && t.verificarPropriedades());
        System.out.println("  Visualização da árvore vazia:");
        t.imprimir();
    }

    private static void testeOperacoesMistas(Supplier<Arvore> f) {
        System.out.println("\n [Propriedades após inserções e remoções intercaladas (aleatórias)]");
        Arvore t = f.get();
        Referencia ref = new Referencia();
        Random r = new Random(2024);
        String erro = null;
        boolean respostasOk = true;
        for (int i = 0; i < 6000 && erro == null; i++) {
            int v = r.nextInt(150);
            if (r.nextInt(100) < 55) {
                if (!ref.contem(v)) {
                    t.inserir(v);
                    ref.inserir(v);
                }
            } else {
                respostasOk &= t.remover(v) == ref.remover(v);
            }
            respostasOk &= t.buscar(v) == ref.contem(v);
            erro = estadoConsistente(t, ref);
            if (erro != null) erro = "operação " + i + ": " + erro;
        }
        verificar("6000 operações; estado conferido após cada uma", erro);
        verificar("Resultados de remover/buscar coincidem com a referência", respostasOk);
    }

    private static void testeRemocaoTotal(Supplier<Arvore> f) {
        System.out.println("\n [Remover todos os elementos]");
        int n = 2000;
        Arvore t = f.get();
        Referencia ref = new Referencia();
        for (int v : sequencia(n, "aleatoria", 11)) { t.inserir(v); ref.inserir(v); }
        String erro = null;
        int passo = 0;
        for (int v : sequencia(n, "aleatoria", 99)) {
            t.remover(v);
            ref.remover(v);
            if (++passo % 20 == 0 || passo == n) {
                erro = estadoConsistente(t, ref);
                if (erro != null) { erro = "após " + passo + " remoções: " + erro; break; }
            }
        }
        verificar("Esvaziar uma árvore de " + n + " nós mantendo as propriedades", erro);
        verificar("No final a árvore está vazia (altura 0)", t.estaVazia() && t.altura() == 0);
    }

    /** As duas árvores, recebendo exatamente as mesmas operações, devem produzir o mesmo conteúdo. */
    private static void testeEquivalencia() {
        titulo("TESTE COMPARATIVO: mesmas operações nas duas árvores");
        Arvore avl = FABRICA_AVL.get(), rn = FABRICA_RN.get();
        Random r = new Random(555);
        boolean mesmasRespostas = true;
        for (int i = 0; i < 20_000; i++) {
            int v = r.nextInt(500);
            switch (r.nextInt(3)) {
                case 0: if (!avl.buscar(v)) { avl.inserir(v); rn.inserir(v); } break;
                case 1: mesmasRespostas &= avl.remover(v) == rn.remover(v); break;
                default: mesmasRespostas &= avl.buscar(v) == rn.buscar(v);
            }
        }
        verificar("As duas árvores responderam igual a todas as remoções/buscas", mesmasRespostas);
        verificar("Percurso em ordem idêntico (" + avl.getQuantidadeNos() + " elementos)", avl.emOrdem().equals(rn.emOrdem()));
        verificar("Ambas com propriedades válidas", avl.verificarPropriedades() && rn.verificarPropriedades());
        System.out.println("  AVL: altura " + avl.getAlturaFinal() + ", rotações " + avl.getRotacoes()
                + ", ajustes " + avl.getAjustesBalanceamento());
        System.out.println("  RN : altura " + rn.getAlturaFinal() + ", rotações " + rn.getRotacoes()
                + ", ajustes " + rn.getAjustesBalanceamento());
    }

    private static void testeVisualizacao() {
        titulo("VISUALIZAÇÃO: mesma sequência nas duas árvores");
        int[] seq = {8, 3, 10, 1, 6, 14, 4, 7, 13};
        System.out.println("  Sequência inserida: " + java.util.Arrays.toString(seq));
        for (Supplier<Arvore> f : FABRICAS) {
            Arvore t = f.get();
            for (int v : seq) t.inserir(v);
            t.imprimir();
            verificar(t.getNome() + ": visualização exibida e árvore válida", t.verificarPropriedades());
        }
    }

    /** Conjunto de dados + sequências de operações, idêntico para as duas árvores. */
    private static final class Conjunto {
        final String nome;
        final int[] insercoes;
        final int[] buscas; // 1ª metade: existentes; 2ª metade: inexistentes
        final int[] remocoes; // metade dos elementos inseridos, em ordem aleatória

        Conjunto(String nome, int[] insercoes, Random r) {
            this.nome = nome;
            this.insercoes = insercoes;
            int n = insercoes.length;

            int[] existentes = insercoes.clone();
            embaralhar(existentes, r);
            buscas = new int[2 * n];
            System.arraycopy(existentes, 0, buscas, 0, n);
            for (int i = 0; i < n; i++) buscas[n + i] = -(i + 1); // nunca inseridos (todos ≥ 0)

            int[] ordem = insercoes.clone();
            embaralhar(ordem, r);
            remocoes = java.util.Arrays.copyOf(ordem, n / 2);
        }

        private static void embaralhar(int[] a, Random r) {
            for (int i = a.length - 1; i > 0; i--) {
                int j = r.nextInt(i + 1);
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
        }
    }

    private static final class Resultado {
        long nsInsercao, nsBusca, nsRemocao;
        int alturaInsercao, alturaRemocao;
        long rotInsercao, rotRemocao, ajInsercao, ajRemocao;
        int encontrados;
        boolean propriedadesOk;
    }

    private static List<Conjunto> criarConjuntos(int n, long semente) {
        Random r = new Random(semente);
        int[] cresc = new int[n], decresc = new int[n], aleat = new int[n];
        for (int i = 0; i < n; i++) { cresc[i] = i; decresc[i] = n - 1 - i; aleat[i] = i; }
        Conjunto.embaralhar(aleat, r);
        return List.of(
                new Conjunto("Crescente", cresc, r),
                new Conjunto("Decrescente", decresc, r),
                new Conjunto("Aleatório", aleat, r));
    }

    private static Resultado medir(Supplier<Arvore> fabrica, Conjunto c) {
        Arvore t = fabrica.get();
        Resultado res = new Resultado();

        long t0 = System.nanoTime();
        for (int v : c.insercoes) t.inserir(v);
        res.nsInsercao = System.nanoTime() - t0;
        res.alturaInsercao = t.getAlturaFinal();
        res.rotInsercao = t.getRotacoes();
        res.ajInsercao = t.getAjustesBalanceamento();
        boolean okIns = t.verificarPropriedades();

        t0 = System.nanoTime();
        int achados = 0;
        for (int v : c.buscas) if (t.buscar(v)) achados++;
        res.nsBusca = System.nanoTime() - t0;
        res.encontrados = achados;

        t0 = System.nanoTime();
        for (int v : c.remocoes) t.remover(v);
        res.nsRemocao = System.nanoTime() - t0;
        res.alturaRemocao = t.getAlturaFinal();
        res.rotRemocao = t.getRotacoes() - res.rotInsercao;
        res.ajRemocao = t.getAjustesBalanceamento() - res.ajInsercao;
        res.propriedadesOk = okIns && t.verificarPropriedades();
        return res;
    }

    private static void comparativo(int n) {
        titulo("COMPARATIVO DE DESEMPENHO (n = " + String.format(BR, "%,d", n) + ")");
        System.out.println("  Aquecendo a JVM...");
        List<Conjunto> aquecimento = criarConjuntos(Math.min(n, 20_000), 1);
        for (int rodada = 0; rodada < 2; rodada++)
            for (Conjunto c : aquecimento) for (Supplier<Arvore> f : FABRICAS) medir(f, c);

        List<Conjunto> conjuntos = criarConjuntos(n, 42);
        for (Conjunto c : conjuntos) {
            Resultado a = medir(FABRICA_AVL, c);
            Resultado r = medir(FABRICA_RN, c);
            System.out.println();
            System.out.println("  >> Conjunto: " + c.nome + "   (" + c.insercoes.length + " inserções, "
                    + c.buscas.length + " buscas [metade inexistentes], " + c.remocoes.length + " remoções)");
            System.out.println(String.format("    %-34s %16s %16s", "", "AVL", "Rubro-Negra"));
            linha("Tempo de inserção (ms)", ms(a.nsInsercao), ms(r.nsInsercao));
            linha("Tempo de busca (ms)", ms(a.nsBusca), ms(r.nsBusca));
            linha("Tempo de remoção (ms)", ms(a.nsRemocao), ms(r.nsRemocao));
            linha("Altura após as inserções", a.alturaInsercao, r.alturaInsercao);
            linha("Altura após as remoções", a.alturaRemocao, r.alturaRemocao);
            linha("Rotações nas inserções", a.rotInsercao, r.rotInsercao);
            linha("Rotações nas remoções", a.rotRemocao, r.rotRemocao);
            linha("Ajustes de balanc. (inserções)", a.ajInsercao, r.ajInsercao);
            linha("Ajustes de balanc. (remoções)", a.ajRemocao, r.ajRemocao);
            linha("Buscas bem-sucedidas", a.encontrados, r.encontrados);
            linha("Propriedades preservadas", a.propriedadesOk ? "sim" : "NÃO", r.propriedadesOk ? "sim" : "NÃO");
            verificar("Comparativo '" + c.nome + "': propriedades preservadas e buscas coerentes",
                    a.propriedadesOk && r.propriedadesOk
                            && a.encontrados == c.insercoes.length && r.encontrados == c.insercoes.length);
        }
        System.out.println();
        System.out.println("  Observação: tempos variam entre execuções/máquinas; use-os para comparar as duas árvores entre si.");
    }

    private static void linha(String rotulo, Object avl, Object rn) {
        System.out.println(String.format(BR, "    %-34s %16s %16s", rotulo, avl, rn));
    }
}
