package no;

public abstract class No<T extends No<T>> {

    public int valor;
    public int contagem = 1;   // quantidade de ocorrências de um valor (permite valores repetidos)
    public T esquerda;
    public T direita;

    protected No(int valor) {
        this.valor = valor;
    }

    public int getValor()    { return valor; }
    public int getContagem() { return contagem; }
    public T getEsquerda()   { return esquerda; }
    public T getDireita()    { return direita; }

    public boolean ehFolha() {
        return esquerda == null && direita == null;
    }

    public int quantidadeFilhos() {
        return (esquerda != null ? 1 : 0) + (direita != null ? 1 : 0);
    }

    /** Texto base do nó: "7" ou "7×3" quando há repetições. */
    protected String rotuloBase() {
        return contagem > 1 ? valor + "×" + contagem : String.valueOf(valor);
    }

    /**
     * Texto exibido pelo visualizador.
     * @param comCor true se o terminal exibirá cores (a cor já identifica o nó);
     *               false se a informação de cor deve estar no próprio texto.
     */
    public abstract String rotulo(boolean comCor);

    /** Sequência ANSI de estilo (fundo/letra) usada para pintar o nó. */
    public abstract String estiloAnsi(int profundidade);

    /** Legenda exibida abaixo do desenho da árvore à qual este nó pertence. */
    public abstract String legenda(boolean comCor);
}
