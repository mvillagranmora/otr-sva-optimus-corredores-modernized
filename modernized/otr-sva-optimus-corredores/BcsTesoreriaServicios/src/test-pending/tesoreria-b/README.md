# Pending family B tests (not compiled) and the intended Step 2 API

Not compiled because the modern class they target is missing or has no ports yet. Goldens already exist (recorded from the legacy), so these are
specifications, not skips: when the API below exists, move the files to `src/test/java/cl/bcs/tesoreria/familia_b/` (package `cl.bcs.tesoreria.familia_b`) and they run.

| Test (pending) | Cases | Target |
|---|---|---|
| IngresarMandatoPACServiceTest | TES-B-97..119 (23) | `IngresarMandatoPACService` (missing: only the Manejador stub exists) |
| AnulacionMandatoPACServiceTest | TES-B-135..147 (13) | `AnulacionMandatoPACService` (missing) |
| RevocacionMandatoPACServiceTest | TES-B-148..159 (12) | `RevocacionMandatoPACService` (missing) |
| ProcesarDistribucionPreRendicionServiceTest | TES-B-309..318 (10) | `ProcesarDistribucionService#validaPreRendicion` (public legacy method `ValidaPreRendicion`; the service only has `execute`) |

## Intended ports (accepted by the user; package `cl.bcs.tesoreria.port.out`)
The compilable declarations are in `ports/` (src/main was NOT edited). Exact signatures:

```java
public interface Colaboradores        { JsonObject invocar(String servicio, JsonObject entrada); }      // ALWAYS the LAST constructor argument
public interface DiccionarioMensajes  { String mensaje(Long idEmpresa, String codigo, Map<String, String> valores); }
public interface ConfiguracionSistema { String parametro(Long idEmpresa, String modulo, String nombre); }   // null when not configured
public interface Secuencias           { long siguiente(String nombre); }
public interface Reloj                { Instant ahora(); }                                                 // pinned 2025-04-01T10:15:30Z in the cases
public interface AlmacenArchivos      { void escribir(String rutaRelativa, byte[] contenido) throws IOException; }   // file-output seam (CargaMasivaIngresoEgresoDAPFFMM CSV)
```
- `Colaboradores.invocar(servicio, entrada)`: `servicio` = legacy simple class name (RevisarRequisitosBasicos, CrearIngresoEgreso, LiquidarComprobante, CrearComprobante,
  CrearCargoAbono, AnularCargoAbono, CalcularParidad, CalcularMontoNumeroCuota, CrearSolicitudAporteRescateNormal, ConsultarInstrumentoPrecioBolsa,
  CalcularMontoYCobrosOrden, CrearOrdenes, AgregarPrograma, AgregarLog, CrearEventoAutorizacionMasivo, AnulacionContratoCSC, RevocacionContratoCSC,
  CambiarEstadoContratosSAAPV, StoredProcedure); `entrada` = the legacy input object as Gson JSON (the golden "colaboradores" trace holds exactly that under "entrada");
  the result is the legacy output object as JSON (its shape is the one the oracle shim documents in its Javadoc, `legacy-oracle/shims/**`).
- Constructor shape used by the pending tests: `new XService(NamedParameterJdbcTemplate jdbc, ..., Colaboradores colaboradores)`; for the services that read configuration, messages, sequences or the clock:
  `new XService(jdbc, Colaboradores, DiccionarioMensajes, ConfiguracionSistema, Secuencias[, Reloj])`.
- `ProcesarDistribucionService#validaPreRendicion(JsonObject entrada)` returns the same JSON as `execute` (SalidaRecaudacionTraspaso) and writes PAC.LogMandatoPAC.

## Test doubles (pending sources, syntax-checked against the test classpath)
`support/ColaboradoresDeCaso`, `ConfiguracionDelCaso`, `DiccionarioDeCaso`, `SecuenciasDeCaso` (package `cl.bcs.tesoreria.familia_b.pending`): replay case.json
`colaboradores` / `config` / `secuencias`, render the oracle message format `[CODE]{k=v}` and record the collaborator call trace that is compared with the golden.
The compiled classes of the same family (`src/test/java/.../familia_b/*ServiceTest`) are written against the CURRENT constructors and are red until these ports exist.
