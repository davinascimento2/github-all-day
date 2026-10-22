# Technical Survey & Architectural Specification: Characters, Animation, Levels & Narrative

**Agent**: Explorer Survey 2 (Characters, Animation, Levels & Narrative Explorer)  
**Date**: 2026-10-22  
**Target Project**: `ana_birthday_quest` — 3D Standalone Cozy Browser Game for Ana Gleice's Birthday  
**Document Status**: Complete & Actionable Specification  

---

## 1. Executive Summary & Design Vision

This specification defines the character architecture, animation engine, level environments, and narrative sequence for **"A Sinfonia de Ana Gleice: Uma Aventura de Aniversário"**.

### Core Philosophy
1. **Zero External 3D Asset Dependencies**:
   All 3D models (Ana Gleice, Toadbigode, and Davi Chibi) are engineered procedurally using Three.js compound primitives (`THREE.Group`, `THREE.SphereGeometry`, `THREE.CylinderGeometry`, `THREE.TorusGeometry`, etc.) and styled with soft toon/cel materials. This guarantees **instant zero-latency loading**, avoids broken external CDN links or CORS errors, works offline or via local HTTP, and runs at a buttery **60 FPS** on both desktop and mobile browsers.
2. **Ghibli & Nintendo Cozy Storybook Aesthetic**:
   Visual proportions follow a 1:2.3 Chibi/Nendoroid ratio with expressive facial features, pastel color grading, soft bounce physics, and harmonic audiovisual resonance (each step and interaction chimes in pentatonic harmony).
3. **Emotional Climax & Story Arc**:
   The 3-stage progression guides Ana through three thematic realms celebrating three distinct reasons why Davi loves her, culminating in the magical reveal of Toadbigode as Davi and a tender 3D hug under a moonlit cherry blossom garden with an interactive piano and birthday cake.

---

## 2. 3D Procedural Chibi Character Models

### 2.1. Ana Gleice (The Protagonist & Musician)

#### Anatomy & Dimensions
- **Scale**: Height ~1.45 units (Y-axis), Width ~0.65 units, Depth ~0.50 units.
- **Proportion**: 1:2.3 head-to-body ratio (Chibi / Animal Crossing / Zelda Wind Waker aesthetic).
- **Pivot Point**: Base between feet at `(0, 0, 0)`.

```
                    [ Treble Clef Hairpin (Gold #fbbf24) ]
                             ( \
                     ___..---""---..___
                  .-"      (Hair)      "-.
                .'   #3b2219 Brunette     '.
               /   .-----.        .-----.   \
              ;   / (Eye) \      / (Eye) \   ;  <-- Eyes: Espresso #2d1810 + Highlights
              |   |  (o)  |      |  (o)  |   |
              |    '-----'        '-----'    |
              ;     ( ' ) Blush    ( ' )     ;  <-- Blush: Peach Pink #ff99aa
               \         \__  __/           /   <-- Sweet Smile #c26a76
                '.          ""            .'
                  '--..______________..--'
                            |  |  (Neck)
                       .----+--+----.
                      /   (Cardigan) \
                     /  #c8b6ff Soft  \
                    /    Lavender      \
                   /____________________\
                   \   (Flared Skirt)   /
                    \   #e0b0ff Pastel /
                     \________________/
                          |      |
                         [Leg]  [Leg]   <-- White Socks #fffaf0
                         [Shoe] [Shoe]  <-- Mary Jane Shoes #452820
```

