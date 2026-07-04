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