# BcsCartolasMaster — Transformation Notes

## Fuente
- **Legacy:** `BSCL/Normativa/Java/BcsCartolasMaster/`
- **Stack legacy:** Java 8 / Java EE, JasperReports 6.x, JFreeChart 1.0.x, Apache DBCP2, HANA JDBC directo
- **Stack moderno:** Spring Boot 4.1.1 / Java 25, Hexagonal Architecture
- **Puerto:** 8105

---

## Qué hace el módulo

BcsCartolasMaster genera **Cartolas Master** — estados de cuenta de inversión en formato PDF multi-página para clientes de la corredora. Cada cartola:

1. Se compone de hasta 4 páginas JasperReports: portada con resumen consolidado (Pag0), detalle de portafolio (Pag1), próximos eventos / observaciones (Pag2) y grillas de cartera (Pag4, una por tipo de instrumento).
2. Puede incluir gráficos embebidos: torta de composición del activo (JFreeChart pie) y serie temporal de patrimonio (JFreeChart line).
3. Se genera en dos modos:
   - **Online** (`generarOnline`): para visualización inmediata de un cliente/portafolio.
   - **Batch** (`ProcesoEmisionMaster`): thread pool paralelo sobre una nómina completa; archivado en ZIP.

---

## Arquitectura hexagonal aplicada

```
cl.bcs.cartolas.master/
  domain/
    CartolaMaster.java              ← value object (nombreArchivo + bytes[])
    ReporteMaster.java              ← enum con 31 configuraciones de reporte JR
    GraficoMaster.java              ← enum con 3 tipos de gráfico JFreeChart
    ImagenMaster.java               ← enum con 2 recursos de imagen (banner, logo)
    PaletaColores.java              ← mapa estático de colores por clase de activo
    ConfiguracionCartola.java       ← record CAR.Config
    ParametroCartola.java           ← record CAR.Parametro
  port/in/
    CartolaMasterUseCase.java       ← obtenerConfiguracion, obtenerParametros, generarOnline, generarPorIdCartola
  port/out/
    CartolaMasterDataPort.java      ← findConfiguracionByEmpresa, findParametrosByNomina
  service/
    CartolaMasterService.java       ← implements CartolaMasterUseCase; pure-logic static: incluir(), construirNombreArchivo()
    GraficadorPastelService.java    ← @Service; pure-logic static: graficar(Map, titulo) → BufferedImage 217×152
    GraficadorSerieService.java     ← @Service; pure-logic static: graficar(TimeSeries, titulo) → BufferedImage 780×570
  adapter/in/
    CartolaMasterController.java    ← @RestController /cartolas-master/*
    GlobalExceptionHandler.java     ← NPE/IAE/NFE→400, UOE→503
  adapter/out/
    CartolaMasterRepositoryAdapter.java ← NamedParameterJdbcTemplate + MapSqlParameterSource
  BcsCartolasMasterApplication.java
```

---

## Decisiones de transformación

### 1. JasperReports y HANA — @Disabled en tests
La generación de PDFs requiere:
- Templates `.jrxml` compilados y cargados desde HANA (`CAR.Plantilla`, `CAR.Recurso`)
- Stored procedures HANA (`CAR.GrabarNomina`, `CAR.GrabarParametro`)
- DataSources persistentes con datos del portfolio del cliente

Ambas operaciones (`generarOnline`, `generarPorIdCartola`) lanzan `UnsupportedOperationException` en producción sin HANA y están marcadas `@Disabled("requires HANA")` en 2 tests adicionales. El `GlobalExceptionHandler` mapea UOE → 503.

### 2. JFreeChart — pure-logic testeable sin Spring
Los `GraficadorPastelService` y `GraficadorSerieService` tienen la lógica de gráficos en métodos `public static` siguiendo el patrón obligatorio. Los tests unitarios verifican dimensiones (217×152 y 780×570 px), exclusión de valores cero en pie chart, y manejo de series vacías — sin levantar el contexto Spring.

