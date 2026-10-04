# CHANGELOG

## indev.1.0.0

### Add

#### Items

- Dyer Precision Jpc 2.0
- Steel Plate
- SlimeIndustries Plate
- BallisticConvoy Plate

#### Mechanism

- Plates consume durability and take in bullet damages depends on bullet damage amount, and it's protection level.
- Plate will bounce off the bullets which is Ricochet if the incident angle is big.

## indev.1.0.2

### Add

#### Items

- Dyer Precision Armatus Vest System
- Steel Ingot
- Steel Sheet

#### Mechanism

- Stamina System
- Armor plate movement speed fix

#### Recipes

- Steel plate can be made using Mechanical press and Mechanical saw from Mod *Create*.

## indev.1.0.4

### Fix

Fixed the issue with plates do not absorb damage.

## indev.1.0.5

### Add

This is a Data-driven feature update, you can customize your own ballistic plate from now on.
Tutorials about customize your own datapack can be found in GitHub wiki.

## indev.1.0.6

### Add

This is a Data-driven feature update, you can customize your own ballistic plate from now on.
Tutorials about customize your own datapack can be found in GitHub wiki.

## indev.1.0.7

### Add

- Added new hostile entity `Armed Raider`, can be spawn from the spawn egg.
- Added bullet whizz sound effect, you can hear bullet pass by you.

### Fix

- Changed the behaviour the stamina system interrupt player from sprinting. But absolute normal functioning is not
  guaranteed.

## indev.1.0.7-hotfix1

### Fix

- Fixed the behaviour the stamina system interrupt player from sprinting or jumping.

## indev.1.0.8

### Add

- New Items
    - Helmets
        - 6b47
        - Bastion
        - Altyn
        - AirFrame
        - Untar
    - Armors & plate-carriers
        - Thor
        - Untar
        - 6b2
        - 6b23-1
        - 6b23-2
        - Hexgrid
        - Defender2
- New Slots
    - Uniform slots
    - Armband slots

- New feature
    - Now you can toggle visor on `Alytn`
    - Certain `Plate carriers` will absorb a little bit of damage by its fabrics/lining even though it has no plates
      installed
    - Data-driven feature and structure have been changed, there will be a tutorial soon, or join communities for
      getting help.

### Fix & Optimize

- Optimized `Raider` gun perform logics, Pathing logics
- Shrunk the size of the `Raider`
- Fixed the issue that `Raider` does drop plates after reloading the level
- Fixed the issue that `Raider` would stick on an animation phase
- `Raider` can open doors now
- Some miscellaneous fix

## indev.1.0.9

- New Items
    - T-20 backpack
    - Colorful Armbands
    - Variants of Bastion helmet
    - Variants of Tagilla helmet
    - Updated AVS model
    - Cold fear mask
    - Cyan cap
    - Arctic variant of 6b47 helmet
- New Slots
    - Backpack slot
- New mechanism
    - `/stamina <true|false>` Command to toggle stamina feature
    - `/dogtag <true|false>` Command to toggle the spawn of dog tags
    - `/expermentalBallistic <true|false>` Command to toggle damage interceptor for TACZ guns, see Wiki
    - Supports expanding formatted datapacks to customize armors, PC plates, helmets, and custom interceptor to modify caliber damages
### Fix & Optimize

- AI improves for `raider`
- Fix crashes in specific situations

## indev.1.1.0

### New Items

- A workbench named *thoughbook* for crafting gears in survival.
- Uniform G99, SPN, British23rd regiment
- Backpack British23rd regiment
- *Thunderbolt* Rigs
- *Jumpable Plate Carrier* 1.0, 2.0, swimmer cut
- 6B43 body armor
- Phonetalker helmet
- TBH helmet
- 6B47 helmet [Arctic, EMR]
- Gas masks [PMK2,ZK,M50,MP5]
- Cap *Boss*

### New Mechanism

- Override workebench recipes by datapacks.
- Backpacks can be interacted with allies by crouch right click behind.

### Fix & Optimize

- Ammo can be extracted from Rigs into TacZ guns.
- Fix the Altyn visor malfunction.
- Improvement for calculating armor & helmet durability separated from overall equipments.
- Improvement for calculating damage for armors & plate-carriers.
- Add tooltips for damage reduction.
- Basic localization


## indev.1.1.1

