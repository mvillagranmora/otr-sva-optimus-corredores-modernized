INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF" ("Fecha","DocumentoIdentidad","TipoDocumentoIdentidad","IdPortafolio","CodigoPortafolio","IdCliente","Nemotecnico","IdInstrumento","IdTipoMovimiento","MontoOperacionFinal")
(SELECT MVTOCMBTE."FechaLiquidacion",Cli."DocumentoIdentidad",Cli."TipoDocumentoIdentidad",Port."IdPortafolio",Port."CodigoPortafolio",MVTOCMBTE."IdCliente" ,
(CASE WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."NemoSVS" 
		ELSE MVTOCMBTE."NemotecnicoInstrumento" END) AS "Nemotecnico",
MVTOCMBTE."IdInstrumento",MVTOCMBTE."IdTipoOperacion" ,
CASE WHEN MVTOCMBTE."IdTipoOperacion" IN ('COMIIF','COMRF','COMIRF')  THEN (CASE WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."ValorRescate" 
		ELSE MVTOCMBTE."Cantidad" END)*(-1) ELSE (CASE WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."ValorRescate" 
		ELSE MVTOCMBTE."Cantidad" END) END AS "Monto" 
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MVTOCMBTE
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteUI" AS Cli ON Cli."IdCliente"= MVTOCMBTE."IdCliente" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = MVTOCMBTE."IdPortafolio" 
AND MVTOCMBTE."FechaLiquidacion"  >:p_FechaInicio AND MVTOCMBTE."FechaLiquidacion"<= :p_FechaFin)