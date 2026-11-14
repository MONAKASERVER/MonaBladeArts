# Implementation status — 1.0.0-alpha.1

Implemented:

- Server-authoritative item identity and persistent blade data
- Four-step ground chain, dash cut, draw cut, aerial chain and plunge
- Guard, just guard and projectile reflection
- Summoned sword and delayed multi-slash finisher
- Combat-rank gain and decay from D through SSS
- Soul gauge, kill counter and refinement-aware damage
- PvP disabled by default; NPC/shopkeeper exclusion
- Per-action target and particle budgets
- Five original blade models and six forging materials
- Slimefun nested categories and recipes
- Optional validated client input channel

Known limitation:

- A Paper server can trigger vanilla arm swings, movement, particles, sounds and model changes, but cannot install arbitrary skeletal full-body animation clips into an unmodified client. The server version reproduces timing and gameplay behavior. A future optional client animation module can consume the validated input/result protocol without becoming authoritative.
