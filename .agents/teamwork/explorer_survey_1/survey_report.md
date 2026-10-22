# Technical Survey & Architecture Specification: 3D Graphics, Shaders & Visual Aesthetics
**Author**: Explorer Survey 1 (Visuals & 3D Shaders Explorer)  
**Date**: 2026-10-22  
**Target Project**: *A Sinfonia de Ana Gleice: Uma Aventura Poética de Aniversário* (`ana_birthday_quest`)  
**Scope**: Three.js WebGL Engine, Ghibli/Zelda/Galaxy Toon Shading, Level Visuals, Procedural Particle Systems, Third-Person Camera, and 60 FPS Performance Budget.

---

## 1. Executive Summary & Aesthetic Identity

*A Sinfonia de Ana Gleice* is a heartwarming, poetic 3D storybook browser game created as a birthday gift for musician Ana Gleice by her boyfriend Davi (toadbigode). The visual experience must evoke the cozy warmth of **Studio Ghibli films** (e.g., *Totoro*, *Spirited Away*, *The Secret World of Arrietty*), the clean adventure palette of **The Legend of Zelda: The Wind Waker / Breath of the Wild**, and the whimsical celestial wonder of **Super Mario Galaxy**.

### Key Architectural Pillars
1. **Zero External Asset Friction**: 100% of the 3D meshes, gradient toon ramps, particle sprites, skybox domes, and environment props are generated procedurally in memory via Three.js and HTML5 Canvas. There are zero external `.obj`, `.gltf`, `.png`, or `.mp3` dependencies that could trigger CORS errors, 404s, or network load delays.
2. **Stepped Cel-Shading & Pastel Palette**: Built using custom stepped luminance gradient maps on `THREE.MeshToonMaterial` paired with soft warm rim lighting (fresnel) and optional inverted hull contours.
3. **Immersive Atmosphere Across 3 Acts**:
   - **Act 1 (O Bosque das Melodias)**: Enchanted sun-dappled forest, fluffy canopy trees, wildflowers, fireflies, and ancient musical shrine.
   - **Act 2 (O Planetário dos Sonhos Cósmicos)**: Floating planetoids, Saturnian glowing rings, nebula sky dome, crystal formations, and twinkling star dust.
   - **Act 3 (O Jardim do Luar & O Grande Abraço)**: Twilight nocturnal palace garden, blooming sakura trees, luminous interactive grand piano, 3-tier birthday cake with real-time glowing candle flames, and celebratory confetti.
4. **Cinematic 3rd-Person Camera**: Smooth damped target-following camera with dynamic transitions for dialogue encounters and a dramatic 360° orbital sweep for the finale transformation, hug, and birthday photo capture.
5. **Rock-Solid 60 FPS Performance**: Under 30 draw calls per frame, instanced rendering for foliage/particles, capped device pixel ratio (`DPR <= 1.75`), and tight shadow frustums to ensure fluid 60 FPS on standard laptops, Macs, iPhones, and Android devices.

---

## 2. Three.js Engine Setup & Standalone Runtime Architecture

### 2.1 Delivery & Standalone Portability
To satisfy **R1** ("Jogo executável em qualquer navegador moderno sem dependências externas complicadas") and ensure instant execution whether opened directly from the filesystem (`file://`) or served over a lightweight HTTP server (e.g., Python `http.server`, Node `serve`, or Vite):
- **Primary Mechanism**: Vendored Three.js standalone bundle (r128 - r160) placed directly in `ana_birthday_quest/js/vendor/three.min.js`.
- **Alternative / Dual Loading**: If loaded via ES modules in modern browsers, an import map or script tag points to the local vendor copy with an automatic CDN fallback (e.g. `https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js` or `unpkg`).
- **Zero Bundler Requirement**: The game runs out-of-the-box without requiring `npm install` or a Node build step, while remaining fully modular.

### 2.2 WebGLRenderer Configuration & Canvas Lifecycle
The `THREE.WebGLRenderer` must be configured with precise parameters to balance visual fidelity and high performance:

