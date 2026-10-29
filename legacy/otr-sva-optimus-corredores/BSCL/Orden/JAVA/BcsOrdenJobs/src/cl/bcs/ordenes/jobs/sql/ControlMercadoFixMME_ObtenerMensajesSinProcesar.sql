SELECT "BSCL.Orden.BD.Secuencias::DMA_IdInterno".NEXTVAL as "IdInterno","TipoMensaje" as "_TipoMensaje","IdMensaje1" as "_IdMensaje1","IdMensaje2" as "_IdMensaje2",
COALESCE("NRO_ENTRADA",'') AS "NRO_ENTRADA", "SIDE_TRADE_ID" as "FOLIO_TRANS","FECHA","NEMO","SEC_ORDEN","CANTIDAD","PRECIO","CAN_ASIG_ACUM",COALESCE("REJECT_TEXT",'') AS "MENSAJE",
COALESCE("ESTADO",'-1') AS "ESTADO", "PROCESADO","IN_SGC","MONEDA",COALESCE("BOLSA",'XSGO') AS "BOLSA","CONDICION_LIQUIDACION","EXEC_ID","ORDER_ID","MERCADO",
"FECHA_VENCIMIENTO","TIPO_OPERAC","SEC_ORDEN_2","ID_CLIENTE","SECUENCIA","ESOPTIMUS"

FROM (

	SELECT  cm.*,dma.*,CASE WHEN (dma."SEC_ORDEN" = dmasol."SEC_ORDEN") THEN 'S' ELSE 'N' END AS "ESOPTIMUS"
	FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
	LEFT JOIN "<NOMBRE_TABLA>" dma on cm."IdMensaje1" = CAST(dma."SECUENCIA" AS NVARCHAR(40)) AND cm."IdMensaje2" = (dma."FECHA" || dma."IN_SGC")
	LEFT JOIN (select distinct "SEC_ORDEN" from "BSCL.Orden.BD::MME.TBSOLICITUDES_FIX5SP2" where "TIPO_ORDEN" = 'SA') dmasol on (dma."SEC_ORDEN" = dmasol."SEC_ORDEN" )
	--LEFT JOIN "BSCL.Orden.BD::DMA.TBRESPUESTAS" dma on cm."IdMensaje1" = CAST(dma."NRO_ENTRADA" AS NVARCHAR(40)) AND cm."IdMensaje2" = dma."FECHA"
	where "TipoMensaje" = :TipoMensaje AND "Procesado" IN ('N')
	UNION
	SELECT *, '' as "ESOPTIMUS"
	FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
	LEFT JOIN "<NOMBRE_TABLA>" dma on cm."IdMensaje1" = CAST(dma."SECUENCIA" AS NVARCHAR(40)) AND cm."IdMensaje2" = (dma."FECHA" || dma."IN_SGC")
	--LEFT JOIN "BSCL.Orden.BD::DMA.TBRESPUESTAS" dma on cm."IdMensaje1" = CAST(dma."NRO_ENTRADA" AS NVARCHAR(40)) AND cm."IdMensaje2" = dma."FECHA"
	where "TipoMensaje" = :TipoMensaje AND "Procesado" IN ('R')
)
ORDER BY "FechaIngresoRegistro" ASC
