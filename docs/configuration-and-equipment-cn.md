# ZeroContact 配置、命令与装备操作

本文对照截至 `09966b2`（2026-10-04）的提交整理，面向 Forge 版玩家与整合包作者。自定义物品字段见[数据驱动包指南](./data-driven-pack-guide-cn.md)，弹匣兼容和伤害事件见[弹药定义参考](./ammo-definition-json-cn.md)。

## 配置

配置项定义见 [`ModConfigs`](../forge/src/main/java/net/zerocontact/config/ModConfigs.java)，注册见 [`ZeroContactForge`](../forge/src/main/java/net/zerocontact/forge/ZeroContactForge.java)。客户端配置使用 `config/zerocontact-client.toml`；服务端配置使用世界目录下的 `serverconfig/zerocontact-server.toml`（专用服务器通常为 `world/serverconfig/zerocontact-server.toml`）。先启动相应客户端或世界以生成文件，再修改对应配置。

| 作用端 | 配置路径 | 默认值 | 用途 |
| --- | --- | --- | --- |
| 客户端 | `sound_and_visual_effects.audio_effect` | `true` | 启用符合条件的枪声、环境、实体与天气声音的 PCM 处理，包括耳机配置。 |
| 客户端 | `sound_and_visual_effects.bullet_suppression` | `true` | 弹丸压制的视听反馈。 |
| 客户端 | `tooltips.trajectory_tooltip` | `true` | 持枪检查物品栏弹药时显示弹道。 |
| 客户端 | `tooltips.ammo_type_Overlay` | `true` | 弹种 HUD 提示；注意键名大小写。 |
| 客户端 | `tooltips.ammo_type_toolTip` | `true` | 弹种物品提示；注意键名大小写。 |
| 服务端 | `damage.flesh_damage_on_unarmored` | `true` | 对无护甲目标启用口径肉体伤害计算；四肢命中有独立处理路径。 |
| 服务端 | `first_aid.limbs_factor` | `0.25` | First Aid 部位解析成功时的手臂/腿部伤害倍率。 |
| 服务端 | `first_aid.head_factor` | `0.2` | First Aid 部位解析成功，且玩家佩戴实现 `HelmetInfoProvider` 与 `ICombatArmorItem` 的头盔时的头部伤害倍率。 |

两个 First Aid 倍率范围均为 `0.1..128`，在部位伤害计算后应用一次。集成需要同时安装 `firstaid` 与 `tacz_firstaid_compat`；它不会为未安装依赖的游戏自动提供四肢定位。服务器决定伤害规则，客户端配置界面中的视听选项不修改这些服务端倍率。

