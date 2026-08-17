# ZeroContact 数据驱动包制作指南

本文档面向数据包作者，说明如何通过外部数据驱动包为 ZeroContact 添加装备、弹药、工作台配方、Lua 行为和客户端资源。规则依据默认包结构以及 `ZPackManager`、`ZContentLoader`、`ZAssetManager` 的当前实现整理。

弹药字段和 Lua API 的完整说明分别参见：

- [`弹药定义 JSON 用法表`](./ammo-definition-json-cn.md)
- [`ZeroContact Lua Helpers 用法`](./lua-helpers-cn.md)

## 安装位置与加载时机

将每个扩展包作为独立目录放入：

```text
config/zerocontact/packs/<包目录>/
```

管理器只扫描 `packs` 下的一级子目录，不会直接加载放在这里的 ZIP 文件。包内的物品与弹药会在物品注册阶段加载，因此新增或修改这些内容后应完整重启游戏；不要把 `/reload` 当作重新注册物品的方式。

启动时，模组还会把内置默认包解压到：

```text
config/zerocontact/packs/default_pack/
```

默认情况下，同名文件会被内置版本覆盖。可在 `config/zerocontact/override.toml` 中设置：

```toml
pack.default_pack_override = false
```

这只控制默认包的重新解压，不影响其他扩展包。

## 最小目录结构

```text
my_pack/
├─ manifest.json
├─ pack.mcmeta
├─ assets/
│  └─ zerocontact/
│     ├─ lang/
│     │  ├─ zh_cn.json
│     │  └─ en_us.json
│     ├─ models/item/
│     ├─ textures/item/
│     ├─ geo/
│     ├─ animations/
│     └─ textures/models/
└─ data/
   └─ zerocontact/
      ├─ items/
      ├─ ammoDefinitions/
      ├─ gear_recipes/
      └─ scripts/
```

`manifest.json` 和 `pack.mcmeta` 应放在包根目录。`items`、`ammoDefinitions` 和 `gear_recipes` 会递归查找以小写 `.json` 结尾的文件；`scripts` 会递归查找以小写 `.lua` 结尾的文件。`scripts` 可以直接省略；其他三个内容目录缺失时虽然不会阻止后续类型继续加载，但会产生读取失败日志，因此建议保留不使用的空目录。

目录名 `ammoDefinitions` 的大小写来自当前加载器常量。为兼容 Linux 等大小写敏感环境，请严格按示例书写。

## 包清单

### `manifest.json`

没有此文件的目录不会加入 ZeroContact 外部包集合。

```json
{
  "pack_name": "my_pack",
  "author": "Your Name",
  "version": "1.0.0"
}
```

| 字段 | 用途 |
| --- | --- |
| `pack_name` | 创造模式分页标识，同时用于物品包来源提示。建议使用唯一的小写 ID，例如 `my_pack`。 |
| `author` | 按住 Shift 查看物品包信息时显示的作者。 |
| `version` | 按住 Shift 查看物品包信息时显示的版本。 |

为分页和包提示添加本地化：

```json
{
  "itemGroup.zerocontact.my_pack": "我的扩展包",
  "tooltip.zerocontact.pack.my_pack": "我的扩展包"
}
```

多个包使用相同的 `pack_name` 时会共用分页标识，来源信息也可能产生歧义，因此应保持唯一。

### `pack.mcmeta`

