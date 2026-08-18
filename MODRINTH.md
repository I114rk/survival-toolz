# Survival ToolZ

A small Fabric mod with tools for handling mobs in survival. Teleport villagers from far away, pin
them where you want them, spawn new ones from a villager heart, and carry any mob around inside a
heart with everything it owns.

Every item lives in the **Tools & Utilities** creative tab.

---

## Villager Teleporter

Hold it and look at a block — even one far out of reach — and flame particles mark the spot.

- **Right click**: the nearest villager **without** the `isTeleported` tag is teleported to the
  marked block, then gets the tag so it cannot be moved again.
- **Right click a tagged villager**: the tag is removed and that villager can be teleported again.
- Aims up to **128 blocks** away and looks for villagers within **640 blocks**.
- **500 durability**, one point per teleport.

```
[ ] [E] [ ]
[E] [P] [E]      E = Emerald
[ ] [E] [ ]      P = Ender Pearl
```

## Pinning villagers

**Shift + right click** a villager to pin it: it stops walking but still turns its head and body,
keeps trading, and stays pinned across saves. Shift + right click again to let it go.

No item needed.

## Villager Spawner

Right click a block to place a new villager there.

- **32 uses**, one per villager.
- **64 second cooldown** between villagers.

```
[ ] [E] [ ]
[E] [H] [E]      E = Emerald
[ ] [E] [ ]      H = Villager Heart
```

### Villager Heart

The crafting ingredient for the spawner.

```
[E] [P]          E = Emerald
[B] [E]          P = Potato
                 B = Bread
```

## Soul collector

### Empty Heart

```
[L] [L]          L = Leather
[L] [L]
```

**Right click any mob** with it and the mob is stored inside: the empty heart becomes a **Full
Heart** that names what it holds, for example *Full Heart (Sheep)*.

### Full Heart

- Stores **one** mob, with everything about it: a pink sheep stays pink, a zombie keeps its diamond
  armour, a tamed wolf keeps its owner, and health, age and effects come back untouched.
- **Right click a block** to release the mob there. The heart becomes empty again.
- Renaming the heart in an anvil renames the mob that comes out of it, and a mob that already had a
  name keeps it on the item.

## Jade integration (optional)

If [Jade](https://modrinth.com/mod/jade) is installed, looking at a chest that holds a full heart
shows the **spawn egg of the stored mob** right next to the heart, so you can tell your storage
apart without opening it. Nothing else about Jade's tooltip changes, and the mod works exactly the
same without Jade.

## Compatibility

- Minecraft **26.2** and **26.1.x**, Fabric only
- **Fabric API** is required, Java 25 or newer
- Install it on both the client and the server: the client draws the rotating aiming marker,
  everything else happens server side

Released under the MIT license.

---
---

# Survival ToolZ (русский)

Небольшой мод для Fabric с инструментами для работы с мобами в выживании. Телепортируйте жителей
издалека, фиксируйте их там, где нужно, создавайте новых из сердца жителя и носите любого моба в
сердце вместе со всем, что у него есть.

Все предметы лежат в креативной вкладке **Инструменты**.

---

## Телепортатор жителей

Возьмите его в руки и посмотрите на блок — даже далеко за пределами досягаемости — и над блоком
появятся частицы пламени.

- **ПКМ**: ближайший житель **без** тега `isTeleported` телепортируется на отмеченный блок и
  получает этот тег, чтобы его нельзя было переместить повторно.
- **ПКМ по жителю с тегом**: тег снимается, и этого жителя снова можно телепортировать.
- Прицеливание работает на **128 блоков**, жители ищутся в радиусе **640 блоков**.
- **500 прочности**, по одной единице за телепортацию.

```
[ ] [E] [ ]
[E] [P] [E]      E = изумруд
[ ] [E] [ ]      P = жемчуг Края
```

## Фиксация жителей

**Шифт + ПКМ** по жителю — житель зафиксирован: он больше не ходит, но продолжает поворачиваться и
торговать, и остаётся зафиксированным после перезахода. Шифт + ПКМ ещё раз — и он свободен.

Предмет для этого не нужен.

## Спавнер жителей

ПКМ по блоку — на этом месте появляется новый житель.

- **32 использования**, по одному на жителя.
- **Перезарядка 64 секунды** между жителями.

```
[ ] [E] [ ]
[E] [H] [E]      E = изумруд
[ ] [E] [ ]      H = сердце жителя
```

### Сердце жителя

Ингредиент для спавнера.

```
[E] [P]          E = изумруд
[B] [E]          P = картофель
                 B = хлеб
```

## Сборщик душ

### Пустое сердце

```
[L] [L]          L = кожа
[L] [L]
```

**ПКМ по любому мобу** — моб оказывается внутри: пустое сердце превращается в **полное сердце**,
которое подписано тем, кто в нём лежит, например *Полное сердце (Овца)*.

### Полное сердце

- Хранит **одного** моба со всем, что у него есть: розовая овца останется розовой, зомби сохранит
  алмазную броню, прирученный волк — хозяина, а здоровье, возраст и эффекты вернутся без изменений.
- **ПКМ по блоку** — моб выходит на это место, а сердце снова становится пустым.
- Переименование сердца в наковальне переименовывает моба, который из него выйдет, а если моб уже
  был назван, имя остаётся и на предмете.

## Интеграция с Jade (необязательная)

Если установлен [Jade](https://modrinth.com/mod/jade), при взгляде на сундук с полным сердцем рядом
с ним показывается **яйцо призыва хранящегося моба** — так можно разобраться в хранилище, не открывая
его. Больше ничего в подсказке Jade не меняется, а без Jade мод работает точно так же.

## Совместимость

- Minecraft **26.2** и **26.1.x**, только Fabric
- Нужен **Fabric API**, Java 25 или новее
- Ставится и на клиент, и на сервер: клиент рисует вращающуюся метку прицеливания, всё остальное
  происходит на сервере

Мод распространяется под лицензией MIT.
