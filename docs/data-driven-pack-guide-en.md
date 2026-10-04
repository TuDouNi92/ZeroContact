# ZeroContact Data-Driven Pack Authoring Guide

This guide is intended for pack authors who want to add equipment, ammunition, workbench recipes, Lua behavior, and client resources to ZeroContact through external data-driven packs. The rules described here are based on the current default-pack layout and the implementations of `ZPackManager`, `ZContentLoader`, and `ZAssetManager`.

For complete ammunition-field and Lua API references, see:

- [`Ammo Definition JSON Usage Table`](./ammo-definition-json-en.md)
- [`ZeroContact Lua Helpers`](./lua-helpers-en.md)
- [`Configuration, Commands, and Equipment Operations`](./configuration-and-equipment-en.md)

This guide has been checked against commits and implementation through `09966b2` (2026-10-04). Damage extension events are documented in the ammunition reference.

## Installation location and loading time

Place each extension pack in its own directory under:

```text
config/zerocontact/packs/<pack-directory>/
```

The manager only scans first-level directories under `packs`; it does not directly load ZIP files placed there. Items and ammunition are loaded during item registration, so fully restart the game after adding or changing this content. Do not rely on `/reload` to register items again.

At startup, the mod also extracts each pack in the built-in ZIP into a separate directory under `packs`:

```text
config/zerocontact/packs/default_ammo/
config/zerocontact/packs/default_armor/
config/zerocontact/packs/default_loadout/
config/zerocontact/packs/default_module/
config/zerocontact/packs/content_creator_smat/
```

By default, files with matching names are overwritten by the built-in versions. To disable this behavior, set the following value in `config/zerocontact/override.toml`:

```toml
pack.default_pack_override = false
```

This setting only controls extraction of the default pack; it does not affect other extension packs.

A legacy pack whose manifest has `pack_name: "zero_contact"` is skipped with a `Detected deprecated pack` warning. Migrate it to the current structure and a unique pack name; renaming its directory alone does not remove this restriction. Extraction overwrites matching files but does not remove old files no longer present in the built-in ZIP. Check for leftover definitions after upgrades to avoid duplicate registration.

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
| `module` | Generic module, such as a battery, beacon, pouch, or radio | `mount_type`, `module_trait` |
| `module_nvg` | Night-vision or thermal module | `mount_type`, `module_trait` |
| `module_headset` | Headset module with an audio profile | `mount_type`, `module_trait` |

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
| `attachments` | `object[]` | `[]` | Module mounts for `ARMOR`, `PLATE_CARRIER`, `HELMET`, and `MASK`; see below. |

`hurt_modifier` supports:

| Field | Default | Description |
| --- | --- | --- |
| `ricochet_multiplier` | `0.05` | Still deserialized and passed to the item; the current `DamageProcessor` only marks `RICOCHET` and does not apply this additional multiplier. |
| `penetrate_multiplier` | `0.7` | Multiplier for positive penetration damage, normally applied to ammo `flesh_damage`. |
| `blunt_multiplier` | `0.1` | Non-penetration blunt multiplier; the current base calculation is `penetration_class * 0.3 * blunt_multiplier`, rather than a fixed proportion of original damage. |

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

### Armor module mounts: `attachments[]`

| Field | Type | Description |
| --- | --- | --- |
| `mount_id` | `string` | Mount path unique within this equipment; automatically uses the `zerocontact` namespace. Commands also use this path. |
| `mount_type` | `string` | Mount position type: `HELMET_FRONT`, `HELMET_RAIL`, `HELMET_BACK`, `HELMET_TOP`, `FACE`, `ARMOR_POUCH`, or `UNDEFINED`. |
| `mount_bone` | `string` | Attachment bone name in the equipment's GeckoLib model. |
| `accept_categories` | `string[]` | Accepted module categories; provide an array. Names are case-insensitive. |

Module categories are `NIGHT_VISION`, `FLASH_LIGHT`, `VISOR`, `ADMIN_POUCH`, `HELMET_COVER`, `POUCH`, `BATTERY`, `BEACON`, `HEADSET`, and `UNDEFINED`. Unknown names or an empty category array become `UNDEFINED` and do not provide valid module candidates. Categories determine mount compatibility; they do not grant functionality.

Add this fragment to a helmet definition whose model has `nvg_fix` and `headset_fix` bones:

```json
"attachments": [
  {
    "mount_id": "nvg_mount",
    "mount_type": "helmet_front",
    "mount_bone": "nvg_fix",
    "accept_categories": ["night_vision"]
  },
  {
    "mount_id": "headset_mount",
    "mount_type": "helmet_top",
    "mount_bone": "headset_fix",
    "accept_categories": ["headset"]
  }
]
```

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

Held-plate installation now uses the `install` clip in the built-in `animations/plate.animation.json`; the held-item renderer does not read the definition's `animation` field. Custom models need compatible bones. To override the animation, supply a client resource at that same path. See the [equipment operations reference](./configuration-and-equipment-en.md).

### `loadout` fields

| Field | Type | Default / requirement | Description |
| --- | --- | --- | --- |
| `type` | `string` | Must be `loadout` | Selects the container-equipment data model. |
| `id` | `string` | Required | Generates `zerocontact:<id>`. |
| `container_size` | `integer` | `0` | Number of container slots. |
| `equipment_slot` | `string` | Required | Currently supports `BACKPACK` or `RIGS`. |
| `texture`, `model`, `animation` | `string` | Empty string | GeckoLib resource paths in the `zerocontact` namespace. |

`equipment_slot: "HEADSET"` is still unsupported for `loadout`; define headsets with `type: "module_headset"`.

### Common module fields

`module`, `module_nvg`, and `module_headset` share these fields. All belong in `items` and do not require `equipment_slot`:

