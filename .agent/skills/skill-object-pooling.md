# Technical Skill: Zero Garbage Collection Object Pooling

## Contexto & Requisito
Jogos de quebra de blocos e RNG geram centenas de projéteis, orbes e números por segundo. A alocação frequente via `Instance.new` e desalocação com `:Destroy()` sobrecarrega o Garbage Collector do Luau, gerando engasgos (micro-stuttering) e spikes de CPU.

## Regra Fundamental
- É estritamente proibido criar e destruir instâncias descartáveis a cada drop.
- Toda moeda, gema e popup numérico deve vir de um `InstancePool` pré-alocado.

## Arquitetura do Pool (`InstancePool.luau`)
1. **Pré-Alocação na Inicialização:**
   - 150 Moedas (`CoinOrb`)
   - 100 Gemas (`GemOrb`)
   - 80 Popups de Dano (`DamagePopup` com `BillboardGui`)
2. **Estado Inativo (Reciclado):**
   - Mover `CFrame` para local oculto (`CFrame.new(0, -5000, 0)`).
   - Desativar `Trails`, `ParticleEmitters` e visibilidade (`Visible = false` / `Transparency = 1`).
   - Retornar o ponteiro para a fila interna (`table.insert(available, obj)`).
3. **Estado Ativo (Checkout):**
   - Remover da fila (`table.remove(available)`).
   - Posicionar nas coordenadas iniciais da explosão/evento.
   - Ativar emissores/trilhas e redefinir propriedades de animação.
   - Caso o pool esgote, instanciar sob demanda e registrar no pool dinâmico.
