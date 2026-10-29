SET SCHEMA "BSCL";  

NRO_ENTRADA;FOLIO_TRANS;FECHA;NEMO;SEC_ORDEN;CANTIDAD;PRECIO;CAN_ASIG_ACUM;PRECIO_PROM;MENSAJE;ESTADO;PROCESADO;IN_SGC;MONEDA;BOLSA;CONDICION_LIQUIDACION;SPONSORING_FIRM;EXEC_ID;ORDER_ID;MERCADO;SUB_MERCADO;PREPAGO;TASA_MAXIMA;TASA_PROPIA;TASA_ADJUDICACION;MEJOR_POSTURA;ID_REFERENCIA;FECHA_VENCIMIENTO;DIVISIBLE;OD;HORA_ADJUDICACION;SEC_ORDER_OD;TIPO_OPERAC;OP_INTERNO;SEC_ORDEN_2;ID_CLIENTE
38;  220005;2017-04-26;AXXION;669;1.250.000;1,32;1.250.000;1,32;;2;N;2017-04-26:23:09:33.335;CLP;XSGO;CN;;ex1711644000270;ord1711644000054;AC;;;0;0;0;;;;;N;;0;C;450;;

INSERT INTO "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada"
SELECT "NRO_ENTRADA" as "IdInterno",*
FROM "BSCL.Orden.BD::DMA.TBRESPUESTAS_2" where "NRO_ENTRADA" = 1476


1405 1407 1408

select * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada";

select * from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" where "IdTipoCanalDistribucion" = 'RUA' and "IdTipoInstrumento" = 'ACN';

SELECT * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada" order by "IdInterno";
CALL "BSCL.Orden.BD.Servicios::ProcesarMensajesNegociacion" (
	p_IdEmpresa						=> 1,
    p_IdUsuario						=> 25,
    p_SegundosZonaHoraria			=> -10800,
    p_out_InformacionContador		=> ?,
	p_out_AdvertenciasContador 		=> ?,
	p_out_ErroresContador 			=> ?);

SELECT * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_Resultado" order by "IdInterno";
SELECT * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_ErroresDetalle" order by "IdInterno";
SELECT * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_AdvertenciasDetalle" order by "IdInterno";
SELECT * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Salida_InformacionDetalle" order by "IdInterno";

SELECT * FROM "BSCL"."BSCL.Orden.BD::DMA.TBSOLICITUDES_2"

SELECT * FROM "BSCL"."BSCL.Orden.BD::DMA.TBRESPUESTAS_2"
DELETE FROM "BSCL"."BSCL.Orden.BD::DMA.TBRESPUESTAS_2"
commit
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "Folio" =  '216494'
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" =  311235

rollback;

select *
FROM "BSCL.Orden.BD::DMA.TBSOLICITUDES" 
select *
FROM "BSCL.Orden.BD::DMA.TBRESPUESTAS" 
commit
insert into "BSCL.Orden.BD::DMA.TBSOLICITUDES_2" 
select * from "BSCL.Orden.BD::DMA.TBSOLICITUDES" 

update "BSCL.Orden.BD::DMA.TBSOLICITUDES_2"  set "PROCESADO" = 'N'
select *
FROM "BSCL.Orden.BD::DMA.TBRESPUESTAS" 
WHERE PROCESADO='N' AND SUBSTRING(IN_RUTEO,1,10)='2017-01-24' AND BOLSA IN ('XSGO','OFS') AND 
MERCADO IN ('AC','AO','CF','CO','IF','MO','OP','RF') AND TO_TIME (SUBSTRING(IN_RUTEO,12,20), 'HH24:MI:SS') <= CURRENT_TIME


select * from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada"
select CURRENT_TIMESTAMP,TO_TIME (SUBSTRING(IN_RUTEO,12,20), 'HH24:MI:SS'),CURRENT_time,* from "BSCL.Orden.BD::DMA.TBSOLICITUDES"
select * from "BSCL.Orden.BD::DMA.TBRESPUESTAS"
select * from "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV"

select * from "BSCL.Orden.BD::MRV.RESUMEN_RV"
SELECT * FROM "BSCL.Orden.BD::ORD.ControlMercado"

delete from "BSCL.Orden.BD::ORD.ControlMercado"

