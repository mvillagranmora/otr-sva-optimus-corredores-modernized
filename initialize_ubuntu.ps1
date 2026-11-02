<#
.SYNOPSIS
  Prepara la instancia Ubuntu de WSL2 para compilar y probar el proyecto fuera de Windows.
  Se ejecuta DESDE WINDOWS (PowerShell). No toca ninguna otra distribucion (p. ej. podman-machine-default) ni el legacy.

.DESCRIPTION
  Que hace (por pasos; cada uno es idempotente: se puede repetir):
    1. Comprueba WSL2 y que exista la distribucion (por defecto "Ubuntu"); se niega a usar podman-machine-default.
    2. Como root (wsl -u root, sin pedir contrasena): apt-get con git, rsync, curl, unzip, zip, jq, dos2unix, python3-venv, etc.
    3. /etc/wsl.conf: appendWindowsPath=false para que el PATH de Windows NO se cuele (hoy "mvn" de Ubuntu es el de Windows). Reinicia SOLO esa distribucion.
    4. Como usuario normal, sin sudo: JDK Temurin 25 (Adoptium, con SHA-256) en ~/tools/jdk25 y Maven 3.10.0 (copia de C:\Devops o descarga) en ~/tools/maven,
       y un bloque en ~/.bashrc (JAVA_HOME, MAVEN_HOME, PATH, LANG=C.UTF-8).
    5. Crea ~/work (ext4: compilar sobre /mnt/c es varias veces mas lento).
  Modos opcionales:
    -Verificar        solo informa lo que hay y lo que falta (no cambia nada).
    -Sincronizar      copia el proyecto Windows -> ~/work/nuam-modernizado (rsync; excluye .git, target, .env, temporales).
    -TraerResultados  lista (dry-run) lo que WSL genero y es mas nuevo que lo de Windows en analysis/; con -Aplicar lo copia a Windows (sin borrar nada).
    -Prueba           tras instalar y sincronizar, compila e instala BcsUtils y prueba BcsCustodiaServicios en Ubuntu (humo de extremo a extremo).

  Flujo recomendado: el repositorio y los commits se quedan en Windows; Ubuntu compila y corre los oraculos sobre la copia de ~/work.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\initialize_ubuntu.ps1 -Verificar
  powershell -ExecutionPolicy Bypass -File .\initialize_ubuntu.ps1
  powershell -ExecutionPolicy Bypass -File .\initialize_ubuntu.ps1 -Sincronizar -Prueba
#>
[CmdletBinding()]
param(
    [string]$Distro = 'Ubuntu',
    [string]$JavaMajor = '25',
    [string]$MavenVersion = '3.10.0',
    [string]$MavenWindows = 'C:\Devops\apache-maven-3.10.0',
    [switch]$Verificar,
    [switch]$Sincronizar,
    [switch]$TraerResultados,
    [switch]$Aplicar,
    [switch]$Prueba,
    [switch]$NoReiniciar
)

$ErrorActionPreference = 'Stop'
$Raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
$Tmp = Join-Path $env:TEMP 'initialize_ubuntu'
New-Item -ItemType Directory -Force -Path $Tmp | Out-Null

function Info($m)  { Write-Host "[init-ubuntu] $m" -ForegroundColor Cyan }
function Aviso($m) { Write-Host "[init-ubuntu] AVISO: $m" -ForegroundColor Yellow }
function Falla($m) { Write-Host "[init-ubuntu] ERROR: $m" -ForegroundColor Red; exit 1 }

# ---------- utilidades ----------
function Get-WslPath([string]$ruta) {
    $r = & wsl.exe -d $Distro -- wslpath -a ($ruta -replace '\\', '/') 2>$null
    return ($r | Select-Object -First 1).Trim()
}

# Escribe un script bash con saltos de linea LF y lo ejecuta en la distribucion (como root o como el usuario normal).
function Invoke-Bash([string]$nombre, [string]$contenido, [switch]$Root) {
    $archivo = Join-Path $Tmp "$nombre.sh"
    $lf = ($contenido -replace "`r`n", "`n")
    [System.IO.File]::WriteAllText($archivo, $lf, (New-Object System.Text.UTF8Encoding($false)))
    $ruta = Get-WslPath $archivo
    if ($Root) { & wsl.exe -d $Distro -u root -- bash $ruta } else { & wsl.exe -d $Distro -- bash $ruta }
    if ($LASTEXITCODE -ne 0) { Falla "el paso '$nombre' termino con codigo $LASTEXITCODE" }
}

