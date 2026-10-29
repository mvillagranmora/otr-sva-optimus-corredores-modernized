SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete: Para borrar descripción de diccionario               */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::PLA.Diccionario" 
WHERE "CodDiccionario" IN (
'UI_MANTENEDOR_MOTIVOS','UI_ACTIVO','UI_MODAL_MANTENEDOR_MOTIVOS','UI_TIPO_MOTIVO_A','UI_TIPO_MOTIVO_M','UI_INSTRUCCION_VIGENTE',
'UI_CARGA_MASIVA_INSTRUCCIONES','UI_CARGA_MASIVA_ORDENES','UI_NO_HAY_INSTRUCCION_CANCELAR','UI_CANCELAR_INSTRUCCION',
'UI_ES_CANCELABLE', 'UI_SOLO_PERMITIDO_CANCELAR_ESTADOS', 'UI_DIAS_TRANSCURRIDOS_INSUFICIENTE_CANCELAR_INSTRUCCION',
'UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE','UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO',
'UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO', 'UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO',
'UI_ACEPTAR_CANCELACION','UI_RECHAZAR_CANCELACION','UI_ACEPTAR_SOL_CANCELACION',
'UI_RECHAZAR_SOL_CANCELACION','UI_NO_HAY_INSTRUCCIONES_POR_ACEPTAR','ACEPTAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS',
'UI_NO_HAY_INSTRUCCIONES_POR_RECHAZAR','RECHAZAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS',
'INSTRUCCION_ACEPTACION_SOL_CANCELACION','INSTRUCCION_RECHAZO_SOL_CANCELACION',
'UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE','UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE',
'UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE','UI_CARGA_INSTRUCCIONES_MOTIVO_NO_EXISTE', 
'UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE', 'UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE', 
'UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE', 'UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE',
'UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA', 'UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE',
'FOLIO_TRANSACCION_INFORMATIVO','UI_CARGA_INSTRUCCIONES_CORTE_MINIMO_CERO'
); 


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_MANTENEDOR_MOTIVOS', 'CL', 'Mantenedor de Motivos', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_ACTIVO', 'CL', 'Activo', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_MODAL_MANTENEDOR_MOTIVOS', 'CL', 'Motivo', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_TIPO_MOTIVO_A', 'CL', 'Automatico', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_TIPO_MOTIVO_M', 'CL', 'Manual', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_INSTRUCCION_VIGENTE', 'CL', 'Instrucciones Vigentes', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_MASIVA_INSTRUCCIONES', 'CL', 'Carga Masiva de Instrucciones', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_MASIVA_ORDENES', 'CL', 'Carga Masiva de Ordenes', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_NO_HAY_INSTRUCCION_CANCELAR', 'CL', 'Instrucción(es) no son cancelables', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CANCELAR_INSTRUCCION', 'CL', 'Cancelar instrucción', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_ES_CANCELABLE', 'CL', '¿Es cancelable?', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_SOLO_PERMITIDO_CANCELAR_ESTADOS', 'CL', 'No es posible cancelar instrucciones con estados distintos a "Ingresada", "Modificada", "Rechazada" y "No liquidada"', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_DIAS_TRANSCURRIDOS_INSUFICIENTE_CANCELAR_INSTRUCCION', 'CL', 'Días transcurridos son insuficientes para cancelar una instrucción del canal Cliente', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE', 'CL', 'Fecha Vencimiento ::FechaVencimiento:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO', 'CL', 'Valor Rescate ::ValorRescate:: no informado', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO', 'CL', 'Cantidad ::Cantidad:: no informada', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO', 'CL', 'Valor Nominal ::ValorNominal:: no informado', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_ACEPTAR_CANCELACION', 'CL', 'Aceptar Cancelación', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_RECHAZAR_CANCELACION', 'CL', 'Rechazar Cancelación', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_ACEPTAR_SOL_CANCELACION', 'CL', 'Aceptar Solicitud de Cancelación', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_RECHAZAR_SOL_CANCELACION', 'CL', 'Rechazar Solicitud de Cancelación', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_NO_HAY_INSTRUCCIONES_POR_ACEPTAR', 'CL', 'No hay solicitudes para aceptar', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('ACEPTAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS', 'CL', 'No es posible aceptar solicitudes de cancelacion con estados distintos a "Solicitud de Cancelacion"', '');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_NO_HAY_INSTRUCCIONES_POR_RECHAZAR', 'CL', 'No hay solicitudes para rechazar', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('RECHAZAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS', 'CL', 'No es posible rechazar solicitudes de cancelacion con estados distintos a "Solicitud de Cancelacion"', '');

