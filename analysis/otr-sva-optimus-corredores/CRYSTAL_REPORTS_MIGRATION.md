# Crystal Reports Migration Report
## otr-sva-optimus-corredores

**Generado:** 2026-10-05  
**Alcance:** 160 archivos `.rpt` en 10 dominios de negocio, 36 Java callers  
**Bloqueante Phase 4:** BcsFacturacionServicios (DatosComprobanteCrystal / GenerarInformePrefacturas / GenerarInformeIGK)

---

## 1. Hallazgo arquitectural crítico

**Los templates `.rpt` NO se leen desde el filesystem en producción.**

`BcsPlataformaReporteApi/ReportDocument.java` los lee desde la tabla HANA:
```
"BSCL.Report::Template" (columna "ReportFile" tipo BLOB, "Id" tipo UUID string)
```

El flujo real es:
1. `report.readDocument(uuid)` → SELECT from HANA BLOB → escribe `.rpt` a `tmp/` → abre con `ReportClientDocument`
2. El `.rpt` contiene SQL embebido que se ejecuta contra HANA via el `DataSource` pasado al constructor
3. `report.exportDocument(format)` → Crystal runtime genera PDF/Excel/CSV/Word/RTF como `InputStream`

Los 160 archivos `.rpt` en el filesystem son **copias de diseño** (para editar en SAP Crystal Reports Designer). La fuente de verdad es la tabla HANA. Esto tiene consecuencias directas en la estrategia de migración (ver §6).

---

## 2. Inventario completo por dominio

| Dominio | #rpt | Tamaño total | Rango KB | Tipo predominante | Prioridad |
|---|---|---|---|---|---|
| Mesa | 55 | ~7.3 MB | 39–550 KB | Contrato/Informe FWD/MRF | Alta |
| Normativa | 35 | ~5.6 MB | 49–548 KB | Certificado regulatorio | **Crítica** |
| FondoMutuo | 18 | ~2.6 MB | 49–298 KB | Template operacional FFMM | Alta |
| Custodia | 13 | ~1.9 MB | 59–198 KB | Comprobante/Certificado/Traspaso | Alta |
| Orden | 9 | ~1.7 MB | 4–413 KB | Comprobante + Palos (datos) | Alta |
| Tesoreria | 7 | ~0.8 MB | 81–127 KB | Carta instrucción/Comprobante | Alta |
| Contabilidad | 7 | ~0.7 MB | 74–145 KB | Informe operaciones | Media |
| Facturacion | 5 | ~0.4 MB | 53–97 KB | Comprobante/Boleta/Informe | Alta (**Phase 4**) |
| APV | 3 | ~4.6 MB | 116–4,200 KB | Contrato/Notificación | Alta |
| CuentaInversion | 2 | ~0.2 MB | 80–117 KB | Comprobante/Anexo | Alta |
| **Total** | **160** | **~25.8 MB** | — | — | — |

**Nota APV:** `NOTIFICACIONEMPLEADORSAAPV.rpt` pesa 4.2 MB — probablemente contiene imágenes/subreportes embebidos. Requiere análisis especial.

---

## 3. Java callers — cómo se usa Crystal Reports hoy

La arquitectura centraliza Crystal en `BcsPlataformaReporteApi`. Todos los callers siguen el mismo patrón:

```java
ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);
report.readDocument(templateId);  // templateId = UUID en HANA
// ... setear parámetros del reporte si aplica ...
InputStream is = report.exportDocument("pdf");  // pdf | excel | csv | word | rtf
```

