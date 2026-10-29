# Cómo usar el login modernizado desde la UI

Para quien desarrolla la UI estática (opción B: la UI vive fuera del core y habla con el servicio solo por HTTP/JSON).
Estado al 26-oct-2026. Los ejemplos con datos están en `USO_LOGIN_anexos/` (recortados; las respuestas reales pesan 1,3 MB el login y 2,4 MB el home).

> **Estado del servicio.** Los endpoints existen y están probados, pero la ruta del gateway (`bcs-plataforma-login`) está **apagada** hasta que la verificación se firme. Mientras tanto, para desarrollar contra QA se llama directo a `bcs-plataforma-web` (puerto 8117) con las mismas rutas `/PLA/...`. El contrato JSON del bootstrap (`optimus-bootstrap` v1) es un **borrador**: si algo le estorba a la UI, se cambia ahora, antes de congelarlo.

## 1. El flujo en una vista

```
1. GET  /PLA/bootstrap/login          sin token   -> datos de la página de login (empresas, logo, diccionario, ambiente)
2. POST /PLA/CrearSesion              usuario + clave (hash)  -> 200 { P_OUT_TOKEN } | 401
3. POST /PLA/bootstrap/home           cabecera X-SESSION-TOKEN -> 200 datos de la sesión (sesión, accesos, preferencias, formatos...) | 401
4. el resto de las llamadas           cabecera X-SESSION-TOKEN en cada una
```

Qué cambió respecto al legacy: antes el servidor armaba el HTML de login y de home con las variables JavaScript `BCS_*` escritas dentro de la página, y el token viajaba en un formulario `POST /UI/optimus` con el campo `X-SESSION-TOKEN`. Ahora el servidor **solo devuelve JSON**. La UI pide el JSON, define las mismas variables `BCS_*` (los nombres de las claves del JSON son exactamente los nombres de las variables) y arranca Angular. Menús y ventanas (`BCS_FULLMENU`, `BCS_FULLMENU_PERU`, `BCS_MENU_AGENTE`, `BCS_VENTANA`) **no** vienen del servidor: son de la UI.

## 2. Convenciones

| Tema | Regla |
|---|---|
| URL pública | `https://<host>/BSCL/Plataforma/GWJAVA/PLA/<endpoint>`. El gateway la reescribe a `/PLA/<endpoint>`. En desarrollo directo: `http://<servicio>:8117/PLA/<endpoint>`. |
| Cuerpo | JSON, `Content-Type: application/json`, UTF-8. |
| Token | Cabecera `X-SESSION-TOKEN: <P_OUT_TOKEN>` en **todo** llamado salvo los de la lista de la sección 6. Sin cabecera: **400**. Token que no existe o ya no es válido: **401**. |
| Mismo origen | La UI y el gateway comparten dominio, así que no se necesita CORS. Si algún día la UI se sirve desde otro origen, hay que pedir que lo agreguen a `BCS_GATEWAY_CORS_ALLOWED_ORIGINS` (lista exacta, sin comodín). |
| Cabeceras de seguridad | El gateway pone `X-Frame-Options: SAMEORIGIN` y `X-Content-Type-Options: nosniff`. La UI no debe necesitar incrustarse en un iframe de otro dominio. |
| Errores 500 | Fallo del servidor o sin conexión a la base. El cuerpo no trae detalle. |
| Hash de clave | Ver la sección 3: **en minúsculas hexadecimales**, porque el servidor vuelve a calcular el hash sobre ese texto. Una letra en mayúscula da un hash distinto y el login falla. |

## 3. Endpoints

### 3.1 `GET /PLA/bootstrap/login`: datos de la página de login

Sin token. Respuesta 200 con las claves de la sección 4 en su versión "de login" (sesión, accesos, preferencias, feriados y configuración vienen **vacíos**), más:

- `empresas`: `[{ "IdEmpresa": 1, "Nombre": "..." }]`, solo empresas activas, para el selector de empresa.
- `logo`: `[{ "NombreImagen": "LOGO_CORREDOR_LOGIN", "Binario": "<base64>" }]`, el logo de la página de login (PNG en base64).
- `BCS_DICCIONARIO`: textos en español (clave `CODIGO` → texto, y `CODIGO_ABR` → abreviatura). Trae unas 23.000 claves; guárdelo en memoria, no lo renderice completo.
- `BCS_AMBIENTE` (`QA`, `DES`, `PRD`...), `BCS_OFFSET` (segundos de zona horaria), `BCS_MUESTRA_SSO_GOOGLE` (texto `"S"`, `"N"` o el **texto** `"null"` cuando no hay valor, como lo escribía la página legacy; compárelo como texto), `CONF_IMAGEN_FONDO` (`["santiago-bg.jpg","santiago2-bg.jpg"]`, imágenes de fondo del login).

Ejemplo recortado: `USO_LOGIN_anexos/bootstrap-login.ejemplo.json`.

### 3.2 `POST /PLA/CrearSesion`: iniciar sesión

Cuerpo:

```json
{ "p_IdEmpresa": 1, "p_NombreUsuario": "USUARIO", "p_ClaveUsuario": "<sha256 hex en minúsculas de la clave>", "p_IdCanal": "COR" }
```

- `p_ClaveUsuario` es `SHA-256(clave en texto)` escrito en hexadecimal **minúsculo** (64 caracteres). La clave en texto nunca viaja. En JavaScript: `crypto.subtle.digest('SHA-256', new TextEncoder().encode(clave))` y convertir a hexadecimal.
- `p_IdCanal`: `COR` (corredores, la UI de Optimus), `AGE` (negocios/agente), `CLI` (clientes), `WS` (web services).
- Los nombres de usuario no distinguen mayúsculas (el servidor los pasa a mayúsculas).

Éxito, **200**:

```json
{ "P_OUT_ENTIDAD": "", "P_OUT_TOKEN": "<token>", "P_OUT_ADVERTENCIASCONTADOR": 0, "P_OUT_ERRORESCONTADOR": 0,
  "P_OUT_ADVERTENCIASDETALLE": [], "P_OUT_ERRORESDETALLE": [] }
```

Guarde `P_OUT_TOKEN` donde la UI guarda hoy el token de sesión y envíelo en `X-SESSION-TOKEN`.

Rechazo, **401**, **siempre el mismo cuerpo** sea cual sea la causa:

```json
{ "P_OUT_ERRORESCONTADOR": 1, "P_OUT_ERRORESDETALLE": [ { "CODIGO": "USUARIO_CLAVE_INCORRECTA", "Descripcion": "Usuario o contrasena incorrecta." } ] }
```

La UI **no puede** distinguir clave incorrecta, usuario inexistente, usuario bloqueado, cuenta vencida, canal no autorizado o JSON inválido: es deliberado (no revelar si el usuario existe). Muestre un mensaje genérico (`BCS_DICCIONARIO.USUARIO_CLAVE_INCORRECTA` si existe).

Reglas del servidor que la UI debe conocer: cada clave incorrecta suma un intento; al llegar al máximo configurado el usuario queda **bloqueado temporalmente** unos minutos y durante ese tiempo incluso la clave correcta da 401. Hay **una sola sesión activa por usuario**: iniciar sesión en otro lugar invalida la anterior (el canal `WS` reutiliza la existente). Un cliente (`CLI`) bloqueado no puede entrar (corrección aprobada respecto al legacy).

### 3.3 `POST /PLA/bootstrap/home`: datos de la sesión

Cabecera `X-SESSION-TOKEN`, **sin cuerpo**. Éxito **200** con todas las claves de la sección 4 llenas. Usa el token (cuenta un uso y marca al usuario como conectado).

| Código | Cuándo |
|---|---|
| 200 | Sesión válida. |
| 400 | Falta la cabecera `X-SESSION-TOKEN`. |
| 401 | El token no existe o ya no vale, o el usuario está bloqueado/vencido o la empresa está mal configurada. Con la sesión ya identificada, el cuerpo es `{ "P_OUT_ERRORESCONTADOR": n, "P_OUT_MENSAJES": [ { "Codigo": "..." } ] }`. Vuelva a la pantalla de login. |
| 500 | Falla del servidor. |

Ejemplo recortado: `USO_LOGIN_anexos/bootstrap-home.ejemplo.json`.

### 3.4 Clave provisoria y cambio de clave (no llevan token)

