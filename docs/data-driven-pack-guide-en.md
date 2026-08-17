# ZeroContact Data-Driven Pack Authoring Guide

This guide is intended for pack authors who want to add equipment, ammunition, workbench recipes, Lua behavior, and client resources to ZeroContact through external data-driven packs. The rules described here are based on the current default-pack layout and the implementations of `ZPackManager`, `ZContentLoader`, and `ZAssetManager`.

For complete ammunition-field and Lua API references, see:

- [`Ammo Definition JSON Usage Table`](./ammo-definition-json-en.md)
- [`ZeroContact Lua Helpers`](./lua-helpers-en.md)

## Installation location and loading time

Place each extension pack in its own directory under:

```text
config/zerocontact/packs/<pack-directory>/
```

The manager only scans first-level directories under `packs`; it does not directly load ZIP files placed there. Items and ammunition are loaded during item registration, so fully restart the game after adding or changing this content. Do not rely on `/reload` to register items again.

At startup, the mod also extracts its built-in default pack to:

```text
config/zerocontact/packs/default_pack/
```

By default, files with matching names are overwritten by the built-in versions. To disable this behavior, set the following value in `config/zerocontact/override.toml`:

```toml
pack.default_pack_override = false
```

This setting only controls extraction of the default pack; it does not affect other extension packs.

## Minimum directory structure

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

Place `manifest.json` and `pack.mcmeta` in the pack root. The `items`, `ammoDefinitions`, and `gear_recipes` directories are searched recursively for files ending in lowercase `.json`; `scripts` is searched recursively for files ending in lowercase `.lua`. The `scripts` directory may be omitted. Missing any of the other three content directories does not stop later content types from loading, but it does produce a read-failure log entry, so keeping unused directories empty is recommended.

The capitalization of `ammoDefinitions` comes from the current loader constant. Use the exact spelling shown above for compatibility with case-sensitive systems such as Linux.

## Pack metadata

### `manifest.json`

A directory without this file is not added to ZeroContact's external-pack collection.

```json
{
  "pack_name": "my_pack",
  "author": "Your Name",
  "version": "1.0.0"
}
```

| Field | Purpose |
| --- | --- |
| `pack_name` | Creative-tab identifier and item pack-source identifier. Use a unique lowercase ID such as `my_pack`. |
| `author` | Author shown in the item pack details while Shift is held. |
| `version` | Version shown in the item pack details while Shift is held. |

Add translations for the creative tab and pack-source tooltip:

```json
{
  "itemGroup.zerocontact.my_pack": "My Extension Pack",
  "tooltip.zerocontact.pack.my_pack": "My Extension Pack"
}
```

Packs that use the same `pack_name` share a creative-tab identifier, and their source details may become ambiguous. Keep this value unique.

### `pack.mcmeta`

