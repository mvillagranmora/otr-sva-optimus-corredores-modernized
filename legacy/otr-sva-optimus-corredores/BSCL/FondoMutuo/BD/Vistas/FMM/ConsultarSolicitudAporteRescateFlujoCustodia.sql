SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia"
AS
SELECT
 'FM' 													AS "IdClasificacionInstrumento" 
,"SerieDeFondo"."IdInstrumento" 						AS "IdInstrumento"
,"Fondo"."IdEmisor" 									AS "IdEmisor"
,"SolicitudAporteRescate"."IdSerieFondo" 				AS "Nemotecnico"
,"Fondo"."CodigoSVS" 									AS "NemoSVS"
,"Fondo"."IdMoneda" 									AS "IdMonedaInstrumento"
,'' 													AS "IdTipoReajuste"
,"Fondo"."FechaConstitucion" 							AS "FechaEmisionInstrumento"
,'0001-01-01' 											AS "FechaVencimientoInstrumento"
,'' 													AS "CodigoIFRS"
,'N' 													AS "EsFungible"
,'COR' 													AS "IdBoveda"
,'' 													AS "IdBolsa"
,"TipoMovimientoFondoMutuo"."IdTipoOperacion"			AS "IdTipoOperacion"
,'' 													AS "IdMesa"
,"SolicitudAporteRescate"."FechaLiquidacion"			AS "FechaLiquidacion"
,"SolicitudAporteRescate"."NumeroCuotas" 				AS "Cantidad"
,0 														AS "ValorRescate"
,"SolicitudAporteRescate"."ValorCuota" 					AS "PrecioCompra"
,0 														AS "TasaCompra"
,"SolicitudAporteRescate"."Monto" 						AS "MontoATasaCompra"
,"SolicitudAporteRescate"."ValorCuota" 					AS "PrecioMercado"
,0 														AS "TasaMercado"
,"SolicitudAporteRescate"."Monto" 						AS "MontoATasaMercado"
,"SolicitudAporteRescate"."IdCliente"					AS "IdCliente"
,"SolicitudAporteRescate"."IdPortafolio"				AS "IdPortafolio"
,"SolicitudAporteRescate"."IdSolicitudAporteRescate"	AS "IdRegistro"
,"SolicitudAporteRescate"."IdSolicitudAporteRescate"	AS "FolioRegistro"
,"SolicitudAporteRescate"."IdEmpresa" 					AS "IdEmpresa"
,"Fondo"."IdTipoInstrumento"							AS "IdTipoInstrumento"
,"SolicitudAporteRescate"."Estado"						AS "IdEstado"
, CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM'
	    			   WHEN 'RES' THEN 'RESCFM'
	    			   ELSE ''
	    			   END AS "IdConcepto"
,"SolicitudAporteRescate"."IdRegimenTributario"
,"SolicitudAporteRescate"."FechaCertificacion"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" AND "SolicitudAporteRescate"."IdEmpresa" = "SerieDeFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieDeFondo"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