内置包覆盖开关是单独的 `config/zerocontact/override.toml` 中的 `pack.default_pack_override`，不属于上述 Forge 配置文件，详见[包安装说明](./data-driven-pack-guide-cn.md#安装位置与加载时机)。

## 命令

命令定义见 [`CommandManager`](../forge/src/main/java/net/zerocontact/command/CommandManager.java)。

| 命令 | 权限与用途 |
| --- | --- |
| `/zerocontact equipment` | 玩家打开装备模块菜单，无需 OP 权限。 |
| `/dogtag <true\|false>` | 玩家且权限等级至少为 `2`；设置狗牌掉落，新建状态默认关闭。 |
| `/experimentalBallistic` | 玩家且权限等级至少为 `2`；查询实验弹道开关。 |
| `/experimentalBallistic <true\|false>` | 同上；设置实验弹道，新建状态默认开启。关闭时伤害口径匹配使用内置口径集合，不能据此认为全部自定义弹体参数或钩子都被禁用。 |
| `/modular <target> <equipmentSlot> <mount_id> <module>` | 玩家且权限等级至少为 `2`；为目标装备挂载模块或移除模块。 |

后三类管理命令要求命令来源是玩家，服务器控制台不能直接执行。狗牌与实验弹道状态存入世界的 `zerocontact_command_state` SavedData，并非 TOML 选项。

`/modular` 的 `target` 必须匹配单个生物实体；`equipmentSlot` 使用原版槽位名（`head`、`chest`、`legs`、`feet`、`mainhand`、`offhand`），`mount_id` 使用装备挂点路径，不带 `zerocontact:` 前缀。装备须实现 `ModularEquipment`，模块分类须被该挂点接受。命令会提供槽位和挂点补全。

例如，先穿戴默认创作者包的 `zerocontact:helmet_c2r`，再执行：

```mcfunction
/modular @s head headset_mount zerocontact:headset_c2r
/modular @s head headset_mount minecraft:air
```

第一条挂载默认物品栈，第二条移除该模块。这是管理编辑命令：挂载不会消耗背包中的模块，移除不会把模块返还到背包。普通玩家应通过装备菜单操作。

## 模块菜单与快捷操作

| 默认按键 | 用途 |
| --- | --- |
| 反引号键 | 打开装备模块菜单，也可用 `/zerocontact equipment`。 |
| `G` | 开关模块 HUD，打开时用滚轮切换选中模块。 |
| `H` | 激活当前选中的模块动作。 |
| `Shift + 3` | 打开第一个已挂载且已开启的无线电配置界面。 |

按键可在游戏控制设置中修改。装备必须定义兼容挂点；只有放在背包中的模块不会作为已挂载模块工作。

### MBITR 无线电

默认 `prc_148`、`prc_148_black` 使用 `module_trait: "radio"`，挂载到接受 `POUCH` 分类的挂点。通过模块动作开启电台后，按 `Shift + 3` 设置主频道和扫描频道。

先输入主频道并按 `ENT` 提交；`GR` 切换主频道/扫描频道模式，`↑`、`↓` 选择扫描项，输入数字后按 `ENT` 更新或添加该项。`ESC` 清除当前输入，`×` 关闭界面。例如输入 `300` 并提交，主频道显示为 `30.0 MHz`。

- 频率范围为 `30.0..512.0 MHz`，以 `0.1 MHz` 为步长；内部用整数 `300..5120` 存储，例如 `300` 表示 `30.0 MHz`。数字输入也使用此整数形式。
- 最多设置 `16` 个扫描频道。频道信息保存在模块能力 NBT 中，而不是物品定义 JSON。
- 语音传输接入 Simple Voice Chat，双方需有可用的语音连接并开启已挂载的电台；沿用其麦克风/PTT 输入。
- 接收者需在同一维度、距离不超过 `128` 格，且当前频道与发送频道一致或扫描列表包含发送频道。
- 扫描可捕获正在发送的扫描频道，随后发话时在捕获频道回复；停止发话后恢复主频道。

实现参考 [`MBITRController`](../forge/src/main/java/net/zerocontact/armor/modular/module/radio/service/MBITRController.java)、[`RadioContainer`](../forge/src/main/java/net/zerocontact/armor/modular/module/radio/container/RadioContainer.java) 和 [`RadioChatPlugin`](../forge/src/main/java/net/zerocontact/compat/RadioChatPlugin.java)。

### 插板安装

主手持插板并使用，会在前/后插板有空位时开始约 `50` tick（2.5 秒）的安装动画。完成后服务端再次核对手持物品及标签，优先装入 `front_plate`，其次 `back_plate`，消耗手持插板并播放装备声音。切换主手物品或其标签发生变化会中断安装；两槽已满时不会开始安装。

自定义插板的模型和固定动画资源要求见[插板字段](./data-driven-pack-guide-cn.md#plate-字段)。实现参考 [`PlateInteractionManager`](../forge/src/main/java/net/zerocontact/client/interaction/PlateInteractionManager.java) 与 [`EquipPlatePacket`](../forge/src/main/java/net/zerocontact/network/c2s/EquipPlatePacket.java)。
