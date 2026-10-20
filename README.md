# Trabalho de EDBII: Unidade II - Amazon Engineering

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

## Sumário

- [Introdução](#introdução)
- [Dependências](#dependências)
- [Estrutura dos arquivos](#estrutura-dos-arquivos)
- [Compilar e executar](#compilar-e-executar)
- [Funcionalidades implementadas](#funcionalidades-implementadas)
- [Exemplos de uso](#exemplos-de-uso)
- [Comentários sobre o funcionamento](#comentários-sobre-o-funcionamento)
- [Autor do trabalho](#autor-do-trabalho)

---

## Introdução

Trabalho de implementação e análise de árvores balanceadas AVL e Rubro-Negra referente à disciplina de Estrutura de Dados Básicas II do curso de BTI/UFRN.



## Dependências

| Item | Requisito |
|------|-----------|
| Java | JDK 11 ou superior (testado com JDK 27) |
| Bibliotecas externas | Só a biblioteca padrão do Java (`java.util`, `java.io`) |
| Terminal | Com suporte a UTF-8 e a cores ANSI de 256 cores |

Observações sobre o terminal:

* **Windows**: use o Windows Terminal ou o PowerShell moderno.
* Se os caracteres de desenho (`┌ ─ ┴ ┐ ┃`) ficarem quebrados, o terminal não está em UTF-8. Ajuste a codificação ou execute com `java -Dfile.encoding=UTF-8 -cp bin SystemView`.

## Estrutura dos arquivos

```mermaid
treeView-beta
            src/
                main/ 
                    SystemView.java 
                    Visualizador.java
                    no/ 
                        No.java
                        NoAVL.java
                        NoRN.java
                    arvores/ 
                        Arvore.java
                        Renderizador.java
                        ArvoreAVL.java
                        ArvoreRubroNegra.java
                README.md

```

| Arquivo | O que desempenha |
|---------|-------|
| `main/SystemView.java` | main, menu, modo interativo, testes e medição de tempos |
| `main/Visualizador.java` | Desenho da árvore no terminal (estilo bonsai, clássico ou lateral) |
| `main/no/No.java` | Classe abstrata `No<T>`: valor, contador de repetições e filhos |
| `main/no/NoAVL.java` | `NoAVL extends No`: acrescenta a altura do nó |
| `main/no/NoRN.java` | `NoRN extends No`: acrescenta a cor (vermelho/preto) |
| `main/arvores/Arvore.java` | Classe abstrata `Arvore`: declara as operações abstratas (inserir, remover, buscar, em ordem, imprimir...) e guarda os contadores de altura final, rotações e ajustes de balanceamento |
| `main/arvores/ArvoreAVL.java` | `ArvoreAVL extends Arvore` |
| `main/arvores/ArvoreRubroNegra.java` | `ArvoreRubroNegra extends Arvore` |
| `main/arvores/Renderizador.java` | Interface pequena que liga as árvores ao `Visualizador` |

## Compilar e executar

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
| `--bonsai` / `--classica` | Escolhe o estilo do desenho na inicialização do progrma |
| `--largura=N` | Largura do terminal em colunas (por padrão é detectada) |
| `--ajuda` | Lista as opções dessa tabela |

## Funcionalidades implementadas

### Árvores (AVL e Rubro-Negra)

* **Escolha em tempo de execução** da árvore (AVL ou Rubro-Negra) no menu.
* **Inserção** (com suporte a valores repetidos).
* **Remoção** de uma ocorrência (nós folha, com um filho e com dois filhos).
* **Busca** (`buscar` devolve `true/false`, `ocorrencias` devolve quantas vezes o valor foi inserido).
* **Percurso em ordem** (valores em ordem crescente, com as repetições).
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
| Valores repetidos | contagem, remoção decrementando, estrutura inalterada ao duplicar tudo |
| Comparação entre as estruturas | 20 000 operações aleatórias idênticas: respostas e conteúdo final iguais |

### Medição de tempo

O comparativo usa 4 conjuntos: crescente, decrescente, aleatório e aleatório com repetições.

Para cada um, as duas árvores recebem a mesma sequência de inserções, buscas (metade de valores existentes, metade inexistentes) e remoções (metade dos elementos, em ordem aleatória).

Os tempos são medidos com `System.nanoTime()` e a JVM é aquecida antes da medição.

## Exemplos de uso

### Sessão interativa (Rubro-Negra, sem cores)

```
Escolha (Padrão: 1): 1           (estilo bonsai)
Escolha: 1                       (menu principal: Modo interativo)
Escolha: 2                       (Árvore Rubro-Negra)
Escolha: 1                       (Inserir)
  Valor(es) a inserir (separados por espaço): 10 20 30 40 50 25 25
  + 10 inserido
  ...
  + 25 já existia: repetição registrada (agora ×2)
  7 inserção(ões) em 92,33 µs

  Árvore Rubro-Negra
              (25×2)
                 └──────┐
 [10]                 [30]
   └──────┬─────────────┘
        (20)                        [50]
          └────────────────────┬──────┘
                             [40]
                               ┃
                               ┃
                     ╭─────────┴─────────╮
                      ╲                 ╱
                       ╲_______________╱
  (7) = nó vermelho   [7] = nó preto   (×n = n repetições do valor)

Escolha: 2    (Remover: 20 999)
  - 20 removido (uma ocorrência)
  ! 999 não existe na árvore: nada foi alterado

  (exibição da arvore)

Escolha: 3    (Buscar: 25 7)
  > 25 encontrado (2 ocorrência(s)) em 4,55 µs
  ! 7 não encontrado (1,86 µs)

Escolha: 4    (Percurso em ordem)
  Em ordem (6 elementos): [10, 25, 25, 30, 40, 50]
```

### AVL com a mesma sequência (inclui repetições)

Sequência `8 3 10 1 6 14 4 7 13 6 6 1` (a AVL fica com raiz 8; a rubro-negra, com raiz 6):

```
  Árvore AVL
          4       7
          └───┬───┘
 1×2         6×3         10      14
  └───┬───────┘           └───┬───┘
      3                      13
      └───────────────┬───────┘
                      8
```

### Testes e comparativo

```bash
java -cp bin SystemView --testes --sem-cor
```

```
  > Propriedades e percurso em ordem corretos após cada inserção
  > Altura final 10 ≤ limite teórico 14
  ...
  Aprovados: 93   Reprovados: 0

  >> Conjunto: Aleatório (sem repetição)   (100000 inserções, 200000 buscas [metade inexistentes], 50000 remoções)
                                                    AVL      Rubro-Negra
    Tempo de inserção (ms)                       37,748           33,859
    Tempo de busca (ms)                          22,455           27,223
    Tempo de remoção (ms)                        21,912           37,400
    Altura após as inserções                         20               24
    Altura após as remoções                          19               22
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

Variação de caída para a esquerda e equivalente a uma árvore 2-3.

A inserção coloca um nó vermelho e, na volta da recursão, aplica três correções em sequência:

1. filho direito vermelho e esquerdo preto → **rotação à esquerda**;
2. filho esquerdo vermelho com neto esquerdo vermelho → **rotação à direita**;
3. os dois filhos vermelhos → **inversão de cores**.

A remoção usa `moverVermelhoEsquerda` / `moverVermelhoDireita` para garantir que o nó por onde se desce tenha um vermelho "emprestado".

## Autor do trabalho

Victor Matheus de Abreu Dantas

Matrícula: 20250040208