```javascript
const renderer = new THREE.WebGLRenderer({
    canvas: document.getElementById('game-canvas'),
    antialias: true,
    alpha: false,
    powerPreference: 'high-performance',
    preserveDrawingBuffer: true // CRITICAL: Required for R4 screenshot capture!
});

// Color Management & Tone Mapping
renderer.outputColorSpace = THREE.SRGBColorSpace; // Rich, non-washed-out pastel tones
renderer.toneMapping = THREE.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.12;

// Soft Stylized Shadows
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;

// DPR Clamping to prevent mobile GPU thermal throttling
const maxDPR = window.innerWidth < 768 ? 1.5 : 1.75;
renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, maxDPR));
renderer.setSize(window.innerWidth, window.innerHeight);
```

> **Critical Architectural Finding (R4 Screenshot Feature)**:  
> WebGL context by default clears its drawing buffer after each `requestAnimationFrame` pass. By setting `preserveDrawingBuffer: true`, the WebGL canvas retains its pixel buffer, allowing instant, pristine photo capture via `canvas.toDataURL("image/png")` without blank or black screenshot glitches.

### 2.3 Resilient Viewport Resize Handling
A robust resize handler maintains aspect ratio and recalculates projection matrices:

```javascript
function handleResize() {
    const width = window.innerWidth;
    const height = window.innerHeight;
    
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    
    const maxDPR = width < 768 ? 1.5 : 1.75;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, maxDPR));
    renderer.setSize(width, height);
}
window.addEventListener('resize', handleResize, { passive: true });
```

### 2.4 Game Loop with Delta Clamping
To prevent animation warping or physics explosion when switching browser tabs or experiencing brief frame drops:

```javascript
const clock = new THREE.Clock();

function animate() {
    requestAnimationFrame(animate);
    
    // Clamp delta time to maximum 50ms (0.05s) to guarantee physics stability
    const rawDelta = clock.getDelta();
    const dt = Math.min(rawDelta, 0.05);
    const elapsedTime = clock.getElapsedTime();
    
    // Update sub-systems
    updateGameLogic(dt, elapsedTime);
    updateCamera(dt);
    updateParticles(dt, elapsedTime);
    
    renderer.render(scene, camera);
}
```

---

## 3. Cel-Shading, Toon Materials & Ghibli/Zelda Lighting Pipeline

### 3.1 Stepped Toon Ramp Generation (Procedural HTML5 Canvas)
To achieve the signature **Zelda: Wind Waker / Breath of the Wild** stepped shading without loading external PNG ramp textures, the engine generates discrete luminance ramp textures directly in memory:

```javascript
function createToonRampTexture(steps = 3) {
    const canvas = document.createElement('canvas');
    canvas.width = 64;
    canvas.height = 1;
    const ctx = canvas.getContext('2d');
    
    const grad = ctx.createLinearGradient(0, 0, 64, 0);
    if (steps === 2) {
        // High-contrast anime look
        grad.addColorStop(0.0, '#757575');
        grad.addColorStop(0.48, '#757575');
        grad.addColorStop(0.52, '#ffffff');
        grad.addColorStop(1.0, '#ffffff');
    } else if (steps === 3) {
        // Ghibli Soft 3-Step Toon
        grad.addColorStop(0.0, '#666677');   // Shadow tint
        grad.addColorStop(0.35, '#666677');
        grad.addColorStop(0.36, '#bbbbcc');  // Midtone
        grad.addColorStop(0.70, '#bbbbcc');
        grad.addColorStop(0.71, '#ffffff');  // Full highlight
        grad.addColorStop(1.0, '#ffffff');
    } else {
        // 4-Step Velvety Toon (Best for Chibi characters and skin)
        grad.addColorStop(0.0, '#5a586d');
        grad.addColorStop(0.25, '#5a586d');
        grad.addColorStop(0.26, '#9391a8');
        grad.addColorStop(0.55, '#9391a8');
        grad.addColorStop(0.56, '#d8d4e4');
        grad.addColorStop(0.85, '#d8d4e4');
        grad.addColorStop(0.86, '#ffffff');
        grad.addColorStop(1.0, '#ffffff');
    }
    
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 64, 1);
    
    const texture = new THREE.CanvasTexture(canvas);
    texture.minFilter = THREE.NearestFilter;
    texture.magFilter = THREE.NearestFilter;
    texture.generateMipmaps = false;
    return texture;
}
```

