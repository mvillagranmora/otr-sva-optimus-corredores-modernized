# BcsPlataformaServicios — Transformation Notes

## Legacy → Modernized

| Item | Legacy | Modernized |
|------|--------|-----------|
| Runtime | Java EE 8 / TomEE | Spring Boot 4.1.1 / Java 25 |
| Pattern | JavaCall interface + JSON dispatch | Hexagonal architecture (domain/service/port/adapter) |
| Database | SAP HANA (direct Connection) | NamedParameterJdbcTemplate + MapSqlParameterSource |
| Port | (embedded in TomEE) | 8114 |
| Tests | None | 46 tests (34 unit + 12 integration), BUILD SUCCESS |

## Legacy Module Inventory

**Total: 432 Java files across 25 packages**

| Package | Files | Domain | Status |
|---------|-------|--------|--------|
| `cl.bcs.plataforma.com` | 101 | Commercial (cobros, perfiles, matriz operacional) | Stub → `ComercialController` |
| `cl.bcs.plataforma.pla` | 81 | Platform core (params, dates, logs, monitor, dictionaries) | Stub → `PlataformaController` + Pure logic |
| `cl.bcs.plataforma.pau` | 35 | Valuation schedule (pauta, cartera) | Stub → `PautaController` |
| `cl.bcs.plataforma.ins` | 22 | Instruments (clasificacion, vectores, market maker) | Stub → `InstrumentoController` |
| `cl.bcs.plataforma.acc` | 22 | Access/users (usuarios, roles, perfiles, sesiones) | Stub → `AccesoController` |
| `cl.bcs.plataforma.lim` | 21 | Limits | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.mer` | 20 | Market (paridad, precios, deterioro emisor) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.aut` | 19 | Authorization workflow (eventos, etapas) | Stub → `AutorizacionController` |
| `cl.bcs.plataforma.evt` | 14 | Events (calendario eventos) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.enc` | 14 | Surveys (encuestas, respuestas, puntajes) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.ccl` | 14 | Conciliation (facturación, FFMM, mesa RF) | Stub → `ConciliacionController` |
| `cl.bcs.plataforma.val` | 11 | Validation | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.frm` | 11 | Forms (campos, validaciones) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.ctr` | 8 | Contracts | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.tas` | 7 | Tasks (tareas, agenda) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.doc` | 7 | Documents | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.ind` | 6 | Indicators | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.bat` | 6 | Batch (programas, agenda) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.demo` | 4 | Demo/examples | Not migrated (dev tooling only) |
| `cl.bcs.plataforma.cre` | 3 | ? | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.gta` | 2 | ? | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.bck` | 1 | Blockchain (login) | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.sap` | 1 | SAP integration | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.ftp` | 1 | FTP file transfer | Stub → `RemainingDomainsController` |
| `cl.bcs.plataforma.cua` | 1 | ? | Stub → `RemainingDomainsController` |

## Fully Implemented Services (with Tests)

### ConvertirMontoEscritoService (pure logic, 34 tests)
- `cantidadConLetra(String)` — converts numeric string to Spanish words
- `cantidadNumerosRomanos(String)` — converts number to Roman numerals (range 1-3999)
- `numerosPrimos(int)` — generates prime numbers up to max
- Legacy source: `cl.bcs.plataforma.pla.utils.ConvertirMontoEscrito`
- REST: `POST /api/plataforma/conversor-monto/letras`, `/romanos`, `/letras/batch`

## Stub Services (UnsupportedOperationException → HTTP 503)

All other 430+ legacy classes are stubbed as REST endpoints returning HTTP 503.
Each stub includes a comment: `// TODO: wire when HANA datasource is available`

## Architecture

```
cl.bcs.plataforma
├── PlataformaApplication.java          ← @SpringBootApplication (only main())
├── config/
│   ├── DataSourceConfig.java           ← HANA datasource (@Profile("!test"))
│   └── TestDataSourceConfig.java       ← H2 datasource (@Profile("test"))
├── domain/
│   ├── MensajeSalida.java              ← Standard message wrapper
│   └── RespuestaEstandar.java          ← Standard response envelope
├── service/
│   └── plataforma/
│       └── ConvertirMontoEscritoService.java  ← Pure static logic, no Spring
├── port/
│   ├── in/
│   │   └── ConvertirMontoEscritoUseCase.java
│   └── out/
│       ├── ParametroRepository.java    ← PLA.Parametro port
│       └── FeriadoRepository.java      ← PLA.Feriado port
├── adapter/
│   ├── in/rest/
│   │   ├── GlobalExceptionHandler.java ← NPE→400, IAE→400, NFE→400, UOE→503
│   │   ├── ConvertirMontoEscritoController.java  ← Implemented
│   │   ├── PlataformaController.java   ← Stub (81 legacy classes)
│   │   ├── AccesoController.java       ← Stub (22 legacy classes)
│   │   ├── AutorizacionController.java ← Stub (19 legacy classes)
│   │   ├── ComercialController.java    ← Stub (101 legacy classes)
│   │   ├── PautaController.java        ← Stub (35 legacy classes)
│   │   ├── InstrumentoController.java  ← Stub (22 legacy classes)
│   │   ├── ConciliacionController.java ← Stub (14 legacy classes)
│   │   └── RemainingDomainsController.java  ← Stub (remaining domains)
│   └── out/jdbc/
│       ├── ParametroJdbcAdapter.java   ← NamedParameterJdbcTemplate (HANA stub)
│       └── FeriadoJdbcAdapter.java     ← NamedParameterJdbcTemplate (HANA stub)
```

