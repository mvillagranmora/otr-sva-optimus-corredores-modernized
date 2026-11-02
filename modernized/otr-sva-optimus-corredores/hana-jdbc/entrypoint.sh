#!/bin/sh
# smoke (default) | sql "<SELECT ...>" | test [mvn args]   (the module is mounted at /work/BcsSURA by run.sh)
set -eu
cmd="${1:-smoke}"; [ $# -gt 0 ] && shift || true
case "$cmd" in
  smoke) exec java -cp /opt/hana/lib/ngdbc.jar /opt/hana/HanaSmoke.java ;;
  sql)   exec java -cp /opt/hana/lib/ngdbc.jar /opt/hana/HanaSmoke.java "$@" ;;
  test)
    # the app reads HANA_URL / HANA_USER / HANA_PASSWORD; the dbprop (k=v;k=v) becomes the URL query, as the ngdbc driver expects (e.g. currentschema=BSCL)
    base="$(printenv 'qa01.saphana.dbURL')"; extra="$(printenv 'qa01.saphana.dbprop' || true)"
    q="$(printf '%s' "$extra" | tr ';' '&')"
    case "$base" in *\?) sep="" ;; *\?*) sep="&" ;; *) sep="?" ;; esac
    export HANA_URL="$base${q:+$sep$q}" HANA_USER="$(printenv 'qa01.saphana.dbuser')" HANA_PASSWORD="$(printenv 'qa01.saphana.dbpass')"
    # the module is mounted read-only: build from a copy
    mkdir -p /tmp/w && cp /work/pom.xml /tmp/w/ && (cd /work && tar cf - --exclude=target BcsSURA) | tar xf - -C /tmp/w
    cd /tmp/w/BcsSURA && exec mvn -o -Dmaven.test.failure.ignore=true "$@" test ;;
  *) echo "usage: smoke | sql \"SELECT ...\" | test [mvn args]" >&2; exit 2 ;;
esac
