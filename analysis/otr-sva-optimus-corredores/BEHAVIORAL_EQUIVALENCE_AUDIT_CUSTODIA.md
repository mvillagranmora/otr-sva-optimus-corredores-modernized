# Auditoría de Equivalencia — BcsCustodiaServicios
**Fecha**: 2026-10-07  
**Servicios auditados**: 180 de 184 legados (97,8%)  
**Metodología**: Comparación manual legado vs modernizado + análisis de estructura de código  
**Auditor**: Claude Sonnet 4.6 (agente de equivalencia)

---

## Resumen Ejecutivo

> **ALERTA CRÍTICA**: El módulo BcsCustodiaServicios modernizado NO implementa ningún servicio de negocio. El 100% de las clases de servicio son STUBs que devuelven HTTP 503 SERVICE_UNAVAILABLE al ser invocadas. La modernización está en estado INOPERABLE para producción.

| Estado | Cantidad | % |
|--------|----------|---|
| ✅ EQUIVALENTE | 0 | 0% |
| ⚠️ DIVERGENTE | 0 | 0% |
| ❌ STUB | 180 | 100% |
| ⚠️ SQL_MISMATCH | 0 | 0% |
| ❓ SIN_LEGADO | 2 | — |

### Desglose de STUBs por subdomain

| Subdomain | STUBs | Descripción |
|-----------|-------|-------------|
| `service/` (raíz) | 97 | Servicios principales de custodia |
| `service/evc/` | 40 | Eventos de capital |
| `service/prt/` | 28 | Préstamos y empréstitos |
| `service/utils/` | 11 | Utilidades de negocio |
| `service/sim/` | 2 | Simultáneas |
| `service/wbs/` | 2 | Web services de custodia |
| **Total** | **180** | |

### Desglose de STUBs por causa de deferral

| Causa | Cantidad | Descripción |
|-------|----------|-------------|
| STUB con dependencias cross-service reales | 127 | Requiere clientes HTTP hacia otros módulos (cuentaInversion, tesoreria, mesa, ordenes, facturacion, socionegocio, fondomutuo, plataforma business) |
| STUB SIN justificación técnica | 53 | Marcados DEFERRED pero sin imports cross-service. Usan solo utilidades intra-módulo (cl.bcs.custodia.utils.*) y pl plataforma.utils.*. Podrían implementarse de inmediato. |

---

## Patrón Uniforme de los STUBs

Cada archivo de servicio modernizado sigue exactamente este patrón:

```java
@Service
public class XxxService implements JavaCallService {
    @Override
    public String getFqn() { return "cl.bcs.custodia.Xxx"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("XxxService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.Xxx is deferred: <lista de deps>...");
    }
}
```

No se ejecuta ninguna lógica de negocio, consulta SQL, ni validación.

---

## Hallazgos Detallados — Servicios de ALTO Riesgo

### 1. AnularGarantia
**FQN legado**: `cl.bcs.custodia.AnularGarantia`  
**Archivo modernizado**: `service/AnularGarantiaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: CRÍTICO  
**Dependencias cross-service**: `cl.bcs.cuentaInversion.AnularCargoAbono`, `cl.bcs.plataforma.pla.AgregarLog`  
**Impacto**: Proceso complejo (1019 líneas en legado). Gestiona anulación de garantías CUS/CTA/ESP con reingresos de cartera, retiro de custodia, y registro de log de auditoría. Su indisponibilidad bloquea toda operación de anulación de garantías.  
**Campos de salida legado**: `P_OUT_MENSAJES`, `P_OUT_ERRORESCONTADOR`  
**Campos de salida modernizado**: HTTP 503 — sin respuesta funcional

---

### 2. EjecutarVencimiento
**FQN legado**: `cl.bcs.custodia.EjecutarVencimiento`  
**Archivo modernizado**: `service/EjecutarVencimientoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: CRÍTICO  
**Dependencias cross-service**: `cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF`, `cl.bcs.plataforma.mer.CalcularParidad`, `cl.bcs.cuentaInversion.CrearCargoAbono`, `cl.bcs.mesa.MES.CambiarEstadoOperacion`, `cl.bcs.mesa.MRF.VencerOperacion`, `cl.bcs.ordenes.LiquidarCustodiaOrdenCompra`, `cl.bcs.ordenes.LiquidarCustodiaOrdenVenta`, `cl.bcs.plataforma.pla.ObtenerDiaAnterior`, `cl.bcs.tesoreria.CrearIngresoEgreso`  
**Impacto**: Servicio de cierre de día crítico. Liquida vencimientos de instrumentos de renta fija. Paraliza el proceso de cierre si no está disponible.