## Key TODOs Before Production

1. **PLA.Servicio registry**: The legacy system has 1,817+ service registrations in PLA.Servicio
   that map JavaCall class names to HTTP routes. Once a DDL + SELECT * export is provided,
   wire the stub controllers to real JDBC implementations.

2. **ConvertirMontoEscritoService**: Verified against legacy behavior:
   - "VeintiUn " (capital U) — matches legacy UNIDADES[] array casing
   - "Millon " (without accent) — intentional (avoids encoding issues)

3. **HANA-specific services**: All services requiring HANA are annotated with
   `throw new UnsupportedOperationException("requires HANA — X not yet wired")`.

4. **Priority implementation order** (based on call frequency in PLA.Servicio):
   - `ConsultarParametros` → ParametroJdbcAdapter
   - `ObtenerDiaSiguiente`/`ObtenerDiaAnterior` → FeriadoJdbcAdapter
   - `InsertarLogServicio` → LogJdbcAdapter (create new)
   - `FormatearMonedaFormato` → MonedaFormatoJdbcAdapter (create new)
   - `ValidarClaveUsuario` → AccesoJdbcAdapter (create new)

## Test Results

```
Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- 12 integration tests (`PlataformaApplicationTest`) — Spring Boot context + MockMvc
- 34 unit tests (`ConvertirMontoEscritoServiceTest`) — pure static logic, no Spring

## Notes on Legacy Patterns

The legacy codebase uses the `JavaCall` interface:
```java
public interface JavaCall {
    JsonObject call(Connection con, JsonObject jin) throws Exception;
}
```

PLA.Servicio acts as a service registry mapping service names to implementing classes.
Each class receives a `Connection` (from TomEE data source pool) and a `JsonObject` request.

The modernized version replaces this with:
- Spring REST controllers (adapter/in/rest)
- NamedParameterJdbcTemplate for DB access (adapter/out/jdbc)
- Port interfaces for dependency inversion
- Pure static methods in @Service classes for testable business logic

---

## Bloque 1, ola A (alcance acordado: SOLO `cl.bcs.plataforma.cre.ConsultarEstadoModulo`, JavaCall 600017) - 2026-10-22

Estado: **implementado y probado sobre H2; NO PROVEN, sin trafico real** (el router de `BcsPlataformaWeb` no se toco; `clases-habilitadas` sigue vacio; paso 9 pendiente). Las otras tres clases de la ola A (CalcularParidad, ConsultarFlujoCaja, ConsultarMatrizOperacional) quedan aplazadas. Las 326 clases stub siguen en el arbol, pero **fuera del contexto de Spring** (D-4 / DEV-A1). Reglas: `analysis/otr-sva-optimus-corredores/RULES_PLATAFORMA_ESM.md` (PLA-ESM-01..11, D-ESM-1..11, Q-ESM-1..6; ids provisionales).

### Mapa legacy -> nuevo (LOC reales de los archivos)

| Legacy (`ConsultarEstadoModulo.java`, 124 LOC) | Nuevo |
|---|---|
| `:22-23` SELECT inline con `:IdEmpresa` | `adapter/out/jdbc/ModuloJdbcRepository.java` (66 LOC): constante textual identica; `:IdEmpresa` -> `?`; `setObject`, o `setNull(VARCHAR)` si es null (igual que `NamedParameterStatement:203`). No se construyo un ayudante general de parametros nombrados (no hace falta para esta clase) |
| `:28-57` POJOs `Modulo`, `InConsultarEstadoModulo`, `OutConsultarEstadoModulo` | `cre/ConsultarEstadoModuloService.java`: `record Entrada`, `Modulo`, `Salida` (mismo nombre de campo = misma clave JSON y mismo orden). Se omiten `p_out_AdvertenciasDetalle`/`p_out_ErroresDetalle` (siempre null, nunca se emitian) |
| `:61-69` `call(con, jin)` con `new Gson()` plano | `ConsultarEstadoModuloService.call(JsonObject)`: mismo `new Gson()` (sin `serializeNulls`: el golden fija que no salen claves nulas; el `getGsonBuilder()` de `JavaCall` NO se usa en la clase) |
| `:109-123` `logica` | `ConsultarEstadoModuloService.logica` + puerto `port/out/ModuloRepository` (1 metodo) |
| `:71-104` `main()` con host/usuario/clave HANA | **no se porta** (RULE-076, D-ESM-10) |
| `PluginLoader` instancia por llamada + `JavaCall.call(Connection, JsonObject)` | `adapter/in/javacall/JavaCallHandler` + `ConsultarEstadoModuloHandler` (una instancia de servicio y repositorio por llamada; Java puro, sin Spring) |
| `CallServlet:441-456,495` commit/rollback | `adapter/in/javacall/JavaCallUnitOfWorkFilter` (una conexion/transaccion por llamada; COMMIT solo con 2xx y `P_OUT_ERRORESCONTADOR` = 0; sin contador, contador > 0, 503 o 500 = ROLLBACK). Compara la ruta normalizada (cubre `//internal/java-call` y `;x=1`) |
| (no existia) | `adapter/in/javacall/TokenInternoFilter` (falla cerrado, lista blanca de rutas publicas solo `/actuator/health|info`), `JavaCallDispatcher` (lista blanca = los `JavaCallHandler`: 1 clase), `InternalJavaCallController` reescrito (400/401/503/500 generico), `config/JavaCallTransaccionConfig`, `PlataformaApplication` con `@ComponentScan` que excluye todo `JavaCallService` |

