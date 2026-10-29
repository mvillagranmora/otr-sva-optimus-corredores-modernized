# TODO_MVP — Login en el core modernizado (opción B: UI fuera del core)

Fecha: 2026-10-23 · Repo: `modernized/otr-sva-optimus-corredores` @ `f43d20c9`

Diagramas de referencia (en esta carpeta):

| Archivo | Qué muestra |
|---|---|
| `LEGACY_LOGIN.html` | Secuencia del login actual en TomEE |
| `MVP_LOGIN.html` | Secuencia del login en el core modernizado y sus brechas |
| `LEGACY_LOGIN_CAJAS.html` | Componentes del legacy, con los objetos HANA de cada uno |
| `MVP_LOGIN_CAJAS.html` | Componentes del MVP (verde = existe, rojo = falta), con los objetos HANA de cada uno |

## 1. Alcance

**Qué entra en el MVP.** El usuario abre la página de login, ingresa con usuario y clave (canal COR), y puede:
- resolver una clave provisoria;
- recuperar su clave;
- aterrizar en el home de Optimus;
- volver al login.

Todo esto ocurre **sin TomEE**.

**Decisión de arquitectura (opción B).** La UI queda fuera del core.
- **Legacy:** `BootstrapServlet` arma `login.html` y `home.html` en Java, con los datos incrustados.
- **Modernizado:**
  - el equipo UI sirve los archivos estáticos;
  - el core expone el bootstrap como **API JSON**;
  - un loader de la UI pide ese JSON y define las mismas variables `BCS_*` antes de arrancar Angular.

**Qué hace este equipo:** el core. Son dos componentes que ya existen y no requieren módulos nuevos:
- **BcsGateway**, puerto `:9090`;
- **BcsPlataformaWeb**, puerto `:8117`, con BcsUtils como librería.

**Qué no hace este equipo:**
- **HANA:** tablas, vistas, procedimientos almacenados y datos.
- **UI del cliente:** HTML, JS, el loader y su servidor estático.

Lo que el core necesita de esos equipos está en la §6.

**Fuera del MVP:**
- las opciones del menú del home: llaman `/PLA/call?t=<IdServicio>`, que es el dispatcher CallServlet (hoy un stub 503), y cada opción pertenece a otro módulo;
- guardar preferencias (`call?t=1701`);
- CallWebsocket, reportes y descargas;
- los canales AGE/CLI/WS más allá de lo que comparte CrearSesion;
- BcsFilters y el resto de los 42 módulos.

El login no usa MongoDB, colas ni cachés.

## 2. Flujo objetivo

```
Navegador ──HTML/JS──► UI estática (equipo UI)
    │
    └──/BSCL/Plataforma/GWJAVA/PLA/*──► BcsGateway ──/PLA/*──► BcsPlataformaWeb ──JDBC──► SAP HANA
                                       (reescribe prefijo)     filtro de sesión
                                                               ├─ GET  /PLA/bootstrap/login   (JSON, sin token)
                                                               ├─ POST /PLA/CrearSesion
                                                               ├─ POST /PLA/bootstrap/home    (JSON, X-SESSION-TOKEN)
                                                               ├─ /PLA/ValidaClaveProvisoria, /PLA/ActualizarClave
                                                               ├─ /PLA/SolicitudRecuperacionClave, /PLA/RecuperacionClave, /PLA/ConfirmarCambioClave
                                                               └─ /PLA/SSO* (opcional)
```

Secuencia funcional:
1. La UI carga el login estático.
2. El loader llama `GET /PLA/bootstrap/login` y recibe las empresas y el logo.
3. `POST /PLA/CrearSesion` devuelve `P_OUT_TOKEN`; la UI lo guarda en `sessionStorage`, como ya hace hoy.
4. La UI navega al home estático.
5. El loader llama `POST /PLA/bootstrap/home` con `X-SESSION-TOKEN`:
   - **200:** recibe los datos `BCS_*` y arranca la SPA.
   - **401:** vuelve al login.

## 3. Estado actual