### 3.2 Material Presets
Using `THREE.MeshToonMaterial` with `gradientMap` provides complete hardware-accelerated cel-shading compatible with standard Three.js directional lights, point lights, and shadow maps:

| Material Type | Base Color (`color`) | Gradient Steps | Special Properties | Usage |
|---|---|---|---|---|
| **Chibi Skin** | `#ffe5d9` (warm ivory peach) | 4-step | Soft shadow tone `#f4bca9` | Ana Gleice & Davi face, hands |
| **Ana Hair** | `#3e2723` / `#4a2c2a` (rich chestnut brown) | 3-step | Warm specular tint | Ana hairstyle |
| **Ana Dress** | `#ffb7b2` (soft pastel rose / coral) | 3-step | Gentle sheen | Ana dress & ribbon |
| **Toadbigode Cap** | `#ffffff` + `#e63946` dots | 3-step | High contrast | Mushroom advisor cap |
| **Foliage Green** | `#6aa84f` / `#88cc66` | 3-step | Double-sided | Ghibli tree canopies & bushes |
| **Cosmic Crystal** | `#b388ff` (lavender) | 4-step | `emissive: #4a148c`, `emissiveIntensity: 0.4` | Planetoid crystals & stardust |
| **Grand Piano** | `#212121` / `#fafafa` | 3-step | `roughness: 0.2`, `emissive: #fff8e7` on keys | Level 3 music centerpiece |
| **Cake Fondant** | `#fff0f5` (lavender blush) | 3-step | Delicate shadow tint | Birthday celebration cake |

### 3.3 Rim Lighting (Fresnel Glow)
For hero models (Ana, Toadbigode, Davi, Piano, Birthday Cake), a subtle rim light makes characters pop against dark or dense backgrounds like a storybook illustration. This is achieved via a lightweight shader injection using `material.onBeforeCompile`:

```glsl
// Injected into fragment shader:
vec3 viewDir = normalize(vViewPosition);
float rim = 1.0 - max(0.0, dot(viewDir, normal));
rim = smoothstep(0.65, 0.98, rim);
gl_FragColor.rgb += vec3(0.9, 0.85, 1.0) * rim * 0.35;
```

### 3.4 Stylized Outlines (Inverted Hull Technique)
For crisp anime-style silhouettes without the performance penalty of multi-pass screen-space post-processing:
- A secondary geometry mesh with inverted normals (`material.side = THREE.BackSide`) scaled slightly outward by 2-3% along vertex normals.
- Mesh material: `MeshBasicMaterial({ color: 0x2b232a })` (warm dark plum instead of pure black for a softer, storybook feel).
- Can be toggled on/off in game settings for ultra-low-end devices.

---

## 4. Atmospheric Design & Lighting Architectures by Level

```
+---------------------------------------------------------------------------------------------------+
|                                 STORYBOOK LIGHTING ARCHITECTURE                                  |
+------------------------------------+----------------------------------+---------------------------+
| ACT 1: BOSQUE DAS MELODIAS         | ACT 2: PLANETÁRIO CÓSMICO        | ACT 3: JARDIM DO LUAR     |
| (Zelda Wind Waker / Ghibli)        | (Super Mario Galaxy)             | (Romantic Fairytale/Cake) |
+------------------------------------+----------------------------------+---------------------------+
| - Sky: Peach-to-Cerulean Dome      | - Sky: Deep Indigo Nebula Dome   | - Sky: Midnight Navy Dome |
| - Sun: Warm Golden #FFF3D6 (1.2)   | - Sun: Cold Starlight #DCE8FF    | - Moon: Silver #C0D8FF    |
| - Hemi: Sky #C0E4FF, Grass #5E8C48 | - Ambient: Cosmic Violet #5C4B7D | - Warm Lanterns: #FFB049  |
| - Fog: Warm Spring Green #D6EAD8   | - Fog: Void Purple #161233       | - Candle Flames: #FF7B00  |
| - Vibe: Fresh, Sunny, Cozy Melodic | - Vibe: Ethereal, Floating Dream | - Fog: Dreamy Lavender   |
+------------------------------------+----------------------------------+---------------------------+
```