---

### 3. LiquidacionOpciones
**FQN legado**: `cl.bcs.custodia.LiquidacionOpciones`  
**Archivo modernizado**: `service/LiquidacionOpcionesService.java`  
**Estado**: ❌ STUB  
**Riesgo**: CRÍTICO  
**Dependencias cross-service**: `cl.bcs.cuentaInversion.CrearCargoAbono`, `cl.bcs.tesoreria.CrearIngresoEgreso`  
**Impacto**: Liquida posiciones de opciones. Bloquea la liquidación del mercado de derivados.

---

### 4. LiquidacionFuturos
**FQN legado**: `cl.bcs.custodia.LiquidacionFuturos` (inferido del nombre del service)  
**Archivo modernizado**: `service/LiquidacionFuturosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: CRÍTICO  
**Impacto**: Proceso de liquidación diaria de futuros.

---

### 5. ActualizarCustodiaOSA
**FQN legado**: `cl.bcs.custodia.ActualizarCustodiaOSA`  
**Archivo modernizado**: `service/ActualizarCustodiaOSAService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  
**Nota**: El legado usa `cl.bcs.custodia.utils.*` (intra-módulo) y `cl.bcs.plataforma.pla.utils.ConfiguracionSistema` (platform util). No tiene dependencias cross-service que justifiquen el deferral.  
**Impacto**: Actualiza custodias de OSA (Órdenes de Suscripción y Aporte). Servicio de alto impacto.

---

### 6. IngresarEjercicio
**FQN legado**: `cl.bcs.custodia.IngresarEjercicio`  
**Archivo modernizado**: `service/IngresarEjercicioService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  
**Impacto**: Registra ejercicios de opciones. Proceso clave para el mercado de derivados.

---

### 7. AnularCarteraNegativa
**FQN legado**: `cl.bcs.custodia.AnularCarteraNegativa`  
**Archivo modernizado**: `service/AnularCarteraNegativaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 8. AnularMovimientosCertificados
**FQN legado**: `cl.bcs.custodia.AnularMovimientosCertificados`  
**Archivo modernizado**: `service/AnularMovimientosCertificadosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 9. AnularMovimientosEnTransito
**FQN legado**: `cl.bcs.custodia.AnularMovimientosEnTransito`  
**Archivo modernizado**: `service/AnularMovimientosEnTransitoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 10. GenerarMovimientoContraparteGarantia
**FQN legado**: `cl.bcs.custodia.GenerarMovimientoContraparteGarantia`  
**Archivo modernizado**: `service/GenerarMovimientoContraparteGarantiaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Dependencias cross-service**: `cl.bcs.plataforma.mer.CalcularParidad`, `cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores`  

---

### 11. NetearClientes
**FQN legado**: `cl.bcs.custodia.NetearClientes`  
**Archivo modernizado**: `service/NetearClientesService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 12. NetearCustodiaNegativa
**FQN legado**: `cl.bcs.custodia.NetearCustodiaNegativa`  
**Archivo modernizado**: `service/NetearCustodiaNegativaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 13. ValorizarCustodiaMasiva
**FQN legado**: `cl.bcs.custodia.ValorizarCustodiaMasiva`  
**Archivo modernizado**: `service/ValorizarCustodiaMasivaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  
**Legado**: 264 líneas de lógica de valorización masiva.

