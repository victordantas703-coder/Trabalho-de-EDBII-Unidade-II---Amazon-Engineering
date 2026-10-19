package no;

public class NoRN extends No<NoRN> {

    public static final boolean VERMELHO = true;
    public static final boolean PRETO = false;

    public boolean cor = VERMELHO;

    public NoRN(int valor) {
        super(valor);
    }

    public boolean ehVermelho() { return cor == VERMELHO; }

    @Override
    public String rotulo(boolean comCor) {
        if (comCor) return rotuloBase();
        // Sem cores no terminal: (7) = vermelho, [7] = preto
        return ehVermelho() ? "(" + rotuloBase() + ")" : "[" + rotuloBase() + "]";
    }

    private static final String RESET = "\u001B[0m";
    private static final String ESTILO_VERMELHO = "\u001B[97;48;5;160m";
    private static final String ESTILO_PRETO = "\u001B[97;48;5;238m";

    @Override
    public String estiloAnsi(int profundidade) {
        return ehVermelho() ? ESTILO_VERMELHO : ESTILO_PRETO;
    }

    @Override
    public String legenda(boolean comCor) {
        if (comCor) {
            return ESTILO_VERMELHO + " vermelho " + RESET + "  " + ESTILO_PRETO + " preto " + RESET
                    + "   (×n = n repetições do valor)";
        }
        return "(7) = nó vermelho   [7] = nó preto   (×n = n repetições do valor)";
    }
}
