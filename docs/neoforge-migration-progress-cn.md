# NeoForge 迁移进度汇报

更新日期：2026-11-12。目标：Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21。
检查基线：`1.21.1-neoforge` 分支，HEAD `00e42ff`，包含当前未提交和未跟踪的源码改动。

本文统一维护网络、能力、模块存储、业务依赖及验证进度；旧的网络适配说明、模块能力迁移说明、Capability 清单和源码引用索引已合并移除。历史调用数量、已删除文件的行号和早期阻塞描述不再沿用。

## 当前结论

迁移处于“主要接口与模块核心存储已适配，业务及完整工程验收待完成”阶段。20 个自定义网络包已接入 NeoForge Payload；9 类模块能力已接入 `ItemCapability`、数据组件、嵌套写回及同步，已有 Java 21 隔离编译和 44 项回归通过记录。背包菜单已直接使用 `IItemHandler` / `ItemHandlerCopySlot`，实体库存能力和 HUD 注册接口也已切换。

本次在 `neoforge/src/main/java` 中未检出 `net.minecraftforge`、`LazyOptional`、`initCapabilities`、旧 Overlay 接口和 `ItemStack.of`。这只是限定范围的文本复查结果，不能证明整个工程可编译或运行；测试源码仍有旧 Forge API 和已删除兼容类引用。

尚待闭合的重点为旧背包库存格式处理、胸挂库存入口整理、维修及装备业务验收、体力功能去留、测试迁移和构建配置核对。目前没有完整构建、游戏启动、联机或 CI 验收结果，不以文件改动或旧 API 命中数量换算完成百分比。

## 分项进度

| 范围 | 当前状态 | 已有改动与剩余边界 |
| --- | --- | --- |
| 目标版本与加载入口 | 配置已切换，待完整验证 | `gradle.properties` 指向 1.21.1、NeoForge 21.1.265、Architectury 13.0.8、Curios 9.5.1、GeckoLib 4.8.4；根构建使用 Java 21。模组构造器使用 `IEventBus` / `ModContainer`，注册模块数据组件、模块能力和网络事件；平台实现已移至 `neoforge` 包。 |
| 网络协议 | 源码适配完成，业务及运行待验收 | 20 个 `CustomPacketPayload` 全部注册：13 个 C2S、7 个 S2C，协议版本 `1`，主线程处理。物品使用 `RegistryFriendlyByteBuf` / `ItemStack.OPTIONAL_STREAM_CODEC`，详见下方逐包清单。 |
| 菜单与工作台 | 接口已调整，待交互验收 | 弹药、模块、背包和工作台使用新开屏接口；工作台使用 `useItemOn` / `useWithoutItem`；菜单屏幕通过 `RegisterMenuScreensEvent` 注册。实际购买、装配、槽位与同步行为待验收。 |
| 枪械弹药能力 | 消费者已切换，待业务验证 | `caliber/capability/CapabilityRegistry.CARTRIDGE` 使用 `ItemCapability` 与 `RegisterCapabilitiesEvent`，`ItemStackMixin` 提供每栈 holder；`MagazinesCompat` 等消费者已切换，旧总能力注册表及启动调用已删除。 |
| 弹药服务与持久化 | 已知写回点已修复，待业务验证 | 已有 `AmmoInjectService` / `AmmoInjector`，主要读写使用 `CUSTOM_DATA`。`TagUtil` 返回副本，修改须显式写回；`saveRounds` 已写回轮序、数量及弹种，`remapSelectedMagazineSlot` 已更新或移除选中槽位。装填、退弹及联机业务尚未验收。 |
| 模块装备与模块 trait | 核心迁移完成，隔离验证通过；游戏内待验收 | 装备挂载容器及 8 种 trait 使用每栈独立能力缓存，接通组件持久化、嵌套写回、复制及完整快照同步。旧 Provider、`ModuleStackSync` 和临时物品流 Mixin 已移除；已有 44 项隔离回归记录，实际 Mixin 加载、渲染和联机仍待验收。 |
| 背包与胸挂 | 组件存储及菜单适配已接通，旧格式待处理 | `BaseBackpack` 及胸挂子类注册基于 `CONTAINER` 的 `ComponentItemHandler`；菜单使用 `IItemHandler` / `ItemHandlerCopySlot`，已解除原先必须实现 `Container` 的限制。手持开屏已有宿主槽位锁定及服务端物品引用校验。旧 `BackpackInventory` / `IndexSimpleContainer` 已删除；未见旧 `inventory` 导入到 `CONTAINER` 的实现，`BaseRigs.manualSave` 仍保留旧格式写入入口。 |
| 实体库存 | 能力与持久化接口已调整，待行为验收 | `ArmedRaider` 保留自身 `SimpleContainer`，通过 `InventoryCarrier` 的读写接口传入注册表上下文；`ModRegEventBus` 注册 `Capabilities.ItemHandler.ENTITY` / `InvWrapper`，旧 `LazyOptional` 及实体能力覆写已移除。库存存档、换弹与掉落仍待运行验证。 |
| 维修 | 已改为服务端会话，待行为验收 | 旧 `RepairKitCap` 已删除；`RepairController` 按玩家 UUID 管理 `RepairSession`，保存计时、目标槽位、菜单和目标引用。手持使用、停止使用、菜单 Post Tick 和退出清理已接入；目标切换、菜单关闭、死亡及断线边界仍需验收。 |
| 玩家体力 | 旧实现已删除，功能未闭合 | `PlayerStamina`、Provider、挂载事件、HUD 和服务端 Tick 注册已删除。世界开关、C2S / S2C 包和 `ClientData` 接收仍保留；未见替代状态存储、计算、生命周期、服务端同步发送或 HUD 实现。 |
| 装备渲染、HUD 与事件 | 主要接口已调整，待显示及运行验收 | 已接入 `RenderableItem`、GeckoLib `GeoRenderProvider`、`RegisterClientExtensionsEvent`；模块 HUD、夜视仪电量 / 暗角和压制层使用 `RegisterGuiLayersEvent` / `LayeredDraw.Layer`。旧 Overlay 接口已无命中，显示层及渲染效果仍未经游戏验收。 |
| 第三方兼容与 Mixin | 源码已调整，待逐项运行验收 | 手持弹匣包 Mixin 使用 `IPayloadContext`；模块同步直接依赖数据组件；Minecraft Mixin 配置包含 `ModularItemStackMixin`，兼容级别为 `JAVA_21`。导航板 / 夜视仪动画 ID 检查已适配 GeckoLib 4.8。FirstAid 主源码兼容类已删除，旧测试仍需处理；TaCZ、弹匣、Curios、语音等链路待验收。 |
| CI 与发布 | 配置已对齐，待构建及产物验收 | Nightly 使用 JDK 21 和 `:neoforge:remapJar`，上传及重命名来源为 `neoforge/build/libs`，产物名称含分支、构建号和短 SHA。未执行工作流，产物匹配和发布结果待验证。 |