### Fix & Optimize
- Fix the missing argument of custom datapack
- Fix Gui malfunction when have both backpack and rigs equipped
- Custom datapacks now have their own creative menu tabs
- Format change of datapacks
- Fix crash in the specific condition of playing bullet fly-by sound
- Fix wrong ammo extracting on rigs
- Fix a crash caused by the entity when have Oculus and shader packs enabled
- Fix abnormal damage calculation
- Fix invulnerability on falling or getting hit by mobs with armor
- Rebalancing calibers
- Ai and animation optimization for the entity
- Chinese localization

## indev.1.1.2

- Optimize tooltips for armors.
- Add config to toggle bullet whizz.
- Fix missing properties for plates.
- Fix a crash caused by raider.
- Add voices for raider.

## Release.1.1.3

- New Cartridge/Ammo Type system as a completion of the mod, support data pack, check wiki
  - This feature is active by default. If you want old behavior, command `/experimentalBallistic false` 
- New rigs:  
  - Alice
  - Condor 498
  - Fast Tac
  - S.O.P
- New backpack: 6sh118
- New built-in cartridges for caliber:
    - 9x19: AP,Luger,Pbp,Pst,Rip
    - .45ACP: AP,FMJ,HS,Rip
    - .308: M62,M80,M80A1,M993,SP,UN
    - .338LM: AP,FMJ,TAC-X,UCW
    - 5.45x39: BP,BS,BT,PP,PS,T
    - 5.56x45: AP,HP,M855,M855A1,M856,M856A1,M995
    - 7.62x39: AP,BP,HP,PP,PS,SP
    - 7.62x54: FMJ,HP,LPS,SNB
    - All above's data can be edited from the datapack, Check wiki.
- Compatibility for `FirstAid`,`FirstAidCompat`
- Compatibility for NVG from `ModernMayhem`
- Refine the GUI for backpack container
- Miscellaneous fixes
- Migrate built-in Armor/Ammo data into default datapack, check wiki to modify them.
- `Raider` supports custom NBT spawn with loadout, check wiki

## Release 1.1.4

- Update Forge dependency to 47.3.10, versions before this are no longer supported
- Update TaCZ dependency to 1.1.8-hotfix, versions before this are no longer supported
- Compatibility for TaCZ:Magazines
- New repair kit. Armors are no longer broken and vanished anymore.
- Fix the issue where manual-feed guns failed to load any ammo
- Fix the issue where failed to extract ammo from ammo boxes
- Fix the issue where system cannot apply the proper blunt damage
- The penetrations odds are more realistic now
- Improve the effect for bullet suppression scenario
- Uniform arm layers now render in first-person, unknown incompatibilities for now.
- Miscellaneous Fixes and Improvements


TaCZ:magazines Compatibility Notes:

The magazines provided by the TaCZ:magazines mod support two methods of loading ammunition:

In the inventory: Pick up ammunition and right-click a magazine to load it into the magazine. Right-click the magazine again to unload the ammunition.
When holding the magazine: Left-click to load ammunition, and right-click to unload ammunition.

Q: The magazine does not automatically load ZeroContact ammunition when first held. What should I do?
A: In the inventory screen, pick up the ammunition and load at least one round into the magazine first. After that, you can continue loading ammunition while holding the magazine.

Q: Do magazines support mixed ammunition/catridge types?
A: Currently, the compatibility system does not support mixed ammunition types in a single magazine.

Q: How do I select ammunition/cartridge types in Creative Mode?
A: In Creative Mode, the magazine loading system works by copying data from the weapon's currently installed magazine and applying it to newly generated magazines.

For ZeroContact ammunition/cartridge types, you can:

Take an empty magazine.
Load the desired ammunition type into it.
Switch to Survival Mode and equip that magazine on the weapon.
Switch back to Creative Mode.

The weapon will then be able to use that ammunition type infinitely in Creative Mode.

## Release 1.1.4-hotfix

- Fix the issue where damage reduction applies to non-armored entity or invulnerable to entity with plate-carrier installed.
- Fix the issue where uniform no longer render in a specific situation.
- Fix the issue where user can trigger infinite kit repairing when kit has low durability and equals to the target item's.

## Beta 1.1.5

- Effect declaring hook and Lua script support for custom ammo types.
- Basic compatibility for TaCz:magazines 0.2.0-beta.
- Add config to toggle applying flesh damage to unarmored entity.
- Add Mask curios.
- Advanced ammo stats for custom ammo types.
- Fix ammo type indicator shifting with scaling.
- Miscellaneous fixes.