Estos endpoints se pueden llamar **sin sesión** (como en el legacy), salvo `ActualizarClave`, que **exige sesión** (ver su fila). Los mensajes de texto salen del diccionario de la base en español; si el diccionario no tiene el texto, la respuesta es **200 con cuerpo vacío**, así que la UI debe tolerarlo.

| Endpoint | Cuerpo | Respuesta |
|---|---|---|
| `POST /PLA/ValidaClaveProvisoria` | `{ "p_CodigoUsuario": "U", "p_ClaveProvisoria": "<clave en texto>" }` | 200 `{ "Estado": "OK"\|"NOK", "status": "<texto>" }`; textos: `CLAVE_PROVISORIA_VALIDA`, `CLAVE_PROVISORIA_INVALIDA`, `CLAVE_PROVISORIA_VENCIDA`, `SUPERA_INTENTOS_MAXIMOS`. Usuario inexistente: 200 **vacío**. |
| `POST /PLA/ActualizarClave` (**con sesión**: cabecera `X-SESSION-TOKEN`; sin ella 400, token inválido 401, y la sesión debe ser del mismo usuario `p_CodigoUsuario` o responde 403) | `{ "p_CodigoUsuario": "U", "Clave1": "<nueva en texto>", "Clave2": "<confirmación>" }` | 200 `{ "Estado": "OK", "status": "<contraseña actualizada>" }`; `NOK` si están vacías o no coinciden; si no cumple la política: `{ "Estado": "NOK", "status": ["La contraseña debe contener:", "- criterio", ...] }` (**ojo: `status` es una lista aquí**). |
| `POST /PLA/RecuperacionClave` | `{ "p_CodigoUsuario": "U", "p_CanalSistema": "OPTIMUS" }` (`OPTIMUS`, `AGENTE` o `CLIENTE`) | 200 `{ "Estado": "OK", "status": "CLAVE ENVIADA" }` (envía por correo una **clave provisoria**) \| 401 `{ "P_OUT_ERRORESCONTADOR": 1, "P_OUT_ERRORESDETALLE": [ { "CODIGO": "ERROR_RECUPERAR_CLAVE", ... } ] }`. |
| `POST /PLA/SolicitudRecuperacionClave` | `{ "p_IdEmpresa": 1, "p_Username": "U" }` | 200 `{ "statusCode": "0", "statusMessage": "OK", "recoveryTo": "j*****@e*****.cl" }` (correo ofuscado al que se envió un **token**) \| 200 `{ "statusCode": <n>, "statusMessage": "ERROR_RECUPERAR_CLAVE" }`. |
| `POST /PLA/ConfirmarCambioClave` | `{ "p_IdEmpresa": 1, "p_Username": "U", "p_Password": "<sha256 hex minúsculas de la nueva>", "p_Token": "<token del correo>" }` | 200 `{ "statusCode": "0", "statusMessage": "OK" }` \| 200 `{ "statusCode": <n>, "statusMessage": "ERROR_CONFIRMAR_CLAVE" }`. |

Hay **dos caminos** de recuperación, ambos soportados:

1. *Clave provisoria:* `RecuperacionClave` → el usuario recibe una clave provisoria por correo → `ValidaClaveProvisoria` → `ActualizarClave`. **Como `ActualizarClave` ahora exige sesión, este camino no puede terminar solo**: tras validar la provisoria el usuario no tiene sesión. Mientras no se defina cómo se obtiene (por ejemplo, entrar con la provisoria y cambiar la clave ya con sesión), use el camino 2 para quien olvidó su clave; `ActualizarClave` queda para el cambio de clave con la sesión abierta.
2. *Token por correo:* `SolicitudRecuperacionClave` → el usuario recibe un token → `ConfirmarCambioClave`.

Atención con **qué se envía hasheado**: `CrearSesion.p_ClaveUsuario` y `ConfirmarCambioClave.p_Password` van como SHA-256 hexadecimal en minúsculas (esta última, probada en QA el 26-oct: con el hash en minúsculas de la misma clave el procedimiento la deja intacta y el login siguiente entra). `ValidaClaveProvisoria`, `ActualizarClave` y la clave provisoria van **en texto**, porque el servidor valida su composición (la política exige, por ejemplo, dígitos y no estar en la lista de palabras prohibidas) y es él quien la hashea.