### 3. Esquema H2 — nombres planos
Las tablas HANA usan nombres quoted con namespace (p.e. `"BSCL.Normativa.BD::CAR.Config"`). En el perfil test se mapean a tablas H2 con nombres planos (`car_config`, `car_parametro`) definidas en `schema-cartolas-master.sql`. El adaptador JDBC usa la misma query SQL sin cambios.

### 4. ReporteMaster enum — 31 constantes preservadas
El enum `ReporteMaster` conserva todos los campos del legacy (nombreArchivo, nombreReporteBaseDato, nombreDataSource, nombreParametroReporte, nombreParametroDataSource, nombreParametroInclusion, tipoSubReport, reportePadre, reporteConsolidado, poseeSubReporteCabecera, poseeSubReporteDetalle) y agrega el método `tieneParametroInclusion()` para simplificar la lógica `incluir()`.

### 5. main() — sólo en BcsCartolasMasterApplication
El `main()` de debug en `GeneradorMaster` (con credenciales hardcodeadas) fue eliminado. Solo queda el `main()` de Spring Boot en `BcsCartolasMasterApplication`.

### 6. Objects.requireNonNull — guardas en todos los puertos
Todos los métodos de puerto y servicio validan sus argumentos con `Objects.requireNonNull` antes de delegación.

---

## Comportamientos cubiertos en tests

| ID | Descripción | Tipo | Estado |
|----|-------------|------|--------|
| B-CM-01 | CartolaMaster value object: constructor, getters, sizeBytes | Unit | PASS |
| B-CM-02 | ReporteMaster enum: 31 constantes, tieneParametroInclusion | Unit | PASS |
| B-CM-03 | GraficoMaster enum: datasource y reportePadre | Unit | PASS |
| B-CM-04 | ImagenMaster enum: banner.png y logo.gif | Unit | PASS |
| B-CM-05 | PaletaColores: 7 colores, lookup por clave, null para desconocido | Unit | PASS |
| B-CM-06 | CartolaMasterService.incluir: sin parámetro→true, con true→true, con false→false, ausente→false | Unit | PASS |
| B-CM-07 | CartolaMasterService.construirNombreArchivo: formato correcto, null guards | Unit | PASS |
| B-CM-08 | GraficadorPastelService: imagen 217×152, ceros excluidos, null guard | Unit | PASS |
| B-CM-09 | GraficadorSerieService: imagen 780×570, serie vacía, null guard | Unit | PASS |
| B-CM-10 | REST GET /configuracion/1 → 4 items H2 | Integration | PASS |
| B-CM-11 | REST GET /configuracion/2 → 1 item H2 | Integration | PASS |
| B-CM-12 | REST GET /configuracion/999 → array vacío | Integration | PASS |
| B-CM-13 | REST GET /parametros/100 → 4 items | Integration | PASS |
| B-CM-14 | REST GET /parametros/200 → 1 item | Integration | PASS |
| B-CM-15 | REST GET /parametros/999 → array vacío | Integration | PASS |
| B-CM-16 | POST /generar-online sin HANA → 503 | Integration | PASS |
| B-CM-17 | POST /generar/99 sin HANA → 503 | Integration | PASS |
| B-CM-DH-1 | generarOnline (HANA) | @Disabled | SKIP |
| B-CM-DH-2 | generarPorIdCartola (HANA) | @Disabled | SKIP |

**Total:** 40 tests ejecutados, 0 fallos, 0 errores, 2 @Disabled (HANA).

---

## Credenciales eliminadas
El `main()` de `GeneradorMaster` legacy contenía credenciales hardcodeadas:
```java
String user = "RULLOA";
String pass = "B0lsa123456";
```
Estas fueron **eliminadas**. La configuración de producción usa variables de entorno `${HANA_URL}`, `${HANA_USER}`, `${HANA_PASSWORD}`.

---

## Canary test
```
Tests run: 40, Failures: 0, Errors: 0, Skipped: 2 — BUILD SUCCESS
```
