# ZeroContact Configuration, Commands, and Equipment Operations

This reference covers the Forge implementation through `09966b2` (2026-10-04) for players and modpack authors. See the [pack authoring guide](./data-driven-pack-guide-en.md) for custom item fields and the [ammo reference](./ammo-definition-json-en.md) for magazine compatibility and damage events.

## Configuration

Settings are defined in [`ModConfigs`](../forge/src/main/java/net/zerocontact/config/ModConfigs.java) and registered in [`ZeroContactForge`](../forge/src/main/java/net/zerocontact/forge/ZeroContactForge.java). Client settings use `config/zerocontact-client.toml`. Server settings use `serverconfig/zerocontact-server.toml` inside the world directory (usually `world/serverconfig/zerocontact-server.toml` on a dedicated server). Start the client or world first to generate the files, then edit the relevant configuration.

| Side | Setting path | Default | Purpose |
| --- | --- | --- | --- |
| Client | `sound_and_visual_effects.audio_effect` | `true` | PCM processing for eligible gunfire, ambient, entity, and weather sounds, including headset profiles. |
| Client | `sound_and_visual_effects.bullet_suppression` | `true` | Audiovisual feedback from incoming bullets. |
| Client | `tooltips.trajectory_tooltip` | `true` | Trajectory display while holding a gun and inspecting inventory ammunition. |
| Client | `tooltips.ammo_type_Overlay` | `true` | Ammo-type HUD display; preserve the key's capitalization. |
| Client | `tooltips.ammo_type_toolTip` | `true` | Ammo-type tooltips; preserve the key's capitalization. |
| Server | `damage.flesh_damage_on_unarmored` | `true` | Caliber flesh-damage calculation for unarmored targets; limb hits have a separate handling path. |
| Server | `first_aid.limbs_factor` | `0.25` | Arm/leg damage factor when First Aid body-part resolution succeeds. |
| Server | `first_aid.head_factor` | `0.2` | Head damage factor when First Aid resolution succeeds and the player's helmet implements both `HelmetInfoProvider` and `ICombatArmorItem`. |

Both First Aid factors accept `0.1..128` and are applied once after body-part damage calculation. Integration requires both `firstaid` and `tacz_firstaid_compat`; these settings do not supply limb detection without those dependencies. The server controls damage rules; audiovisual options in the client configuration screen do not change the server factors.

The built-in pack override is a separate setting, `pack.default_pack_override` in `config/zerocontact/override.toml`, rather than one of these Forge configuration files. See [pack installation](./data-driven-pack-guide-en.md#installation-location-and-loading-time).

## Commands

Commands are defined in [`CommandManager`](../forge/src/main/java/net/zerocontact/command/CommandManager.java).

| Command | Permission and purpose |
| --- | --- |
| `/zerocontact equipment` | Opens the player's equipment module menu; no OP permission required. |
| `/dogtag <true\|false>` | Player with permission level at least `2`; sets dogtag drops, disabled in newly created command state. |
| `/experimentalBallistic` | Player with permission level at least `2`; queries the experimental-ballistics switch. |
| `/experimentalBallistic <true\|false>` | Same permission; sets experimental ballistics, enabled in newly created command state. Disabling it uses the built-in caliber set for damage matching; this does not imply that all custom projectile parameters or hooks are disabled. |
| `/modular <target> <equipmentSlot> <mount_id> <module>` | Player with permission level at least `2`; mounts or removes a module on the target's equipment. |

The three administrative command families require a player source and cannot be executed directly from the server console. Dogtag and experimental-ballistics states are persisted in the world's `zerocontact_command_state` SavedData rather than TOML settings.

For `/modular`, `target` must identify one living entity. `equipmentSlot` uses a vanilla slot name (`head`, `chest`, `legs`, `feet`, `mainhand`, or `offhand`). `mount_id` is the equipment's mount path without a `zerocontact:` prefix. Equipment must implement `ModularEquipment`, and the mount must accept the module category. The command supplies slot and mount suggestions.

For example, first equip the default creator pack's `zerocontact:helmet_c2r`, then run:

```mcfunction
/modular @s head headset_mount zerocontact:headset_c2r
/modular @s head headset_mount minecraft:air
```

The first command installs a default item stack; the second removes it. This is an administrative editing command: installation does not consume an inventory module, and removal does not return one to the inventory. Ordinary players should use the equipment menu.

## Module menu and shortcuts

| Default key | Purpose |
| --- | --- |
| Grave accent key | Opens the equipment module menu; `/zerocontact equipment` also works. |
| `G` | Toggles the module HUD; scroll to select a module while it is open. |
| `H` | Activates the selected module action. |
| `Shift + 3` | Opens configuration for the first mounted, active radio. |

Keys can be changed in the game controls. Equipment must define compatible mounts; inventory-only modules do not function as mounted modules.

### MBITR radio

Default `prc_148` and `prc_148_black` items use `module_trait: "radio"` and mount to slots accepting `POUCH`. Activate the radio through its module action, then press `Shift + 3` to configure the home and scan channels.

Enter the home channel first and press `ENT` to submit. `GR` switches between home and scan-channel modes; `↑` and `↓` select a scan entry. Enter digits and press `ENT` to update or add it. `ESC` clears the current input and `×` closes the screen. For example, submitting `300` sets the home channel to `30.0 MHz`.

- Frequencies range from `30.0..512.0 MHz` in `0.1 MHz` increments. They are stored as integers `300..5120`, so `300` means `30.0 MHz`; numeric input also uses this integer form.
- Up to `16` scan channels are accepted. Profiles are stored in the module capability's NBT, not the item-definition JSON.
- Voice transmission integrates with Simple Voice Chat. Both players need working voice connections and active mounted radios; use its microphone/PTT input.
- Receivers must be in the same dimension, within `128` blocks, and either on the transmitting channel or scanning it.
- Scanning can capture a transmitting scan channel, allowing the next transmission to reply on that channel. Ending transmission restores the home channel.

See [`MBITRController`](../forge/src/main/java/net/zerocontact/armor/modular/module/radio/service/MBITRController.java), [`RadioContainer`](../forge/src/main/java/net/zerocontact/armor/modular/module/radio/container/RadioContainer.java), and [`RadioChatPlugin`](../forge/src/main/java/net/zerocontact/compat/RadioChatPlugin.java).

### Plate installation

Using a plate in the main hand starts an installation animation lasting approximately `50` ticks (2.5 seconds) when a front or back plate slot is empty. On completion, the server checks the held item and tags again, fills `front_plate` first or `back_plate` next, consumes the held plate, and plays the equip sound. Changing the main-hand item or its tags interrupts installation; it does not start when both slots are occupied.

See [plate fields](./data-driven-pack-guide-en.md#plate-fields) for custom model and fixed animation requirements. The implementation is in [`PlateInteractionManager`](../forge/src/main/java/net/zerocontact/client/interaction/PlateInteractionManager.java) and [`EquipPlatePacket`](../forge/src/main/java/net/zerocontact/network/c2s/EquipPlatePacket.java).
