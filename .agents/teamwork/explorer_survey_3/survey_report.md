# Relatório de Arquitetura & Especificação Técnica — Explorer Survey 3
## Audio Engine Procedural, Storybook UI/UX & Arquitetura de Testes Automatizados E2E
**Projeto**: *A Sinfonia de Ana Gleice — O Passeio Encantado de Aniversário*  
**Autor**: Explorer 3 (Audio, UI/UX & Testing Architect)  
**Data**: 2026-10-22  
**Status**: Aprovado para Implementação  

---

## 1. Resumo Executivo

O presente documento detalha a arquitetura técnica para três pilares essenciais de *A Sinfonia de Ana Gleice*, jogo 3D de navegador cozy/storybook desenvolvido como presente de aniversário para a musicista Ana Gleice Almeida de Oliveira por seu namorado Davi Nascimento Chaves Homen (Toadbigode):

1. **Audio Engine Procedural via Web Audio API**: Sistema 100% sintetizado em tempo real, sem qualquer arquivo de áudio externo (.mp3, .ogg, .wav). Elimina em 100% riscos de latência, erros 404 de CDN, bloqueios de CORS e falhas de decodificação. Incorpora síntese de piano acústico, dedilhado de violão de nylon, pads lofi aconchegantes com modulação tape flutter, trilhas dinâmicas para as 3 fases, efeitos sonoros estilizados e balbucios de diálogo pentatônicos estilo Animal Crossing / Zelda.
2. **Sistema Storybook UI/UX (Zero UI Slop)**: Interface inspirada em livros de contos de fadas e jogos da Nintendo/Ghibli, com molduras pergaminho pastel, sombras orgânicas, cantos arredondados, tipografia elegante (Fredoka e Quicksand), caixa de diálogo com efeito máquina de escrever sincrônico, modal animado do pergaminho da carta de amor (com botão permanente de releitura), captura de foto/screenshot em moldura Polaroid de lembrança e controles táteis para dispositivos móveis.
3. **Estrutura do Projeto & Arquitetura de Testes Automatizados E2E**: Estrutura standalone servível via qualquer servidor HTTP local, expondo o harness determinístico `window.__GAME_STATE__` e `window.gameTestHooks`, permitindo que suites E2E headless (Playwright/Puppeteer) verifiquem integralmente a inicialização, movimento, diálogos, áudio, transição das 3 fases e captura de imagem.

---

## 2. Pilar 1: Procedural Web Audio API Sound & Music Architecture

Sendo a homenageada uma **musicista**, a qualidade tímbrica, precisão harmônica e expressividade da sonoridade procedural são prioridades absolutas. A síntese não deve soar como "bipes de 8-bits estridentes", mas sim como um ambiente acústico, orgânico e acolhedor (warm & cozy).

### 2.1 Por Que Áudio 100% Procedural?
- **Zero Assets Externos**: Sem requisições de rede para arquivos `.mp3` ou `.wav`. O jogo carrega instantaneamente, funciona offline e nunca sofre com conexões instáveis.
- **Transições Suaves e Dinâmicas**: Mudanças de andamento, acordes, modulações de fase e parâmetros de filtro (cutoff, Q) reagem diretamente à posição da jogadora e ao estágio narrativo.
- **Variação Infinita sem Repetição Enfadonha**: Pequenas variações de afinação (micro-detuning) e velocidade criam a sensação de um instrumento real sendo tocado ao vivo.

### 2.2 Especificação dos Módulos de Síntese Instrumental

#### A. Síntese de Piano Acústico Harmônico
O som do piano acústico é composto por um impacto mecânico inicial (martelo de feltro na corda de aço), seguido pela ressonância de múltiplos parciais harmônicos que decaem em taxas distintas através da tábua harmônica de madeira.

*Arquitetura do Sintetizador de Piano*:
- **Ataque Transiente Percussivo**:
  - Pulso curto de onda triangular ou ruído filtrado (duração: 8ms, cutoff bandpass: 1800Hz) para emular o golpe do martelo.