# ---------- 1. WSL y la distribucion ----------
if (-not (Get-Command wsl.exe -ErrorAction SilentlyContinue)) { Falla 'wsl.exe no existe: instala WSL2 (wsl --install).' }
if ($Distro -match 'podman') { Falla "No se usa '$Distro' (es la distribucion de Podman). Indica otra con -Distro." }

$prev = [Console]::OutputEncoding
[Console]::OutputEncoding = [System.Text.Encoding]::Unicode
$lista = (& wsl.exe -l -v) | ForEach-Object { $_.ToString().Trim([char]0xFEFF) } | Where-Object { $_ -match '\S' }
[Console]::OutputEncoding = $prev
$linea = $lista | Where-Object { $_ -match "(^|\s|\*)$([regex]::Escape($Distro))\s" } | Select-Object -First 1
if (-not $linea) { Falla "No existe la distribucion '$Distro'. Las que hay:`n$($lista -join "`n")" }
if ($linea -notmatch '\s2\s*$') { Falla "La distribucion '$Distro' no es WSL2: $linea" }
Info "Distribucion: $linea"

$wslcfg = Join-Path $env:USERPROFILE '.wslconfig'
if (Test-Path $wslcfg) {
    if ((Get-Content $wslcfg -Raw) -match 'networkingMode\s*=\s*mirrored') { Info '.wslconfig: networkingMode=mirrored (la VPN de HANA QA se ve desde WSL).' }
    else { Aviso '.wslconfig sin networkingMode=mirrored: con NAT la VPN corporativa puede no verse desde WSL.' }
} else { Aviso 'No hay .wslconfig (WSL2 en NAT): la VPN corporativa puede no verse desde WSL; crea [wsl2] networkingMode=mirrored.' }

# ---------- Verificar ----------
$bashVerificar = @'
#!/bin/bash
echo "--- sistema";   . /etc/os-release; echo "$PRETTY_NAME | kernel $(uname -r) | usuario $(whoami)"
echo "--- recursos";  echo "CPU $(nproc) | RAM $(free -g | awk '/Mem:/{print $2}') GB | disco libre $(df -h / | awk 'NR==2{print $4}')"
echo "--- /etc/wsl.conf (appendWindowsPath)"; grep -i appendWindowsPath /etc/wsl.conf 2>/dev/null || echo "(no definido: el PATH de Windows se cuela)"
echo "--- herramientas (PATH actual)"
for t in git rsync curl unzip zip jq dos2unix python3 pip3; do p=$(command -v "$t" 2>/dev/null); printf '%-9s %s\n' "$t" "${p:-NO INSTALADO}"; done
echo "--- JDK y Maven de ~/tools"
if [ -x "$HOME/tools/jdk25/bin/java" ]; then "$HOME/tools/jdk25/bin/java" -version 2>&1 | head -1; else echo "JDK 25: NO INSTALADO (~/tools/jdk25)"; fi
if [ -x "$HOME/tools/maven/bin/mvn" ]; then JAVA_HOME="$HOME/tools/jdk25" "$HOME/tools/maven/bin/mvn" -v 2>/dev/null | head -1; else echo "Maven: NO INSTALADO (~/tools/maven)"; fi
echo "--- 'mvn' y 'java' que ve un shell nuevo"
bash -lc 'echo "mvn  -> $(command -v mvn || echo no)"; echo "java -> $(command -v java || echo no)"'
echo "--- ~/work"; ls -d "$HOME/work/nuam-modernizado" 2>/dev/null || echo "(sin copia del proyecto: usa -Sincronizar)"
echo "--- red"; for u in https://github.com https://repo.maven.apache.org/maven2/ https://api.adoptium.net; do curl -s -m 8 -o /dev/null -w "$u HTTP %{http_code}\n" "$u"; done
'@
if ($Verificar) { Invoke-Bash 'verificar' $bashVerificar; exit 0 }

# ---------- 2 y 3. Paquetes y wsl.conf (root) ----------
if (-not $TraerResultados) {
$bashRoot = @'
#!/bin/bash
set -e
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y --no-install-recommends ca-certificates curl wget git rsync unzip zip tar jq dos2unix file procps tzdata locales python3 python3-venv python3-pip
# El PATH de Windows no debe colarse en Ubuntu (hoy mvn resuelve a /mnt/c/Devops/...)
if grep -q '^\[interop\]' /etc/wsl.conf 2>/dev/null; then
  grep -q 'appendWindowsPath' /etc/wsl.conf || sed -i '/^\[interop\]/a appendWindowsPath=false' /etc/wsl.conf
else
  printf '\n[interop]\nappendWindowsPath=false\n' >> /etc/wsl.conf
fi
echo "wsl.conf:"; cat /etc/wsl.conf
'@
Info 'Paquetes del sistema y /etc/wsl.conf (como root)...'
Invoke-Bash 'root' $bashRoot -Root

# ---------- 4. JDK + Maven (usuario, sin sudo) ----------
$mvnLinux = if (Test-Path $MavenWindows) { Get-WslPath $MavenWindows } else { '' }
$bashUsuario = @"
#!/bin/bash
set -e
JAVA_MAJOR='$JavaMajor'
MAVEN_VERSION='$MavenVersion'
MAVEN_WIN_COPIA='$mvnLinux'
mkdir -p "`$HOME/tools" "`$HOME/work"
cd "`$HOME/tools"

if [ ! -x "`$HOME/tools/jdk25/bin/java" ]; then
  echo "== JDK Temurin `$JAVA_MAJOR"
  meta=`$(curl -fsSL "https://api.adoptium.net/v3/assets/latest/`$JAVA_MAJOR/hotspot?architecture=x64&image_type=jdk&os=linux&vendor=eclipse")
  link=`$(echo "`$meta" | jq -r '.[0].binary.package.link')
  sum=`$(echo "`$meta"  | jq -r '.[0].binary.package.checksum')
  [ -n "`$link" ] && [ "`$link" != null ] || { echo "no se pudo resolver el JDK en Adoptium"; exit 1; }
  curl -fSL -o jdk.tar.gz "`$link"
  echo "`$sum  jdk.tar.gz" | sha256sum -c -
  rm -rf jdk25 jdk-tmp && mkdir jdk-tmp && tar -xzf jdk.tar.gz -C jdk-tmp && mv jdk-tmp/* jdk25 && rm -rf jdk-tmp jdk.tar.gz
else echo "== JDK ya instalado"; fi

if [ ! -x "`$HOME/tools/maven/bin/mvn" ]; then
  echo "== Maven `$MAVEN_VERSION"
  if [ -n "`$MAVEN_WIN_COPIA" ] && [ -d "`$MAVEN_WIN_COPIA/bin" ]; then
    rm -rf maven && rsync -a "`$MAVEN_WIN_COPIA/" maven/ && dos2unix -q maven/bin/mvn maven/bin/mvnDebug 2>/dev/null || true
  else
    for base in https://dlcdn.apache.org https://archive.apache.org/dist; do
      if curl -fSL -o maven.tgz "`$base/maven/maven-3/`$MAVEN_VERSION/binaries/apache-maven-`$MAVEN_VERSION-bin.tar.gz"; then break; fi
    done
    [ -s maven.tgz ] || { echo "no se pudo bajar Maven `$MAVEN_VERSION"; exit 1; }
    rm -rf maven maven-tmp && mkdir maven-tmp && tar -xzf maven.tgz -C maven-tmp && mv maven-tmp/* maven && rm -rf maven-tmp maven.tgz
  fi
  chmod +x maven/bin/mvn
else echo "== Maven ya instalado"; fi

# Bloque idempotente en ~/.profile Y en ~/.bashrc: el .bashrc de Ubuntu corta al principio en shells no interactivos
# (por ejemplo `wsl -d Ubuntu -- bash -lc "mvn ..."`), asi que el bloque tambien va en ~/.profile, que leen los shells de login.
marca_ini='# >>> initialize_ubuntu >>>'; marca_fin='# <<< initialize_ubuntu <<<'
for rc in "`$HOME/.profile" "`$HOME/.bashrc"; do
  touch "`$rc"
  sed -i "/`$marca_ini/,/`$marca_fin/d" "`$rc"
  cat >> "`$rc" <<'BLOQUE'
# >>> initialize_ubuntu >>>
export JAVA_HOME="`$HOME/tools/jdk25"
export MAVEN_HOME="`$HOME/tools/maven"
export PATH="`$JAVA_HOME/bin:`$MAVEN_HOME/bin:`$PATH"
export LANG=C.UTF-8
export MAVEN_OPTS="-Dfile.encoding=UTF-8"
# <<< initialize_ubuntu <<<
BLOQUE
done
export JAVA_HOME="`$HOME/tools/jdk25" PATH="`$HOME/tools/jdk25/bin:`$HOME/tools/maven/bin:`$PATH"
java -version 2>&1 | head -2
mvn -v 2>&1 | head -3
"@
Info 'JDK y Maven en ~/tools (sin sudo)...'
Invoke-Bash 'usuario' $bashUsuario

if (-not $NoReiniciar) {
    Info "Reiniciando SOLO la distribucion '$Distro' para aplicar wsl.conf..."
    & wsl.exe --terminate $Distro | Out-Null
    Start-Sleep -Seconds 3
    & wsl.exe -d $Distro -- bash -lc 'echo "mvn  -> $(command -v mvn)"; echo "java -> $(command -v java)"; echo "PATH tiene /mnt/c: $(echo $PATH | grep -c /mnt/c)"'
} else { Aviso 'Sin reiniciar: ejecuta "wsl --terminate Ubuntu" para que wsl.conf tenga efecto.' }
}

# ---------- Sincronizar Windows -> ~/work ----------
if ($Sincronizar) {
    $origen = (Get-WslPath $Raiz) + '/'
    $bashSync = @"
#!/bin/bash
set -e
mkdir -p "`$HOME/work/nuam-modernizado"
echo "rsync $origen -> ~/work/nuam-modernizado (ext4)"
rsync -a --delete --info=stats1 \
  --exclude='.git/' --exclude='**/target/' --exclude='.env' --exclude='.env.*' --exclude='.claude/settings.local.json' \
  --exclude='analysis/**/verify*/tmp/' --exclude='**/__pycache__/' \
  '$origen' "`$HOME/work/nuam-modernizado/"
find "`$HOME/work/nuam-modernizado" -name '*.sh' -not -path '*/legacy/*' -exec chmod +x {} + 2>/dev/null || true
echo "copia lista: `$(du -sh "`$HOME/work/nuam-modernizado" | cut -f1)"
"@
    Info 'Sincronizando el proyecto a ~/work (puede tardar varios minutos)...'
    Invoke-Bash 'sincronizar' $bashSync
}

# ---------- Traer resultados de WSL a Windows (solo analysis/, sin borrar) ----------
if ($TraerResultados) {
    $destino = (Get-WslPath $Raiz) + '/'
    $flag = if ($Aplicar) { '' } else { '--dry-run' }
    $bashBack = @"
#!/bin/bash
set -e
cd "`$HOME/work/nuam-modernizado"
echo "analysis/: lo de WSL mas nuevo que lo de Windows (`$([ -z '$flag' ] && echo 'SE COPIA' || echo 'dry-run: no se copia nada'))"
rsync -a --update $flag --itemize-changes \
  --exclude='verify*/tmp/' --exclude='**/__pycache__/' \
  analysis/ '${destino}analysis/' | grep -v '^\.' | head -200
"@
    Invoke-Bash 'traer' $bashBack
    if (-not $Aplicar) { Info 'Eso fue una simulacion. Repite con -TraerResultados -Aplicar para copiar.' }
}

