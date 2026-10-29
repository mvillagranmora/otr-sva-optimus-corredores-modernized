SELECT SAR."IdSolicitudAporteRescate"
, CASE WHEN COALESCE(VC."ValorCuota",0) = SAR."ValorCuota" THEN 'ADV' ELSE 'OK' END AS "ResultadoValorCuota"
, COALESCE(VC."ValorCuota",TO_DECIMAL(0,26,8)) AS "ValorCuota"
, COALESCE(VC."FechaCierre",TO_DATE('0001-01-01','YYYY-MM-DD')) AS "FechaCierre"
, SAR."IdSerieFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SF ON SF."IdSerieFondo" = SAR."IdSerieFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" F ON F."IdFondo" = SF."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" VC ON VC."IdSerieFondo" = SAR."IdSerieFondo"
WHERE SAR."TipoSolicitud" = 'APO' AND SAR."Estado" = 'CRT' 
AND SAR."FechaCertificacion" = :FechaCertificacion AND SAR."IdEmpresa" = :IdEmpresa
AND F."MoneyMarket" = 'N'