- **Banco de Parciais Harmônicos Aditivos**:
  - Parcial 1 (Fundamental $f_0$): Ganho 1.0, decaimento exponencial de 2.5s.
  - Parcial 2 ($2f_0$ - Oitava): Ganho 0.45, decaimento exponencial de 1.8s.
  - Parcial 3 ($3f_0$ - Quinta): Ganho 0.22, decaimento exponencial de 1.2s.
  - Parcial 4 ($4f_0$ - Oitava superior): Ganho 0.12, decaimento exponencial de 0.8s.
  - Parcial 5 ($5f_0$ - Terça maior): Ganho 0.05, decaimento exponencial de 0.5s.
- **Micro-Desafinação Estéreo (Chorus Natural)**:
  - Dois osciladores na fundamental com desvio de $+1.5$ cents e $-1.5$ cents, simulando as cordas uníssonas dos pianos de cauda.
- **Filtro de Corpo & Tábua Harmônica (Soundboard)**:
  - `BiquadFilterNode` configurado em `lowpass` (frequência de corte inicial de 2400Hz decaindo para 800Hz com o tempo, $Q = 1.2$), conferindo o calor e o amortecimento natural da madeira.

#### B. Simulação de Violão de Nylon Acústico (Dedilhado Pastoral)
- **Excitação de Corda**:
  - Modelo baseado em excitação por onda triangular rápida com filtro dinâmico que simula o toque da ponta do dedo/unha na corda de nylon.
  - Envelope de filtro: varredura de corte inicial rápida (de 3200Hz para 450Hz em 75ms).
  - Leve inflexão de afinação inicial (ataque com $+12$ cents retornando ao tom fundamental em 25ms, recriando o estiramento inicial da corda ferida).
- **Sustentação Aconchegante**:
  - Decaimento quente com ressonância subsônica sutil ($0.5 \times f_0$ em ganho de -18dB) para simular o bojo oco do violão.

#### C. Lofi Synth Pad & Textura Tape Warmth
- **Camada de Osciladores**:
  - 2 osciladores dente-de-serra (sawtooth) e 1 oscilador senoidal suave, com detune de $\pm 8$ cents.
- **Filtro Lofi Quente**:
  - `BiquadFilterNode` passa-baixa em 520Hz com ressonância aconchegante ($Q = 0.9$).
- **Modulador LFO (Wow & Flutter de Fita K7)**:
  - Oscilador LFO senoidal de baixíssima frequência (0.45 Hz) modulando levemente a afinação dos osciladores primários ($\pm 3.5$ cents), conferindo a nostalgia acolhedora dos teclados vintage e fitas lofi.
- **Textura de Vinil Procedural**:
  - Buffer de ruído rosa gerado em memória, filtrado entre 800Hz e 2800Hz, com ganho ultra-sutil (0.004) gerando uma sensação tátil de lareira/disco de vinil.

---

### 2.3 Trilha Sonora Dinâmica por Fase

Cada fase possui uma paisagem tonal única, desenhada para dialogar com a narrativa:

| Fase | Tonalidade & Modo | Andamento | Instrumentação | Progressão Harmônica & Clima |
| :--- | :--- | :--- | :--- | :--- |
| **Fase 1: O Bosque das Melodias** (Zelda Vibe) | Sol Maior / Ré Maior (Pastoril) | 76 BPM (Andante) | Violão de nylon dedilhado + flauta/piano solo | $G^{\text{maj7}} \rightarrow D^{\text{maj7}} \rightarrow Em^7 \rightarrow C^{\text{maj7}} \rightarrow D^{\text{sus4}}$. Arpejos suaves como folhas ao vento. |
| **Fase 2: O Planetário dos Sonhos** (Galaxy Vibe) | Lá Maior / Fá# Menor com 4ª aumentada (Lídio Cósmico) | 80 BPM (Flutuante) | Caixa de música estelar + piano elétrico com delay feedback | $A^{\text{maj7}} \rightarrow B/A \rightarrow C\#m^7 \rightarrow D^{\text{maj7(\#11)}} \rightarrow E^{\text{sus4}}$. Delay rítmico de colcheia pontuada (375ms, feedback 0.40). |
| **Fase 3: O Jardim do Luar & O Grande Abraço** (Clímax) | Dó Maior / Fá Maior (Lírico Romântico) | 72 BPM (Valsa 3/4) | Piano de cauda lírico + cordas lofi + sinos de cristal | Valsa em 3/4 baseada em $C \rightarrow Em/B \rightarrow F/A \rightarrow G^{\text{sus4}} \rightarrow C$, incorporando a melodia do "Parabéns pra Você" em fraseado clássico e romântico. |

#### Melodia de Parabéns Integrada (Easter Egg Musical Poético)
Em vez de um toque óbvio de celular, o tema da Fase 3 integra as notas do hino de aniversário de forma sofisticada e terna no piano:
- $G4 \rightarrow G4 \rightarrow A4 \rightarrow G4 \rightarrow C5 \rightarrow B4$
- $G4 \rightarrow G4 \rightarrow A4 \rightarrow G4 \rightarrow D5 \rightarrow C5$
Acompanhadas por baixos arpejados em estilo noturno de Chopin, celebrando o aniversário de Ana Gleice com elegância musical.

---

### 2.4 Balbucios de Diálogo Estilo Animal Crossing / Zelda
Para enriquecer a leitura do diálogo:
- O texto aparece com efeito máquina de escrever. A cada 3 caracteres emitidos, um blip sonoro é disparado.
- **Mapeamento Pentatônico por Caractere**:
  - O código ASCII do caractere é mapeado na escala pentatônica maior:
    $$\text{nota} = \text{EscalaPentatonica}[\text{char.charCodeAt}(0) \pmod 5]$$
  - Isso garante que a "fala" dos personagens sempre cante uma melodia harmoniosa e jamais produza ruídos desafinados.
- **Timbres Distintos por Personagem**:
  - **Toadbigode**: Frequência base $\sim 500\text{Hz}$, onda triangular, pitch ramp ascendente rápido ($+15\%$), timbre alegre, saltitante e curioso.
  - **Davi Chibi**: Frequência base $\sim 260\text{Hz}$, mistura de seno e triângulo, timbre aconchegante, afetuoso e calmo.
  - **Carta do Coração / Altar**: Frequência base $\sim 780\text{Hz}$, sino de cristal etéreo com harmônico brilhante.

---

### 2.5 Catálogo Completo de Efeitos Sonoros (SFX)

1. **Passos Melódicos (`playStepSound`)**:
   - Alternância de notas sutis em escala pentatônica ($E4, G4, A4, C5$) com envoltória de 90ms e ganho muito baixo (0.02), dando sensação lúdica aos passos de Ana pelo gramado, estrelas e teclas de piano.
2. **Brilho de Descoberta / Interação (`playSparkle`)**:
   - Arpejo ascendente veloz em cascata ($C6, E6, G6, B6, C7$) com ressonância estéreo e decaimento cristalino de 1.8s.
3. **Puff Mágico da Revelação do Davi (`playMagicPoof`)**:
   - Estouro de ruído branco com filtro dinâmico varrendo de 2500Hz para 180Hz em 0.35s + sino grave de confirmação emocional.
4. **Acendimento das Velas do Bolo (`playCandleIgnite`)**:
   - Varredura ascendente de sopro suave (noise sweep) seguida por brilho dourado ressonante.
5. **Fanfarra do Abraço & Parabéns (`playCelebrationFanfare`)**:
   - Acorde aberto triunfal ($C - G - C - E - G$ em oitavas superiores) com sustentação calorosa e cintilação de partículas sonoras.

---