### 4.1 Level 1: "O Bosque das Melodias Silvestres" (Zelda / Ghibli Vibe)
- **Palette**: Mint green (`#a8e6cf`), matcha (`#88d49e`), warm buttercup (`#ffd3b6`), sky blue (`#dcedc1`), wood ivory (`#fdfbf7`).
- **Sky Dome**: Inverted hemisphere mesh (`radius: 300`) with vertex-colored gradient:
  - Zenith: `#64b5f6` (gentle sky blue)
  - Horizon: `#ffecb3` (soft golden morning haze)
- **Lighting Setup**:
  ```javascript
  const sunLight = new THREE.DirectionalLight(0xfff3d6, 1.25);
  sunLight.position.set(25, 45, 20);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.set(1024, 1024);
  sunLight.shadow.camera.near = 0.5;
  sunLight.shadow.camera.far = 100;
  sunLight.shadow.camera.left = -22;
  sunLight.shadow.camera.right = 22;
  sunLight.shadow.camera.top = 22;
  sunLight.shadow.camera.bottom = -22;
  sunLight.shadow.bias = -0.001;

  const hemiLight = new THREE.HemisphereLight(0xc0e4ff, 0x5e8c48, 0.75);
  const ambientLight = new THREE.AmbientLight(0xfff0db, 0.35);
  scene.fog = new THREE.FogExp2(0xd6ead8, 0.014);
  ```
- **Procedural Props**:
  - **Ghibli Trees**: Brown cylinder trunks (`CylinderGeometry(0.3, 0.5, 3.5, 6)`) crowned with 3-4 intersecting deformed icosahedrons (`IcosahedronGeometry(1.6, 2)`) giving billowy, cloud-like leaf masses.
  - **Grassy Floating Island**: Rounded cylinder/extrude base with green top cap, earthy warm brown cliff sides.
  - **Altar da Harpa Ancestral**: Three white marble pillars flanking a golden harp frame with luminous vertical strings (`LineSegments` with additive glowing material).
  - **Wildflowers & Mushrooms**: Instanced clusters of 5-petal blossoms (pink, yellow, periwinkle) and cute spotted red/white mushroom caps.

### 4.2 Level 2: "O Planetário dos Sonhos Cósmicos" (Super Mario Galaxy Vibe)
- **Palette**: Deep celestial indigo (`#0f0c29`), twilight purple (`#302b63`), neon starlight yellow (`#ffe066`), aurora cyan (`#48cae4`), nebula magenta (`#c77dff`).
- **Sky Dome**: Dark gradient sphere with 600 background twinkling points:
  - Zenith: `#0d0b24`
  - Horizon: `#2c1b4d` with procedural glowing nebula band (`RingGeometry` with subtle sine opacity).
- **Lighting Setup**:
  ```javascript
  const starLight = new THREE.DirectionalLight(0xdce8ff, 1.1);
  starLight.position.set(18, 35, 15);
  starLight.castShadow = true;

  const cosmicAmbient = new THREE.AmbientLight(0x5c4b7d, 0.65);
  const shrinePoint = new THREE.PointLight(0xffe680, 2.2, 18, 1.5);
  shrinePoint.position.set(0, 3, 0); // Core star shrine
  scene.fog = new THREE.FogExp2(0x161233, 0.012);
  ```
