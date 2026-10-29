# Lineas de codigo del legacy (2026-10-21)

Fuente: `legacy/otr-sva-optimus-corredores` (solo lectura), excluyendo las copias derivadas de `bin/`, `build/` y `out/`. "Lineas de codigo" = sin lineas en blanco ni comentarios completos (`//`, `--`, `/* */`); la clasificacion de comentarios es aproximada (no cuenta comentarios al final de una linea de codigo). Se cuentan solo `.java` y `.sql` (no XML, HTML, JS, properties ni Gradle).

## 1. Totales

| | Lineas de codigo |
|---|---:|
| Java (todo el legacy) | 793,585 |
| SQL (todo el legacy) | 1,011,780 |

## 2. Codigo EFECTIVO a modernizar

**Java a modernizar** = codigo de produccion (sin pruebas) de los modulos que el brief aprobado incluye en las fases 1 a 5 (42 modulos con carpeta de codigo): **765,652 lineas**. Quedan fuera 21,129 lineas de Java de modulos que el brief no incluye (ver seccion 4) y el codigo de pruebas del legacy.

**SQL a modernizar (mover y verificar, no reescribir)** = las consultas y sentencias embebidas en los modulos Java (`src/**/sql/*.sql`, usadas por los servicios y que viajan con cada modulo como recursos): **188,823 lineas** en 7,480 archivos. El SQL de `BD/` (vistas, rutinas y scripts de tablas/datos: 801,711 lineas) **permanece en HANA** y no se reescribe: se verifica contra HANA en una fase de pruebas de integracion; las consultas de reportes (21,246 lineas) pertenecen al sidecar de Crystal y no se modernizan.

| Alcance | Java | SQL |
|---|---:|---:|
| A modernizar (modulos del brief) | 765,652 | 188,823 (embebido, se mueve y se verifica) |
| Permanece en HANA (vistas, rutinas, scripts) | n/a | 801,711 |
| Cuarentena Crystal (consultas de reportes) | n/a | 21,246 |
| Fuera del alcance del brief | 21,129 | (ver seccion 4) |

## 3. Java a modernizar por dominio y modulo (produccion)

| Dominio | Modulos | Lineas Java |
|---|---:|---:|
| Plataforma | 9 | 122,690 |
| Normativa | 7 | 111,474 |
| SocioNegocio | 2 | 106,179 |
| Custodia | 2 | 102,138 |
| Orden | 3 | 69,028 |
| FondoMutuo | 2 | 60,565 |
| Mesa | 2 | 57,016 |
| Facturacion | 2 | 36,276 |
| APV | 2 | 26,302 |
| Tesoreria | 2 | 23,832 |
| CuentaInversion | 2 | 20,349 |
| Contabilidad | 2 | 18,122 |
| Portafolio | 1 | 7,071 |
| Corredor | 2 | 3,453 |
| Replica | 1 | 904 |
| CanalAgente | 1 | 253 |
| **Total** | **42** | **765,652** |

| Modulo | Dominio | Archivos | Lineas Java |
|---|---|---:|---:|
| BcsSocioNegocioServicios | SocioNegocio | 558 | 104,899 |
| BcsCustodiaServicios | Custodia | 286 | 96,409 |
| BcsPlataformaServicios | Plataforma | 432 | 82,016 |
| BcsNormativaServicios | Normativa | 225 | 66,429 |
| BcsFondoMutuoServicios | FondoMutuo | 175 | 58,637 |
| BcsOrdenServicios | Orden | 188 | 57,763 |
| BcsMesaServicios | Mesa | 124 | 53,668 |
| BcsNormativaJobs | Normativa | 229 | 35,378 |
| BcsFacturacionServicios | Facturacion | 104 | 34,597 |
| BcsAPVServicios | APV | 73 | 25,224 |
| BcsTesoreriaServicios | Tesoreria | 62 | 21,516 |
| BcsPlataformaJobs | Plataforma | 153 | 18,143 |
| BcsCuentaInversionServicios | CuentaInversion | 44 | 17,976 |
| BcsContabilidadServicios | Contabilidad | 104 | 16,931 |
| BcsOrdenJobs | Orden | 29 | 9,630 |
| BcsPortafolioServicios | Portafolio | 25 | 7,071 |
| BcsUtils | Plataforma | 76 | 7,045 |
| BcsPlataformaWeb | Plataforma | 38 | 6,199 |
| BcsValo2 | Plataforma | 87 | 6,045 |
| BcsCustodiaJobs | Custodia | 37 | 5,729 |
| BcsCartolas | Normativa | 56 | 3,636 |
| BcsNormativaCore | Normativa | 65 | 3,367 |
| BcsMesaJobs | Mesa | 17 | 3,348 |
| BcsCuentaInversionJobs | CuentaInversion | 23 | 2,373 |
| BcsTesoreriaJobs | Tesoreria | 12 | 2,316 |
| BECB | Corredor | 9 | 2,275 |
| BcsJobsFondoMutuo | FondoMutuo | 18 | 1,928 |
| BcsFacturacionJobs | Facturacion | 14 | 1,679 |
| BcsJobOrders | Orden | 7 | 1,635 |
| BcsJobManager | Plataforma | 11 | 1,548 |
| BcsCartolasMaster | Normativa | 12 | 1,542 |
| BcsSocioNegocioJobs | SocioNegocio | 13 | 1,280 |
| BcsContabilidadJobs | Contabilidad | 8 | 1,191 |
| SURA | Corredor | 6 | 1,178 |
| BcsAPVJobs | APV | 4 | 1,078 |
| BcsCartolasJobs | Normativa | 15 | 1,016 |
| BcsReplica | Replica | 5 | 904 |
| BcsPlataformaReporteApi | Plataforma | 4 | 888 |
| BcsPortalConfirmacionWeb | Plataforma | 4 | 485 |
| BcsFilters | Plataforma | 2 | 321 |
| BcsCanalAgenteServicios | CanalAgente | 1 | 253 |
| BcsTributarioJobs | Normativa | 1 | 106 |