#### Node Hierarchy & Three.js Primitives
- **`anaGroup`** (`THREE.Group`): Root anchor.
  - **`bodyGroup`** (`THREE.Group`, pivot `y = 0.55`):
    - `torsoMesh`: `THREE.CylinderGeometry(0.18, 0.24, 0.35, 16)`, Material: Pastel Lavender (`#c8b6ff`, Toon/Standard, roughness 0.6).
    - `skirtMesh`: `THREE.ConeGeometry(0.38, 0.35, 20, 1, true)`, inverted/tapered downward flare with white petticoat trim line (`#fffaf0`).
    - `collarMesh`: `THREE.TorusGeometry(0.16, 0.03, 8, 16)`, crisp white Peter Pan collar (`#ffffff`).
  - **`headGroup`** (`THREE.Group`, pivot `y = 0.95`):
    - `headMesh`: `THREE.SphereGeometry(0.42, 24, 20)`, scaled `(1.05, 0.98, 1.0)`, warm soft skin tone (`#ffe4d6`).
    - **Face Features**:
      - `leftEye` / `rightEye`: `THREE.SphereGeometry(0.065, 16, 16)` flattened on Z (`scale(1.0, 1.3, 0.3)`), dark espresso brown (`#2d1810`).
      - `eyeSparkleL` / `eyeSparkleR`: Tiny white reflective sphere dots (`SphereGeometry(0.018)`, `#ffffff`) offset to top-right of pupils.
      - `leftBlush` / `rightBlush`: Subtle circular discs (`CircleGeometry(0.055, 16)`), soft peach pink (`#ff99aa`, opacity 0.55).
      - `mouth`: Soft smiling curve (`TorusGeometry(0.04, 0.01, 8, 12, Math.PI)`), soft berry tone (`#c26a76`).
    - **Hair Sculpt**:
      - `hairBase`: Helmet dome `THREE.SphereGeometry(0.45, 24, 20)` over top and back of head, rich warm brunette (`#3b2219`).
      - `bangs`: 3 curved overlapping ellipsoids (`SphereGeometry(0.14, 12, 12)` scaled `(1.2, 0.6, 0.4)`) framing the forehead.
      - `sideLocks`: Flowing hair strands framing the cheeks down to collar level (`CylinderGeometry(0.04, 0.02, 0.35)`).
      - `backWaves`: Soft wavy volume behind shoulders (`SphereGeometry(0.28)` layered down to upper back).
    - **Musical Accessory**:
      - `hairpin`: A golden musical hairpin shaped as a G-clef / treble clef (`#fbbf24`, metallic 0.4, roughness 0.3) positioned on the right temple with a tiny pink gemstone.
  - **`leftArmGroup` / `rightArmGroup`** (`THREE.Group`, pivots at `y = 0.82, x = ±0.26`):
    - `armMesh`: `THREE.CylinderGeometry(0.05, 0.045, 0.32, 12)`, sleeve color `#c8b6ff`.
    - `handMesh`: `THREE.SphereGeometry(0.05, 12, 12)`, skin tone (`#ffe4d6`).
  - **`leftLegGroup` / `rightLegGroup`** (`THREE.Group`, pivots at `y = 0.35, x = ±0.12`):
    - `legMesh`: `THREE.CylinderGeometry(0.05, 0.045, 0.30, 12)`, cream socks (`#fffaf0`).
    - `shoeMesh`: `THREE.BoxGeometry(0.08, 0.06, 0.12)` rounded with toe sphere, warm chocolate leather (`#452820`).
  - **`musicalAuraGroup`** (`THREE.Group`):
    - 4 floating translucent musical notes (treble clefs, quavers `#f472b6` & `#fbbf24`) hovering and orbiting lightly around Ana with glowing particle sparkles.

---

### 2.2. Toadbigode (The Royal Mushroom Advisor)

#### Role & Character Archetype
- Toadbigode represents Davi's affectionate, playful guardian alter-ego who accompanies Ana through Levels 1 and 2, speaking with wisdom and humor, while secretly holding the greatest love surprise.
- Proportions: Squat, round, ultra-chibi (Height ~1.15 units).

```
                      ___...-----...___
                   .-"   (Red Cap)     "-.
                 .'    #e11d48 Crimson    '.
                /   (o)   Spots: #fffbeb    \
               |     White Polka Dots    (o) |
               ;                             ;
                \       .-----------.       /
                 '.____/   Rim #fff  \____.'
                     |  (Stem Face)  |
                     |  o         o  |  <-- Cheerful Beady Eyes
                     |     (===)     |  <-- GLORIOUS MUSTACHE! ("Bigode")
                     '---._______.---'
                        / [Vest] \     <-- Royal Navy Vest #1e3a8a + Gold Trim
                       (  [Pants] )    <-- White Puffy Diaper-Pants #f8fafc
                        '-[Shoes]-'
```