## 网络协议与逐包清单

实现入口：[ModMessages](../neoforge/src/main/java/net/zerocontact/network/ModMessages.java)。模组构造器只注册一次 `RegisterPayloadHandlersEvent` 监听，`PayloadRegistrar` 使用协议版本 `1` 和 `HandlerThread.MAIN`，通过 NeoForge `PacketDistributor` 发送。所有 Payload 均为必需包；客户端与服务端需同时更新，不兼容旧 Forge 数字包 ID 协议。

全部包提供 `TYPE` / `STREAM_CODEC`；下表路径的命名空间均为 `zerocontact`。保留行为描述业务入口，不代表依赖已完成运行验收。

| 数据包 | 方向 | Payload 路径 | 保留行为 |
| --- | --- | --- | --- |
| `ToggleStaminaPacket` | C2S | `toggle_stamina` | 更新世界体力开关；体力实现仍缺失 |
| `FlipVisorPacket` | C2S | `flip_visor` | 翻转面罩并回传动画 |
| `ToggleBackpackPacket` | C2S | `toggle_backpack` | 更新背包、胸挂开关 |
| `BuyGearsPacket` | C2S | `buy_gears` | 传递坐标与装备物品，调用购买逻辑 |
| `OpenAmmoSelectorPacket` | C2S | `open_ammo_selector` | 检查弹道开关、持枪及弹匣兼容 |
| `SelectAmmoPacket` | C2S | `select_ammo` | 选择弹药、判定弹种并回包 |
| `EquipPlatePacket` | C2S | `equip_plate` | 比较物品组件，装板、消耗并播放声音 |
| `SelectEquipmentMountPacket` | C2S | `select_equipment_mount` | 校验菜单类型、容器 ID 和锚点 |
| `OpenModularMenuPacket` | C2S | `open_modular_menu` | 打开模块装备菜单 |
| `MountPacket` | C2S | `mount` | 校验容器、槽位、物品快照并服务端装配 |
| `UnMountPacket` | C2S | `un_mount` | 校验容器并卸载模块 |
| `ModuleActionPacket` | C2S | `module_action` | 校验玩家、装备 / 模块身份和操作权限 |
| `ModuleDataPacket` | C2S | `module_data` | 传递操作 ID 与 NBT，由 Controller 校验并提交结果 |
| `SyncStaminaPacket` | S2C | `sync_stamina` | 接收体力值和开关；未见服务端发送实现 |
| `ToggleVisorResultPacket` | S2C | `toggle_visor_result` | 同步动画名称、长度、播放状态及空值 |
| `ClientAmmoReloadPacket` | S2C | `client_ammo_reload` | 传递枪械槽位、弹种键并客户端装填 |
| `AppendSuppressionPacket` | S2C | `append_suppression` | 传递弹道、压制增量和掠弹声音参数 |
| `SyncModulesPacket` | S2C | `sync_modules` | 同步实体、装备目标、物品 ID 和完整模块快照 |
| `SyncEquipmentCandidatesPacket` | S2C | `sync_equipment_candidates` | 同步菜单、装备、锚点、候选槽位及物品副本 |
| `UpdateNavBoardPacket` | S2C | `update_nav_board` | 校验装备 / 模块 / 维度，客户端采样地形 |

