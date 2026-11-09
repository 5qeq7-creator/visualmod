# VisualMod для Minecraft 1.21.11 (Fabric)

Клиентский мод: небо, туман, вид мира (время суток и оттенок), геометрические частицы мира,
рука, цвет свечения зачарования, звук крита, оттенок брони по прочности, размер своей модели,
оптимизация FPS, AutoEat и автокликер.
Меню открывается клавишей **Right Shift**, вкладки: Visual, World, Particles, Combat, PvE, Player & FPS.

## ВАЖНО
- AutoEat и AutoClicker на многих серверах (в том числе ReallyWorld) считаются читами и могут привести к бану.
  Они выключены по умолчанию. Перед использованием прочитай правила сервера.
- Остальное (небо, туман, частицы, рука, глинт, размер модели) только визуальное и клиентское.

## Как собрать (нужен компьютер)
1. Установи JDK 21 и IntelliJ IDEA (Community подойдёт).
2. Открой https://fabricmc.net/develop/template, выбери Minecraft 1.21.11, Yarn, нажми Download и распакуй архив.
3. Из шаблона скопируй в папку проекта (где лежит этот README) только:
   `gradlew`, `gradlew.bat` и папку `gradle/wrapper`.
   Остальные файлы шаблона НЕ нужны.
4. Открой папку проекта в IntelliJ IDEA и дождись, пока Gradle всё загрузит.
5. В терминале IntelliJ запусти:
   - Windows: `gradlew.bat build`
   - Linux/Mac: `./gradlew build`
6. Готовый мод: `build/libs/visualmod-1.0.0.jar` (файл с `-sources` не нужен).

## Как установить
1. Установи Fabric Loader (0.18.x) для Minecraft 1.21.11.
2. Положи в папку `mods`: `visualmod-1.0.0.jar` и Fabric API (версия 0.141.x для 1.21.11).
3. Запусти игру, зайди в мир, нажми **Right Shift**.

## Новые вкладки
- **World**: World Time (день, утро, вечер, ночь или своё время 0-24000) и World Tint (свой цвет неба и тумана).
  Меняется только у тебя на экране, сервер время не меняется.
- **Particles**: World Particles. 15 фигур (10 объёмных, 2 вида лепестков, доллар, звезда, сердце) (точка, куб, тетраэдр, октаэдр, пирамида, линия, крест, кольцо, звезда, спираль),
  цвет (палитра или радуга), размер, скорость падения, радиус (до 20 блоков), максимум частиц, появление/исчезновение,
  скорость скольжения после приземления и время на земле. Нарисованы обычными цветными частицами пыли.
- **Combat**: Crit Sound (звук при критическом ударе: мяу, level up, XP, ping, allay, villager; громкость и высота)
  и Armor Tint (оттенок брони: тёмно-зелёный = целая, тёмно-красный = почти сломана, для своей и чужой брони).
- Звук "мяу" это ванильный звук кошки. Женский голос или свои звуки потребуют отдельных .ogg-файлов (ресурспак), в мод они не входят.

## Target HUD (вкладка HUD)
Плашка цели как на твоём фото: голова, ник, сердце с числом здоровья, предметы (правая рука, левая рука, шлем, нагрудник,
поножи, ботинки с полоской прочности) и цветная полоса здоровья (по умолчанию розовая, цвет меняется палитрой).
- Показывается, когда ты бьёшь игрока или наводишь на него прицел, и держится несколько секунд (настройка Hold).
- Здоровье показывается сразу точным числом (не уроном). Когда цель ест яблоко или лечится, число и полоса обновляются.
  Золотые сердца показываются отдельно золотым "+N".
- Открой чат на T: плашка покажется (на тебе как предпросмотр) и её можно перетащить мышкой. Позиция сохраняется.
- Здоровье чужих игроков берётся из данных, которые присылает сервер. Если ReallyWorld их скрывает, число может быть неточным.

