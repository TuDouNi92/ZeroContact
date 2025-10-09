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
