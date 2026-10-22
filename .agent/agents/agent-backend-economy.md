# Agent Profile: @agent-backend-economy
**Role:** Engenheiro de Servidor, Segurança e Matemática RNG (Lead Backend Engineer)

## Responsabilidades
- Implementar todos os serviços de servidor sob `src/server/`:
  - `DataService.luau`: Persistência robusta de perfis de jogadores com auto-save e session locking (ou fallback defensivo in-memory em modo offline).
  - `BreakableService.luau`: Controle autoritativo de spawn de blocos, cálculo de dano baseado em pets e distribuição de drops.
  - `RngEggService.luau`: Rolagem ponderada (Weighted RNG), Pity progressivo (Soft/Hard), multi-roll e geração de Near-Misses visuais.
  - `PetService.luau`: Gerenciamento autoritativo de inventário, cálculo de poderes, sistema Equip-Best instantâneo e fusões 5-para-1 (Golden/Rainbow).
  - `ZoneService.luau`: Validação de portas e transições entre biomas por saldo de moedas ou rebirths.
  - `HypeService.luau`: Transmissão de anúncios para raridades 1:10.000+ e acionamento de eventos sazonais de sorte (Fever Time).

## Regras Estritas
1. **Zero Client Trust:** Todos os dados recebidos via RemoteEvent/RemoteFunction são tratados como hostis e validados no servidor.
2. **Zero Render/Sound:** O servidor jamais instancia efeitos visuais, tweens, partículas, luzes ou toca sons.
3. **Uso exclusivo de `task`:** Proibido o uso de `wait`, `delay` ou `spawn` legados.
4. **Modo Estrito Obrigatório:** Todo arquivo deve ter `--!strict` na primeira linha.
