# Reporte para el DBA: preferencias de usuario del login, HANA QA frente a Postgres QA

Fecha: 2026-11-03. Solo lectura sobre ambas bases (agregados, sin escribir nada). Las bases comparadas son las de QA indicadas en `.env` (`qa01.saphana.*` y `qa01.postgres.*`).

## 1. Qué es y de dónde sale

El home del login (`POST /PLA/bootstrap/home`) arma `BCS_PREFERENCIAS` y `BCS_PREFERENCIAS_GRILLA` desde dos vistas. Cada vista es un `SELECT` directo de una tabla.

| Respuesta | Vista HANA | Tabla HANA | Vista Postgres | Tabla Postgres |
|---|---|---|---|---|
| `BCS_PREFERENCIAS` | `BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario` | `BSCL.Plataforma.BD::PLA.ConfiguracionUsuario` | `bscl.v_pla_pla_consultar_configuracion_usuario` | `bscl.t_pla_pla_configuracion_usuario` |
| `BCS_PREFERENCIAS_GRILLA` | `BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla` | `BSCL.Plataforma.BD::PLA.ConfiguracionGrilla` | `bscl.v_pla_pla_consultar_configuracion_grilla` | `bscl.t_pla_pla_configuracion_grilla` |

Columnas (iguales en ambos motores): usuario `IdConfiguracionUsuario, IdEmpresa, IdUsuario, Llave, Valor`; grilla `IdConfiguracionGrilla, IdEmpresa, Llave, Valor`. **La estructura está bien; lo que falta son filas.**

La `Llave` de usuario tiene la forma `<IdEmpresa>::<IdUsuario>::<nombre de la preferencia>` (por ejemplo `1::<id>::VerDiccionario`).

## 2. Resultado de la comparación

| | HANA QA | Postgres QA |
|---|---|---|
| Filas de preferencias de usuario | 6.355 | 6.486 |
| Usuarios distintos con preferencias | 396 | 406 |
| Empresas | solo la 1 | solo la 1 |
| Filas de la grilla | 10 | 10 |

- **Grilla: sin diferencias** (mismas 10 llaves, empresa 1).
- **Usuario: las dos bases no son la misma foto.** Llaves comunes: 6.229, y en todas el valor es igual. Llaves solo en HANA: **125**. Llaves solo en Postgres: **256**.
- **Usuario de prueba del login** (mismo `IdUsuario` en las dos bases): **11 filas en HANA y 0 en Postgres**. Es la causa de que `BCS_PREFERENCIAS` salga vacío en Postgres.
  Preferencias que faltan: `FiltroRangoDias`, `MinimizarMensajeExito`, `NotificacionesEnLinea`, `OcultarBarraInferior`, `VerDiccionario`, `menuMinimizado`, `permiteCambioTamanoVentana`, `portafolioRecordado`, `recordarCliente`, `recordarClienteAutomatico`, `showBcsMensaje`.
- Usuarios con preferencias en HANA: 396. De ellos, 387 ya tienen alguna fila en Postgres y 9 no tienen ninguna. **21 usuarios** tienen al menos una fila que existe en HANA y falta en Postgres. En ningún caso falta el usuario en `t_pla_acc_usuario` de Postgres (el usuario existe; faltan las preferencias).

## 3. Filas que existen en HANA y faltan en Postgres (125), por preferencia

`VerDiccionario` 20, `FiltroRangoDias` 9, `MinimizarMensajeExito` 9, `NotificacionesEnLinea` 9, `OcultarBarraInferior` 9, `menuMinimizado` 9, `permiteCambioTamanoVentana` 9, `portafolioRecordado` 9, `recordarCliente` 9, `recordarClienteAutomatico` 9, `showBcsMensaje` 9, `portafolioAutomatico` 5, `combobox-condicion-liquidacion_false` 3, `combobox-modalidad-pago_TRA` 2, y una fila cada una de `GRID_grid-custodia-portafolio`, `GRID_grid-movimientos`, `combobox-modalidad-pago_EFE`, `combobox-modalidad-pago_false`, `combobox-tipo-normativa_IdNormativa`.

La lista completa, llave por llave, se obtiene con una consulta por `Llave` entre las dos tablas; no se incluye aquí.

## 4. Filas que existen solo en Postgres (256)

Son preferencias de otros usuarios creadas después de la última carga (mismos nombres de preferencia, más 19 usuarios nuevos por cada una de las 9 preferencias estándar, más `combobox-*` y `GRID_*` propios). No hay que borrarlas por este reporte: se informa para que el DBA decida si la carga debe reemplazar o completar.

## 5. Qué se le pide al DBA

1. Cargar en `bscl.t_pla_pla_configuracion_usuario` las 125 filas que existen en HANA QA y faltan, **en particular las 11 del usuario de prueba**, conservando `IdEmpresa`, `IdUsuario`, `Llave` y `Valor`, y generando `IdConfiguracionUsuario` según la secuencia de Postgres.
2. Aclarar si la réplica HANA a Postgres es continua o una carga puntual (hoy las dos bases difieren en ambos sentidos: 125 y 256 filas).
3. Confirmar que en Postgres la tabla nunca se debe sobrescribir con la de HANA sin conservar las 256 filas propias.

## 6. Otras diferencias de datos vistas en el bootstrap (no son de estructura)

Mismo usuario y misma empresa, comparando las respuestas del home:
- `BCS_AMBIENTE`: `QA` en HANA y `UAT` en Postgres.
- Nombre de la empresa 1: distinto en las dos bases.
- Parámetro de configuración `CobroComisionesMensuales` (producto `PRO`): `NO ACTIVO` en HANA y `ACTIVO` en Postgres.
- Diccionario de mensajes: 23.042 entradas en HANA y 23.036 en Postgres (hay entradas que existen en un solo lado y algunos textos distintos).
- Fecha del sistema devuelta por el procedimiento de fechas: 2026-10-19 en HANA y 2026-10-21 en Postgres (el formato es el mismo).

## 7. Qué no cambia el comportamiento del login

Con las preferencias vacías el login y el home responden 200 y el resto del home es idéntico en estructura; solo se pierden los ajustes de pantalla del usuario (se aplican los valores por defecto).
