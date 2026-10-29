SELECT
"TipoMovimiento"
,"IdEmisor"
,"IdMoneda"
,"FondoExtranjero"
FROM
(
SELECT 
CASE WHEN CAR."TipoSolicitud" = 'APO' THEN 'A'
WHEN DESTINO."Switch" = 'S' THEN 'S'
ELSE 'R'  END AS "TipoMovimiento"
,CAR."IdEmisor"
,CAR."IdMoneda"
,FONDO."FondoExtranjero"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = CAR."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" DESTINO ON CAR."IdSolicitudAporteRescate" = DESTINO."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIP ON TIP."IdTipoMovimientoFondoMutuo" = CAR."IdTipoMovimientoFondoMutuo"
WHERE CAR."IdSolicitudAporteRescate" IN (:Solicitudes) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" = 'ING'
AND FONDO."FondoExtranjero" = 'S' AND TIP."GeneraOmnibus" = 'S'
)
GROUP BY 
"TipoMovimiento"
,"IdEmisor"
,"IdMoneda"
,"FondoExtranjero"