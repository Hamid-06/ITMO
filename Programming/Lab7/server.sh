#!/usr/bin/env bash
# Запуск на Helios: bash server.sh
# Смена сохранённого пароля: bash server.sh --password
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

if [[ $# -gt 1 || (${1:-} != "" && ${1:-} != "--password") ]]; then
    printf 'Использование: bash server.sh [--password]\n' >&2
    exit 2
fi

server_jar=server/target/server.jar
[[ -f "$server_jar" ]] || server_jar=server.jar
if [[ ! -f "$server_jar" ]]; then
    printf 'Не найден server.jar. Сначала собери проект: mvn package\n' >&2
    exit 1
fi

# На Helios подключаемся напрямую к кафедральной PostgreSQL.
export DB_URL='jdbc:postgresql://pg:5432/studs?currentSchema=s505412'
export DB_USER='s505412'
password_file=.helios-db-password
save_password=false

if [[ ${1:-} == "--password" || ! -f "$password_file" ]]; then
    printf 'Пароль PostgreSQL для %s: ' "$DB_USER"
    if ! IFS= read -r -s DB_PASSWORD; then
        printf '\nВвод пароля прерван.\n' >&2
        exit 1
    fi
    printf '\n'
    save_password=true
else
    if ! IFS= read -r DB_PASSWORD < "$password_file"; then
        printf 'Не удалось прочитать пароль. Выполни: bash server.sh --password\n' >&2
        exit 1
    fi
fi
export DB_PASSWORD

# Явные свойства JVM также перекрывают старый URL туннеля в db.local.properties.
java_options=("-Ddb.url=$DB_URL" "-Ddb.user=$DB_USER")
if ! java "${java_options[@]}" -jar "$server_jar" --check-db; then
    printf 'Подключение не удалось. Для повторного ввода пароля: bash server.sh --password\n' >&2
    exit 1
fi

if [[ "$save_password" == true ]]; then
    # Сохраняем пароль только после успешной проверки подключения.
    (umask 077; printf '%s\n' "$DB_PASSWORD" > "$password_file")
fi
chmod 600 "$password_file"

exec java "${java_options[@]}" -jar "$server_jar" 18080 127.0.0.1