An external pack is registered as both a built-in client resource pack and a server data pack. It is enabled by default and placed at the top. The pack root therefore needs valid Minecraft pack metadata:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My ZeroContact data-driven pack"
  }
}
```

`pack_format` must match the target Minecraft version. The value `15` is used by the current default pack; it is not a fixed value for every version.

## Namespace and ID rules

The custom loader currently reads content from these fixed locations:

```text
data/zerocontact/items
data/zerocontact/ammoDefinitions
data/zerocontact/gear_recipes
data/zerocontact/scripts
```

Generated equipment and ammunition items are also registered in the `zerocontact` namespace. Consequently:

- An item definition containing `id: "example"` creates `zerocontact:example`.
- An ammunition definition containing `variant: "example_ammo"` creates `zerocontact:example_ammo`.
- Do not reuse item `id`, ammunition `variant`, or Lua script IDs across extension packs.
- IDs and paths should use only lowercase letters, digits, underscores, hyphens, slashes, and periods, while following Minecraft `ResourceLocation` rules.

## Equipment definitions: `data/zerocontact/items`

Each JSON file defines one item. The top-level `type` field is a required type discriminator. The current loader accepts only:

| `type` | Purpose | Additional classification field |
| --- | --- | --- |
| `armor` | Armor, helmets, masks, plate carriers, uniforms, or armbands | `equipment_slot` |
| `plate` | Armor plate inserted into a plate carrier | None |
| `loadout` | Backpack or chest-rig container | `equipment_slot` |

An unknown `type` cannot be deserialized. Valid JSON also does not guarantee that an item will be generated: an unsupported `equipment_slot` may be ignored or cause loading to fail.

### `armor` fields

| Field | Type | Default / requirement | Description |
| --- | --- | --- | --- |
| `type` | `string` | Must be `armor` | Selects the armor data model. |
| `id` | `string` | Required | Generates `zerocontact:<id>`. |
| `equipment_slot` | `string` | Required | Supports `ARMOR`, `PLATE_CARRIER`, `HELMET`, `MASK`, `UNIFORM_TOP`, `UNIFORM_PANTS`, and `ARMBAND`. |
| `defense` | `integer` | `0` | Vanilla armor defense value. |
| `protection_class` | `integer` | `0` | ZeroContact protection class. |
| `default_durability` | `integer` | `0` | Initial/maximum durability parameter. |
| `movement_fix` | `number` | `0` | Movement modifier. The default pack generally uses small negative values for movement penalties. |
| `durability_loss_modifier` | `number` | `1` | Durability-loss multiplier when hit. |
| `immune_effects` | `string[]` | `[]` | Mob-effect resource IDs to ignore. Unresolvable IDs are discarded. Primarily used by the helmet and mask adapters. |
| `texture` | `string` | Empty string | GeckoLib texture path in the `zerocontact` namespace. |
| `model` | `string` | Empty string | GeckoLib model path in the `zerocontact` namespace. |
| `animation` | `string` | Empty string | GeckoLib animation path in the `zerocontact` namespace. |
| `hurt_modifier` | `object` | Default multiplier object | Damage multipliers applied to different hit outcomes. |

`hurt_modifier` supports:

| Field | Default | Description |
| --- | --- | --- |
| `ricochet_multiplier` | `0.05` | Proportion of the original damage applied after a ricochet result. |
| `penetrate_multiplier` | `0.7` | Proportion of the original damage applied after a penetration result. |
| `blunt_multiplier` | `0.1` | Proportion of the original damage applied as blunt damage when penetration fails. |

Example:

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

Different armor categories consume different subsets of the available fields:

| `equipment_slot` | Main fields actually used by its adapter |
| --- | --- |
| `ARMOR`, `PLATE_CARRIER` | `defense`, `protection_class`, `default_durability`, `movement_fix`, all three damage multipliers, and model resources. |
| `HELMET`, `MASK` | `defense`, `protection_class`, `default_durability`, `durability_loss_modifier`, `immune_effects`, all three damage multipliers, and model resources. |
| `UNIFORM_TOP`, `UNIFORM_PANTS`, `ARMBAND` | `id`, `default_durability`, and model resources. Other combat fields are not currently passed to the generated item. |

Do not assume that every field present in the POJO affects every `equipment_slot`.

### `plate` fields

| Field | Type | Default / requirement | Description |
| --- | --- | --- | --- |
| `type` | `string` | Must be `plate` | Selects the armor-plate data model. |
| `id` | `string` | Required | Generates `zerocontact:<id>`. |
| `durability` | `integer` | `0` | Plate durability. |
| `defense` | `integer` | `0` | Vanilla defense value. |
| `protection_class` | `integer` | `0` | Protection class. |
| `movement_fix` | `number` | `0` | Movement modifier. |
| `durability_loss_modifier` | `number` | `1` | Durability-loss multiplier. |
| `texture`, `model`, `animation` | `string` | Empty string | GeckoLib resource paths in the `zerocontact` namespace. |
| `hurt_modifier` | `object` | Should be provided in practice | Uses the same field names and default multipliers as `armor`. |

Important: the current POJO recognizes only `ricochet_multiplier`, `penetrate_multiplier`, and `blunt_multiplier`. Names ending in `*_modifier` are treated as unknown fields by Gson and ignored.

### `loadout` fields

| Field | Type | Default / requirement | Description |
| --- | --- | --- | --- |
| `type` | `string` | Must be `loadout` | Selects the container-equipment data model. |
| `id` | `string` | Required | Generates `zerocontact:<id>`. |
| `container_size` | `integer` | `0` | Number of container slots. |
| `equipment_slot` | `string` | Required | Currently supports `BACKPACK` or `RIGS`. |
| `texture`, `model`, `animation` | `string` | Empty string | GeckoLib resource paths in the `zerocontact` namespace. |

Although `HEADSET` exists in the internal equipment-type enum, it currently has no data-generation adapter and cannot be registered through JSON alone.

## Ammunition definitions: `data/zerocontact/ammoDefinitions`

Each JSON file in this directory performs all of the following:

1. Registers a `zerocontact:<variant>` ammunition item.
2. Registers its ballistic parameters in the caliber-variant registry.
3. Places the item in the creative tab identified by `pack_name` in `manifest.json`.

See the [`Ammo Definition JSON Usage Table`](./ammo-definition-json-en.md) for fields, defaults, explosion and ignition settings, and event hooks. When an event hook calls Lua, its script ID must match the path mapping described in the next section.

Client resources should normally include an item model, texture, and name. For example, a `variant` named `example_ammo` uses:

```text
assets/zerocontact/models/item/example_ammo.json
assets/zerocontact/textures/item/example_ammo.png
assets/zerocontact/lang/en_us.json → item.zerocontact.example_ammo
```

## Lua scripts: `data/zerocontact/scripts`

The script directory is optional. A script resource ID is derived by removing `.lua` from its relative path:

```text
data/zerocontact/scripts/incendiary/on_hit.lua
→ zerocontact:incendiary/on_hit
```

The following rules apply:

- Only regular files whose names end in lowercase `.lua` are scanned.
- Packs are sorted by normalized absolute path, and scripts inside each pack are sorted by path.
- Every pack shares the `zerocontact` script namespace.
- If a script ID is duplicated, an error is logged, the later duplicate is skipped, and the first loaded script remains active.
- A script is skipped with an error when its path cannot form a valid `ResourceLocation`, its Lua source cannot be compiled, or the file cannot be read.

See [`ZeroContact Lua Helpers`](./lua-helpers-en.md) for event context, target selectors, and helper functions.

## Workbench recipes: `data/zerocontact/gear_recipes`

Each file contains a `recipes` array:

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

| Field | Type | Description |
| --- | --- | --- |
| `recipes` | `array` | All workbench recipes in the current file. |
| `gear_id` | `string` | Full resource ID of the output equipment or ammunition. |
| `ingredient_items` | `array` | Required ingredient list. |
| `itemId` | `string` | Full resource ID of an ingredient. Note the camelCase spelling; this is not `item_id`. |
| `count` | `integer` | Required quantity. |

The filename affects merge behavior:

- A file named `default.json` adds a recipe only when its `gear_id` has not already been added.
- Other files directly replace the ingredient list for the same `gear_id` in the merged map.
- Pack-set iteration and ordinary JSON-file traversal do not provide a stable priority guarantee. Do not rely on several non-default files overriding one another; preferably give each `gear_id` one unambiguous non-default definition.
- Final recipes are grouped by `gear_id`; filenames and directory levels do not become recipe IDs.

## Client resources: `assets/zerocontact`

External packs are loaded as Minecraft client resource packs and may provide standard resources such as:

| Path | Purpose |
| --- | --- |
| `lang/zh_cn.json`, `lang/en_us.json` | Item names, creative-tab names, and pack-source tooltips. |
| `models/item/<id>.json` | Inventory and held-item models. |
| `textures/item/<id>.png` | Ordinary item textures. |
| `geo/...` | GeckoLib geometry models. |
| `animations/...` | GeckoLib animations. |
| `textures/models/...` | Textures for GeckoLib wearable models. |

Example ordinary item model:

```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "zerocontact:item/example_ammo"
  }
}
```

Common translation keys:

```json
{
  "item.zerocontact.example_ammo": "Example Ammunition",
  "item.zerocontact.example_helmet": "Example Helmet",
  "itemGroup.zerocontact.my_pack": "My Extension Pack",
  "tooltip.zerocontact.pack.my_pack": "My Extension Pack"
}
```

## Loading order and conflict handling

Custom content types are read in this order:

1. `items`
2. `ammoDefinitions`
3. `scripts`
4. `gear_recipes`

Collected equipment and ammunition are then generated during item registration. External packs are also registered as top-priority, enabled-by-default client resource packs and server data packs.

Except for Lua scripts, there is no stable cross-pack loading priority on which authors should rely. In particular, item registry IDs, ammunition variant IDs, and creative-tab IDs should be made unique instead of being used to override another pack.

## Pre-release checklist

- The pack is a first-level directory under `config/zerocontact/packs`, not a ZIP file.
- Valid `manifest.json` and `pack.mcmeta` files both exist in the pack root.
- `pack_name`, item `id`, ammunition `variant`, and script-relative paths use valid, unique lowercase IDs.
- Fixed directory names and the `zerocontact` namespace are spelled correctly, including the capitalization of `ammoDefinitions`.
- Every JSON file is strict JSON, with no comments, trailing commas, or duplicate keys.
- Values of `type` and `equipment_slot` in `items` are supported by a current adapter.
- `hurt_modifier` uses field names ending in `*_multiplier`.
- Material entries in `gear_recipes` use the spelling `itemId`, and recipes avoid ambiguous multi-file overrides.
- Every generated item has the required translation key and model, texture, or GeckoLib resources.
- Fully restart the game after changing registry-backed content, then check the log for JSON parsing, resource ID, Lua compilation, and duplicate-ID errors.