## Вкладка Shaders
Это не настоящие шейдеры (GLSL), а клиентские эффекты погоды и яркости. Меняют только твою картинку.
- **Snow**: везде идёт ванильный снег (снежинки из обычного Майнкрафта), небо светло-голубое, туман белёсый и ближе. Intensity задаёт силу.
- **Rain**: везде идёт ванильный дождь, небо и туман серые, видимость ближе.
- **Bright**: яркость выше обычного максимума (ползунок Brightness). Контраст не регулируется.
- НЕ сделано: слои снега на блоках и мокрый блеск сверху. Это требует настоящих шейдеров или переписывания отрисовки блоков.
  Для этого можно поставить Iris и бесплатный шейдерпак (например, Complementary): у некоторых есть снег на блоках и мокрые блоки в дождь.
  Проверь, что версия под 1.21.11 есть, и прочитай правила сервера.

## Как пользоваться меню
- ЛКМ по модулю: включить или выключить.
- ПКМ по модулю: открыть настройки (слайдеры, палитра цвета).
- Настройки сохраняются при закрытии меню (`config/visualmod.properties`).

## Если не собирается или игра ругается
Версия 1.21.11 использует новую систему рендера, и несколько мест я не смог проверить на живой сборке.
Если появится ошибка, пришли мне текст ошибки целиком, и я исправлю. Самые вероятные места:

| Файл | Что может не совпасть |
|---|---|
| mixin/HandMixin | имя метода renderFirstPersonItem |
| mixin/GlintMixin | константа ItemRenderState.Glint.STANDARD |
| mixin/EntityCullMixin | сигнатура shouldRender |
| mixin/SkyMixin | тип возврата getSkyColor (должен быть int) |
| GlintRecolor | методы NativeImage (getColorArgb / setColorArgb) и конструктор NativeImageBackedTexture |
| VisualClient, ClickGuiScreen | mc.getWindow().getHandle() |
| mixin/TimeMixin | ClientWorld.Properties.getTimeOfDay (найден в Yarn 1.21.11, но влияет ли он на положение солнца, не проверено) |
| mixin/ArmorTintMixin | методы EquipmentRenderer.render и getDyeColor (найдены в Yarn, поведение не проверено) |
| WorldParticles (лепестки) | ParticleTypes.CHERRY_LEAVES, ParticleTypes.TINTED_LEAVES и TintedParticleEffect.create (найдены в Yarn), Particle.scale(float) |
| mixin/WeatherPrecipMixin | WeatherRendering.getPrecipitationAt (найден в Yarn 1.21.11) |
| mixin/RainGradientMixin | World.getRainGradient (не проверен, это самое вероятное место для правки) |
| mixin/GammaMixin | SimpleOption.getValue и options.getGamma (нужно проверить, что яркость читается именно так) |
| WorldParticles | mc.particleManager и Particle.setMaxAge, DustParticleEffect(int, float) (конструктор проверен) |
| TargetHud | PlayerListEntry.getSkinTextures, PlayerSkinDrawer.draw (найден в Yarn), mc.targetedEntity |
| VisualClient (HUD) | HudElementRegistry.attachElementBefore (есть в доке Fabric для 1.21.11), пакет net.fabricmc.fabric.api.client.rendering.v1.hud |
| CritSound | player.fallDistance, константы SoundEvents.ENTITY_* и AttackEntityCallback |

Каждый миксин отключается независимо: если один не применился, остальные функции продолжат работать
(в visualmod.mixins.json стоит defaultRequire = 0).

## Что проверено, а что нет
Проверено по документации Yarn 1.21.11: FogRenderer.applyFog, PlayerEntityRenderer.scale,
ItemRenderState.LayerRenderState.setGlint, ClientWorld.Properties.getTimeOfDay,
EquipmentRenderer.getDyeColor и render, DustParticleEffect(int, float), версии Fabric Loader/API/Yarn.
Весь Java-код проверен компилятором на синтаксис. Полная сборка и запуск игры мной не выполнялись.
