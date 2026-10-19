import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.No;

/**
 * Desenha árvores binárias no terminal, inspirado no cbonsai.
 *
 * Modos de desenho:
 *   - BONSAI  (padrão): a árvore "cresce" para cima a partir de um tronco e de um
 *     vaso; a raiz fica embaixo e as folhas (nós mais profundos) ficam no topo.
 *   - CLÁSSICO: raiz no topo, descendo (use --classica).
 *   - LATERAL: usado automaticamente quando a árvore é larga demais para a janela
 *     do terminal; a raiz fica à esquerda e a árvore é "deitada".
 *
 * Cores (ANSI de 256 cores): galhos marrons; nós da AVL em tons de verde (mais
 * claro = mais fundo); nós rubro-negros em vermelho ou cinza-escuro.
 * Sem cores (--sem-cor): rubro-negros aparecem como (7) = vermelho e [7] = preto.
 */
public final class Visualizador {

    // ----------------------------------------------------------- configuração
    public static boolean usarCor = true;
    public static boolean modoBonsai = true;
    public static int larguraTerminal = 120;
    /** Acima disso o desenho é omitido (não caberia de forma legível). */
    public static final int LIMITE_NOS = 255;

    // ------------------------------------------------------------------ estilos
    static final String RESET = "\u001B[0m";
    static final String NEGRITO = "\u001B[1m";
    static final String GALHO = "\u001B[38;5;94m";
    static final String VASO = "\u001B[38;5;130m";

    /** Tenta descobrir a largura do terminal (COLUMNS, depois stty); senão 120. */
    public static int detectarLargura() {
        try {
            String c = System.getenv("COLUMNS");
            if (c != null) return Math.max(40, Integer.parseInt(c.trim()));
        } catch (Exception ignorado) { /* tenta o próximo método */ }
        try {
            Process p = new ProcessBuilder("sh", "-c", "stty size < /dev/tty 2>/dev/null").start();
            String s = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor();
            String[] partes = s.split("\\s+");
            if (partes.length == 2) return Math.max(40, Integer.parseInt(partes[1]));
        } catch (Exception ignorado) { /* usa o padrão */ }
        return 120;
    }

    // -------------------------------------------------------------- estruturas
    private static final class Item {
        No<?> no;
        int idx;        // posição no percurso em ordem (define a coluna)
        int prof;       // profundidade (raiz = 0)
        int cx;         // coluna central do rótulo
        Item esq, dir;
    }

    private final List<Item> emOrdem = new ArrayList<>();
    private int profMax;

    private Visualizador() { }

    // ------------------------------------------------------------- ponto de entrada

    /** Assinatura compatível com arvores.Renderizador (registrada em SystemView via Arvore.setRenderizador). */
    public static void desenhar(No<?> raiz, String titulo) {
        desenhar(raiz, titulo, true);
    }

    public static void desenhar(No<?> raiz, String titulo, boolean comLegenda) {
        System.out.println();
        System.out.println(negrito("  " + titulo));
        if (raiz == null) {
            System.out.println("    (árvore vazia)");
            System.out.println();
            return;
        }
        Visualizador v = new Visualizador();
        Item r = v.construir(raiz, 0);

        if (v.emOrdem.size() > LIMITE_NOS) {
            System.out.println("    Árvore com " + v.emOrdem.size() + " nós distintos: grande demais para ser desenhada"
                    + " (limite " + LIMITE_NOS + ").");
        } else {
            int slot = 0;
            for (Item it : v.emOrdem) slot = Math.max(slot, v.texto(it).length());
            slot += 1;
            if (v.emOrdem.size() * slot <= larguraTerminal - 2) {
                v.vertical(r, slot);
            } else {
                System.out.println("  (árvore larga demais para a janela: desenhada na horizontal)");
                v.lateral(raiz, "  ", 0, 0);
            }
        }
        if (comLegenda) System.out.println("  " + raiz.legenda(usarCor));
        System.out.println();
    }

    private static String negrito(String s) {
        return usarCor ? NEGRITO + s + RESET : s;
    }

    // ----------------------------------------------------------- construção

    private Item construir(No<?> no, int prof) {
        if (no == null) return null;
        Item it = new Item();
        it.no = no;
        it.prof = prof;
        it.esq = construir(no.getEsquerda(), prof + 1);
        it.idx = emOrdem.size();
        emOrdem.add(it);
        it.dir = construir(no.getDireita(), prof + 1);
        profMax = Math.max(profMax, prof);
        return it;
    }

    private String texto(Item it) {
        String r = it.no.rotulo(usarCor);
        return usarCor ? " " + r + " " : r;
    }

    // --------------------------------------------------- desenho vertical (grade)