| Pieza | Estado | Comentario |
|---|---|---|
| BcsGateway (proxy, circuit breaker, fallback) | Existe | No tiene rutas para el login, no reescribe el prefijo, todas las rutas están `enabled=false` y cae a TomEE |
| CrearSesion (`CrearSesionController` + `CrearSesionService`) | Existe con brechas | T-03 a T-09 |
| ValidaClaveProvisoria / ActualizarClave (`PasswordController`) | Existe con brechas | `ActualizarClave` guarda la clave en BCrypt y se salta la política de claves (T-12) |
| SSO (`SSOController`) | Existe, sin revisar | Opcional para el MVP (T-16) |
| Bootstrap (`BootstrapController`) | **Placeholder** | Devuelve un HTML fijo y no valida el token; se reemplaza por la API JSON (T-20, T-21) |
| Recuperar clave (3 endpoints) | **No existe** | T-25 a T-27 |
| Filtro de sesión / ACL en BcsPlataformaWeb | **No existe** | El de BcsFilters usa tablas que no existen en HANA (`ACC_USUARIO`, `PLA_SERVICIO`) |
| Datos de sesión (`Sesion.getSesionData`) | **No existe** en el modernizado | Lo necesita el bootstrap home |
| Llamada a procedimientos almacenados con parámetros OUT | Parcial (`ProcedureDispatcherService`) | Revisar para los SP del bootstrap y de recuperación (T-22) |
| Prefijo `/PLA` en las rutas | **No existe** | Los controllers exponen `/CrearSesion`, `/bootstrap`… y el gateway no reescribe el path |
| HybridPasswordEncoder (BcsUtils) | Existe | Acepta SHA-256 y BCrypt; `encode()` produce BCrypt |
| Verificación | PARTLY PROVEN | BcsPlataformaWeb y BcsGateway, según VERIFICATION.md del 2026-10-20, sin firma; el test de integración HANA de CrearSesion está `@Disabled` |

## 4. Objetos HANA por componente

Los consume el core. No hay cambios de esquema.

| Componente | Tablas, vistas y procedimientos almacenados |
|---|---|
| Filtro de sesión | `ACC.Sesion` (`IdTokenSesion`, `TokenValido='S'`); para `/call`, además `PLA.Servicio`, `PLA.Configuracion`, `ACC.Usuario` |
| CrearSesion | `ACC.Usuario`, `PLA.Configuracion`, `EMP.Empleado` / `PER.Persona` / `CLI.Cliente`, `ACC.Sesion`, `PLA.Log`, `PLA.DetalleLog`, secuencias `Secuencias::Log` y `::DetalleLog`, `DUMMY` |
| Clave provisoria | ValidaClaveProvisoria: `ACC.Usuario`, `PLA.Configuracion`, `PLA.Diccionario`. ActualizarClave: `ACC.Usuario`, `PLA.Diccionario`, y la política `ValidarNuevaClave` (`ACC.PasswordProhibidas`, `PLA.Configuracion`) |
| Recuperar clave | SP `ACC.RecuperarClaveUsuario`, SP `ACC.ConfirmarCambioClaveUsuario`, SP `EVT.AgregarEvento` (notificación con la clave provisoria); `ACC.Usuario`, `PLA.Configuracion`, `PLA.Imagenes`, vista `PER.ConsultarPersona`, `SOC.SocioDeNegocio`, vista `CTT.ConsultarContratoDatosGenerales` |
| Bootstrap login | Vistas `PLA.ConsultarEmpresa` (`IdEstado='ACT'`) y `PLA.ConsultarImagen` (`LOGO_CORREDOR_LOGIN`) |
| Bootstrap home: sesión | `ACC.Sesion`, `ACC.Usuario`, `ACC.ConfiguracionSSO`, `EMP.Empleado`, `CLI.Cliente`, `PLA.Configuracion`, `PLA.Empresa`, `SOC.SocioDeNegocio`, `INS.Corredora`, `PLA.ValorDefectoSistema` |
| Bootstrap home: accesos | Vista `ACC.ConsultarUsuarioAcceso`, SP `ACC.ConsultarUsuarioAdmin` |
| Bootstrap home: catálogos | SP `ConsultarFormatos`; vistas `PLA.ConsultarConfiguracionUsuario` y `PLA.ConsultarConfiguracionGrilla`; `PLA.Diccionario`, `REG.DiaFeriado`, `INS.TipoOperacion`, `INS.TipoOperacionTipoInstrumento`, `PLA.ConfiguracionFiltroInterfaz` |
| SSO (opcional) | `ACC.ConfiguracionSSO`, `ACC.Usuario`, `PLA.Configuracion`, `EMP.Empleado`, `PER.DireccionElectronica`; crea la sesión en `ACC.Sesion` (canal SSO) |

## 5. Tareas del core

Prioridad: **P0** bloquea el MVP · **P1** es necesario para que el MVP sea equivalente al legacy · **P2** es opcional.

### 5.1 BcsGateway

