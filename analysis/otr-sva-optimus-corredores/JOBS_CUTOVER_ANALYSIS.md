# Corte por tipo de programa: datos reales de HANA QA (2026-10-21)

Fuente: consultas de solo lectura (`analysis/otr-sva-optimus-corredores/tools/HanaReadOnlyQueries.java`, resultados en `hana_qa_results/`), HANA QA, esquema BSCL. Sin cambios en la BD.

## Hechos

- **311 tipos de programa** (`BAT.TipoPrograma`): 308 de clase JAVA y 3 JOB; `MaximoIntentos` = 1 en 301 tipos y 3 en 10.
- **`Nodo` de los tipos:** (vacio): 263, A: 1, D: 34, J: 1, K: 2, M: 1, W: 3, X: 3, Y: 1, Z: 2. La inmensa mayoria de los tipos y de los programas usa el nodo vacio.
- **Estados de los programas:** ASI/S: 2537; ERR/N: 3051; ERR/S: 92915; PEN/N: 268138; PEN/S: 256; TER/N: 10; TER/S: 385213. `TER/S` es lo normal (el reclamo pone `Bloqueo='S'` y el final no lo libera): confirma el diseno del reclamo y del estado final de H4.
- **Todos los tipos de los jobs ya migrados** (Tesoreria, APV, CuentaInversion, Contabilidad: 29 tipos) tienen `Nodo` vacio, salvo `SALDO_LIBRO_INVENTARIO` (`W` en el tipo; sus programas usan `W` (267.973) y vacio (91.839)).
- El Job Manager legacy selecciona con `P.Nodo = ?` y, opcionalmente, `P.IdTipoPrograma = ?` (igualdad con UN tipo; `TransactionManager.readProgramm`): **no existe forma de EXCLUIR tipos**. Un manager arrancado para el nodo vacio sin tipo procesa los 311 tipos de ese nodo.

## Consecuencia (conflicto entre las reglas del proyecto)

Las reglas "sin cambios en la BD", "legacy sin modificar" y "el legacy nunca interactua con el modernizado" no se pueden cumplir a la vez para el corte de un tipo: para que el Job Manager legacy deje de tomar los programas de un tipo migrado hay que (a) cambiar un dato de configuracion (`BAT.TipoPrograma.Nodo` del tipo, o `BAT.Agenda.habilitado`), (b) detener ese manager legacy para todo el nodo (arrastra los otros ~280 tipos, que aun no estan migrados), o (c) arrancar un manager legacy por cada tipo no migrado. Sin una de esas, ambos motores compiten por los mismos programas `PEN`/`N` y, como el reclamo legacy no es atomico, puede haber doble ejecucion.

## Tipos de programa de los modulos de Jobs ya modernizados