物品传输使用 `RegistryFriendlyByteBuf` / `ItemStack.OPTIONAL_STREAM_CODEC`，包含数量、组件及空物品；开屏额外数据的读写已成对调整。体力、面罩及压制的客户端处理集中于 `ClientPacketHandlers`，弹药回包使用现有 `CARTRIDGE` 能力。

手持弹匣加载 / 卸载 Mixin 已改用依赖实际声明的 `IPayloadContext` 签名。早期网络迁移中曾使用 `ItemStackStreamCodecMixin` 和临时 `CUSTOM_DATA` 标签传模块数据；它们已被数据组件方案取代，不能作为当前同步链路或后续验收目标。

## 模块能力、存储与同步

实现入口：[能力注册](../neoforge/src/main/java/net/zerocontact/armor/modular/capability/ModularCapabilityRegistry.java)、[trait 注册](../neoforge/src/main/java/net/zerocontact/armor/modular/registry/ModuleRegistry.java)、[物品栈 Mixin](../neoforge/src/main/java/net/zerocontact/mixin/minecraft/ModularItemStackMixin.java)。

`ModularCapabilityRegistry` 注册装备挂载能力及 `ADMIN_POUCH`、`POUCH`、`NAV_BOARD`、`NVG`、`BATTERY`、`BEACON`、`HEADSET`、`RADIO` 共 9 类 `ItemCapability`。装备按 `ModularEquipment` 注册，模块按 `EquipmentModule.getModuleTrait()` 查找容器工厂；未知 trait 不注册。`ModularItemStackMixin` 为每栈懒加载独立能力缓存，复制栈只复制组件，首次查询时创建自己的能力对象。

| 数据 | 当前存储及所有权 |
| --- | --- |
| 装备挂载 ID 与完整模块栈 | 自定义 `zerocontact:mounted_modules` 组件；使用不可变快照与防御性复制，注册持久化及网络编解码 |
| 夜视仪、电池、信标、耳机、导航板、电台的持久状态 | `CUSTOM_DATA` 中 `zerocontact:module_state` 下对应 trait 的数据 |
| 管理包单格地图库存 | 原版 `CONTAINER`，由 `ComponentItemHandler` 管理 |
| 普通 pouch | 无持久状态 |
| 导航板区块缓存 / 调度、电台通话状态 | 每栈能力对象的运行态，不随物品复制或存档保留 |

`StackModuleState` 在查询及修改前检查组件状态，持久状态修改立即写回；保留旧能力引用时，组件替换会在下次调用时反映。电台频道列表保存为不可变副本。当前不读取或转换旧 ForgeCaps 存档，也不导入早期临时传输标签。

挂载、复制与同步保留以下语义：

