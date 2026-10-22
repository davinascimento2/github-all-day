# Dispatch: E2E Test Writer (Test Track)

## Objective
Author the requirement-driven E2E test suite (Tiers 1-4) for *A Sinfonia de Ana Gleice* following `TEST_INFRA.md` and `ORIGINAL_REQUEST.md`.

## Working Directory
`c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\test_writer_1`

## Files to Read
1. `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\ORIGINAL_REQUEST.md` (Authoritative requirements)
2. `c:\Users\Davi\Documents\antigravity\zealous-hypatia\TEST_INFRA.md` (Test architecture & thresholds)
3. `c:\Users\Davi\Documents\antigravity\zealous-hypatia\PROJECT.md` (Project specifications & contracts)

## File Boundaries & Ownership
You exclusively own:
- `c:\Users\Davi\Documents\antigravity\zealous-hypatia\ana_birthday_quest\tests/`
- Test dependencies and configuration (e.g., `package.json` test script / Playwright config).
Do NOT modify game implementation code in `js/` or `css/`.

## Deliverables
1. Automated Playwright test suite in `ana_birthday_quest/tests/e2e.spec.js` covering:
   - Tier 1: Feature coverage (Canvas, Three.js, Chibi models, Controls, Audio engine, Storybook UI, Levels 1-3, Reveal, Hug, Cake candles, Love letter, Photo capture)
   - Tier 2: Boundary & Corner cases (window resize, rapid input, mute toggle, dialogue skip, extreme canvas dimensions)
   - Tier 3: Cross-feature combinations (stage progression + audio transitions + HUD updates + screenshot)
   - Tier 4: Real-world application journeys (complete playthrough, mobile touch navigation, letter re-read session)
2. Verify tests execute cleanly via `npx playwright test` (or Node test runner) once dependencies are set up.
3. Publish `c:\Users\Davi\Documents\antigravity\zealous-hypatia\TEST_READY.md` when the test suite is ready, following the template in `PROJECT.md` / Project Pattern.
4. Write handoff report to `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\test_writer_1\handoff.md`.

## 2026-10-22T02:12:57Z
You are the E2E Test Writer for the E2E Testing Track.
Working directory: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\test_writer_1
Read your dispatch: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\test_writer_1\DISPATCH.md
Read authoritative requirements: c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\ORIGINAL_REQUEST.md
Read test infrastructure spec: c:\Users\Davi\Documents\antigravity\zealous-hypatia\TEST_INFRA.md
Read project index: c:\Users\Davi\Documents\antigravity\zealous-hypatia\PROJECT.md

Your role is to author a comprehensive, requirement-driven, opaque-box E2E test suite (Tiers 1-4) in `c:\Users\Davi\Documents\antigravity\zealous-hypatia\ana_birthday_quest\tests\e2e.spec.js`.
Ensure test dependencies (e.g. Playwright / package.json test scripts) are configured so the test suite can be run headlessly against a local static server.
When the test suite is authored, publish `c:\Users\Davi\Documents\antigravity\zealous-hypatia\TEST_READY.md` containing the runner command and coverage summary.
Write your handoff report to `c:\Users\Davi\Documents\antigravity\zealous-hypatia\.agents\teamwork\test_writer_1\handoff.md`.
Send a completion message back when done.
