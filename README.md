# Tweakeroo Overlay

一个 [Tweakeroo](https://modrinth.com/mod/tweakeroo) 的**非官方**附属模组：在屏幕右上角以
**右对齐**的列表实时显示当前已启用的 Tweakeroo 功能开关，每行一个，默认使用**绿色**文字。

A (unofficial) Tweakeroo add-on that renders a right-aligned HUD list of the currently enabled
Tweakeroo feature toggles in a screen corner. One feature per line, green by default.

![icon](src/main/resources/assets/tweakeroo_overlay/icon.png)

---

## 功能 / Features

- **实时显示已启用的 Tweakeroo 功能**：直接读取 `FeatureToggle` 的当前值，用热键开关功能后
  无需任何操作即可立即反映到悬浮列表中。
- **变更追踪**：按 `F7` 记录当前已启用的功能作为基准；之后**只显示发生变化的功能**（仅显示
  功能名，无额外标记）：新开启的显示为绿色，新关闭的显示为红色，未变化的功能自动隐藏；再按一次
  `F7` 清除基准。
- **默认右上角、右对齐、每行一个、绿色**；颜色、位置、缩放、背景、行距、排序等均可配置。
- **可扩展、能适配其他 Tweakeroo 附属模组**：
  - 自动发现其他依赖/提及 Tweakeroo 的附属模组（例如 [TweakerMore](https://modrinth.com/mod/tweakermore)、
    [Eclipse's Tweakeroo Additions](https://modrinth.com/mod/eclipses-tweakeroo-additions)），
    通过 MaLiLib 的配置界面注册表（`Registry.CONFIG_SCREEN`）和配置类静态字段读取它们的
    功能开关；
  - 提供公开的 `FeatureProvider` API，其他模组可以通过自定义 Fabric entrypoint
    `tweakeroo_overlay` 主动接入（见下文）。
- **只读、安全**：不会写入或修改 Tweakeroo / MaLiLib 的任何配置，不会注册配置回调，只是轮询
  当前值；任何 provider/可视化错误都会被捕获并降级处理，不会导致游戏崩溃。
- **可配置隐藏**：打开任意界面或 F3 调试界面时自动隐藏（可关闭）。
- **i18n**：内置英文与简体中文翻译；F6 可开关整个悬浮列表。

## 运行要求 / Requirements

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.3.x（26.3 ≤ 版本 < 26.4） |
| Fabric Loader | ≥ 0.20.1 |
| Fabric API | 任意 26.3 版本（≥ 0.165.0+26.3 测试通过） |
| Java | ≥ 25 |
| [MaLiLib](https://modrinth.com/mod/malilib) | ≥ 0.30.0（0.30.2 测试通过） |
| [Tweakeroo](https://modrinth.com/mod/tweakeroo) | ≥ 0.30.0（0.30.1 测试通过） |

> 本模组只安装在**客户端**（`"environment": "client"`），不需要安装在服务器。

## 安装 / Installation

1. 安装 Fabric Loader 0.20.1+ 与 Fabric API；
2. 将 `tweakeroo-overlay-1.0.0.jar` 放入 `mods/` 文件夹；
3. 同时安装 MaLiLib 与 Tweakeroo；
4. 启动游戏，默认在右上角显示已启用的 Tweakeroo 功能；按 `F6` 开关悬浮列表，按 `F7` 记录/
   清除变更基准。

## 配置 / Configuration

配置文件：`config/tweakeroo-overlay.json`（首次启动后自动生成，修改后重启游戏生效）。
所有颜色均为 ARGB 十进制整数（例如 `0xFF55FF55` = 不透明绿色）。

```json
{
  "schemaVersion": 1,
  "enabled": true,
  "corner": "TOP_RIGHT",
  "offsetX": 4,
  "offsetY": 4,
  "scale": 1.0,
  "showBackground": true,
  "backgroundColor": 1711276032,
  "backgroundPadding": 2,
  "lineSpacing": 1,
  "showShadow": true,
  "showDisabled": false,
  "enabledColor": -11141291,
  "disabledColor": -5592406,
  "newlyEnabledColor": -16711936,
  "newlyDisabledColor": -43691,
  "baselineActive": false,
  "baselineFeatures": [],
  "diffOnlyChanged": true,
  "showProviderName": false,
  "sortMode": "CONFIG_ORDER",
  "maxLines": 0,
  "includeTweakeroo": true,
  "includeTweakerooDisableToggles": false,
  "includeTweakerooFixes": false,
  "includeAddonMods": true,
  "includeCustomProviders": true,
  "addonRequireHotkey": true,
  "extraAddonModIds": [],
  "hiddenFeatures": [],
  "hiddenMods": [],
  "visibleMods": [],
  "hideWithDebugScreen": true,
  "hideInScreens": true
}
```

| 字段 | 说明 |
| --- | --- |
| `enabled` | 悬浮列表总开关（也可用 F6 切换并保存） |
| `corner` | 锚定角落：`TOP_LEFT` / `TOP_RIGHT` / `BOTTOM_LEFT` / `BOTTOM_RIGHT`；右侧角落自动右对齐 |
| `offsetX` / `offsetY` | 距离锚定边缘的像素偏移 |
| `scale` | HUD 缩放（0.25–4.0，默认 1.0） |
| `showBackground` / `backgroundColor` / `backgroundPadding` | 背景框开关、颜色与内边距 |
| `lineSpacing` | 行间距（像素） |
| `showShadow` | 是否绘制文字阴影 |
| `showDisabled` | 是否显示未启用的功能（使用 `disabledColor`） |
| `enabledColor` / `disabledColor` | 启用/未启用的文字颜色（ARGB，默认绿色 `0xFF55FF55`） |
| `newlyEnabledColor` / `newlyDisabledColor` | 相对基准新开启/新关闭功能的颜色（ARGB，默认亮绿/红） |
| `baselineActive` / `baselineFeatures` | 变更基准是否激活、基准中记录的功能 ID（由 F7 自动维护，一般无需手改） |
| `diffOnlyChanged` | 基准激活时只显示相对基准发生变化的功能（默认 `true`，即未变化项不显示）；设为 `false` 则显示全部功能并仅高亮变化项 |
| `showProviderName` | 是否在每行前加 `[模组名]` 前缀 |
| `sortMode` | 排序：`CONFIG_ORDER`（默认，Tweakeroo 配置顺序）/ `ALPHABETICAL` / `MOD_THEN_ALPHABETICAL` |
| `maxLines` | 最多显示行数，`0` 表示不限制 |
| `includeTweakeroo` | 是否包含 Tweakeroo 本体的功能 |
| `includeTweakerooDisableToggles` | 是否包含 Tweakeroo “Disable” 分类的热键开关 |
| `includeTweakerooFixes` | 是否包含 Tweakeroo “Fixes” 分类中的布尔选项 |
| `includeAddonMods` | 是否自动发现其他附属模组的功能 |
| `includeCustomProviders` | 是否加载自定义 entrypoint provider |
| `addonRequireHotkey` | 自动发现时是否只保留带热键的开关（推荐开启，可过滤掉普通设置项） |
| `extraAddonModIds` | 额外强制扫描的模组 ID 列表（针对未声明 Tweakeroo 关联的模组） |
| `hiddenFeatures` | 永不显示的功能 ID 列表（可写 `tweakeroo:tweakFastBlockPlacement` 或 `tweakFastBlockPlacement`） |
| `hiddenMods` / `visibleMods` | 按模组 ID 黑名单 / 白名单（`visibleMods` 非空时只显示其中的模组） |
| `hideWithDebugScreen` / `hideInScreens` | F3 调试界面 / 任意 GUI 界面打开时隐藏 |

## 变更追踪 / Change tracking

按下 `F7`（可在“控制”中改键）会把当前所有已启用功能记录为基准。之后**只显示相对基准发生变化的功能**（仅显示功能名，无额外标注）：

- **新开启**的功能：使用 `newlyEnabledColor`（默认绿色）显示；
- **新关闭**的功能：使用 `newlyDisabledColor`（默认红色）显示，即使 `showDisabled` 为 `false` 也会显示；
- **未变化**的功能：默认不显示；
- 再按一次 `F7` 清除基准，恢复普通的已启用功能列表；
- 基准会写入配置文件，重启游戏后依然有效。

如果希望在基准激活时仍然显示全部功能、只把变化项高亮，可设置 `diffOnlyChanged: false`。
基准按功能 ID 记录，因此只影响当前仍然存在的功能；`hiddenFeatures`、`hiddenMods`、`visibleMods`
等过滤规则对变化行同样生效。

## 给附属模组开发者的扩展 API / Add-on API

### 方式一：自定义 Fabric entrypoint（推荐）

在自己的模组 `fabric.mod.json` 中声明：

```json
{
  "entrypoints": {
    "tweakeroo_overlay": [
      "com.example.myaddon.MyOverlayFeatureProvider"
    ]
  }
}
```

并实现 `io.github.tweakeroooverlay.api.FeatureProvider`（需要把本模组作为编译期依赖，或直接
按下面的签名实现接口）：

```java
public final class MyOverlayFeatureProvider implements FeatureProvider {
    @Override
    public String modId() {
        return "myaddon";
    }

    @Override
    public List<FeatureEntry> collectFeatures() {
        // 返回启用与未启用的条目都行，悬浮列表会根据配置过滤；
        // enabled 状态在每次刷新时重新读取，因此这里返回实时值即可。
        return List.of(
            FeatureEntry.of("myaddon:myToggle", "myaddon", "My Toggle", "Tweaks",
                MyConfigs.MY_TOGGLE.getBooleanValue())
        );
    }
}
```

`FeatureEntry` 的字段：`id`（全局唯一，建议 `"<modid>:<name>"`）、`modId`、`label`（HUD 显示
文本，已本地化）、`category`（可选分组）、`enabled`。自定义 provider 优先级最高，同 ID 的条目
会覆盖自动发现的条目。

### 方式二：依赖 MaLiLib 配置界面

如果附属模组通过 MaLiLib 注册了配置界面（`Registry.CONFIG_SCREEN.registerConfigScreenFactory`），
或按 Tweakeroo 风格把开关放在带 `ConfigBooleanHotkeyed` 静态字段的配置类中，本模组会自动发现，
**无需任何额外代码**。发现机制按以下顺序进行（结果会去重）：

1. MaLiLib 配置界面注册表（实现了 “All” 标签页的模组可获得完整列表）；
2. MaLiLib 的 `IConfigHandler`；
3. 模组 jar 中 `.config` 包下的配置类静态字段。

自动发现可以通过 `includeAddonMods`、`addonRequireHotkey`、`extraAddonModIds`、
`hiddenMods`、`visibleMods` 等配置项控制。

## 兼容性策略 / Compatibility

- **编译目标**：Minecraft 26.3（26.x 起游戏 jar 不再混淆，因此项目不使用 Yarn/Mojang mappings，
  也不做 remap），Fabric Loom 1.19.3，Java 25。
- **对 Tweakeroo / MaLiLib 的访问尽量通过反射 + 鸭子类型进行**：`FeatureToggle` 枚举、
  `Configs$Disable` / `Configs$Fixes` 列表、其他模组的配置对象都只在运行时按名字/方法探测；
  某个 API 缺失时对应的 provider 会被自动禁用并记录日志，而不是让游戏崩溃。
- **向上兼容**：依赖声明不对 Tweakeroo/MaLiLib 设置上限（`>=0.30.0-`），并配合“找不到就降级”
  的策略，理论上可用于后续 0.30.x/0.31.x；若未来大写重构导致渲染接口变化，请更新到对应版本。
- **向下兼容**：Tweakeroo 的 `FeatureToggle` 枚举结构自 1.21 起未变，0.29.x/0.28.x 也可工作
  （未逐一测试）；建议使用 0.30.x。
- 只读取功能状态，**不修改**任何 Tweakeroo/MaLiLib 配置，不与 Tweakeroo 争夺热键回调。

## 构建 / Building

需要 JDK 25 与网络（首次构建会下载 Minecraft、Fabric API 与 MaLiLib/Tweakeroo 编译依赖）：

```bash
./gradlew build        # Windows: gradlew.bat build
```

产物：`build/libs/tweakeroo-overlay-1.1.1.jar`（另外会生成 sources jar）。

其他常用任务：

```bash
./gradlew test         # 运行单元测试（Minecraft 无关的核心逻辑）
./gradlew runClient    # 启动开发环境客户端（需要已安装的 MaLiLib/Tweakeroo 由 Loom 自动放入运行时）
```

> 如果 Maven 下载较慢，可以给 Gradle 增加
> `-Dorg.gradle.internal.http.socketTimeout=120000 -Dorg.gradle.internal.http.connectionTimeout=120000`。

## 项目结构 / Project structure

```
src/main/java/io/github/tweakeroooverlay/
├── TweakerooOverlayClient.java        # Fabric 入口点
├── api/                               # 公开扩展 API（FeatureProvider 等）
├── client/                            # 入口初始化、HUD 渲染、按键、控制器
├── config/                            # JSON 配置数据类与读写
├── feature/                           # Tweakeroo / 附属模组功能发现（反射兼容层）
└── util/                              # 纯 Java 的过滤/排序/反射工具
src/main/resources/
├── fabric.mod.json
└── assets/tweakeroo_overlay/          # 语言文件与图标
src/test/java/                         # JUnit 5 单元测试
```

设计上把 **Minecraft 无关的核心逻辑**（`api`、`config`、`util`）与 **客户端/渲染相关代码**
（`client`、`feature` 中的 MaLiLib 交互）分开，核心逻辑由单元测试覆盖，渲染与模组发现层则尽量
使用“探测失败就禁用”的防御式写法。

## 已知限制 / Known limitations

- 仅客户端；不会同步到服务器，也不会出现在其他玩家的画面上。
- 自动发现依赖 MaLiLib 配置界面/配置类结构，未遵循这些结构的模组请使用自定义 entrypoint API。
- 通过 Mixin 直接注入 Tweakeroo 配置列表的附属模组会被当作 Tweakeroo 的功能显示（无法区分归属）。
- 修改配置文件后需要重启游戏；F6/F7 开关会立即生效并写回配置。
- 变更追踪按功能 ID 记录基准；模组更新导致功能改名/移除时，该功能会从对比结果中消失。
- 本模组是非官方附属模组，与 masa / sakura-ryoko、Tweakeroo 官方无关；"Tweakeroo" 名称仅用于说明用途。

## 许可证 / License

[MIT](LICENSE)