- [ ] **T-01 (P0) Reescritura del prefijo.** `/BSCL/Plataforma/GWJAVA/PLA/**` → `/PLA/**` antes de comparar las rutas y de armar la URL de destino (`ProxyRouter.java:100-135`, `buildTargetUri` en `:329-335`). Reemplaza la regla que hoy hacen nginx (Docker) y Apache (producción).
  *Aceptación:* un `POST /BSCL/Plataforma/GWJAVA/PLA/CrearSesion` llega a bcs-plataforma-web como `/PLA/CrearSesion`, con el body y las cabeceras intactos.
- [ ] **T-02 (P0) Rutas del login hacia bcs-plataforma-web, sin TomEE.**
  - Rutas: `/PLA/CrearSesion`, `/PLA/bootstrap/**`, `/PLA/ValidaClaveProvisoria`, `/PLA/ActualizarClave`, `/PLA/SolicitudRecuperacionClave`, `/PLA/RecuperacionClave`, `/PLA/ConfirmarCambioClave` y `/PLA/SSO*`.
  - Con el fallback a TomEE apagado: si no hay servicio, responde 503, nunca TomEE.
  - Habilitarlas solo después de G-01 (regla en `application.yml:15`).
  - *Aceptación:* con TomEE apagado, el flujo de la §2 completo responde desde el core.
- [ ] **T-02b (P1) Cabeceras de seguridad.** Agregar `X-Frame-Options: SAMEORIGIN` y `X-Content-Type-Options: nosniff`, que hoy pone nginx. Si la UI se sirve desde otro origen, configurar CORS para ese origen.
- [ ] **T-02c (P1) Despliegue mínimo.** Compose o manifiesto con solo `bcs-gateway` y `bcs-plataforma-web`. Hoy el gateway depende de 12 servicios (`docker-compose.yml:978-990`).

### 5.2 BcsPlataformaWeb: transversal

- [ ] **T-10 (P0) Prefijo `/PLA`.** `server.servlet.context-path=/PLA`, o mapear `/PLA/...` en cada controller. Revisar que `/internal/**` y `actuator` sigan donde los esperan los jobs y el healthcheck.
- [ ] **T-11 (P0) Filtro de sesión.** Equivalente a `SesionFilter`/`ServiceFilter` del legacy, sobre `ACC.Sesion`.
  - **Exentas:** login, bootstrap/login, clave, recuperación, SSO, ping y configuraciones.
  - **Resto, incluido `bootstrap/home`:** exige `X-SESSION-TOKEN`. Responde 400 si falta y 401 si no existe o tiene `TokenValido<>'S'`.
  - El control por servicio (`PLA.Servicio.ValidaAccesoServicio`) queda para cuando entre `/call`.
  - Referencias legacy: `SesionFilter.java:43-118` y `ServiceFilter.java:32-212`.
- [ ] **T-13 (P0) Transacción única por petición.** Hoy los adapters usan `NamedParameterJdbcTemplate` con su propia conexión, y el `commit`/`rollback` de la `Connection` del controller no los cubre. Usar `@Transactional`, o pasar la conexión a los adapters.
  Debe quedar igual que el legacy:
  - **Éxito:** commit.
  - **Clave errónea:** se confirma igual el incremento de `IntentosConexion` y el bloqueo, como hace el legacy en `CrearSesion.java:504`.
- [ ] **T-15 (P1) Configuración y secretos.** `HANA_URL`, `HANA_USER` y `HANA_PASSWORD` por variable de entorno o vault, sin literales (RULE-076).
- [ ] **T-22 (P0) Procedimientos almacenados con parámetros OUT.** Llamador común para los SP del bootstrap y de recuperación (`SimpleJdbcCall`, o extender `ProcedureDispatcherService`).

### 5.3 BcsPlataformaWeb: CrearSesion

Referencia legacy: `CrearSesion.java:107-392`, `Sesion.java:130-200`, `SesionUtils.java:42-76`, `AgregarLog.java:205-279`.

- [ ] **T-03 (P0) Estado de la entidad.** Según `TipoEntidad` y canal, consultar:
  - `EMP.Empleado` (canales COR/AGE/WS);
  - `PER.Persona` (CLI);
  - `CLI.Cliente` (WS/CLI).

  Exigir `IdEstado` ACT (CLI también acepta BLO). Hoy solo se miran los indicadores del usuario.
