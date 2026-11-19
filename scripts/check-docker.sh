#!/bin/sh
set -eu

if ! command -v docker >/dev/null 2>&1; then
    printf '%s\n' 'Docker no está instalado o no se encuentra en PATH.' >&2
    exit 1
fi

if ! docker info --format '{{.ServerVersion}}' >/dev/null 2>&1; then
    printf '%s\n' 'Docker está instalado, pero no se pudo conectar al daemon. Comprobá que esté iniciado y que tu usuario tenga acceso.' >&2
    exit 1
fi