### New Builtin Ammo Types
- 50BMG API-T.
- 40mm incendiary shell, smoke shell.
- 12g AP slug, Dragon-Breath shell, Flechette shell.
- 5.7x28mm L191, SB193, SS190.
- 5.8x42mm DBX95, DBP191, DVC12.

## Beta 1.1.6

### New Items

- Night vision and thermal goggles: PVS-31, PVS-31A, GPNVG-18, and T7.
- Headsets: ComTac II (OD, CB) and Ops-Core AMP (TAN, BLACK).
- Equipment modules: AA Battery Pouch, Helmet Mounted Beacon, TMC Admin Pouch, and S&S Navboard FlipLite.
- AN/PRC-148 MBITR radio, including a black variant.
- SMAT Content Creator Pack:
  - FCPC V5, SS LV119 (Icebreaker), IBA OTV, IOTV Gen4, C2R MOR Lite, and Hound Wolf plate carrier.
  - C2R Fast and Hound Wolf helmets, and a black Bastion helmet cover.
  - CP G3 Pro uniforms, including CTSFO variants, Hound Wolf uniform and jeans, and The Wedge jacket and pants.
  - Adept Colossus plate, additional PVS-31A and GPNVG-18 models, ComTac XPI and Hound Wolf AMP headsets.

### New Mechanism

- Modular equipment system: install compatible modules on armor and helmets through the equipment menu.
  - Open the menu with the grave accent key or `/zerocontact equipment`.
  - Use `G` to open the module HUD, scroll to select a module, and use `H` to activate it. The HUD remembers the last selection.
- Night vision and thermal effects with view masks, equip animations, and sound effects.
- Headset audio processing with sound pickup, equalization, and gunfire compression.
- MBITR radio integration with Simple Voice Chat, supporting a home channel and up to 16 scan channels.
  - Use `Shift + 3` to configure an active mounted radio. Communication works within 128 blocks in the same dimension.
- Animated installation of held plates into empty front or back plate slots.
- `/modular <target> <equipmentSlot> <mount_id> <module>` command for installing or removing equipment modules, with slot and mount suggestions.
- Client config option to toggle audio processing; server config options for First Aid limb and helmeted-head damage factors.
- Workbench module category, improved list layout, and additional recipes for existing equipment and modules.

### Data Packs & Extensions

- Reorganize built-in content into separate ammo, armor, loadout, module, and SMAT creator packs.
- Add data-driven equipment mounts and modules, configurable night vision colors and view masks, and custom headset audio profiles.
- Add per-mob headshot and bodyshot damage rules.
- Add Forge `ResolveHitBodyPartEvent.Pre/Post` and `HitProcessEvent.Pre/Post` events for body-part resolution and damage extensions.
- Add English and Chinese references for [pack authoring](docs/data-driven-pack-guide-en.md), [ammo definitions](docs/ammo-definition-json-en.md), [Lua helpers](docs/lua-helpers-en.md), and [configuration and equipment operations](docs/configuration-and-equipment-en.md).
- Legacy packs using `pack_name: "zero_contact"` are now skipped with a deprecation warning. Migrate them to the current pack structure and a unique pack name; check for leftover definitions after upgrading.

### Compatibility

- Update temporary compatibility for TaCZ Magazines 0.2.6.
- Preserve ammo IDs and variants per cartridge, supporting mixed variants of the same caliber in detachable magazines.
- Track chambered ammunition separately so the next shot retains its ammo parameters when a magazine is removed or replaced.
- Improve magazine loading, unloading, reloading, selector overlays, and tooltips showing cartridge groups in feeding order.
- Improve First Aid body-part damage handling; limb hits no longer consume helmet, chest armor, or plate durability.

### Fix & Optimize

- Fix modules disappearing after armor is unequipped on servers, and improve module capability synchronization.
- Fix multiplayer night vision animation desync, equip sound synchronization, and incorrect module durability loss.
- Improve night vision rendering and view masks, disable its animations in GUI previews, and fix rendering after switching game modes.
- Fix thermal target depth glitches and the SMAT GPNVG-18 view mask.
- Fix plate installation failing to complete, reset the installation timer, and cancel installation when the held item changes.
- Fix backpack inventory synchronization, ammo selection after refactoring, and explosion delay calculation.
- Fix TaCZ and TaCZ Magazines mixin compatibility issues and improve reload detection.
- Refactor damage processing and correct body-part and headshot factor handling.