---

### 14. FusionCarteras
**FQN legado**: `cl.bcs.custodia.FusionCarteras`  
**Archivo modernizado**: `service/FusionCarterasService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 15. FusionCarterasClientes
**FQN legado**: `cl.bcs.custodia.FusionCarterasClientes`  
**Archivo modernizado**: `service/FusionCarterasClientesService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 16. IngresoRetiroTitulosJava
**FQN legado**: `cl.bcs.custodia.IngresoRetiroTitulosJava`  
**Archivo modernizado**: `service/IngresoRetiroTitulosJavaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 17. ResultadoDiarioFuturos
**FQN legado**: `cl.bcs.custodia.ResultadoDiarioFuturos`  
**Archivo modernizado**: `service/ResultadoDiarioFuturosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 18. ConfirmarAutorizacionRetiroTitulos
**FQN legado**: `cl.bcs.custodia.ConfirmarAutorizacionRetiroTitulos`  
**Archivo modernizado**: `service/ConfirmarAutorizacionRetiroTitulosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 19. ConfirmarAutorizacionIngresoTitulos
**FQN legado**: `cl.bcs.custodia.ConfirmarAutorizacionIngresoTitulos`  
**Archivo modernizado**: `service/ConfirmarAutorizacionIngresoTitulosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 20. RechazoAutorizacionRetiroTitulos
**FQN legado**: `cl.bcs.custodia.RechazoAutorizacionRetiroTitulos`  
**Archivo modernizado**: `service/RechazoAutorizacionRetiroTitulosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 21. RechazoAutorizacionIngresoTitulos
**FQN legado**: `cl.bcs.custodia.RechazoAutorizacionIngresoTitulos`  
**Archivo modernizado**: `service/RechazoAutorizacionIngresoTitulosService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  
**Causa del STUB**: "no cross-service cl.bcs.* imports found" — DEFERRAL SIN JUSTIFICACIÓN  

---

