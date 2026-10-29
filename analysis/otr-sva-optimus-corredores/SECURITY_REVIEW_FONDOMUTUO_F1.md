# Revision de seguridad: FondoMutuo F1 (BcsFondoMutuoServicios, commit efe886f0)

Fecha: 2026-10-22. Revision estatica de solo lectura (agente `security-auditor`); no se ejecuto codigo ni escaner de dependencias/CVE. Resultado: **0 CRITICAL, 1 HIGH, 4 MEDIUM, 5 LOW; sin inyeccion SQL**.

| Id | Sev | CWE | Hallazgo | Estado |
|---|---|---|---|---|
| SEC-001 | HIGH | 1327/284 | El compose publica `8111:8111` de un servicio interno; el token es la unica barrera y permite suplantar `p_IdEmpresa`/`p_IdUsuario`. Aplica a los 25 servicios que publican puertos en `docker-compose.yml`. | Decision del usuario pendiente (acotar a 127.0.0.1 en todo el compose o quitar `ports:`) |
| SEC-002 | MEDIUM | 639/287 | La identidad la trae el llamador; el router (`JavaCallRouterService:183-185`) solo la sobreescribe si la clave ya viene. Un titular del token interno puede suplantar. Legacy: `CallServlet` hacia lo mismo. | Decision pendiente: validar no nulo/positivo (400) = desviacion; token propio por servicio |
| SEC-003 | MEDIUM | 209/200 | `config/GlobalExceptionHandler` devuelve `ex.getMessage()` y la causa JDBC (host/driver) en errores fuera del cuerpo del controlador (`DataSourceUtils.getConnection` esta fuera del try). Contradice DEV-F3. | Corregir |
| SEC-004 | MEDIUM | 770/400 | Sin limites de tamano de cuerpo, profundidad JSON ni longitud de listas; IN-list O(n x len(sql)). | Corregir (tope de cuerpo, tope de listas, lectura con limite) |
| SEC-005 | MEDIUM | 362 | Operacion de dinero no repetible sin clave de idempotencia; timeout de lectura de 60 s del cliente puede ver timeout mientras el servidor confirma, y un reintento duplica. El bloqueo no atomico es legacy aceptado; la ausencia de idempotencia es nueva. | Recomendacion para el corte global (no se implementa ahora: seria diseno nuevo) |
| SEC-006 | LOW | 117 | Log de `ex.getMessage()` en el handler global puede traer valores de la peticion. | Corregir junto con SEC-003 |
| SEC-007 | LOW | 693 | `actuator/info` publico. | Quitar `info` o fijar show-details=never |
| SEC-008 | LOW | 200 | Comparacion de token: `isEqual` filtra solo la longitud. | Sin accion |
| SEC-009 | LOW | 1104/250 | Imagen base sin digest fijo, Maven por apk sin version, sin HEALTHCHECK ni `-XX:MaxRAMPercentage`, compose sin `read_only`/`cap_drop`. | Endurecer |
| SEC-010 | LOW | 532/798 | `QA_ACCESS_PROCEDURE.md:40` contiene una contrasena QA en claro (fuera del modulo). El modulo no copio credenciales del legacy (verificado por busqueda). | Rotar y sustituir por marcador: pendiente del usuario |

## Controles que se verificaron correctos
- Inyeccion SQL: ninguna; `SqlNombrado` enlaza todo por `PreparedStatement`; las dos `Statement` crudas ejecutan SQL constante.
- Lista blanca fija de 2 FQN, sin reflexion sobre entrada del llamador; handler fuera de la lista detiene el arranque.
- Filtro de token: fail-closed, comparacion en tiempo constante, normalizacion de `//`, `;x=1`, `%xx` repetido, `.`/`..`, sin elusion encontrada; el filtro de unidad de trabajo usa la misma normalizacion.
- Transaccion: commit solo con 2xx y contador 0; respuesta bufferizada hasta decidir.
- `RouterClient`: URL solo de `JAVACALL_URL`, sin SSRF; el token solo va a esa base; timeouts acotados.
- Sin credenciales en `application.yml`/compose (todo `${...}`); `INTERNAL_TOKEN` obligatorio; compuertas cerradas por defecto.

## Decisiones del usuario (2026-10-22)
- **SEC-001:** usar el nombre del servicio/contenedor y no loopback: los 24 servicios internos del compose del repo dejan de publicar puertos (`expose`, acceso por nombre en `optimus-net`); solo `bcs-gateway` (9090) queda publicado (commit a044d1aa). El compose superior ya solo publica TomEE y los frontends.
- **SEC-002:** se mantiene tal como viene del legacy (el modulo confia en la identidad que trae el llamador; el router/gateway es responsable de inyectarla). No se valida ni se cambia en el modulo.
- **SEC-010:** se deja (es QA y no afecta); en el peor caso, pasarla a variable en los archivos env. Sin accion ahora.
- SEC-005 (idempotencia): requisito del corte global, no se implementa ahora.
- SEC-003, 004, 006, 007 y 009 se corrigen sin cambiar comportamiento legacy servido.