| Clase Java | Módulo | Template cargado | Parámetros | Formato salida |
|---|---|---|---|---|
| `GenerarInformePrefacturas` | BcsFacturacionServicios | UUID desde HANA (por tipo comprobante) | `DatosComprobanteCrystal` (107 campos) | PDF |
| `GenerarInformeIGK` | BcsFacturacionServicios | UUID desde HANA (`obtenerIdReporte`) | `DatosCertificadoCompleto` (imagen+datos) | PDF |
| `CrearInformesCoberturaPatrimonial` | BcsMesaServicios | UUID dinámico | Via DataSource | PDF |
| `ActualizarPosicionInicialDiaSiguiente` | BcsMesaServicios | UUID dinámico | Via DataSource | — |
| `ConsultarCalculadoraSpot` | BcsMesaServicios | UUID dinámico | Via DataSource | PDF/Excel |
| `GenerarArchivoInformeCarteraCuentaPropia` | BcsMesaJobs | UUID dinámico | Via DataSource | PDF/Excel |
| `GenerarReporteCuadratura` | BcsCustodiaServicios | UUID dinámico | Via DataSource | PDF |
| `CerrarEventoCapitalCorteCupon` | BcsCustodiaServicios | UUID dinámico | Via DataSource | PDF |
| `GuardarPatrimonioRetroactivo` | BcsCustodiaServicios | UUID dinámico | Via DataSource | PDF |
| `GenerarArchivoInformeMayorMenorValor` | BcsCustodiaJobs | UUID dinámico | Via DataSource | PDF |
| `GenerarArchivoInformeSimultaneas` | BcsCustodiaJobs | UUID dinámico | Via DataSource | PDF |
| Normativa (21 callers) | BcsNormativaServicios/Jobs | UUID por certificado (Cert4,7,8,9,17,21,24,31,36,43,71 + CIR+UAF+CI63) | Via DataSource | PDF masivo |
| FondoMutuo (2 callers) | BcsFondoMutuoServicios | UUID dinámico | Via DataSource | PDF/Excel |

**Patrón de datos:** La mayoría de templates ejecutan su propio SQL embebido contra HANA via el `DataSource`. Solo `GenerarInformePrefacturas` pasa un objeto Java (`DatosComprobanteCrystal`) como fuente de datos adicional — los demás confían en que Crystal ejecuta el SQL del `.rpt`.

---

## 4. Análisis de DatosComprobanteCrystal.java

**107 campos públicos** (extends BcsPojo), todos son `public` (no getters/setters):

| Categoría | Campos | Descripción |
|---|---|---|
| Identificación comprobante | 5 | IdComprobante, FolioComprobante, IdTipoComprobante, Folio, FechaComprobante |
| Receptor | 10 | DocumentoIdentidadReceptor, RazonSocialReceptor, Dirección, Ciudad, etc. |
| Emisor | 8 | RazonSocialEmisor, DocumentoIdentidadEmisor, Dirección, etc. |
| Montos | 12 | Monto, MontoLiquidacion, Comisiones, Impuestos, Derechos, Retención, etc. |
| Instrumento | 8 | Nemotecnico, Cantidad, Precio, PrecioOtraMoneda, Tasa, etc. |
| Monedas | 10 | IdMoneda*, CodigoMoneda*, SimboloMoneda* (múltiples monedas) |
| Identificadores empresa | 4 | NombreEmpresa, Sucursal, LogoCorredora (base64 blob), etc. |
| Referencias legales | 8 | GlosaNCredito, IdTipoComprobanteAsocANC, Folios referencia, etc. |
| Campos PE (Perú) | 5 | TipoDocumentoIdentidadReceptorpE, ComisionBVL, ComisionSAB, etc. |
| Otros | ~35 | Glosas, códigos, flags, fechas adicionales |

**Complejidad:** Alta. El bean tiene variantes por país (PE = Perú), por tipo de comprobante (FAC/BOL/NCE/POL), y campos que se usan o no según el template. No hay herencia — un solo POJO "paraguas" para todos los tipos.

**Implicación JasperReports:** `JRBeanCollectionDataSource(List.of(datosCrystal))` funcionaría para reemplazar la alimentación de datos. Los 107 campos serían `$F{NombreCampo}` en el `.jrxml`. La complejidad está en recrear el layout, no en la integración Java.

---

## 5. Complejidad estimada de migración por dominio

| Dominio | #rpt | Tipo predominante | Complejidad layout | SQL embebido | Notas |
|---|---|---|---|---|---|
| **Facturacion** | 5 | Comprobante comercial/Boleta | Media | Sí (moderado) | Phase 4 blocker. Variantes PE. |
| **Normativa** | 35 | Certificados regulatorios | **Alta** | Sí (complejo) | Formato legal exacto obligatorio. Generación masiva. Certificados numerados (4,7,8,9,17,21,24,31,36,43,71). |
| Mesa | 55 | Contratos FWD/MRF | **Alta** | Sí (muy complejo) | Múltiples variantes por banco/contraparte. Algunos >500KB. |
| FondoMutuo | 18 | Templates operacionales | Alta | Sí | Múltiples tipos de operación FFMM. |
| Custodia | 13 | Comprobante/Traspaso/Certificado | Media-Alta | Sí | Duplicados (mayúsculas vs. camelCase). |
| APV | 3 | Contrato/Notificación | **Alta** | Sí | NOTIFICACIONEMPLEADORSAAPV.rpt = 4.2 MB (posiblemente con imágenes embebidas o subreportes masivos). |
| Tesoreria | 7 | Carta instrucción pago | Media | Sí | Variantes por banco (BcoChile, BcoEstado, BcpPE). |
| Contabilidad | 7 | Informe tabular | Baja-Media | Sí | Informes internos, menor criticidad de layout exacto. |
| CuentaInversion | 2 | Comprobante/Anexo16 | Media | Sí | Requisito Anexo16 → probablemente formato CMF. |
| Orden | 9 | Comprobante + Palos | Media | Sí | "Palos" son datos de referencia (algunos <5KB = casi vacíos). |

