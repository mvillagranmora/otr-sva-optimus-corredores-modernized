# BcsCrystalReports: plan de aislamiento (servicio en cuarentena)

Estado: PLANIFICADO (decision del usuario 2026-10-21: se ejecuta cuando terminen los Jobs de la Fase 2).

## Alcance (decision del usuario, vinculante)

- Solo AISLAR el componente Crystal Reports del monolito como microservicio. **No se modifica su funcionamiento ni se moderniza**: mismo motor (`ReportDocument`, SDK Crystal Reports JRC 14.1), mismo comportamiento de renderizado, Java 11 y Spring Boot 2.7.18 se mantienen (el SDK no corre en Java 12+).
- Los **reportes `.rpt` se copian tal cual** (binarios sin tocar), son parte de lo que no se modifica.
- Defectos o rarezas del legacy se mantienen y se listan (regla del proyecto).

## Estado de partida (medido 2026-10-21)

| Pieza | Estado |
|---|---|
| Motor `ReportDocument` | portado, mismos 29 metodos publicos que el legacy (`BcsPlataformaReporteApi`), 938 vs 1126 lineas |
| `ListMapResultSet`, `JsonResultSet`, `SubreportDocument` | portados |
| REST `POST /reports/generate`, `GET /reports/health` | subconjunto: UNA fila plana `beanData` (todo texto), una imagen, un codigo `pdf417` |
| Gestion de plantillas (upload/update/rpt/file de `CallReportServlet` y `TemplateServlet`) | no existe |
| Subinformes, filas multiples, campos tipados (number/date/datetime/string/blob), varias imagenes/barras | no expuestos por REST |
| Token interno | no existe |
| Tests | 3 (1 `@Disabled`), nunca se renderizo un `.rpt` |
| Plantillas `.rpt` | no copiadas |

## Trabajo

1. **Copiar las plantillas `.rpt`** a `BcsCrystalReports/templates/<Dominio>/` (misma estructura que `BSCL/<Dominio>/CrystalReports/Templates`), byte a byte, con manifiesto SHA-256 verificado y test de integridad. Hay **153** `.rpt` reales de Crystal en `BSCL/*/CrystalReports` (APV 3, Contabilidad 7, CuentaInversion 2, Custodia 19, Facturacion 5, FondoMutuo 18, Mesa 55, Normativa 35, Orden 2, Tesoreria 7, SocioNegocio 0). Un conteo ingenuo de `*.rpt` en todo el legacy da 211, pero 58 de ellos NO son Crystal: son 29 archivos de datos CSV (separados por `;`, p.ej. `PALOS_MBI_*.rpt`) en `BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/utils` y su copia en `bin/main`. 153 + 29 = 182, cercano a las ~184 que estimo el usuario: por confirmar si los ~184 incluyen esos CSV o plantillas que viven solo en HANA `BSCL.Report::Template`. Las 111 consultas `.sql` que acompanan a las plantillas (y otros archivos de esas carpetas) se copian tambien como referencia.
2. **Contrato REST fiel al legacy**: reproducir lo que hacia `CallReportServlet` en la parte de renderizado (datos como arreglo de filas, tipos number/date/datetime/string/blob, parametros image/pdf417 multiples, subinformes, formatos pdf/excel/word/rtf/txt con extension y content-type), sin cambiar la semantica. La obtencion de datos desde servicios (`getServiceData`) sigue siendo responsabilidad de la plataforma, no del sidecar.
3. **Gestion de plantillas** equivalente a las acciones update/upload/rpt/file del legacy (cargar/leer `.rpt` en `BSCL.Report::Template`), mismo comportamiento.
4. **Seguridad minima de aislamiento**: token interno (`X-Internal-Token`, lista de permitidos fail-closed como los demas servicios), sin credenciales en el repo, JARs privativos fuera de git (ya), health.
5. **Pruebas y evidencia**: golden de renderizado grabado ejecutando el `ReportDocument` legacy y el sidecar sobre las mismas plantillas y datos (comparar bytes del PDF/TXT con mascaras de fecha/metadatos), canarios, entrada fresca, `test-runs.json`, `proof_pack.py`. Necesita una instancia con las plantillas en HANA/H2 de staging y el SDK (Java 11, JARs en `~/.m2`).
6. **Notas** `TRANSFORMATION_NOTES.md` (cuarentena, que no se modernizo, plantillas copiadas con manifiesto, defectos, no probado).

