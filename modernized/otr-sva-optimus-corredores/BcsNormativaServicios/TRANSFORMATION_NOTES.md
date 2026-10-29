# BcsNormativaServicios — Notas de Transformación

## Inventario completo del legacy

**Módulo legacy**: `BSCL/Normativa/Java/BcsNormativaServicios/`  
**Total archivos Java**: 225  
**Dominio principal**: Cumplimiento normativo CMF (Comisión para el Mercado Financiero)

### Distribución por dominio funcional

| Dominio | Archivos | Descripción |
|---------|----------|-------------|
| `cartola/` | ~50 | Cartola de inversión (rentabilidad, flujo patrimonial, PDF417) |
| `cartola/pojo/` | ~10 | POJOs de cartera (Instrumento, Emisor, Moneda, etc.) |
| `cartola/utils/` | ~10 | Utilidades Jasper/Crystal, configuración |
| `circulares/` | ~20 | Reportes CMF (NCG365, NCG380, Circular1720, 1985) |
| `circulares/ReportesBCC/` | ~15 | Reportes BCC (REPO 2p1, 2p2, 3p1) |
| `declaraciones/` | ~30 | DJ1871, DJ1874, DJ1889, DJ1894, DJ1899, DJ1922, DJ1944, DJ1949 |
| `declaraciones/DJ1/` | ~10 | Declaración Jurada DJ1 (garantías, pagos) |
| `interfaces/` | ~25 | Interfaces C08, C40, P40, ICU, SIG (generación archivos) |
| `ils/` | ~10 | Indicadores de Liquidez y Solvencia |
| `fatca/` | 1 | Generación datos FATCA |
| `barCode/` | 1 | Generación PDF417 (código de barras) |
| Root `cl.bcs.normativa` | ~15 | Servicios raíz (MLT, calificaciones, APV, NCG499) |

### Archivos de alto valor con lógica pura

| Clase legacy | Descripción | Prioridad |
|--------------|-------------|-----------|
| `CalcularRentabilidad.java` | MTD/YTD/ITD — fórmulas de rentabilidad | P0 — implementado |
| `ils/CalcularObligacionesDerechosMonedaExtranjera.java` | Obligaciones ILS — fecha +7 días + SQL | P0 — fecha implementada |
| `declaraciones/DJ1871/ReajustarValorIPC.java` | Reajuste IPC con MathContext.DECIMAL128 | P0 — implementado |
| `declaraciones/DJ1889/ReajustarValorIPC.java` | Reajuste IPC DJ1889 | P1 — stub |
| `declaraciones/DJ1899/ReajustarValorIPC.java` | Reajuste IPC DJ1899 | P1 — stub |

## Estrategia de transformación aplicada

### P0 — Implementados completamente con tests

1. **`RentabilidadService`** — lógica pura MTD/YTD/ITD portada de `CalcularRentabilidad.java`
   - Todos los métodos `static` — testeable sin Spring
   - 6 tests de caracterización en `RentabilidadServiceTest`
   - Fórmula: `numerador = (patrimonio - saldoInicial) - aportes + rescates` / `denominador = saldoInicial + aportesPeriodo - rescatesPeriodo`

2. **`ReajusteIPCService`** — ajuste IPC con `MathContext.DECIMAL128` de `ReajustarValorIPC.java`
   - Todos los métodos `static` — testeable sin Spring
   - 7 tests de caracterización en `ReajusteIPCServiceTest`
   - Aplica a DJ1871 (portado); DJ1889, DJ1899 pendientes de igual patrón

3. **`IlsService.calcularFechaHasta`** — lógica de fecha (+7 días) del cálculo ILS
   - Método `static` — testeable sin Spring
   - 5 tests en `IlsServiceTest`
   - La parte SQL/persistencia del cálculo → stub `@Disabled("requires HANA")`

### P1 — Stubs con `UnsupportedOperationException` (requieren HANA)

Todos los siguientes retornan HTTP 503 vía `GlobalExceptionHandler`:

**CircularesService** (10 operaciones):
- `GenerarArchivoCircular1720` — Crystal Reports + HANA SP
- `GenerarNCG380Custodia` — HANA SP
- `GenerarNCG380` — HANA SP
- `NotificarCIRCULAR1985` — Crystal Reports + HANA SP
- `GenerarAnexo1Circular20` — Crystal Reports + HANA SP
- `NotificarNCG365FFMM` — HANA SP
- `ConsolidarClientes` — HANA SP
- `GenerarInformeMensualInversionistasRV13419` — HANA SP
- `GenerarDatosFatca` — HANA SP

**InterfacesService** (19 operaciones):
- C08: CarteraNormal, CarteraIntermediada, CarteraPacteada, InformacionContable
- C40: CarteraNormal, CarteraIntermediada, InformacionContable
- P40, P40SBIF, ICU, Cliente, Ordenes, SIG, LavadoDinero
- InstrumentosFinancieros, ArchivoCM, Asignaciones, Polizas, MontosIntermediados

**IlsService** (parcial):
- `CalcularObligacionesDerechos` (SQL + upsert)
- `DetalleCoberturaILS`
- `GeneraReporteILS`
- `HomologarCuentasContables`

## Arquitectura hexagonal

```
cl.bcs.normativa.svc/
├── NormativaApplication.java
├── domain/
│   ├── cartola/          # SaldoCuentaCliente, SaldoFinalCliente, Rentabilidad
│   ├── ils/              # ObligacionesDerechos, entradas/salidas
│   └── declaracion/      # ValorIPC, DetalleIPCDJ1871
├── service/
│   ├── cartola/          # RentabilidadService (P0 — pure logic)
│   ├── declaracion/      # ReajusteIPCService (P0 — pure logic)
│   ├── ils/              # IlsService (P0 fecha; P1 SQL)
│   ├── circulares/       # CircularesService (P1 — stubs)
│   └── interfaces/       # InterfacesService (P1 — stubs)
├── adapter/
│   └── in/               # CartolaNormativaController, IlsController,
│                         # CircularesController, InterfacesController
└── config/
    └── GlobalExceptionHandler.java  # NPE/IAE/NFE→400, UOE→503
```

## Configuración

- **Puerto**: 8113
- **HANA**: `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}` (sin credenciales hardcodeadas)
- **Test**: H2 in-memory con `@ActiveProfiles("test")`

## Resultado de tests

```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

- 2 tests `@Disabled("requires HANA")` en `CircularesControllerTest`
- 20 tests activos: 6 rentabilidad + 7 IPC + 5 ILS + 2 controller integration

## Deuda técnica identificada

1. **Crystal Reports** — incompatible con Java 25. Las cartolas PDF siguen en `BcsCrystalReports` (Java 11).
2. **PDF417** — `barCode/PDF417.java` (tercero) usada para código de barras de cartolas → no portado.
3. **DJ1889, DJ1899, DJ1922, DJ1949 IPC** — mismo patrón que DJ1871 → implementar cuando HANA disponible.
4. **FlujoPatrimonial** — lógica compleja de flujo patrimonial con múltiples clases colaboradoras → pendiente P2.
5. **Interfaces BCC** — 3 tipos de reportes REPO (2p1, 2p2, 3p1) con CRUD completo → pendiente P2.
6. **CalcularObligacionesDerechos SQL** — conectar `NamedParameterJdbcTemplate` cuando HANA disponible.