Configuracion nueva: `services.internal-token: ${INTERNAL_TOKEN:}` en `application.yml` (vacio = rechaza todo). Los endpoints REST tipados existentes (`/api/plataforma/**`) tambien quedan detras del token en ejecucion real (lista blanca fail-closed); los tests existentes no usan filtros. Typed REST propio: aplazado (D-2).

### Desviaciones

| Id | Descripcion | Estado |
|---|---|---|
| DEV-A1 / D-4 | 326 stubs fuera del contexto (exclusion por tipo en el component scan; no se borran). Probado: `PlataformaCableadoRealTest.ningunStubEstaEnElContexto_D4`, y una clase fuera de la lista blanca responde 503 (antes 200 con datos falsos) | aprobada |
| DEV-A3 | Excepcion -> 500 generico `{"error":"internal error","correlationId":...}`, sin texto de la excepcion, SQL ni parametros (el legacy devolvia `{CODIGO,PILA,SP}` con traza y SQL si `PLA.MuestraLog='S'`). El golden 29 fija el texto legacy `... Query: <SQL> Parameters: [{IdEmpresa=1}]`: es una **diferencia aprobada** (ver equivalencia). Q-ESM-6 queda resuelta por esta aprobacion | aprobada |
| DEV-A5 | `/internal/java-call` exige token interno y lista blanca (el legacy no tiene ese endpoint) | aprobada |
| (no es desviacion de comportamiento) | los errores de **lectura de la entrada** (`JsonSyntaxException` de `1.5`, `"abc"`, objeto en `p_IdModulo`) conservan tipo y texto en el servicio (goldens 15-18 identicos); lo que cambia es la respuesta HTTP (500 generico) | |

### Defectos mantenidos bug a bug (D-ESM-n, fijados por goldens)

D-ESM-1 (`p_IdModulo`/`p_IdUsuario` ignorados: casos 08-11, 19), D-ESM-2 (alias `NombreModulo` = `CodigoModulo`), D-ESM-3 (sin ORDER BY, orden = el de la BD: caso 27), D-ESM-4 (estado sin validar, `IFNULL` muertos con columnas NOT NULL reales: casos 23, 26 hipotetico), D-ESM-5 (error SQL = excepcion, no contador: 15, 16, 29), D-ESM-6 (empresa ausente = lista vacia: 06, 07, 12), D-ESM-7 (campos ignorados igual se validan por tipo: 17, 18). D-ESM-8, 9, 10, 11 estan fuera de la clase o no aplican (ver reglas). **Q-ESM-1 respondida por el lider** (consulta de catalogo de solo lectura, `hana_qa_results/cat_claves_pau_modulo.csv`): la PK de `PAU.Modulo` es `(IdEmpresa, CodigoModulo)`; por tanto el caso 25 (duplicados sin deduplicar, PLA-ESM-09) es **hipotetico** en HANA real (el esquema H2 del oraculo no declara la PK; no se cambio). Casos hipoteticos: 25 y 26.