- `ModuleContainer.getModule()` 和只读挂载表返回运行中的模块栈，组件快照内部的栈不向业务暴露。对子栈执行 `set`、`remove`、`applyComponents` 等操作时，Mixin 通知宿主装备更新挂载快照。
- 安装时复制单个模块；移除、替换或收到新快照时解除旧回调，旧引用不能覆盖新挂载。重复接收相同快照时保留现有对象及导航、电台运行态。
- `replaceModules()` 表示完整替换，空快照会清空挂载。模块操作 / 配置包先修改副本，仅在返回 `CHANGED` 后提交。
- `SimpleModuleContainer` 的 NBT 读写显式接收 `HolderLookup.Provider`。模块同步包仍使用完整 NBT 快照，保留装备、模块和目标槽位校验。
- 原版物品编解码直接携带组件，覆盖菜单与创造模式物品传输。GeckoLib 会直接修改组件表分配动画 ID，因此显式模块同步前另行提交这些变更。

业务入口继续使用 `ModuleQuery`、`ModuleMountService`、`ModuleSyncService`、各 Controller 及供电 / 电台等现有服务。早期 Capability 清单中的 Service 名称与“全面解耦完成”检查项属于架构建议；当前仍有 HUD、Packet、Renderer 等直接查询能力，不能把删除 Provider 等同于全面 Service 重构完成。

## 本次静态复查

下表仅扫描 `neoforge/src/main/java`，逐行匹配，包含声明、注释及辅助类调用；不代表独立问题或编译错误数量，也不涵盖测试、Fabric 和依赖代码。

| 检索项 | 命中行数 | 文件数 |
| --- | ---: | ---: |
| `net.minecraftforge` | 0 | 0 |
| `LazyOptional` | 0 | 0 |
| `initCapabilities` | 0 | 0 |
| `getOrCreateTag(` | 8 | 4 |
| `.getTag(` | 0 | 0 |
| `ItemStack.of(` | 0 | 0 |
| 旧 `CapabilityRegistries` 任意引用 | 0 | 0 |
| 新 `CapabilityRegistry.CARTRIDGE` 引用 | 13 | 8 |
| `DataComponents.CONTAINER` | 2 | 2 |
| `DataComponentType` | 6 | 2 |
| `AttachmentType` | 0 | 0 |
| `RegisterGuiOverlaysEvent` / `IGuiOverlay` | 0 | 0 |
| `RegisterGuiLayersEvent` | 8 | 4 |

8 行 `getOrCreateTag(` 均为项目 `TagUtil` 的声明或调用，不能继续列作旧原生物品 API；它读取的是 `CUSTOM_DATA` 副本，写操作必须通过 `updateTag` 等显式提交。`DataComponentType` 命中包含声明和 Mixin 描述符，当前只注册了一个自定义 `mounted_modules` 组件。未检出 `AttachmentType` 不表示玩家状态迁移完成，体力业务仍缺失。

相较前次记录，背包菜单的 `Container` 限制、`ArmedRaider` 的旧实体能力覆写、模块 / 夜视仪旧 Overlay、旧通用容器的序列化调用和 FirstAid 主源码直接引用已不再构成当前源码中的同一阻塞；其中部分消失来自实现删除，相关功能仍需按下列条件验收。

## 剩余工作与验收条件

