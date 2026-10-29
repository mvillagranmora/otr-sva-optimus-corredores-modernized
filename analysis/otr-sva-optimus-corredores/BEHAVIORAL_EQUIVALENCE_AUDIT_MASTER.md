# Auditoría de Equivalencia de Comportamiento — otr-sva-optimus-corredores
**Fecha**: 2026-10-07  
**Alcance**: Todos los microservicios modernizados  
**Metodología**: Comparación directa código legado TomEE JavaCall vs modernizado Spring Boot 4.1.1 / Java 25  
**Archivos detallados**: Ver `BEHAVIORAL_EQUIVALENCE_AUDIT_*.md` por microservicio

> ⚠️ **VEREDICTO GENERAL: NO APTO PARA PRODUCCIÓN**  
> La modernización está incompleta. La mayoría de los microservicios críticos tienen entre 54% y 100% de STUBs que retornan éxito falso sin ejecutar ninguna lógica de negocio.

---

## Resumen Ejecutivo Global

| Microservicio | Servicios | ✅ Equiv | ⚠️ Diverg | ❌ Stub | ❓ Sin Legado | 🚫 Sin Mod |
|---|---|---|---|---|---|---|
| BcsCustodiaServicios | 180 | 0 | 0 | **180 (100%)** | 0 | 0 |
| BcsAPVServicios | 67 | 0 | 4 | **63 (94%)** | 0 | 0 |
| BcsFondoMutuoServicios | ~132 | 1 | 5 | **~97 (73%)** | 1 | 4 |
| BcsOrdenServicios | 138 | ~44 | varios | **87 (63%)** | 0 | 0 |
| BcsPlataformaServicios | 328 | 1 | 15 | **~178 (54%)** | 17 | 37 |
| BcsMesaServicios | ~123 | pocos | **~80 contrato erróneo** | varios | 0 | 0 |
| BcsTesoreriaServicios | 48 | 25 | 7 | **16 (33%)** | 4 | 0 |
| BcsFacturacionServicios | 50 | 22 | **13 (26%)** | 15 | 0 | 0 |
| BcsContabilidadServicios | 78 | 52 | 4 | 2 | 3 | **17** |
| BcsCuentaInversionServicios | 43 | 24 | **17 (40%)** | 0 | 0 | 0 |
| BcsPortafolioServicios | 21 | 13 | 1 | 0 | 7 | 0 |
| BcsSocioNegocioServicios | 558 | 3 | 0 | 0 | **555** | 0 |
| BcsCanalAgenteServicios | 1 | 1 | 0 | 0 | 0 | 0 |
| BcsNormativaServicios | ~170 | ✅ OK | 0 | 0 | 0 | 0 |
| BcsJobManager | 4 | ✅ OK | 0 | 0 | 0 | 0 |
| Jobs (todos los batch) | ~540 | ~242 | 7 | **~169** | 1 | 0 |

---

## Hallazgos Críticos — Bloquean Go-Live

### P0 — STUBS con éxito falso silencioso (sistema aparenta funcionar pero no hace nada)

#### BcsCustodiaServicios — 100% INOPERABLE
Todos los 180 servicios responden HTTP 503 al ser invocados. Los motores centrales de custodia están completamente sin implementar:
- `IngresoCustodiaService`, `RetiroCustodiaService` — motores centrales
- `AnularGarantia` — 1019 líneas de lógica compleja en legado, 0 en modernizado
- `EjecutarVencimiento` — cierre de día, liquida RF
- `LiquidacionOpciones`, `LiquidacionFuturos` — derivados
- 40 servicios de Eventos de Capital (dividendos, canjes, corte cupón) — dominio `evc/` completo
- 28 servicios de Préstamos de Valores (CrearPrestamo, AnularPrestamo, LiquidarCustodiaPrestamo) — dominio `prt/` completo

**53 STUBs son reparables inmediatamente** (sin dependencias cross-service, solo portar Java+SQL del legado).

#### BcsOrdenServicios — 63% STUB
87 de 138 servicios retornan éxito sin ejecutar lógica ni SQL. Incluye:
- `CuadraturaDiariaService`
- `LiquidarInstruccionService`
- `EjecutarDesasignacionService`
- `RutearOrdenesService`

