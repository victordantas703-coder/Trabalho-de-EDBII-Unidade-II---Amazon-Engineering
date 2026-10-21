# Trabalho de EDBII: Unidade II - Amazon Engineering

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

## Sumário

- [Introdução](#introdução)
- [Dependências](#dependências)
- [Estrutura dos arquivos](#estrutura-dos-arquivos)
- [Execução do projeto](#compilar-e-executar)
- [Funcionalidades implementadas](#funcionalidades-implementadas)
- [Exemplo de uso](#exemplo-de-uso)
- [Comentários sobre o funcionamento](#comentários-sobre-o-funcionamento)
- [Autor do trabalho](#autor-do-trabalho)
- [Referências Bibliográficas](#referências-bibliográficas)

---

## Introdução

Trabalho de implementação e análise de árvores balanceadas AVL e Rubro-Negra referente à disciplina de Estrutura de Dados Básicas II do curso de BTI/UFRN.



## Dependências

| Item | Requisito |
|------|-----------|
| **Java** | **JDK 11 ou superior (testado com JDK 27)** |
| Bibliotecas externas | Só a biblioteca padrão do Java (`java.util`, `java.io`) |
| Terminal | Com suporte a UTF-8 e a cores ANSI de 256 cores |

Observações sobre o terminal:

* **Windows**: use o Windows Terminal ou o PowerShell moderno.
* Se os caracteres de desenho (`┌ ─ ┴ ┐ ┃`) ficarem quebrados, o terminal não está em UTF-8. Ajuste a codificação ou execute com `java -Dfile.encoding=UTF-8 -cp bin SystemView`.

## Estrutura dos arquivos

```
pasta-projeto/
├── main/
│   ├── SystemView.java
│   ├── Visualizador.java
│   ├── no/
│   │   ├── No.java
│   │   ├── NoAVL.java
│   │   └── NoRN.java
│   └── arvores/
│       ├── Arvore.java
│       ├── Renderizador.java
│       ├── ArvoreAVL.java
│       └── ArvoreRubroNegra.java
├── bin/                        (criada pela compilação)
├── README.md
└── sistema-arvores.jar
```

| Arquivo | O que desempenha |
|---------|-------|
| `main/SystemView.java` | main, menu, modo interativo, testes e medição de tempos |
| `main/Visualizador.java` | Desenho da árvore no terminal (estilo bonsai, clássico ou lateral) |
| `main/no/No.java` | Classe abstrata `No<T>`: valor e filhos |
| `main/no/NoAVL.java` | `NoAVL extends No`: acrescenta a altura do nó |
| `main/no/NoRN.java` | `NoRN extends No`: acrescenta a cor (vermelho/preto) |
| `main/arvores/Arvore.java` | Classe abstrata `Arvore`: declara as operações abstratas (inserir, remover, buscar, em ordem, imprimir...) e guarda os contadores de altura final, rotações e ajustes de balanceamento |
| `main/arvores/ArvoreAVL.java` | `ArvoreAVL extends Arvore` |
| `main/arvores/ArvoreRubroNegra.java` | `ArvoreRubroNegra extends Arvore` |
| `main/arvores/Renderizador.java` | Interface pequena que liga as árvores ao `Visualizador` |

## Execução do projeto

### Opção 1 - Executável .jar

```bash
# 1) clonar projeto
git clone https://github.com/victordantas703-coder/Trabalho-de-EDBII-Unidade-II---Amazon-Engineering.git

# 2) entrar na pasta do projeto
cd Trabalho-de-EDBII-Unidade-II---Amazon-Engineering

# 3) executar o .jar
java -jar sistema-arvores.jar
```

### Opção 2 - Executar através da bin

```bash
# 1) clonar projeto
git clone https://github.com/victordantas703-coder/Trabalho-de-EDBII-Unidade-II---Amazon-Engineering.git

# 2) entrar na pasta do projeto
cd Trabalho-de-EDBII-Unidade-II---Amazon-Engineering

# 3) executar o menu
java -cp bin SystemView
```

### Opção 3 - Compilar e executar

```bash
# 1) clonar projeto
git clone https://github.com/victordantas703-coder/Trabalho-de-EDBII-Unidade-II---Amazon-Engineering.git

# 2) entrar na pasta do projeto
cd Trabalho-de-EDBII-Unidade-II---Amazon-Engineering

# 3) compilar (cria a pasta "bin" com os .class)
mkdir -p bin
javac -encoding UTF-8 -d bin -sourcepath src/main src/main/SystemView.java

# 4) executar o menu
java -cp bin SystemView
```

### Opções adicionais de linha de comando

| Opção | Efeito |
|-------|--------|
| `--testes` | Roda a bateria de testes e o comparativo de desempenho. O código de saída é `0` se tudo passou e `1` se algum teste falhou |
| `--n=VALOR` | Tamanho dos conjuntos do comparativo (padrão: `100000`) |
| `--sem-cor` | Desliga as cores. Na rubro-negra, `(7)` = nó vermelho e `[7]` = nó preto |
| `--bonsai` / `--classica` | Escolhe o estilo do desenho na inicialização do programa |
| `--largura=N` | Largura do terminal em colunas (por padrão é detectada) |
| `--ajuda` | Lista as opções dessa tabela |

## Funcionalidades implementadas

### Árvores (AVL e Rubro-Negra)

* **Escolha em tempo de execução** da árvore (AVL ou Rubro-Negra) no menu.
* **Inserção** de elementos na árvore.
* **Remoção** de um elemento da árvore.
* **Busca**: `buscar` devolve `true/false`.
* **Percurso em ordem**: valores em ordem crescente.
* **Visualização**: na rubro-negra as cores dos nós são exibidas.
* **Verificação das propriedades** (`verificarPropriedades()`, usada pelos testes automáticos e pelo comparativo):
  * AVL: ordem de BST, `|fator de balanceamento| ≤ 1` em todo nó e altura armazenada correta;
  * Rubro-Negra: raiz preta, sem dois vermelhos seguidos, sem ligação vermelha à direita, nenhum nó com os dois filhos vermelhos e mesma altura negra em todos os caminhos.
* **Estatísticas**: altura final, rotações e ajustes de balanceamento (atributos de `Arvore`).
* **Balanceamento**: rotações simples/duplas (AVL) e rotações + inversão de cores + "mover vermelho" (LLRB).

### Fluxo da `SystemView`

Ao iniciar, o programa pergunta como você quer ver as árvores e mostra um pequeno exemplo de cada estilo:

1. **Clássico** (padrão): a raiz fica no topo;
2. **Bonsai**: a raiz fica embaixo e a árvore cresce para cima.

Árvores largas demais para a janela são desenhadas na horizontal automaticamente. Use `--largura=N` se precisar mudar.

**Menu principal:**

1. **Modo interativo**: escolhe a árvore e opera sobre ela (inserir/remover vários valores de uma vez, buscar, percurso em ordem, visualizar, estatísticas e tempos, inserir N aleatórios, limpar, trocar de árvore). Mostra o tempo de cada operação. A árvore é exibida após cada inserção e remoção.
2. **Bateria de testes** nas duas árvores.
3. **Comparativo de desempenho**: tempo de inserção, busca e remoção + altura, rotações e ajustes.

### Testes automáticos

Executados nas duas árvores com os mesmos conjuntos de dados e as mesmas sequências de operações:

| Tipo de operação | O que é feito |
|---|---|
| Inserção crescente, decrescente e não ordenada | 3 testes com 1000 elementos. As propriedades são conferidas após cada inserção |
| Busca de elementos existentes e inexistentes | 500 existentes, 500 inexistentes, fora do intervalo e extremos |
| Remoção com diferentes configurações de filhos | 216 árvores (n = 1..24, 3 ordens de inserção × 3 ordens de remoção), conferindo após cada remoção. O teste exige que tenham sido removidos nós folha, com 1 filho e com 2 filhos |
| Remoção de elementos inexistentes | devolve `false`, árvore e contadores permanecem iguais |
| Árvore com um elemento / árvore vazia | inserir, buscar, remover, reutilizar, visualizar |
| Propriedades após inserções e remoções | 6000 operações aleatórias intercaladas, conferidas uma a uma, esvaziar 2000 nós |
| Comparação entre as estruturas | 20 000 operações aleatórias idênticas: respostas e conteúdo final iguais |

### Medição de tempo

O comparativo usa 3 conjuntos: crescente, decrescente e aleatório.

Para cada um, as duas árvores recebem a mesma sequência de inserções, buscas (metade de valores existentes, metade inexistentes) e remoções (metade dos elementos, em ordem aleatória).

Os tempos são medidos com `System.nanoTime()` e a JVM é aquecida antes da medição.

## Exemplo de uso

### Sessão interativa

```
Escolha (Padrão: 1): 1           (estilo bonsai)
Escolha: 1                       (menu principal: Modo interativo)
Escolha: 2                       (Árvore Rubro-Negra)
Escolha: 1                       (Inserir)
  Valor(es) a inserir (separados por espaço): 10 20 30 40 50 25 25
  + 10 inserido
  ...
  + 25 inserido
  ! 25 já existe na árvore: inserção impedida
  6 inserção(ões) em 72,09 µs

  Árvore Rubro-Negra
          (25)  
            └────┐  
[10]           [30]  
  └────┬─────────┘  
     (20)                [50]  
       └──────────────┬────┘  
                    [40]  
                      ┃  
                      ┃  
            ╭─────────┴─────────╮  
             ╲                 ╱  
              ╲_______________╱  
  (7) = nó vermelho   [7] = nó preto  

Escolha: 2    (Remover: 20 999)
  - 20 removido
  ! 999 não existe na árvore: nada foi alterado

  (exibição da arvore)

Escolha: 3    (Buscar: 25 7)
  > 25 encontrado em 1,53 µs
  ! 7 não encontrado (671 ns)

Escolha: 4    (Percurso em ordem)
  Em ordem (5 elementos): [10, 25, 30, 40, 50]
```

### Testes e comparativo

```bash
java -cp bin SystemView --testes --sem-cor
```

```
  > Propriedades e percurso em ordem corretos após cada inserção
  > Altura final 10 ≤ limite teórico 14
  ...
  Aprovados: 75   Reprovados: 0

  >> Conjunto: Aleatório   (100000 inserções, 200000 buscas [metade inexistentes], 50000 remoções)
                                                    AVL      Rubro-Negra
    Tempo de inserção (ms)                       51,895           32,339
    Tempo de busca (ms)                          20,744           25,670
    Tempo de remoção (ms)                        21,901           39,564
    Altura após as inserções                         20               24
    Altura após as remoções                          19               21
    Rotações nas inserções                        69920           118191
    ...
```

(Os tempos variam para cada ambiente de testes)

## Comentários sobre o funcionamento

### AVL

Cada nó calcula a sua altura e seu fator de balanceamento.

Após inserir ou remover, a função `balancear` é aplicada em cada nó do caminho de volta até a raiz.

Calcula-se `fb = h(esq) − h(dir)` e, se `|fb| > 1`, aplica rotação simples (casos esquerda-esquerda e direita-direita) ou dupla (esquerda-direita e direita-esquerda).

### Rubro-Negra (LLRB)

Variação de caída para a esquerda e equivalente a uma árvore 2-3. Auditada por Robert Sedgewick [3].

A inserção coloca um nó vermelho e, na volta da recursão, aplica três correções em sequência:

1. filho direito vermelho e esquerdo preto → **rotação à esquerda**;
2. filho esquerdo vermelho com neto esquerdo vermelho → **rotação à direita**;
3. os dois filhos vermelhos → **inversão de cores**.

A remoção usa `moverVermelhoEsquerda` / `moverVermelhoDireita` para garantir que o nó por onde se desce tenha um vermelho "emprestado".

## Autor do trabalho

Victor Matheus de Abreu Dantas

Matrícula: 20250040208

## Referências Bibliográficas

[1] Colombo, D. A.; Oliani Junior, S. Conceitos Importantes de Estrutura de Árvores: binárias e binária balanceada (AVL). Interface Tecnológica, v. 20, n. 1, 2023.

[2] Black, P. E. AVL tree. Dictionary of Algorithms and Data Structures, National Institute of Standards and Technology (NIST), 2019.

[3] Sedgewick, R. Left-leaning Red-Black Trees. Department of Computer Science, Princeton University, 2008.

[4] Fleury, P. O Sistema de Processamento de Pedidos e a Gestão do Ciclo do Pedido. ILOS: Especialistas em Logística e Supply Chain, 2003.