- [ ] **T-04 (P0) Sesión única.** Antes del INSERT, `UPDATE ACC.Sesion SET TokenValido='N'` cuando `PLA.SesionUnicaUsuario='S'` y el canal está en `PLA.CanalesSesionUnica` (`Sesion.java:156-170`).
- [ ] **T-05 (P1) Expiración del canal WS.** Reutilizar el token solo si `ahora − FechaUltimoUso ≤ PLA.IdleMaxTime<canal>` (1.800.000 ms por defecto).
- [ ] **T-06 (P1) Auditoría.**
  - Registrar en `PLA.Log` + `PLA.DetalleLog`, con las secuencias `Secuencias::Log` y `::DetalleLog`.
  - Datos del registro: INFORMATIVO, módulo PLA, tipo SESION, acción "INICIO SESION EXITOSA".
- [ ] **T-07 (P1) Formato del token.** `Base64(idEmpresa:usuario:timestamp + UUID)`, y el INSERT debe incluir `IdEntidadSSO`. Hoy el token se genera con empresa y usuario null.
- [ ] **T-08 (P0) No re-hashear a BCrypt en el login.** Mantenerlo apagado mientras otras aplicaciones lean `ACC.Usuario.Clave` en SHA-256.
- [ ] **T-09 (P1) Contrato de respuesta.**
  - **Éxito:** 200 `{P_OUT_TOKEN, P_OUT_ADVERTENCIASCONTADOR, P_OUT_ERRORESCONTADOR}`.
  - **Error:** 401 `USUARIO_CLAVE_INCORRECTA`.
  - Mantener el doble SHA-256: el navegador envía `SHA-256(clave)` (CryptoJS, `login.html:125`) y el servidor vuelve a aplicarle SHA-256.

### 5.4 BcsPlataformaWeb: bootstrap como API JSON (reemplaza el placeholder)

Referencia legacy de las consultas: `BootstrapServlet.java:58-375`.

El core ya no lee plantillas ni arma HTML.

- [ ] **T-20 (P0) `GET /PLA/bootstrap/login`** (sin token). Devuelve `{empresas: [...], logo: ...}`:
  - empresas: vista `PLA.ConsultarEmpresa` con `IdEstado='ACT'`;
  - logo: vista `PLA.ConsultarImagen` con `LOGO_CORREDOR_LOGIN`.

  Reemplaza `<::EMPRESAS::>` y `<::LOGO_CORREDOR::>`.
- [ ] **T-21 (P0) `POST /PLA/bootstrap/home`** (con `X-SESSION-TOKEN`). Devuelve en un JSON lo que hoy va en `BCS_SESION`, `BCS_ACCESOS`, `BCS_USER_MENU`, `BCS_PREFERENCIAS`, `BCS_PREFERENCIAS_GRILLA` y `BCS_DICCIONARIO`, más formatos, feriados, tipos de operación, filtros de interfaz y configuración. Los objetos HANA están en la §4.
  1. **Portar `Sesion.getSesionData`:** lee la sesión y los datos de empresa y corredora, y actualiza `ACC.Usuario` (conectado) y `ACC.Sesion` (uso del token).
  2. **Token inválido:** 401 sin cuerpo con datos.
- [ ] **T-21b (P0) Contrato JSON acordado con UI.** Mismos nombres y forma que las variables `BCS_*` actuales, para que la SPA no note el cambio. Hay que versionar el contrato.
  *Aceptación:* el loader de la UI arma variables `BCS_*` idénticas, comparadas con un home armado por el legacy para el mismo usuario.
- [ ] **T-21c (P1) Menú y ventanas.** Hoy el menú por usuario y `config_ventana.js` salen de archivos estáticos que el bootstrap incrusta. El core entrega solo el código de menú del usuario (`CodigoMenuUsuario`) y los accesos; los archivos de menú pasan a la UI.
- [ ] **T-21d (P1) RUM de Datadog.** Si el home lo usa, el core entrega solo la configuración (ambiente, versión); el script lo incluye la UI.

### 5.5 BcsPlataformaWeb: gestión de clave

- [ ] **T-12 (P0) Corregir `ActualizarClave`** (`PasswordController.java:53`; legacy `ActualizarClave.java:110-150`).
  - Guardar la clave como el legacy: `Utils.encryptPassword`, que es doble SHA-256 en mayúsculas (`Utils.java:710-714`). Hoy se guarda en BCrypt (`HybridPasswordEncoder.encode`).
  - Aplicar la política `ValidarNuevaClave` (`ACC.PasswordProhibidas`, `PLA.Configuracion`) y devolver sus criterios OK/NOK.
  - Actualizar también `FechaIngreso` y `UltimoCambioClave`.