## 4. Java fuera del alcance del brief

| Modulo | Dominio | Lineas Java (produccion) | Lineas de pruebas |
|---|---|---:|---:|
| BcsCargasPeru | Plataforma | 11,567 | 0 |
| BcsPlataformaTests | Plataforma | 0 | 5,871 |
| BcsTesoreriaWebServices | Tesoreria | 3,865 | 0 |
| BcsDeploy | Plataforma | 2,891 | 0 |
| kaiser-odata | Plataforma | 1,888 | 0 |
| BcsPlataformaServlets | Plataforma | 832 | 0 |
| BcsContabilidadServiciosTest | Contabilidad | 0 | 268 |
| BcsPlataformaJobsTest | Plataforma | 0 | 143 |
| BcsCuentaInversionServiciosTest | CuentaInversion | 0 | 88 |
| BcsCanalCliente | CanalCliente | 86 | 0 |
| BcsReplicaTests | Replica | 0 | 45 |
| gradle_tests | Plataforma | 0 | 18 |

## 5. SQL por categoria

| Categoria | Archivos | Lineas de codigo |
|---|---:|---:|
| queries embedded in Java modules | 7,480 | 188,823 |
| BD views (stay in HANA) | 2,117 | 119,498 |
| BD service routines (stay in HANA) | 1,579 | 360,549 |
| BD scripts: DDL/seed data (stay in HANA) | 1,005 | 293,150 |
| BD other (stay in HANA) | 183 | 28,514 |
| report queries (Crystal sidecar) | 111 | 21,246 |

| Dominio | queries embedded in Java modules | BD views | BD service routines | BD scripts: DDL/seed data | BD other | report queries |
|---|---:|---:|---:|---:|---:|---:|
| APV | 3,768 | 3,315 | 2,006 | 2,357 | - | 773 |
| CanalAgente | 18 | - | 312 | 68 | - | - |
| CanalCliente | - | - | 452 | - | - | - |
| Contabilidad | 18,018 | 1,114 | 8,919 | - | 4,712 | 3,275 |
| Corredor | 1,511 | - | - | - | - | - |
| CuentaInversion | 3,596 | 1,679 | 6,825 | 54 | 1,415 | 249 |
| Custodia | 54,992 | 17,764 | 32,927 | 577 | 4,571 | 1,710 |
| Estadisticas | - | 88 | - | - | - | - |
| Facturacion | 7,636 | 6,165 | 5,719 | 27,455 | 3,462 | 911 |
| FondoMutuo | 7,854 | 4,466 | 15,693 | 4,934 | 2,301 | 524 |
| Homologacion | - | 3,067 | 77,803 | 81,800 | - | - |
| Mesa | 13,511 | 8,474 | 15,210 | 60 | 1,915 | 8,922 |
| Normativa | 26,741 | 19,057 | 81,880 | 45,488 | 308 | 3,682 |
| Orden | 8,737 | 5,476 | 11,379 | 13,173 | 1,495 | 242 |
| Plataforma | 17,467 | 11,712 | 50,043 | 78,339 | 2,243 | - |
| Portafolio | 1,555 | 1,458 | 7,546 | 4,560 | 325 | - |
| Replica | 839 | 27,141 | 231 | 22,823 | - | - |
| SocioNegocio | 15,880 | 5,237 | 28,061 | 7,281 | 456 | 176 |
| Tesoreria | 6,700 | 3,285 | 15,543 | 4,181 | 5,311 | 782 |

## 6. Advertencias

- Homologacion y gran parte de `BD/Scripts` son datos semilla y scripts `SET SCHEMA` (miles de archivos pequenos): inflan el conteo de SQL sin ser logica a migrar.
- Las vistas y rutinas de `BD/` implementan logica de negocio en HANA (156 vistas de calculo segun el brief, decision abierta del usuario).
- Los conteos de produccion/pruebas del legacy son aproximados: los `*Tests` y `gradle_tests` se tratan como pruebas.
- No hay una medida de "lineas a modernizar" exacta: los dos numeros de la seccion 2 son una estimacion del tamano, no del esfuerzo.

Nota de conciliacion: Java 793,585 = 765,652 (modulos del brief) + 21,129 (modulos fuera del brief, produccion y pruebas dentro de BSCL/*/JAVA) + 6,804 lineas en otras carpetas (fuera de `BSCL/*/JAVA/<modulo>`, por ejemplo utilitarios, demos o codigo suelto).