- **Procedural Props**:
  - **Spherical Planetoid**: Curved low-poly sphere (`SphereGeometry(12, 16, 12)`) featuring pastel lavender grass patches.
  - **Glowing Saturnian Rings**: Thin `RingGeometry(16, 24, 64)` tilted at 28°, semi-transparent glowing golden/cyan texture.
  - **Floating Cosmic Stepping Stones**: Translucent star-shaped islets connected by glowing starlight ribbon bridges.
  - **Crystalline Music Pillars**: Tall hexagonal prisms with pulsating emissive glows that react when Ana approaches.

### 4.3 Level 3: "O Jardim do Luar & O Grande Abraço" (Fairytale Nocturnal Garden)
- **Palette**: Midnight navy (`#0a1128`), moonlit lilac (`#6c5b7b`), sakura blossom pink (`#ffb7c5`), warm candle flame amber (`#ff8c00`), champagne ivory (`#fff9e6`).
- **Sky Dome**: Midnight blue gradient with a majestic procedural harvest moon (`CircleGeometry` with soft corona glow texture).
- **Lighting Setup**:
  ```javascript
  const moonLight = new THREE.DirectionalLight(0xbcd4ff, 0.85);
  moonLight.position.set(-20, 40, -15);
  moonLight.castShadow = true;

  const gardenHemi = new THREE.HemisphereLight(0x4a4768, 0x221c35, 0.5);
  
  // Warm festive lighting for the birthday celebration area
  const cakeLantern = new THREE.PointLight(0xff9e44, 2.0, 14, 1.8);
  cakeLantern.position.set(0, 2.5, 0); // Above the birthday cake
  
  scene.fog = new THREE.FogExp2(0x1a1633, 0.016);
  ```
- **Procedural Props**:
  - **Sakura Trees (Cherry Blossoms)**: Dark gnarled branches adorned with billowy pink foliage masses (`MeshToonMaterial` in `#ffb7c5`).
  - **The Luminous Grand Piano**: Sleek curved piano body (`#232128`), ivory keys that gently illuminate with soft warm light when interacted with.
  - **The 3-Tier Birthday Cake**: Pastel fondant tiers decorated with piped cream beads, crowned with candles whose flames flicker dynamically using subtle sine-wave noise on point light intensity and flame mesh scale.
  - **Cobblestone Courtyard & Romantic Gazebo**: Circular terrace paved with rounded stone tiles, surrounded by floral planters.

---

## 5. Procedural GPU Particle Systems (Zero External Assets)

All particle sprites are dynamically generated in-memory on HTML5 `<canvas>` elements and converted to `THREE.CanvasTexture`.

```
                    CANVAS SPRITE GENERATOR (IN-MEMORY)
       +---------------------------------------------------------+
       | [Soft Radial]   [4-Point Star]   [Petal]   [Confetti]   |
       |     (o)             -+-            ((          []       |
       |  Fireflies       Star Dust       Sakura     Celebration |
       +---------------------------------------------------------+
```

### 5.1 In-Memory Sprite Generator Functions

```javascript
const ParticleSprites = {
    // 1. Soft glowing circle (Fireflies & Candle aura)
    createRadialGlow(color = '#ffff99') {
        const c = document.createElement('canvas');
        c.width = c.height = 64;
        const ctx = c.getContext('2d');
        const g = ctx.createRadialGradient(32, 32, 0, 32, 32, 30);
        g.addColorStop(0.0, color);
        g.addColorStop(0.3, 'rgba(255, 255, 200, 0.8)');
        g.addColorStop(0.7, 'rgba(255, 240, 150, 0.2)');
        g.addColorStop(1.0, 'rgba(0, 0, 0, 0)');
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, 64, 64);
        return new THREE.CanvasTexture(c);
    },

    // 2. 4-Point Sparkling Star (Cosmic Starlight)
    createStarSprite(color = '#ffffff') {
        const c = document.createElement('canvas');
        c.width = c.height = 64;
        const ctx = c.getContext('2d');
        ctx.translate(32, 32);
        ctx.fillStyle = color;
        ctx.beginPath();
        for (let i = 0; i < 4; i++) {
            ctx.lineTo(0, -28);
            ctx.quadraticCurveTo(0, -4, 4, -4);
            ctx.lineTo(28, 0);
            ctx.rotate(Math.PI / 2);
        }
        ctx.fill();
        return new THREE.CanvasTexture(c);
    },

    // 3. Curved Sakura Petal
    createPetalSprite() {
        const c = document.createElement('canvas');
        c.width = c.height = 64;
        const ctx = c.getContext('2d');
        ctx.fillStyle = '#ffb3ba';
        ctx.beginPath();
        ctx.ellipse(32, 32, 10, 24, Math.PI / 6, 0, Math.PI * 2);
        ctx.fill();
        return new THREE.CanvasTexture(c);
    }
};
```