# ---------- Prueba de humo ----------
if ($Prueba) {
    $bashPrueba = @'
#!/bin/bash
set -e
source "$HOME/.bashrc" 2>/dev/null || true
export JAVA_HOME="$HOME/tools/jdk25" PATH="$HOME/tools/jdk25/bin:$HOME/tools/maven/bin:$PATH"
cd "$HOME/work/nuam-modernizado/modernized/otr-sva-optimus-corredores" || { echo "falta ~/work: usa -Sincronizar"; exit 1; }
echo "== instala BcsUtils (sin pruebas) y prueba BcsCustodiaServicios en Ubuntu"
mvn -B -ntp -q -pl BcsUtils -am install -DskipTests
cd BcsCustodiaServicios
mvn -B -ntp test -Dmaven.test.failure.ignore=true 2>&1 | tee "$HOME/work/humo_custodia.log" | grep -E "Tests run:|BUILD|ERROR" | tail -6
echo "log: ~/work/humo_custodia.log  (en Windows: \\wsl.localhost\Ubuntu\home\$USER\work\humo_custodia.log)"
'@
    Info 'Prueba de humo en Ubuntu...'
    Invoke-Bash 'prueba' $bashPrueba
}

Info 'Listo. Abre Ubuntu y comprueba con:  java -version ; mvn -v'