#### Node Hierarchy & Three.js Primitives
- **`toadbigodeGroup`** (`THREE.Group`): Root anchor.
  - **`capGroup`** (`THREE.Group`, pivot `y = 0.70`):
    - `capDome`: `THREE.SphereGeometry(0.55, 24, 16)` cut in half / squashed vertically (`scale(1.15, 0.82, 1.15)`), vibrant crimson-magenta (`#e11d48`).
    - `capRim`: `THREE.TorusGeometry(0.50, 0.08, 12, 24)`, puffy white cotton underside (`#fffbeb`).
    - `spots`: 5 circular white discs (`THREE.CircleGeometry(0.12, 16)` flush along the cap surface, `#fffbeb`).
  - **`headStem`** (`THREE.CylinderGeometry(0.32, 0.36, 0.38, 20)`, pivot `y = 0.52`):
    - Soft ivory mushroom stem (`#fffbf0`).
    - `eyes`: Two cute vertical black oval capsules (`SphereGeometry(0.04, 12, 12)` scaled `(0.7, 1.4, 0.3)`), with white pinprick specular dots.
    - `blush`: Soft glowing pink cheeks (`#fb7185`, opacity 0.5).
    - **The Signature Bigode (Mustache)**:
      - Stylized curved handlebar mustache beneath the eyes (`TorusGeometry(0.07, 0.025, 8, 16, Math.PI)` inverted with flared curved tips, `#2b1810`).
      - Connected to dialogue typing blips to wiggle cheerfully!
  - **`bodyGroup`** (`THREE.Group`, pivot `y = 0.28`):
    - `vestMesh`: Royal navy blue vest (`#1e3a8a`) with bright golden piping (`#fbbf24`).
    - `bowtieMesh`: Tiny dapper red bow tie (`#ef4444`) centered under the mustache.
    - `pantsMesh`: Puffy spherical pants (`SphereGeometry(0.30, 16, 14)` scaled `(1.1, 0.8, 1.0)`, `#f8fafc`).
    - `feet`: Two rounded stubby brown shoes (`SphereGeometry(0.08)` scaled `(1.0, 0.6, 1.4)`, `#78350f`).

---

### 2.3. Davi Chibi (The Grand Reveal & Hug Partner)

#### Role & Revelation
- In Level 3 (`stage3_climax`), Toadbigode reveals: *"Sempre fui eu, meu amor!"* A vortex of golden sparkles and heart particles envelops Toadbigode. The mushroom model dissolves, and Davi Chibi emerges.
- Proportions: Matches Ana's chibi aesthetic, slightly taller (~1.54 units), broader shoulders, warm boyfriend attire.

```
                     ___..---""---..___
                  .-"   (Wavy Hair)    "-.
                .'   #1f1610 Dark Waves   '.
               /   .-----.        .-----.   \
              ;   |[Glasses]    [Glasses]|   ;  <-- Dark-rimmed Glasses #1e1e1e
              |   |  (o)  |======|  (o)  |   |  <-- Warm Loving Eyes
              |    '-----'        '-----'    |
              ;          { Mustache }        ;  <-- Charming Davi Mustache
               \         \__________/       /   <-- Loving Gentle Smile
                '.                        .'
                  '--..______________..--'
                            |  |
                       .----+--+----.
                      /  (Knit      \
                     /    Cardigan/  \   <-- Warm Burgundy/Navy Sweater
                    /     Sweater)    \
                   /___________________\
                   |    |         |    |
                   |Dark Trousers |    |  <-- Slate Navy Trousers #1e293b
                   |____|         |____|
                   [Shoe]         [Shoe]  <-- Clean Casual Sneakers #f1f5f9
```

#### Node Hierarchy & Three.js Primitives
- **`daviGroup`** (`THREE.Group`): Root anchor (hidden until reveal, then animated into scene).
  - **`bodyGroup`** (`THREE.Group`, pivot `y = 0.60`):
    - `sweaterMesh`: `THREE.CylinderGeometry(0.22, 0.23, 0.42, 16)`, cozy knit burgundy (`#7f1d1d`) or deep midnight navy (`#1e293b`).
    - `shirtCollar`: Tiny white triangular collar peeking out of the sweater neckline (`#f8fafc`).
    - `trouserMesh`: Slate dark trousers (`#1e293b` or `#334155`).
    - `sneakers`: Stylized white/gum-sole sneakers (`#f8fafc` base, `#d97706` sole accent).
  - **`headGroup`** (`THREE.Group`, pivot `y = 1.02`):
    - `headMesh`: `THREE.SphereGeometry(0.44, 24, 20)`, warm skin tone (`#fed7aa`).
    - `glasses`: Stylish dark-rimmed frames (`THREE.TorusGeometry(0.07, 0.015, 8, 16)` for each eye, linked by a bridge `CylinderGeometry(0.01, 0.01, 0.08)`, `#171717`).
    - `eyes`: Gentle, affectionate dark eyes with warm reflections.
    - `mustache`: Neat, charming dark mustache (`#1c1917`) matching Davi's signature look and the "toadbigode" origin.
    - `hair`: Rich textured wavy/curly dark hair (`#1c1917`) with volumetric stylized clumps (`SphereGeometry` clusters sculpted across crown and temples).
  - **`leftArmGroup` / `rightArmGroup`** (`THREE.Group`, articulated for the hug):
    - Shoulders at `y = 0.88, x = ±0.29`.
    - Double-jointed hierarchy: `upperArm` (`CylinderGeometry`), `forearm` (`CylinderGeometry`), `hand` (`SphereGeometry`).
    - Enables wide open arms during greeting and complete forward wrap-around embrace during the hug.

