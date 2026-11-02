#!/bin/sh
# Host side.  oracle.sh legacy|new <inputs.tsv> <outDir>   (paths relative to the repo root, under analysis/)
# SELECT only.  Credentials from the git-ignored .env, as run.sh.
set -eu
here="$(cd "$(dirname "$0")" && pwd)"; root="$(cd "$here/../../../.." && pwd)"
side="$1"; inp="$2"; out="$3"
image=localhost/bcs-hana-jdbc:latest
mkdir -p "$root/$out"
common="--rm --env-file $root/.env -v $root/modernized/otr-sva-optimus-corredores:/work:ro -v $HOME/.m2:/root/.m2 -v $root/analysis/otr-sva-optimus-corredores:/an -v $root/legacy/otr-sva-optimus-corredores:/legacy:ro"
case "$side" in
  legacy) exec podman run $common --entrypoint sh "$image" /work/hana-jdbc/oracle/oracle-legacy.sh "/an/${inp#analysis/otr-sva-optimus-corredores/}" "/an/${out#analysis/otr-sva-optimus-corredores/}" ;;
  new)    exec podman run $common -e "ORACLE_IN=/an/${inp#analysis/otr-sva-optimus-corredores/}" -e "ORACLE_OUT=/an/${out#analysis/otr-sva-optimus-corredores/}" "$image" test -DexcludedGroups= -Dtest=SuraHanaEquivalenciaTest ;;
  *) echo "usage: oracle.sh legacy|new <inputs.tsv> <outDir>" >&2; exit 2 ;;
esac
