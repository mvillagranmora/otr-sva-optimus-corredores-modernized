SELECT
"IdSolicitudAporteRescate"
,"TipoSolicitud"
,"IdTipoMovimientoFondoMutuo"
,"CostoUF"
,"NumeroCuotas"
,"MontoMonedaSistema"
,"ValorUFFechaInversion"
,"IdAgrupador"
,"IdSerieFondo"
,"IdRegimenTributario"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
AND "Estado" = 'CRT'
AND "TipoSolicitud" = 'RES'
--AND "FolioContrato" = 786429