| Field | Type | Default / requirement | Description |
| --- | --- | --- | --- |
| `type` | `string` | Required | One of the three module types above. |
| `id` | `string` | Required | Generates `zerocontact:<id>` and registers its module category. |
| `mount_type` | `string` | Required | **Module category** from `MountCategory`, such as `night_vision`, `headset`, or `pouch`. This differs from the same-named field inside a mount. Case-insensitive. |
| `module_trait` | `string` | Required | Functionality path without a namespace, such as `nvg`, `headset`, or `radio`. |
| `durability` | `integer` | `0` | Item durability; NVG internal battery capacity is currently fixed at `12000` and is not changed by this field. |
| `texture`, `model`, `animation` | `string` | Empty string | GeckoLib resource paths in the `zerocontact` namespace. |

Currently registered generic module traits are `pouch`, `admin_pouch`, `navboard`, `nvg`, `battery`, `beacon`, `headset`, and `radio`. Use the dedicated types for NVGs and headsets to obtain the `INvg` implementation or audio profile. Their item traits are fixed to `nvg` and `headset`; changing `module_trait` does not switch their functionality. The adapter still reads that field, so provide a valid path.

Radios use `module`, category `pouch`, and trait `radio`; there is no `RADIO` module category. See [`prc_148.json`](../common/src/main/resources/data/zerocontact/default_pack/default_module/data/zerocontact/items/prc_148.json) for a complete example. Channels are configured in game and are not current module JSON fields.

### Night vision: `module_nvg`

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `vignette` | `string` | `textures/gui/bino_nvg.png` | View-mask texture path in the `zerocontact` namespace. |
| `color` | `string` | `GREEN` | Supports `GREEN`, `WHITE`, `THERMAL`, and `THERMAL_COLOR`. Case-insensitive; unknown values fall back to `GREEN`. |

```json
{
  "type": "module_nvg",
  "id": "example_nvg",
  "mount_type": "night_vision",
  "module_trait": "nvg",
  "durability": 12000,
  "texture": "textures/sb_pvs31a.png",
  "model": "geo/sb_nvg_pvs31a.geo.json",
  "animation": "animations/nvg_pvs31.animation.json",
  "vignette": "textures/gui/bino_nvg.png",
  "color": "WHITE"
}
```

This example reuses built-in resources; keep their packs enabled when distributing it. Currently `NVG.getAnimation()` always returns `animations/nvg_pvs31.animation.json`, so the JSON `animation` field alone cannot switch the animation file. Animation clips are `activate`, `deactivate`, `on_pose`, and `off_pose`.

### Headsets: `module_headset`

Omitting the entire `audio_profile` gives a compressor ratio of `0`, attack/sustain gains of `0`, pickup attenuation distance of `16`, and five EQ bands at 125, 500, 2000, 4000, and 8000 Hz (gain `0`, Q `1`). When providing this object, supply every field and the `eq` array: missing fields are not merged with the default profile. Do not explicitly use `null`.

| `audio_profile` field | Type | Description |
| --- | --- | --- |
| `compressor_ratio` | `number` | Dynamic compressor ratio. |
| `transient_attack` | `number` | Transient attack gain in dB. |
| `transient_sustain` | `number` | Transient sustain gain in dB. |
| `pick_up_attenuation` | `number` | Linear sound attenuation distance parameter while the headset is on. |
| `eq` | `object[]` | Peaking equalizer bands; `[]` sets no bands. |
| `eq[].freq_hz` | `number` | Center frequency; must be positive and finite, in Hz. |
| `eq[].gain` | `number` | Finite gain in the range `-24..24` dB. |
| `eq[].precision` | `number` | Finite Q value in the range `0.1..20`. |

```json
{
  "type": "module_headset",
  "id": "example_headset",
  "mount_type": "headset",
  "module_trait": "headset",
  "durability": 0,
  "texture": "textures/c2r_headset.png",
  "model": "geo/c2r_headset.geo.json",
  "animation": "",
  "audio_profile": {
    "compressor_ratio": 10,
    "transient_attack": -6,
    "transient_sustain": -12,
    "pick_up_attenuation": 18,
    "eq": [
      { "freq_hz": 125, "gain": -3, "precision": 1 },
      { "freq_hz": 500, "gain": -3, "precision": 1 },
      { "freq_hz": 2000, "gain": -5, "precision": 1 },
      { "freq_hz": 4000, "gain": -2, "precision": 1 },
      { "freq_hz": 8000, "gain": 5, "precision": 1 }
    ]
  }
}
```

The example reuses built-in C2R resources. The audio profile comes from a mounted headset, which must be switched on; PCM processing also depends on client setting `sound_and_visual_effects.audio_effect`. When several headsets are mounted, the implementation selects one profile; do not rely on stacking or a fixed priority. See the full default definition in [`headset_c2r.json`](../common/src/main/resources/data/zerocontact/default_pack/content_creator_smat/data/zerocontact/items/headset_c2r.json).

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

These subdirectories are organizational conventions; fields such as `texture` and `model` determine the actual paths. The current creator pack also uses files directly under `textures`, such as `textures/c2r_headset.png`. Do not infer paths from the older directory layout.

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
- Module `mount_type` uses a category name, while mount `mount_type` uses a position type; `accept_categories` and `mount_bone` match the module and model.
- Headsets provide a complete `audio_profile` or omit the whole object, and EQ values satisfy their ranges.
- `hurt_modifier` uses field names ending in `*_multiplier`.
- Material entries in `gear_recipes` use the spelling `itemId`, and recipes avoid ambiguous multi-file overrides.
- Every generated item has the required translation key and model, texture, or GeckoLib resources.
- Fully restart the game after changing registry-backed content, then check the log for JSON parsing, resource ID, Lua compilation, and duplicate-ID errors.