- [ ] **T-24 (P1) Revisar `ValidaClaveProvisoria`** contra el legacy: intentos, vencimiento de la clave provisoria y mensajes de `PLA.Diccionario`.
- [ ] **T-25 (P0) `SolicitudRecuperacionClave`.** Llama al SP `ACC.RecuperarClaveUsuario` y devuelve el correo enmascarado (`Utils.obfuscateEmail(P_OUT_MAIL)`).
- [ ] **T-26 (P0) `RecuperacionClave`.** Usa los objetos listados en la §4.
- [ ] **T-27 (P0) `ConfirmarCambioClave`.** Llama al SP `ACC.ConfirmarCambioClaveUsuario`.

### 5.6 Opcional

- [ ] **T-16 (P2) SSO** (Google / BofA / logout SSO). Ya tiene endpoint; revisarlo solo si el MVP muestra el botón SSO (`BCS_MUESTRA_SSO_GOOGLE`).
  Con la opción B:
  - las páginas HTML que hoy arma el SSO (por ejemplo `autologout.html`) pasan a la UI;
  - el core responde con redirect o JSON.

### 5.7 Calidad y salida (gates)

- [ ] **G-01 (P0) Verify en PROVEN** de BcsPlataformaWeb y BcsGateway, firmado por el arquitecto en `VERIFICATION.md`, antes de habilitar las rutas.
- [ ] **G-02 (P0) Tests de caracterización** por tarea, nombrando la RULE (RULE-010..030). Incluyen:
  - el test de integración contra la base de pruebas acordada (hoy `@Disabled`);
  - un canario por cada brecha cerrada;
  - un test de contrato del JSON del bootstrap (T-21b).
- [ ] **G-03 (P0) Prueba punta a punta sin TomEE:** abrir el login → error de clave (401 y aumento de intentos) → bloqueo temporal → login correcto → bootstrap/home 200 → token inválido (401).
- [ ] **G-04 (P1) Observabilidad:** logs con correlación por request, sin claves ni tokens completos, y trazas equivalentes al `@Trace` de Datadog.

## 6. Lo que el core necesita de otros equipos

| Equipo | Necesidad | Para qué tarea |
|---|---|---|
| UI | Loader que llame `bootstrap/login` y `bootstrap/home`, defina las variables `BCS_*` y vuelva al login ante un 401 | T-20, T-21 |
| UI | Acordar y firmar el contrato JSON del bootstrap | T-21b |
| UI | Cambiar el POST del formulario a `optimus` por una navegación al home estático, que lee el token de `sessionStorage` (ya lo guarda hoy) | T-21 |
| UI | Asumir los archivos de menú, ventanas, RUM y las páginas 401 y autologout que hoy arma el bootstrap o el SSO | T-21c, T-21d, T-16 |
| UI | Apuntar las llamadas `/BSCL/Plataforma/GWJAVA/PLA/*` al gateway | T-01 |
| UI / infraestructura | Confirmar si Apache de producción traduce `/BSCL/Plataforma/UI/optimus` al bootstrap. Con la opción B esa regla desaparece, porque el home es estático | T-01 |
| HANA | Crear el usuario técnico del core con los permisos mínimos de la §9 (y nada más) | T-15, G-03 |
| HANA | Base de pruebas con usuarios de prueba (canal COR, uno bloqueado, uno con clave provisoria) y credenciales de un usuario técnico | G-02, G-03 |
| HANA | Firma (parámetros IN/OUT) de `ACC.ConsultarUsuarioAdmin`, `ConsultarFormatos`, `ACC.RecuperarClaveUsuario` y `ACC.ConfirmarCambioClaveUsuario` | T-21, T-22, T-25, T-27 |
| HANA | Confirmar cómo se envía el correo de recuperación (el servlet solo enmascara `P_OUT_MAIL`) | T-25 |
| HANA | Valores de `PLA.SesionUnicaUsuario`, `PLA.CanalesSesionUnica` y `PLA.IdleMaxTime*` en el ambiente de pruebas | T-04, T-05 |

## 7. Riesgos y decisiones abiertas

