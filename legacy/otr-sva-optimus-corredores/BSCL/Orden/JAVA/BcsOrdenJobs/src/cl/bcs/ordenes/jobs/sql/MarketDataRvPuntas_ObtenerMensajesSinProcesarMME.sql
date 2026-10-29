/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: SH
fecha		: 16/01/2025
***********************************************************/
SELECT
ins."IdInstrumento"
, ins."IdTipoInstrumento"

, coalesce(punta."TIME_STAMP",'0001-01-01T00:00:00.000Z') as "Timestamp"
, ins."Nemotecnico" as "Instrumento"
, ins."IdCondicionLiquidacion"
, coalesce(punta."BUY_QUANTITY",0) as "CantidadCompra"
, coalesce(punta."SALE_QUANTITY",0) as "CantidadVenta"
, coalesce(punta."BUY_PRICE",0) as "PrecioCompra"
, coalesce(punta."SALE_PRICE",0) as "PrecioVenta",
'' as "CorredorCompra"
, '' AS "OperadorDirectoCompra"
, '' as "CorredorVenta"
, '' AS "OperadorDirectoVenta"
FROM "BSCL.Orden.BD::MME.PUNTAS_RV" punta
Right JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = punta."INSTRUMENTS" 
left join  BSCL."BSCL.Plataforma.BD::INS.TipoInstrumento" tin on ins."IdTipoInstrumento" = tin."IdTipoInstrumento"
where tin."IdClasificacionTipoInstrumento" = 'RV'
and ins."IdInstrumento" is not null
ORDER BY punta."TIME_STAMP" desc;