外部包会同时注册成内置客户端资源包和服务端数据包，且默认启用、位于顶部。根目录需要有效的 Minecraft 包元数据：

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My ZeroContact data-driven pack"
  }
}
```

`pack_format` 必须与目标 Minecraft 版本匹配；示例中的 `15` 对应当前默认包，而不是所有版本的固定值。

## 命名空间与 ID 规则

当前自定义加载器固定从以下位置读取内容：

```text
data/zerocontact/items
data/zerocontact/ammoDefinitions
data/zerocontact/gear_recipes
data/zerocontact/scripts
```

生成的装备和弹药物品也注册到 `zerocontact` 命名空间。因此：

- 物品定义中的 `id: "example"` 对应 `zerocontact:example`。
- 弹药定义中的 `variant: "example_ammo"` 对应 `zerocontact:example_ammo`。
- 不同扩展包之间不要重复使用物品 `id`、弹药 `variant` 或 Lua 脚本 ID。
- ID 和路径建议只使用小写字母、数字、下划线、连字符、斜杠和点，并遵守 Minecraft `ResourceLocation` 规则。

## 装备定义：`data/zerocontact/items`

每个 JSON 定义一个物品。顶层 `type` 是必需的类型判别字段，当前只接受：

| `type` | 用途 | 还需使用的分类字段 |
| --- | --- | --- |
| `armor` | 护甲、头盔、面具、插板背心、制服或臂章 | `equipment_slot` |
| `plate` | 可装入插板背心的防弹插板 | 无 |
| `loadout` | 背包或胸挂容器 | `equipment_slot` |

未知的 `type` 无法反序列化。有效 JSON 并不代表一定能生成物品；不受适配器支持的 `equipment_slot` 会被忽略或导致加载失败。

### `armor` 字段

| 字段 | 类型 | 默认值 / 要求 | 说明 |
| --- | --- | --- | --- |
| `type` | `string` | 必须为 `armor` | 选择护甲数据模型。 |
| `id` | `string` | 必填 | 生成 `zerocontact:<id>`。 |
| `equipment_slot` | `string` | 必填 | 支持 `ARMOR`、`PLATE_CARRIER`、`HELMET`、`MASK`、`UNIFORM_TOP`、`UNIFORM_PANTS`、`ARMBAND`。 |
| `defense` | `integer` | `0` | 原版护甲防御值。 |
| `protection_class` | `integer` | `0` | ZeroContact 防护等级。 |
| `default_durability` | `integer` | `0` | 初始/最大耐久参数。 |
| `movement_fix` | `number` | `0` | 移动修正；默认包通常使用较小的负数表示减速。 |
| `durability_loss_modifier` | `number` | `1` | 受击时的耐久损耗倍率。 |
| `immune_effects` | `string[]` | `[]` | 免疫的状态效果资源 ID；无法解析的 ID 会被忽略。主要由头盔和面具适配器使用。 |
| `texture` | `string` | 空字符串 | `zerocontact` 命名空间下的 GeckoLib 纹理路径。 |
| `model` | `string` | 空字符串 | `zerocontact` 命名空间下的 GeckoLib 模型路径。 |
| `animation` | `string` | 空字符串 | `zerocontact` 命名空间下的 GeckoLib 动画路径。 |
| `hurt_modifier` | `object` | 默认倍率对象 | 不同命中结果最终应用的伤害倍率。 |

`hurt_modifier` 支持：

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `ricochet_multiplier` | `0.05` | 跳弹结果下应用的原伤害比例。 |
| `penetrate_multiplier` | `0.7` | 穿透结果下应用的原伤害比例。 |
| `blunt_multiplier` | `0.1` | 未穿透钝击结果下应用的原伤害比例。 |

示例：

```json
{
  "type": "armor",
  "id": "example_helmet",
  "equipment_slot": "HELMET",
  "defense": 2,
  "protection_class": 4,
  "default_durability": 20,
  "movement_fix": -0.01,
  "durability_loss_modifier": 1,
  "immune_effects": ["minecraft:blindness"],
  "texture": "textures/models/helmet/example.png",
  "model": "geo/helmet/example.geo.json",
  "animation": "animations/helmet/example.animation.json",
  "hurt_modifier": {
    "ricochet_multiplier": 0.05,
    "penetrate_multiplier": 0.7,
    "blunt_multiplier": 0.1
  }
}
```

不同护甲分类实际使用的字段子集不同：

| `equipment_slot` | 适配器实际使用的主要字段 |
| --- | --- |
| `ARMOR`、`PLATE_CARRIER` | `defense`、`protection_class`、`default_durability`、`movement_fix`、三个伤害倍率及模型资源。 |
| `HELMET`、`MASK` | `defense`、`protection_class`、`default_durability`、`durability_loss_modifier`、`immune_effects`、三个伤害倍率及模型资源。 |
| `UNIFORM_TOP`、`UNIFORM_PANTS`、`ARMBAND` | `id`、`default_durability` 及模型资源；其他战斗字段当前不会传给生成物品。 |

因此，不要假定 POJO 中出现的每个字段都会对所有 `equipment_slot` 生效。

### `plate` 字段

| 字段 | 类型 | 默认值 / 要求 | 说明 |
| --- | --- | --- | --- |
| `type` | `string` | 必须为 `plate` | 选择插板数据模型。 |
| `id` | `string` | 必填 | 生成 `zerocontact:<id>`。 |
| `durability` | `integer` | `0` | 插板耐久。 |
| `defense` | `integer` | `0` | 原版防御值。 |
| `protection_class` | `integer` | `0` | 防护等级。 |
| `movement_fix` | `number` | `0` | 移动修正。 |
| `durability_loss_modifier` | `number` | `1` | 耐久损耗倍率。 |
| `texture`、`model`、`animation` | `string` | 空字符串 | `zerocontact` 命名空间下的 GeckoLib 资源路径。 |
| `hurt_modifier` | `object` | 实际使用时应提供 | 字段名和默认倍率与 `armor` 相同。 |

注意：当前 POJO 只识别 `ricochet_multiplier`、`penetrate_multiplier` 和 `blunt_multiplier`。JSON 中写成 `*_modifier` 会被 Gson 当作未知字段忽略。

### `loadout` 字段

| 字段 | 类型 | 默认值 / 要求 | 说明 |
| --- | --- | --- | --- |
| `type` | `string` | 必须为 `loadout` | 选择容器装备数据模型。 |
| `id` | `string` | 必填 | 生成 `zerocontact:<id>`。 |
| `container_size` | `integer` | `0` | 容器槽位数量。 |
| `equipment_slot` | `string` | 必填 | 当前支持 `BACKPACK` 或 `RIGS`。 |
| `texture`、`model`、`animation` | `string` | 空字符串 | `zerocontact` 命名空间下的 GeckoLib 资源路径。 |

`HEADSET` 虽然存在于内部装备类型枚举中，但当前没有对应的数据生成适配器，不能仅凭 JSON 注册。

## 弹药定义：`data/zerocontact/ammoDefinitions`

目录中的每个 JSON 会同时：

1. 注册一项 `zerocontact:<variant>` 弹药物品；
2. 将弹道参数注册到口径变体注册表；
3. 把物品放入 `manifest.json` 中 `pack_name` 对应的创造模式分页。

字段、默认值、爆炸/点燃设置和事件钩子见 [`弹药定义 JSON 用法表`](./ammo-definition-json-cn.md)。若事件钩子调用 Lua，脚本 ID 必须与下节的路径映射一致。

客户端通常还需要提供物品模型、纹理和名称，例如 `variant` 为 `example_ammo` 时：

```text
assets/zerocontact/models/item/example_ammo.json
assets/zerocontact/textures/item/example_ammo.png
assets/zerocontact/lang/zh_cn.json → item.zerocontact.example_ammo
```

## Lua 脚本：`data/zerocontact/scripts`

脚本目录是可选的。脚本资源 ID 由相对路径去掉 `.lua` 后生成：

```text
data/zerocontact/scripts/incendiary/on_hit.lua
→ zerocontact:incendiary/on_hit
```

规则如下：

- 只扫描文件名以小写 `.lua` 结尾的普通文件。
- 包按规范化绝对路径排序，包内脚本按路径排序。
- 所有包共享 `zerocontact` 脚本命名空间。
- 脚本 ID 重复时会记录错误，排序靠后的重复项不会加载，先加载的脚本保留。
- 路径不能转换为有效 `ResourceLocation`、Lua 编译失败或读取失败时，会记录错误并跳过该脚本。

事件上下文、目标选择器和 Helper 函数见 [`ZeroContact Lua Helpers 用法`](./lua-helpers-cn.md)。

## 工作台配方：`data/zerocontact/gear_recipes`

每个文件包含一个 `recipes` 数组：

```json
{
  "recipes": [
    {
      "gear_id": "zerocontact:example_helmet",
      "ingredient_items": [
        {
          "itemId": "minecraft:iron_ingot",
          "count": 4
        },
        {
          "itemId": "minecraft:leather",
          "count": 2
        }
      ]
    }
  ]
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `recipes` | `array` | 当前文件包含的全部工作台配方。 |
| `gear_id` | `string` | 输出装备或弹药的完整资源 ID。 |
| `ingredient_items` | `array` | 所需材料列表。 |
| `itemId` | `string` | 材料的完整资源 ID。注意这里使用驼峰命名，不是 `item_id`。 |
| `count` | `integer` | 所需数量。 |

文件名会影响合并行为：

- 名为 `default.json` 的文件使用“仅在尚无该 `gear_id` 时写入”的默认规则。
- 其他文件会直接替换合并表中相同 `gear_id` 的材料列表。
- 包集合及普通 JSON 的遍历顺序没有稳定优先级保证。不要依赖多个非默认文件互相覆盖；每个 `gear_id` 最好只有一个明确的非默认定义。
- 最终配方按 `gear_id` 汇总，文件名和目录层级不会成为配方 ID。

## 客户端资源：`assets/zerocontact`

外部包会作为 Minecraft 客户端资源包加载，可以提供标准资源，例如：

| 路径 | 用途 |
| --- | --- |
| `lang/zh_cn.json`、`lang/en_us.json` | 物品名、创造分页名和包来源提示。 |
| `models/item/<id>.json` | 物品栏/手持模型。 |
| `textures/item/<id>.png` | 普通物品纹理。 |
| `geo/...` | GeckoLib 几何模型。 |
| `animations/...` | GeckoLib 动画。 |
| `textures/models/...` | GeckoLib 穿戴模型纹理。 |

普通物品模型示例：

```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "zerocontact:item/example_ammo"
  }
}
```

常用语言键：

```json
{
  "item.zerocontact.example_ammo": "示例弹药",
  "item.zerocontact.example_helmet": "示例头盔",
  "itemGroup.zerocontact.my_pack": "我的扩展包",
  "tooltip.zerocontact.pack.my_pack": "我的扩展包"
}
```

## 加载顺序与冲突处理

自定义内容类型按以下顺序读取：

1. `items`
2. `ammoDefinitions`
3. `scripts`
4. `gear_recipes`

随后，收集到的装备和弹药在物品注册阶段生成。外部包还会被注册为顶部、默认启用的客户端资源包和服务端数据包。

除 Lua 脚本外，多个包之间没有可依赖的稳定加载优先级。特别是物品注册 ID、弹药变体 ID和创造分页 ID，应通过唯一命名避免冲突，而不是尝试覆盖其他包。

## 发布前检查清单

- 包是 `config/zerocontact/packs` 下的一级目录，而不是 ZIP。
- 根目录同时存在有效的 `manifest.json` 和 `pack.mcmeta`。
- `pack_name`、物品 `id`、弹药 `variant` 和脚本相对路径均使用合法、唯一的小写 ID。
- 固定目录名与 `zerocontact` 命名空间拼写正确，尤其是 `ammoDefinitions` 的大小写。
- 所有 JSON 都是严格 JSON：没有注释、尾随逗号或重复键。
- `items` 中的 `type` 和 `equipment_slot` 是当前适配器支持的值。
- `hurt_modifier` 使用 `*_multiplier` 字段名。
- `gear_recipes` 中材料字段写作 `itemId`，并避免不确定的多文件覆盖。
- 每个生成物品都有对应的语言键以及所需模型、纹理或 GeckoLib 资源。
- 修改注册型内容后完整重启游戏，并检查日志中的 JSON 解析、资源 ID、Lua 编译和重复 ID 错误。