### 2.6 Gerenciamento do Ciclo de Vida do AudioContext & Autoplay
- **Política de Autoplay dos Navegadores**: O `AudioContext` inicia no estado `suspended` e só pode ser ativado após o primeiro clique ou toque na tela.
- **Tela de Entrada Acolhedora**:
  - Modal suave: *"Clique para Começar o Passeio 🎵"* ou botão *"Tocar com Música"*.
  - Ao primeiro clique: execução imediata de `audioCtx.resume()`, rampa suave de volume master (0 para 0.45 em 1.5s via `linearRampToValueAtTime`).
- **Controles de Volume & Mute**:
  - Botão flutuante estilizado de som no canto superior direito (`🔊` / `🔇`).
  - Tecla de atalho `M` para alternar mudo instantaneamente.
  - Ao mutar, `masterGain.gain.setValueAtTime(0, ctx.currentTime)`. Ao desmutar, restaura suavemente.
- **Tratamento de Mudança de Aba (`visibilitychange`)**:
  - Quando a aba do navegador fica em segundo plano, o áudio é suspenso (`audioCtx.suspend()`). Ao retornar, é retomado (`audioCtx.resume()`), prevenindo sobrecarga ou sustos ao trocar de abas.

---

## 3. Pilar 2: Storybook UI/UX System (Zero UI Slop)

O design de interface deve ser impecável, transmitindo o sentimento artesanal de um conto de fadas interativo da Nintendo (Zelda: The Wind Waker / Animal Crossing) e Studio Ghibli.

### 3.1 Identidade Visual & Design Tokens

```css
:root {
    /* Paleta de Cores Storybook */
    --color-parchment-bg: #fffdfa;
    --color-parchment-border: #f6d285;
    --color-parchment-shadow: rgba(146, 64, 14, 0.12);
    --color-text-main: #292524;
    --color-text-muted: #78716c;
    --color-primary-gold: #f59e0b;
    --color-rose-soft: #f472b6;
    --color-lavender-glow: #c084fc;
    --color-night-card: rgba(19, 23, 34, 0.90);
    
    /* Tipografia */
    --font-heading: 'Fredoka', 'Quicksand', cursive, sans-serif;
    --font-body: 'Quicksand', 'Nunito', sans-serif;
    --font-handwriting: 'Caveat', 'Dancing Script', cursive;
    
    /* Cantos e Sombras Orgânicas */
    --radius-modal: 26px;
    --radius-button: 18px;
    --radius-avatar: 50%;
    --shadow-cozy: 0 16px 36px -6px rgba(0, 0, 0, 0.16), 0 4px 12px rgba(245, 158, 11, 0.10);
    --shadow-button: 0 6px 0px #d97706, 0 10px 20px rgba(0, 0, 0, 0.15);
}
```

### 3.2 Tipografia & Carregamento Resiliente
- **Fontes Google Fonts**: `@import url('https://fonts.googleapis.com/css2?family=Caveat:wght@600;700&family=Fredoka:wght@500;600;700&family=Quicksand:wght@500;600;700&display=swap');`
- **Fallback Web-Safe Robusto**: Fallbacks declarados (`'Segoe UI', 'Trebuchet MS', sans-serif`) para manter a legibilidade perfeita mesmo em ambientes offline ou restritos.

---

### 3.3 Caixa de Diálogo Interativa com Efeito Typewriter

*Estrutura HTML do Componente*:
```html
<div id="dialogue-container" class="dialogue-container hidden">
    <div class="dialogue-parchment">
        <!-- Avatar do Personagem com Moldura Dourada -->
        <div class="dialogue-avatar-wrapper">
            <div id="dialogue-avatar" class="dialogue-avatar">🍄</div>
            <div class="avatar-glow"></div>
        </div>
        
        <!-- Conteúdo do Diálogo -->
        <div class="dialogue-content">
            <div class="dialogue-header">
                <span id="dialogue-speaker" class="dialogue-speaker">Toadbigode</span>
                <span class="dialogue-chapter-badge" id="dialogue-badge">Fase 1</span>
            </div>
            <div id="dialogue-text" class="dialogue-text"></div>
            
            <div class="dialogue-footer">
                <span class="dialogue-hint">Pressione [Espaço] ou clique para avançar</span>
                <button id="dialogue-next-btn" class="squishy-btn primary-btn">
                    <span>Avançar</span>
                    <span class="btn-arrow">➜</span>
                </button>
            </div>
        </div>
    </div>
</div>
```

