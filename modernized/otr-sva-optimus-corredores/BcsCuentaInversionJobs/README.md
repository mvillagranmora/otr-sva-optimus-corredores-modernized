# BcsCuentaInversionJobs

## Descripción

Los 16 jobs del legacy `BSCL/CuentaInversion/JAVA/BcsCuentaInversionJobs` (más los 4 pasos PAUTA), ejecutados **en proceso** con el contrato del `BcsJob`
legacy: una transacción por corrida (commit / rollback / error), estado y `RutaSalida`. Son una biblioteca sobre `bcs-cuenta-inversion-servicios`
(llamadas directas a `execute(JsonObject)`, sin HTTP entre job y servicio). Sin `@Scheduled` ni modo demonio (RULE-061), sin `main()` de depuración ni
credenciales (RULE-076), sin 3DES (RULE-077). Comportamiento legacy reproducido bug por bug (defectos CIJ-DEF-01..13 en `TRANSFORMATION_NOTES.md`).

## Tecnología

Spring Boot 4.1.1 / Java 25, artifact `cl.bcs:bcs-cuenta-inversion-jobs`, puerto 8075.

## Entrada

`POST /internal/job-call` con la cabecera `X-Internal-Token` (clase legacy del job, `idPrograma` y parámetros). Los 4 pasos PAUTA son beans simples
(`ProcesoPauta`); cómo los invoca el runner PAUTA de la plataforma se decide en una fase posterior.

## Variables de entorno

| Variable | Descripción |
|---|---|
| `HANA_URL`, `HANA_USER`, `HANA_PASSWORD` | Conexión HANA (sin default) |
| `JAVACALL_URL` | Router JavaCall de la plataforma para los colaboradores de otros módulos (`InscribirProcesoBatch`, ...); sin default |
| `INTERNAL_TOKEN` | Secreto de `/internal/**`; vacío rechaza toda llamada |
| `CONFIGURACION_URL` | Obligatorio, sin default (la aplicación no arranca sin ella; timeouts conexión 2 s / lectura 60 s): servicio de configuración que usa `ConsultarSaldoCuentaInversion` en proceso |
| `SERVER_PORT` | Puerto (compose: 8075) |

Pruebas y prueba de equivalencia: ver `src/test/java/cl/bcs/cuentainversion/jobs/README.md` y `TRANSFORMATION_NOTES.md`.
