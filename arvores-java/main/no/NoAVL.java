package no;

public class NoAVL extends No<NoAVL> {

    public int altura = 1;

    public NoAVL(int valor) {
        super(valor);
    }

    public int getAltura() { return altura; }

    public static int altura(NoAVL no) {
        return no == null ? 0 : no.altura;
    }

    public void atualizarAltura() {
        altura = 1 + Math.max(altura(esquerda), altura(direita));
    }

    public int fatorBalanceamento() {
        return altura(esquerda) - altura(direita);
    }

    @Override
    public String rotulo(boolean comCor) {
        return rotuloBase();
    }

    /** Tons de verde (256 cores): quanto mais fundo o nó, mais claro o verde. */
    private static final int[] TONS_FOLHA = {34, 40, 46, 82, 118, 154};

    @Override
    public String estiloAnsi(int profundidade) {
        int tom = TONS_FOLHA[Math.min(profundidade, TONS_FOLHA.length - 1)];
        return "\u001B[30;48;5;" + tom + "m";
    }

    @Override
    public String legenda(boolean comCor) {
        return "Cada nó mostra o valor (×n = n repetições). Verde mais claro = nível mais profundo.";
    }
}