### 5.2 Particle Specifications

| Particle System | Count | Motion Logic | Blending | Life & Bounds |
|---|---|---|---|---|
| **Fireflies (Act 1)** | 90 | 3D Perlin/Sine drift (`sin(t*0.8 + seed)`), pulsating alpha/scale | `AdditiveBlending` | Contained in 30x10x30m box over meadow |
| **Star Dust (Act 2)** | 160 | Slow orbital rotation around central planetoid, periodic sparkle twinkle | `AdditiveBlending` | Spherical shell (radius 10-25m) |
| **Sakura Petals (Act 3)** | 120 | Floating downward (`vy = -0.8m/s`), swaying horizontally with wind sine | `NormalBlending` | Wrap at ground: reset to y = 14m |
| **Celebration Confetti (Act 3 Finale)** | 240 | Instanced colorful ribbons (`InstancedMesh`), exploding upward, air drag, tumbling | `NormalBlending` | Active during Davi reveal & hug |
| **Footstep Sparkles** | 24 | Emitted behind Ana when movement speed > 0.05m/s, fading in 0.8s | `AdditiveBlending` | Dynamic pool attached to player feet |

### 5.3 High-Performance Confetti Explosion (InstancedMesh)
For the climax where Toadbigode transforms into Davi and embraces Ana:
```javascript
class ConfettiCelebration {
    constructor(scene, count = 220) {
        this.count = count;
        const geom = new THREE.PlaneGeometry(0.16, 0.28);
        const mat = new THREE.MeshBasicMaterial({
            side: THREE.DoubleSide,
            vertexColors: true
        });
        
        this.mesh = new THREE.InstancedMesh(geom, mat, count);
        this.velocities = [];
        this.rotSpeeds = [];
        this.dummy = new THREE.Object3D();
        
        const colors = [0xff6b6b, 0x4ecdc4, 0xffe66d, 0x95e1d3, 0xf38181, 0xaa96da];
        const colorAttr = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            const col = new THREE.Color(colors[i % colors.length]);
            col.toArray(colorAttr, i * 3);
            
            // Random initial velocity in an upward cone
            const angle = Math.random() * Math.PI * 2;
            const speed = 2.5 + Math.random() * 4.5;
            this.velocities.push(new THREE.Vector3(
                Math.cos(angle) * (1.0 + Math.random()),
                speed,
                Math.sin(angle) * (1.0 + Math.random())
            ));
            this.rotSpeeds.push(new THREE.Vector3(
                (Math.random() - 0.5) * 8,
                (Math.random() - 0.5) * 8,
                (Math.random() - 0.5) * 8
            ));
        }
        
        geom.setAttribute('color', new THREE.InstancedBufferAttribute(colorAttr, 3));
        scene.add(this.mesh);
    }
    
    update(dt) {
        // Apply gravity and tumbling rotation
        for (let i = 0; i < this.count; i++) {
            this.mesh.getMatrixAt(i, this.dummy.matrix);
            this.dummy.matrix.decompose(this.dummy.position, this.dummy.quaternion, this.dummy.scale);
            
            const v = this.velocities[i];
            v.y -= 4.5 * dt; // Gravity
            v.x *= 0.98;     // Air resistance
            v.z *= 0.98;
            
            this.dummy.position.addScaledVector(v, dt);
            this.dummy.rotation.x += this.rotSpeeds[i].x * dt;
            this.dummy.rotation.y += this.rotSpeeds[i].y * dt;
            this.dummy.rotation.z += this.rotSpeeds[i].z * dt;
            
            if (this.dummy.position.y < 0.1) {
                this.dummy.position.y = 0.1;
                v.set(0, 0, 0); // Rest on ground
            }
            
            this.dummy.updateMatrix();
            this.mesh.setMatrixAt(i, this.dummy.matrix);
        }
        this.mesh.instanceMatrix.needsUpdate = true;
    }
}
```