*Comportamentos Chave*:
1. **Digitação com Typewriter Sincronizado**:
   - Velocidade: 22ms por caractere.
   - Blips melódicos emitidos dinamicamente conforme cada caractere é desenhado.
2. **Aceleração Instantânea (Fast-Forward)**:
   - Se o usuário clica ou pressiona `Espaço`/`Enter` enquanto o texto está digitando, o texto é completado instantaneamente.
   - Um segundo clique avança para a próxima fala.
3. **Botão Bouncy / Squishy com Curva de Mola**:
   ```css
   .squishy-btn {
       transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.2s ease;
   }
   .squishy-btn:hover {
       transform: translateY(-3px) scale(1.03);
   }
   .squishy-btn:active {
       transform: translateY(3px) scale(0.97);
       box-shadow: 0 2px 0px #b45309;
   }
   ```

---

### 3.4 Pergaminho Animado da Carta de Amor (Fase 3 & Releitura)

No clímax da Fase 3, após o abraço carinhoso do Davi na Ana Gleice, a grande Carta de Amor é revelada em um pergaminho desdobrável.

*Recursos do Modal da Carta*:
- **Animação de Desdobramento (`unrollScroll`)**:
  - Efeito suave de abertura vertical com selo de cera com coração vermelho gravado (`❤️`).
- **Tipografia Caligráfica e Carinhosa**:
  - Utilização da fonte `Caveat`/`Dancing Script` com espaçamento confortável e detalhes dourados.
- **Conteúdo Completo dos 3 Motivos**:
  - Reúne os 3 motivos pelos quais o Davi ama a Ana Gleice:
    1. *A Arte e a Sensibilidade Musical*: A paixão de Ana pela música colorindo o mundo de poesia e doçura.
    2. *A Força Serena e a Doçura*: O abraço acolhedor de Ana que acalma qualquer tempestade e ilumina os dias difíceis como uma estrela-guia.
    3. *A Certeza da Vida Compartilhada*: A celebração de todos os aniversários futuros e o amor infinito que une os dois.
- **Botão Permanente no HUD: "💌 Ler Carta Novamente"**:
  - Um botão sutil e charmoso permanece no canto superior do HUD. Ana pode reler a carta de amor quantas vezes desejar, a qualquer momento após o término da história!

---

### 3.5 Recurso de Captura de Foto/Screenshot com Moldura de Lembrança (Polaroid)

A aniversariante pode eternizar o momento do abraço ou qualquer cenário do jogo gerando uma foto em alta definição para guardar ou postar.

*Pipeline de Renderização e Composição*:
1. **Configuração Three.js**:
   - `new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: true })`
   - O parâmetro `preserveDrawingBuffer: true` garante que o buffer de cores do WebGL permaneça legível para exportação sem renderizar frames pretos.
2. **Canvas 2D Offscreen de Composição**:
   - Cria um elemento `HTMLCanvasElement` em memória com resolução elevada (ex: 1920x1080 ou 2x DPI da tela).
   - Desenha a imagem do WebGL no canvas offscreen.
3. **Decoração Moldura de Lembrança (Polaroid / Storybook)**:
   - Adiciona moldura sutil com margem inferior creme (`#fefaf0`).
   - Inscreve em dourado e marrom café:
     - Título: *"A Sinfonia de Ana Gleice 🌸"*
     - Subtítulo: *"Com todo amor do mundo, do seu Davi (Toadbigode) • 22 de Outubro de 2026"*
     - Decorações nos cantos com pequenas flores e notas musicais.