---

## 6. Opciones de reemplazo

### Consideración previa crítica

**No es "elegir una librería de PDF"** — es reemplazar una infraestructura completa:
- `BcsPlataformaReporteApi` es un motor compartido por 10 dominios
- Los templates están en HANA como BLOBs (no en filesystem)
- Los templates ejecutan SQL propio contra HANA (Crystal pull-model)
- La interfaz pública es `readDocument(uuid)` + `exportDocument(format)`

La estrategia más eficiente es **reemplazar el motor dentro de `BcsPlataformaReporteApi`** manteniendo la misma interfaz, minimizando impacto en los 36 callers.

---

| Opción | Ventajas para este sistema | Desventajas | Esfuerzo templates |
|---|---|---|---|
| **JasperReports 7.x** | • JDBC DataSource nativo (mismo patrón pull) • Almacena `.jasper` en DB igual que `.rpt` • `JRBeanCollectionDataSource` para DatosComprobanteCrystal • Spring Boot 4.1.1: `net.sf.jasperreports:jasperreports:7.0.1` • Exporta PDF/Excel/CSV/Word/RTF | • SQL embebido debe reescribirse en `.jrxml` • No hay conversión automática de `.rpt` a `.jrxml` — rediseño visual en Jasper Studio requerido • Requiere equipo con conocimiento de JR | 160 templates nuevos en Jasper Studio |
| **Thymeleaf + Flying Saucer (HTML→PDF)** | • Templates en HTML/CSS — más fáciles de mantener • Cualquier dev Java puede editarlos • Control total sobre layout | • Paginación compleja es difícil en CSS • No soporta Excel nativo • Para 160 templates complejos el effort es igual o mayor | 160 templates HTML desde cero |
| **Apache FOP (XSL-FO→PDF)** | • Control de layout exacto (mejor para documentos legales con formato fijo) | • XSL-FO es verboso y difícil de mantener • No soporta Excel | 160 templates XSL-FO desde cero |
| **iText / OpenPDF** | • Solo PDF • Control programático total | • Sin sistema de templates → toda la lógica de layout en Java | N/A — requiere programar layout |
| **Mantener Crystal (corto plazo)** | • Cero esfuerzo de templates • Funciona hoy | • Dependencia de SAP Crystal Runtime (licencia) • No compatible con Spring Boot standalone • `com.crystaldecisions.*` no está en Maven Central | 0 (status quo) |

---

## 7. Recomendación

**JasperReports 7.x como motor de reemplazo, con `BcsPlataformaReporteApi` como capa de abstracción.**

Rationale basado en hallazgos concretos:

1. **El patrón JDBC pull es idéntico** — JasperReports acepta un `java.sql.Connection` como data source; el SQL del template se migra al `.jrxml`, nada cambia en el caller.
2. **La interfaz puede preservarse** — reemplazar `BcsPlataformaReporteApi` internamente: `readDocument(uuid)` lee el BLOB de HANA (ahora un `.jasper` compilado), `exportDocument(format)` llama a `JasperExportManager`. Los 36 callers no se tocan.
3. **Almacenamiento en HANA** — JasperReports compila `.jrxml` → `.jasper` (bytes); se almacena en la misma tabla `"BSCL.Report::Template"` como BLOB nuevo. La gestión de templates es idéntica.
4. **DatosComprobanteCrystal** → `JRBeanCollectionDataSource` es trivial: `new JRBeanCollectionDataSource(List.of(datos))` + campos como `$F{NombreCampo}` en `.jrxml`.

**No recomendado a corto plazo:** Thymeleaf/FOP requieren rediseñar 160 templates desde cero en tecnologías nuevas, con el mismo esfuerzo que JR pero sin el ecosistema de herramientas de diseño visual.