1. **BCrypt durante la convivencia.** Cualquier escritura en BCrypt (T-08, T-12) deja fuera a las aplicaciones que siguen comparando SHA-256. Decisión pendiente: cuándo se migra a BCrypt y quién lo coordina.
2. **Home público con la opción B.** La página del home queda estática y accesible sin sesión; lo protegido son los datos (`bootstrap/home` responde 401). La UI no debe mostrar información sin haber recibido el JSON.
3. **Dos logins en el modernizado.** El MVP usa BcsPlataformaWeb. Hay que decidir si se retira o se corrige el `LoginController` de BcsFilters.
4. **Home sin TomEE.** Los menús llaman `/call`, que está fuera del MVP. Hay que definir qué ve el usuario al pulsar una opción, o si el MVP incluye alguna, lo que sumaría el dispatcher `/call` y sus servicios.
5. **TomEE en paralelo.** Si el legacy sigue activo para otros usuarios, ambos comparten `ACC.Sesion` y `ACC.Usuario`; la sesión única (T-04) cierra sesiones de un sistema desde el otro.

## 8. Orden sugerido

1. **Base:** T-10, T-11, T-13, T-15 y T-22.
2. **CrearSesion completo:** T-03 a T-09.
3. **Bootstrap API:** T-21b (contrato con UI), luego T-20 y T-21, y después T-21c y T-21d.
4. **Clave:** T-12 y T-24 a T-27.
5. **Gateway:** T-01, T-02, T-02b y T-02c.
6. **Gates:** G-02 → G-03 → G-01 (firma) → habilitar las rutas.

## 9. Inventario de recursos SAP HANA (para permisos)

Lista de todo lo que el core consume en HANA para el MVP de login. Sirve para definir los permisos del usuario técnico de BcsPlataformaWeb con mínimo privilegio. La extraje del código legacy que el core replica:
- `CrearSesion`, `BootstrapServlet`, `ValidaClaveProvisoria`, `ActualizarClave`, `SolicitudRecuperacionClave`, `RecuperacionClave`, `ConfirmarCambioClave`, `SSOUtils`;
- de BcsUtils: `Sesion`, `SesionUtils`, `AgregarLog`, `ValidarNuevaClave`, `AgregarNotificacion`, `Utils.getSecuencia`;
- `SesionFilter`.

Prefijos usados:
- `PLA-BD` = `BSCL.Plataforma.BD`;
- `PLA-V` = `BSCL.Plataforma.BD.Vistas`;
- `PLA-S` = `BSCL.Plataforma.BD.Servicios`;
- `PLA-SEQ` = `BSCL.Plataforma.BD.Secuencias`;
- `SN-BD` = `BSCL.SocioNegocio.BD`;
- `SN-V` = `BSCL.SocioNegocio.BD.Vistas`.

Todos los objetos viven en el esquema `BSCL`.

Columna "Alcance": **MVP** = obligatorio · **SSO** = solo si el MVP incluye SSO (T-16) · **Fuera** = no se usa en el MVP, se lista para no otorgarlo por error.

### 9.1 Tablas

