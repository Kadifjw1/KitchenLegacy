# Плейтест NPC-кузнеца ForgeMind

Эта ветка не изменяет `main`. GitHub Actions распаковывает проверенный overlay ForgeMind v0.4.5, собирает Worldsmith и публикует готовый JAR.

## Как получить JAR

1. Откройте pull request плейтеста.
2. Дождитесь зелёной проверки `Build ForgeMind Blacksmith Playtest`.
3. Откройте завершённый workflow run.
4. В разделе **Artifacts** скачайте `worldsmith-forgemind-blacksmith-playtest`.
5. Распакуйте архив и положите JAR в папку `mods`, заменив предыдущую тестовую сборку Worldsmith.

## Создание кузнеца

```mcfunction
/summon minecraft:villager ~ ~ ~ {CustomName:'{"text":"Тестовый кузнец","color":"gold"}',CustomNameVisible:1b,NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,VillagerData:{profession:"minecraft:armorer",level:5,type:"minecraft:plains"},ForgeData:{worldsmith_role:"blacksmith"}}
```

Выдать ресурсы:

```mcfunction
/give @s minecraft:emerald 64
/give @s minecraft:iron_ingot 64
/give @s minecraft:stick 64
```

Нажмите по кузнецу правой кнопкой основной рукой.

## Тестовый рецепт

- 2 железных слитка;
- 1 палка;
- 12 изумрудов;
- 200 тиков, примерно 10 секунд;
- результат — железный меч.

## Проверить

1. Обычный житель не открывает окно кузнеца.
2. Назначенный кузнец открывает GUI.
3. Недостаток ресурсов не вызывает частичного списания.
4. Успешный заказ списывает материалы один раз.
5. Статусы: `QUEUED → IN_PROGRESS → READY`.
6. Получение выдаёт предмет только один раз.
7. Отмена второго ожидающего заказа возвращает материалы и изумруды.
8. Количество 2 даёт два меча и удваивает стоимость, материалы и время.
9. Полный инвентарь выбрасывает результат рядом с игроком.
10. После перезапуска мира очередь сохраняется.
11. Другой игрок не видит и не получает чужие заказы.
12. После отхода дальше восьми блоков действия отклоняются.

При ошибке сохраните `logs/latest.log`, скриншот GUI и номер шага.