| IdTipoPrograma | Clase | Nodo del tipo | MaximoIntentos | Programas por Nodo |
|---|---|---|---:|---|
| ACTUALIZAR_SALDOS_REPORTE_ANTIGUEDAD | ActualizarSaldosReporteAntiguedadJob | (vacio) | 1 | sin programas |
| ACTUALIZAR_SALDO_CARGO_ABONO | ActualizarSaldoCargoAbonoJob | (vacio) | 1 | '': 80871 |
| ACTUALIZAR_SALDO_LIBRO_INVENTARIO | LibroInventarioJob | (vacio) | 3 | sin programas |
| ASIENTO_SAP | InterfazContable | (vacio) | 1 | '': 5870 |
| CARGA_INICIAL_SALDOS_REPORTE_ANTIGUEDAD | CargaInicialSaldosReporteAntiguedadJob | (vacio) | 1 | sin programas |
| CARGA_MASIVA_INGRESO_EGRESO_DAP | BcsJobCargaMasivaIngresoEgresoDAPFFMM | (vacio) | 1 | sin programas |
| CARGA_MASIVA_INGRESO_EGRESO_FFMM | BcsJobCargaMasivaIngresoEgresoDAPFFMM | (vacio) | 1 | sin programas |
| CARGA_MASIVA_PREVIRED | BcsJobCargaMasivaPrevired | (vacio) | 1 | '': 539 |
| GENERAARCHIVOANEXO16 | GenerarArchivoCuentasPorCobrarAnexo16Job | (vacio) | 1 | sin programas |
| GENERAARCHIVOCARGOABONOCTA | GenerarArchivoCargoAbonoJob | (vacio) | 1 | '': 9137 |
| GENERAARCHIVOSALDOSCTA | GenerarArchivoSaldoCuentasJob | (vacio) | 1 | '': 11327 |
| GENERAR_ARCHIVO_CARGOS | GenerarArchivoCargos | (vacio) | 1 | '': 767 |
| GENERAR_ARCHIVO_PAGO | GenerarArchivoPago | (vacio) | 1 | sin programas |
| GENERAR_CENTRALIZACION_MASIVA | CentralizaMultiSeleccionJob | (vacio) | 1 | sin programas |
| GENERAR_INFORME_PATRIMONIO_DIARIO | BcsJobGenerarInformePatrimonioDiario | (vacio) | 1 | '': 405 |
| GENERAR_INFORME_RESUMEN_COBRO | BcsJobGenerarInformeResumenCobro | (vacio) | 1 | '': 15 |
| GENERAR_REPORTE_MOV_INGRESO_EGRESO | GenerarArchivoMovimientosIngresoEgresoJob | (vacio) | 3 | sin programas |
| GENERAR_REPORTE_SALDO_POSITIVO_NEGATIVO | GenerarArchivoSaldoPositivoNegativoJob | (vacio) | 3 | sin programas |
| GENERA_EXCEL_CLIENTES_POR_PAGAR_COBRAR | GenerarArchivoClientesPorPagarYCobrarJob | (vacio) | 1 | '': 7 |
| GenerarReporteAcreedores | GenerarReporteAcreedoresJob | (vacio) | 1 | sin programas |
| GenerarReporteAntiguedadDeudaBolsaCTA | GenerarReporteAntiguedadDeudaBolsaCTAJob | (vacio) | 1 | sin programas |
| GenerarReporteAntiguedadDeudaCTA | GenerarReporteAntiguedadDeudaCTAJob | (vacio) | 1 | sin programas |
| INFORME_OPERACIONES_CLIENTES_REL | GenerarInformeClienteRelacionadoBatch | (vacio) | 1 | sin programas |
| INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL | IngresarOrdenFMBonificacionFiscal | (vacio) | 1 | '': 23206 |
| INGRESAR_ORDEN_RV_BONIFICACION_FISCAL | IngresarOrdenRVBonificacionFiscal | (vacio) | 1 | '': 1029 |
| PATRIMONIO_CAJA_JOB | GuardarPatrimonioCajaJob | (vacio) | 1 | '': 106 |
| RENDICION_PAC | BcsJobProcesarDistribucion | (vacio) | 1 | '': 375 |
| SALDO_LIBRO_INVENTARIO | LibroInventarioJob | W | 1 | '': 91839, W: 267973 |
| VALIDA_SALDO_CUENTA_INVERSION | GenerarArchivoDiferenciaSaldoJob | (vacio) | 1 | '': 11 |

Tipos sin ningun programa historico en QA: se pueden migrar sin riesgo de competencia hoy (no hay programas), pero el riesgo reaparece en cuanto se agenden.

## Alternativas sin cambios en la BD, en sus datos ni en el legacy (decision del usuario 2026-10-21: no se puede cambiar la BD ni sus datos)

Descartadas por violar las reglas: cambiar `TipoPrograma.Nodo` o `Agenda.habilitado` (datos de configuracion), quitar clases del despliegue legacy (marcaria los programas `ERR` con "No encuentro la clase"), y reservar programas con marcas en `Resultado`/`Intento` antes de que el legacy los vea (el propio Job Manager legacy crea los programas desde la agenda y los selecciona en su siguiente vuelta, en milisegundos: no hay ventana para adelantarse, y su reclamo no es atomico, asi que una carrera deja ejecuciones dobles).

Conclusion tecnica: mientras el Job Manager legacy siga procesando la agenda y la cola, NO existe un corte seguro por tipo de programa sin tocar datos o codigo. El corte de los jobs es un evento por ENTORNO, no por tipo:

1. **Motor moderno inerte por defecto.** `BcsJobManager` (moderno) solo reclama programas de los tipos listados en una configuracion explicita (`tipos-habilitados`, vacia por defecto, mismo patron que `plataforma.javacall.clases-habilitadas`): desplegarlo junto al legacy no ejecuta nada.
2. **Verificacion en QA/staging con el Job Manager legacy detenido** (el entorno lo decide quien opera esa base): ahi se activan los tipos migrados y se compara.
3. **Produccion: un solo corte global** cuando todos los tipos que procesa el manager legacy del nodo esten migrados (Fases 3 a 5): se detiene el manager legacy y se habilita el moderno para todos los tipos. Hasta entonces los jobs de produccion siguen ejecutandose con las clases legacy (que operan sobre la misma BD), sin duplicidad.
4. **Excepcion posible, solo operativa:** si en produccion el Job Manager legacy se arranca con un `IdTipoPrograma` explicito por proceso (el filtro legacy `readProgramm` acepta UN tipo por manager), un tipo migrado se puede retirar sin tocar datos simplemente no arrancando su proceso. Pendiente de confirmar: los argumentos de arranque reales (nodo y tipo) de los procesos legacy en produccion.