## 4. El JSON del bootstrap (`_contrato`: `optimus-bootstrap`, versión 1)

Cada clave es el nombre de la variable que la página legacy definía. En la UI: `const BCS_X = json.BCS_X;` (o equivalente) antes de arrancar Angular. Tipos y contenido reales, observados en QA:

| Clave | Tipo | Contenido |
|---|---|---|
| `_contrato` | objeto | `{ "nombre": "optimus-bootstrap", "version": 1 }`. Compruébelo al cargar. |
| `CONF_LOCALE` | texto | Idioma, `"CL"`. |
| `CONF_IMAGEN_FONDO` | lista de textos | Imágenes de fondo del login. |
| `BCS_DICCIONARIO` | objeto | ~23.000 pares `CODIGO` → texto y `CODIGO_ABR` → abreviatura. |
| `BCS_SESION` | objeto | Vacío en el login. En el home: `IdEmpresa`, `IdTokenSesion` (siempre `null`: el token es el que ya tiene), `IdUsuario`, `NombreEmpresa`, `NombreUsuario`, `CodigoMenuUsuario`, `IdSocioDeNegocio`, `IdPersonaCorredora`, `IdMonedaSistema`, `IdPaisSistema`, `FechaSistema`, `BolsaDefectoSistema`, `ValoresDefectoSistema` (lista de `{ IdValorDefectoSistema, Nombre, Valor, IdEmpresa, IdEstado }`). |
| `BCS_OFFSET` | número | Segundos de la zona horaria (ej. `-10800`). |
| `BCS_VERSION` | número | `0` en el login; versión del sistema en el home. |
| `BCS_ACCESOS` | objeto | Vacío en el login. En el home: `ADMIN` (booleano) y, por cada elemento HTML, `{ IdElementoHtml, Existe, EsVisible }` (`"S"`/`"N"`). Son ~1.300 elementos. |
| `BCS_PREFERENCIAS` | objeto | Preferencias del usuario, claves `"<empresa>::<usuario>::<nombre>"` → texto. |
| `BCS_PREFERENCIAS_GRILLA` | objeto | Plantillas de grillas, clave `"<empresa>::<grilla>"` → JSON como texto. |
| `BCS_AMBIENTE` | texto | `QA`, `DES`... |
| `BCS_TIMESTAMP_LOCAL` | texto | Hora local del servidor (`"yyyy-MM-dd HH:mm:ss"`); vacío en el login. |
| `BCS_MUESTRA_SSO_GOOGLE` | texto | Botón de SSO de Google: `"S"`, `"N"` o el texto `"null"` (siempre `"null"` en el home), igual que el legacy. |
| `bcs_feriados` | objeto | `idPais` → `fecha` → `{ Tipo, Dia, DescripcionDia, AgnoCarga }`. |
| `BCS_OPERACION_INSTRUMENTO` | lista | `{ AccionOrden, IdTipoInstrumento, IdTipoOperacion, EsVentaCorta, EsPutEsCall, IdTipoProducto }`. Objeto vacío en el login. |
| `BCS_ESTADO_MODULO` | objeto | Hoy siempre vacío. |
| `BCS_CONFIGURACION` | objeto | Configuración pública por módulo (`ORDENES`, `PRODUCTO`, ...) → parámetro → valor. |
| `BCS_OFFLINE` | booleano | Siempre `true`. |
| `bcs_formato_cantidad`, `_precio`, `_tasa`, `_monto`, `_tipoCambio`, `_paridad`, `_puntos` | objeto | Formatos numéricos: `uso` → `moneda` → `tipoInstrumento` → `tipoOperacion` → `{ <Campo>Decimales, <Campo>TrucaRedondea }`. Solo en el home. |
| `bcs_filtrocombobase` | objeto | Filtros de combos por controlador: `controlador` → `ComboBox` → `nombre` → filtro (JSON como texto). Solo en el home. |
| `BCS_USER_MENU` | texto | Nombre del menú del usuario **en mayúsculas** (`"BCS_FULLMENU"` si no tiene uno). Es la **clave** del menú que la UI debe tener, no el menú mismo. |
| `empresas`, `logo` | listas | Extras del login y el home, ver 3.1. |

