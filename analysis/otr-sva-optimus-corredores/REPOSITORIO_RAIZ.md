# Repositorio en la raiz del proyecto (legacy + analysis + modernized)

Preparado el 2026-10-29. Antes de esto el repositorio era solo `modernized/otr-sva-optimus-corredores` (origin `github.com/mvillagranmora/otr-sva-optimus-corredores`, privado, `main` en `fa32107b`, subido) y el legacy tenia su propio `.git` (origin `github.com/nuamx/otr-sva-optimus-corredores`). `analysis/` no estaba versionado.

## Que se dejo listo (nada de esto toca el legacy)
| Archivo | Para que |
|---|---|
| `.gitignore` (raiz) | `.env` y `**/.env` (credenciales de HANA QA), `.claude/settings.local.json`, `target/`, temporales de verificacion. Las pruebas `*.test-output.txt`, `cases.json`, `fresh-cases.json`, `VERIFICATION.*` y `canary/` SI se versionan (son la evidencia). |
| `.gitattributes` (raiz) | `legacy/** -text` y las rutas de goldens/SQL/evidencia `-text`: este equipo tiene `core.autocrlf=true` a nivel de sistema y sin esto Git podria reescribir saltos de linea del legacy congelado o de los goldens (se comparan byte a byte). |
| `analysis/otr-sva-optimus-corredores/tools/legacy_manifest.py` | `crear` y `verificar`: SHA-256 de cada archivo de `legacy/` (sin los `.git`). |
| `analysis/otr-sva-optimus-corredores/LEGACY_SHA256.manifest` | el manifiesto generado ANTES del `git init`. |
| `analysis/otr-sva-optimus-corredores/tools/git-hooks/pre-commit` | rechaza cambios en `legacy/` despues del primer commit y cualquier `.env` en el area de preparacion. |
| `C:\Users\m.villagran.mora\Downloads\develop\backup-git-20261029\` | copia de los dos `.git` actuales (legacy 443 MB y modernized) por si algo falta. |

## Antes de borrar los `.git` (hay DOS)
1. `modernized/otr-sva-optimus-corredores/.git`: todo subido (`origin/main` = `HEAD`, arbol limpio).
2. `legacy/otr-sva-optimus-corredores/.git` (443 MB), origen `nuamx`: **no es solo un espejo del remoto**:
   - `dev` y `master` difieren del remoto (estaban adelantadas o atrasadas respecto de lo ultimo traido);
   - `remove_ant` no existe en el remoto (apunta al mismo commit que `feat/modernizacion-acn`, que si esta);
   - **6 entradas de stash** que solo viven en ese `.git`;
   - el arbol esta en `feat/acn-update-cicd`, igual al remoto.
   Si necesitas el historial o los stash, conserva la copia de `backup-git-20261029\` (o haz `git bundle create` antes). Borrarlos no cambia ningun archivo fuente del legacy.

## Pasos (en la raiz `...\nuam-modernizado`)
1. Comprobar que la copia termino: existe `backup-git-20261029\COPIA_TERMINADA.txt`, y que el manifiesto termino (`LEGACY_SHA256.manifest` con una linea por archivo).
2. Borrar `legacy/otr-sva-optimus-corredores/.git` y `modernized/otr-sva-optimus-corredores/.git`. IMPORTANTE: si queda un `.git` dentro de `legacy/`, el nuevo repositorio lo tomaria como submodulo vacio.
3. `git init -b main` y `git config core.autocrlf false` (solo este repositorio).
4. `git add legacy` y comprobar que no salio ningun aviso de CRLF; commit aparte: `legacy congelado (copia de feat/acn-update-cicd)`. Verificar: `py -3 -I analysis/otr-sva-optimus-corredores/tools/legacy_manifest.py verificar` debe decir `different 0 | missing 0 | new 0`.
5. Instalar el hook: `cp analysis/otr-sva-optimus-corredores/tools/git-hooks/pre-commit .git/hooks/pre-commit` (en Windows con Git Bash).
6. `git add analysis modernized .gitignore .gitattributes` y revisar `git status` (no debe aparecer `.env` ni `target/`): commit(s) aparte.
7. `git remote add origin <url>` y `git push -u origin main` (repositorio PRIVADO: `legacy/` tiene credenciales de HANA escritas en `main()` muertos, RULE-076).

## Efectos en el desarrollo
- Mis comandos git desde `modernized/otr-sva-optimus-corredores` siguen igual (git sube hasta la raiz). Los commits seguiran siendo en espanol y sin linea de coautor.
- El pack de verificacion comprueba el legacy por FECHA de modificacion (`PREFLIGHT.md`, 40.470 archivos) y no ejecuta git: con `git init` en el mismo directorio las fechas se conservan; un `git checkout`, `git stash`, `git reset --hard`, un clon nuevo o un cambio de rama que reescriba archivos de `legacy/` las cambia. No hacer eso sobre `legacy/`. El manifiesto (40.470 archivos, sin los `.git`) coincide con el conteo del pack, asi que borrar los `.git` del legacy no deberia cambiar ese conteo; `legacy_manifest.py verificar` ya dio `different 0 | missing 0 | new 0` antes de borrar (2026-10-29).
- Un clon del repositorio en otra maquina volveria a escribir `legacy/` y cambiaria las fechas: para verificar alli, usar `legacy_manifest.py verificar` en lugar de la fecha.
