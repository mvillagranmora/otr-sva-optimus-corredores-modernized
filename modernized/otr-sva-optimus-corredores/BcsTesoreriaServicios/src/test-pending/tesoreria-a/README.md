# Pending tests of family A (src/test-pending/tesoreria-a, NOT compiled)

These tests are written against the INTENDED API of the modern services. They are not compiled because the classes they need do not exist under
`cl.bcs.tesoreria.javacall` yet (there is no compatible class in `src/main`). Their goldens are real: `src/test/resources/tesoreria-a/cases/**/expected.json`
was recorded by executing the unmodified legacy class. `pending-cases.txt` lists every case that belongs to a pending test (the inventory test checks it).

When a class is implemented: move its `*ServiceTest.java` to `src/test/java/cl/bcs/tesoreria/familia_a/`, move `support/*` next to the other support
classes (package `cl.bcs.tesoreria.familia_a.support`) and delete the case names from `pending-cases.txt` (the generator in the scratch scaffolding does both).

## Intended API (confirmed decisions)

Package names follow the convention already used by `BcsCuentaInversionServicios` (`port.out.Colaboradores`).

```java
// cl.bcs.tesoreria.port.out.Colaboradores : other legacy services (JavaCall classes) called by name, JSON in / JSON out
public interface Colaboradores {
    JsonObject invocar(String servicio, JsonObject entrada);
}

// cl.bcs.tesoreria.port.out.DiccionarioMensajes : injectable message dictionary (the legacy Diccionario.getMensaje(...).add(k, v).msg())
public interface DiccionarioMensajes {
    String mensaje(String codigo, Map<String, Object> parametros);
}

// cl.bcs.tesoreria.port.out.ConfiguracionSistemaPort : ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, modulo, parametro)
public interface ConfiguracionSistemaPort {
    String valor(String modulo, String parametro);      // null when the parameter does not exist (the service decides whether that is an error)
}
```

Constructor of every pending service (same order for all):

```java
public XxxService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaPort configuracion, DiccionarioMensajes diccionario, Colaboradores colaboradores)
JsonObject execute(JsonObject entrada)      // = legacy call(conn, json); transaction: commit when it returns, rollback when it throws
```

`AutorizacionLiquidacion` has no `execute`: it implements the two entry points of the legacy interface `BcsAutorizaciones`, as separate methods that take and return the
legacy `EntradaAutorizacion` / `SalidaAutorizacion` JSON (cases with `"metodo": "autorizarEvento"` / `"rechazarEvento"`):

```java
JsonObject autorizarEvento(JsonObject entradaAutorizacion);    // legacy: SalidaAutorizacion autorizarEvento(Connection, EntradaAutorizacion)
JsonObject rechazarEvento(JsonObject entradaAutorizacion);     // legacy: SalidaAutorizacion rechazarEvento(Connection, EntradaAutorizacion)
```

`AutorizacionAnularMovimientosTesoreria` has the same two methods. `ConfirmarRechazarAnularMovimientosTesoreria` has `execute` (legacy `call`) and, for the case that records the
legacy `logica`, the same `execute` is used (the cases that differ between `call` and `logica` pin the error counter returned by each; see the case names).

`TraspasoPojoPendingTest` expects the data holders of the legacy `cl.bcs.tesoreria.pojo.Traspaso` under `cl.bcs.tesoreria.javacall.pojo.Traspaso` (same nested classes,
same default values of a fresh instance).

## Collaborators: names used in the goldens

`expected.json` -> `"colaboradores"` is the ordered list of `{servicio, entrada}` that the legacy handed to its collaborators. The modern service must call
`Colaboradores.invocar(servicio, entrada)` with the same names and the same input (compared as canonical JSON). Names that appear:
`RevisarRequisitosBasicos`, `AgregarLog`, `ActualizarSaldosCuentaBanco`, `ConsultarFlujoCaja`, `SaldoFlujoCaja`, `CrearCargoAbono`, `AnularCargoAbono`,
`CrearEventoAutorizacionMasivo`, `ModificarEstadoEventoAutorizacionMasivo`, `AnularNominasTransferencias`, `Cloper`, `ObtenerPortafoliosEjecutivo(.restringeCartera)`
and `StoredProcedure` (a HANA stored procedure: name + scalar inputs; the procedure is OUTSIDE the proof, see the README of familia_a).
`case.json` -> `"colaboradores"` scripts the answers: an object answers every call, an array answers the n-th call, absent = empty answer
(`ColaboradoresDeCaso` implements this; the oracle keys `requisitosBasicos`, `saldoFlujoCaja`, `restringeCartera`, `portafoliosEjecutivo`, `cloper` and
`SalidaProcedimiento` are the oracle's scripts for helpers that are not plain JSON services; the modern adapter must map its own call names onto them).
