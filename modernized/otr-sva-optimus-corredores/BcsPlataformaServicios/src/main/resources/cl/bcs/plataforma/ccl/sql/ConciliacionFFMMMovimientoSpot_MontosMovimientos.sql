SELECT 
SOL."IdSolicitudAporteRescate"
,OPE."IdTipoOperacion" AS "IdTipoOperacionSpot"
,SOL."Monto" AS "MontoSolicitud"
,OPE."MontoMonedaPrincipal" AS "MontoMonedaPrincipalSpot"
,OPE."MontoMonedaSecundaria" AS "MontoMonedaSecundariaSpot"
,SOL."IdMoneda" AS "IdMonedaSolicitud"
,SOL."IdMonedaPago" AS "IdMonedaPago"
,OPE."IdMonedaPrincipal" AS "IdMonedaPrincipal"
,OPE."IdMonedaSecundaria"  AS "IdMonedaSecundaria"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPE ON OPE."IdOperacionSpotOrigen" = SOL."IdOperacionSpot"
WHERE SOL."IdCarga" = 0 AND SOL."IdMonedaPago" != '' AND SOL."IdMonedaPago" != SOL."IdMoneda" AND OPE."Estado" NOT IN ('MOD','ANU')
AND SOL."Monto" != CASE WHEN SOL."IdMoneda" = OPE."IdMonedaPrincipal" THEN OPE."MontoMonedaPrincipal"
						WHEN SOL."IdMoneda" = OPE."IdMonedaSecundaria" THEN OPE."MontoMonedaSecundaria"
						ELSE 0 END;