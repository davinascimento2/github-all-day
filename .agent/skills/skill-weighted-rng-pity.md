# Technical Skill: Weighted RNG, Progressive Pity & Near-Miss Mechanics

## Objetivo
Implementar o motor matemático de rolagem de recompensas (Pets, Auras e Drops) com garantias progressivas (Pity) e gatilhos de engajamento psicológico ético ("Near-Miss").

## 1. Algoritmo de Roleta Ponderada (Weighted Choice)
1. Calcular a soma total dos pesos ativos: $W_{\text{total}} = \sum_{i=1}^n w_i \cdot \text{multiplier}_i$
2. Sortear um número escalar em ponto flutuante $r \in [0, W_{\text{total}})$.
3. Percorrer a tabela acumulando a soma: o item cujo intervalo acumulado contiver $r$ é o vencedor selecionado.

## 2. Camadas de Pity (Proteção contra Má Sorte)
- **Soft Pity (Giro 40+ sem Legendary/Mythic/Secret):**
  - Quando `SoftPityCounter >= 40`, os pesos de todos os itens de categoria `Legendary`, `Mythic` e `Secret` são multiplicados por `2.5x`.
- **Hard Pity (Giro 50 cravado):**
  - Se `HardPityCounter >= 50`, o sorteio restringe a escolha exclusivamente ao conjunto de itens de raridade $\ge \text{Legendary}$.
  - Ao conceder o item garantido, ambos os contadores (`SoftPityCounter` e `HardPityCounter`) são resetados para `0`.
- Ao obter qualquer item $\ge \text{Legendary}$ naturalmente, os contadores de Pity também são resetados.

## 3. Heurística Visual Near-Miss ("Quase Ganhou")
Para manter a retenção e dopamina visual durante a animação de roleta:
- Se o item sorteado for de tier inferior (ex: `Common` ou `Uncommon`), a animação da roleta no cliente deve obrigatoriamente renderizar um item de raridade extrema (`Mythic` ou `Secret`) exatamente a 1 ou 2 posições adjacentes da casa onde o cursor desacelera e trava.
- Esse estímulo gera a sensação tangível de proximidade da recompensa máxima sem adulterar a probabilidade matemática do servidor.