### Pruebas (suite del modulo: 113 ejecutadas, 0 fallos, 0 omitidas)

46 existentes (34 `ConvertirMontoEscrito` + 12 de contexto, sin cambios) + 67 nuevas: `ConsultarEstadoModuloGoldenTest` (30: un test dinamico por cada uno de los 29 goldens + 1; imprime `equivalence cases executed (ConsultarEstadoModulo): 29`), `ConsultarEstadoModuloReglasTest` (11, uno por PLA-ESM-01..11), `PlataformaCableadoRealTest` (8: contexto real H2, ningun bean stub, lista blanca = 1 clase, 503 fuera de lista, 200 con JSON legacy, 401/400, 500 generico sin eco: DEV-A3), `JavaCallUnitOfWorkFilterTest` (6: commit/rollback reales con un manejador de prueba), `TokenInternoFilterTest` (5), `Rule076SinCredencialesNiMainTest` (4), `SinCambiosDeEsquemaTest` (2: el unico SQL es el SELECT legacy, sin DDL/DML/CALL), `ConsultarEstadoModuloLatenciaTest` (1, informativo). Los tests de casos leen el oraculo desde `analysis/.../equivalence/BcsPlataformaServicios` (fallan, no se omiten, si no lo encuentran).

### Equivalencia (oraculo del paso 1B contra el codigo nuevo)

`ORACLE_SUBJECT=cl.bcs.oracle.NewSubject` (fuente `analysis/.../BcsPlataformaServicios/new-subject/cl/bcs/oracle/NewSubject.java`, por reflexion sobre `ConsultarEstadoModuloHandler`; record.sh recibio la perilla opcional `ORACLE_EXTRA_CP` solo para el classpath de EJECUCION) mismas 29 carpetas, mismo H2 y mismo volcado.
- `compare.py` tal cual: **28 same, 1 differs** (caso 29, solo el texto de `excepcion`).
- `compare_new.py` (mi paso de comparacion, usa `compare.py` sin cambios + `approved_differences.json`; los goldens no se tocaron): **28 same, 1 approved, 0 differs, 0 missing** (`EQUIVALENCE_NEW.json`). Aprobada: PLA-ESM-29 campo `excepcion` (DEV-A3, Q-ESM-6), con condiciones: el resto del volcado identico, decision ROLLBACK_HTTP500, y el texto nuevo sin `Query:`/`Parameters:`.
- `self_check.py` del oraculo reejecutado: PASSED (19 controles `ok`: comparador, control positivo, 8 mutantes de sujeto, 8 mutaciones de fuente legacy).

### Canario (6 mutaciones del codigo nuevo, cada una con build limpio y restauracion verificada byte a byte)

Evidencia en `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsPlataformaServicios-transform/<n>-<area>/` (`mvn.log` + XML de surefire con los fallos). Todas se pusieron en rojo (ninguna sobrevivio): 1 `IFNULL ... 'ABT'` -> `'CER'` (2 tests rojos); 2 contador de errores 0 -> 1 (28); 3 token que falla abierto, quitar `esperado.length > 0` (1); 4 quitar la exclusion de stubs (1); 5 el filtro confirma siempre (3); 6 quitar el filtro por empresa (29). Plan pedia 8 mutaciones para toda la ola (D-6); para esta clase se hicieron 6.

### Latencia (informativa, no es prueba de rendimiento)

Handler sobre H2 en memoria, 5 modulos, 20.000 llamadas tras calentamiento, JDK 25: **p50 36 us, p99 124 us, max 1,7 ms, ~26,8 KB asignados por llamada**. Sin red ni HANA; sin cache (el legacy no tiene; no se agrego). A 1,77 M de llamadas la carga es trivial para el servicio; el costo real sera el viaje a HANA (no medido: HANA no se contacto). Falta medir con HANA de QA y bajo concurrencia antes de trafico real (R-01).

### Limites y pendiente

H2 solamente (dialecto, orden de filas, mensajes del driver, tipos); servlet/PluginLoader no se ejercitan; sin entradas frescas ni mutacion de goldens del codigo nuevo (paso 7); sin JaCoCo/lizard del nuevo codigo (D-6); sin revision de arquitectura/seguridad independiente (paso 8); R-05 (el despachador de `BcsPlataformaWeb` salta la sesion con `RegistraUso='N'`, el legacy no) sigue abierto y debe corregirse antes de habilitar `/call`. Hardening de contenedor del plan 5.1 (graceful shutdown, usuario no root, HEALTHCHECK) no se hizo: fuera de este alcance.
