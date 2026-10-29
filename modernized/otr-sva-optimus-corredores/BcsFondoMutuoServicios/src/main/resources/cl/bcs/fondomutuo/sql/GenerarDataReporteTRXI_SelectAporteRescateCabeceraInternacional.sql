SELECT
'' AS "TipoMovimiento"
,"IdEmisor"
,"IdMoneda"
,"FondoExtranjero"
FROM
(
SELECT 
CAR."IdEmisor"
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
"IdEmisor"
,"IdMoneda"
,"FondoExtranjero"