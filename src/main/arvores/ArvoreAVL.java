package arvores;

import java.util.ArrayList;
import java.util.List;
import no.NoAVL;

public class ArvoreAVL extends Arvore {

    private NoAVL raiz;
    private boolean novoNoCriado; // sinal deixado pela inserção recursiva

    @Override
    public String getNome() {
        return "Árvore AVL";
    }

    @Override
    public boolean inserir(int valor) {
        novoNoCriado = false;

        raiz = inserir(raiz, valor);
        quantidadeElementos++;

        if (novoNoCriado) quantidadeNos++;
        return novoNoCriado;
    }

    private NoAVL inserir(NoAVL no, int valor) {
        if (no == null) {
            novoNoCriado = true;
            return new NoAVL(valor);
        }
        if (valor < no.valor) {
            no.esquerda = inserir(no.esquerda, valor);
        } else if (valor > no.valor) {
            no.direita = inserir(no.direita, valor);
        } else {
            no.contagem++; // valor repetido, só conta
            return no;
        }
        return balancear(no);
    }

    @Override
    public boolean remover(int valor) {
        NoAVL alvo = localizar(valor);
        if (alvo == null) return false;

        quantidadeElementos--;
        if (alvo.contagem > 1) { 
            alvo.contagem--; // há repetição, só decrementa
            return true;
        }

        raiz = removerNo(raiz, valor);
        quantidadeNos--;
        return true;
    }

    private NoAVL removerNo(NoAVL no, int valor) {
        if (valor < no.valor) {
            no.esquerda = removerNo(no.esquerda, valor);
        } else if (valor > no.valor) {
            no.direita = removerNo(no.direita, valor);
        } else {
            if (no.esquerda == null)
                return no.direita;
            if (no.direita == null)
                return no.esquerda;

            NoAVL sucessor = minimo(no.direita);
            no.valor = sucessor.valor;
            no.contagem = sucessor.contagem;
            no.direita = removerMinimo(no.direita);
        }
        return balancear(no);
    }

    private NoAVL removerMinimo(NoAVL no) {
        if (no.esquerda == null)
            return no.direita;

        no.esquerda = removerMinimo(no.esquerda);
        return balancear(no);
    }

    private NoAVL minimo(NoAVL no) {
        while (no.esquerda != null)
            no = no.esquerda;

        return no;
    }

    private NoAVL localizar(int valor) {
        NoAVL atual = raiz;
        while (atual != null) {
            if (valor < atual.valor)
                atual = atual.esquerda;
            else if (valor > atual.valor) atual = atual.direita;
            else return atual;
        }
        return null;
    }

    @Override
    public boolean buscar(int valor) {
        return localizar(valor) != null;
    }

    @Override
    public int ocorrencias(int valor) {
        NoAVL no = localizar(valor);
        return no == null ? 0 : no.contagem;
    }

    @Override
    public int configuracaoFilhos(int valor) {
        NoAVL no = localizar(valor);
        return no == null ? -1 : no.quantidadeFilhos();
    }

    private NoAVL balancear(NoAVL no) {
        no.atualizarAltura();
        int fb = no.fatorBalanceamento();

        if (fb > 1) {
            if (no.esquerda.fatorBalanceamento() < 0) {
                no.esquerda = rotacaoEsquerda(no.esquerda);
            }
            ajustesBalanceamento++;
            return rotacaoDireita(no);
        }
        if (fb < -1) {
            if (no.direita.fatorBalanceamento() > 0) {
                no.direita = rotacaoDireita(no.direita);
            }
            ajustesBalanceamento++;
            return rotacaoEsquerda(no);
        }

        return no;
    }

    private NoAVL rotacaoDireita(NoAVL y) {
        NoAVL x = y.esquerda;
        y.esquerda = x.direita;
        x.direita = y;
        y.atualizarAltura();
        x.atualizarAltura();
        rotacoes++;
        return x;
    }

    private NoAVL rotacaoEsquerda(NoAVL x) {
        NoAVL y = x.direita;
        x.direita = y.esquerda;
        y.esquerda = x;
        x.atualizarAltura();
        y.atualizarAltura();
        rotacoes++;
        return y;
    }

    @Override
    public List<Integer> emOrdem() {
        List<Integer> saida = new ArrayList<>(quantidadeElementos);
        emOrdem(raiz, saida);
        return saida;
    }

    private void emOrdem(NoAVL no, List<Integer> saida) {
        if (no == null) return;
        emOrdem(no.esquerda, saida);
        for (int i = 0; i < no.contagem; i++)
            saida.add(no.valor);
        emOrdem(no.direita, saida);
    }

    @Override
    public int altura() {
        return NoAVL.altura(raiz);
    }

    @Override
    public int limiteTeoricoAltura() {
        if (quantidadeNos == 0) return 0;
        return (int) Math.floor(1.4405 * log2(quantidadeNos + 2) - 0.3277);
    }

    @Override
    public boolean verificarPropriedades() {
        erroVerificacao = null;
        if (verificar(raiz, Long.MIN_VALUE, Long.MAX_VALUE) < 0) return false;
        if (emOrdem().size() != quantidadeElementos) {
            falha("Contador de elementos inconsistente com o percurso em ordem");
            return false;
        }
        return true;
    }

    private int verificar(NoAVL no, long min, long max) {
        if (no == null) return 0;
        if (no.valor <= min || no.valor >= max)
            return falha("Ordem de BST violada no nó " + no.valor);
        if (no.contagem < 1)
            return falha("Nó " + no.valor + " com contagem inválida");

        int he = verificar(no.esquerda, min, no.valor);
        if (he < 0) return -1;
        int hd = verificar(no.direita, no.valor, max);
        if (hd < 0) return -1;

        if (Math.abs(he - hd) > 1)
            return falha("Nó " + no.valor + " desbalanceado (fator = " + (he - hd) + ")");
        int h = 1 + Math.max(he, hd);
        if (no.altura != h)
            return falha("Altura armazenada errada no nó " + no.valor
                    + " (guardada " + no.altura + ", real " + h + ")");
        return h;
    }

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
