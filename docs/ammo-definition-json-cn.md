# 弹药定义 JSON 用法表

本文档说明 [`AmmoDataPOJO`](../forge/src/main/java/net/zerocontact/datagen/AmmoDataPOJO.java) 对应的弹药定义 JSON。完整配置可参考 [`40mm_incendiary.json`](../common/src/main/resources/data/zerocontact/default_pack/data/zerocontact/ammoDefinitions/40mm_incendiary.json)。

弹药定义文件放在资源包的 `data/zerocontact/ammoDefinitions` 目录中。表内“默认值”来自 POJO 的字段初始化值；标为“必填”的字段虽然没有显式校验，但缺失时无法形成有效的弹药注册信息。

## 顶层字段

| JSON 字段 | 类型 | 必填 / 默认值 | 用途 | 示例与典型用例 |
| --- | --- | --- | --- | --- |
| `ammo_id` | `string` | 必填 | 要派生新弹种的 TaCZ 原始弹药资源 ID。 | `"tacz:40mm"`：为 TaCZ 40 mm 弹药增加变体。 |
| `variant` | `string` | 必填 | 新弹种名称，不带命名空间；运行时会注册为 `zerocontact:<variant>`。 | `"40mm_incendiary"` → `zerocontact:40mm_incendiary`。 |
| `life` | `integer` | `30` | 弹体生命周期，单位为 tick。 | `100`：最长存在 5 秒（按 20 tick/s 计）。 |
| `speed` | `number` | `1` | 弹速参数。源码已将其标记为可能废弃，非必要时保留默认值。 | `1`。 |
| `bullet_amount` | `integer` | `1` | 单次射击生成的弹丸数量；装填该自定义弹药时，会覆盖枪械脚本原有的弹丸数量。大于 `1` 主要用于霰弹等多弹丸弹种。 | `8`：每次射击生成 8 个弹丸；普通单弹丸弹药使用 `1`。 |
| `friction` | `number` | `0.015` | 空气阻力；影响弹体速度衰减。 | `0.05`：比默认值更快减速。 |
| `gravity` | `number` | `0.15` | 重力参数；影响弹道下坠。 | `0.15`。 |
| `knockback` | `number` | `0` | 弹体击退强度。 | `0`：不额外击退。 |
| `recoil_multiplier` | `number` | `1` | 枪械镜头后坐力倍率。 | `1.2`：后坐力提高 20%。 |
| `inaccuracy_multiplier` | `number` | `1` | 枪械散布倍率。 | `0.8`：散布缩小为原来的 80%。 |
| `base_damage_factor` | `number` | `1` | 同口径武器间的基础伤害平衡系数。 | `4`：40 mm 示例使用的系数。 |
| `penetration_class` | `integer` | `10` | 穿甲等级；参与护甲穿透与伤害计算。 | `0`：燃烧榴弹不强调穿甲。 |
| `flesh_damage` | `number` | `4` | 穿透护甲后或直接命中无甲目标时的肉体伤害。 | `2`。 |
| `armor_damage` | `number` | `0` | 护甲耐久消耗比例；`0` 表示走默认处理。 | `0.44`：按 44% 展示并参与耐久伤害计算。 |
| `stack_size` | `integer` | `30` | 生成弹药物品的最大堆叠数量。 | `2`：榴弹采用较小堆叠。 |
| `tracer_color` | `integer[]` | `[255, 255, 255, 255]` | 曳光颜色数组；前三项为 RGB，取值会按 `0..255` 使用，第四项沿用底层曳光数据。 | `[255, 0, 0]`：红色曳光。建议使用完整 RGBA，如 `[255, 0, 0, 255]`。 |
| `explosion` | `object` | 不提供时禁用 | 覆盖弹体爆炸参数；详见[爆炸字段](#explosion-爆炸字段)。 | 榴弹命中后产生范围爆炸。 |
| `ignite` | `object` | 不提供时禁用 | 覆盖弹体点燃参数；详见[点燃字段](#ignite-点燃字段)。 | 点燃命中的方块或实体。 |
| `effects` | `array` | `[]` | 按弹体事件执行状态效果或 Lua 脚本；详见[事件钩子](#effects-事件钩子)。 | 命中方块时给附近实体施加燃烧效果。 |

## `explosion` 爆炸字段

| JSON 字段 | 类型 | 对象内默认值 | 用途 | 示例 |
| --- | --- | --- | --- | --- |
| `radius` | `number` | `0` | 爆炸半径。 | `2` |
| `damage` | `number` | `0` | 爆炸伤害。 | `8` |
| `destroy_block` | `boolean` | `false` | 是否破坏方块。 | `false` |
| `knockback` | `boolean` | `false` | 爆炸是否产生击退。 | `true` |
| `delay_count` | `integer` | `0` | 传给 TaCZ 弹体的爆炸延迟计数。 | `30` |

若不需要爆炸，请省略整个 `explosion` 对象。只要提供了该对象，运行时就会把弹体标记为爆炸弹，即使其中的数值全为 `0`。

```json
"explosion": {
  "radius": 2,
  "damage": 8,
  "destroy_block": false,
  "knockback": true,
  "delay_count": 30
}
```

## `ignite` 点燃字段

| JSON 字段 | 类型 | 对象内默认值 | 用途 | 示例 |
| --- | --- | --- | --- | --- |
| `ignite_block` | `boolean` | `false` | 是否点燃命中的方块。 | `true` |
| `ignite_entity` | `boolean` | `false` | 是否点燃命中的实体。 | `true` |
| `ignite_entity_time` | `integer` | `0` | 传给 TaCZ 弹体的实体燃烧时长，单位沿用 TaCZ 的秒数参数。 | `5` |

```json
"ignite": {
  "ignite_block": true,
  "ignite_entity": true,
  "ignite_entity_time": 5
}
```

## `effects` 事件钩子

每个数组元素由一个 `trigger` 和可选的 `actions`、`scripts` 组成。同一事件中会先依次执行 `actions`，再依次调用 `scripts`。钩子只在服务端执行。

### 触发器取值

| `trigger` 值 | 触发时机 | 典型用例 |
| --- | --- | --- |
| `SPAWN` | 弹体生成并绑定弹药上下文时。 | 初始化效果或记录日志。 |
| `HIT_ENTITY` | 弹体对生物造成枪械伤害并完成 ZeroContact 伤害流程时。 | 给受击者施加负面效果。 |
| `HIT_BLOCK` | 弹体命中方块时。 | 单次触发爆炸、燃烧或脚本逻辑。 |
| `HIT_BLOCK_TICKING` | 当前实现中与 `HIT_BLOCK` 一同在命中方块时触发。 | 兼容现有烟雾弹等配置；不要假定它会在后续每个 tick 自动重复。 |
| `BULLET_TICKING` | 弹体飞行过程中每个服务端 tick、更新位置前触发。 | 生成飞行粒子或持续扫描弹道附近实体。 |

### `actions[]` 内置效果字段

| JSON 字段 | 类型 | 缺失时的值 | 用途 | 示例 |
| --- | --- | --- | --- | --- |
| `target` | `string` | `null`，实际应填写 | 目标选择器：`SHOOTER`、`VICTIM` 或 `NEARBY`。语义与 Lua Helper 使用的[目标选择器](./lua-helpers-cn.md#目标选择器)一致。 | `"NEARBY"` |
| `effect` | `string` | `null`，实际应填写 | 已注册的 Minecraft / 模组状态效果资源 ID；无法解析时跳过该 action。 | `"zerocontact:ignition"` |
| `duration` | `integer` | `0` | 状态效果持续 tick 数。 | `20`：1 秒。 |
| `amplifier` | `integer` | `0` | 状态效果等级从 0 开始；`0` 表示 I 级。 | `1`：II 级。 |
| `chance` | `number` | `0` | 每次触发的执行概率，通常使用 `0.0..1.0`。 | `1.0`：必定执行。 |
| `radius` | `number` | `0` | `NEARBY` 的水平扩张半径；其他选择器不依赖此值。 | `4.0` |

`NEARBY` 会沿弹体上一位置到当前位置的轨迹查找生物、排除射手，并在垂直方向额外扩张 2 格。`VICTIM` 只在存在受击实体的上下文中有目标，例如 `HIT_ENTITY`。

### `scripts[]` Lua 脚本字段

| JSON 字段 | 类型 | 缺失时的值 | 用途 | 示例 |
| --- | --- | --- | --- | --- |
| `script` | `string` | `null`，实际应填写 | `data/zerocontact/scripts` 下 Lua 文件的资源 ID，不含 `.lua`。 | `"zerocontact:40mm_incendiary_logic"` |
| `function` | `string` | `"run"` | Lua 文件返回 table 中要调用的函数名。 | `"on_hit_glowing"` |
| `arguments` | `object` | `{}` | 原样转换后作为回调第二个参数 `args` 传入，可包含字符串、数字、布尔值、数组或嵌套对象。 | `{ "duration": 50, "radius": 4.0 }` |

Lua 文件必须返回 table，并暴露与 `function` 同名的函数。可用的 Helper、回调上下文 `ctx`、目标选择器及完整脚本写法见 [`ZeroContact Lua Helpers 用法`](./lua-helpers-cn.md)。

## 完整用例：40 mm 燃烧弹

以下配置根据仓库中的示例整理，演示基础弹道、爆炸、内置状态效果与 Lua 回调的组合：

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

## 配置注意事项

- 资源 ID 使用小写命名空间格式，例如 `tacz:40mm`、`minecraft:poison`。
- `actions` 或 `scripts` 可省略；反序列化后会按空列表处理。
- `chance` 缺失时为 `0`，action 将不会执行；需要必定触发时显式填写 `1.0`。
- `target` 或 `effect` 缺失可能导致 action 无法执行；把它们视为 action 的必填字段。
- `tracer_color` 至少提供 RGB 三项；颜色展示逻辑会把前三项限制在 `0..255`。
- JSON 中的 `function` 必须与 Lua 文件返回 table 中的函数名完全一致；省略时调用 `run`。