---

## 8. Esfuerzo estimado (en número de templates)

El esfuerzo real de migración es el rediseño visual en Jasper Studio, no el código Java.

| Dominio | #templates | Estimación complejidad/template | Total unidades diseño |
|---|---|---|---|
| Normativa | 35 | Alta (certificados regulatorios con formato CMF/SVS) | 35 × Alto |
| Mesa | 55 | Alta (contratos legales multipage, variantes banco) | 55 × Alto |
| FondoMutuo | 18 | Media-Alta | 18 × Medio |
| Custodia | 13 | Media (algunos duplicados reutilizables) | ~10 × Medio |
| APV | 3 | Alta (NOTIFICACIONEMPLEADOR = 4.2MB, subreportes) | 3 × Alto |
| Tesoreria | 7 | Media (variantes por banco, layout similar) | ~4 × Medio (reutilizables) |
| Facturacion | 5 | Media (variantes PE, DatosComprobanteCrystal) | 5 × Medio |
| Contabilidad | 7 | Baja-Media (informes tabulares internos) | 7 × Bajo |
| CuentaInversion | 2 | Media (Anexo16 = formato regulatorio) | 2 × Medio |
| Orden | 9 | Media (Palos ~vacíos, 2 comprobantes reales) | ~4 × Medio |

**Recomendación de secuencia:** Contabilidad (7, menor criticidad) → Facturacion (5, Phase 4 blocker) → Tesoreria (7, variantes reutilizables) → Custodia (13) → CuentaInversion (2) → APV (3) → FondoMutuo (18) → Normativa (35) → Mesa (55).

---

## 9. Ruta crítica

### Phase 4 exit blocker (solo Facturacion)

Únicamente 3 Java files en BcsFacturacionServicios usan Crystal:
- `DatosComprobanteCrystal.java` — POJO de 107 campos (no requiere cambio)
- `GenerarInformePrefacturas.java` — carga template por UUID, exporta PDF
- `GenerarInformeIGK.java` — certificado IGA, carga template por UUID, exporta PDF

Los 5 templates Facturacion:
- `ComprobanteFacturacion.rpt` (59 KB)
- `ComprobanteFacturacionPE.rpt` (97 KB)
- `ComprobanteFacturacionPEPol.rpt` (83 KB)
- `BoletaFacturacion.rpt` (68 KB)
- `InformeGananciaCapital.rpt` (53 KB)

**Para desbloquear Phase 4 exit:**
1. Reemplazar `BcsPlataformaReporteApi` con implementación JasperReports (nuevo módulo `BcsPlataformaReporteApi-Jasper`)
2. Migrar los 5 templates Facturacion a `.jrxml` en Jasper Studio
3. Compilar a `.jasper`, subir a tabla HANA `"BSCL.Report::Template"`
4. `GenerarInformePrefacturas` y `GenerarInformeIGK` no requieren cambios Java

### Diferible a fase dedicada (no bloquea Phase 4)

Todo el resto — Mesa (55), Normativa (35), FondoMutuo (18), Custodia (13), APV (3), Tesoreria (7), Contabilidad (7), CuentaInversion (2), Orden (9) — pertenece a módulos que aún no están en scope de transformación (BcsMesaServicios, BcsNormativaServicios, BcsFondoMutuoServicios no están en las fases 1-4 definidas). Se recomienda una **Fase de Migración de Reportes** independiente una vez que el motor JR esté validado con los 5 templates de Facturacion.

---

## 10. Dependencias adicionales a resolver

| Dependency | Situación | Acción requerida |
|---|---|---|
| `com.crystaldecisions.sdk.*` | JAR propietario SAP, no en Maven Central | Reemplazar con `net.sf.jasperreports:jasperreports:7.0.1` |
| `org.krysalis.barcode4j` | Ya en `ReportDocument.java` para barcodes | Compatible con JR via `JRBarcode4JComponent` |
| `org.apache.poi` | Ya en codebase (Excel) | Compatible con JR Excel export |
| `org.apache.fop` (avalon-framework) | Usado para PDF417 en `ReportDocument.java` | Evaluar si se mantiene o reemplaza con JR barcode |
| Tabla `"BSCL.Report::Template"` | HANA BLOB store | Agregar columna `TemplateType` (CRYSTAL/JASPER) para coexistencia durante migración |
| Crystal Runtime License SAP | Costo recurrente | Eliminar tras migración completa |