---

## 3. Character Controller, Camera & Animation Engine

### 3.1. Unified Input System (Desktop & Mobile)
- **Desktop Controls**:
  - `KeyW` / `ArrowUp`: Move Forward (-Z)
  - `KeyS` / `ArrowDown`: Move Backward (+Z)
  - `KeyA` / `ArrowLeft`: Move Left (-X)
  - `KeyD` / `ArrowRight`: Move Right (+X)
  - `ShiftLeft` / `ShiftRight`: Gentle Run / Sprint (+40% speed)
  - `Space` / `KeyE`: Interact / Talk / Advance Dialogue
- **Mobile Touch Controls**:
  - Floating virtual joystick on the lower-left screen quadrant.
  - On touch start: joystick center anchors to touch location.
  - On touch move: drag vector calculated, normalized, clamped to radius (60px), with deadzone (10px).
  - On touch end: snaps back to center with zero velocity.
  - Dedicated floating squishy "Ação" / "Conversar" button on bottom-right for interactions.

### 3.2. Kinematic Motion & Physics Math
```javascript
// Input vector normalization
const rawInput = new THREE.Vector2(0, 0);
if (keys.KeyW || keys.ArrowUp) rawInput.y -= 1;
if (keys.KeyS || keys.ArrowDown) rawInput.y += 1;
if (keys.KeyA || keys.ArrowLeft) rawInput.x -= 1;
if (keys.KeyD || keys.ArrowRight) rawInput.x += 1;

// Merge with touch joystick vector
if (touchJoystick.active) {
    rawInput.x += touchJoystick.vector.x;
    rawInput.y += touchJoystick.vector.y;
}

if (rawInput.lengthSq() > 1.0) rawInput.normalize();

const isMoving = rawInput.lengthSq() > 0.01;
const targetSpeed = isMoving ? (keys.ShiftLeft ? 6.2 : 4.2) : 0.0;

// Smooth acceleration / deceleration
currentSpeed = THREE.MathUtils.lerp(currentSpeed, targetSpeed, delta * 10.0);

if (isMoving) {
    // Smooth angular slerp towards moving direction
    const targetAngle = Math.atan2(rawInput.x, rawInput.y);
    playerGroup.rotation.y = THREE.MathUtils.lerpAngle(playerGroup.rotation.y, targetAngle, delta * 12.0);
    
    // Position displacement
    const moveX = Math.sin(playerGroup.rotation.y) * currentSpeed * delta;
    const moveZ = Math.cos(playerGroup.rotation.y) * currentSpeed * delta;
    
    // Level boundary collision check
    const nextPos = playerGroup.position.clone().add(new THREE.Vector3(moveX, 0, moveZ));
    if (isValidPosition(nextPos, currentLevel)) {
        playerGroup.position.copy(nextPos);
    }
}
```

### 3.3. Third-Person Storybook Camera System
- **Tracking Mode**:
  - Desired camera position = `player.position + Vector3(0, 3.8, 6.2)`.
  - Camera follows with smooth damp: `camera.position.lerp(desiredCamPos, delta * 4.5)`.
  - Look target = `player.position + Vector3(0, 0.9, 0)`.
- **Cinematic Cutscene Transitions**:
  - During dialogue triggers and the Level 3 finale, the camera smoothly decouples from behind the player.
  - It moves along a cubic Bézier curve to focus on a romantic 3/4 angle framing Ana, Toadbigode/Davi, and the landmark.
  - During the Hug sequence, the camera enters an orbital rotation (`angle += delta * 0.25`), slowly panning around the couple while soft pink petals and heart particles swirl upward.

---

### 3.4. Procedural Animation Engine (Zero Skeletal File Overhead)

#### A. Idle Breathing Cycle
- **Cycle Rate**: ~2.6 rad/sec.
- **Torso**: `bodyGroup.scale.y = 1.0 + Math.sin(t * 2.6) * 0.025`
- **Head**: `headGroup.position.y = baseHeadY + Math.sin(t * 2.6) * 0.015`
- **Arms**: Gentle sway `leftArm.rotation.z = 0.05 + Math.sin(t * 2.6) * 0.03`
- **Hair**: Relaxed wave `hairMesh.rotation.z = Math.sin(t * 2.0) * 0.02`
- **Musical Aura**: Translucent notes hover in a gentle Lissajous orbit above Ana.