delete from "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada"
commit;


select * from "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
update "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" set "GeneraMovimientoFacturacionAutomatico" = 'S' where "IdTipoInstrumento" = 'ACN'



SELECT * FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" > 3000



SELECT "BSCL.Orden.BD.Secuencias::DMA_IdInterno".NEXTVAL as "IdInterno","NRO_ENTRADA","FOLIO_TRANS","FECHA","NEMO","SEC_ORDEN","CANTIDAD","PRECIO","CAN_ASIG_ACUM",
				"PRECIO_PROM","MENSAJE","ESTADO", "PROCESADO","IN_SGC","MONEDA","BOLSA","CONDICION_LIQUIDACION","SPONSORING_FIRM","EXEC_ID","ORDER_ID","MERCADO","SUB_MERCADO","PREPAGO","TASA_MAXIMA", "TASA_PROPIA",
				"TASA_ADJUDICACION","MEJOR_POSTURA","ID_REFERENCIA","FECHA_VENCIMIENTO","DIVISIBLE","OD","HORA_ADJUDICACION","SEC_ORDER_OD","TIPO_OPERAC","OP_INTERNO","SEC_ORDEN_2","ID_CLIENTE"
				FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
				LEFT JOIN "BSCL.Orden.BD::DMA.TBRESPUESTAS" dma on cm."IdMensaje1" = dma."NRO_ENTRADA" AND cm."IdMensaje2" = dma."FECHA"
				where "TipoMensaje" = 'RESPUESTA_DMA_AGENTE'

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 310718

SELECT * FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
				LEFT JOIN "BSCL.Orden.BD::DMA.TBRESPUESTAS" dma on cm."IdMensaje1" = dma."NRO_ENTRADA" AND cm."IdMensaje2" = dma."FECHA"
				where "TipoMensaje" = 'RESPUESTA_DMA_AGENTE'


UPDATE "BSCL"."BSCL.Orden.BD::ORD.Orden" set "OrdenVigente" = 'S' where "IdEstadoVigenciaOrden" = 'VIG'
commit;


SELECT * FROM "BSCL"."BSCL.Orden.BD::DMA.TBSOLICITUDES" dma 
SELECT * FROM "BSCL"."BSCL.Orden.BD::DMA.TBRESPUESTAS" dma 





rollback;


SELECT * FROM "BSCL"."BSCL.Orden.BD::MRV.PALOS_RV" where "tradeID" = 210019

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "Folio" = '210019'


SELECT *
FROM "BSCL.Orden.BD::ORD.DMA_ProcesarMensajeNegociacion_Entrada_Entrada" ent
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ord on ord."IdOrden" = ent."SEC_ORDEN"
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on tran."Folio" = RTRIM(LTRIM(ent."FOLIO_TRANS")) AND tran."FechaSistemaCreacion" = TO_DATE(ent."FECHA") 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tran."IdTipoOperacion" = tipoope."IdTipoOperacion" AND tipoope."AccionOrden" = CASE ent."TIPO_OPERAC" WHEN 'C' THEN 'COMPRA' WHEN 'V' THEN 'VENTA' END
WHERE ent."ESTADO" IN ('1','2');

NRO_ENTRADA;FOLIO_TRANS;FECHA;NEMO;SEC_ORDEN;CANTIDAD;PRECIO;CAN_ASIG_ACUM;PRECIO_PROM;MENSAJE;ESTADO;PROCESADO;IN_SGC;MONEDA;BOLSA;CONDICION_LIQUIDACION;SPONSORING_FIRM;EXEC_ID;ORDER_ID;MERCADO;SUB_MERCADO;PREPAGO;TASA_MAXIMA;TASA_PROPIA;TASA_ADJUDICACION;MEJOR_POSTURA;ID_REFERENCIA;FECHA_VENCIMIENTO;DIVISIBLE;OD;HORA_ADJUDICACION;SEC_ORDER_OD;TIPO_OPERAC;OP_INTERNO;SEC_ORDEN_2;ID_CLIENTE
204;;2017-01-18;CCU;310.703;100;7.100;0;0;;0;N;2017-01-18:11:43:09.284;CLP;XSGO;CN;;ex1701841;ord1701841;AC;;;0;0;0;;;;;N;;0;C;450;;
