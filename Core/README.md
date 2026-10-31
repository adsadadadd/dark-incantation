# Core

Плагин ядра сервера для Paper 1.21.11. Автор x1no4ekk; Java 21, Maven, MySQL через HikariCP.

## Сборка и установка
- Сборка: `mvn clean package` → `target/Core-1.0.0.jar`.
- MySQL обязателен: настройте секцию `database` в `plugins/Core/settings.yml`. Если подключиться не удалось, плагин пишет `SEVERE` и выключается.
- Необязательные зависимости: Vault (экономика «АРЫ» из плагина Economy, префиксы и группы), PlaceholderAPI.
- Сообщение «нет прав» для команд из plugin.yml задаётся в `config/paper-global.yml` (`messages.no-permission`).

## Архитектура
- **Модули** (`module/CoreModule`): у каждого функционального блока свои команды, слушатели, задачи и конфиг. `/core reload` работает по принципу «всё или ничего»: если хотя бы один файл неверный, остаются прежние настройки.
- **Данные игроков** хранятся только в MySQL. Кэш `User` есть только для онлайн-игроков: загрузка в `AsyncPlayerPreLoginEvent`, при выходе данные сохраняются асинхронно и удаляются из RAM.
- **Состояние модулей** (заявки /tpa, сессии /spectate, GPS, поиск RTP, сессии эндер-сундука, кулдауны) хранится по UUID и очищается при выходе игрока и при выключении плагина. Ссылок на `Player`/`Entity` в коллекциях нет.
- **GUI** построены на `InventoryHolder` (`Gui`, `PaginatedGui`): без глобальных списков и фоновых потоков.
- **Телепортация** идёт через `TeleportService`: задержка с боссбаром и частицами, отмена при движении, уроне или начале боя, запоминание точки для `/back`, неуязвимость после прибытия, `teleportAsync`.
- **Боевой режим** (`CombatManager`): метка PvP, запрет команд из списка, отключение полёта, наказание за выход в бою.
- **API для других плагинов**: `CoreAPI` (бой, телепортация, `/back`, ваниш, god, экономика) и `GuildTagProvider` (тег гильдии в чате). Оба регистрируются через `ServicesManager`.

## Таблицы MySQL
`core_users`, `core_homes`, `core_warps` (+ колонка `slot`), `core_warp_grades`, `core_server_locations` (спавн), `core_enderchests`, `core_enderchest_meta`, `core_stats`, `core_kit_cooldowns`, `core_ignores`. Для `/baltop` читается таблица плагина Economy `economy_accounts` (имя и колонки настраиваются).

## Конфигурация
- `settings.yml`: БД, пользователи, хомы и рог, `teleport`, `combat`, `gameplay`, `utility`, `travel`, `rtp`, `tops`, `enderchest`.
- `messages.yml`: все сообщения (MiniMessage).
- `warps.yml`: лимиты варпов, оценки и меню варпов.
- `kits.yml`: наборы, меню наборов.
- `killermoney.yml`: награды за убийство мобов и игроков.

## Хомы
`/home` отключена: телепортироваться домой можно только Рогом телепортации (`GOAT_HORN` с меткой в PDC). ПКМ открывает меню домов. Рецепт, внешний вид, звук и перезарядка настраиваются в `settings.yml`.