#### B. Bobbing Walk Cycle
- **Stride Frequency**: `freq = 11.0 * (currentSpeed / 4.2)`
- **Vertical Pelvis Bob**: `bodyGroup.position.y = basePelvisY + Math.abs(Math.sin(t * freq)) * 0.12`
- **Body Side-Tilt (Kawaii Waddle)**: `playerGroup.rotation.z = Math.sin(t * freq * 0.5) * 0.05`
- **Leg Swing**:
  - `leftLeg.rotation.x = Math.sin(t * freq) * 0.62`
  - `rightLeg.rotation.x = -Math.sin(t * freq) * 0.62`
- **Arm Counter-Swing**:
  - `leftArm.rotation.x = -Math.sin(t * freq) * 0.48`
  - `rightArm.rotation.x = Math.sin(t * freq) * 0.48`
- **Footstep Audio Trigger**:
  - At zero-crossing of `Math.sin(t * freq)` with downward derivative, triggers `window.musicEngine.playStepSound()`, producing a soft melodic chime.

#### C. Toadbigode Hop & Mustache Wiggle
- **Walk**: Instead of swinging legs, Toadbigode performs playful rhythmic hops:
  - `y = base + Math.max(0, Math.sin(t * 8.0)) * 0.22`
  - Squashes on landing: `scale.y = 0.88`, stretches during jump: `scale.y = 1.12`.
- **Mustache Talk Animation**:
  - When dialogue typewriter is active, the mustache rotates up and down rapidly (`rotation.x = Math.sin(t * 28.0) * 0.15`), synchronizing with Animal Crossing speech blips.

---

### 3.5. The Emotional Hug Animation Sequence (Choreography & State Machine)

The hug is the emotional summit of the game. It is triggered by `window.gameApp.triggerDaviHug()` during dialogue message 136 in `dialogue.js`.

```
================================================================================
                    THE GRAND HUG CHOREOGRAPHY TIMELINE
================================================================================

 [t = 0.0s] DIALOGUE CLIMAX TRIGGER ("Vem cá me dar um abraço bem apertado!")
    │
    ├── Player movement controls disabled (input locked).
    ├── Camera transitions to romantic low-angle three-quarter orbit.
    │
 [t = 0.5s - 1.8s] THE APPROACH
    │
    ├── Davi and Ana orient directly towards each other (smooth lookAt).
    ├── Davi opens both arms wide in a warm, welcoming gesture:
    │     leftShoulder.rotation.set(0.6, -0.6, 0.4);
    │     rightShoulder.rotation.set(0.6, 0.6, -0.4);
    ├── Ana's eyes widen with joy, cheeks blush brighter (#ff6b8b).
    ├── They take 3 gentle steps forward until distance <= 0.62 units.
    │
 [t = 1.8s - 3.2s] THE TENDER EMBRACE
    │
    ├── Davi wraps both arms around Ana's upper back/shoulders:
    │     leftArm wraps forward & inward (rotX: 0.85, rotY: -0.45, rotZ: 0.35)
    │     rightArm wraps forward & inward (rotX: 0.85, rotY: 0.45, rotZ: -0.35)
    ├── Ana raises her arms around Davi's waist/chest in a reciprocal hug:
    │     leftArm wraps forward (rotX: 0.75, rotY: -0.35)
    │     rightArm wraps forward (rotX: 0.75, rotY: 0.35)
    ├── Both heads tilt gently toward each other (head.rotation.z = ±0.14 rad),
    │   resting forehead-to-forehead / cheek-to-cheek.
    │
 [t = 3.2s - 5.0s] THE CELESTIAL CELEBRATION
    │
    ├── Heart Particle Fountain erupts from their center (swirling pink hearts).
    ├── Golden stardust rings expand outward across the grass.
    ├── Birthday Cake candles burst into warm flickering golden flames.
    ├── Confetti ribbons burst into the sky.
    ├── Web Audio plays the triumphant, emotional "Happy Birthday to Ana" hymn.
    │
 [t = 5.0s+] THE LOVE LETTER SCROLL
    │
    └── Romantic animated parchment scroll slides down with Motivo #3.
        UI buttons activate: "Tirar Foto do Abraço 📸" & "Reler Carta 💌".
```

---

## 4. Level Design & Narrative Interactive Beats

### 4.1. Level 1: O Bosque das Melodias Silvestres