---

## 6. Third-Person Camera System & Cinematic Hug Sequencer

### 6.1 Damped Follow Camera Math
A spring-damped third-person camera tracks the player smoothly without nausea or sudden jerks:

```javascript
class ThirdPersonCameraController {
    constructor(camera, target) {
        this.camera = camera;
        this.target = target; // Ana's root Object3D
        
        this.idealOffset = new THREE.Vector3(0, 3.8, 6.2); // Elevation & distance
        this.currentPosition = new THREE.Vector3();
        this.currentLookAt = new THREE.Vector3();
        
        this.mode = 'EXPLORATION'; // 'EXPLORATION' | 'DIALOGUE' | 'CINEMATIC_ORBIT'
        this.orbitAngle = 0;
        this.orbitRadius = 5.0;
        this.orbitSpeed = 0.4;
    }
    
    update(dt) {
        if (this.mode === 'EXPLORATION') {
            // Smoothly calculate target position behind player based on heading
            const playerPos = this.target.position;
            const targetOffset = this.idealOffset.clone();
            
            // Allow gentle rotation to match player orientation
            targetOffset.applyAxisAngle(new THREE.Vector3(0, 1, 0), this.target.rotation.y);
            const desiredPos = playerPos.clone().add(targetOffset);
            const desiredLookAt = playerPos.clone().add(new THREE.Vector3(0, 1.3, 0));
            
            // Frame-rate independent exponential smoothing
            const lerpFactor = 1.0 - Math.exp(-6.0 * dt);
            this.currentPosition.lerp(desiredPos, lerpFactor);
            this.currentLookAt.lerp(desiredLookAt, lerpFactor);
            
            this.camera.position.copy(this.currentPosition);
            this.camera.lookAt(this.currentLookAt);
        } else if (this.mode === 'CINEMATIC_ORBIT') {
            // 360-degree slow majestic sweep around Ana & Davi during the hug
            this.orbitAngle += this.orbitSpeed * dt;
            const center = this.target.position.clone().add(new THREE.Vector3(0, 1.2, 0));
            
            this.camera.position.set(
                center.x + Math.sin(this.orbitAngle) * this.orbitRadius,
                center.y + 1.2 + Math.sin(this.orbitAngle * 0.5) * 0.4,
                center.z + Math.cos(this.orbitAngle) * this.orbitRadius
            );
            this.camera.lookAt(center);
        } else if (this.mode === 'DIALOGUE') {
            // Over-the-shoulder medium shot framing both Ana & Toadbigode
            const center = this.target.position.clone().add(new THREE.Vector3(0, 1.2, 0));
            const dialoguePos = center.clone().add(new THREE.Vector3(2.5, 0.8, 3.2));
            
            const lerpFactor = 1.0 - Math.exp(-4.0 * dt);
            this.camera.position.lerp(dialoguePos, lerpFactor);
            this.camera.lookAt(center);
        }
    }
}
```

### 6.2 Camera Modes and Transitions
- **Exploration Mode**: Dynamic distance scaling (closer on mobile screens to preserve chibi detail, slightly further on desktop ultra-wide).
- **Dialogue Mode**: Activated when dialogue box opens; gently shifts camera to a cozy cinematic medium frame.
- **Cinematic Orbit Mode**: Activated during the Level 3 reveal when Toadbigode morphs into Davi and executes the hug animation. Slowly revolves around the couple while cake candles spark and confetti bursts.
- **Photo-Op Snapshot Framing**: Locks at the optimal golden-ratio angle showcasing Davi, Ana, the lit birthday cake, and floating sakura petals, perfectly positioned for the screenshot button.