## Criterios de terminado

- Las ~N plantillas copiadas con SHA-256 identico al origen.
- Endpoints equivalentes a los usados por la plataforma; todos los formatos.
- Renderizado de al menos una plantilla por dominio igual byte a byte (o dentro de mascaras documentadas) al del legacy.
- Veredicto del pack: PROVEN o PARTLY PROVEN con las razones listadas.

## Riesgos

- El SDK Crystal solo corre en Java 11 y requiere AWT headless; sin las plantillas en HANA de staging el renderizado real no se puede probar localmente.
- Spring Boot 2.7.18 sin soporte: aceptado por ser servicio en cuarentena.
- Licenciamiento de los JARs privativos (no versionarlos).

## Actualizacion 2026-10-21

- Plantillas copiadas byte a byte: `BcsCrystalReports/templates/<Dominio>/` (264 archivos, 153 `.rpt` + 111 `.sql`, 27 MB) con `MANIFEST.sha256.json`.
- Sondeo de factibilidad: `CRYSTAL_SPIKE.md` (el motor legacy y el del sidecar renderizan localmente bajo JDK 11 sin pantalla; el sidecar necesita primero: `ListMapResultSet` real, 5 jars, fuentes en `lib/fonts`, `-Djava.awt.headless`, `-Dfile.encoding=UTF-8`).
- **Decision del usuario (alcance de la prueba de equivalencia):** renderizar **2 plantillas por dominio** (10 dominios con `.rpt`: APV, Contabilidad, CuentaInversion, Custodia, Facturacion, FondoMutuo, Mesa, Normativa, Orden, Tesoreria; SocioNegocio no tiene), con **datos deducidos de su `.sql`**, en todos los formatos (pdf, excel, word, rtf, txt), motor legacy sin modificar contra motor del sidecar, con las mascaras documentadas (titulo del PDF con el nombre temporal, hora de impresion en el flujo PDF, 6 bytes de hora en Excel, `\creatim` en RTF). Las 153 plantillas quedan copiadas y verificadas por SHA-256, pero solo ~20 se renderizan en la prueba: el resto queda como "no probado" en `leftOut`.
- Orden de trabajo: (1) arreglar el motor del sidecar [en curso], (2) arnes de renderizado y goldens del legacy para las ~20 plantillas, (3) contrato REST fiel (datos en arreglo con esquema por campo, subinformes, imagenes/pdf417 multiples, plantillas upload/update/rpt, token interno, mapeo de errores 404/500 y nombre de archivo), (4) pruebas, canarios, notas, revision de arquitectura, verificacion, (5) redirigir despues los ~29 llamadores en proceso (Normativa, Mesa, FondoMutuo, Facturacion, Custodia) hacia el sidecar cuando se migren sus modulos.

## Conteo de `.rpt` revalidado (2026-10-21, decision del usuario)

Recorrido completo del legacy: 211 archivos `.rpt` = 153 plantillas Crystal reales (formato OLE2, `BSCL/*/CrystalReports/Templates`, todas copiadas y con SHA-256 identico) + 29 archivos de datos CSV en `BSCL/Orden/JAVA/BcsOrdenServicios/src/cl/bcs/ordenes/utils` + su copia identica en `bin/main/cl/bcs/ordenes/utils`. Los 58 archivos de datos NO son plantillas y se dejan FUERA de la copia (decision del usuario). Solo hay un nombre repetido (`certificado31.rpt`, en dos dominios).
Contraste con HANA (`Result_1.csv`, 55.964 filas): solo 95 filas con archivo; 82 de las 153 plantillas del repositorio no aparecen en esa HANA; 21 filas de HANA no tienen equivalente en el repositorio (`CRYSTAL_HANA_ONLY_TEMPLATES.csv`, pendientes de exportar por quien tenga acceso); 55.714 filas `EMISORES` sin archivo (datos ajenos a los reportes).