#### Narrative Foundation
- **Theme**: Reason #1 — Ana's lifelong passion for music, her artistic talent, and the magical sensitivity with which she colors the world.
- **Atmosphere**: Zelda Breath of the Wild / Kokiri Forest at golden hour. Warm amber sunlight rays filtering through towering stylized trees, gentle grass, and glowing wild mushrooms.

#### Environment Layout & Coordinates
- **Play Area**: Circular woodland glade (Radius 22 units), bounded by lush dense foliage trees and moss-covered timber logs.
- **Key Landmarks**:
  1. **Spawn Point** `(0, 0, 14)`:
     - Clear grassy knoll with a rustic storybook wooden signpost: *"Caminho das Melodias"*.
  2. **Toadbigode Welcome Post** `(0, 0, 9)`:
     - Toadbigode stands on a whimsical tree stump, waving to Ana as she enters.
  3. **The Melodic Stone Path**:
     - Cobblestone stepping stones winding from `(0, 0, 9)` to `(0, 0, -8)`.
     - 5 interactive musical stones: stepping on each plays a clear harp/bell chime in C Major Pentatonic (`C4, D4, E4, G4, A4`).
  4. **The Ancient Harp Shrine (Altar da Harpa Ancestral)** `(0, 0, -10)`:
     - An ivory stone gazebo overgrown with climbing wisteria flowers.
     - Inside sits an ornate wooden harp with glowing strings that radiate soft golden light.
  5. **The Star Portal (Portal de Estrelas)** `(8, 0, -12)`:
     - Two ancient entwined cherry-birch trees forming an archway. Initially dormant, activates with a swirling celestial vortex upon finishing the Shrine dialogue.

#### Interactive Walkthrough Beats
1. **Beat 1.1 — Awakening**:
   - Ana spawns into the lush forest. Toadbigode hops towards her and automatically starts `stage1_intro`:
     *"Olá, senhorita Ana Gleice! Seja muito bem-vinda ao Bosque das Melodias Encantadas! ... Hoje é o seu dia mais especial do ano!"*
2. **Beat 1.2 — Musical Walk**:
   - Player takes control. Walking forward along the path triggers footstep chimes and flower blooms. Stepping on the 5 path stones plays a pentatonic chord progression.
3. **Beat 1.3 — The Harp Shrine & Reason 1**:
   - Approaching within 2.5 units of the Shrine triggers the interaction prompt `[E] / [Conversar]`.
   - `stage1_shrine` triggers. Toadbigode introduces the Altar, and the parchment opens with **Motivo #1**:
     *«Ana, o seu dom com a música não é apenas talento: é a forma mais pura de poesia que já vi... Eu me apaixono por essa sua sensibilidade todos os dias.»*
4. **Beat 1.4 — Celestial Portal Activation**:
   - The harp strings resonate with a rich arpeggio. Toadbigode instructs: *"Agora dê mais alguns passos em direção ao Portal de Estrelas para cruzarmos a galáxia!"*
   - Walking into the glowing Star Portal initiates a soft screen fade to cosmic lilac and transitions to Level 2.

---

### 4.2. Level 2: O Planetário dos Sonhos Cósmicos

#### Narrative Foundation
- **Theme**: Reason #2 — Ana's serene inner strength, her soothing sweetness, and how her presence shines as Davi's guiding star ("estrela-guia") through any storm.
- **Atmosphere**: Super Mario Galaxy meets The Little Prince. Deep twilight space (`#130a2a`), rotating celestial rings, lilac nebula dust, and glowing star lilies.

#### Environment Layout & Coordinates
- **Play Area**: Trio of connected floating planetoids suspended in a starry nebula.
- **Key Landmarks**:
  1. **Planetoid 1: The Arrival Moon** `(0, 0, 16)`, radius 7 units:
     - Soft turquoise moss, crystalline star-shaped flowers that glow with a gentle pulse.
  2. **The Starlight Ribbon Bridge**:
     - Translucent glowing rainbow pathway bridging Planetoid 1 to Planetoid 2 (`z = 16` to `z = 0`).
     - Floating star notes hover along the bridge; Ana collects them with pleasant high chimes.
  3. **Planetoid 2: The Constellation Observatory** `(0, 0, -4)`, radius 9 units:
     - Surrounded by a rotating golden Saturnian ring of stardust.
     - Center feature: **The Stardust Sanctuary (Anel de Poeira Estelar)** — a crystal observation platform where star constellations map out a musical clef woven into a glowing heart.
  4. **The Cosmic Launch Star** `(0, 0, -11)`:
     - A brilliant, spinning golden Launch Star pad that glows with rainbow rings.

