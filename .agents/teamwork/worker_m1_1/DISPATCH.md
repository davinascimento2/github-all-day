# Dispatch: Worker Milestone 1 (Core 3D Engine, Models, Audio & UI Shell)

## Objective
Implement the complete core foundations of *A Sinfonia de Ana Gleice* in `ana_birthday_quest/`:
1. Standalone Three.js setup (`index.html`, `js/vendor/three.min.js`, `main.js`) with WebGL canvas, 60 FPS render loop, `preserveDrawingBuffer: true`, DPR capping, and resize handling.
2. Cel-shading aesthetic: stepped toon materials (`MeshToonMaterial`), procedural luminance ramp texture, inverted hull contours, soft lighting, and particle systems.
3. Procedural 3D Chibi Models (`js/models.js`):
   - Ana Gleice: brunette wavy hair, musician hairpin, lavender dress, cute expressive face, articulated limbs.
   - Toadbigode: royal mushroom cap with spots, dapper vest, cute animated mustache.
   - Davi Chibi: transformed model for Level 3 with stylish glasses, cozy knit sweater, articulated hug rig.
4. Character Movement & Camera Controller (`js/controls.js`):
   - Keyboard WASD/Arrow keys + mobile virtual touch joystick.
   - Smooth 3rd-person follow camera, dialogue framing, and cinematic hug orbit mode.
5. Procedural Web Audio API Engine (`js/audio.js`):
   - 100% procedural synthesis (zero external audio files).
   - Additive acoustic piano, nylon guitar arpeggios, lofi pad with warm LFO tape flutter, Animal Crossing style pentatonic voice blips, footsteps, sparkles, fanfares, birthday hymn, and mute toggle.
6. Storybook UI System (`css/style.css`, `index.html`, `js/dialogue.js`):
   - Storybook parchment frames, Fredoka/Quicksand typography, typewriter dialogue controller with instant skip, squishy bouncy buttons, HUD controls, and audio unlock overlay.
7. Test hooks exposed on `window.__GAME_STATE__` and `window.gameApp` per `PROJECT.md § Interface Contracts`.

## Files to Read Before Starting
1. `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\ORIGINAL_REQUEST.md` (Authoritative requirements)
2. `c:\Users\Davi\Documents\antigravity\zealous-hypatia\PROJECT.md` (Architecture, specifications, contracts)
3. Explorer Reports:
   - `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_1\survey_report.md`
   - `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_2\survey_report.md`
   - `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_3\survey_report.md`

## File Boundaries & Ownership
You exclusively own:
- `ana_birthday_quest/index.html`
- `ana_birthday_quest/css/style.css`
- `ana_birthday_quest/js/` (all files: `main.js`, `models.js`, `controls.js`, `audio.js`, `dialogue.js`, `capture.js`, and `vendor/three.min.js`)
Do NOT touch `ana_birthday_quest/tests/`.

## Mandatory Integrity Warning
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

## Deliverables
- Fully working, verified implementation.
- Run build/syntax checks and verify in a browser or test script.
- Write handoff report to `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\worker_m1_1\handoff.md`.

## 2026-10-22T02:12:57Z
[Message] timestamp=2026-10-22T02:12:57Z sender=83219276-5dc0-45d5-b200-77979799e8aa priority=MESSAGE_PRIORITY_HIGH content=You are Worker 1 for Milestone 1 (Core 3D Engine, Models, Audio & UI Shell).
Working directory: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\worker_m1_1
Read your dispatch: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\worker_m1_1\DISPATCH.md
Read authoritative requirements: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\ORIGINAL_REQUEST.md
Read project index: c:\Users\Davi\Documents\antigravity\zealous-hypatia\PROJECT.md
Read explorer survey reports:
- c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_1\survey_report.md
- c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_2\survey_report.md
- c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\explorer_survey_3\survey_report.md

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Implement the standalone HTML5/Three.js setup, Cel-shading & Ghibli/Zelda/Galaxy aesthetic, procedural 3D Chibi models (Ana Gleice, Toadbigode, Davi), character movement & camera controller (WASD/touch joystick, follow camera, orbit), procedural Web Audio API engine, and storybook UI system with dialogue typewriter and test hooks.
Verify your changes by running syntax/execution checks.
Write your handoff report to `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\worker_m1_1\handoff.md`.
Send a completion message back when done.