#### BcsMesaServicios — Contratos de salida incompatibles (~80 servicios)
El agente de transformación interpretó los servicios SPT/FWD como "consulta + efecto secundario". Retornan datos **pre-ejecución** (puntas, portafolios, operaciones existentes) en vez del **resultado post-ejecución** (`P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`, `P_OUT_ADVERTENCIASCONTADOR`, IDs de objetos creados). Los clientes que consumen estas APIs recibirán respuestas estructuralmente distintas a las esperadas.

#### BcsPlataformaServicios — 54% STUB + dependencia bloqueante
176 servicios marcados con `// TODO: requires pla.* utility — complete after pla subpackage migration`. El subpaquete `pla.*` nunca fue migrado, bloqueando más de la mitad del microservicio más grande del sistema.

`CrearEventoAutorizacion` es STUB crítico — nunca invoca el SP `AUT.CrearEventoAutorizacion`. **Todos los flujos de autorización del sistema están bloqueados.**

#### BcsFondoMutuoServicios — ~73% STUB
El ciclo completo Crear → Certificar → Liquidar → Anular son todos STUBs. El sistema no puede procesar ninguna solicitud de fondo mutuo.

#### BcsAPVServicios — 94% STUB
Todos los repositorios (`ContratoRepository`, `TraspasoRepository`, `RecaudacionRepository`, `BonificacionRepository`, `PACRepository`) lanzan `UnsupportedOperationException`. APV completamente inoperable.

#### BcsPlataformaJobs — Pauta diaria inoperable
`LaPautaService.ejecutarPauta()` y ~60 procesos `Pauta*` lanzan `UnsupportedOperationException`. La pauta diaria (valorización de carteras, vencimientos, réplicas, facturación mensual, FATCA) no puede ejecutarse.

---

### P0 — Divergencias de Comportamiento Críticas

#### BcsPlataformaServicios — Seguridad de acceso comprometida
| Servicio | Divergencia | Riesgo |
|---|---|---|
| `CrearUsuario` | Falta validación de rol de seguridad (brecha de control de acceso); **doble hash vs simple hash** (incompatibilidad de credenciales existentes); falta evento de autorización y notificación email | CRÍTICO |
| `CambiarClaveUsuario` | Historial inserta clave **nueva** en lugar de **antigua** — anula protección de contraseñas repetidas | CRÍTICO |
| `ValidacionPreventiva` | Solo 2 de 20+ validaciones implementadas | ALTO |
| `ProcesarCuadraturaCustodiaRFIF` | Faltan 3 pasos SQL críticos (SFINALIF, SFINALIFN, MOD_SALDO_CALCULADO); orden de pasos incorrecto | ALTO |
| `CrearAutorizadoEtapa` | 7 validaciones omitidas; falta generación de PK | ALTO |

#### BcsTesoreriaServicios — Motor financiero incompleto
| Servicio | Divergencia | Riesgo |
|---|---|---|
| `CrearIngresoEgresoService` | 49 líneas vs 1707 en legado. Faltan: validación flujo de caja, procesamiento FormaDePago, 50+ validaciones de negocio, contadores errores/advertencias | CRÍTICO |
| `LiquidarMovimientoAutomaticoService` | No ejecuta CrearComprobante ni LiquidarComprobante; ignora canal "WEB"; retorna `okEmpty()` | CRÍTICO |
| `CrearComprobanteService` | TODO explícito: SP HANA `CrearComprobante` nunca se ejecuta | CRÍTICO |
| `EjecutarAnulacionComprobantesService` | Lee comprobantes pero no anula ninguno (TODO sin implementar) | CRÍTICO |
| `LiquidacionMasivaComprobanteService` | Lee datos pero no liquida (TODO: delegate via RestClient) | ALTO |

#### BcsCuentaInversionServicios — Cargos/abonos incorrectos
`CrearCargoAbonoService`: usa `LocalDate.now()` en lugar de `FechaSistema` desde `ConfiguracionSistema`; faltan 15+ validaciones (`CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO`, `MONTO_MENOR_IGUAL_A_CERO`, `FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO`); no llama `SaldoFlujoCaja`; no registra `AgregarLog`; no schedula `AgregarPrograma` para cuentas propias.

#### BcsMesaJobs — `LiberaBloqueoEdicionPactos` DIVERGENTE CRÍTICO
Omite el filtro `minutosTranscurridos > TIEMPOMAXEDICIONPACTO`. **Libera TODAS las operaciones bloqueadas sin restricción de tiempo**, rompiendo la invariante de negocio del sistema de pactos.

