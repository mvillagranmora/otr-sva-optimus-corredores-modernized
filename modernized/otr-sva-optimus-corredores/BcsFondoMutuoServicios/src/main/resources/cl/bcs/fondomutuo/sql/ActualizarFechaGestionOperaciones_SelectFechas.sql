SELECT
SAR."IdSolicitudAporteRescate"
,SAR."Estado"
,SAR."EstadoLiquidacion"
,SAR."FolioOmnibus"
,SAR."IdTipoMovimientoFondoMutuo"
,SAR."IdCliente"
,TO_NVARCHAR(SAR."FechaLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion"
,TO_NVARCHAR(SAR."FechaCertificacion",'YYYY-MM-DD') AS "FechaCertificacion"
,TO_NVARCHAR(SAR."FechaAceptacion",'YYYY-MM-DD') AS "FechaAceptacion"
,TO_NVARCHAR(SAR."FechaValorCuota",'YYYY-MM-DD') AS "FechaValorCuota"
,SDF."IdInstrumento"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SAR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SDF ON (SDF."IdSerieFondo" = SAR."IdSerieFondo" )
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
AND "Estado" IN ('ING', 'CRT', 'PEN')
AND "EstadoLiquidacion" IN ('ING', 'LIQ');