4. **Download Instantâneo sem Servidor**:
   - Converte o canvas para Blob (`canvas.toBlob(...)`) com formato `image/png`.
   - Gera URL temporária (`URL.createObjectURL(blob)`), cria um elemento `<a download="Lembranca_Aniversario_Ana_e_Davi.png">` e dispara o clique.
5. **Feedback Visual (Toast Notification)**:
   - Exibe no topo da tela um aviso adorável com ícone de câmera: *"📸 Foto salva com sucesso na sua galeria!"* com som de obturador suave.

---

### 3.6 Controles Mobile Responsivos (Touch Overlay)

Para possibilitar uma experiência fluida no smartphone de Ana Gleice:
- **Joystick Analógico Virtual (Inferior Esquerdo)**:
  - Base circular semi-translúcida (`touch-zone`) com botão direcionador interno móvel.
  - Calcula o vetor deslocamento normalizado $(\Delta x, \Delta y)$ limitado ao raio máximo de 45px.
  - Alimenta diretamente a velocidade e rotação da personagem Ana.
- **Botão de Ação Tátil (Inferior Direito)**:
  - Botão grande e macio com ícone de balão de diálogo / faísca (`💬` / `✨`).
  - Permite interagir com o Toadbigode, ativar os altares e avançar o diálogo sem tocar no meio da tela.
- **Adequação Ergonômica**:
  - Zonas de toque dimensionadas segundo as diretrizes de acessibilidade mobile (mínimo de $48 \times 48$ CSS pixels).
  - Prevenção de gestos padrão indesejados (`touch-action: none; user-select: none`).

---

## 4. Pilar 3: Estrutura do Projeto & Arquitetura de Testes Automatizados E2E

### 4.1 Estrutura Standalone do Projeto em `ana_birthday_quest/`

O projeto é concebido como uma aplicação web moderna, modular e autocontida:

```
ana_birthday_quest/
├── index.html                   # Documento raiz com canvas Three.js e containers de UI
├── package.json                 # Dependências de teste (Playwright), scripts de execução
├── playwright.config.js         # Configuração de testes E2E headless multi-browser
├── css/
│   └── style.css                # Sistema de design Storybook, botões, modais e responsividade
├── js/
│   ├── main.js                  # Inicializador, loop de renderização (requestAnimationFrame), transições
│   ├── audio.js                 # Engine Web Audio procedural (Sintetizadores, BGM, SFX)
│   ├── dialogue.js              # Script narrativo dos 3 motivos, máquina de escrever, modais
│   ├── models.js                # Geometrias procedurais 3D de Ana, Toadbigode, Davi e fases
│   ├── controls.js              # Gerenciador de input (Teclado, Joystick Touch, Câmera orbital)
│   └── capture.js               # Gerador de screenshot com moldura Polaroid souvenir
└── tests/
    ├── e2e/
    │   ├── 01_boot_sanity.spec.js      # Boot, WebGL, inicialização de áudio e DOM
    │   ├── 02_movement_levels.spec.js  # Movimentação e transições Fase 1 -> 2 -> 3
    │   ├── 03_dialogue_narrative.spec.js# Diálogos, 3 Motivos, Revelação do Davi e Abraço
    │   ├── 04_audio_engine.spec.js     # Validação da síntese procedural e botão mudo
    │   └── 05_souvenir_letter.spec.js  # Captura de foto e releitura da carta de amor
    └── unit/
        ├── audio_frequencies.test.js   # Validação matemática de frequências e escalas
        └── narrative_integrity.test.js # Validação textual dos 3 motivos e roteiro
```

---

### 4.2 Harness de Testes Determinístico: `window.__GAME_STATE__`

Para garantir que robôs de teste automatizados (como Playwright ou Puppeteer) possam validar todas as funcionalidades sem necessidade de "adivinhar" animações ou trapacear:

