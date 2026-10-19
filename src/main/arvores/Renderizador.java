package arvores;

import no.No;

@FunctionalInterface
public interface Renderizador {
    void desenhar(No<?> raiz, String titulo);
}
