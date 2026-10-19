package arvores;

import java.util.ArrayList;
import java.util.List;
import no.NoRN;

public class ArvoreRubroNegra extends Arvore {

    private NoRN raiz;
    private boolean novoNoCriado;

    @Override
    public String getNome() {
        return "Árvore Rubro-Negra";
    }

    private static boolean vermelho(NoRN no) {
        return no != null && no.cor == NoRN.VERMELHO;
    }

    @Override
    public boolean inserir(int valor) {
        novoNoCriado = false;

        raiz = inserir(raiz, valor);
        raiz.cor = NoRN.PRETO;
        quantidadeElementos++;

        if (novoNoCriado)
            quantidadeNos++;
        return novoNoCriado;
    }

    private NoRN inserir(NoRN h, int valor) {
        if (h == null) {
            novoNoCriado = true;
            return new NoRN(valor);
        }

        if (valor < h.valor) {
            h.esquerda = inserir(h.esquerda, valor);
        } else if (valor > h.valor) {
            h.direita = inserir(h.direita, valor);
        } else {
            h.contagem++; // valor repetido, só conta
            return h;
        }
        return corrigir(h);
    }

    private NoRN corrigir(NoRN h) {
        if (vermelho(h.direita) && !vermelho(h.esquerda)) { 
            h = rotacaoEsquerda(h);
            ajustesBalanceamento++;
        }
        if (vermelho(h.esquerda) && vermelho(h.esquerda.esquerda)) {
            h = rotacaoDireita(h);
            ajustesBalanceamento++;
        }
        if (vermelho(h.esquerda) && vermelho(h.direita)) {
            inverterCores(h);
            ajustesBalanceamento++;
        }
        return h;
    }

    // ------------------------------------------------------------------ remoção

    @Override
    public boolean remover(int valor) {
        NoRN alvo = localizar(valor);
        if (alvo == null) return false;

        quantidadeElementos--;
        if (alvo.contagem > 1) { // havia valor repetido, só decrementa
            alvo.contagem--;
            return true;
        }

        // Se os dois filhos da raiz são pretos, pinta a raiz de vermelho para que
        // o algoritmo possa "emprestar" vermelho para baixo.
        if (!vermelho(raiz.esquerda) && !vermelho(raiz.direita))
            raiz.cor = NoRN.VERMELHO;
        
        raiz = removerNo(raiz, valor);

        if (raiz != null)
            raiz.cor = NoRN.PRETO;

        quantidadeNos--;

        return true;
    }

    private NoRN removerNo(NoRN h, int valor) {
        if (valor < h.valor) {
            if (!vermelho(h.esquerda) && !vermelho(h.esquerda.esquerda)) {
                h = moverVermelhoEsquerda(h);
            }
            h.esquerda = removerNo(h.esquerda, valor);
        } else {
            if (vermelho(h.esquerda)) {
                h = rotacaoDireita(h);
                ajustesBalanceamento++;
            }
            if (valor == h.valor && h.direita == null) {
                return null;                     // folha (ou nó sem filho direito)
            }
            if (!vermelho(h.direita) && !vermelho(h.direita.esquerda)) {
                h = moverVermelhoDireita(h);
            }
            if (valor == h.valor) {
                // 2 filhos: sucessor em ordem assume valor e contagem
                NoRN sucessor = minimo(h.direita);
                h.valor = sucessor.valor;
                h.contagem = sucessor.contagem;
                h.direita = removerMinimo(h.direita);
            } else {
                h.direita = removerNo(h.direita, valor);
            }
        }
        return corrigir(h);
    }

    private NoRN removerMinimo(NoRN h) {
        if (h.esquerda == null) return null;
        if (!vermelho(h.esquerda) && !vermelho(h.esquerda.esquerda)) {
            h = moverVermelhoEsquerda(h);
        }
        h.esquerda = removerMinimo(h.esquerda);
        return corrigir(h);
    }

    private NoRN minimo(NoRN no) {
        while (no.esquerda != null)
            no = no.esquerda;
        return no;
    }

    /** Supondo h vermelho e h.esquerda e h.esquerda.esquerda pretos, faz h.esquerda ou um filho dele ficar vermelho. */
    private NoRN moverVermelhoEsquerda(NoRN h) {
        inverterCores(h);
        if (vermelho(h.direita.esquerda)) {
            h.direita = rotacaoDireita(h.direita);
            h = rotacaoEsquerda(h);
            inverterCores(h);
        }
        ajustesBalanceamento++;
        return h;
    }

