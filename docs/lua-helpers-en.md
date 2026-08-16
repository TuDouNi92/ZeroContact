# ZeroContact Lua Helpers Usage

When a Lua script runs, the global `zc` table provides all currently registered helpers. Use the following call format:

```lua
zc.<namespace>.<function>(arguments...)
```

## Helper Usage Table

| Namespace | Function | Parameters | Return value | Usage and notes | Example |
| --- | --- | --- | --- | --- | --- |
| `zc.log` | `info(message)` | `message: string` | None | Writes an INFO log entry containing the script ID. Messages are limited to 1,000 characters; longer messages are truncated. | `zc.log.info("Triggered")` |
| `zc.effect` | `apply(target, effect, duration, amplifier?, radius?)` | `target: string`; `effect: string`; `duration: integer`; `amplifier: integer = 0`; `radius: number = 0` | None | Applies a status effect to the target. `duration` is measured in ticks and clamped to `1..72000`; `amplifier` is clamped to `0..255`; `radius` is clamped to `0..32`. | `zc.effect.apply("NEARBY", "minecraft:poison", 100, 1, 4)` |
| `zc.entity` | `position(target)` | `target: string` | `{x, y, z}` or an empty table | Returns the position of the first matching entity. This is an array-style table; read its values with `pos[1]`, `pos[2]`, and `pos[3]`. Returns an empty table when no target exists. | `local pos = zc.entity.position("VICTIM")` |
| `zc.entity` | `type(target, entity_type)` | `target: string`; `entity_type: string` | `boolean` | Checks whether the first matching entity has the specified entity type. Returns `false` when the target does not exist or the entity type is not registered. | `zc.entity.type("VICTIM", "minecraft:zombie")` |
| `zc.entity` | `health(target)` | `target: string` | `number` or `nil` | Returns the current health of the first matching entity. | `local hp = zc.entity.health("VICTIM")` |
| `zc.entity` | `max_health(target)` | `target: string` | `number` or `nil` | Returns the maximum health of the first matching entity. | `local max_hp = zc.entity.max_health("VICTIM")` |
| `zc.entity` | `distance_between()` | None | `number` or `nil` | Returns the **squared distance** between the shooter and victim. Returns `nil` if either entity is unavailable. | `local distance_sq = zc.entity.distance_between()` |
| `zc.entity` | `is_on_fire(target)` | `target: string` | `boolean` | Checks whether the first matching entity is on fire. Returns `false` when no target exists. | `zc.entity.is_on_fire("VICTIM")` |
| `zc.entity` | `count(target, radius)` | `target: string`; `radius: number` | `integer` | Counts the matching entities. | Expected usage: `zc.entity.count("NEARBY", 4)` |
| `zc.entity` | `has_effect(target, effect)` | `target: string`; `effect: string` | `boolean` | Checks whether the first matching entity has the specified status effect. Returns `false` when the target or effect does not exist. | `zc.entity.has_effect("VICTIM", "minecraft:poison")` |
| `zc.entity` | `remove_effect(target, effect)` | `target: string`; `effect: string` | `boolean` | Removes the specified status effect from the first matching entity. Returns whether the effect was successfully removed. | `zc.entity.remove_effect("VICTIM", "minecraft:poison")` |
| `zc.entity` | `extinguish(target)` | `target: string` | None | Extinguishes the first matching entity. | `zc.entity.extinguish("VICTIM")` |
| `zc.entity` | `heal(target, amount?, radius?)` | `target: string`; `amount: integer = 1`; `radius: number = 1` | None | Heals all matching entities. `radius` is primarily relevant to `NEARBY`. | `zc.entity.heal("NEARBY", 4, 3)` |
| `zc.entity` | `ignite(target, seconds?, radius?)` | `target: string`; `seconds: integer = 1`; `radius: number = 1` | None | Sets all matching entities on fire. The duration is measured in seconds. `radius` is primarily relevant to `NEARBY`. | `zc.entity.ignite("VICTIM", 5)` |
| `zc.particle` | `spawn_simple(particle, x, y, z, count, x_offset, y_offset, z_offset, speed)` | `particle: string`; coordinates, offsets, and speed are `number`; `count: integer` | `boolean` | Sends simple particles from the server. Returns `false` when the particle ID is not registered and `true` when it is registered. Only a `SimpleParticleType` is actually spawned. | `zc.particle.spawn_simple("minecraft:flame", 0, 64, 0, 10, 0.2, 0.2, 0.2, 0.01)` |

## Target Selectors

Target names are case-insensitive. An invalid name raises a Lua error.

| Value | Meaning |
| --- | --- |
| `SHOOTER` | The shooter in the current hook context. |
| `VICTIM` | The hit entity in the current hook context. |
| `NEARBY` | Living entities near the projectile's current and previous positions, excluding the shooter. The helper's `radius` parameter controls the range. |

For functions that only read the first entity and do not accept a radius, such as `position` and `health`, `NEARBY` uses a radius of `0`.

## Script Example

```lua
local handlers = {}

function handlers.on_hit(ctx, args)
    zc.log.info("on_hit triggered by " .. ctx.script)

    if zc.entity.health("VICTIM") ~= nil then
        zc.entity.ignite("VICTIM", args.fire_seconds or 3)
        zc.effect.apply(
            "VICTIM",
            "minecraft:slowness",
            args.duration or 60,
            args.amplifier or 0
        )
    end

    zc.particle.spawn_simple(
        "minecraft:flame",
        ctx.position.x,
        ctx.position.y,
        ctx.position.z,
        12,
        0.2,
        0.2,
        0.2,
        0.01
    )
end

return handlers
```

The script file must return a table. The `function` configured in JSON must match a function name in that table.

## Callback Context: `ctx`

| Field | Type | Meaning |
| --- | --- | --- |
| `ctx.script` | `string` | The current script resource ID. |
| `ctx.function` | `string` | The name of the function being called. |
| `ctx.trigger` | `string` | The trigger name. |
| `ctx.position` | `{ x: number, y: number, z: number }` | The current projectile or hook position. |
| `ctx.previous_position` | `{ x: number, y: number, z: number }` or `nil` | The previous position. |
| `ctx.shooter` | `string` or `nil` | The shooter's UUID. |
| `ctx.victim` | `string` or `nil` | The victim entity's UUID. |

JSON `arguments` are passed as the callback's second parameter. Missing fields are `nil` in Lua; use `args.value or default_value` to provide defaults.
