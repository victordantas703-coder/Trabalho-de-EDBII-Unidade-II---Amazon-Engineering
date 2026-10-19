package arvores;

import java.util.List;
import no.No;

public abstract class Arvore {

    protected int alturaFinal;
    protected long rotacoes;
    protected long ajustesBalanceamento;
    protected int quantidadeNos;
    protected int quantidadeElementos; // difenrete da quantidade nós, esse conta com as repetições
    protected String erroVerificacao;


    // Visualização
    private static Renderizador renderizador;

    public static void setRenderizador(Renderizador r) {
        renderizador = r;
    }

    protected void desenhar(No<?> raiz) {
        if (renderizador == null) {
            System.out.println("(nenhum renderizador configurado: use Arvore.setRenderizador)");
            return;
        }
        renderizador.desenhar(raiz, getNome());
    }


    // Operações de árvores
    public abstract String getNome();

    public abstract boolean inserir(int valor);
    public abstract boolean remover(int valor);
    public abstract boolean buscar(int valor);

    public abstract int ocorrencias(int valor);
    public abstract List<Integer> emOrdem();
    public abstract void imprimir();
    public abstract int altura();
    public abstract boolean verificarPropriedades();

    public abstract int limiteTeoricoAltura();
    public abstract int configuracaoFilhos(int valor);

    public abstract void limpar();


    // Estatísticas
    public int atualizarAlturaFinal() {
        alturaFinal = altura();
        return alturaFinal;
    }

    public int getAlturaFinal() { return atualizarAlturaFinal(); }
    public long getRotacoes() { return rotacoes; }
    public long getAjustesBalanceamento() { return ajustesBalanceamento; }
    public int getQuantidadeNos() { return quantidadeNos; }
    public int getQuantidadeElementos() { return quantidadeElementos; }
    public String getErroVerificacao() { return erroVerificacao; }
    public boolean estaVazia() { return quantidadeElementos == 0; }

    public void resetarContadores() {
        rotacoes = 0;
        ajustesBalanceamento = 0;
        alturaFinal = 0;
    }

    protected int falha(String mensagem) {
        erroVerificacao = mensagem;
        return -1;
    }

    protected static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
