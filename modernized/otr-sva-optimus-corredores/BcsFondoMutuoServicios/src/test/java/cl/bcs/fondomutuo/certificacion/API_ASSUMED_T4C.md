# API assumed by the F3 W3 T4-C tests (make Certificar and the in-process AR REACHABLE, everything closed by default)

The production code of T4-C does NOT exist yet (red before code). The tests (`ApiT4c`, `CerT4cHandlerTest`, `CerT4cEquivalenciaTest`, `CerT4cCableadoTest`, `CerT4cEstructuraTest` in
`cl.bcs.fondomutuo.certificacion` and `CerT4cAdaptadorArTest` in `cl.bcs.fondomutuo.aportereinversion`) COMPILE without it: the new classes and constants are reached by reflection in `ApiT4c` (the only
file that knows their names) and every test that needs them fails with `AssertionError: T4-C not implemented: the production class ... does not exist yet`.

User-approved plan (2026-11-02): the facade of Certificar (T5) and the in-process AR (T4-A) exist as services but nothing SERVES them. T4-C makes them reachable by the router.
Decisions of the user: NO JavaCall handler for the AR (0 direct uses), NO Manejador destination `REINVERSION_APORTE` (deferred), `ProcesarCarga` out of scope.
No new oracle: the handler adds only the JSON codec; the goldens of T5 (`fondomutuo-cer/cases-t5`, 206 cases) and of CSR (`fondomutuo-csr/cases`, 224 cases) are the oracle.

## 1. Classes and constants

| Item | Package | Shape the tests use |
|---|---|---|
| `JavaCallDispatcher.CERTIFICAR_SOLICITUD_APORTE_RESCATE` | `adapter.in.javacall` | `public static final String` = `"cl.bcs.fondomutuo.CertificarSolicitudAporteRescate"`; added to `LISTA_BLANCA` (12 -> 13); NOT in `CLASES_LECTURA` (stays 8) |
| `CertificarAporteRescateJavaCallHandler` | `certificacion` | `public`, `@Component`, `implements JavaCallHandler`, ONE public constructor `(CertificarAporteRescatePort servicio, Compuertas compuertas)`, final fields only. `fqn()` = the constant above |
| `CertificarAporteRescateConfiguracion` | `certificacion` | `@Configuration`; `public static final String LISTA = "CERTIFICAR_SOLICITUD_APORTE_RESCATE"`; `@Bean CertificarAporteRescatePort` built with `CertificarAporteRescateFabrica.crear(Dependencias)` (style of `RescateReinversionConfiguracion`); the fail-closed replacements of 3 |
| `ManejadorConfiguracion.LISTAS_ESCRITURA_UI` | `manejador` | gains `CertificarAporteRescateConfiguracion.LISTA` (next to `CARGAR_VALOR_CUOTA`; size 2). `LISTAS_LECTURA` is unchanged (8) |
| `AporteReinversionJsonAdapter` | `aportereinversion` | `public`, `implements certificacion.CrearAporteReinversionPort`, ONE public constructor `(AporteReinversionPort)`, final fields only |
| `AporteReinversionConfiguracion` | `aportereinversion` | `@Configuration`; `@Bean AporteReinversionPort` built with `AporteReinversionFabrica.crear(Dependencias)` with the same ports as `RescateReinversionConfiguracion` / the harness `SujetoCsr` |

## 2. The handler (`call(Connection, JsonObject)`, legacy C:360-368)

1. FIRST statement: `if (!compuertas.listasServidas().contains(CertificarAporteRescateConfiguracion.LISTA)) throw new ServicioNoServidoException("G0", ...)`: before the input is read (the tests pass a `null` JSON), before the
   service runs, no connection used (the tests pass a `null` connection). Models: `ConsultarCustodiaPortafolioJavaCallHandler`, `NormalJavaCallHandler`.
2. Plain `new Gson()` in and out (`JsonLegacy.plano()`): `Entrada` read from `in.toString()` (absent field = null, the defaults of the class, unknown keys ignored); `Salida` written with NO null keys.
   `CertificarAporteRescateJson.gson()` is the same Gson. The input `JsonObject` is not modified.
3. `servicio.logica(entrada)`; whatever it throws goes up UNTOUCHED (same instance: runtime, checked, `ServicioNoServidoException`, `Error`).
4. It WRITES: not in `CLASES_LECTURA` (its unit of work is read-write).
5. RULE-076: only `call` is ported, never the legacy `main`.

## 3. Wiring (default configuration)

