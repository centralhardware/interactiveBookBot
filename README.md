![build status](https://github.com/centralhardware/interactiveBookBot/actions/workflows/maven.yml/badge.svg)

Телеграмм бот позволяющий проходить книги-игры в удобном телеграмм-формате
[@InteractiveBookBot](https://t.me/InteractiveBookBot)
> По причинам того, что авторские права на книги принадлежать издателям публичный репозиторий их не содержит

# Стек:
- java 20
- spring boot 
- redis
- clickhouse

## clickhouse
Логирования всех действий для построения дешборда в grafana
## redis 
Хранение: 
- прочитанных частях
- открытых концовках
- выборах пользователей 
- выборах пользователя в последнем прохождение
- кеш времени чтения для текста