#### BcsFondoMutuoServicios — Cálculos financieros incorrectos
| Servicio | Divergencia |
|---|---|
| `CalcularSpreadRescate` | Falta conversión de moneda via `CalcularParidad` para fondos extranjeros; spread incorrecto cuando `monedaFondo ≠ monedaSistema` |
| `ValidaMontoInversion` | Límites desde BD (legado) vs parámetros de entrada (modernizado); falta validación `AportesAltos`; falta agrupación por `FolioContrato` |
| `ConsultarCustodiaPortafolioFFMM` | Contrato de salida incompatible: `p_out_tbl` vs `P_OUT_CUSTODIAS`/`P_OUT_GARANTIAS` |

---

### P1 — Funcionalidad Completamente Ausente (sin contraparte modernizada)

| Funcionalidad | Microservicio | Impacto |
|---|---|---|
| 12 servicios de conciliación financiera (ConciliacionFFMM*, ConciliacionMesaRF*) | BcsPlataformaServicios | Conciliación financiera completamente inoperante |
| `CalcularCobros`, `CalcularCobrosPe`, `BcsAutorizaciones` | BcsPlataformaServicios | — |
| 17 servicios de contabilidad (ArchivoControl, EnvioContabilidad, ProcesarInterfazBasilea, SFTPConnector, etc.) | BcsContabilidadServicios | Determinar si son out-of-scope |
| 4 servicios de AutorizaciónModificación / ConfirmarEjecutar fechas | BcsFondoMutuoServicios | — |
| Jobs CustodiaJobs: BlockChain, DerivadosNeteo, GenerarArchivoGarantia, 17+ más | BcsCustodiaJobs | Cobertura 16% solamente |
| ~40 jobs regulatorios CMF/SBif | BcsNormativaJobs | Cobertura 6% solamente |
| Gestión de clientes (48), personas (155), documentos (15), ejecutivos (17), blockchain (26) | BcsSocioNegocioServicios | 555/558 servicios sin migrar |

---

### P1 — Pista de Auditoría Incompleta

14 servicios en BcsFacturacionServicios y BcsTesoreriaServicios tienen TODO explícito para llamar `AgregarLog`. Todas las operaciones escritas ocurren sin trazabilidad de auditoría.

**BcsFacturacionServicios**: `RevisarRequisitosBasicos` omitido en 13 servicios de escritura — permite creaciones/modificaciones sin validar usuario activo, módulo habilitado ni empresa activa.

---

### P1 — Jobs Batch sin Trigger

`BcsContabilidadJobs`: servicios de negocio correctamente implementados pero sin REST controller ni `@Scheduled`. Los jobs son inalcanzables desde ningún sistema.

---

## Microservicios en Buen Estado

| Microservicio | Estado | Notas |
|---|---|---|
| BcsNormativaServicios | ✅ APTO | Sin stubs, sin divergencias detectadas |
| BcsJobManager | ✅ APTO | SQL HANA fixes aplicados correctamente en sesión anterior |
| BcsContabilidadServicios | ✅ MAYORMENTE APTO | 52/78 equivalentes; fix intencional `+=` vs `=` documentado en Javadoc |
| BcsPortafolioServicios | ✅ MAYORMENTE APTO | 13/21 equiv; `CrearPortafolio` correcto con orquestación completa |
| BcsCanalAgenteServicios | ✅ APTO | 1/1 equiv |
| BcsCuentaInversionServicios (SQL puro) | ✅ PARCIALMENTE APTO | 24/43 equiv; servicios con orquestación cross-service divergen |
| BcsTributarioJobs, BcsJobsFondoMutuo, BcsJobOrders, BcsCartolasJobs, BcsValo2, BcsSURA, BcsBECB, BcsReplica, BcsOrdenJobs | ✅ APTOS | Bien implementados |

---

## Plan de Remediación Priorizado

### Sprint 0 — Desbloquear infraestructura crítica (P0)
1. **Migrar subpaquete `pla.*`** de BcsPlataformaServicios — desbloquea 176 servicios con `TODO: requires pla.*`
2. **Implementar `CrearEventoAutorizacion`** con llamada real al SP `AUT.CrearEventoAutorizacion` — desbloquea todos los flujos de autorización
3. **Implementar clientes HTTP cross-service** (los 5-6 más frecuentes): `CalcularParidad`, `RevisarRequisitosBasicos`, `AgregarLog`, `CrearIngresoEgreso`, `AnularCargoAbono`

