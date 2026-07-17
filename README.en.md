# Life Debt

[简体中文](./README.md) ｜ **English**

> Spend tomorrow's life to cheat today's death — and when the debt comes due unpaid, burn out where you stand.

A Fabric mod. Swallow a Totem of Undying to take on **Life Debt**: while it lasts you shrug off otherwise-fatal blows, growing harder to kill the closer you brush with death — yet every reprieve you wring from the reaper is repaid, once the buff fades, in **permanent maximum health**. Run the debt too deep to settle, and you burn to ash on the spot.

## Core mechanics

- **Edible totem**: the Totem of Undying becomes food you can eat at any time (nutrition 6, saturation 1.0). Eating one grants **5 minutes of Life Debt**.
- **Cheat death**: while Life Debt is active, a fatal hit no longer kills you — it triggers a totem-equivalent revival instead: full heal, every negative effect cleansed, plus Regeneration II (45s), Absorption II (5s), Fire Resistance (40s), Night Vision (3min), and **Strength** raised two tiers above its current level (1min). Each death you resist adds one to your death count.
- **Harder to die (compounding damage reduction)**: the reduction ratio grows with each death resisted —
  `R = 1 − 1 / (1 + k · D / H_max)`
  where `D` is the number of deaths resisted during this buff, `H_max` is your current maximum health, and `k` is a configurable coefficient (default 20). Damage taken = original × (1 − R), together with an equal measure of **knockback resistance** (capped at 100%).
- **Repay with your life**: when the buff ends, your **maximum health is permanently reduced by your death count**.
- **Burn out**: if that reduction leaves your maximum health at zero or below, you die by the custom "burn out" damage type (death message: "%s burned out") and earn the advancement **"Consume me!"**.
- **Respawn clears the debt**: once you respawn, the maximum-health penalty is lifted.

Death count and session state persist through player NBT.

## Configuration

Config file: `config/lifedebt.yml` (generated on first run, with the formula documented in its comments).

| Key | Default | Description |
|---|---|---|
| `damageReductionK` | `20.0` | Damage-reduction coefficient `k`. Solve it from a target reduction: `k = H_max · R / (D · (1 − R))`. For example, to reach `R = 90%` at `H_max = 20, D = 10`, set `k = 18`. |

## Supported versions

A single source set, compiled into one jar per API-compatible era via [Stonecutter](https://stonecutter.kikugie.dev/). Each jar declares its actual supported Minecraft version range; together they cover every version from `1.16.5` through `1.21.11` without gaps:

`1.16.5` · `1.17.1` · `1.19.2` · `1.19.3` · `1.20.1` · `1.20.4` · `1.20.6` · `1.21.1` · `1.21.4` · `1.21.5` · `1.21.8` · `1.21.11`

> Minecraft 26.x carries heavier changes and is on hold for now, to be reassessed later.

Loader: Fabric Loader `>=0.19.3` (common across all versions). Requires Fabric API.

## License

[Apache License 2.0](./LICENSE)