Si el diccionario, los formatos o los accesos pesan demasiado para su gusto, se puede pedir otro endpoint más fino; hoy el contrato es una sola respuesta por pantalla, como el legacy.

## 5. Ejemplo mínimo de un loader

```js
const BASE = '/BSCL/Plataforma/GWJAVA/PLA';

async function sha256Hex(texto) {
  const h = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(texto));
  return [...new Uint8Array(h)].map(b => b.toString(16).padStart(2, '0')).join('');
}

function definirVariables(json) {            // las mismas variables globales que escribía la página legacy
  for (const [k, v] of Object.entries(json)) if (k !== '_contrato') window[k] = v;
}

async function paginaDeLogin() {
  const r = await fetch(`${BASE}/bootstrap/login`);
  const json = await r.json();
  if (json._contrato?.nombre !== 'optimus-bootstrap') throw new Error('contrato inesperado');
  definirVariables(json);                     // luego, arrancar Angular
}

async function iniciarSesion(idEmpresa, usuario, clave) {
  const r = await fetch(`${BASE}/CrearSesion`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ p_IdEmpresa: idEmpresa, p_NombreUsuario: usuario, p_ClaveUsuario: await sha256Hex(clave), p_IdCanal: 'COR' })
  });
  if (r.status !== 200) return null;          // 401: mensaje genérico, siempre el mismo
  return (await r.json()).P_OUT_TOKEN;
}

async function paginaDeHome(token) {
  const r = await fetch(`${BASE}/bootstrap/home`, { method: 'POST', headers: { 'X-SESSION-TOKEN': token } });
  if (r.status === 401 || r.status === 400) return volverAlLogin();
  definirVariables(await r.json());
}
```

## 6. Qué no necesita token

`CrearSesion`, `bootstrap/login`, `ValidaClaveProvisoria`, `RecuperacionClave`, `SolicitudRecuperacionClave`, `ConfirmarCambioClave`, `Ping`, `Configuraciones`, `CallBatch`, `AuditoriaWS`, `LoginCliente`, `WebStatic`, `RepoStatic`, `CallWebsocket` y los `SSOInit`/`SSOLogon*`/`SSOLogout`. **Todo lo demás pide `X-SESSION-TOKEN`**, incluso una ruta sin el prefijo `/PLA` (decisión del 26-oct: el filtro está cerrado por defecto).

## 7. Qué está probado y qué no

- **Igual al legacy, comprobado en QA con un usuario real y solo lectura:** `bootstrap/home` y `bootstrap/login` devuelven las mismas constantes que las páginas del legacy (20 JSON y 5 textos en el home; todas las del login). Difieren solo el reloj (`BCS_TIMESTAMP_LOCAL`) y el texto `null` de `BCS_MUESTRA_SSO_GOOGLE`.
- **Probado contra el legacy real en QA con un usuario de prueba (26-oct), mismas respuestas y mismos cambios en la base:** `CrearSesion` con clave buena y mala (los cinco intentos, el bloqueo de 15 minutos y el rechazo de la clave buena mientras dura), `SolicitudRecuperacionClave` + `ConfirmarCambioClave` (token bueno y malo), `RecuperacionClave` y `ValidaClaveProvisoria`, y los rechazos de `ActualizarClave`. Única diferencia conocida: la lista de requisitos de `ActualizarClave` trae un renglón más, "Al menos 1 dígito" (regla aprobada).
- **Solo con pruebas automáticas (H2):** el filtro de sesión cerrado por defecto y los canales distintos de `COR`. `ActualizarClave` que **sí cambia** la clave no se ejecutó contra QA (se probó solo cuando rechaza).
- **No cubierto:** menús y ventanas (UI), SSO, y las llamadas `/PLA/call` (fuera del MVP de login).

## 8. A quién preguntarle

Dudas del contrato, nuevos campos o un endpoint más fino: el equipo del core (este módulo). El destino de la UI en el Ingress y el origen (dominio) los define infraestructura al desplegar en EKS (`BcsGateway/deploy/eks/mvp-login.yaml`).