```javascript
window.__GAME_STATE__ = {
    // Estado do Ciclo do Jogo
    isReady: true,
    currentLevel: 1, // 1, 2, 3
    isLevelTransitioning: false,

    // Estado da Personagem e Câmera
    player: {
        x: 0,
        y: 0,
        z: 0,
        isMoving: false,
        speed: 0
    },

    // Estado Narrativo & Personagens Secundários
    toadbigode: {
        isRevealed: false,  // true na Fase 3 após transformação
        isHugging: false    // true durante animação do abraço
    },
    dialogue: {
        isActive: false,
        currentQueueKey: null,
        currentIndex: 0,
        isTyping: false,
        speaker: null,
        finishedCount: 0
    },

    // Estado do Motor de Áudio
    audio: {
        isContextRunning: false,
        isMuted: false,
        volume: 0.45,
        currentStageTrack: 1,
        totalNotesSynthesized: 0
    },

    // Estado de Modais e UI
    ui: {
        letterModalOpen: false,
        photoCapturedCount: 0,
        unmutePromptVisible: true
    },

    // Ações Programáticas Expostas para Testes Headless
    actions: {
        // Inicialização e Mute
        initAudio: () => window.musicEngine.init(),
        toggleMute: () => window.musicEngine.toggleMute(),

        // Navegação e Transição Direta
        warpToLevel: (levelNum) => window.gameApp.loadStage(levelNum),
        teleportPlayer: (x, z) => window.gameApp.teleportPlayer(x, z),

        // Controle de Diálogo
        startDialogue: (key) => window.dialogueSystem.startDialogue(key),
        advanceDialogue: () => window.dialogueSystem.advance(),
        closeDialogue: () => window.dialogueSystem.closeDialogue(),

        // Eventos Especiais da Fase 3
        triggerReveal: () => window.gameApp.triggerDaviReveal(),
        triggerHug: () => window.gameApp.triggerDaviHug(),
        openLoveLetter: () => window.gameApp.openLoveLetterModal(),
        closeLoveLetter: () => window.gameApp.closeLoveLetterModal(),

        // Captura de Tela
        takeScreenshot: () => window.captureManager.captureSnapshot()
    }
};
```

---

### 4.3 Especificação dos Testes Automatizados E2E (5 Quality Tiers)

A suíte Playwright é dividida em 5 níveis rigorosos de verificação:

#### Tier 1: Sanity & Boot Verification (`01_boot_sanity.spec.js`)
- [ ] Carregamento da página com código de status HTTP 200.
- [ ] Ausência de erros não tratados no console (`page.on('console', msg => ...)` e `page.on('pageerror', ...)`).
- [ ] Elemento `<canvas id="webgl-canvas">` montado e com contexto WebGL2/WebGL ativo.
- [ ] Taxa de quadros estável (mínimo 50-60 FPS detectados via `performance.now()`).
- [ ] Elementos da interface (botão de som, botão de foto, containers de diálogo) presentes na árvore DOM.

#### Tier 2: Movimentação & Progressão de Fases (`02_movement_levels.spec.js`)
- [ ] Ana Gleice responde às teclas `KeyW`, `KeyA`, `KeyS`, `KeyD` e setas com alteração na posição `window.__GAME_STATE__.player`.
- [ ] Colisão/gatilho com o Altar da Harpa na Fase 1 inicia o diálogo do Motivo 1.
- [ ] Cruzar o portal leva à Fase 2 (Planetário dos Sonhos Cósmicos), verificando `window.__GAME_STATE__.currentLevel === 2`.
- [ ] Cruzar o portal estelar da Fase 2 leva à Fase 3 (Palácio das Cerejeiras), verificando `window.__GAME_STATE__.currentLevel === 3`.