## Команды
| Команда | Алиасы | Право | Описание |
|---|---|---|---|
| `/core` |  | `core.admin` | Администрирование плагина Core |
| `/sethome` |  | `core.sethome` | Создать точку дома |
| `/delhome` |  | `core.delhome` | Удалить точку дома |
| `/playerhome` |  | `core.playerhome` | Посмотреть точки дома игрока |
| `/home` | /homes | `` | Подсказка: телепортация домой работает только через Рог телепортации |
| `/gamemode` | /gm | `core.gamemode` | Изменить режим игры |
| `/gms` |  | `core.gamemode` | Режим выживания |
| `/gmc` |  | `core.gamemode` | Творческий режим |
| `/gma` |  | `core.gamemode` | Режим приключения |
| `/gmsp` |  | `core.gamemode` | Режим наблюдателя |
| `/fly` |  | `core.fly` | Включить/выключить полёт |
| `/flyelytra` | /elytrafly | `core.flyelytra` | Полёт на элитрах |
| `/flyspeed` |  | `core.flyspeed` | Скорость полёта |
| `/speed` |  | `core.speed` | Скорость ходьбы (в полёте — скорость полёта) |
| `/god` |  | `core.god` | Режим бога |
| `/heal` |  | `core.heal` | Исцелить |
| `/feed` | /food | `core.feed` | Утолить голод |
| `/ext` |  | `core.ext` | Потушить огонь |
| `/burn` |  | `core.burn` | Поджечь игрока |
| `/milk` |  | `core.milk` | Снять все эффекты |
| `/kill` |  | `core.kill` | Убить игрока |
| `/suicide` |  | `core.suicide` | Покончить с собой |
| `/more` | /dupe | `core.more` | Пополнить стак в руке |
| `/hat` |  | `core.hat` | Надеть предмет из руки на голову |
| `/head` | /skull | `core.head` | Получить голову игрока |
| `/repair` | /fix | `core.repair` | Починить предметы |
| `/itemrename` | /rename | `core.itemrename` | Переименовать предмет в руке |
| `/itemdb` | /dura | `core.itemdb` | Информация о предмете в руке |
| `/enchant` |  | `core.enchant` | Зачаровать предмет в руке |
| `/give` |  | `core.give` | Выдать предмет |
| `/clearinventory` | /ci | `core.clearinventory` | Очистить инвентарь |
| `/firework` |  | `core.firework` | Запустить фейерверки |
| `/workbench` | /wb, /craft | `core.workbench` | Открыть верстак |
| `/anvil` |  | `core.anvil` | Открыть наковальню |
| `/carttable` | /cartographytable | `core.carttable` | Открыть стол картографа |
| `/etable` | /enchanttable, /enchantingtable | `core.etable` | Открыть стол зачаровывания |
| `/grindstone` |  | `core.grindstone` | Открыть точило |
| `/loom` |  | `core.loom` | Открыть ткацкий станок |
| `/smithtable` | /smithingtable | `core.smithtable` | Открыть кузнечный стол |
| `/stonecutter` |  | `core.stonecutter` | Открыть камнерез |
| `/invsee` |  | `core.invsee` | Посмотреть инвентарь игрока |
| `/vanish` | /v | `core.vanish` | Режим невидимости |
| `/spectate` | /spec | `core.spectate` | Слежка за игроком в режиме наблюдателя |
| `/spectatestop` | /specstop, /specoff | `core.spectatestop` | Закончить слежку за игроком |
| `/whois` |  | `core.whois` | Информация об игроке |
| `/realname` | /rn | `core.realname` | Узнать настоящий ник игрока |
| `/nick` | /nickname | `core.nick` | Установить ник |
| `/kick` |  | `core.kick` | Кикнуть игрока |
| `/gc` | /lag | `core.gc` | Состояние сервера: TPS, память, миры |
| `/ping` |  | `core.ping` | Пинг |
| `/list` | /online | `core.list` | Список игроков онлайн |
| `/near` |  | `core.near` | Игроки рядом |
| `/getpos` | /coords | `core.getpos` | Координаты |
| `/compass` |  | `core.compass` | Куда вы смотрите |
| `/gps` |  | `core.gps` | Навигация к точке или игроку |
| `/date` |  | `core.date` | Дата и время сервера |
| `/rules` | /rule | `core.rules` | Правила сервера |
| `/links` | /link | `core.links` | Ссылки сервера |
| `/day` |  | `core.day` | Установить день |
| `/night` |  | `core.night` | Установить ночь |
| `/time` |  | `core.time` | Время мира |
| `/sun` |  | `core.sun` | Ясная погода |
| `/rain` |  | `core.rain` | Дождь |
| `/storm` |  | `core.storm` | Гроза |
| `/weather` |  | `core.weather` | Погода мира |
| `/ptime` | /playertime | `core.ptime` | Личное время |
| `/pweather` | /playerweather | `core.pweather` | Личная погода |
| `/tp` |  | `core.tp` | Телепортироваться к игроку или телепортировать игрока к другому |
| `/tphere` |  | `core.tphere` | Телепортировать игрока к себе |
| `/tppos` |  | `core.tppos` | Телепортироваться по координатам |
| `/tpall` |  | `core.tpall` | Телепортировать всех игроков к себе |
| `/tpa` | /call | `core.tpa` | Запросить телепортацию к игроку |
| `/tpahere` |  | `core.tpahere` | Позвать игрока к себе |
| `/tpaccept` | /tpyes, /tpallow | `core.tpaccept` | Принять запрос на телепортацию |
| `/tpdeny` | /tpno | `core.tpdeny` | Отклонить запрос на телепортацию |
| `/tpcancel` | /tpacancel | `core.tpcancel` | Отменить свой запрос на телепортацию |
| `/teleportcancel` |  | `core.teleportcancel` | Отменить ожидание телепортации |
| `/tptoggle` |  | `core.tptoggle` | Включить или выключить телепортации к себе |
| `/back` |  | `core.back` | Вернуться на предыдущую позицию |
| `/top` |  | `core.top` | Подняться на самый верхний блок |
| `/setspawn` |  | `core.setspawn` | Установить точку спавна |
| `/spawn` |  | `core.spawn` | Телепортироваться на спавн |
| `/rtp` | /tpr, /randomtp | `core.rtp` | Случайная телепортация |
| `/warp` | /warps | `core.warp` | Меню варпов или телепортация на варп |
| `/setwarp` | /createwarp, /addwarp | `core.setwarp` | Создать варп |
| `/delwarp` | /deletewarp, /removewarp, /remwarp | `core.delwarp` | Удалить варп |
| `/warpinfo` |  | `core.warpinfo` | Информация о варпе |
| `/editwarp` |  | `core.editwarp` | Изменить описание, иконку или позицию своего варпа |
| `/serverwarp` | /systemwarp | `core.serverwarp` | Варпы сервера |
| `/killtop` | /topkills, /topkill | `core.killtop` | Топ игроков по убийствам |
| `/deathtop` | /topdeaths | `core.deathtop` | Топ игроков по смертям |
| `/baltop` | /balancetop, /topbal | `core.baltop` | Топ игроков по балансу |
| `/kit` | /kits | `core.kit` | Наборы предметов |
| `/enderchest` | /ec | `core.enderchest` | Эндер-сундук |

Полный список прав с значениями по умолчанию — в `src/main/resources/plugin.yml`.
