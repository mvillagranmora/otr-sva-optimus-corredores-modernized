SELECT
a."IdEmpresa"
,a."IdSolicitudAporteRescate"
,a."TipoSolicitud"
,a."IdTipoMovimientoFondoMutuo"
,a."NumeroCuotas"
,a."MontoMonedaSistema"
,a."ValorUFFechaInversion"
,a."IdAgrupador"
,a."IdSerieFondo"
,a."IdRegimenTributario"
,fon."FondoExtranjero"
,TO_DECIMAL(0,26,8) AS "CostoUF"
,c."RES_IndicadorMLT"
,c."RES_IdRegimenTributario"
,c."RES_FondoExtranjero"
,c."sumCostoUF" AS "CostoTotalRescatado"
,c."sumMontoMonedaSistema" AS "MontoTotalRescatado"
,a."FechaCertificacion"
,par."Valor" as "ValorUF"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" a
INNER JOIN  (
				SELECT sol."IdAgrupador",sol."IndicadorMLT" as "RES_IndicadorMLT",sol."IdRegimenTributario" as "RES_IdRegimenTributario",fon."FondoExtranjero" as "RES_FondoExtranjero", sum(sol."CostoUF") AS "sumCostoUF", sum(sol."MontoMonedaSistema") AS "sumMontoMonedaSistema"
				FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
				INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ser ON ser."IdSerieFondo" = sol."IdSerieFondo"
				INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" fon ON fon."IdFondo" = ser."IdFondo"
				INNER JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" reinv ON reinv."IdSolicitudAporteRescate" = sol."IdSolicitudAporteRescate"
				WHERE "TipoSolicitud"='RES'
				GROUP BY sol."IdAgrupador",sol."IndicadorMLT",sol."IdRegimenTributario",fon."FondoExtranjero"
			) c ON (c."IdAgrupador"=a."IdAgrupador")
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" par ON par."IdParametroFinanciero" = 'UF' AND par."Fecha" = a."FechaValorCuota"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ser ON ser."IdSerieFondo" = a."IdSerieFondo"
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" fon ON fon."IdFondo" = ser."IdFondo"
WHERE a."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND a."Estado" = 'CRT'
AND a."TipoSolicitud" = 'APO'
AND a."IdTipoMovimientoFondoMutuo" = 'APOREINV'
--AND a."IndicadorMLT" = 'S'
AND a."IdPortafolio" IN (:Portafolio)
AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
				WHERE M."IdEmpresa"	= a."IdEmpresa"
				  AND M."IdSolicitudAporteRescate"	= a."IdSolicitudAporteRescate"
				  AND M."TipoSolicitud"	= a."TipoSolicitud"
				  AND M."IdEstado" ='PRO')