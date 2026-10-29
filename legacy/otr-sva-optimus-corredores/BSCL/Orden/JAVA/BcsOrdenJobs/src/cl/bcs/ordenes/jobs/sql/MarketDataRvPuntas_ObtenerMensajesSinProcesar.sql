/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT ins."IdInstrumento",ins."IdTipoInstrumento",

punta."Timestamp",punta."Instrumento",punta."CondicionLiquidacion",punta."CantidadCompra",punta."CantidadVenta",punta."PrecioCompra",punta."PrecioVenta",punta."CorredorCompra",
punta."OperadorDirectoCompra",punta."CorredorVenta",punta."OperadorDirectoVenta"

FROM "BSCL.Orden.BD::MRV.PUNTAS_RV" punta
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = punta."Instrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
WHERE "Timestamp" is not null and "IdInstrumento" is not null
AND COALESCE(tins."IdClasificacionTipoInstrumento", '') = CASE WHEN COALESCE(tins."IdClasificacionTipoInstrumento", '') <> '' THEN 'RV' ELSE '' END
ORDER BY punta."Timestamp" desc;