    private void vertical(Item raiz, int slot) {
        int n = emOrdem.size();
        int linhas = 2 * (profMax + 1) - 1;       // cada nível: 1 linha de nós + 1 de ligações
        int largura = n * slot + 2;
        char[][] g = new char[linhas][largura];
        String[][] e = new String[linhas][largura];
        for (char[] l : g) Arrays.fill(l, ' ');

        for (Item it : emOrdem) it.cx = it.idx * slot + slot / 2;

        // nós
        for (Item it : emOrdem) {
            String t = texto(it);
            int ini = it.cx - t.length() / 2;
            String estilo = usarCor ? it.no.estiloAnsi(it.prof) : null;
            for (int i = 0; i < t.length(); i++) {
                g[2 * it.prof][ini + i] = t.charAt(i);
                e[2 * it.prof][ini + i] = estilo;
            }
        }

        // ligações (galhos): sempre construídas "de cima para baixo"
        String galho = usarCor ? GALHO : null;
        for (Item it : emOrdem) {
            if (it.esq == null && it.dir == null) continue;
            int linha = 2 * it.prof + 1;
            int px = it.cx;
            if (it.esq != null && it.dir != null) {
                int lx = it.esq.cx, rx = it.dir.cx;
                trecho(g, e, linha, lx, rx, galho);
                marcar(g, e, linha, lx, '┌', galho);
                marcar(g, e, linha, px, '┴', galho);
                marcar(g, e, linha, rx, '┐', galho);
            } else if (it.esq != null) {
                int lx = it.esq.cx;
                trecho(g, e, linha, lx, px, galho);
                marcar(g, e, linha, lx, '┌', galho);
                marcar(g, e, linha, px, '┘', galho);
            } else {
                int rx = it.dir.cx;
                trecho(g, e, linha, px, rx, galho);
                marcar(g, e, linha, px, '└', galho);
                marcar(g, e, linha, rx, '┐', galho);
            }
        }

        int margem = modoBonsai ? Math.max(0, 10 - raiz.cx) : 0;
        if (modoBonsai) {
            for (int l = linhas - 1; l >= 0; l--) imprimirLinha(g[l], e[l], margem, true);
            String tronco = " ".repeat(margem + raiz.cx) + (usarCor ? GALHO + "┃" + RESET : "┃");
            System.out.println(tronco);
            System.out.println(tronco);
            String pad = " ".repeat(Math.max(0, margem + raiz.cx - 10));
            String[] vaso = {
                    "╭─────────┴─────────╮",
                    " ╲                 ╱ ",
                    "  ╲_______________╱  "};
            for (String l : vaso) System.out.println(pad + (usarCor ? VASO + l + RESET : l));
        } else {
            for (int l = 0; l < linhas; l++) imprimirLinha(g[l], e[l], margem, false);
        }
    }

    private static void trecho(char[][] g, String[][] e, int linha, int de, int ate, String estilo) {
        for (int c = de + 1; c < ate; c++) marcar(g, e, linha, c, '─', estilo);
    }

    private static void marcar(char[][] g, String[][] e, int linha, int col, char c, String estilo) {
        g[linha][col] = c;
        e[linha][col] = estilo;
    }

    /** Ao desenhar de cabeça para cima, as peças de ligação precisam ser espelhadas. */
    private static char espelhar(char c) {
        switch (c) {
            case '┌': return '└';
            case '└': return '┌';
            case '┐': return '┘';
            case '┘': return '┐';
            case '┴': return '┬';
            case '┬': return '┴';
            default:  return c;
        }
    }

    private static void imprimirLinha(char[] g, String[] e, int margem, boolean espelhado) {
        int fim = g.length - 1;
        while (fim >= 0 && g[fim] == ' ' && e[fim] == null) fim--;
        StringBuilder sb = new StringBuilder(" ".repeat(margem));
        String atual = null;
        for (int c = 0; c <= fim; c++) {
            String est = e[c];
            if (est == null ? atual != null : !est.equals(atual)) {
                if (atual != null) sb.append(RESET);
                if (est != null) sb.append(est);
                atual = est;
            }
            sb.append(espelhado ? espelhar(g[c]) : g[c]);
        }
        if (atual != null) sb.append(RESET);
        System.out.println(sb);
    }

    // ----------------------------------------------------- desenho lateral (texto)

    /**
     * @param pos 0 = raiz, 1 = filho direito (desenhado acima), 2 = filho esquerdo (abaixo)
     */
    private void lateral(No<?> no, String prefixo, int pos, int prof) {
        if (no == null) return;
        String prefDir = prefixo + (pos == 0 ? "" : pos == 1 ? "    " : "│   ");
        String prefEsq = prefixo + (pos == 0 ? "" : pos == 1 ? "│   " : "    ");
        lateral(no.getDireita(), prefDir, 1, prof + 1);

        String conector = pos == 0 ? "" : pos == 1 ? "┌── " : "└── ";
        String r = no.rotulo(usarCor);
        String rotulo = usarCor ? no.estiloAnsi(prof) + " " + r + " " + RESET : r;
        String lado = usarCor ? GALHO + prefixo + conector + RESET : prefixo + conector;
        System.out.println(lado + rotulo);

        lateral(no.getEsquerda(), prefEsq, 2, prof + 1);
    }
}
