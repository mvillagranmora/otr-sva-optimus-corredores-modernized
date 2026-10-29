# BcsSocioNegocioServicios — Transformation Notes

## Module Summary
- **Port**: 8098
- **Spring Boot**: 4.1.1 / Java 25
- **Pattern**: Strangler-fig, hexagonal architecture
- **Legacy source**: `legacy/otr-sva-optimus-corredores/BSCL/SocioNegocio/JAVA/BcsSocioNegocioServicios/` (571 files, ~132K LOC)

## Behavior Mapping

| Legacy Class | Behavior | Modern Location |
|---|---|---|
| `cl.bcs.socionegocio.utils.IUP` | IUP generation / document extraction | `SocioNegocioServiciosService.iupGenerar()` / `iupObtenerDocumentoAsociado()` |
| `cl.bcs.socionegocio.utils.OfuscarDocumentoIdentidad` | RUT/passport obfuscation via SHA-256 | `SocioNegocioServiciosService.ofuscarDocumentoIdentidad()` |
| `cl.bcs.socionegocio.soc.SocioNegocioUtils` | Value-or-default, estado autorizacion, campo truncation | `SocioNegocioServiciosService.obtenerValorNoNulo()` / `obtenerIdEstadoAutorizacion()` / `obtenerCampoRegistroTruncado()` |
| HANA CONFIGURACION_T query | `obtenerFechaSistema(idEmpresa)` | `SocioNegocioServiciosRepositoryAdapter.obtenerFechaSistema()` |
| HANA SOCIOS_NEGOCIO_T query | `consultarSociosNegocio(idEmpresa)` | `SocioNegocioServiciosRepositoryAdapter.consultarSociosNegocio()` |
| HANA SP (crear/modificar socio) | Requires HANA stored procedure | `UnsupportedOperationException` (adapter stub) |

## Key Business Rules Preserved

### IUP (Identity Unique Prefix)
- **B-IUP-01**: Each document type has a fixed 2-char prefix code and a fixed padding length. See `ConfiguracionDocumentoIdentidad` enum.
- **B-IUP-02**: FIC type has three sub-variants (FIC_J for juridica, FIC_M for mixta, FIC_N for natural/default).
- **B-IUP-03**: CEX, CE, and RUT all map to the same configuration (code "20", length 8).
- **B-IUP-04**: Total IUP length is always 14 characters (rightPad with "0").
- **B-IUP-05**: Unknown/unsupported types map to ERROR config ("EE", length 0).

### Document Obfuscation
- **B-OFU-01**: RUT pattern `^\d{7,8}-[0-9kK]{1}$` (after dot removal) triggers `generarRut`.
- **B-OFU-02**: All other inputs trigger `generarPasaporte` (SHA-256 → 10 uppercase letters).
- **B-OFU-03**: The obfuscated RUT DV is always invalid (differs from the correct Luhn DV).
- **B-OFU-04**: Both transformations are deterministic (SHA-256 is used in both paths).
- **B-OFU-05**: null/empty inputs pass through unchanged.

### SocioNegocioUtils
- **B-UTL-01**: `obtenerValorNoNulo` — null-safe coalesce.
- **B-UTL-02**: `obtenerIdEstadoAutorizacion(true)` → "ING"; any non-true (including null) → "PEN".
- **B-UTL-03**: `obtenerCampoRegistroTruncado` — truncates to exactly 40 chars; null safe.

## HANA Dependencies → Stubs

| Method | Stub Reason |
|---|---|
| `crearSocioNegocio` | Requires HANA stored procedure; `UnsupportedOperationException("requires HANA SP")` |
| `modificarSocioNegocio` | Requires HANA stored procedure; `UnsupportedOperationException("requires HANA SP")` |

## @Disabled Tests

| Test | Reason |
|---|---|
| `SocioNegocioServiciosIntegrationTest.crearSocioNegocio_throwsUnsupportedOperation` | Disabled because the operation throws UOE by design — no HANA SP available |

## Canary Result

**Break introduced**: Removed `StringUtils.rightPad(...)` from `iupGenerar`, returning only `codigoDocumento + leftPad(documentoIdentidad, largo, "0")` (no trailing zeros, no 14-char guarantee).

**Failures observed** (6 tests):
- `SocioNegocioServiciosServiceTest.iupGenerar_RUT_returns14chars`
- `SocioNegocioServiciosServiceTest.iupGenerar_DNI_returns14chars`
- `SocioNegocioServiciosServiceTest.iupGenerar_FIC_J_usesFicJConfig`
- `SocioNegocioServiciosServiceTest.iupGenerar_FIC_M_usesFicMConfig`
- `SocioNegocioServiciosServiceTest.iupGenerar_UNKNOWN_usesErrorConfig`
- `SocioNegocioServiciosIntegrationTest.iupGenerar_validInput_returns200AndCorrectValue`

**Restored**: `rightPad` restored. All 25 tests pass (1 skipped), BUILD SUCCESS.

## Final Test Results

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```
