#!/bin/sh
# Usage (from anywhere):  hana-jdbc/run.sh build | smoke | sql "SELECT ..." | test [mvn args]
# Credentials come from the git-ignored .env at the repo root (template: .env.example).
set -eu
here="$(cd "$(dirname "$0")" && pwd)"
root="$(cd "$here/../../.." && pwd)"
image=localhost/bcs-hana-jdbc:latest
jar="$HOME/.m2/repository/com/sap/cloud/db/jdbc/ngdbc/2.8.12/ngdbc-2.8.12.jar"
cmd="${1:-smoke}"; [ $# -gt 0 ] && shift || true
if [ "$cmd" = build ]; then
  cp "$jar" "$here/lib/ngdbc.jar"
  exec podman build -t "$image" -f "$here/Containerfile" "$here"
fi
[ -f "$root/.env" ] || { echo "Missing $root/.env (copy .env.example and fill it in)" >&2; exit 2; }
exec podman run --rm --env-file "$root/.env" \
  -v "$root/modernized/otr-sva-optimus-corredores:/work:ro" -v "$HOME/.m2:/root/.m2" \
  "$image" "$cmd" "$@"