1. **背包 / 胸挂库存与旧数据。** [BackpackContainerMenu](../neoforge/src/main/java/net/zerocontact/item/backpack/menu/BackpackContainerMenu.java) 已完成 handler 和复制槽位适配，需验收普通点击、Shift 转移、数字键 / 副手交换、宿主槽位锁定、物品替换后关闭，以及手持、Curios 和队友背包入口的同步与存档。当前未见旧 `CUSTOM_DATA.inventory` 到 `CONTAINER` 的导入，需明确是否支持该旧格式。`BaseRigs.manualSave` 仍写旧 `inventory`；`ReloadManager` / `ServerAmmoSelector` 仅在 handler 为 `ItemStackHandler` 时调用它，新组件 handler 直接写回组件。需整理遗留路径并验证菜单和换弹使用同一库存，不把旧方法保留视为旧数据已迁移。
2. **弹药、模块与维修业务。** 验收弹匣轮序、数量、弹种、选中槽位重映射、选择弹种、装填 / 退弹、创造模式一次性标记及客户端回包；模块需覆盖玩家 / Curios 槽位、装卸 / 替换、空快照、自身与跟踪玩家同步、创造模式传输、共享电池和导航 / 电台运行态。维修需覆盖间隔、手持与菜单切换、目标替换、菜单关闭、死亡与断线，核对每次业务 tick 的计时和清理。
3. **体力功能去留。** 如保留，补齐服务端权威状态、计算、初始同步、生命周期 / 死亡复制规则及 HUD；如移除，清理命令、世界开关、网络和客户端残留。现有网络包不构成完整实现。
4. **显示层、第三方兼容与 Mixin。** 验收装备 / Curios 渲染、面罩动画、模块 HUD、夜视仪电量 / 暗角、热成像及压制效果；核对 TaCZ、弹匣、Curios、GeckoLib 和语音兼容。模块隔离代理不能证明游戏加载器实际应用了 `ModularItemStackMixin`，关键 Minecraft / TaCZ / 弹匣 Mixin 均需在实际启动中确认。
5. **测试与构建范围。** [MagazinesCompatNullSafetyTest](../neoforge/src/test/java/net/zerocontact/compat/MagazinesCompatNullSafetyTest.java) 仍引用 `net.minecraftforge.registries` 和旧 `getOrCreateTag`；[HitBodyPartTest](../neoforge/src/test/java/net/zerocontact/caliber/damage/HitBodyPartTest.java) 仍引用 FirstAid 类型以及主源码已删除的 `FirstAidCompatHandler` / `FirstAidCompatCompat`。这些测试未完成目标版本适配。`settings.gradle` 仍包含 Fabric 子项目，其 Fabric API 配置为 1.20.1，需结合 `enabled_platforms=neoforge` 核对构建范围；尚未验证实际解析影响。
6. **完整工程与发布验收。** 先取得用户对可能重建 Loom 缓存、影响现有启动配置的明确许可，再运行可能触发配置阶段缓存变更的 Gradle 命令。完整构建通过后，验证独立服务端启动、客户端连接、20 个包的实际触发、菜单、物品持久化与同步及关键 Mixin；最后验证 Nightly 工作流、JAR 匹配、重命名与发布结果。

## 已有验证记录与边界

本次文档更新只静态读取构建文件、源码、Git 状态和已有日志，并检查文档引用；没有重新运行编译或回归测试，没有运行 Gradle、IDE 同步、游戏、联机或 CI。

模块迁移已有日志 `neoforge/build/modular-capability-check/regression.log` 记录：`ModularCapability: 44 regression checks passed`。复现入口为[模块检查脚本](../scripts/check-modular-capabilities.ps1)：

```powershell
./scripts/check-modular-capabilities.ps1 -GradleUserHome E:/.gradle -JavaHome 'C:/Program Files/Microsoft/jdk-21.0.3.9-hotspot'
```

脚本只读取已有映射 JAR、Minecraft / NeoForge 库、模组依赖及 `common/build/classes/java/main`，不调用 Gradle、不下载依赖，输出写入 `neoforge/build/modular-capability-check`。它隔离编译能力、存储、模型、trait 注册、Controller、查询和挂载服务等局部源码；真实 Minecraft / NeoForge 类运行于独立 JVM，测试代理复制已编译 Mixin 的字段和方法并按目标签名织入回调，验证存储行为与注入目标方法存在。

44 项检查覆盖 9 类能力、未知 trait、每栈缓存及复制隔离、嵌套耗电与共享电池、存档和原版物品网络编解码、组件防御性复制、模块 NBT 快照、空快照清除、旧回调失效、地图库存、导航运行态、耳机 / 信标开关、夜视仪内部电量与外部供电快照、电台配置与通话状态隔离。该结果不覆盖完整工程、全部网络及显示层，也不表示游戏加载器已实际应用 Mixin；本次未重跑，不能自动视为当前所有未提交改动均已通过。

此前网络适配记录另有基于现有 JDK / 缓存 JAR 的临时 `javac --release 21 -proc:none` 检查，覆盖 20 个包的编解码、注册引用及部分辅助接口；当时替换了业务处理体和少量模型，且部分物品流实现后来已被替换，因此仅保留为历史局部验证记录，不作为当前完整网络业务或 Mixin 的通过依据。

遵守项目 `AGENTS.md`：本次未重建、清理、删除或使 Loom 缓存失效，也未停止 IDE 或游戏进程。