#### Interactive Walkthrough Beats
1. **Beat 2.1 — Space Arrival**:
   - Ana touches down on the Arrival Moon. Toadbigode floats down gracefully with a tiny stardust umbrella and launches `stage2_intro`:
     *"Estamos no Planetário dos Sonhos Cósmicos! Cada pequena estrela que brilha nesta nebulosa representa um sorriso que você já colocou no rosto do Davi."*
2. **Beat 2.2 — Crossing the Starlight Ribbon**:
   - Player navigates across the celestial bridge with floaty low-gravity physics (`gravity = -9.8 * 0.6`).
   - Floating star bits chime melodically upon contact in A Lydian mode (`A4, C#5, E5, G#5`).
3. **Beat 2.3 — The Constellation Sanctuary & Reason 2**:
   - Reaching Planetoid 2, Toadbigode awaits at the center of the Stardust Ring.
   - Proximity triggers `stage2_constellation`, unveiling **Motivo #2**:
     *«O segundo motivo é a sua força serena e a sua doçura infinita... Mesmo nos dias mais difíceis, a sua voz e o seu abraço têm o poder cósmico de acalmar qualquer tempestade. Você é a minha estrela-guia...»*
4. **Beat 2.4 — Cosmic Launch Star**:
   - Toadbigode: *"Prepare-se, Ana... A última fase nos aguarda no Palácio de Cristal de Aniversário! É hora do grande momento!"*
   - Stepping onto the Launch Star catapults Ana and Toadbigode through a dazzling aurora borealis warp sequence straight into Level 3!

---

### 4.3. Level 3: O Jardim do Luar & O Grande Abraço

#### Narrative Foundation
- **Theme**: Reason #3, The Grand Reveal, and Lifelong Love Declaration.
- **Atmosphere**: Enchanted nighttime palace garden. Silvery moonlight, weeping Sakura cherry blossoms showering drifting petals, glowing lotus ponds, an interactive giant piano of light, and a majestic 3-tier birthday cake.

#### Environment Layout & Coordinates
- **Play Area**: Royal palace courtyard (Width 26 units, Depth 34 units) enclosed by marble balustrades and glowing moon lanterns.
- **Key Landmarks**:
  1. **Garden Entrance & Moon Gate** `(0, 0, 16)`:
     - Framed by two weeping Sakura trees dropping pink blossom petals.
  2. **The Piano of Light (Teclado Mágico)**:
     - Extending from `(0, 0, 12)` down to `(0, 0, 0)`.
     - 10 giant translucent crystal piano keys laid flat on the moonlit grass.
     - Stepping on key 1 through 10 lights each key up in pastel rainbow hues and plays the sequential melody of **"Parabéns pra Você"**!
  3. **The Birthday Celebration Plaza** `(0, 0, -6)`:
     - Circular terrace framed by glowing fairy lanterns, pastel gift boxes with satin ribbons, and party balloons.
  4. **The Grand Birthday Cake (O Bolo Real de Aniversário)** `(0, 0, -10)`:
     - 3-tier fairytale cake:
       * Bottom Tier: Strawberry cream pink (`#f472b6`) with sugar pearl borders.
       * Middle Tier: Vanilla chiffon ivory (`#fef08a`) with golden musical note piping.
       * Top Tier: Pastel sky blue (`#93c5fd`) with fresh strawberry toppings.
       * Golden Birthday Candles: 3 tall golden candles with unlit wicks (burst into magical flames during the climax!).
  5. **Toadbigode Standing in Front of the Cake** `(0, 0, -7)`:
     - Awaiting Ana's arrival at the end of the musical piano walkway.

#### Interactive Walkthrough Beats (The Climax Sequence)
1. **Beat 3.1 — Arrival in the Moonlight**:
   - Ana arrives at the moonlit garden. Toadbigode introduces the realm in `stage3_intro`:
     *"Seja bem-vinda ao Palácio das Cerejeiras de Aniversário! Olhe aquele bolo gigante ali na frente! O caminho até lá é feito de teclas de piano mágicas. Cada passo seu toca uma harmonia de parabéns!"*
2. **Beat 3.2 — Walking the Piano of Light**:
   - Ana walks across the luminous crystal piano keys. Each key pressed illuminates under her feet with a soft glow and chimes the birthday anthem notes via Web Audio API.
