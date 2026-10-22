# Technical Skill: Zero-Physics Client Pet Formation & CFrame Rendering

## Arquitetura de Performance
Instanciar instâncias de `Humanoid`, `AlignPosition`, `BodyPosition` ou restrições físicas para dezenas de pets causa degradação severa no motor de física do servidor e replicação de rede redundante.

## Regras Arquiteturais
1. **No Servidor:** Os pets equipados por um jogador são representados unicamente como uma lista ordenada de UUIDs em sua tabela de sessão (`PlayerData.EquippedPetUUIDs`).
2. **No Cliente:** Cada cliente calcula e atualiza visualmente os modelos dos pets de todos os jogadores locais e remotos em um único loop `RunService.RenderStepped` através de transformações diretas de `CFrame`.

## Geometria da Formação & Animação "Waddle"
### 1. Posicionamento em Arco (Seguindo o Jogador)
Para $N$ pets equipados, distribuídos em arco angular atrás do jogador:
- Raio base $R = 4.5$ studs.
- Ângulo para o pet de índice $i \in \{1, \dots, N\}$:
  $$\theta_i = \pi + \left(i - \frac{N + 1}{2}\right) \cdot \Delta \theta$$
  onde $\Delta \theta = \frac{\pi}{N + 1}$.
- Deslocamento relativo em coordenadas locais do `HumanoidRootPart`:
  $$\vec{D}_i = \text{Vector3.new}(\sin(\theta_i) \cdot R, 0, \cos(\theta_i) \cdot R)$$

### 2. Animação Procedural de Caminhada ("Waddle")
Para conferir vida e fofura elástica aos pets:
- **Inclinação lateral (Roll):**
  $$\text{roll} = \sin(\text{os.clock()} \cdot 12) \cdot \text{math.rad}(15)$$
- **Salto vertical (Bobbing):**
  $$\Delta y = |\cos(\text{os.clock()} \cdot 12)| \cdot 1.8$$
- O CFrame final do pet interpola suavemente com `CFrame:Lerp()` com fator `math.clamp(dt * 10, 0, 1)` para evitar movimentos rígidos ou teletransporte ao virar.