### Sprint 1 — Custodia (53 STUBs sin dependencias)
4. **Portar los 53 STUBs independientes** de BcsCustodiaServicios — solo requieren Java+SQL del legado. Prioridad: `IngresoCustodia`, `RetiroCustodia`, `ConfirmarAutorizacionIngresoTitulos`, `ConfirmarAutorizacionRetiroTitulos`, `NetearClientesService`, `ValorizarCustodiaMasiva`

### Sprint 2 — Tesorería financiera crítica
5. **Reimplementar `CrearIngresoEgresoService`** desde las 1707 líneas del legado
6. **Implementar `CrearComprobanteService`** con llamada al SP HANA
7. **Implementar `LiquidarMovimientoAutomaticoService`** con orquestación `CrearComprobante` + `LiquidarComprobante`
8. **Implementar `EjecutarAnulacionComprobantesService`** delegando a `AnularRegistrosService`

### Sprint 3 — Seguridad y autenticación
9. **Corregir `CrearUsuario`**: restaurar validación de rol, evento de autorización, doble hash + script migración de credenciales existentes
10. **Corregir `CambiarClaveUsuario`**: historial debe insertar clave antigua, no nueva
11. **Implementar `ValidacionPreventiva`**: 20+ validaciones faltantes

### Sprint 4 — FondoMutuo y APV
12. **Implementar ciclo FondoMutuo**: Crear → Certificar → Liquidar → Anular
13. **Reparar `CalcularSpreadRescate`**: agregar conversión via `CalcularParidad`
14. **Implementar repositorios APV** (todos lanzan `UnsupportedOperationException`)
15. **Agregar trigger a BcsContabilidadJobs** (REST controller o `@Scheduled`)

### Sprint 5 — Órdenes y Pactos
16. **Implementar 87 STUBs críticos** de BcsOrdenServicios
17. **Corregir `LiberaBloqueoEdicionPactos`**: restaurar filtro de tiempo `TIEMPOMAXEDICIONPACTO`
18. **Corregir contratos de salida de ~80 servicios** de BcsMesaServicios SPT/FWD

### Sprint 6 — Cobertura batch y regulatory
19. **BcsCustodiaJobs**: portar 31 jobs faltantes (cobertura actual: 16%)
20. **BcsNormativaJobs**: portar ~40 jobs regulatorios CMF/SBif (cobertura actual: 6%)
21. **Implementar 12 servicios de conciliación** en BcsPlataformaServicios
22. **Pauta diaria**: implementar `LaPautaService.ejecutarPauta()` y 60 procesos Pauta*

---

## Notas de Seguridad

> ⚠️ RECORDATORIO: Rotar credenciales HANA MASTER01/MASTER02 + clave 3DES ANTES de cualquier go-live. Estado: PENDIENTE (diferido de sesión anterior).

El bug de hash de contraseñas en `CrearUsuario` (sha256 simple vs doble) requiere un script de migración de datos que re-hashee las credenciales de todos los usuarios existentes antes del cutover. Ejecutar en ventana de mantenimiento coordinada con el equipo de seguridad.

---

## Archivos de detalle por microservicio

| Archivo | Microservicio(s) |
|---|---|
| `BEHAVIORAL_EQUIVALENCE_AUDIT.md` | BcsMesaServicios, BcsOrdenServicios, BcsNormativaServicios, BcsJobManager |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_PLATAFORMA.md` | BcsPlataformaServicios |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_CUSTODIA.md` | BcsCustodiaServicios |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_FONDOMUTUO.md` | BcsFondoMutuoServicios |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_CONTAB_TESO_FACT.md` | BcsContabilidadServicios, BcsTesoreriaServicios, BcsFacturacionServicios |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_CTAINV_PORT_APV_SOCIO.md` | BcsCuentaInversionServicios, BcsPortafolioServicios, BcsAPVServicios, BcsSocioNegocioServicios, BcsCanalAgenteServicios |
| `BEHAVIORAL_EQUIVALENCE_AUDIT_JOBS_PEQUENOS.md` | Todos los Jobs y microservicios pequeños |
