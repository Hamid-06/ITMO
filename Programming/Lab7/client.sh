#!/usr/bin/env bash
# Запуск на Helios во втором SSH-терминале: bash client.sh
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

client_jar=client/target/client.jar
[[ -f "$client_jar" ]] || client_jar=client.jar
if [[ ! -f "$client_jar" ]]; then
    printf 'Не найден client.jar. Сначала собери проект: mvn package\n' >&2
    exit 1
fi

exec java -jar "$client_jar" 127.0.0.1 18080
