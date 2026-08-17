# Ammo Definition JSON Usage Table

This document describes the ammo definition JSON represented by [`AmmoDataPOJO`](../forge/src/main/java/net/zerocontact/datagen/AmmoDataPOJO.java). For a complete configuration, see [`40mm_incendiary.json`](../common/src/main/resources/data/zerocontact/default_pack/data/zerocontact/ammoDefinitions/40mm_incendiary.json).

Place ammo definition files in the resource pack's `data/zerocontact/ammoDefinitions` directory. Defaults in the tables come from the POJO field initializers. Fields marked as required have no explicit validation, but omitting them prevents the definition from producing valid ammo registration data.

## Top-level fields

| JSON field | Type | Required / default | Purpose | Example and use case |
| --- | --- | --- | --- | --- |
| `ammo_id` | `string` | Required | Resource ID of the original TaCZ ammo from which the new variant is derived. | `"tacz:40mm"`: adds a variant of TaCZ 40 mm ammo. |
| `variant` | `string` | Required | Name of the new ammo variant without a namespace. It is registered at runtime as `zerocontact:<variant>`. | `"40mm_incendiary"` becomes `zerocontact:40mm_incendiary`. |
| `life` | `integer` | `30` | Projectile lifetime in ticks. | `100`: allows the projectile to exist for up to 5 seconds at 20 ticks per second. |
| `speed` | `number` | `1` | Projectile speed parameter. The source marks this field as possibly deprecated, so keep the default unless an override is needed. | `1` |
| `bullet_amount` | `integer` | `1` | Number of projectiles spawned per shot. While this custom ammo is loaded, it overrides the gun script's original projectile count. Values greater than `1` are mainly intended for shotgun-style multi-projectile ammo. | `8`: spawns 8 projectiles per shot; use `1` for ordinary single-projectile ammo. |
| `friction` | `number` | `0.015` | Air friction; controls how quickly projectile velocity decreases. | `0.05`: slows the projectile more quickly than the default. |
| `gravity` | `number` | `0.15` | Gravity parameter; affects projectile drop. | `0.15` |
| `knockback` | `number` | `0` | Projectile knockback strength. | `0`: applies no additional knockback. |
| `recoil_multiplier` | `number` | `1` | Camera recoil multiplier for the weapon. | `1.2`: increases recoil by 20%. |
| `inaccuracy_multiplier` | `number` | `1` | Weapon spread multiplier. | `0.8`: reduces spread to 80% of its original value. |
| `base_damage_factor` | `number` | `1` | Base damage balancing factor among weapons that use the same caliber. | `4`: the factor used by the 40 mm example. |
| `penetration_class` | `integer` | `10` | Armor penetration class; participates in penetration and damage calculations. | `0`: suitable for an incendiary grenade that does not emphasize armor penetration. |
| `flesh_damage` | `number` | `4` | Damage dealt after penetrating armor or when directly hitting an unarmored target. | `2` |
| `armor_damage` | `number` | `0` | Armor durability damage ratio. `0` uses the default armor-damage process. | `0.44`: displayed as 44% and used in durability-damage calculations. |
| `stack_size` | `integer` | `30` | Maximum stack size of the generated ammo item. | `2`: a small stack for grenades. |
| `tracer_color` | `integer[]` | `[255, 255, 255, 255]` | Tracer color array. The first three entries are RGB values consumed in the `0..255` range; the fourth entry is passed through to the underlying tracer data. | `[255, 0, 0]`: red tracer. A complete RGBA value such as `[255, 0, 0, 255]` is recommended. |
| `explosion` | `object` | Disabled when omitted | Overrides projectile explosion parameters. See [Explosion fields](#explosion-fields). | Creates an area explosion when a grenade hits. |
| `ignite` | `object` | Disabled when omitted | Overrides projectile ignition parameters. See [Ignite fields](#ignite-fields). | Ignites the hit block or entity. |
| `effects` | `array` | `[]` | Runs status-effect actions or Lua scripts in response to projectile events. See [Event hooks](#effects-event-hooks). | Applies an ignition effect to nearby entities when the projectile hits a block. |

## `explosion` fields

| JSON field | Type | Default inside object | Purpose | Example |
| --- | --- | --- | --- | --- |
| `radius` | `number` | `0` | Explosion radius. | `2` |
| `damage` | `number` | `0` | Explosion damage. | `8` |
| `destroy_block` | `boolean` | `false` | Whether the explosion destroys blocks. | `false` |
| `knockback` | `boolean` | `false` | Whether the explosion produces knockback. | `true` |
| `delay_count` | `integer` | `0` | Explosion delay count passed to the TaCZ projectile. | `30` |

Omit the entire `explosion` object when the projectile should not explode. Providing the object marks the projectile as explosive at runtime even when all of its values are `0` or `false`.

```json
"explosion": {
  "radius": 2,
  "damage": 8,
  "destroy_block": false,
  "knockback": true,
  "delay_count": 30
}
```

## `ignite` fields

| JSON field | Type | Default inside object | Purpose | Example |
| --- | --- | --- | --- | --- |
| `ignite_block` | `boolean` | `false` | Whether the projectile ignites the block it hits. | `true` |
| `ignite_entity` | `boolean` | `false` | Whether the projectile ignites the entity it hits. | `true` |
| `ignite_entity_time` | `integer` | `0` | Entity burn duration passed to the TaCZ projectile, using TaCZ's seconds parameter. | `5` |

```json
"ignite": {
  "ignite_block": true,
  "ignite_entity": true,
  "ignite_entity_time": 5
}
```

## `effects` event hooks

Each array entry consists of a `trigger` and optional `actions` and `scripts`. For a single event, all `actions` run in order before any `scripts` are invoked. Hooks run on the server only.

### Trigger values

| `trigger` value | When it fires | Typical use case |
| --- | --- | --- |
| `SPAWN` | When a projectile is spawned and its ammo context is bound. | Initialize an effect or write a log entry. |
| `HIT_ENTITY` | After the projectile deals gun damage to a living entity and completes the ZeroContact damage pipeline. | Apply a negative effect to the victim. |
| `HIT_BLOCK` | When the projectile hits a block. | Run one-shot explosion, ignition, or script logic. |
| `HIT_BLOCK_TICKING` | In the current implementation, fires together with `HIT_BLOCK` when a block is hit. | Supports existing smoke-ammo configurations. Do not assume that it repeats automatically on subsequent ticks. |
| `BULLET_TICKING` | On each server tick while the projectile is flying, before its position is updated. | Spawn in-flight particles or continuously scan for entities along the trajectory. |

### Built-in `actions[]` fields

| JSON field | Type | Value when omitted | Purpose | Example |
| --- | --- | --- | --- | --- |
| `target` | `string` | `null`; required in practice | Target selector: `SHOOTER`, `VICTIM`, or `NEARBY`. The values have the same semantics as the Lua Helper [target selectors](./lua-helpers-en.md#target-selectors). | `"NEARBY"` |
| `effect` | `string` | `null`; required in practice | Resource ID of a registered Minecraft or mod status effect. The action is skipped if the effect cannot be resolved. | `"zerocontact:ignition"` |
| `duration` | `integer` | `0` | Status-effect duration in ticks. | `20`: one second. |
| `amplifier` | `integer` | `0` | Zero-based status-effect amplifier; `0` means level I. | `1`: level II. |
| `chance` | `number` | `0` | Execution probability for each trigger, normally in the `0.0..1.0` range. | `1.0`: always executes. |
| `radius` | `number` | `0` | Horizontal expansion radius used by `NEARBY`; the other selectors do not depend on it. | `4.0` |

`NEARBY` searches for living entities along the segment between the projectile's previous and current positions, excludes the shooter, and expands the search vertically by 2 blocks. `VICTIM` only produces a target in a context that contains a hit entity, such as `HIT_ENTITY`.

### Lua `scripts[]` fields

| JSON field | Type | Value when omitted | Purpose | Example |
| --- | --- | --- | --- | --- |
| `script` | `string` | `null`; required in practice | Resource ID of a Lua file under `data/zerocontact/scripts`, without the `.lua` extension. | `"zerocontact:40mm_incendiary_logic"` |
| `function` | `string` | `"run"` | Name of the function to call in the table returned by the Lua file. | `"on_hit_glowing"` |
| `arguments` | `object` | `{}` | Converted and passed unchanged as the callback's second `args` parameter. It may contain strings, numbers, booleans, arrays, or nested objects. | `{ "duration": 50, "radius": 4.0 }` |

The Lua file must return a table and expose a function whose name exactly matches `function`. See [`ZeroContact Lua Helpers Usage`](./lua-helpers-en.md) for the available helpers, callback context `ctx`, target selectors, and a complete script example.

## Complete example: 40 mm incendiary ammo

The following configuration is based on the repository example and demonstrates the combination of basic ballistics, an explosion, a built-in status effect, and a Lua callback:

```json
{
  "ammo_id": "tacz:40mm",
  "variant": "40mm_incendiary",
  "base_damage_factor": 4,
  "penetration_class": 0,
  "armor_damage": 0,
  "flesh_damage": 2,
  "stack_size": 2,
  "life": 100,
  "recoil_multiplier": 1,
  "friction": 0.05,
  "gravity": 0.15,
  "knockback": 0,
  "explosion": {
    "radius": 2,
    "delay_count": 30
  },
  "effects": [
    {
      "trigger": "HIT_BLOCK",
      "actions": [
        {
          "target": "NEARBY",
          "effect": "zerocontact:ignition",
          "duration": 20,
          "amplifier": 0,
          "chance": 1.0,
          "radius": 4.0
        }
      ],
      "scripts": [
        {
          "script": "zerocontact:40mm_incendiary_logic",
          "function": "on_hit_glowing",
          "arguments": {
            "duration": 50,
            "amplifier": 0,
            "radius": 4.0
          }
        }
      ]
    }
  ]
}
```

## Configuration notes

- Use lowercase namespaced resource IDs such as `tacz:40mm` and `minecraft:poison`.
- `actions` and `scripts` may be omitted; they are converted to empty lists during deserialization.
- Omitting `chance` gives it a value of `0`, so the action will not execute. Set it explicitly to `1.0` for guaranteed execution.
- Missing `target` or `effect` values may prevent an action from executing. Treat both fields as required for an action.
- Provide at least the three RGB entries in `tracer_color`. The display logic clamps the first three values to `0..255`.
- The JSON `function` must exactly match a function in the table returned by the Lua file. When omitted, the engine calls `run`.
