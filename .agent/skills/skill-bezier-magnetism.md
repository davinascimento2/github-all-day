# Technical Skill: Quadratic Bézier Magnetic Drop Physics

## Objetivo
Implementar física de atração magnética altamente gratificante e dinâmica para todos os drops (moedas, gemas, trevos), evitando trajetórias retas rígidas. Os drops devem explodir para cima em dispersão radial e curvar-se organicamente até o torso do jogador.

## Equação Matemática da Trajetória
A posição da partícula $B(t)$ no instante de interpolação $t \in [0, 1]$ é determinada pela Curva de Bézier Quadrática:

$$B(t) = (1 - t)^2 P_0 + 2(1 - t)t P_1 + t^2 P_2$$

### Definição dos Pontos de Controle:
- $P_0 \in \mathbb{R}^3$: Posição de origem da quebra do bloco (`Breakable.Position`).
- $P_1 \in \mathbb{R}^3$: Ponto de dispersão e apogeu vertical aleatório:
  $$P_1 = P_0 + \text{Vector3.new}(\text{random}(-12, 12), \text{random}(15, 28), \text{random}(-12, 12))$$
- $P_2 \in \mathbb{R}^3$: Posição em tempo real do alvo do jogador (`HumanoidRootPart.Position`), recalculada a cada frame de renderização.

## Progressão Temporal & Aceleração
O parâmetro de tempo $t$ avança com aceleração exponencial/quadrática para criar a sensação de "sucção magnética":
$$t = \text{math.clamp}\left(\frac{\text{elapsed}}{\text{duration}}, 0, 1\right)^2$$
- Duração padrão: entre 0.45s e 0.70s por orbe, com leve jitter temporal entre moedas do mesmo pacote para evitar chegadas perfeitamente simultâneas.
- Ao atingir $t \ge 1.0$:
  - Acionar som de coleta com elevação harmônica de pitch (`+0.03` até `1.6x`).
  - Disparar micro flash no HUD de moedas.
  - Devolver a instância ao `InstancePool`.
