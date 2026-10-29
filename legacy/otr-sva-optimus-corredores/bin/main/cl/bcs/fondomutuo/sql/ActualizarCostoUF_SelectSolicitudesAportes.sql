SELECT
"IdSolicitudAporteRescate"
,"TipoSolicitud"
,"IdTipoMovimientoFondoMutuo"
,"NumeroCuotas"
,"MontoMonedaSistema"
,"ValorUFFechaInversion"
,"IdAgrupador"
,"IdSerieFondo"
,"IdRegimenTributario"
,TO_DECIMAL(0,26,8) AS "CostoUF"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND "Estado" = 'CRT'
AND "TipoSolicitud" = 'APO'
AND "IdTipoMovimientoFondoMutuo" = 'APOREINV'
AND "IndicadorMLT" = 'S'
--AND "FolioContrato" = 786429