3. **Beat 3.3 — The Grand Reveal & The Great Hug**:
   - Ana reaches the Cake Plaza and talks to Toadbigode (`stage3_climax`).
   - Dialogue advances:
     * Toadbigode: *"Ana... antes de você soprar as velinhas do seu bolo, eu preciso te contar a verdade mais importante de todas..."*
     * Toadbigode: *"Eu nunca fui apenas um conselheiro... Quem esteve disfarçado cuidando de cada detalhe desta surpresa..."*
     * Dialogue event `isReveal`: **"SEMPRE FUI EU, MEU AMOR!"**
     * Golden sparkle burst envelops Toadbigode; he transforms into **Davi Chibi**!
     * Dialogue event `isHug`: **"Vem cá me dar um abraço bem apertado!"**
     * Davi and Ana step forward into the tender 3D embrace.
     * Arms wrap around each other, heads rest gently together.
     * Heart particles and golden stars swirl upward.
     * The Birthday Cake candles ignite with warm flickering flames.
     * Music bursts into the lyrical birthday waltz.
4. **Beat 3.4 — The Love Letter Scroll & Celebration**:
   - The ornate Storybook Parchment unrolls on screen with **Motivo #3**:
     *«Meu amor, Ana Gleice Almeida de Oliveira: o terceiro e maior motivo é a certeza de que a minha vida só faz sentido completo quando compartilhada com você. Quero ouvir todas as suas músicas, vibrar com cada conquista sua e segurar a sua mão em todos os aniversários que o tempo nos der. Feliz Aniversário, minha musicista favorita e meu grande amor! Eu te amo daqui até o infinito!»*
   - Interactive UI controls are enabled:
     * **"Tirar Foto do Abraço 📸"**: Captures the canvas and saves a clean memory snapshot PNG.
     * **"Reler Carta de Amor 💌"**: Allows Ana to re-open and re-read the love letter at any time.

---

## 5. Architectural Integration & Deterministic Test Hooks

To ensure seamless coordination with Explorer 1 (Visuals) and Explorer 3 (Audio/Testing), the character and level controller exposes deterministic game state hooks on `window.gameApp`:

```javascript
window.gameApp = {
    // Current state snapshot
    currentStage: 1,
    isCutsceneActive: false,
    isDaviRevealed: false,
    isHugging: false,
    
    // Programmatic level control for automated E2E testing
    loadLevel(stageNumber) {
        // Unloads current level meshes, constructs stage geometry, updates BGM
    },
    
    // Proximity triggers
    triggerDaviReveal() {
        // Executes transformation particle poof & swaps Toadbigode -> Davi
        this.isDaviRevealed = true;
    },
    
    triggerDaviHug() {
        // Executes 3D hug animation choreography and camera orbit
        this.isHugging = true;
    },
    
    // State inspection for automated assertions (Playwright / Jest)
    getCharacterPositions() {
        return {
            ana: playerGroup.position.clone(),
            toadbigode: toadbigodeGroup.position.clone(),
            davi: daviGroup ? daviGroup.position.clone() : null
        };
    }
};
```

---

## 6. Implementation Readiness & Asset Summary

| Asset / System | Construction Method | Dependencies | Performance Cost |
|---|---|---|---|
| **Ana Gleice 3D Chibi** | Procedural `THREE.Group` of primitives (`Sphere`, `Cylinder`, `Torus`, `Cone`) | Pure Three.js | ~1,200 polygons, instant load |
| **Toadbigode 3D** | Procedural mushroom cap, stem, signature mustache, vest, bow tie | Pure Three.js | ~950 polygons, instant load |
| **Davi Chibi 3D** | Procedural chibi with styled hair, dark glasses, mustache, knit sweater | Pure Three.js | ~1,350 polygons, instant load |
| **Animation Engine** | Mathematical trigonometric oscillation (`sin`/`cos` delta-time) | Zero external files | 0 KB asset size, 60 FPS |
| **Controller** | Keyboard (WASD/Arrows) + Mobile Virtual Touch Joystick | Vanilla JS DOM | Lightweight |
| **Level 1 (Bosque)** | Instanced low-poly trees, grass plane, harp shrine, fireflies | Pure Three.js | ~3,500 polygons total |
| **Level 2 (Planetário)**| 3 mini-planetoids, stardust rings, celestial bridge, launch star | Pure Three.js | ~4,200 polygons total |
| **Level 3 (Jardim)** | Moonlit terrace, cherry blossoms, 10-key light piano, 3-tier cake | Pure Three.js | ~5,100 polygons total |

This completes the technical specification for Characters, Animation, Level Design, and Narrative Beats.