| # | Objeto | Permiso | Alcance | Motivo del uso |
|---|---|---|---|---|
| 1 | `PLA-BD::ACC.Usuario` | SELECT, UPDATE | MVP | **SELECT:** leer el usuario en el login (clave, estado, vigencia, intentos, bloqueo, `TipoEntidad`, indicadores por canal), en la clave provisoria, en el cambio de clave, en la recuperación y en los datos de sesión del home. **UPDATE:** `IntentosConexion` y `FechaBloqueoTemporal` (clave errónea y reset); `FechaUltimaConexion`, `Conectado` y `Hora` al armar el home; `ClaveProvisoria`, `FechaClaveProvisoria` e `IntentosConexionClaveProvisoria` (validar y recuperar clave); `Clave`, `FechaIngreso`, `IdEstado`, `DebeCambiarClave` y `UltimoCambioClave` (ActualizarClave) |
| 2 | `PLA-BD::ACC.Sesion` | SELECT, INSERT, UPDATE | MVP | **INSERT:** crear el token de sesión. **UPDATE:** invalidar tokens por sesión única (`TokenValido='N'`), invalidar el token WS inactivo, y registrar el uso (`FechaUltimoUso`, `UsoToken`). **SELECT:** buscar un token WS reutilizable, validar `X-SESSION-TOKEN` en el filtro y leer la sesión para el home |
| 3 | `PLA-BD::ACC.ConfiguracionSSO` | SELECT | MVP | LEFT JOIN de los datos de sesión del home (`RedirectURL` cuando la sesión vino por SSO). Se lee aunque no se use SSO; en SSO, además, la configuración del proveedor |
| 4 | `PLA-BD::ACC.PasswordProhibidas` | SELECT | MVP | Política de clave nueva (`ValidarNuevaClave`): rechazar claves prohibidas en ActualizarClave |
| 5 | `PLA-BD::PLA.Configuracion` | SELECT | MVP | Parámetros: `PLA.FechaSistema`, `PLA.ClaveUsuarioIntentos`, `PLA.ClaveUsuarioTiempoBloqueo`, `PLA.IdleMaxTime*`, `PLA.SesionUnicaUsuario`, `PLA.CanalesSesionUnica`, la política de claves, los parámetros de sesión del home, `PLA.ConflictoAccesoDefecto` y la configuración de recuperación |
| 6 | `PLA-BD::PLA.Empresa` | SELECT | MVP | Datos de la empresa para la sesión del home (`getSesionData`) |
| 7 | `PLA-BD::PLA.ValorDefectoSistema` | SELECT | MVP | Valores por defecto de la empresa (`IdEstado='ACT'`) que se entregan con la sesión del home |
| 8 | `PLA-BD::PLA.Diccionario` | SELECT | MVP | Textos por idioma: `BCS_DICCIONARIO` del home y mensajes de ValidaClaveProvisoria y ActualizarClave |
| 9 | `PLA-BD::PLA.ConfiguracionFiltroInterfaz` | SELECT | MVP | Filtros de interfaz activos que el home entrega a la UI |
| 10 | `PLA-BD::PLA.Imagenes` | SELECT | MVP | Logo de la corredora que se incluye en la notificación de recuperación de clave |
| 11 | `PLA-BD::PLA.Log` | INSERT | MVP | Auditoría: cabecera del registro "INICIO SESION EXITOSA" (módulo PLA, tipo SESION) |
| 12 | `PLA-BD::PLA.DetalleLog` | INSERT | MVP | Auditoría: detalle del registro (FechaIngreso, ACCION, CANAL, TokenValido) |
| 13 | `PLA-BD::INS.Corredora` | SELECT | MVP | Corredora asociada a la empresa, para la sesión del home |
| 14 | `PLA-BD::INS.TipoOperacion` | SELECT | MVP | Catálogo de tipos de operación del home |
| 15 | `PLA-BD::INS.TipoOperacionTipoInstrumento` | SELECT | MVP | Relación tipo de operación / tipo de instrumento del home |
| 16 | `PLA-BD::REG.DiaFeriado` | SELECT | MVP | Feriados activos que el home entrega a la UI |
| 17 | `SN-BD::EMP.Empleado` | SELECT | MVP | Estado del empleado al hacer login (canales COR/AGE/WS) y datos de sesión del home; en SSO, buscar al empleado |
| 18 | `SN-BD::CLI.Cliente` | SELECT | MVP | Estado del cliente al hacer login (canales WS/CLI) y datos de sesión del home |
| 19 | `SN-BD::PER.Persona` | SELECT | MVP | Estado de la persona al hacer login (canal CLI). CrearSesion lo consulta según `TipoEntidad` |
| 20 | `SN-BD::SOC.SocioDeNegocio` | SELECT | MVP | Socio de negocio de la empresa (sesión del home) y del usuario (datos para la notificación de recuperación) |
| 21 | `SN-BD::PER.DireccionElectronica` | SELECT | SSO | Buscar al usuario por su correo en el login SSO |
| 22 | `PLA-BD::PLA.Servicio`, `PLA-BD::PLA.ServicioExtra` | SELECT | Fuera | Registro de servicios de `/call` y ACL por servicio; entra cuando se migre CallServlet |

### 9.2 Vistas

| # | Objeto | Permiso | Alcance | Motivo del uso |
|---|---|---|---|---|
| 23 | `PLA-V::PLA.ConsultarEmpresa` | SELECT | MVP | Empresas activas para el selector de la página de login (`bootstrap/login`) |
| 24 | `PLA-V::PLA.ConsultarImagen` | SELECT | MVP | Logo `LOGO_CORREDOR_LOGIN` de la página de login |
| 25 | `PLA-V::PLA.ConsultarConfiguracionUsuario` | SELECT | MVP | Preferencias del usuario (`BCS_PREFERENCIAS`) |
| 26 | `PLA-V::PLA.ConsultarConfiguracionGrilla` | SELECT | MVP | Preferencias de grillas (`BCS_PREFERENCIAS_GRILLA`) |
| 27 | `PLA-V::ACC.ConsultarUsuarioAcceso` | SELECT | MVP | Accesos del usuario (`BCS_ACCESOS`) |
| 28 | `PLA-V::CTT.ConsultarContratoDatosGenerales` | SELECT | MVP | Razón social de la corredora para la notificación de recuperación de clave |
| 29 | `SN-V::PER.ConsultarPersona` | SELECT | MVP | Nombre y dirección electrónica principal del usuario para la notificación de recuperación de clave |