### 22. CertificarMovimientosEnTransito
**FQN legado**: `cl.bcs.custodia.CertificarMovimientosEnTransito`  
**Archivo modernizado**: `service/CertificarMovimientosEnTransitoService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 23. IngresarGarantiaJava
**FQN legado**: `cl.bcs.custodia.IngresarGarantiaJava`  
**Archivo modernizado**: `service/IngresarGarantiaJavaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 24. RetirarGarantiaJava
**FQN legado**: `cl.bcs.custodia.RetirarGarantiaJava`  
**Archivo modernizado**: `service/RetirarGarantiaJavaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

### 25. RetirarGarantiaAgrupada
**FQN legado**: `cl.bcs.custodia.RetirarGarantiaAgrupada`  
**Archivo modernizado**: `service/RetirarGarantiaAgrupadaService.java`  
**Estado**: ❌ STUB  
**Riesgo**: ALTO  

---

## STUBs de Dominio prt/ (Préstamos y Empréstitos) — 28 servicios

Todos los 28 servicios del dominio prt son STUBs. Se listan los de mayor riesgo:

| Servicio | FQN Legado | Riesgo | Deps Cross-service |
|----------|-----------|--------|-------------------|
| CrearPrestamoService | cl.bcs.custodia.prt.CrearPrestamo | CRÍTICO | cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.socionegocio.bck.* |
| CrearEmprestitoService | cl.bcs.custodia.prt.CrearEmprestito | CRÍTICO | cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.plataforma.bat.AgregarPrograma |
| AnularPrestamoService | cl.bcs.custodia.prt.AnularPrestamo | CRÍTICO | múltiples |
| AnularEmprestitoService | cl.bcs.custodia.prt.AnularEmprestito | CRÍTICO | múltiples |
| LiquidarCustodiaPrestamoService | cl.bcs.custodia.prt.LiquidarCustodiaPrestamo | CRÍTICO | cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.plataforma.pla.ConsultarFlujoCaja |
| LiquidarCustodiaEmprestitoService | cl.bcs.custodia.prt.LiquidarCustodiaEmprestito | CRÍTICO | múltiples |
| ModificarPrestamoService | cl.bcs.custodia.prt.ModificarPrestamo | ALTO | cl.bcs.plataforma.mer.CalcularParidad |
| AnticiparPrestamoService | cl.bcs.custodia.prt.AnticiparPrestamo | ALTO | múltiples |
| VencerPrestamosService | cl.bcs.custodia.prt.VencerPrestamos | ALTO | cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores |
| RenovarPrestamoMasivoService | cl.bcs.custodia.prt.RenovarPrestamoMasivo | ALTO | cl.bcs.plataforma.pla.RevisarRequisitosBasicos |

**4 servicios prt sin cross-service deps** (deferral injustificado): ConsultarPrestamistasDisponibles, ConsultarResumenEmprestitoHistorico, ConsultarResumenEmprestitoVigente, ConsultarResumenPrestamoVigente.

---

## STUBs de Dominio evc/ (Eventos de Capital) — 40 servicios

Todos los 40 servicios del dominio evc son STUBs. Se listan los de mayor riesgo:

| Servicio | Riesgo | Deps Cross-service |
|----------|--------|-------------------|
| ProcesarEventoCapitalDividendoEnDinero | CRÍTICO | cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos |
| ProcesarEventoCapitalCorteCupon | CRÍTICO | múltiples |
| ProcesarEventoCapitalEmisionPago | CRÍTICO | múltiples |
| CerrarEventoCapital | ALTO | cl.bcs.plataforma.pla.RevisarRequisitosBasicos |
| CrearEventoCapital | ALTO | múltiples |
| EliminarEventoCapital | ALTO | múltiples |
| CertificarEventoCapital | ALTO | múltiples |

**4 servicios evc sin cross-service deps** (deferral injustificado): BuscarMonedaPreferenciaPagoEventoCapitalCliente, ConsultarImpuestoDividendoDineroExtranjero, ConsultarImpuestoGastoEventoCapitalClienteDV, ProcesarRetencionIsfut.

---

## STUBs de Dominio utils/ — 11 servicios

Todos son STUBs, incluidas las utilidades que internamente son llamadas por otros servicios:

| Servicio | Riesgo | Nota |
|----------|--------|------|
| IngresoCustodiaService | CRÍTICO | Motor central de ingreso de custodia — llamado por decenas de servicios |
| RetiroCustodiaService | CRÍTICO | Motor central de retiro de custodia |
| ValorizarCustodiaService | ALTO | Valorización de custodias |
| ControlRetiroCustodiaService | ALTO | Validaciones de retiro |
| EstandarizacionRegistroCustodiaService | MEDIO | Normalización de registros |

**2 servicios utils sin cross-service deps**: IngresoCustodiaService, RetiroCustodiaService (paradójicamente, ambos son los más críticos pero fueron marcados DEFERRED sin justificación).

---

## Servicios sin contraparte modernizada (SIN_LEGADO invertido)

Los siguientes servicios del legado NO tienen un `*Service.java` directo en el código modernizado:

| Servicio Legado | FQN | Situación en Modernizado |
|-----------------|-----|-------------------------|
| ConsultarCarteras | `cl.bcs.custodia.ConsultarCarteras` | Solo existen los SQL files en `resources/`. No hay Service.java. Es una clase utilitaria interna; probablemente embedded en otros stubs. |
| ConsultarCarterasWS | `cl.bcs.custodia.ConsultarCarterasWS` | No hay Service.java directo. Posiblemente cubierto por ConsultarCustodiaPortafolioWSService (stub). |
| ConsultarCustodiaMovimientos | `cl.bcs.custodia.ConsultarCustodiaMovimientos` | Solo existe ConsultarCustodiaMovimientosAgenteService (stub). La versión base sin "Agente" no tiene Service.java. |
| AutorizacionIngresoTitulo | `cl.bcs.custodia.AutorizacionIngresoTitulo` | Particionado en Confirmar + Rechazo (ambos stubs). |
| AutorizacionRetiroTitulo | `cl.bcs.custodia.AutorizacionRetiroTitulo` | Particionado en Confirmar + Rechazo (ambos stubs). |
| CargaCustodiaCircular1518Utils | `cl.bcs.custodia.CargaCustodiaCircular1518Utils` | Clase utilitaria sin Service.java. Funcionalidad absorbida por CargarArchivoCircular1518Service (stub). |

---

## Clases de Infraestructura (no STUBs, pero tampoco equivalentes a un servicio legado específico)

| Clase | Descripción | Legado de origen |
|-------|-------------|-----------------|
| `JavaCallService` | Interfaz Spring Boot que reemplaza `cl.bcs.utils.JavaCall` | Multiple legacy classes |
| `CustodiaServiciosService` | Container de constantes y métodos utilitarios puros extraídos de varias clases legadas | LiquidarCustodiaEmprestito, ControlRetiroCustodia, GuardarPatrimonioRetroactivo, AnticiparPrestamo, ConsultarCustodiaPortafolio |

---

## 53 STUBs Sin Justificación Técnica (Acción Inmediata Posible)

Los siguientes servicios fueron marcados DEFERRED pero NO tienen dependencias cross-service que lo justifiquen. Son candidatos inmediatos para implementación:

### Alta prioridad (mutaciones de datos):
1. `service/ActualizarCustodiaOSAService.java` — `cl.bcs.custodia.ActualizarCustodiaOSA`
2. `service/IngresarEjercicioService.java` — `cl.bcs.custodia.IngresarEjercicio`
3. `service/NetearClientesService.java` — `cl.bcs.custodia.NetearClientes`
4. `service/NetearCustodiaNegativaService.java` — `cl.bcs.custodia.NetearCustodiaNegativa`
5. `service/ValorizarCustodiaMasivaService.java` — `cl.bcs.custodia.ValorizarCustodiaMasiva`
6. `service/ConfirmarAutorizacionRetiroTitulosService.java`
7. `service/ConfirmarAutorizacionIngresoTitulosService.java`
8. `service/RechazoAutorizacionRetiroTitulosService.java`
9. `service/RechazoAutorizacionIngresoTitulosService.java`
10. `service/CrearCargaMasivaISFUTService.java`
11. `service/CrearEnvioOperacionesPendientesService.java`
12. `service/EliminaReservaService.java`
13. `service/FusionCarterasService.java`
14. `service/GuardarCuadraturaCustodiaService.java`
15. `service/GuardarPatrimonioCustodiaService.java`
16. `service/IngresoReservaService.java`
17. `service/LimpiarCargasCircular1518Service.java`
18. `service/NetearClientesService.java`
19. `service/ProcesaCargaMasivaCustodiaReporteService.java`
20. `service/RetiraCustodiaDeMovimientoNominalService.java`
21. `service/RetirarCustodiaOSAService.java`

### Media prioridad (carga de datos y archivos):
22. `service/CargarArchivoCircular1518Service.java`
23. `service/CargarArchivoCircularCarteraClienteService.java`
24. `service/CargarPosicionesArchivoL02Circular1518Service.java`
25. `service/CargarPosicionesOtrosSaldosCircular1518Service.java`
26. `service/CargarSorteoLetrasArchivoL043Circular1518Service.java`
27. `service/CargaArchivoBONYService.java`
28. `service/CargaArchivoCavaliService.java`
29. `service/CargaArchivoLEKService.java`
30. `service/CargaDividendosEnDineroService.java`
31. `service/CargaSimultaneasDesdeSebraService.java`
32. `service/CambioNemosService.java`
33. `service/CertificaSuscripcionOSAMasivoService.java`

### Menor prioridad (consultas):
34. `service/ConsultaCarterasSuscripcionClienteService.java`
35. `service/ConsultarBalanceCustodiaService.java`
36. `service/ConsultarCoberturaGarantiaResumenService.java`
37. `service/ConsultarEjercicioBloqueoService.java`
38. `service/ConsultarEventoCapitalClienteService.java`
39. `service/ConsultarGarantiasAgrupadasService.java`
40. `service/ConsultarMovimientosEnTransitoTotalizadorService.java`
41. `service/ConsultarSimultaneasVigentesService.java`
42. `service/ConsultarSorteoContratosService.java`
43. `service/GenerarCuadraturaCustodiaCircular1518Service.java`
44. `service/GenerarCuadraturaCustodiaCircular1548Service.java`

### Subdominios prt, evc, utils (10 adicionales):
45. `service/prt/ConsultarPrestamistasDisponiblesService.java`
46. `service/prt/ConsultarPrestamistasDisponiblesSinCustodiaService.java`
47. `service/prt/ConsultarResumenEmprestitoHistoricoService.java`
48. `service/prt/ConsultarResumenEmprestitoVigenteService.java`
49. `service/evc/BuscarMonedaPreferenciaPagoEventoCapitalClienteService.java`
50. `service/evc/ConsultarImpuestoDividendoDineroExtranjeroService.java`
51. `service/evc/ConsultarImpuestoGastoEventoCapitalClienteDVService.java`
52. `service/utils/IngresoCustodiaService.java`
53. `service/utils/RetiroCustodiaService.java`

---

## Servicios No Auditados (fuera de scope)

Los siguientes 4 servicios legados son clases utilitarias internas (no JavaCall) y no tienen Service.java equivalente en modernizado (no es una omisión — son helpers):

- `cl.bcs.custodia.CargaCustodiaCircular1518Utils` — utilidad de parseo de archivos
- `cl.bcs.custodia.ConsultarCarteras` — helper interno de consulta de carteras, SQL files migrados
- `cl.bcs.custodia.ConsultarCarterasWS` — variant WS cubierta por ConsultarCustodiaPortafolioWS
- `cl.bcs.custodia.AutorizacionIngresoTitulo` / `AutorizacionRetiroTitulo` — divididos en Confirmar+Rechazo (en modernizado, pero STUBs)

---

## Conclusiones y Recomendaciones

### Estado general
La modernización de BcsCustodiaServicios está en **FASE DE SCAFFOLDING INCOMPLETO**. Los 180 servicios son únicamente esqueletos Spring Boot sin lógica de negocio. El módulo no puede ir a producción.

### Prioridad inmediata
1. **Implementar los 53 STUBs sin justificación** — No requieren clientes HTTP hacia otros módulos. Solo requieren portar la lógica Java + SQL del legado al framework Spring Boot. Estimado: 2-3 sprints.

2. **Diseñar e implementar los clientes HTTP inter-servicio** para las dependencias cross-service más frecuentes:
   - `cl.bcs.cuentaInversion.*` — aparece en ~15 servicios
   - `cl.bcs.plataforma.mer.CalcularParidad` — aparece en ~8 servicios
   - `cl.bcs.tesoreria.CrearIngresoEgreso` — aparece en ~6 servicios
   - `cl.bcs.plataforma.pla.AgregarLog` — aparece en ~5 servicios
   - `cl.bcs.plataforma.pla.RevisarRequisitosBasicos` — aparece en ~7 servicios

3. **Priorizar por riesgo operativo**: Primero los servicios de Liquidación (LiquidacionOpciones, LiquidacionFuturos, EjecutarVencimiento), luego los de Anulación (AnularGarantia, AnularCarteraNegativa), luego los de préstamos/empréstitos.

### Assets reutilizables en modernizado
- Los archivos SQL han sido correctamente migrados a `src/main/resources/cl/bcs/custodia/sql/`
- La infraestructura Spring Boot (controller, application, datasource) está configurada
- El patrón `JavaCallService` (interfaz) y `CustodiaServiciosService` (utility) están disponibles