    /** Supondo h vermelho e h.direita e h.direita.esquerda pretos, faz h.direita ou um filho dele ficar vermelho. */
    private NoRN moverVermelhoDireita(NoRN h) {
        inverterCores(h);
        if (vermelho(h.esquerda.esquerda)) {
            h = rotacaoDireita(h);
            inverterCores(h);
        }
        ajustesBalanceamento++;
        return h;
    }

    private NoRN localizar(int valor) {
        NoRN atual = raiz;
        while (atual != null) {
            if (valor < atual.valor)
                atual = atual.esquerda;
            else if (valor > atual.valor)
                atual = atual.direita;
            else
                return atual;
        }
        return null;
    }

    @Override
    public boolean buscar(int valor) {
        return localizar(valor) != null;
    }

    @Override
    public int ocorrencias(int valor) {
        NoRN no = localizar(valor);
        return no == null ? 0 : no.contagem;
    }

    @Override
    public int configuracaoFilhos(int valor) {
        NoRN no = localizar(valor);
        return no == null ? -1 : no.quantidadeFilhos();
    }

    private NoRN rotacaoEsquerda(NoRN h) {
        NoRN x = h.direita;
        h.direita = x.esquerda;
        x.esquerda = h;
        x.cor = h.cor;
        h.cor = NoRN.VERMELHO;
        rotacoes++;
        return x;
    }

    private NoRN rotacaoDireita(NoRN h) {
        NoRN x = h.esquerda;
        h.esquerda = x.direita;
        x.direita = h;
        x.cor = h.cor;
        h.cor = NoRN.VERMELHO;
        rotacoes++;
        return x;
    }

    private void inverterCores(NoRN h) {
        h.cor = !h.cor;
        h.esquerda.cor = !h.esquerda.cor;
        h.direita.cor = !h.direita.cor;
    }

    @Override
    public List<Integer> emOrdem() {
        List<Integer> saida = new ArrayList<>(quantidadeElementos);
        emOrdem(raiz, saida);
        return saida;
    }

    private void emOrdem(NoRN no, List<Integer> saida) {
        if (no == null) return;
        emOrdem(no.esquerda, saida);
        for (int i = 0; i < no.contagem; i++)
            saida.add(no.valor);
        emOrdem(no.direita, saida);
    }

    @Override
    public int altura() {
        return altura(raiz);
    }

    private int altura(NoRN no) {
        return no == null ? 0 : 1 + Math.max(altura(no.esquerda), altura(no.direita));
    }

    @Override
    public int limiteTeoricoAltura() {
        if (quantidadeNos == 0) return 0;
        return (int) Math.floor(2 * log2(quantidadeNos + 1));
    }

    @Override
    public boolean verificarPropriedades() {
        erroVerificacao = null;
        if (vermelho(raiz)) {
            falha("A raiz deve ser preta");
            return false;
        }
        if (verificar(raiz, Long.MIN_VALUE, Long.MAX_VALUE) < 0) return false;
        if (emOrdem().size() != quantidadeElementos) {
            falha("Contador de elementos inconsistente com o percurso em ordem");
            return false;
        }
        return true;
    }

    private int verificar(NoRN no, long min, long max) {
        if (no == null) return 0;
        if (no.valor <= min || no.valor >= max)
            return falha("Ordem de BST violada no nó " + no.valor);
        if (no.contagem < 1)
            return falha("Nó " + no.valor + " com contagem inválida");
        if (vermelho(no.direita))
            return falha("Ligação vermelha pendendo para a direita no nó " + no.valor);
        if (vermelho(no) && vermelho(no.esquerda))
            return falha("Dois nós vermelhos consecutivos em " + no.valor);
        if (vermelho(no.esquerda) && vermelho(no.direita))
            return falha("Nó " + no.valor + " com os dois filhos vermelhos");

        int be = verificar(no.esquerda, min, no.valor);
        if (be < 0) return -1;
        int bd = verificar(no.direita, no.valor, max);
        if (bd < 0) return -1;
        if (be != bd)
            return falha("Alturas negras diferentes no nó " + no.valor + " (" + be + " x " + bd + ")");
        return be + (vermelho(no) ? 0 : 1);
    }

    // --------------------------------------------------------------- visualização

    @Override
    public void imprimir() {
        desenhar(raiz);
    }

    @Override
    public void limpar() {
        raiz = null;
        quantidadeNos = 0;
        quantidadeElementos = 0;
    }
}
