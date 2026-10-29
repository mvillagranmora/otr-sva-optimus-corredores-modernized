# BcsSURA — Transformation Notes

**Legacy:** `BSCL/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio/` — 1,468 LOC
**Target:** Spring Boot 4.1.1 / Java 25
**Equivalence:** trace-based — HANA DB and SURA SOAP WS sandbox required for full dual-execution

---

## Behavior Mapping

| Behavior ID | Legacy file:lines | Target file | Description |
|---|---|---|---|
| B-SURA-01 | `ModificarPerfilesPortafolioUtils.java:132-135` | `service/PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv` | Portfolios con Proposito=="PROPOSITOAPV" → ActualizarFormaCobro = "N"; resto sin cambio |
| B-SURA-02 | `ModificarPerfilesPortafolioUtils.java:94-99` | `service/PerfilesPortafolioService.filtrarParaIngresarIndicadores` | Filtro IdIndicador==0 → candidatos a INSERT en tabla indicadores |
| B-SURA-03 | `ModificarPerfilesPortafolioUtils.java:112-117` | `service/PerfilesPortafolioService.filtrarParaModificarIndicadores` | Filtro IdIndicador!=0 AND ActualizarFormaCobro=="S" → candidatos a UPDATE |
| B-SURA-04 | `ModificarPerfilesPortafolioUtils.java:252-255` | `service/PerfilesPortafolioService.fusionarFormaCobro` | Si IdFormaCobro vacío en resultado DB → copiar desde input por IdPortafolio; si ya tiene valor → mantener |
| B-SURA-05 | `ModificarPerfilesPortafolioUtils.java:287` | `service/PerfilesPortafolioService.filtrarConIdFormaCobro` | Excluir entidades con IdFormaCobro vacío |
| B-SURA-06 | `ModificarPerfilesPortafolioUtils.java:276-279` | `service/PerfilesPortafolioService.aplicarFormaCobro` | Asignar valor de indicador COBROCUST a todos los perfiles matching |

---

## Canary

Romper `validarIndicadorFormaCobroPortafolioApv` cambiando `"PROPOSITOAPV".equals(p.proposito)` → negando la condición produce:
- `validarApv_propositoAPV_setActualizarN` → FAIL
- `validarApv_mixto_soloAPVCambia` → FAIL
- `validarApv_multiplesAPV_todosSetN` → FAIL

**Canary: invertir condición APV → 3 tests fallidos**

---

## Casos ejecutados

**26 tests, 0 fallos, 5 skipped** (@Disabled por HANA/SOAP sandbox no disponible)

---

## Deviaciones deliberadas

| Código | Descripción |
|---|---|
| HANA-defer | `SuraRepositoryAdapter` lanza `UnsupportedOperationException("requires HANA: ...")` para todas las operaciones de BD: `PERFILES_COMERCIALES_T`, `PORTAFOLIOS_T`, `PERFILES_PORTAFOLIO_T`, `INDICADORES_PORTAFOLIO_T`. |
| SOAP-defer | Endpoints `/crear-cliente` y `/modificar-cliente` en `SuraController` lanzan `UnsupportedOperationException` — requieren sandbox SURA SOAP (`CrearClienteFullWSModificarPerfilesPortafolio`, `ModificarClienteModificarPerfilesPortafolioWS`). |
| Mutable domain | `IdsEntidadYPerfiles` usa campos públicos mutables para equivalencia con el patrón `BcsPojo` del legacy (actualizarFormaCobro="S" por defecto, como en legacy). |
| Tabla temporal | `generarTablaTemporal` / `obtenerPerfilesFiltradosPorTablaTemporal` no migradas — requieren tabla temporal HANA específica; diferidas a Fase 4. |

---

## No migrado

- `CrearClienteFullWSModificarPerfilesPortafolio.java` (350 LOC) — creación de cliente via SOAP WS: requiere sandbox SURA.
- `CrearSocioNegocioModificarPerfilesPortafolio.java` (263 LOC) — creación de socio-negocio: requiere sandbox SURA.
- `ModificarClienteModificarPerfilesPortafolioWS.java` (307 LOC) — modificación de cliente WS: requiere sandbox SURA.
- `ModificarSocioNegocioModificarPerfilesPortafolio.java` (188 LOC) — modificación de socio-negocio: requiere sandbox SURA.
- Tabla temporal HANA: `generarTablaTemporal`, `obtenerPerfilesFiltradosPorTablaTemporal` — operaciones de tabla temporal específica HANA.
- `agregarMensajeError*` (4 variantes) — dependen de `PlataformaUtils.MensajeSalida` con conexión activa; diferidas.

---

## Seguimiento

- Activar @Disabled cuando sandbox SURA disponible
- Implementar cliente SOAP Spring-WS para endpoints de creación/modificación
- Implementar tabla temporal HANA con NamedParameterJdbcTemplate
- Equivalencia dual-run con datos de perfil históricos en staging
