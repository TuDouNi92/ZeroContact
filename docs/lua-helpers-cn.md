# ZeroContact Lua Helpers 用法

Lua 脚本执行时，全局表 `zc` 会提供当前已注册的 helper。调用形式为：

```lua
zc.<命名空间>.<函数名>(参数...)
```

## Helper 用法表

| 命名空间 | 函数 | 参数 | 返回值 | 用途与注意事项 | 示例 |
| --- | --- | --- | --- | --- | --- |
| `zc.log` | `info(message)` | `message: string` | 无 | 输出带脚本 ID 的 INFO 日志；消息最长 1000 个字符，超出部分会被截断。 | `zc.log.info("Triggered")` |
| `zc.effect` | `apply(target, effect, duration, amplifier?, radius?)` | `target: string`；`effect: string`；`duration: integer`；`amplifier: integer = 0`；`radius: number = 0` | 无 | 为目标施加状态效果。`duration` 单位为 tick，限制为 `1..72000`；`amplifier` 限制为 `0..255`；`radius` 限制为 `0..32`。 | `zc.effect.apply("NEARBY", "minecraft:poison", 100, 1, 4)` |
| `zc.entity` | `position(target)` | `target: string` | `{x, y, z}` 或空表 | 返回第一个匹配实体的位置。这里是数组形式，使用 `pos[1]`、`pos[2]`、`pos[3]` 读取。没有目标时返回空表。 | `local pos = zc.entity.position("VICTIM")` |
| `zc.entity` | `type(target, entity_type)` | `target: string`；`entity_type: string` | `boolean` | 判断第一个匹配实体是否为指定实体类型；目标不存在或实体类型未注册时返回 `false`。 | `zc.entity.type("VICTIM", "minecraft:zombie")` |
| `zc.entity` | `health(target)` | `target: string` | `number` 或 `nil` | 返回第一个匹配实体的当前生命值。 | `local hp = zc.entity.health("VICTIM")` |
| `zc.entity` | `max_health(target)` | `target: string` | `number` 或 `nil` | 返回第一个匹配实体的最大生命值。 | `local max_hp = zc.entity.max_health("VICTIM")` |
| `zc.entity` | `distance_between()` | 无 | `number` 或 `nil` | 返回射手与受击者之间的**距离平方**；任一实体不存在时返回 `nil`。 | `local distance_sq = zc.entity.distance_between()` |
| `zc.entity` | `is_on_fire(target)` | `target: string` | `boolean` | 判断第一个匹配实体是否正在着火；目标不存在时返回 `false`。 | `zc.entity.is_on_fire("VICTIM")` |
| `zc.entity` | `count(target, radius)` | `target: string`；`radius: number` | `integer` | 统计匹配实体数量。 | 预期用法：`zc.entity.count("NEARBY", 4)` |
| `zc.entity` | `has_effect(target, effect)` | `target: string`；`effect: string` | `boolean` | 判断第一个匹配实体是否拥有指定状态效果；目标或效果不存在时返回 `false`。 | `zc.entity.has_effect("VICTIM", "minecraft:poison")` |
| `zc.entity` | `remove_effect(target, effect)` | `target: string`；`effect: string` | `boolean` | 移除第一个匹配实体的指定状态效果；返回是否成功移除。 | `zc.entity.remove_effect("VICTIM", "minecraft:poison")` |
| `zc.entity` | `extinguish(target)` | `target: string` | 无 | 熄灭第一个匹配实体。 | `zc.entity.extinguish("VICTIM")` |
| `zc.entity` | `heal(target, amount?, radius?)` | `target: string`；`amount: integer = 1`；`radius: number = 1` | 无 | 治疗所有匹配实体。`radius` 主要对 `NEARBY` 有意义。 | `zc.entity.heal("NEARBY", 4, 3)` |
| `zc.entity` | `ignite(target, seconds?, radius?)` | `target: string`；`seconds: integer = 1`；`radius: number = 1` | 无 | 点燃所有匹配实体，持续时间单位为秒。`radius` 主要对 `NEARBY` 有意义。 | `zc.entity.ignite("VICTIM", 5)` |
| `zc.particle` | `spawn_simple(particle, x, y, z, count, x_offset, y_offset, z_offset, speed)` | `particle: string`；坐标、偏移和速度为 `number`；`count: integer` | `boolean` | 在服务端发送简单粒子。粒子 ID 未注册时返回 `false`；已注册时返回 `true`。仅 `SimpleParticleType` 会实际生成粒子。 | `zc.particle.spawn_simple("minecraft:flame", 0, 64, 0, 10, 0.2, 0.2, 0.2, 0.01)` |

## 目标选择器

目标名称不区分大小写；无效名称会抛出 Lua 错误。

| 值 | 含义 |
| --- | --- |
| `SHOOTER` | 当前钩子上下文中的射手。 |
| `VICTIM` | 当前钩子上下文中的受击实体。 |
| `NEARBY` | 弹道当前位置与上一位置附近的生物实体，不包含射手；范围由 helper 的 `radius` 参数控制。 |

对于只读取第一个实体且没有半径参数的函数（例如 `position`、`health`），`NEARBY` 使用半径 `0`。

## 脚本示例

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

脚本文件必须返回一个 table；JSON 中配置的 `function` 必须与该 table 中的函数名一致。

## 回调上下文 `ctx`

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `ctx.script` | `string` | 当前脚本资源 ID。 |
| `ctx.function` | `string` | 当前调用的函数名。 |
| `ctx.trigger` | `string` | 触发器名称。 |
| `ctx.position` | `{ x: number, y: number, z: number }` | 当前弹体或钩子位置。 |
| `ctx.previous_position` | `{ x: number, y: number, z: number }` 或 `nil` | 上一位置。 |
| `ctx.shooter` | `string` 或 `nil` | 射手 UUID。 |
| `ctx.victim` | `string` 或 `nil` | 受击实体 UUID。 |

JSON `arguments` 会作为回调的第二个参数传入；缺少的字段在 Lua 中为 `nil`，可用 `args.value or 默认值` 提供默认值。
