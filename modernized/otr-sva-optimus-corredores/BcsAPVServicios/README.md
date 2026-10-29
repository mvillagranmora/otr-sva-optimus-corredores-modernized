# BcsAPVServicios

## Descripcion

Servicio de **Ahorro Previsional Voluntario (APV)**: contratos SAAPV (familia A), traspasos (familia B) y recaudacion, PAC, Previred, bonificaciones y distribucion (familia C). Reemplaza las 66 clases JavaCall del legado (64 invocables) por casos de uso tipados; el comportamiento del legado se conserva, defectos incluidos (cada uno esta marcado `LEGACY DEFECT kept` en el codigo y listado en `TRANSFORMATION_NOTES.md`).

## Tecnologia y modulos

- Spring Boot 4.1.1 / Java 25, puerto 8083, artefacto `cl.bcs:apv-service`, arquitectura hexagonal.
- Tres modulos Maven (se compilan juntos desde `otr-sva-optimus-corredores/pom.xml`):
  - `bcs-apv-api` (`BcsAPVApi`): records tipados de entrada/salida y las interfaces `cl.bcs.apv.api.casouso.*` (`R ejecutar(E)`).
  - `bcs-apv-javacall-adapter` (`BcsApvJavaCallAdapter`): traduccion JSON legado <-> records, registro de clases legado y pipeline en proceso, para el router de plataforma.
  - `apv-service` (este modulo): un `@Service` por caso de uso (`application/{contratos,traspasos,recaudacion}`), puertos de salida (`domain/port/out`), adaptadores JDBC/HTTP (`infrastructure`) y REST.
- Todos los casos de uso estan tipados (no quedan stubs con parametros `Object`) y son `@Transactional(rollbackFor = Exception.class)`.

## Configuracion (variables de entorno)

| Variable | Descripcion | Default |
|---|---|---|
| `HANA_URL` | URL JDBC de SAP HANA | sin default (obligatoria) |
| `HANA_USER` | Usuario HANA | sin default (obligatoria) |
| `HANA_PASSWORD` | Contrasena HANA | sin default (obligatoria) |
| `JAVACALL_URL` | Router JavaCall de plataforma para los colaboradores de otros modulos (`POST {JAVACALL_URL}/internal/java-call`) | sin default (obligatoria; el servicio no arranca sin ella) |
| `INTERNAL_TOKEN` | Secreto compartido de las llamadas servicio a servicio | vacio = falla cerrado: se rechaza todo salvo los health probes |
| `APV_ARCHIVOS_DIR` | Carpeta base donde el modulo escribe archivos (`AlmacenArchivosLocal` confina las escrituras a ella) | vacio = salida de archivos deshabilitada |
| `SERVER_PORT` | Puerto HTTP | `8083` |

El despliegue debe fijar `JAVA_TOOL_OPTIONS=-Duser.timezone=UTC -Duser.language=en` (el legado calculaba fechas y formatos con esa zona e idioma); `docker-compose.yml` ya lo hace.

## API REST

Tres controllers (`infrastructure/web`) exponen los 64 casos de uso invocables bajo `/apv/...`, con JSON tipado:

| Controller | Rutas | Familia |
|---|---|---|
| `ContratosController` | `/apv/contratos/**`, `/apv/portafolios/custodia`, `/apv/portafolios/preferencia-pago`, `/apv/perfiles/asociacion`, `/apv/recaudaciones/asignacion-contrato`, `/apv/recaudaciones/validacion-asignacion-contratos` | A |
| `TraspasosController` | `/apv/traspasos/**` | B |
| `RecaudacionController` | `/apv/recaudaciones/**`, `/apv/pac/**`, `/apv/bonificaciones/**`, `/apv/bancos-previred/**`, `/apv/portafolios/replica` | C |

El mapa completo clase legado -> caso de uso -> ruta esta en `TRANSFORMATION_NOTES.md`.

- **Autenticacion:** `TokenInternoFilter` es una lista blanca: todo requiere el header de token interno excepto `/actuator/health` y `/actuator/info`.
- **Errores:** `ApvExceptionHandler` mapea en un solo lugar las excepciones del legado (la clase de excepcion lanzada se conserva; un `UndeclaredThrowableException` de un proxy Spring se desenvuelve con `Sneaky.causa`).
- **JavaCall:** las clases legado se invocan por el pipeline en proceso del adapter (`ApvJavaCallPipelineEnProceso`), no por un endpoint propio de este servicio.
- **Health:** `/actuator/health` (puerto 8083).

## Dependencias de runtime

| Servicio | Via | Uso |
|---|---|---|
| SAP HANA | `HANA_URL` | base de datos principal (`BSCL.*`) |
| Router JavaCall de plataforma | `JAVACALL_URL` | colaboradores de otros modulos (`ColaboradoresJavaCall`) |

## Build y pruebas

Desde `modernized/otr-sva-optimus-corredores`:

```
mvn -o -pl :bcs-apv-api,:bcs-apv-javacall-adapter,:apv-service clean install
```

1024 pruebas, 0 fallas: 939 casos de caracterizacion ejecutados contra H2 (equivalencia con el legado, `4149 of 4149 same`), mas pruebas de cableado real (`ApvCableadoRealTest`), proxies Spring y adaptadores.
La imagen Docker (`BcsAPVServicios/Dockerfile`, contexto = raiz del repo modernizado) instala primero `BcsUtils`, `bcs-apv-api` y `bcs-apv-javacall-adapter` y luego empaqueta el servicio.

## Notas

- Las tablas temporales de sesion (`CREATE LOCAL TEMPORARY TABLE`) se eliminan tambien cuando el bloque falla (`try/finally` con DROP que ignora errores).
- RULE-076: ningun `main()` fuera de `ApvApplication.java`; credenciales hardcodeadas del legado eliminadas.