* The bean `CertificarAporteRescatePort` (ONE) is the facade `CertificarAporteRescateFachada`, built from the adapters that exist: `CertificarAporteRescateDatosJdbc`, `...EscrituraJdbc`, `...EscrituraMontoJdbc`,
  `...EscrituraCierreJdbc`, `Compuertas`, `LogPort`, `FlujoCajaPort`, `ParidadPort`, `PreciosTasasPort`, `ConfiguracionSistemaPort`, `DiccionarioMensajesPort`, `CalendarioPort`, `SeriesFondoPort`, `CuotasPort`,
  `SecuenciaPort`, `CalcularComisiones`, `FormatoMoneda`, the typed ports of Tesoreria and Spot (`ProcesosExternos.TesoreriaPort`, `SpotPort`) when beans exist.
* Ports WITHOUT a production implementation are wired with a replacement that FAILS CLOSED: `ProcesarCustodiaFondoMutuoPort`, `CalculaCustodiaRescatePort`, `CrearCargoAbonoPort`, `FacturacionPort`,
  `NotificacionCertificacionPort`, `NotificacionOrdenesOwpPort`. Every method raises `ServicioNoServidoException` before reading its argument (the tests call each method with `new JsonObject()` / an empty list / null).
  The tests assert ONE bean of each type exists (see Q-T4C-2).
* `CrearAporteReinversionPort` (ONE bean) is the `AporteReinversionJsonAdapter` over the bean `AporteReinversionPort`; the AR keeps its own gates (G1, G2, G3, G-CUS).
* With the default configuration every request that gets past the early exits of F-A / F-B answers 503 (G8 is evaluated first and is closed): only the early rejections are served. The HTTP entry answers 503 for the
  class while its list is closed (the controller maps `ServicioNoServidoException` to 503).

## 4. The adapter of the AR (`crear(JsonObject)`)

JSON -> `AporteReinversionContrato.Entrada` (`JsonLegacy.plano()`), `AporteReinversionPort.logica`, `Salida` -> JSON (`JsonLegacy.plano()`): exactly what `ApiCsr.llamar` does. It holds no state, calls the port exactly
once, does not modify its input and lets whatever the port throws out UNCHANGED (same instance, checked exceptions included: see Q-T4C-1).

## 5. Harness changes (no existing assertion changed)

* `CasoCerT5.Opciones.porHandler()` (field `porHandler`, default false): the request goes through `ApiT4c.llamarPorHandler` (the handler with its list open, `call(null, json)`) instead of `CasoCer.llamar`.
* `CasoCsr.ejecutarPorAdaptador(id, perfil)`: the case goes through the adapter (`ApiT4c.llamarPorAdaptador`) instead of `ApiCsr.llamar`. Both rethrow `T4-C not implemented` (it is never an observation of the legacy).

## 6. Open questions (each with a recommendation)

* **Q-T4C-1 (checked exceptions through `CrearAporteReinversionPort.crear`).** The port declares no `throws`, the real AR throws `Exception` (SQL errors, plain `Exception`) and the goldens compare the exception CLASS.
  Recommendation: make the port `JsonObject crear(JsonObject) throws Exception` (the facade's F-I unit already declares `throws Exception`); the alternative (sneaky throw in the adapter) is pinned by the same tests but is
  uglier. The tests do NOT force one: they assert the same class and the same instance.
* **Q-T4C-2 (are the fail-closed replacements beans?).** The plan says "via `ObjectProvider.getIfAvailable(...)`"; the tests assert beans exist (ONE per type). Recommendation: `@Bean @ConditionalOnMissingBean` methods in
  `CertificarAporteRescateConfiguracion` returning the replacement, so a later real adapter replaces it by just being a bean. If you prefer no beans, change `losPuertosSinImplementacionFallanCerrado` (one test).
* **Q-T4C-3 (replacement classes vs lambdas).** The T2 / T3 / T4 tests `sinImplementacionDeProduccion...` scan `cl.bcs.fondomutuo` for ANY concrete class implementing these ports; a NAMED replacement class (or the new
  adapter for `CrearAporteReinversionPort`) makes them fail. Recommendation: lambdas inside the `@Bean` methods for the single-method ports (anonymous: no scan hit); `FacturacionPort` has two methods and `CrearAporteReinversionPort` gets a named adapter,
  so those need an exclusion in the scan tests, applied with your explicit approval as Q-T5-8 did.
* **Q-T4C-4 (the handler's `Compuertas`).** The handler reads the list from the same `Compuertas` bean as the other handlers. The gate G8 and the others stay in the facade (they are evaluated after F-A / F-B).
* **Q-T4C-5 (list-open start-up).** With the list open and the default gates every non-early request is 503: an operator may open the list and see nothing served. Recommendation: log one WARN at start-up when the
  list is open and G8 is closed (not tested).
* **Q-T4C-6 (case count of CSR).** The task says 226 CSR cases; the directory holds 224 `case.json`. The tests compare ALL the directories found (>= 220 guard) and print `N of M`.