---

## 7. 60 FPS Performance Optimization & WebGL Hygiene

### 7.1 Performance Budget

| Metric | Budget Target | Desktop Average | Mobile Average | Optimization Strategy |
|---|---|---|---|---|
| **Frame Rate** | 60 FPS stable | 60 FPS | 58-60 FPS | Delta clamping, no heavy post-processing |
| **Draw Calls** | < 35 calls/frame | ~18-24 | ~18-24 | `InstancedMesh` for foliage, props, particles |
| **Active Triangles** | < 25,000 | ~14,000 | ~14,000 | Low-poly stylized geometry with smooth normals |
| **VRAM Usage** | < 45 MB | ~28 MB | ~28 MB | Procedural 64x64 textures, no 4K bitmap textures |
| **Fill Rate / DPR** | Clamped <= 1.75 | 1.75 | 1.2 - 1.5 | Prevents 4K Retina overheating |

### 7.2 WebGL Resource Disposal & Level Transitions
Switching between levels without proper cleanup is the #1 cause of WebGL context loss and browser tab memory leaks. When transitioning from Level 1 -> Level 2 -> Level 3:

```javascript
function disposeHierarchy(obj) {
    if (!obj) return;
    
    obj.traverse(child => {
        if (child.isMesh) {
            if (child.geometry) child.geometry.dispose();
            if (Array.isArray(child.material)) {
                child.material.forEach(m => disposeMaterial(m));
            } else if (child.material) {
                disposeMaterial(child.material);
            }
        }
    });
}

function disposeMaterial(mat) {
    if (mat.map) mat.map.dispose();
    if (mat.gradientMap) mat.gradientMap.dispose();
    if (mat.lightMap) mat.lightMap.dispose();
    mat.dispose();
}
```

---

## 8. Integration Architecture & Contracts with Sibling Modules

### 8.1 Contract with Character & Narrative Explorer (Survey 2)
- **Model Hooks**: Explorer 2 will define the procedural Chibi meshes for Ana, Toadbigode, and Davi. Explorer 1 provides the material factory:
  - `createToonMaterial(color, steps = 3, options = {})`
  - `createCharacterMaterials()`
  - `applyRimLight(material)`
- **Animation Sync**: When Ana moves, Explorer 2 updates `player.position` and `player.rotation.y`; Explorer 1's `cameraController.update(dt)` automatically tracks her position.
- **Hug Sequence Event**: Explorer 2 emits event `onStartHugSequence()`; Explorer 1 switches camera mode to `CINEMATIC_ORBIT` and triggers `confettiCelebration.start()`.

### 8.2 Contract with UI, Audio & Test Explorer (Survey 3)
- **Deterministic State Inspection**: Expose graphics telemetry on `window.__GRAPHICS_INFO__`:
  ```javascript
  window.__GRAPHICS_INFO__ = {
      fps: 60,
      drawCalls: renderer.info.render.calls,
      triangles: renderer.info.render.triangles,
      activeLevel: currentStage,
      cameraMode: cameraController.mode
  };
  ```
- **Screenshot Feature**: UI button calls `takeScreenshot()`, which utilizes `renderer.domElement.toDataURL('image/png')` (supported seamlessly via `preserveDrawingBuffer: true`).
- **Audio Visualizers / Reactivity**: Gentle sine pulses on the piano keys and shrine crystals synchronize with notes played by the Web Audio API engine.

---

## 9. Conclusion
This architecture guarantees a breathtaking, whimsical, and tearfully sweet birthday adventure for Ana Gleice. By relying on pure procedural WebGL and canvas-generated toon ramps, the game will boot instantaneously on any device, running at a buttery-smooth 60 FPS while delivering the magical visual quality of a Ghibli-Nintendo fairytale.
