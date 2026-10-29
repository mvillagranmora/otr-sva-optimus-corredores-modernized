INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF" ("Fecha","DocumentoIdentidad","TipoDocumentoIdentidad","IdPortafolio","CodigoPortafolio","IdCliente","Nemotecnico","IdInstrumento","IdTipoMovimiento","MontoOperacionFinal")
SELECT mov."FechaOperacion",Cli."DocumentoIdentidad",Cli."TipoDocumentoIdentidad",Port."IdPortafolio",Port."CodigoPortafolio" ,
mov."IdCliente" ,
(CASE WHEN mov."IdClasificacionTipoInstrumento" = 'IF' THEN mov."NemoSVS" 
		ELSE mov."Nemotecnico" END) AS "Nemotecnico",
mov."IdInstrumento" ,mov."IdTipoOperacion",
CASE WHEN mov."IdTipoOperacion" IN ('RETTIT')  THEN (CASE WHEN mov."IdClasificacionTipoInstrumento" = 'IF' THEN mov."ValorRescate" 
		ELSE mov."Cantidad" END)*(-1) ELSE (CASE WHEN mov."IdClasificacionTipoInstrumento" = 'IF' THEN mov."ValorRescate" 
		ELSE mov."Cantidad" END) END AS "MontoCompra"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"  mov
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteUI" AS Cli ON Cli."IdCliente"= mov."IdCliente" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = mov."IdPortafolio" 
WHERE mov."IdTipoOperacion" IN ('INGTIT','RETTIT') AND mov."IdClasificacionTipoInstrumento" IN ('RF','IF') 
AND mov."FechaLiquidacion" >:p_FechaInicio AND mov."FechaLiquidacion" <= :p_FechaFin AND mov."IdEstado"='CRT'