-------------------------

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE', 'CL', 'Cliente ::DocumentoIdentidad:: ::CodigoPortafolio:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE', 'CL', 'Tipo Instrucción ::TipoInstruccion:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE', 'CL', 'Operación ::Operacion:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_MOTIVO_NO_EXISTE', 'CL', 'Motivo para canal ::Canal:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE', 'CL', 'Instrumento ::Nemotecnico:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE', 'CL', 'Intermediario ::Intermediario:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE', 'CL', 'Emisor ::IdEmisor:: no existe', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE', 'CL', 'Condición de liquidación ::CondicionLiquidacion:: no existe', '');

-------------------------
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA', 'CL', 'Fecha trade ::FechaTrade:: es mayor a la fecha de sistema', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE', 'CL', 'Para el nemotécnico ::Nemotecnico:: del emisor ::IdEmisor:: el Valor de ::tipoValor:: ::Valor:: no es multiplo del corte mínimo (::CorteMinimo::)', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('FOLIO_TRANSACCION_INFORMATIVO', 'CL', 'Folio Transacción: ::IdTransaccion::', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_CARGA_INSTRUCCIONES_CORTE_MINIMO_CERO', 'CL', 'Para el nemotécnico ::Nemotecnico:: del emisor ::IdEmisor::, el corte mínimo es cero', '');

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM  "BSCL.Plataforma.BD::PLA.Diccionario" 
WHERE "CodDiccionario" IN (
'UI_MANTENEDOR_MOTIVOS','UI_ACTIVO','UI_MODAL_MANTENEDOR_MOTIVOS','UI_TIPO_MOTIVO_A','UI_TIPO_MOTIVO_M','UI_INSTRUCCION_VIGENTE',
'UI_CARGA_MASIVA_INSTRUCCIONES','UI_CARGA_MASIVA_ORDENES','UI_NO_HAY_INSTRUCCION_CANCELAR','UI_CANCELAR_INSTRUCCION',
'UI_ES_CANCELABLE', 'UI_SOLO_PERMITIDO_CANCELAR_ESTADOS', 'UI_DIAS_TRANSCURRIDOS_INSUFICIENTE_CANCELAR_INSTRUCCION',
'UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE','UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO',
'UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO', 'UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO',
'UI_ACEPTAR_CANCELACION','UI_RECHAZAR_CANCELACION','UI_ACEPTAR_SOL_CANCELACION',
'UI_RECHAZAR_SOL_CANCELACION','UI_NO_HAY_INSTRUCCIONES_POR_ACEPTAR','ACEPTAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS',
'UI_NO_HAY_INSTRUCCIONES_POR_RECHAZAR','RECHAZAR_SOL_CAN_INSTR_ESTADOS_PERMITIDOS',
'INSTRUCCION_ACEPTACION_SOL_CANCELACION','INSTRUCCION_RECHAZO_SOL_CANCELACION',
'UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE','UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE',
'UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE','UI_CARGA_INSTRUCCIONES_MOTIVO_NO_EXISTE', 
'UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE', 'UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE', 
'UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE', 'UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE',
'UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA', 'UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE',
'FOLIO_TRANSACCION_INFORMATIVO','UI_CARGA_INSTRUCCIONES_CORTE_MINIMO_CERO'
); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;