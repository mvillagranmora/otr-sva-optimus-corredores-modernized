SELECT
 r."IdEmpresa"
,r."IdSolicitudAporteRescate"
,r."TipoSolicitud"
,r."IdTipoMovimientoFondoMutuo"
,r."CostoUF"
,r."NumeroCuotas"
,r."MontoMonedaSistema"
,r."ValorUFFechaInversion"
,r."IdAgrupador"
,r."IdSerieFondo"
,r."IdRegimenTributario"
,c."sumEGRCostoInversionEnFactorAjuste" AS "CostoTotalRescatado"
,r."FechaCertificacion"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" r 
INNER JOIN  (
			SELECT "EGRIdOperacion", sum("EGRCostoInversionEnFactorAjuste") AS "sumEGRCostoInversionEnFactorAjuste"
			FROM "BSCL.Custodia.BD::CUS.Cartera" WHERE  "IdClasificacionTipoInstrumento" = 'FM'
			GROUP BY "EGRIdOperacion"
			) c ON (c."EGRIdOperacion"=r."IdSolicitudAporteRescate")
WHERE r."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND r."Estado" = 'CRT'
AND r."TipoSolicitud" = 'RES'
AND r."IdPortafolio" IN (:Portafolio)
AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
				WHERE M."IdEmpresa"	= r."IdEmpresa"
				  AND M."IdSolicitudAporteRescate"	= r."IdSolicitudAporteRescate"
				  AND M."TipoSolicitud"	= r."TipoSolicitud"
				  AND M."IdEstado" ='PRO')
--and r."IdSolicitudAporteRescate"=13270102;