#### Tier 3: Narrativa, Revelação e Abraço (`03_dialogue_narrative.spec.js`)
- [ ] Verificação da presença do texto do **Motivo 1** (arte e música de Ana).
- [ ] Verificação da presença do texto do **Motivo 2** (força e doçura como estrela-guia).
- [ ] Na Fase 3, gatilho do clímax ativa a transformação de Toadbigode no Davi (`window.__GAME_STATE__.toadbigode.isRevealed === true`).
- [ ] Acionamento do abraço carinhoso com animação aproximando os modelos 3D (`window.__GAME_STATE__.toadbigode.isHugging === true`).
- [ ] Desdobramento da grande carta de amor do **Motivo 3**.

#### Tier 4: Motor de Áudio Procedural (`04_audio_engine.spec.js`)
- [ ] `actions.initAudio()` transiciona o `AudioContext.state` para `'running'`.
- [ ] Disparo de notas musicais do piano e violão incrementa `totalNotesSynthesized` sem lançar exceções numéricas (como valores `NaN` ou `Infinity` no ganho).
- [ ] Blips melódicos disparam em sincronia durante a máquina de escrever.
- [ ] Alternância de mudo altera `masterGain.gain.value` para 0 e de volta para 0.45.
- [ ] Mudança de aba (`document.visibilityState = 'hidden'`) suspende o áudio corretamente.

#### Tier 5: Captura de Foto & Modal da Carta (`05_souvenir_letter.spec.js`)
- [ ] `actions.takeScreenshot()` gera um PNG válido, com cabeçalho de assinatura de imagem PNG (`89 50 4E 47`) e tamanho superior a 50KB.
- [ ] Modal da Carta de Amor pode ser reaberto pelo botão *"Ler Carta Novamente"* no HUD após o encerramento do diálogo, garantindo acesso perpétuo para a Ana.
- [ ] Verificação de responsividade visual nos viewports Desktop ($1920 \times 1080$), Tablet ($768 \times 1024$) e Mobile ($375 \times 812$).

---

## 5. Roteiro de Implementação & Guia para os Próximos Agentes (WBS)

Para a fase de implementação imediata:

1. **Criação do `index.html`**:
   - Montar o esqueleto HTML com o canvas `#webgl-canvas`, containers da UI storybook, overlays para mobile, modais de diálogo e carta, e inclusão dos scripts via módulos ou scripts clássicos bem ordenados.
2. **Refinamento do `audio.js`**:
   - Evoluir a classe `MusicEngine` adicionando os métodos de síntese aditiva para o piano acústico (`playAcousticPiano`), filtro de corda para o violão (`playNylonPluck`), pads quentes com LFO (`playLofiPad`) e os efeitos especiais (`playMagicPoof`, `playCandleIgnite`, `playSparkle`).
3. **Criação do `css/style.css`**:
   - Implementar todas as regras visuais storybook, paleta de cores pergaminho, botões com efeito mola, animações de abertura de pergaminho e responsividade mobile.
4. **Criação do `js/capture.js`**:
   - Implementar o compositor de foto souvenir Polaroid unindo a cena 3D do canvas e a moldura decorada com tipografia dourada.
5. **Criação do `js/controls.js`**:
   - Implementar os handlers de teclado, joystick virtual touch e movimentação suave da personagem.
6. **Integração no `js/main.js`**:
   - Orquestrar o Three.js, os modelos procedurais de `models.js`, o fluxo narrativo de `dialogue.js`, o som de `audio.js` e expor `window.__GAME_STATE__`.
7. **Configuração da Suíte Playwright**:
   - Instalar `@playwright/test` e rodar os 5 tiers automatizados garantindo 100% de sucesso.

---

## 6. Conclusão
A arquitetura aqui delineada garante que a experiência de Ana Gleice seja emocionante, tecnicamente impecável, artisticamente tocante e 100% livre de falhas de carregamento ou bugs visuais/sonoros. A ausência de assets externos confere robustez total, e os ganchos determinísticos asseguram cobertura total pelos testes automatizados.