Si las vistas tienen `SQL SECURITY INVOKER`, el usuario también necesita SELECT sobre sus tablas base; con DEFINER basta el SELECT sobre la vista. Lo confirma el DBA.

### 9.3 Procedimientos almacenados

| # | Objeto | Permiso | Alcance | Motivo del uso |
|---|---|---|---|---|
| 30 | `PLA-S::ACC.ConsultarUsuarioAdmin` | EXECUTE | MVP | Indica si el usuario es administrador (`p_out_EsAdmin` → `BCS_ACCESOS.ADMIN`) |
| 31 | `PLA-S::ConsultarFormatos` | EXECUTE | MVP | Formatos de moneda, instrumento y operación que el home entrega a la UI |
| 32 | `PLA-S::ACC.RecuperarClaveUsuario` | EXECUTE | MVP | Inicia la recuperación de clave (SolicitudRecuperacionClave) y devuelve `P_OUT_MAIL`, que el core enmascara |
| 33 | `PLA-S::ACC.ConfirmarCambioClaveUsuario` | EXECUTE | MVP | Confirma el cambio de clave (ConfirmarCambioClave) |
| 34 | `PLA-S::EVT.AgregarEvento` | EXECUTE | MVP | Registra el evento `SOLICITUD_CAMBIO_CLAVE` (vía `AgregarNotificacion`) que dispara la notificación o el correo con la clave provisoria (RecuperacionClave) |

Si los procedimientos son `SQL SECURITY DEFINER` (el valor por defecto en HANA), basta EXECUTE; con INVOKER, el usuario necesita los permisos de lo que el procedimiento toca por dentro.

`EVT.AgregarEvento` recibe parámetros de tipo tabla mediante tablas temporales locales (`#AgregarEvento_P_EVENTOS`, `#..._P_VALORES`, `#..._P_ARCHIVOSADJUNTOS`, `#..._P_IDDIRECCIONES`). El usuario debe poder crearlas en su sesión; el DBA confirma el permiso necesario.

### 9.4 Secuencias y objetos del sistema

| # | Objeto | Permiso | Alcance | Motivo del uso |
|---|---|---|---|---|
| 35 | `PLA-SEQ::Log` | SELECT (NEXTVAL) | MVP | Ids de `PLA.Log` en la auditoría del login (`Utils.getSecuencia`) |
| 36 | `PLA-SEQ::DetalleLog` | SELECT (NEXTVAL) | MVP | Ids de `PLA.DetalleLog` |
| 37 | `DUMMY`, `SERIES_GENERATE_BIGINT` | (PUBLIC) | MVP | Hora UTC del servidor (`SELECT CURRENT_UTCTIMESTAMP FROM DUMMY`) y generación de varios NEXTVAL en una consulta; normalmente no requieren permiso adicional |

### 9.5 Resumen de permisos

- **SELECT + UPDATE:** `ACC.Usuario`.
- **SELECT + INSERT + UPDATE:** `ACC.Sesion`.
- **Solo INSERT:** `PLA.Log`, `PLA.DetalleLog`.
- **SELECT:** las tablas 3 a 10 y 13 a 20, y las vistas 23 a 29.
- **SELECT en secuencias:** `Secuencias::Log`, `Secuencias::DetalleLog`.
- **EXECUTE:** los procedimientos 30 a 34.
- **Con SSO, además:** SELECT sobre `PER.DireccionElectronica`.
- **No otorgar** DELETE, DDL ni permisos sobre otros objetos: el login no borra filas ni crea objetos, salvo las tablas temporales locales de la §9.3.

### 9.6 A confirmar

- El helper `cl.bcs.nuam.StoredProcedure`, que usan los procedimientos 30 a 33, viene en una librería privada que no está en el repo. Si lee el catálogo del sistema (por ejemplo `SYS.PROCEDURE_PARAMETERS`) para armar la llamada, el usuario necesita acceso a esas vistas del sistema, que suelen ser PUBLIC. El reemplazo modernizado (T-22) debe documentar qué consulta.
- Los modos `SQL SECURITY` de las vistas 23 a 29 y de los procedimientos 30 a 34 (ver las notas de §9.2 y §9.3).
- El canal por el que `EVT.AgregarEvento` envía el correo; puede requerir permisos o servicios adicionales del lado de HANA.
